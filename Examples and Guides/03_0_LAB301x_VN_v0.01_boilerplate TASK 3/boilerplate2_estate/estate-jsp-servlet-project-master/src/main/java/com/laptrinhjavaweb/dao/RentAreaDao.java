package com.laptrinhjavaweb.dao;

import com.laptrinhjavaweb.entity.RentAreaEntity;

import java.sql.SQLException;

public interface RentAreaDao extends GenericDao<Integer, RentAreaEntity> {
    void deleteAllByBuildingId(Integer id) throws SQLException;
}
