package com.benayed.mailing.assets.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.service.HiPathSuppressionService;

@ExtendWith(MockitoExtension.class)
public class HiPathSuppressionServiceTest {

	@Mock
	private SuppressionDataRepository suppressionDataRepository;

	@Captor
	private ArgumentCaptor<Path> captor;
	
	private HiPathSuppressionService hiPathSuppressionService;
	
	private final String tmpHiPathUnzipTestFolder = "src\\test\\resources\\tmp";

	
	@BeforeEach
	private void init() {
		hiPathSuppressionService = new HiPathSuppressionService(suppressionDataRepository);
		ReflectionTestUtils.setField(hiPathSuppressionService, "hiPathUnzipLocation", tmpHiPathUnzipTestFolder);
	}
	
	@Test
	public void should_call_suppression_repository_with_correct_params_when_fetching_suppression() throws IOException {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "someUrl";
		
		//Act
		hiPathSuppressionService.fetchSuppressionDataFromHiPath(suppressionId, suppressionUrl);
		
		//Assert
		Mockito.verify(suppressionDataRepository,  Mockito.times(1)).fetchHiPathSuppressionData(Mockito.eq(suppressionUrl), captor.capture());
		Assertions.assertThat(captor.getValue().getFileName().toString()).startsWith(HiPathSuppressionService.UNZIP_SUBFOLDER_PREFIX + suppressionId);
		
		FileUtils.deleteDirectory(Paths.get(tmpHiPathUnzipTestFolder).toFile()); //created by the service
	}
	
	@Test
	public void should_uncheck_ioexception_correctly_when_it_is_thrown() throws IOException {
		//Arrange
		Long anyId = 1L;
		String anyUrl = "someUrl";
		Mockito.when(suppressionDataRepository.fetchHiPathSuppressionData(Mockito.anyString(), Mockito.any())).thenThrow(new IOException());
		
		//Act
		assertThrows(UncheckedIOException.class, () -> 
		hiPathSuppressionService.fetchSuppressionDataFromHiPath(anyId, anyUrl));
		
		//Assert
		// => exception type asserted in ACT pat

		FileUtils.deleteDirectory(Paths.get(tmpHiPathUnzipTestFolder).toFile()); //created by the service
	}
	
	@Test
	public void should_filter_mail_present_in_suppressionfile() {
		//Arrange
		String mailToBeFiltered = "test@test.test"; // present in suppressionFile in md5 format
		Path hiPathDataSuppressionFile = Paths.get("src", "test", "resources", "HiPath_Filtering_Test_Rss", "0.txt");
		
		//Act
		Boolean isMailNotInSuppressionFile = hiPathSuppressionService.notInSuppressionFile(mailToBeFiltered, hiPathDataSuppressionFile);
	
		//Assert
		Assertions.assertThat(isMailNotInSuppressionFile).isFalse();
	}
	
	@Test
	public void should_not_filter_mail_not_present_in_suppressionfile() {
		//Arrange
		String mailNotToBeFiltered = "mailNotPresentInSuppFile@benayed.com"; // present in suppressionFile in md5 format
		Path hiPathDataSuppressionFile = Paths.get("src", "test", "resources", "HiPath_Filtering_Test_Rss", "0.txt");
		
		//Act
		Boolean isMailNotInSuppressionFile = hiPathSuppressionService.notInSuppressionFile(mailNotToBeFiltered, hiPathDataSuppressionFile);
	
		//Assert
		Assertions.assertThat(isMailNotInSuppressionFile).isTrue();
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	public void should_not_filter_blank_or_null_mail(String blankMail) {
		//Arrange
		Path hiPathDataSuppressionFile = Paths.get("src", "test", "resources", "HiPath_Filtering_Test_Rss", "0.txt");
		
		//Act
		Boolean isMailNotInSuppressionFile = hiPathSuppressionService.notInSuppressionFile(blankMail, hiPathDataSuppressionFile);
	
		//Assert
		Assertions.assertThat(isMailNotInSuppressionFile).isTrue();
	}
	
}
