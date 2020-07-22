package com.benayed.mailing.assets.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class GroupDto {
	private Long id;
	private String name;
	private LocalDateTime creationDate;
	private AssetDto asset;
	private List<DataItemDto> dataItems;

}
