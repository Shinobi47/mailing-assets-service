package com.benayed.mailing.assets.test;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.service.SuppressionService;
import com.benayed.mailing.assets.utils.FileUtils;

@ExtendWith(MockitoExtension.class)
public class SuppressionServiceTest {

	@Mock
	private SuppressionDataRepository suppressionDataRepository;

	FileUtils fileUtils = new FileUtils();
	
	private SuppressionService suppressionService;
	
	private final String tmpHiPathUnzipTestFolder = "src\\test\\resources\\tmp";

	
	@BeforeEach
	private void init() {
		suppressionService = new SuppressionService(suppressionDataRepository, fileUtils);
		ReflectionTestUtils.setField(suppressionService, "parentUnzipLocation", tmpHiPathUnzipTestFolder);
	}
		
	@Test
	public void should_throw_exception_when_suppression_platform_is_unknown() throws IOException{
		//Arrange
		Long subFolderPrefix = 1L;
		String suppressionUrl = "cant_guess_platform_from_this_url";
		
		//Act
		assertThrows(TechnicalException.class, () -> 
		suppressionService.fetchSuppressionData(subFolderPrefix, suppressionUrl));
		
		//Assert
		// => exception thrown
		
		deleteDirectory(Paths.get(tmpHiPathUnzipTestFolder).toFile()); //created by the service
	}
	
	@Test
	public void should_perform_right_repository_call_when_hipath_suppression_detected() throws IOException{
		//Arrange
		Long subFolderPrefix = 1L;
		String suppressionUrl = "http://api.loadsmooth.com/suppdownload.php?z=MTAxNDE4NDMyOXw0NzA4Mjh8MTR8MTg5NDUxNzQxOA";
		
		
		//Act
		suppressionService.fetchSuppressionData(subFolderPrefix, suppressionUrl);
		
		//Assert
		verify(suppressionDataRepository, times(1)).fetchHiPathSuppressionData(eq(suppressionUrl), any());
		verify(suppressionDataRepository, never()).fetchOptizmoSuppressionData(eq(suppressionUrl), any());
		
		deleteDirectory(Paths.get(tmpHiPathUnzipTestFolder).toFile()); //created by the service
	}
	
	@Test
	public void should_perform_right_repository_call_when_optizmo_suppression_detected() throws IOException{
		//Arrange
		Long subFolderPrefix = 1L;
		String suppressionUrl = "https://mailer.optizmo.net/sm-gllf-d21-232b5cf228ae28ab16c629ae83149c09&one=1&icma=cb619841296375b3d5f66639c526c1c7";
		
		//Act
		suppressionService.fetchSuppressionData(subFolderPrefix, suppressionUrl);
		
		//Assert
		verify(suppressionDataRepository, never()).fetchHiPathSuppressionData(eq(suppressionUrl), any());
		verify(suppressionDataRepository, times(1)).fetchOptizmoSuppressionData(eq(suppressionUrl), any());
		
		deleteDirectory(Paths.get(tmpHiPathUnzipTestFolder).toFile()); //created by the service
	}
	
	
	@Test
	public void should_uncheck_ioexception_when_raised() throws IOException{
		//Arrange
		Long subFolderPrefix = 1L;
		String suppressionUrl = "https://mailer.optizmo.net/sm-gllf-d21-232b5cf228ae28ab16c629ae83149c09&one=1&icma=cb619841296375b3d5f66639c526c1c7";
		when(suppressionDataRepository.fetchOptizmoSuppressionData(Mockito.anyString(), Mockito.any())).thenThrow(new IOException());

		//Act
		assertThrows(UncheckedIOException.class, () -> 
		suppressionService.fetchSuppressionData(subFolderPrefix, suppressionUrl));
		
		//Assert
		// => exception thrown by mock call
		deleteDirectory(Paths.get(tmpHiPathUnzipTestFolder).toFile()); //created by the service

	}
	
	
	
	@Test
	public void should_filter_mail_present_in_suppressionfile() {
		//Arrange
		String mailToBeFiltered = "test@test.test"; // present in suppressionFile in md5 format
		Path hiPathDataSuppressionFile = Paths.get("src", "test", "resources", "filtering_Test_Rss", "0.txt");
		
		//Act
		Boolean isMailNotInSuppressionFile = suppressionService.notInSuppressionFile(mailToBeFiltered, hiPathDataSuppressionFile);
	
		//Assert
		Assertions.assertThat(isMailNotInSuppressionFile).isFalse();
	}
	
	@Test
	public void should_not_filter_mail_not_present_in_suppressionfile() {
		//Arrange
		String mailNotToBeFiltered = "mailNotPresentInSuppFile@benayed.com";
		Path hiPathDataSuppressionFile = Paths.get("src", "test", "resources", "filtering_Test_Rss", "0.txt");
		
		//Act
		Boolean isMailNotInSuppressionFile = suppressionService.notInSuppressionFile(mailNotToBeFiltered, hiPathDataSuppressionFile);
	
		//Assert
		Assertions.assertThat(isMailNotInSuppressionFile).isTrue();
	}
	
	@ParameterizedTest
	@NullAndEmptySource
	public void should_not_filter_blank_or_null_mail(String blankMail) {
		//Arrange
		Path hiPathDataSuppressionFile = Paths.get("src", "test", "resources", "filtering_Test_Rss", "0.txt");
		
		//Act
		Boolean isMailNotInSuppressionFile = suppressionService.notInSuppressionFile(blankMail, hiPathDataSuppressionFile);
	
		//Assert
		Assertions.assertThat(isMailNotInSuppressionFile).isTrue();
	}
	
	@Test
	public void should_uncheck_ioexception_when_generated_with_unexisting_file() {
		//Arrange
		String dummyMail = "mail";
		Path unexistingFile = Paths.get("src", "test", "resources", "I_dont_exist");
		
		//Act
		assertThrows(UncheckedIOException.class, () -> 
		suppressionService.notInSuppressionFile(dummyMail, unexistingFile));
	
		//Assert
		//=> Unchecked exception caught;
	}
	

	
}
