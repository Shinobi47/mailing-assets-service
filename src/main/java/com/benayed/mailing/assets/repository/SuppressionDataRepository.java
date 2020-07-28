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

import com.benayed.mailing.assets.dto.HiPathSuppressionFilesLocationDto;
import com.benayed.mailing.assets.exception.TechnicalException;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SuppressionDataRepository {
	
	private final String ZIP_FILE_SUFFIX = "tmp";
	private final String ZIP_FILE_PREFIX = "HiPathSuppData";
	public static final String HIPATH_DOMAINS_SUPPRESSION_FILE_NAME = "domains.txt";
	public static final String HIPATH_DATA_SUPPRESSION_FILE_NAME = "0.txt";
	private RestTemplate restTemplate;

	public HiPathSuppressionFilesLocationDto fetchHiPathSuppressionData(String zipFileUrl, Path unzipLocation) throws IOException {
		Assert.notNull(zipFileUrl, "Cannot fetch zip suppressionFile with null url");
		Assert.notNull(unzipLocation, "unzipLocation cannot be null");
		
		File suppZipFile = restTemplate.execute(zipFileUrl, HttpMethod.GET, null, this::writeResponseToTempFile);

		List<Path> paths = unzip(suppZipFile.getCanonicalFile().toPath(), unzipLocation);

		return HiPathSuppressionFilesLocationDto.builder()
        		.domainsPath(getFilePath(HIPATH_DOMAINS_SUPPRESSION_FILE_NAME, paths))
        		.dataPath(getFilePath(HIPATH_DATA_SUPPRESSION_FILE_NAME, paths)).build();
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

	private Path getFilePath(String fileName, List<Path> paths) {
		return paths.stream().filter( path -> fileName.equals(path.getFileName().toString())).findFirst().orElseThrow(() -> new TechnicalException("Suppression file \"" + fileName + "\" not found"));
	}

	private File writeResponseToTempFile(ClientHttpResponse clientHttpResponse) throws IOException, FileNotFoundException {
		
		File file = File.createTempFile(ZIP_FILE_PREFIX, ZIP_FILE_SUFFIX);
		FileOutputStream fos = new FileOutputStream(file);
		StreamUtils.copy(clientHttpResponse.getBody(), fos);
		fos.close();
		return file;
	}

}
