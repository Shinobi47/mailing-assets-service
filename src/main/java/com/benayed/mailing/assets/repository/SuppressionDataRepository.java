package com.benayed.mailing.assets.repository;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.benayed.mailing.assets.dto.OptizmoSuppressionDto;
import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.utils.FileUtils;

@Repository
public class SuppressionDataRepository {
	
	private final String ZIP_FILE_SUFFIX = "tmp";
	private final String ZIP_FILE_PREFIX = "HiPathSuppData";
	public static final String HIPATH_DOMAINS_SUPPRESSION_FILE_NAME = "domains.txt";
	private RestTemplate restTemplate;
	private FileUtils fileUtils;
	
	@Value("${optizmo.api-token}")
	private String optizmoApiToken;
	
	public SuppressionDataRepository(RestTemplate restTemplate, FileUtils fileUtils) {
		this.restTemplate = restTemplate;
		this.fileUtils = fileUtils;
	}
	
	public SuppressionFilesLocationDto fetchOptizmoSuppressionData(String optizmoUrl, Path unzipLocation) throws IOException {
		Assert.notNull(optizmoUrl, "Cannot fetch zip suppressionFile with null optizmo url");
		Assert.notNull(unzipLocation, "unzipLocation cannot be null");
		
		OptizmoSuppressionDto optizmoSupp = prepareOptizmoDownload(optizmoUrl);
		
		File suppZipFile = restTemplate.execute(optizmoSupp.getDownloadLink(), HttpMethod.GET, null, this::writeResponseToTempFile);

		List<Path> paths = fileUtils.unzip(suppZipFile.getCanonicalFile().toPath(), unzipLocation);

		return SuppressionFilesLocationDto.builder()
        		.domainsPath(getDomainsSuppressionFilePath(paths))
        		.dataPath(getDataSuppressionFile(paths)).build();
	}

	
	public SuppressionFilesLocationDto fetchHiPathSuppressionData(String zipFileUrl, Path unzipLocation) throws IOException {
		Assert.notNull(zipFileUrl, "Cannot fetch zip suppressionFile with null url");
		Assert.notNull(unzipLocation, "unzipLocation cannot be null");
		
		File suppZipFile = restTemplate.execute(zipFileUrl, HttpMethod.GET, null, this::writeResponseToTempFile);

		List<Path> paths = fileUtils.unzip(suppZipFile.getCanonicalFile().toPath(), unzipLocation);

		return SuppressionFilesLocationDto.builder()
        		.domainsPath(getDomainsSuppressionFilePath(paths))
        		.dataPath(getDataSuppressionFile(paths)).build();
	}
	
	private OptizmoSuppressionDto prepareOptizmoDownload(String optizmoUrl) {
		String campaignAccessKey = StringUtils.substringBetween(optizmoUrl, "optizmo.net/", "&");
		String zipFileUrl = "https://mailer-api.optizmo.net/accesskey/download/" + campaignAccessKey + "?token=" + optizmoApiToken +"&format=md5";
		return restTemplate.getForObject(zipFileUrl, OptizmoSuppressionDto.class);
	}

	private Path getDataSuppressionFile(List<Path> paths) {
		return paths
				.stream()
				.filter(fileUtils::doesFileNameNotContainDomains)
				.findFirst()
				.orElse(null);
	}


	private Path getDomainsSuppressionFilePath(List<Path> paths) {
		return paths
				.stream()
				.filter(fileUtils::doesFileNameContainDomains)
				.findFirst()
				.orElse(null);
	}


	private File writeResponseToTempFile(ClientHttpResponse clientHttpResponse) throws IOException, FileNotFoundException {
		
		File file = File.createTempFile(ZIP_FILE_PREFIX, ZIP_FILE_SUFFIX);
		FileOutputStream fos = new FileOutputStream(file);
		StreamUtils.copy(clientHttpResponse.getBody(), fos);
		fos.close();
		return file;
	}

}
