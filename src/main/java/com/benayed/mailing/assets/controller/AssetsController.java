package com.benayed.mailing.assets.controller;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.benayed.mailing.assets.dto.DataItemDto;
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
	
	private SuppressionDataRepository suppressionDataRepository;
	private DataItemRepository dataItemRepository;
	private MailingDataService myService;

	
	@GetMapping(path = "/assets/{id}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
	public void getAssets(@PathVariable(name = "id") String assetId, @RequestParam(name = "suppression-data-location") String suppressionDataLocation) {
		try {
			suppressionDataRepository.fetchHiPathSuppressionData(suppressionDataLocation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GetMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<DataItemEntity> tst(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size) {
			Page<DataItemEntity> p = dataItemRepository.findByGroup_id(1L, PageRequest.of(page, size));
		return p;

	}
	
	@GetMapping(path = "/test2", produces = MediaType.APPLICATION_JSON_VALUE)
	public Page<DataItemDto> tsts(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size){
			try {
				return myService.getFilteredPaginatedData(1L, PageRequest.of(page, size), Platform.HiPath, "zip", "http://api.1318amethyst.com/suppdownload.php?z=ODg1MjkxMDA4fDI3MTI5NXwzODg1fDk0OTUwNjAxMA");
			} catch (UncheckedIOException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

	}

}
