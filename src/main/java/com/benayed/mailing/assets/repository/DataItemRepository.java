package com.benayed.mailing.assets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.benayed.mailing.assets.entity.DataItemEntity;

public interface DataItemRepository extends JpaRepository<DataItemEntity, Long> {

	public Page<DataItemEntity> findByGroup_id(Long groupId, Pageable pageable);
}
