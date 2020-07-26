package com.benayed.mailing.assets.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.benayed.mailing.assets.entity.SuppressionInfoEntity;

@Repository
public interface SuppressionInfoRepository extends JpaRepository<SuppressionInfoEntity, Long>{

	public Optional<SuppressionInfoEntity> findBySuppressionId(Long suppressionId);
}
