package com.benayed.mailing.assets.dto;

import com.benayed.mailing.assets.enums.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class SuppressionInfoDto {

	private Long id;
	private Long suppressionId;
	private String suppressionLocation;
	private Platform suppressionPlatform;

}
