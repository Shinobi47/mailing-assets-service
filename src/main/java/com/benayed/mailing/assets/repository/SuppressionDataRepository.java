package com.benayed.mailing.assets.repository;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SuppressionDataRepository {
	
	private final String ZIP_FILE_SUFFIX = "tmp";
	private final String ZIP_FILE_PREFIX = "HiPathSuppData";
	public static final String HIPATH_DOMAINS_SUPPRESSION_FILE_NAME = "domains.txt";
	private RestTemplate restTemplate;

	public SuppressionFilesLocationDto fetchHiPathSuppressionData(String zipFileUrl, Path unzipLocation) throws IOException {
		Assert.notNull(zipFileUrl, "Cannot fetch zip suppressionFile with null url");
		Assert.notNull(unzipLocation, "unzipLocation cannot be null");
		
		File suppZipFile = restTemplate.execute(zipFileUrl, HttpMethod.GET, null, this::writeResponseToTempFile);

		List<Path> paths = unzip(suppZipFile.getCanonicalFile().toPath(), unzipLocation);

		return SuppressionFilesLocationDto.builder()
        		.domainsPath(getDomainsSuppressionFilePath(paths))
        		.dataPath(getDataSuppressionFile(paths)).build();
	}

	private Path getDataSuppressionFile(List<Path> paths) {
		return paths
				.stream()
				.filter(this::isFileHavingHiPathSuppressionDataFileNamePattern)
				.findFirst()
				.orElse(null);
	}


	private Path getDomainsSuppressionFilePath(List<Path> paths) {
		return paths
				.stream()
				.filter(this::isFileHavingHiPathSuppressionDomainsFileNamePattern)
				.findFirst()
				.orElse(null);
	}
	
	public boolean isFileHavingHiPathSuppressionDataFileNamePattern(Path path) {
		return Character.isDigit(path.getFileName().toString().charAt(0)); //cuz hipath data suppression file start with a digit
	}
	
	public boolean isFileHavingHiPathSuppressionDomainsFileNamePattern(Path path) {
		return HIPATH_DOMAINS_SUPPRESSION_FILE_NAME.equalsIgnoreCase(path.getFileName().toString());
	}	
	

	private List<Path> unzip(Path zipFile, Path unzipDestination) throws IOException {
		try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            List<Path> entriesPaths = new ArrayList<Path>();
            
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = unzipDestination.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.copy(zipInputStream, toPath);
                } else {
                    Files.createDirectory(toPath);
                }
                 
                entriesPaths.add(toPath);
            }
            Files.delete(zipFile);
            return entriesPaths;
        } catch (IOException e) {
        	throw e;
        }
	}

	private File writeResponseToTempFile(ClientHttpResponse clientHttpResponse) throws IOException, FileNotFoundException {
		
		File file = File.createTempFile(ZIP_FILE_PREFIX, ZIP_FILE_SUFFIX);
		FileOutputStream fos = new FileOutputStream(file);
		StreamUtils.copy(clientHttpResponse.getBody(), fos);
		fos.close();
		return file;
	}

}
