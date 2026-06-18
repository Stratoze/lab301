package com.laptrinhjavaweb.service.impl;

import com.laptrinhjavaweb.converter.AssignmentConverter;
import com.laptrinhjavaweb.dao.AssignmentDao;
import com.laptrinhjavaweb.dao.impl.AssignmentDaoImpl;
import com.laptrinhjavaweb.dao.util.SingletonDaoUtil;
import com.laptrinhjavaweb.dto.AssignmentDto;
import com.laptrinhjavaweb.entity.AssignmentEntity;
import com.laptrinhjavaweb.service.AssignmentService;

import java.util.Date;

public class AssignmentServiceImpl extends AbstractService<Integer, AssignmentDto, AssignmentEntity> implements AssignmentService {
    public AssignmentServiceImpl() {
        super.genericDao = SingletonDaoUtil.getAssignmentDaoInstance();
        super.converter = new AssignmentConverter();
    }

    @Override
    public AssignmentDto save(AssignmentDto dto) throws Exception {
        dto.setCreatedDate(new Date());
        return super.save(dto);
    }

    @Override
    public void deleteAllByBuildingId(Integer id) throws Exception {
        ((AssignmentDaoImpl) genericDao).deleteAllByBuildingId(id);
    }

    @Override
    public boolean isExistByStaffIdAndBuildingId(Integer staffId, Integer buildingId) {
        AssignmentEntity assignmentEntity = ((AssignmentDao) genericDao).findOneByStaffIdAndBuildingId(staffId, buildingId);
        return assignmentEntity != null;
    }
}
