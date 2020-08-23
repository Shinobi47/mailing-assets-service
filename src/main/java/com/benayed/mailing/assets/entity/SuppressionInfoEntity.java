package com.benayed.mailing.assets.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
@Table(name = "SUPPRESSION_INFO")
public class SuppressionInfoEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "suppression_info_pk_generator")
	@SequenceGenerator(name="suppression_info_pk_generator", sequenceName = "SUPPRESSION_INFO_PK_SEQ", allocationSize = 1)
	@Column(name = "SINFO_ID")
	private Long id;
	
	@Column(name = "SUPPRESSION_ID")
	private Long suppressionId;
	
	@Column(name = "SUPPRESSION_LOCATION")
	private String suppressionLocation;

}
