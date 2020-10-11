package com.benayed.mailing.assets.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	@GetMapping(path = "/groups/{ids}/data", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getData(@PathVariable("ids") String stringGroupsIds, @RequestParam Boolean filtered, @RequestParam(name = "suppression-id") Long suppressionId, @RequestParam(required = false) Integer offset, @RequestParam(required = false) Integer limit){
		List<Long> groupsIds = Arrays.asList(stringGroupsIds.split(",")).stream().map(Long::parseLong).collect(Collectors.toList());
		if(Boolean.TRUE.equals(filtered)) {
			List<DataItemDto> filteredData = mailingDataService.getFilteredData(groupsIds, suppressionId, offset, limit);

			return filteredData.isEmpty() ? 
					new ResponseEntity<>(HttpStatus.NOT_FOUND) : 
						new ResponseEntity<List<DataItemDto>>(filteredData, HttpStatus.OK);
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
