package com.benayed.mailing.assets.controller;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.SuppressionInfoDto;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.enums.Platform;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.SuppressionDataRepository;
import com.benayed.mailing.assets.service.MailingDataService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1")
public class AssetsController {
	
	private MailingDataService mailingDataService;

	
	@GetMapping(path = "/assets/{id}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
	public void getAssets(@PathVariable(name = "id") String assetId, @RequestParam(name = "suppression-data-location") String suppressionDataLocation) {
//		try {
//			suppressionDataRepository.fetchHiPathSuppressionData(suppressionDataLocation);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@GetMapping(path = "/test2", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<DataItemDto> tsts(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size){
			try {
				return mailingDataService.getFilteredPaginatedData(1L, PageRequest.of(page, size), Platform.HiPath, "zip", 1L);
			} catch (UncheckedIOException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}
	
//	@PostMapping(path = "/assets/groups/suppression-infos", produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<?> tstss(@RequestBody SuppressionInfoDto suppressionInfo){
//		myService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionInfo.getSuppressionId(), suppressionInfo.getSuppressionLocation(), suppressionInfo.getSuppressionPlatform());

		@GetMapping(path = "/test3", produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<?> tstss(){
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(1L, "http://api.1318amethyst.com/suppdownload.php?z=ODg1MjkxMDA4fDI3MTI5NXwzODg1fDk0OTUwNjAxMA", Platform.HiPath);
		
		return null;

	}
		
		
	@GetMapping(path = "test4")
	public ResponseEntity<?> tssssst(){
		mailingDataService.deleteSuppressionInformations(1L);
		return null;
	}

}
