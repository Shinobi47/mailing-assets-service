package com.benayed.mailing.assets.service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoKey;
import com.benayed.mailing.assets.entity.GroupEntity;
import com.benayed.mailing.assets.entity.SuppressionInfoEntity;
import com.benayed.mailing.assets.enums.Platform;
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
	private HiPathSuppressionService hiPathSuppressionService;
	
	@Transactional
	public void deleteSuppressionInformations(Long suppressionId) {
		try {
			SuppressionInfoEntity suppressionInfoToDelete = suppressionInfoRepository.findBySuppressionId(suppressionId)
					.orElseThrow(() -> new NoSuchElementException("No suppressionInfo found with the given suppressionId : " + suppressionId));
			
			filteredGroupInfoRepository.deleteAllBySuppressionInfoId(suppressionId);
			suppressionInfoRepository.deleteById(suppressionInfoToDelete.getId());
			FileUtils.deleteDirectory(new File(suppressionInfoToDelete.getSuppressionLocation()));
			
		}catch(IOException e) {
			throw new UncheckedIOException(e);
		}

	}


	@Transactional
	public void updateAllGroupsFilteringCountWithNewSuppressionData(Long suppressionId, String suppressionUrl, Platform platform) {
		
		SuppressionFilesLocationDto hiPathSuppressionFiles = hiPathSuppressionService.fetchSuppressionDataFromHiPath(suppressionId, suppressionUrl);
		 
		SuppressionInfoEntity savedSuppressionInfo = persistSuppressionDataInfos(suppressionId, hiPathSuppressionFiles.getDataPath().getParent().toString() ,platform);
		
		groupRepository.findAll().stream()
		.map(group -> filterGroupDataWithSuppression(hiPathSuppressionFiles, group))
		.forEach(groupWithFilteredData -> persistInfoAboutFilteredData(savedSuppressionInfo, groupWithFilteredData));

	}
	
	public Page<DataItemDto> getFilteredPaginatedData(Long groupId, Pageable pageable, Platform platform, String dataType, Long suppressionId) throws IOException {
		Assert.notNull(groupId, "Cannot fetch data with null group id !");
		Assert.notNull(pageable, "Cannot fetch data with null pageable !");
		Assert.notNull(platform, "Cannot fetch data with null platform !");
		Assert.notNull(dataType, "Cannot fetch data with null dataType !");
		Assert.notNull(suppressionId, "Cannot find suppressionInfo with null SuppressionId!");
		
		if(hiPathSuppressionService.isHiPathSuppressionData(platform, dataType)) {
			log.info("Fetching suppression data from suppression repository ...");
	
			SuppressionFilesLocationDto hiPathSuppressionFiles = fetchHiPathSuppressionDataLocation(suppressionId);
			
			return fetchDataFilteredWithHiPathSuppression(hiPathSuppressionFiles, groupId, pageable);
		}
		
		throw new TechnicalException("Unsupported platform/dataType, cannot fetch suppression Data!");
	}

	private SuppressionInfoEntity persistSuppressionDataInfos(Long suppressionId, String suppressionLocation,
			Platform platform) {
		SuppressionInfoEntity suppressionInfo = SuppressionInfoEntity.builder()
				.suppressionId(suppressionId)
				.suppressionPlatform(platform)
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
		.filter(dataItem -> hiPathSuppressionService.notInSuppressionFile(dataItem.getProspectEmail(), suppressionFiles.getDataPath()))
		.collect(Collectors.toList());
		
		groupToFilter.setDataItems(filteredDataEntities);
		return groupToFilter;
	}
	

	private SuppressionFilesLocationDto fetchHiPathSuppressionDataLocation(Long suppressionId) {
		String suppressionParentFileLocation = suppressionInfoRepository.findBySuppressionId(suppressionId)
			.map(SuppressionInfoEntity::getSuppressionLocation)
			.orElseThrow(() -> new NoSuchElementException("No suppressionInfo available with the given suppression id :" + suppressionId));
		
		return hiPathSuppressionService.getHiPathSuppressionFilesFromParentDirectory(Paths.get(suppressionParentFileLocation));
	}
	

	private Page<DataItemDto> fetchDataFilteredWithHiPathSuppression(SuppressionFilesLocationDto suppressionFiles, Long groupId, Pageable pageable) {
		log.info("Fetching Data page from DB ...");
		Page<DataItemDto> mailingData = dataItemRepository.findByGroup_id(groupId, pageable).map(dataMapper::toDto);
		
		List<DataItemDto> filteredMailingData =
				mailingData.getContent().stream()
				.filter(dataItem -> hiPathSuppressionService.notInSuppressionFile(dataItem.getProspectEmail(), suppressionFiles.getDataPath()))
				.collect(Collectors.toList());
		
		return new PageImpl<DataItemDto>(filteredMailingData, pageable, mailingData.getTotalElements());
	}

}
