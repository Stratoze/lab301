package com.laptrinhjavaweb.service;

import com.laptrinhjavaweb.builder.BuildingBuilder;
import com.laptrinhjavaweb.dto.BuildingDto;
import com.laptrinhjavaweb.paging.Pageable;

import java.util.List;

public interface BuildingService extends GenericService<Integer, BuildingDto> {
    List<BuildingDto> findAll(Pageable pageable, BuildingBuilder builder);
}
