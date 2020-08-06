package com.benayed.mailing.assets.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.SuppressionFilesLocationDto;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.GroupEntity;
import com.benayed.mailing.assets.entity.SuppressionInfoEntity;
import com.benayed.mailing.assets.enums.Platform;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.FilteredGroupInfoRepository;
import com.benayed.mailing.assets.repository.GroupRepository;
import com.benayed.mailing.assets.repository.SuppressionInfoRepository;
import com.benayed.mailing.assets.service.MailingDataService;
import com.benayed.mailing.assets.service.SuppressionService;
import com.benayed.mailing.assets.utils.DataMapper;

@ExtendWith(MockitoExtension.class)
public class MailingDataServiceTest {

	private MailingDataService mailingDataService;
	
	@Mock
	private DataItemRepository dataItemRepository;
	
	private DataMapper dataMapper = new DataMapper();
	
	@Mock
	private SuppressionInfoRepository suppressionInfoRepository;

	@Mock
	private FilteredGroupInfoRepository filteredGroupInfoRepository;
	
	@Mock
	private GroupRepository groupRepository;
	
	@Mock
	private SuppressionService suppressionService;
	
	@Captor
	private ArgumentCaptor<SuppressionInfoEntity> suppInfoEntityCaptor;
	
	@Captor
	private ArgumentCaptor<FilteredGroupInfoEntity> filteredGroupInfoEntityCaptor;
	
	@BeforeEach
	private void init() {
		mailingDataService = new MailingDataService(dataItemRepository, dataMapper, suppressionInfoRepository, filteredGroupInfoRepository, groupRepository, suppressionService);
	
	}
	
	
	/////	TEST CASES FOR  deleteSuppressionInformations /////
	
	@Test
	public void should_throw_exception_when_no_suppressionInfo_found_for_the_given_suppressionId() {
		//Arrange
		Long suppressionId = 1L;
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty());
		
		//Act
		assertThrows(NoSuchElementException.class, () -> 
		mailingDataService.deleteSuppressionInformations(suppressionId));
		
