package com.benayed.mailing.assets.service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoKey;
import com.benayed.mailing.assets.entity.GroupEntity;
import com.benayed.mailing.assets.entity.SuppressionInfoEntity;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.FilteredGroupInfoRepository;
import com.benayed.mailing.assets.repository.GroupRepository;
import com.benayed.mailing.assets.repository.SuppressionInfoRepository;
import com.benayed.mailing.assets.utils.DataMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class MailingDataService {
	
	private DataItemRepository dataItemRepository;
	private DataMapper dataMapper;
	
	private SuppressionInfoRepository suppressionInfoRepository;
	private FilteredGroupInfoRepository filteredGroupInfoRepository;
	private GroupRepository groupRepository;
	private SuppressionService suppressionService;

	
	@Transactional
	public void deleteSuppressionInformations(Long suppressionId) {
		Assert.notNull(suppressionId, "Cannot delete Suppression Information with null suppression");
		try {
			SuppressionInfoEntity suppressionInfoToDelete = suppressionInfoRepository.findBySuppressionId(suppressionId)
					.orElseThrow(() -> new NoSuchElementException("No suppressionInfo found with the given suppressionId : " + suppressionId));
			
			filteredGroupInfoRepository.deleteAllBySuppressionInfoId(suppressionInfoToDelete.getId());
			suppressionInfoRepository.deleteById(suppressionInfoToDelete.getId());
			FileUtils.deleteDirectory(new File(suppressionInfoToDelete.getSuppressionLocation()));
			
		}catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		
	}

	@Transactional
	public void updateAllGroupsFilteringCountWithNewSuppressionData(Long suppressionId, String suppressionUrl) {
		
		Assert.notNull(suppressionId, "Cannot update groups with suppression infos with null suppressionId");

		if(suppressionInfoRepository.findBySuppressionId(suppressionId).isPresent()){
			throw new TechnicalException("A suppressionInfo already exists with the given suppressionId : " + suppressionId + " Cannot update mailing data with existing suppression !");
		}
		
		SuppressionFilesLocationDto suppressionFiles = suppressionService.fetchSuppressionData(suppressionId, suppressionUrl);
		 
		SuppressionInfoEntity savedSuppressionInfo = persistSuppressionDataInfos(suppressionId, getDataSuppressionParentLocation(suppressionFiles));
		
		groupRepository.findAll().stream()
		.map(group -> filterGroupDataWithSuppression(suppressionFiles, group))
		.forEach(groupWithFilteredData -> persistInfoAboutFilteredData(savedSuppressionInfo, groupWithFilteredData));

	}
	
	public List<DataItemDto> getFilteredData(List<Long> groupsIds, Long suppressionId, Integer offset, Integer limit){
		
		log.info("Fetching Filtered Data ...");
	
		SuppressionFilesLocationDto suppressionFiles = fetchSuppressionDataLocation(suppressionId);
			
		return fetchDataFilteredWithSuppressionData(suppressionFiles.getDataPath(), groupsIds, offset, limit);
	}

	private SuppressionInfoEntity persistSuppressionDataInfos(Long suppressionId, String suppressionLocation) {
		SuppressionInfoEntity suppressionInfo = SuppressionInfoEntity.builder()
				.suppressionId(suppressionId)
				.suppressionLocation(suppressionLocation).build();
		SuppressionInfoEntity savedSuppressionInfo = suppressionInfoRepository.save(suppressionInfo);
		return savedSuppressionInfo;
	}

	private void persistInfoAboutFilteredData(SuppressionInfoEntity savedSuppressionInfo, GroupEntity group) {
		FilteredGroupInfoKey infoId = FilteredGroupInfoKey.builder()
				.groupId(group.getId())
				.suppressionInfoId(savedSuppressionInfo.getId())
				.build();
		FilteredGroupInfoEntity info = FilteredGroupInfoEntity.builder()
				.id(infoId)
				.suppressionInfo(savedSuppressionInfo)
				.group(group)
				.filteredDataCount(group.getDataItems().size()).build();
		filteredGroupInfoRepository.save(info);
	}

	private GroupEntity filterGroupDataWithSuppression(SuppressionFilesLocationDto suppressionFiles, GroupEntity groupToFilter) {
		log.info("Filtering data with suppression files ...");
		
		List<DataItemEntity> filteredDataEntities = groupToFilter
		.getDataItems().stream()
		.filter(dataItem -> suppressionService.notInSuppressionFile(dataItem.getProspectEmail(), suppressionFiles.getDataPath()))
		.collect(Collectors.toList());
		
		groupToFilter.setDataItems(filteredDataEntities);
		return groupToFilter;
	}
	

	private SuppressionFilesLocationDto fetchSuppressionDataLocation(Long suppressionId) {
		Assert.notNull(suppressionId, "specific suppressionInfo is needed to filter the data, cannot fetch suppressionInfo with null suppressionId");
		
		String suppressionParentFileLocation = suppressionInfoRepository.findBySuppressionId(suppressionId)
				.map(SuppressionInfoEntity::getSuppressionLocation)
				.orElseThrow(() -> new NoSuchElementException("No suppressionInfo available with the given suppression id :" + suppressionId));
		
		return suppressionService.getSuppressionFilesFromDirectory(Paths.get(suppressionParentFileLocation));
	}
	

	private List<DataItemDto> fetchDataFilteredWithSuppressionData(Path suppressionDataPath, List<Long> groupsIds, Integer offset, Integer limit) {
		Assert.isTrue(!CollectionUtils.isEmpty(groupsIds), "Cannot fetch data with null or empty group id !");
		
		log.info("Fetching Data page from DB ...");
		
		return dataItemRepository.findByGroup_idOrderByItemId(groupsIds, offset, limit).stream()
				.map(dataMapper::toDto)
				.filter(dataItem -> suppressionService.notInSuppressionFile(dataItem.getProspectEmail(), suppressionDataPath))
				.collect(Collectors.toList());
		
	}
	
	private String getDataSuppressionParentLocation(SuppressionFilesLocationDto suppressionFiles) {
		Assert.notNull(suppressionFiles, "Cannot find parent directory location with null argument");
		Assert.notNull(suppressionFiles.getDataPath(), "Cannot find parent directory location with null dataPath");
		Assert.notNull(suppressionFiles.getDataPath().getParent(), "Cannot find parent directory location with null parent");
		
		return suppressionFiles.getDataPath().getParent().toString();
		
	}

}
