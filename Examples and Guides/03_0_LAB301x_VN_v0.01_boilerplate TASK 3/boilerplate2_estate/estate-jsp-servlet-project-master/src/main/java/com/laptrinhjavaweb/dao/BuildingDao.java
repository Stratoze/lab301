package com.laptrinhjavaweb.dao;

import com.laptrinhjavaweb.builder.BuildingBuilder;
import com.laptrinhjavaweb.entity.BuildingEntity;
import com.laptrinhjavaweb.paging.Pageable;

import java.util.List;

public interface BuildingDao extends GenericDao<Integer, BuildingEntity> {
    List<BuildingEntity> findAll(Pageable pageable, BuildingBuilder builder);
}
