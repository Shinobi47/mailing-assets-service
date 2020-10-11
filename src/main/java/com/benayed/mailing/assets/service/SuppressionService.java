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
import static com.benayed.mailing.assets.enums.Platform.OPTIZMO;
import static com.benayed.mailing.assets.enums.Platform.HIPATH;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.utils.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SuppressionService {

	private SuppressionDataRepository suppressionDataRepository;
	private FileUtils fileUtils;
	
	@Value("${suppression.unzip-location}")
	private String parentUnzipLocation;
	
	public static final String UNZIP_SUBFOLDER_PREFIX = "id-";
	
	public SuppressionService(SuppressionDataRepository suppressionDataRepository,  FileUtils fileUtils) {
		this.suppressionDataRepository = suppressionDataRepository;
		this.fileUtils = fileUtils;
	}

	public SuppressionFilesLocationDto fetchSuppressionData(Long subFolderPrefix, String suppressionUrl){
		try {
			Platform platform = getUsedPlatform(suppressionUrl);
			String subfolder = UNZIP_SUBFOLDER_PREFIX + String.valueOf(subFolderPrefix) + "_" + platform + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_hh-mm-ss_SSS-a"));
			Path unzipLocation = Paths.get(parentUnzipLocation, subfolder);

			Files.createDirectories(unzipLocation);
			
			if(HIPATH.equals(platform)) {
				return suppressionDataRepository.fetchHiPathSuppressionData(suppressionUrl, unzipLocation);
			}
			else if(OPTIZMO.equals(platform)) {
				return suppressionDataRepository.fetchOptizmoSuppressionData(suppressionUrl, unzipLocation);
			}
			throw new TechnicalException("suppression url doesn't correspond to a known platform, cannot fetch suppression data !");
		}catch(IOException e) {
			throw new UncheckedIOException(e);
		}

	}

	private Platform getUsedPlatform(String suppressionUrl) {
		if(suppressionUrl.contains("optizmo"))
			return OPTIZMO;
		else if(suppressionUrl.contains("api") && suppressionUrl.contains("suppdownload"))
			return HIPATH;
		return null;
	}

	public SuppressionFilesLocationDto getSuppressionFilesFromDirectory(Path parentDirectory) {
		Path suppressionDataPath = fileUtils.getFileRespectingAPredicateFromDirectory(parentDirectory, fileUtils::doesFileNameNotContainDomains);
		Path suppressionDomainsPath = fileUtils.getFileRespectingAPredicateFromDirectory(parentDirectory, fileUtils::doesFileNameContainDomains);
		
		return SuppressionFilesLocationDto.builder()
				.dataPath(suppressionDataPath)
				.domainsPath(suppressionDomainsPath).build();
	}


	public boolean  notInSuppressionFile(String mail, Path path) {
		if(StringUtils.isBlank(mail)) {
			log.warn("Your data contains a blank email adress !");
			return true;
		}
		try(BufferedReader bufferedReader = Files.newBufferedReader(path)) {
			String hashedMail = DigestUtils.md5DigestAsHex(mail.getBytes());
			return bufferedReader.lines()
					.noneMatch(hashedMail::equals);
		} catch (IOException e) {
			throw new UncheckedIOException(e); 
		}

	}

	
}
