package com.benayed.mailing.assets.test;

import static com.benayed.mailing.assets.repository.SuppressionDataRepository.HIPATH_DOMAINS_SUPPRESSION_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;

@ExtendWith(MockitoExtension.class)
public class HiPathSuppressionRepositoryTest {

	private SuppressionDataRepository suppressionDataRepository;
	
	@Mock
	private RestTemplate restTemplate;
	
	@BeforeEach
	private void init() {
		suppressionDataRepository = new SuppressionDataRepository(restTemplate);
	}
	
	@Test
	public void should_throw_exception_when_fetching_suppression_with_null_url() {
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
	public void should_throw_exception_when_fetching_suppression_with_null_unzipLocation() {
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
	public void should_unzip_HiPath_suppression_zip_file_and_delete_it_successfully_when_calling_HiPath_api() throws IOException {
		//Arrange
		String zipFileUrl = "someUrl";
		Path zipTestFile = Paths.get("src", "test", "resources", "HiPath_Supp_file_with_domains_and_data.zip");
		Path unzipLocation = Paths.get("src", "test", "resources", "toBeDeleted");
		Path temporaryZipFile = copyZipTestFileIntoTemporaryLocation(unzipLocation, zipTestFile); //because our service deletes it when unzipped
		
		when(restTemplate.execute(eq(zipFileUrl), eq(HttpMethod.GET), any(), any())).thenReturn(temporaryZipFile.toFile());
		
		//Act
		SuppressionFilesLocationDto suppressionData = suppressionDataRepository.fetchHiPathSuppressionData(zipFileUrl, unzipLocation);
		
		//Assert
		Assertions.assertThat(suppressionData.getDataPath().getParent()).isEqualTo(unzipLocation);
		Assertions.assertThat(Character.isDigit(suppressionData.getDataPath().getFileName().toString().charAt(0))).isTrue();
		Assertions.assertThat(suppressionData.getDomainsPath()).isEqualTo(unzipLocation.resolve(HIPATH_DOMAINS_SUPPRESSION_FILE_NAME));
		Assertions.assertThat(temporaryZipFile).doesNotExist(); //service deleted it
		
		FileUtils.deleteDirectory(unzipLocation.toFile());
	}
	

	private Path copyZipTestFileIntoTemporaryLocation(Path unzipLocation, Path testZipFilePath) throws IOException {
		Path tempZip = unzipLocation.resolve(testZipFilePath.getFileName());
		Files.createDirectory(unzipLocation);
		Files.copy(testZipFilePath, tempZip);
		return tempZip;
	}
}
