package com.benayed.mailing.assets.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.benayed.mailing.assets.entity.DataItemEntity;

@Repository
public interface DataItemRepository extends JpaRepository<DataItemEntity, Long> {

	public Page<DataItemEntity> findByGroup_id(Long groupId, Pageable pageable);
}
