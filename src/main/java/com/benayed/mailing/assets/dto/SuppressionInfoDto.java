package com.benayed.mailing.assets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class SuppressionInfoDto {

	private Long suppressionId;
	private String suppressionLocation;

}
