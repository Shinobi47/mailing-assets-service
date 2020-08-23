package com.benayed.mailing.assets.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

import com.benayed.mailing.assets.dto.AssetDto;
import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.SuppressionInfoDto;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.service.AssetService;
import com.benayed.mailing.assets.service.MailingDataService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1")
public class AssetsController { 
	
	private MailingDataService mailingDataService;
	private AssetService assetService; 

	
	@GetMapping(path = "/groups/{id}/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getData(@PathVariable("id") Long groupId, @RequestParam(name = "page") int page, @RequestParam(name = "size") int size, @RequestParam Boolean filtered, @RequestParam(required = false) Long suppressionId){
		if(Boolean.TRUE.equals(filtered)) {
			Page<DataItemDto> filteredPaginatedData = mailingDataService.getFilteredPaginatedData(groupId, PageRequest.of(page, size), suppressionId);
			return filteredPaginatedData.isEmpty() ? 
					new ResponseEntity<>(HttpStatus.NOT_FOUND) : 
						new ResponseEntity<Page<DataItemDto>>(filteredPaginatedData, HttpStatus.OK);
		}

		throw new TechnicalException("Only filtered data calls are supported ATM");
	}
	
	@PostMapping(path = "/suppression-info", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> postSuppression(@RequestBody SuppressionInfoDto suppressionInfo){
		mailingDataService.updateAllGroupsFilteringCountWithNewSuppressionData(suppressionInfo.getSuppressionId(), suppressionInfo.getSuppressionLocation());
		return new ResponseEntity<String>(HttpStatus.CREATED);

	}
		
		
	@DeleteMapping(path = "/suppression-info/{id}")
	public ResponseEntity<?> deleteSuppression(@PathVariable Long id){
		mailingDataService.deleteSuppressionInformations(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}


	
	@GetMapping(path = "/assets/groups")
	public ResponseEntity<?> getAssetsWithGrps(){
		List<AssetDto> assetsWithGroups = assetService.fetchAssetsWithGroups();
		return assetsWithGroups.isEmpty() 
				? new ResponseEntity<>(HttpStatus.NOT_FOUND) 
						: new ResponseEntity<>(assetsWithGroups, HttpStatus.OK);
	}
	
	@GetMapping(path = "/filtering/{groupId}-{suppressionId}")
	public ResponseEntity<?> get(@PathVariable Long groupId, @PathVariable Long suppressionId){
		System.out.println(groupId + " and " + suppressionId);
		return new ResponseEntity<>(assetService.fetchFilteringInfos(groupId, suppressionId), HttpStatus.OK);
	}

}
