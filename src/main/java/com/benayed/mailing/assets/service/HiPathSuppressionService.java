package com.benayed.mailing.assets.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.enums.Platform;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.utils.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HiPathSuppressionService {
	
	private SuppressionDataRepository suppressionDataRepository;
	private FileUtils fileUtils;
	
//	@Value("${suppression.hipath.unzip-location}")
	private String hiPathUnzipLocation;
	
	public static final String UNZIP_SUBFOLDER_PREFIX = "id-";
	public static final String HIPATH_SUPP_FILE_EXTENTION = "zip";
	
	public HiPathSuppressionService(SuppressionDataRepository suppressionDataRepository) {
		this.suppressionDataRepository = suppressionDataRepository;
	}


	public SuppressionFilesLocationDto fetchSuppressionDataFromHiPath(Long suppressionId, String suppressionUrl){
		try {
			String subfolder = UNZIP_SUBFOLDER_PREFIX + String.valueOf(suppressionId) + "__" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm-ss_SSS-a"));
			Path hiPathSuppressionUnzipLocation = Paths.get(hiPathUnzipLocation, subfolder);
			Files.createDirectories(hiPathSuppressionUnzipLocation);
			return suppressionDataRepository.fetchHiPathSuppressionData(suppressionUrl, hiPathSuppressionUnzipLocation);
		}catch(IOException e) {
			throw new UncheckedIOException(e);
		}

	}
//
//	public boolean  notInSuppressionFile(String mail, Path path) {
//		if(StringUtils.isBlank(mail)) {
//			log.warn("Your data contains a blank email adress !");
//			return true;
//		}
//		
//		try(BufferedReader bufferedReader = Files.newBufferedReader(path)) {
//			String hashedMail = DigestUtils.md5DigestAsHex(mail.getBytes());
//			return bufferedReader.lines()
//					.noneMatch(hashedMail::equals);
//		} catch (IOException e) {
//			throw new UncheckedIOException(e); 
//		}
//
//	}
//	
//	public SuppressionFilesLocationDto getHiPathSuppressionFilesFromParentDirectory(Path parentDirectory) {
//		
//		Path hiPathSuppressionDataFile = fileUtils.getFileRespectingAPredicateFromDirectory(parentDirectory, suppressionDataRepository::isFileHavingHiPathSuppressionDataFileNamePattern);
//		Path hiPathSuppressionDomainsFile = fileUtils.getFileRespectingAPredicateFromDirectory(parentDirectory, suppressionDataRepository::isFileHavingHiPathSuppressionDomainsFileNamePattern);
//		
//		return SuppressionFilesLocationDto.builder()
//				.dataPath(hiPathSuppressionDataFile)
//				.domainsPath(hiPathSuppressionDomainsFile).build();
//	}
	
	
//	public Boolean isHiPathSuppressionData(Platform platform, String fileType) {
//		return Platform.HiPath.equals(platform) &&
//				HIPATH_SUPP_FILE_EXTENTION.equals(fileType);
//	}

	
}
