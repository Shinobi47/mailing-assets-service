package com.benayed.mailing.assets.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.HiPathSuppressionFilesLocationDto;
import com.benayed.mailing.assets.enums.Platform;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
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
	
	public Page<DataItemDto> getFilteredPaginatedData(Long groupId, Pageable pageable, Platform platform, String dataType, String suppressionUrl) throws IOException {
		Assert.notNull(groupId, "Cannot fetch data with null group id !");
		Assert.notNull(pageable, "Cannot fetch data with null pageable !");
		Assert.notNull(platform, "Cannot fetch data with null platform !");
		Assert.notNull(dataType, "Cannot fetch data with null dataType !");
		
		if(isHiPathSuppressionData(platform, dataType)) {
			log.info("Fetching suppression data from suppression repository ...");
			HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = suppressionDataRepository.fetchHiPathSuppressionData(suppressionUrl);
//			HiPathSuppressionFilesLocationDto hiPathSuppressionFiles = HiPathSuppressionFilesLocationDto.builder().dataPath(Paths.get("C:\\Users\\Kenji\\Desktop\\Mailing\\supp\\0.txt")).domainsPath(Paths.get("C:\\Users\\Kenji\\Desktop\\Mailing\\supp\\domains.txt")).build();
			return fetchDataFilteredWithHiPathSuppression(hiPathSuppressionFiles, groupId, pageable);
		}
		
		throw new TechnicalException("Unsupported platform/dataType, cannot fetch suppression Data!");
	}
		
	private Page<DataItemDto> fetchDataFilteredWithHiPathSuppression(HiPathSuppressionFilesLocationDto suppressionFiles, Long groupId, Pageable pageable) {
		log.info("Fetching Data page from DB ...");
		Page<DataItemDto> mailingData = dataItemRepository.findByGroup_id(groupId, pageable).map(dataMapper::toDto);

		log.info("Filtering data with suppression files ...");
		List<DataItemDto> filteredMailingData = mailingData.stream()
				.filter(dataItem -> notInSuppressionFile(dataItem.getProspectEmail(), suppressionFiles.getDataPath()))
				.filter(dataItem -> notHavingForbiddenIsp(dataItem.getIsp(), suppressionFiles.getDomainsPath()))
				.collect(Collectors.toList());
		return new PageImpl<DataItemDto>(filteredMailingData, pageable, mailingData.getTotalElements());
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
