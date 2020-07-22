package com.benayed.mailing.assets.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
@Table(name = "DATA_ASSET")
public class AssetEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "assets_pk_generator")
	@SequenceGenerator(name="assets_pk_generator", sequenceName = "DATA_ASSET_PK_SEQ", allocationSize = 1)
	@Column(name = "ASSET_ID")
	private Long id;
	
	@Column(name = "ASSET_NAME")
	private String name;
	
	@ToString.Exclude
	@OneToMany(mappedBy = "asset")
	private List<GroupEntity> groups;

}
