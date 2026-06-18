package com.laptrinhjavaweb.service;

import com.laptrinhjavaweb.dto.AssignmentStaffDto;
import com.laptrinhjavaweb.dto.UserDto;

import java.util.List;

public interface UserService extends GenericService<Integer, UserDto> {
    List<AssignmentStaffDto> getStaffAssignmentListByBuildingId(Integer buildingId);

    UserDto findLoginUser(String username, String password);

    List<UserDto> findAllActiveStaff();
}
