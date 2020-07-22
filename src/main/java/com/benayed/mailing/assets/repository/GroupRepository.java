package com.benayed.mailing.assets.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.benayed.mailing.assets.entity.GroupEntity;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

}
