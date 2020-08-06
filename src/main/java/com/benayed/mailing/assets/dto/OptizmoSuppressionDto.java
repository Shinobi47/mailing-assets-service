package com.benayed.mailing.assets.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class OptizmoSuppressionDto {

	@JsonAlias("download_link")
	private String downloadLink;
}
