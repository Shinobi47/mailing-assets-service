package com.benayed.mailing.assets.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Embeddable
public class FilteredGroupInfoKey implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7987963139443733968L;

	@Column(name = "SFG_GROUP_ID", nullable = false, insertable = false, updatable = false)
    private Long groupId;
 
    @Column(name = "SFG_SINFO_ID", nullable = false, insertable = false, updatable = false)
    private Long suppressionInfoId;

}
 