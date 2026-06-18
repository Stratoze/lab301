package com.laptrinhjavaweb.service;

import com.laptrinhjavaweb.dto.RentAreaDto;

import java.util.List;

public interface RentAreaService extends GenericService<Integer, RentAreaDto> {
    List<RentAreaDto> findAllByBuildingId(Integer buildingId);

    void deleteAllByBuildingId(Integer id) throws Exception;
}
