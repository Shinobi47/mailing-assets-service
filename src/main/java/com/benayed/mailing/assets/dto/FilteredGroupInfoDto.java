package com.benayed.mailing.assets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class FilteredGroupInfoDto {

	private Long groupId;
	private Long suppressionInfoId;
	private Long suppressionId;
	private Integer originalDataCount;
	private Integer filteredDataCount;
}
