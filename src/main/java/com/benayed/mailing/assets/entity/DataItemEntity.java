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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
@Table(name = "DATA_ITEM")
public class DataItemEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "data_item_generator")
	@SequenceGenerator(name="data_item_generator", sequenceName = "DATA_ITEM_PK_SEQ", allocationSize = 1)
	@Column(name = "ITEM_ID")
	private Long id;
	
	@Column(name = "PROSPECT_EMAIL")
	private String prospectEmail;
	
	@Column(name = "EMAIL_ISP")
	private String isp;
	
	@ManyToOne
    @JoinColumn(name="ITEM_GROUP_ID")
	private GroupEntity group;

}
