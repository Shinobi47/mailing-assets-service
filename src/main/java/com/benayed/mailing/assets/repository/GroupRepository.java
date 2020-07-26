package com.benayed.mailing.assets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.benayed.mailing.assets.entity.GroupEntity;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

}
