package com.benayed.mailing.assets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.benayed.mailing.assets.entity.SuppressionFilteredGroupInfoEntity;
import com.benayed.mailing.assets.entity.SuppressionFilteredGroupInfoKey;

@Repository
public interface SuppressionFilteredGroupInfoRepository extends JpaRepository<SuppressionFilteredGroupInfoEntity, SuppressionFilteredGroupInfoKey>{

}
