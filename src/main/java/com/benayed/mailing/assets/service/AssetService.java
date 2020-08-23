package com.benayed.mailing.assets.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.benayed.mailing.assets.dto.AssetDto;
import com.benayed.mailing.assets.dto.FilteredGroupInfoDto;
import com.benayed.mailing.assets.entity.FilteredGroupInfoEntity;
import com.benayed.mailing.assets.exception.TechnicalException;
import com.benayed.mailing.assets.repository.AssetRepository;
import com.benayed.mailing.assets.repository.DataItemRepository;
import com.benayed.mailing.assets.repository.FilteredGroupInfoRepository;
import com.benayed.mailing.assets.utils.DataMapper;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AssetService {

	private DataMapper dataMapper;
	private AssetRepository assetRepository;
	private FilteredGroupInfoRepository filteredGroupInfoRepository;
	private DataItemRepository dataItemRepository;
	
	public List<AssetDto> fetchAssetsWithGroups() {
		boolean shouldMapGroups = true;
		return assetRepository.findAll().stream().map(asset -> dataMapper.toDto(asset, shouldMapGroups)).collect(Collectors.toList());
		
	}
	
	public FilteredGroupInfoDto fetchFilteringInfos(Long groupId, Long suppressionId) {
		FilteredGroupInfoEntity entity = filteredGroupInfoRepository.findByGroup_IdAndSuppressionInfo_SuppressionId(groupId, suppressionId)
				.orElseThrow(() -> new TechnicalException("No FilteredGroupInfoEntity found with the given groupId and suppressionId"));
		
		Integer count = dataItemRepository.countByGroup_Id(entity.getId().getGroupId());

		return FilteredGroupInfoDto.builder()
				.filteredDataCount(entity.getFilteredDataCount())
				.suppressionInfoId(entity.getId().getSuppressionInfoId())
				.groupId(entity.getId().getGroupId())
				.suppressionId(entity.getSuppressionInfo().getSuppressionId())
				.originalDataCount(count)
				.build();		
	}
}
