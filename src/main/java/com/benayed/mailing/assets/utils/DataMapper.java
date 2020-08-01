package com.benayed.mailing.assets.utils;

import org.springframework.stereotype.Component;

import com.benayed.mailing.assets.dto.AssetDto;
import com.benayed.mailing.assets.dto.DataItemDto;
import com.benayed.mailing.assets.dto.GroupDto;
import com.benayed.mailing.assets.entity.AssetEntity;
import com.benayed.mailing.assets.entity.DataItemEntity;
import com.benayed.mailing.assets.entity.GroupEntity;

@Component
public class DataMapper {

	public DataItemDto toDto(DataItemEntity entity) {
		return DataItemDto.builder()
				.id(entity.getId())
				.isp(entity.getIsp())
				.prospectEmail(entity.getProspectEmail())
				.group(toDto(entity.getGroup())).build();
	}

	public GroupDto toDto(GroupEntity entity) {
		return GroupDto.builder()
				.id(entity.getId())
				.name(entity.getName())
				.creationDate(entity.getCreationDate())
				.asset(toDto(entity.getAsset())).build();
	}
	
	public AssetDto toDto(AssetEntity entity) {
		return AssetDto.builder()
				.id(entity.getId())
				.name(entity.getName()).build();
	}
	
	
	public DataItemEntity toEntity(DataItemDto dto) {
		return DataItemEntity.builder()
				.id(dto.getId())
				.isp(dto.getIsp())
				.prospectEmail(dto.getProspectEmail())
				.group(toEntity(dto.getGroup())).build();
	}

	public GroupEntity toEntity(GroupDto dto) {
		return GroupEntity.builder()
				.id(dto.getId())
				.name(dto.getName())
				.creationDate(dto.getCreationDate())
				.asset(toEntity(dto.getAsset())).build();
	}
	
	public AssetEntity toEntity(AssetDto dto) {
		return AssetEntity.builder()
				.id(dto.getId())
				.name(dto.getName()).build();
	}
}
