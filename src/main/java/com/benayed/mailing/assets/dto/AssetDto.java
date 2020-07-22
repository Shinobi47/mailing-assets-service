package com.benayed.mailing.assets.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AssetDto {

	private Long id;
	private String name;
	private List<GroupDto> group;
}
