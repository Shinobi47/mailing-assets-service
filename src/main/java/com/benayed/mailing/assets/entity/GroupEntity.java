package com.benayed.mailing.assets.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "DATA_GROUP")
public class GroupEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "group_generator")
	@SequenceGenerator(name="group_generator", sequenceName = "DATA_GROUP_PK_SEQ", allocationSize = 1)
	@Column(name = "GROUP_ID")
	private Long id;
	
	@Column(name = "GROUP_NAME")
	private String name;
	
	@Column(name = "GROUP_CREAT_DATE")
	private LocalDateTime creationDate;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="GROUP_ASSET_ID")
	private AssetEntity asset;
	
	@ToString.Exclude
	@OneToMany(mappedBy = "group")
	private List<DataItemEntity> dataItems;
	
	@ToString.Exclude
	@OneToMany(mappedBy = "group")
    private List<FilteredGroupInfoEntity> filteredDataInfo;

}
