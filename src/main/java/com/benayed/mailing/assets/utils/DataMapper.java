package com.benayed.mailing.assets.utils;

import java.util.List;
import java.util.stream.Collectors;

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
		return entity == null ? null :
			DataItemDto.builder()
				.id(entity.getId())
				.isp(entity.getIsp())
				.prospectEmail(entity.getProspectEmail())
				.group(toDto(entity.getGroup())).build();
	}

	public GroupDto toDto(GroupEntity entity) {
		return entity == null ? null :
			GroupDto.builder()
				.id(entity.getId())
				.name(entity.getName())
				.creationDate(entity.getCreationDate()).build();
	}
	
	public GroupDto toDto(GroupEntity entity, boolean shouldMapAssets) {
		GroupDto dto = toDto(entity);
		if(shouldMapAssets) {
			dto.setAsset(toDto(entity.getAsset()));
		}
		return dto;
	}
	
	public AssetDto toDto(AssetEntity entity) {
		return entity == null ? null :
			AssetDto.builder()
				.id(entity.getId())
				.name(entity.getName()).build();
	}
	
	public AssetDto toDto(AssetEntity entity, boolean shouldMapGroups) {
		AssetDto dto = toDto(entity);
		if(shouldMapGroups) {
			List<GroupDto> groups = entity.getGroups() == null ? null :
				entity.getGroups().stream().map(this::toDto).collect(Collectors.toList());
			dto.setGroup(groups);
		}
		return dto;
	}
	
	
	public DataItemEntity toEntity(DataItemDto dto) {
		return dto == null ? null :
			DataItemEntity.builder()
				.id(dto.getId())
				.isp(dto.getIsp())
				.prospectEmail(dto.getProspectEmail())
				.group(toEntity(dto.getGroup())).build();
	}

	public GroupEntity toEntity(GroupDto dto) {
		return dto == null ? null :
			GroupEntity.builder()
				.id(dto.getId())
				.name(dto.getName())
				.creationDate(dto.getCreationDate())
				.asset(toEntity(dto.getAsset())).build();
	}
	
	public AssetEntity toEntity(AssetDto dto) {
		return dto == null ? null :
			AssetEntity.builder()
				.id(dto.getId())
				.name(dto.getName()).build();
	}
}
