package com.benayed.mailing.assets.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor @Builder
@Entity
@Table(name = "SUPPRESSION_FILTERED_GROUP")
public class FilteredGroupInfoEntity {

	@EmbeddedId
	private FilteredGroupInfoKey id;
	
	@ManyToOne
	@MapsId("SFG_GROUP_ID")
	@JoinColumn(name = "SFG_GROUP_ID")
	private GroupEntity group;

	@ManyToOne
	@MapsId("SFG_SINFO_ID")
	@JoinColumn(name = "SFG_SINFO_ID")
	private SuppressionInfoEntity suppressionInfo;
    
    @Column(name =  "FILTERED_DATA_COUNT")
    private Integer filteredDataCount;

}
