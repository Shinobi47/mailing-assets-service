package com.benayed.mailing.assets.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.benayed.mailing.assets.utils.FileUtils;
import static org.apache.commons.io.FileUtils.deleteDirectory;

@ExtendWith(MockitoExtension.class)
public class FileUtilsTest {
	
	private FileUtils fileUtils = new FileUtils();
	
	private final String TEST_FILE_NAME = "FileUtils_predicate_Test_Rss.txt";
	
	
	
	////////////////////////      getFileRespectingAPredicateFromDirectory test cases      /////////////////////

	
	@Test
	public void should_throw_exception_when_looking_for_a_file_in_null_directory(){
		//Arrange
		Path parentDirToWalk = null;
		Predicate<Path> filePattern = filePath -> TEST_FILE_NAME.equals(filePath.getFileName().getFileName().toString());
		
		//Act
		assertThrows(IllegalArgumentException.class, () ->
		fileUtils.getFileRespectingAPredicateFromDirectory(parentDirToWalk, filePattern));
		
		//Assert
		//ExceptionThrown
		
	}
	
	@Test
	public void should_throw_exception_when_looking_for_a_file_with_null_pattern(){
		//Arrange
		Path parentDirToWalk = Paths.get("src", "test", "resources");
		Predicate<Path> filePattern = null;
		
		//Act
		assertThrows(IllegalArgumentException.class, () ->
		fileUtils.getFileRespectingAPredicateFromDirectory(parentDirToWalk, filePattern));
		
		//Assert
		//ExceptionThrown
		
	}
	
	@Test
	public void should_throw_exception_when_looking_for_a_file_in_unvalid_directory(){
		//Arrange
		Path parentDirToWalk = Paths.get("this_is_a_file_or_an_unexisting_directory");
		Predicate<Path> filePattern = filePath -> TEST_FILE_NAME.equals(filePath.getFileName().getFileName().toString());
		
		//Act
		assertThrows(IllegalArgumentException.class, () ->
		fileUtils.getFileRespectingAPredicateFromDirectory(parentDirToWalk, filePattern));
		
		//Assert
		//ExceptionThrown
	}
	
	@Test
	public void should_return_correct_path_for_existing_file(){
		//Arrange
		Path parentDirToWalk = Paths.get("src", "test", "resources");
		Predicate<Path> filePattern = filePath -> TEST_FILE_NAME.equals(filePath.getFileName().getFileName().toString());
		
		//Act
		Path fileRespectingAPredicateFromDirectory = fileUtils.getFileRespectingAPredicateFromDirectory(parentDirToWalk, filePattern);
		
		//Assert
		Path expectedCorrectFilePath = parentDirToWalk.resolve(TEST_FILE_NAME);
		
		Assertions.assertThat(fileRespectingAPredicateFromDirectory).exists();
		Assertions.assertThat(fileRespectingAPredicateFromDirectory).isEqualTo(expectedCorrectFilePath);
	}

	
	////////////////////////      doesFileNameContainDomains test cases      /////////////////////

	
	@Test
	public void should_return_false_when_path_is_null() {
		//Arrange
		Path path = null;
		
		//Act
		boolean doesFileNameContainDomains = fileUtils.doesFileNameContainDomains(path);
		
		//Assert
		Assertions.assertThat(doesFileNameContainDomains).isFalse();
	}
	
	@Test
	public void should_return_false_when_path_is_empty() {
		//Arrange
		Path path = Paths.get("");

		//Act
		boolean doesFileNameContainDomains = fileUtils.doesFileNameContainDomains(path);
		
		//Assert
		Assertions.assertThat(doesFileNameContainDomains).isFalse();
	}
	
	@Test
	public void should_return_false_when_filename_doesnt_contain_domains() {
		//Arrange
		Path path = Paths.get("src", "fileName.zip");

		//Act
		boolean doesFileNameContainDomains = fileUtils.doesFileNameContainDomains(path);
		
		//Assert
		Assertions.assertThat(doesFileNameContainDomains).isFalse();
	}
	
