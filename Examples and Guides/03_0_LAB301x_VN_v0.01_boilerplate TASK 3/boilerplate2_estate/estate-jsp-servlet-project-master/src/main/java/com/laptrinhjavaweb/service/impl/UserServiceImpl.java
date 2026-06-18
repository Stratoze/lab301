package com.laptrinhjavaweb.service.impl;

import com.laptrinhjavaweb.converter.UserConverter;
import com.laptrinhjavaweb.dao.UserDao;
import com.laptrinhjavaweb.dao.util.SingletonDaoUtil;
import com.laptrinhjavaweb.dto.AssignmentStaffDto;
import com.laptrinhjavaweb.dto.UserDto;
import com.laptrinhjavaweb.entity.UserEntity;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Logical;
import com.laptrinhjavaweb.service.AssignmentService;
import com.laptrinhjavaweb.service.UserService;
import com.laptrinhjavaweb.service.util.SingletonServiceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserServiceImpl extends AbstractService<Integer, UserDto, UserEntity> implements UserService {
    private AssignmentService assignmentService;

    public UserServiceImpl() {
        super.genericDao = SingletonDaoUtil.getUserDaoInstance();
        super.converter = new UserConverter();

        assignmentService = SingletonServiceUtil.getAssignmentServiceInstance();
    }

    @Override
    public List<AssignmentStaffDto> getStaffAssignmentListByBuildingId(Integer buildingId) {
        List<UserEntity> userEntityList = ((UserDao) genericDao).findAllActiveStaffList();
        List<AssignmentStaffDto> assignmentStaffDtoList = new ArrayList<>();

        userEntityList.forEach(staff -> {
            AssignmentStaffDto assignmentStaffDto = new AssignmentStaffDto();
            assignmentStaffDto.setId(staff.getId());
            assignmentStaffDto.setFullName(staff.getFullName());

            if (assignmentService.isExistByStaffIdAndBuildingId(staff.getId(), buildingId)) {
                assignmentStaffDto.setChecked("checked");
            } else {
                assignmentStaffDto.setChecked("");
            }

            assignmentStaffDtoList.add(assignmentStaffDto);
        });

        return assignmentStaffDtoList;
    }

    @Override
    public UserDto findLoginUser(String username, String password) {
        UserEntity userEntity = ((UserDao) genericDao).findActiveUserByUsernameAndPassword(username, password);

        return converter.entityToDto(userEntity);
    }

    @Override
    public List<UserDto> findAllActiveStaff() {
        Criterion criterion = Logical.and("roleId").eq(2);
        Criterion criterion1 = Logical.and("status").eq(true);
        return this.findAllByProperties(null, Arrays.asList(criterion, criterion1));
    }
}
