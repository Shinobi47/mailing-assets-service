package com.benayed.mailing.assets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.benayed.mailing.assets.entity.DataItemEntity;

@Repository
public interface DataItemRepository extends JpaRepository<DataItemEntity, Long> {

	@Query(value = "select item_id, item_group_id, email_isp, prospect_email from data_item left outer join data_group  on item_group_id=group_id where group_id in (:groupsIds) Order By item_id LIMIT :limit OFFSET :offset", nativeQuery = true)
	public List<DataItemEntity> findByGroup_idOrderByItemId(@Param(value = "groupsIds") List<Long> groupsIds, @Param(value = "offset") Integer offset, @Param(value = "limit") Integer limit);
	
	public Integer countByGroup_Id(Long id);
}
 