	@Test
	public void should_return_false_when_path_is_directory() {
		//Arrange
		Path path = Paths.get("src","test","resources", "domains_DON_NOT_DELETE_OR_RENAME");
		
		//Act
		boolean doesFileNameContainDomains = fileUtils.doesFileNameContainDomains(path);
		
		//Assert
		Assertions.assertThat(doesFileNameContainDomains).isFalse();
	}
	
	@Test
	public void should_return_true_when_filename_contains_domains() {
		//Arrange
		Path path = Paths.get("src", "Sbatalouj_domains_skerdla.zip");

		//Act
		boolean doesFileNameContainDomains = fileUtils.doesFileNameContainDomains(path);
		
		//Assert
		Assertions.assertThat(doesFileNameContainDomains).isTrue();
	}
	
	@Test
	public void should_return_true_when_filename_contains_domains_case_insensitive() {
		//Arrange
		Path path = Paths.get("src", "Benayed_DoMaInS.zip");

		//Act
		boolean doesFileNameContainDomains = fileUtils.doesFileNameContainDomains(path);
		
		//Assert
		Assertions.assertThat(doesFileNameContainDomains).isTrue();
	}
	
	////////////////////////      unzip test cases      /////////////////////
	
	@Test
	public void should_throw_exception_when_unzipping_with_null_zipFile() {
		//Arrange
		Path zipFile = null;
		Path unzipDestination = Paths.get("dummy");

		//Act
		assertThrows(IllegalArgumentException.class, () ->
		fileUtils.unzip(zipFile, unzipDestination));
		
		//Assert
		// => exception thrown
	}
	
	@Test
	public void should_throw_exception_when_unzipping_with_null_unzip_location() {
		//Arrange
		Path zipFile = Paths.get("dummy");
		Path unzipDestination = null;

		//Act
		assertThrows(IllegalArgumentException.class, () ->
		fileUtils.unzip(zipFile, unzipDestination));
		
		//Assert
		// => exception thrown
	}
	
	@Test
	public void should_throw_exception_when_unzipping_with_null_unzip_loczation() {
		//Arrange
		Path zipFile = Paths.get("noSuchFileException");
		Path unzipDestination =  Paths.get("dummy");

		//Act
		assertThrows(UncheckedIOException.class, () ->
		fileUtils.unzip(zipFile, unzipDestination));
		
		//Assert
		// => exception thrown
	}
	
	
	
	@Test
	public void should_() throws IOException{
		//Arrange
		Path zipFilePath = Paths.get("src", "test", "resources", "unzip_test_file.zip");
		String fileName1InZip = "file1.txt";
		String fileName2InZip = "file2.txt";		
		Path unzipDestination = Paths.get("src", "test", "resources", "toBeDeleted");
		Path temporaryZipFile = copyZipTestFileIntoTemporaryLocation(unzipDestination, zipFilePath); //because our service deletes it when unzipped

		//Act
		List<Path> twoFilesList = fileUtils.unzip(temporaryZipFile, unzipDestination);
		
		//Assert
		Assertions.assertThat(twoFilesList).hasSize(2);
		Consumer<String> isFile1 = fileName -> Assertions.assertThat(fileName).isEqualTo(fileName1InZip);
		Consumer<String> isFile2 = fileName -> Assertions.assertThat(fileName).isEqualTo(fileName2InZip);
		Assertions.assertThat(twoFilesList.get(0).getFileName().toString()).satisfiesAnyOf(isFile1, isFile2);
		Assertions.assertThat(twoFilesList.get(1).getFileName().toString()).satisfiesAnyOf(isFile1, isFile2);
		
		deleteDirectory(unzipDestination.toFile());
	}
	
	
	private Path copyZipTestFileIntoTemporaryLocation(Path unzipLocation, Path testZipFilePath) throws IOException {
		Path tempZip = unzipLocation.resolve(testZipFilePath.getFileName());
		Files.createDirectory(unzipLocation);
		Files.copy(testZipFilePath, tempZip);
		return tempZip;
	}

}
