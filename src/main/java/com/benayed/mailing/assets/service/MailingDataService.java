package com.benayed.mailing.assets.service;

import static com.benayed.mailing.assets.repository.SuppressionDataRepository.HIPATH_DATA_SUPPRESSION_FILE_NAME;
import static com.benayed.mailing.assets.repository.SuppressionDataRepository.HIPATH_DOMAINS_SUPPRESSION_FILE_NAME;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.HiPathSuppressionFilesLocationDto;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.entity.GroupEntity;
import com.benayed.mailing.assets.entity.SuppressionFilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.SuppressionFilteredGroupInfoKey;
import com.benayed.mailing.assets.entity.SuppressionInfoEntity;
import com.benayed.mailing.assets.enums.Platform;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.GroupRepository;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.repository.SuppressionFilteredGroupInfoRepository;
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
	private SuppressionFilteredGroupInfoRepository suppressionFilteredGroupInfoRepository;
	private GroupRepository groupRepository;
	
	
	public void updateAllGroupsFilteringCountWithNewSuppressionData(Long suppressionId, String suppressionUrl, Platform platform) {
//		HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = fetchSuppressionDataFromHiPath(suppressionId, suppressionUrl);
		HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = HiPathSuppressionFilesLocationDto.builder().dataPath(Paths.get("C:\\Users\\Kenji\\Desktop\\Mailing\\supp\\0.txt")).domainsPath(Paths.get("C:\\Users\\Kenji\\Desktop\\Mailing\\supp\\domains.txt")).build();
		
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
		SuppressionFilteredGroupInfoKey infoId = SuppressionFilteredGroupInfoKey.builder()
				.groupId(group.getId())
				.suppressionInfoId(savedSuppressionInfo.getId())
				.build();
		SuppressionFilteredGroupInfoEntity info= SuppressionFilteredGroupInfoEntity.builder()
				.id(infoId)
				.suppressionInfo(savedSuppressionInfo)
				.group(group)
				.filteredDataCount(group.getDataItems().size()).build();
		suppressionFilteredGroupInfoRepository.save(info);
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
	
			HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = fetchSuppressionDataLocation(suppressionId);
			
			return fetchDataFilteredWithHiPathSuppression(hiPathSuppressionFiles, groupId, pageable);
		}
		
		throw new TechnicalException("Unsupported platform/dataType, cannot fetch suppression Data!");
	}

	private HiPathSuppressionFilesLocationDto fetchSuppressionDataLocation(Long suppressionId) {
		String suppressionParentFileLocation = suppressionInfoRepository.findBySuppressionId(suppressionId)
			.map(SuppressionInfoEntity::getSuppressionLocation)
			.orElseThrow(() -> new NoSuchElementException("No suppressionInfo available for the given suppression id :" + suppressionId));
		System.out.println(suppressionInfoRepository.findBySuppressionId(suppressionId));
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
		try {
			return Files.newBufferedReader(path).lines()
					.map(domain -> domain.split("@")[1])
					.noneMatch(isp::equalsIgnoreCase);
		} catch (IOException e) {

			throw new UncheckedIOException(e);
		}
	}
	private boolean  notInSuppressionFile(String mail, Path path) {

		try {
			String hashedMail = DigestUtils.md5DigestAsHex(mail.getBytes());
			return Files.newBufferedReader(path).lines().noneMatch(hashedMail::equals);
		} catch (IOException e) {

			throw new UncheckedIOException(e);
		}

	}

}
