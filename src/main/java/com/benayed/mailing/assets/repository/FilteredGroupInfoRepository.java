package com.benayed.mailing.assets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.benayed.mailing.assets.entity.FilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.FilteredGroupInfoKey;

@Repository
public interface FilteredGroupInfoRepository extends JpaRepository<FilteredGroupInfoEntity, FilteredGroupInfoKey>{

	@Modifying
	@Query(value = "DELETE FROM FilteredGroupInfoEntity sfgi WHERE sfgi.id.suppressionInfoId = :suppressionInfoId")
	public void deleteAllBySuppressionInfoId(@Param("suppressionInfoId") Long suppressionInfoId);
}
