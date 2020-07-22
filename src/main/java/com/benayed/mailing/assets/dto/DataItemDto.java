package com.benayed.mailing.assets.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class DataItemDto {

	private Long id;
	private String prospectEmail;
	private String isp;
	
	@JsonIgnore
	private GroupDto group;
}
