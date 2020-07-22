package com.benayed.mailing.assets.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.benayed.mailing.assets.enums.Platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
@Table(name = "FILTERED_GROUP_INFO")
public class FilteringInfoEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "filtered_data_group_generator")
	@SequenceGenerator(name="filtered_data_group_generator", sequenceName = "FILTERED_GROUP_INFO_PK_SEQ", allocationSize = 1)
	@Column(name = "FGI_ID")
	private Long id;
	
	@Column(name = "SUPPRESSION_ID")
	private Long suppressionId;
	
	@Column(name = "SUPPRESSION_LOCATION")
	private String suppressionLocation;
	
	@Column(name = "SUPPRESSION_PLATFORM")
	private Platform suppressionPlatform;
	
	@Column(name = "FILTERED_DATA_COUNT")
	private Long filteredDataCount;
	
	@ManyToOne
    @JoinColumn(name="FGI_GROUP_ID")
	private GroupEntity group;


}
