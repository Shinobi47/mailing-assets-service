package com.benayed.mailing.assets.test;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

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
		
		//Act
//		Assertions.assertThrows(IllegalArgumentException.class, () -> 
//		suppressionDataRepository.fetchHiPathSuppressionData(zipFileUrl));
		
		//Assert
		//=> exception thrown
	}

	@Test
	public void tst() throws IOException {
		//Arrange
		String zipFileUrl = "url";
		Mockito.when(restTemplate.execute(zipFileUrl, HttpMethod.GET, null, Mockito.any())).thenReturn(new File(""));
		
		//Act
//		suppressionDataRepository.fetchHiPathSuppressionData(zipFileUrl);
		
		//Assert
		//=> exception thrown
	}
}
