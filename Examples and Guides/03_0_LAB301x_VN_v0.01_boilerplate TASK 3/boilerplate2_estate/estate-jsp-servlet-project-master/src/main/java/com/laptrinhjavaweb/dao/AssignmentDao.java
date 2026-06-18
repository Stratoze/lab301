package com.laptrinhjavaweb.dao;

import com.laptrinhjavaweb.entity.AssignmentEntity;

import java.sql.SQLException;

public interface AssignmentDao extends GenericDao<Integer, AssignmentEntity> {
    void deleteAllByBuildingId(Integer id) throws SQLException;

    AssignmentEntity findOneByStaffIdAndBuildingId(Integer staffId, Integer buildingId);
}
