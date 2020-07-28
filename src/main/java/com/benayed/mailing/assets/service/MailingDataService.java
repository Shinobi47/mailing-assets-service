package com.benayed.mailing.assets.service;

import static com.benayed.mailing.assets.repository.SuppressionDataRepository.HIPATH_DATA_SUPPRESSION_FILE_NAME;
import static com.benayed.mailing.assets.repository.SuppressionDataRepository.HIPATH_DOMAINS_SUPPRESSION_FILE_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.util.DigestUtils;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.HiPathSuppressionFilesLocationDto;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.entity.GroupEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoKey;
import com.benayed.mailing.assets.entity.SuppressionInfoEntity;
import com.benayed.mailing.assets.enums.Platform;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.GroupRepository;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.repository.FilteredGroupInfoRepository;
import com.benayed.mailing.assets.repository.SuppressionInfoRepository;
import com.benayed.mailing.assets.utils.DataMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class MailingDataService {
	
	private SuppressionDataRepository suppressionDataRepository;
	private DataItemRepository dataItemRepository;
	private DataMapper dataMapper;
	private final String ZIP_FILE_EXTENTION = "zip";
	
	private SuppressionInfoRepository suppressionInfoRepository;
	private FilteredGroupInfoRepository filteredGroupInfoRepository;
	private GroupRepository groupRepository;
	
//	NoSuchFileException: C:\Users\Kenji\Desktop\Mailing\supp\domains.txt
//	insert into SUPPRESSION_INFO (SINFO_ID,SUPPRESSION_ID, SUPPRESSION_LOCATION  ) values (1, 1,  'src\main\resources\Unzip\id-1__28-07-2020_06-00-15_306-PM');
//	insert into SUPPRESSION_FILTERED_GROUP (SFG_GROUP_ID   ,SFG_SINFO_ID ,FILTERED_DATA_COUNT ) values (1,1,'3');
//	insert into SUPPRESSION_FILTERED_GROUP (SFG_GROUP_ID   ,SFG_SINFO_ID ,FILTERED_DATA_COUNT ) values (2,1,'3');
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
		HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = fetchSuppressionDataFromHiPath(suppressionId, suppressionUrl);
//		HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = HiPathSuppressionFilesLocationDto.builder().dataPath(Paths.get("C:\\Users\\Kenji\\Desktop\\Mailing\\supp\\0.txt")).domainsPath(Paths.get("C:\\Users\\Kenji\\Desktop\\Mailing\\supp\\domains.txt")).build();
		 
		SuppressionInfoEntity savedSuppressionInfo = persistSuppressionDataInfos(suppressionId, hiPathSuppressionFiles.getDataPath().getParent().toString() ,platform);
		
		groupRepository.findAll().stream()
		.map(group -> filterGroupDataWithSuppression(hiPathSuppressionFiles, group))
		.forEach(groupWithFilteredData -> persistInfoAboutFilteredData(savedSuppressionInfo, groupWithFilteredData));

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
		FilteredGroupInfoEntity info= FilteredGroupInfoEntity.builder()
				.id(infoId)
				.suppressionInfo(savedSuppressionInfo)
				.group(group)
				.filteredDataCount(group.getDataItems().size()).build();
		filteredGroupInfoRepository.save(info);
	}
	
	private GroupEntity filterGroupDataWithSuppression(HiPathSuppressionFilesLocationDto suppressionFiles, GroupEntity groupToFilter) {
		log.info("Filtering data with suppression files ...");
		List<DataItemEntity> filteredData =  groupToFilter.getDataItems().stream()
				.filter(dataItem -> notInSuppressionFile(dataItem.getProspectEmail(), suppressionFiles.getDataPath()))
				.filter(dataItem -> notHavingForbiddenIsp(dataItem.getIsp(), suppressionFiles.getDomainsPath()))
				.collect(Collectors.toList());
		groupToFilter.setDataItems(filteredData);
		return groupToFilter;
	}
	
	private HiPathSuppressionFilesLocationDto fetchSuppressionDataFromHiPath(Long suppressionId, String suppressionUrl){
		try {
			String subfolder = "id-" + String.valueOf(suppressionId) + "__" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm-ss_SSS-a"));
			Path hiPathSuppressionUnzipLocation = Paths.get("src", "main", "resources", "Unzip", subfolder);
			Files.createDirectories(hiPathSuppressionUnzipLocation);
			return suppressionDataRepository.fetchHiPathSuppressionData(suppressionUrl, hiPathSuppressionUnzipLocation);
		}catch(IOException e) {
			throw new UncheckedIOException(e);
		}

	}
	
	public Page<DataItemDto> getFilteredPaginatedData(Long groupId, Pageable pageable, Platform platform, String dataType, Long suppressionId) throws IOException {
		Assert.notNull(groupId, "Cannot fetch data with null group id !");
		Assert.notNull(pageable, "Cannot fetch data with null pageable !");
		Assert.notNull(platform, "Cannot fetch data with null platform !");
		Assert.notNull(dataType, "Cannot fetch data with null dataType !");
		Assert.notNull(suppressionId, "Cannot find suppressionInfo with null SuppressionId!");
		
		if(isHiPathSuppressionData(platform, dataType)) {
			log.info("Fetching suppression data from suppression repository ...");
	
			HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = fetchHiPathSuppressionDataLocation(suppressionId);
			
			return fetchDataFilteredWithHiPathSuppression(hiPathSuppressionFiles, groupId, pageable);
		}
		
		throw new TechnicalException("Unsupported platform/dataType, cannot fetch suppression Data!");
	}

	private HiPathSuppressionFilesLocationDto fetchHiPathSuppressionDataLocation(Long suppressionId) {
		String suppressionParentFileLocation = suppressionInfoRepository.findBySuppressionId(suppressionId)
			.map(SuppressionInfoEntity::getSuppressionLocation)
			.orElseThrow(() -> new NoSuchElementException("No suppressionInfo available with the given suppression id :" + suppressionId));

		HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = HiPathSuppressionFilesLocationDto.builder()
				.dataPath(Paths.get(suppressionParentFileLocation, HIPATH_DATA_SUPPRESSION_FILE_NAME))
				.domainsPath(Paths.get(suppressionParentFileLocation, HIPATH_DOMAINS_SUPPRESSION_FILE_NAME)).build();
		return hiPathSuppressionFiles;
	}
	
	private Page<DataItemDto> fetchDataFilteredWithHiPathSuppression(HiPathSuppressionFilesLocationDto suppressionFiles, Long groupId, Pageable pageable) {
		log.info("Fetching Data page from DB ...");
		Page<DataItemDto> mailingData = dataItemRepository.findByGroup_id(groupId, pageable).map(dataMapper::toDto);
		List<DataItemDto> filteredMailingData = filterDataWithHiPathSuppression(mailingData.getContent(), suppressionFiles);
		return new PageImpl<DataItemDto>(filteredMailingData, pageable, mailingData.getTotalElements());
	}

	private List<DataItemDto> filterDataWithHiPathSuppression(List<DataItemDto> dataToFilter, HiPathSuppressionFilesLocationDto suppressionFiles) {
		log.info("Filtering data with HiPath suppression files ...");
		return dataToFilter.stream()
				.filter(dataItem -> notInSuppressionFile(dataItem.getProspectEmail(), suppressionFiles.getDataPath()))
				.filter(dataItem -> notHavingForbiddenIsp(dataItem.getIsp(), suppressionFiles.getDomainsPath()))
				.collect(Collectors.toList());
	}

	private boolean isHiPathSuppressionData(Platform platform, String fileType) {
		return Platform.HiPath.equals(platform) &&
				ZIP_FILE_EXTENTION.equals(fileType);
	}


	private boolean notHavingForbiddenIsp(String isp, Path path) {
		try(BufferedReader bufferedReader = Files.newBufferedReader(path)) {;
			return bufferedReader.lines()
					.map(domain -> domain.split("@")[1])
					.noneMatch(isp::equalsIgnoreCase);
		} catch (IOException e) {

			throw new UncheckedIOException(e);
		}
	}
	private boolean  notInSuppressionFile(String mail, Path path) {

		try(BufferedReader bufferedReader = Files.newBufferedReader(path)) {
			String hashedMail = DigestUtils.md5DigestAsHex(mail.getBytes());
			return bufferedReader.lines().noneMatch(hashedMail::equals);
		} catch (IOException e) {
			throw new UncheckedIOException(e); 
		}

	}

}
