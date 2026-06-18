package com.laptrinhjavaweb.service;

import com.laptrinhjavaweb.dto.AssignmentDto;

public interface AssignmentService extends GenericService<Integer, AssignmentDto> {
    void deleteAllByBuildingId(Integer id) throws Exception;

    boolean isExistByStaffIdAndBuildingId(Integer staffId, Integer buildingId);
}
