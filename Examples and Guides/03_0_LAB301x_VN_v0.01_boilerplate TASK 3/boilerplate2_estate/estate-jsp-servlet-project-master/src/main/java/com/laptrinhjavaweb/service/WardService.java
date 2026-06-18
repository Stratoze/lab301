package com.laptrinhjavaweb.service;

import com.laptrinhjavaweb.dto.WardDto;

import java.util.List;

public interface WardService extends GenericService<Integer, WardDto> {
    List<WardDto> findAllByDistrictId(Integer districtId);
}
