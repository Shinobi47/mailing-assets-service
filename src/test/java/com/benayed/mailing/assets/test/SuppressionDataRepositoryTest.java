package com.benayed.mailing.assets.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.benayed.mailing.assets.dto.OptizmoSuppressionDto;
import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.utils.FileUtils;

@ExtendWith(MockitoExtension.class)
public class SuppressionDataRepositoryTest {

	private SuppressionDataRepository suppressionDataRepository;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private FileUtils fileUtils;
	
	@BeforeEach
	private void init() {
		suppressionDataRepository = new SuppressionDataRepository(restTemplate, fileUtils);
	}
	
	@Test
	public void should_throw_exception_when_fetching_hipath_suppression_with_null_url() {
		//Arrange
		String zipFileUrl = null;
		Path unzipLocation = Paths.get("someUnzipLocation");
		
		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		suppressionDataRepository.fetchHiPathSuppressionData(zipFileUrl, unzipLocation));
		
		//Assert
		//=> exception thrown
	}

	@Test
	public void should_throw_exception_when_fetching_hipath_suppression_with_null_unzipLocation() {
		//Arrange
		String zipFileUrl = "someUrl";
		Path unzipLocation = null;
		
		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		suppressionDataRepository.fetchHiPathSuppressionData(zipFileUrl, unzipLocation));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_perform_right_calls_when_fetching_hipath_suppression_data() throws IOException {
		//Arrange
		String zipFileUrl = "someUrl";
		Path unzipLocation = Paths.get("dumm_unzip_folder_location");
		File pathToZipFile = new File("dummy_zip_file_location");
		
		when(restTemplate.execute(eq(zipFileUrl), eq(HttpMethod.GET), any(), any())).thenReturn(pathToZipFile);
		when(fileUtils.unzip(pathToZipFile.getCanonicalFile().toPath(), unzipLocation)).thenReturn(new ArrayList<Path>());
		
		//Act
		SuppressionFilesLocationDto suppressionData = suppressionDataRepository.fetchHiPathSuppressionData(zipFileUrl, unzipLocation);
		
		//Assert
		verify(restTemplate, times(1)).execute(eq(zipFileUrl), eq(HttpMethod.GET), any(), any());
		verify(fileUtils, times(1)).unzip(pathToZipFile.getCanonicalFile().toPath(), unzipLocation);
		Assertions.assertThat(suppressionData).isNotNull();

	}
	
	@Test
	public void should_throw_exception_when_fetching_optizmo_suppression_with_null_url() {
		//Arrange
		String optizmoUrl = null;
		Path unzipLocation = Paths.get("someUnzipLocation");
		
		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		suppressionDataRepository.fetchOptizmoSuppressionData(optizmoUrl, unzipLocation));
		
		//Assert
		//=> exception thrown
	}

	@Test
	public void should_throw_exception_when_fetching_optizmo_suppression_with_null_unzipLocation() {
		//Arrange
		String optizmoUrl = "someUrl";
		Path unzipLocation = null;
		
		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		suppressionDataRepository.fetchOptizmoSuppressionData(optizmoUrl, unzipLocation));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_perform_right_calls_when_fetching_optizmo_suppression_data() throws IOException {
		//Arrange
		String campaignKey = "sm-gllf-d21-232b5cf228ae28ab16c629ae83149c09"; // to be extracted from optizmo url and used in a new url to fetch zipfile
		String optizmoUrlGivenByCaller = "https://mailer.optizmo.net/" + campaignKey + "&one=1&icma=cb619841296375b3d5f66639c526c1c7";
		Path unzipLocation = Paths.get("dummy_unzip_folder_location");
		File pathToZipFile = new File("dummy_zip_file_location");
		
		OptizmoSuppressionDto osd = OptizmoSuppressionDto.builder().downloadLink("Url_Where_Zip_File_Is").build();
		
		when(restTemplate.getForObject("https://mailer-api.optizmo.net/accesskey/download/" + campaignKey + "?token=null&format=md5", OptizmoSuppressionDto.class)).thenReturn(osd);
		when(restTemplate.execute(eq(osd.getDownloadLink()), eq(HttpMethod.GET), any(), any())).thenReturn(pathToZipFile);
		when(fileUtils.unzip(pathToZipFile.getCanonicalFile().toPath(), unzipLocation)).thenReturn(new ArrayList<Path>());
		
		//Act
		SuppressionFilesLocationDto suppressionData = suppressionDataRepository.fetchOptizmoSuppressionData(optizmoUrlGivenByCaller, unzipLocation);
		
		//Assert
		verify(restTemplate, times(1)).getForObject("https://mailer-api.optizmo.net/accesskey/download/" + campaignKey + "?token=null&format=md5", OptizmoSuppressionDto.class);
		verify(restTemplate, times(1)).execute(eq(osd.getDownloadLink()), eq(HttpMethod.GET), any(), any());
		verify(fileUtils, times(1)).unzip(pathToZipFile.getCanonicalFile().toPath(), unzipLocation);
		Assertions.assertThat(suppressionData).isNotNull();

	}

}