		//Assert
		// => Exception thrown
		
	}

	@Test
	public void should_throw_exception_when_calling_deleteSuppressionInformations_with_null_suppressionId() {
		//Arrange
		Long suppressionId = null;
		
		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.deleteSuppressionInformations(suppressionId));
		
		//Assert
		// => Exception thrown
	}
	
	@Test
	public void should_informations_related_to_suppressionId_successfully_when_data_exists_in_DB() {
		//Arrange
		Long suppressionId = 1L; // suppressionInfo is going to be fetched with this
		Long suppressionInfoId = 2L; // suppression informations are going to be deleted with this
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.<SuppressionInfoEntity>of(SuppressionInfoEntity.builder().id(suppressionInfoId).suppressionLocation("dummy").build()));
		
		//Act
		mailingDataService.deleteSuppressionInformations(suppressionId);
		
		//Assert
		verify(suppressionInfoRepository, times(1)).findBySuppressionId(suppressionId);
		verify(filteredGroupInfoRepository, times(1)).deleteAllBySuppressionInfoId(suppressionInfoId);
		verify(suppressionInfoRepository, times(1)).deleteById(suppressionInfoId);
		// => Exception thrown
	}

	/////	TEST CASES FOR  updateAllGroupsFilteringCountWithNewSuppressionData /////

	@Test
	public void should_throw_exception_when_executing_update_with_null_suppressionId() {
		//Arrange
		Long suppressionId = null;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform));
		
		//Assert
		//=> Exception thrown
	}
	
	@Test
	public void should_throw_exception_when_executing_update_with_null_platform() {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = null;

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform));
		
		//Assert
		//=> Exception thrown
	}
	
	@Test
	public void should_throw_exception_when_executing_update_with_existing_suppression_data() { //new suppression infos are created while updating
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;

		Optional<SuppressionInfoEntity> existingSuppressionInfo = Optional.<SuppressionInfoEntity>of(SuppressionInfoEntity.builder().build());
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(existingSuppressionInfo);

		//Act
		assertThrows(TechnicalException.class, () -> 
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform));
		
		//Assert
		//=> Exception thrown

	}
	
	@Test
	public void should_throw_exception_when_hipathservice_returns_null_suppressionFilesLocation() {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;
		SuppressionFilesLocationDto nullarg = null;
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty());
		when(suppressionService.fetchSuppressionData(suppressionId, suppressionUrl)).thenReturn(nullarg);

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform));
		
		//Assert
		//=> Exception thrown

	}
	
	@Test
	public void should_throw_exception_when_hipathservice_returns_suppressionFilesLocation_with_null_data_Path() {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;
		SuppressionFilesLocationDto sflWithNullDataPath = SuppressionFilesLocationDto.builder().dataPath(null).build();
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty());
		when(suppressionService.fetchSuppressionData(suppressionId, suppressionUrl)).thenReturn(sflWithNullDataPath);

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform));
		
		//Assert
		//=> Exception thrown

	}
	
	@Test
	public void should_throw_exception_when_hipathservice_returns_suppressionFilesLocation_having_data_Path_with_null_parent() {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;
		SuppressionFilesLocationDto sflWithNullDataPath = SuppressionFilesLocationDto.builder().dataPath(Paths.get("pathWithNoParent")).build();
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty());
		when(suppressionService.fetchSuppressionData(suppressionId, suppressionUrl)).thenReturn(sflWithNullDataPath);

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform));
		
		//Assert
		//=> Exception thrown

	}

	
	
	
	@Test
	public void should_save_suppressionInfo_with_correct_arguments_when_updating_groups_with_new_suppression() {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;
		Path parentSuppLocation = Paths.get("dummy");
		SuppressionFilesLocationDto validDataSuppLocation = SuppressionFilesLocationDto.builder().dataPath(parentSuppLocation.resolve("0.txt")).build();
		
		
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty()); //shouldn't exist cuz it's gonna be created
		when(suppressionService.fetchSuppressionData(suppressionId, suppressionUrl)).thenReturn(validDataSuppLocation);

		//Act

		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform);
		
		//Assert
		verify(suppressionInfoRepository, times(1)).save(suppInfoEntityCaptor.capture());
		SuppressionInfoEntity savedSuppInfoEntity = suppInfoEntityCaptor.getValue();
		Assertions.assertThat(savedSuppInfoEntity.getSuppressionId()).isEqualTo(suppressionId);
		Assertions.assertThat(savedSuppInfoEntity.getSuppressionLocation()).isEqualTo(parentSuppLocation.toString());
		Assertions.assertThat(savedSuppInfoEntity.getSuppressionPlatform()).isEqualTo(platform);

	}
	
	
	@Test
	public void should_call_repositories_with_proper_arguments_and_save_proper_filtered_data_count_when_updating_groups_with_new_suppression() {
		//Arrange
		Long suppressionId = 1L;
		String suppressionUrl = "url";
		Platform platform = Platform.HIPATH;
		Path suppressionDataPath = Paths.get("parent", "dummy");
		
		DataItemEntity itemToRemain = DataItemEntity.builder().prospectEmail("mail1").build();
		DataItemEntity itemToBeRemoved = DataItemEntity.builder().prospectEmail("mail2").build();
		List<DataItemEntity> DataToBeFiltered = Arrays.asList(itemToRemain, itemToBeRemoved);
		GroupEntity group = GroupEntity.builder().dataItems(DataToBeFiltered).build();
		
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty()); //shouldn't exist cuz it's gonna be created
		when(suppressionService.fetchSuppressionData(suppressionId, suppressionUrl)).thenReturn(SuppressionFilesLocationDto.builder().dataPath(suppressionDataPath).build());
		when(groupRepository.findAll()).thenReturn(Arrays.asList(group));
		when(suppressionInfoRepository.save(Mockito.any())).thenReturn(new SuppressionInfoEntity());
		when(suppressionService.notInSuppressionFile(itemToRemain.getProspectEmail(), suppressionDataPath)).thenReturn(true);
		
		//Act
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionId, suppressionUrl, platform);
		
		//Assert
		verify(suppressionService, times(1)).notInSuppressionFile(itemToRemain.getProspectEmail(), suppressionDataPath);
		verify(suppressionService, times(1)).notInSuppressionFile(itemToBeRemoved.getProspectEmail(), suppressionDataPath);
		
		verify(filteredGroupInfoRepository, times(1)).save(filteredGroupInfoEntityCaptor.capture());
		Assertions.assertThat(filteredGroupInfoEntityCaptor.getValue().getFilteredDataCount()).isEqualTo(1);
		

	}
	
	/////	TEST CASES FOR  getFilteredPaginatedData /////


	@Test
	public void should_throw_exception_when_fetching_data_with_null_suppressionId() throws IOException {
		//Arrange
		Long groupId = 7L;
		Pageable pageable = PageRequest.of(1, 1);
		Long suppressionId = null;

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.getFilteredPaginatedData(groupId, pageable, suppressionId));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_throw_exception_when_no_suppressionInfo_found_for_the_given_suppressionId_when_fetching_filteredPaginatedData() throws IOException {
		//Arrange
		Long groupId = 7L;
		Pageable pageable = PageRequest.of(1, 1);
		Long suppressionId = 70L;
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.empty());


		//Act
		assertThrows(NoSuchElementException.class, () -> 
		mailingDataService.getFilteredPaginatedData(groupId, pageable, suppressionId));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_throw_exception_when_fetching_data_but_suppressionInfo_has_null_suppressionLocation() throws IOException {
		//Arrange
		Long groupId = 7L;
		Pageable pageable = PageRequest.of(1, 1);
		Long suppressionId = 70L;
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.<SuppressionInfoEntity>ofNullable(SuppressionInfoEntity.builder().build()));

		//Act
		assertThrows(NoSuchElementException.class, () -> 
		mailingDataService.getFilteredPaginatedData(groupId, pageable, suppressionId));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_throw_exception_when_fetching_data_with_null_groupId() throws IOException {
		//Arrange
		Long groupId = null;
		Pageable pageable = PageRequest.of(1, 1);
		Long suppressionId = 70L;
		
		when(suppressionService.getSuppressionFilesFromDirectory(Mockito.any())).thenReturn(SuppressionFilesLocationDto.builder().build());
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.<SuppressionInfoEntity>ofNullable(SuppressionInfoEntity.builder().suppressionLocation("dummySL").build()));
		
		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.getFilteredPaginatedData(groupId, pageable, suppressionId));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_throw_exception_when_fetching_data_with_null_pageable() throws IOException {
		//Arrange
		Long groupId = 7L;
		Pageable pageable = null;
		Long suppressionId = 70L;
		
		when(suppressionService.getSuppressionFilesFromDirectory(Mockito.any())).thenReturn(SuppressionFilesLocationDto.builder().build());
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.<SuppressionInfoEntity>ofNullable(SuppressionInfoEntity.builder().suppressionLocation("dummySL").build()));

		//Act
		assertThrows(IllegalArgumentException.class, () -> 
		mailingDataService.getFilteredPaginatedData(groupId, pageable, suppressionId));
		
		//Assert
		//=> exception thrown
	}
	
	@Test
	public void should_fetch_data_page_then_fetch_suppression_data_then_filter_fetched_data_thenfinally_return_new_page_with_expected_params() throws IOException {
		//Arrange
		Long groupId = 7L;
		Pageable pageable = PageRequest.of(1, 1);
		Long suppressionId = 70L;

		SuppressionInfoEntity ExistingSuppressionData = SuppressionInfoEntity.builder().suppressionLocation("suppression_parent_folder").build();
		SuppressionFilesLocationDto suppDataSupposedLocation = SuppressionFilesLocationDto.builder().dataPath(Paths.get("DummyDataLocation")).build();
		DataItemEntity itemToRemain = DataItemEntity.builder().prospectEmail("mail1").build();
		DataItemEntity itemToBeRemoved = DataItemEntity.builder().prospectEmail("mail2").build();
		List<DataItemEntity> dataToBeFiltered = Arrays.asList(itemToRemain, itemToBeRemoved);
		
		
		when(suppressionInfoRepository.findBySuppressionId(suppressionId)).thenReturn(Optional.<SuppressionInfoEntity>ofNullable(ExistingSuppressionData));
		when(suppressionService.getSuppressionFilesFromDirectory(Paths.get(ExistingSuppressionData.getSuppressionLocation()))).thenReturn(suppDataSupposedLocation);
		
		when(dataItemRepository.findByGroup_id(groupId, pageable)).thenReturn(new PageImpl<DataItemEntity>(dataToBeFiltered));
		when(suppressionService.notInSuppressionFile(itemToRemain.getProspectEmail(), suppDataSupposedLocation.getDataPath())).thenReturn(true);

		//Act
		Page<DataItemDto> filteredPaginatedData = mailingDataService.getFilteredPaginatedData(groupId, pageable, suppressionId);
		
		//Assert
		Assertions.assertThat(filteredPaginatedData).hasSize(1); //one item has been filtered and one remains
		Assertions.assertThat(filteredPaginatedData.getTotalElements()).isEqualTo(dataToBeFiltered.size()); // repagination keeps previous params (ie db repository params)
		Assertions.assertThat(filteredPaginatedData.getPageable()).isEqualTo(pageable);
	}
	
	
}
