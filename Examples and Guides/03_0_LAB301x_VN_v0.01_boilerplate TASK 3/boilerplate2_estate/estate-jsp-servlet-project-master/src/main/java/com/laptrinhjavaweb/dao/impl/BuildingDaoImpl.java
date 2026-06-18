package com.laptrinhjavaweb.dao.impl;

import com.laptrinhjavaweb.builder.BuildingBuilder;
import com.laptrinhjavaweb.dao.BuildingDao;
import com.laptrinhjavaweb.entity.BuildingEntity;
import com.laptrinhjavaweb.orm.query.sqlquery.SQLQuery;
import com.laptrinhjavaweb.orm.session.Session;
import com.laptrinhjavaweb.orm.session.SessionFactory;
import com.laptrinhjavaweb.paging.Pageable;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingDaoImpl extends AbstractDao<Integer, BuildingEntity> implements BuildingDao {
    @Override
    public List<BuildingEntity> findAll(Pageable pageable, BuildingBuilder builder) {
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT b.* FROM building b");
        StringBuilder from = new StringBuilder();
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");

//        tim kiem theo ten toa nha
        if (StringUtils.isNotBlank(builder.getName())) {
            where.append(" AND b.name LIKE {name}");
            paramMap.put("name", "%" + builder.getName() + "%");
        }

//        tim kiem theo dien tich san cua toa nha
        if (builder.getBuildingAreaFrom() != null && builder.getBuildingAreaFrom() != 0) {
            where.append(" AND b.building_area >= {buildingAreaFrom}");
            paramMap.put("buildingAreaFrom", builder.getBuildingAreaFrom());
        }
        if (builder.getBuildingAreaTo() != null && builder.getBuildingAreaTo() != 0) {
            where.append(" AND b.building_area <= {buildingAreaTo}");
            paramMap.put("buildingAreaTo", builder.getBuildingAreaTo());
        }

//        tim kiem theo quan hoac phuong
        if (builder.getWardId() != null && builder.getWardId() != 0) {
            from.append(" JOIN ward w ON b.ward_id = w.id");
            where.append(" AND w.id = {wardId}");
            paramMap.put("wardId", builder.getWardId());
        } else if (builder.getDistrictId() != null && builder.getDistrictId() != 0) {
            from.append(" JOIN ward w ON b.ward_id = w.id");
            where.append(" AND w.district_id = {districtId}");
            paramMap.put("districtId", builder.getDistrictId());
        }

//        tim kiem theo ten duong
        if (StringUtils.isNotBlank(builder.getStreet())) {
            where.append(" AND b.street LIKE {street}");
            paramMap.put("street", "%" + builder.getStreet() + "%");
        }

//        tim kiem theo ten nguoi quan ly
        if (StringUtils.isNotBlank(builder.getManagerName())) {
            where.append(" AND b.manager_name LIKE {managerName}");
            paramMap.put("managerName", "%" + builder.getManagerName() + "%");
        }

//        tim kiem theo sdt nguoi quan ly
        if (StringUtils.isNotBlank(builder.getManagerPhone())) {
            where.append(" AND b.manager_phone LIKE {managerPhone}");
            paramMap.put("managerPhone", "%" + builder.getManagerPhone() + "%");
        }

//        tim kiem theo nhan vien phu trach
        if (builder.getStaffArray() != null && builder.getStaffArray().length > 0) {
            from.append(" JOIN assignment a ON b.id = a.building_id");
            where.append(" AND a.user_id IN (");
            for (int i = 0; i < builder.getStaffArray().length; i++) {
                String paramName = "staffId_" + i;
                where.append("{" + paramName + "}");
                paramMap.put(paramName, builder.getStaffArray()[i]);

                if (i < builder.getStaffArray().length - 1) {
                    where.append(", ");
                }
            }
            where.append(")");
        }

//        tim kiem theo dien tich thue
        if ((builder.getRentAreaFrom() != null && builder.getRentAreaFrom() != 0)
                || (builder.getRentAreaTo() != null && builder.getRentAreaTo() != 0)) {
            from.append(" JOIN rent_area ra ON b.id = ra.building_id");
        }
        if (builder.getRentAreaFrom() != null && builder.getRentAreaFrom() != 0) {
            where.append(" AND ra.area >= {rentAreaFrom}");
            paramMap.put("rentAreaFrom", builder.getRentAreaFrom());
        }
        if (builder.getRentAreaTo() != null && builder.getRentAreaTo() != 0) {
            where.append(" AND ra.area <= {rentAreaTo}");
            paramMap.put("rentAreaTo", builder.getRentAreaTo());
        }

//        tim kiem theo gia thue
        if (builder.getRentalCostFrom() != null && builder.getRentalCostFrom() != 0) {
            where.append(" AND b.rental_cost >= {rentalCostFrom}");
            paramMap.put("rentalCostFrom", builder.getRentalCostFrom());
        }
        if (builder.getRentalCostTo() != null && builder.getRentalCostTo() != 0) {
            where.append(" AND b.rental_cost <= {rentalCostTo}");
            paramMap.put("rentalCostTo", builder.getRentalCostTo());
        }

//        tim kiem theo so tang ham
        if (builder.getNumberOfBasement() != null && builder.getNumberOfBasement() != 0) {
            where.append(" AND b.number_of_basement = {numberOfBasement}");
            paramMap.put("numberOfBasement", builder.getNumberOfBasement());
        }

//        tim kiem theo huong
        if (StringUtils.isNotBlank(builder.getDirection())) {
            where.append(" AND b.direction = {direction}");
            paramMap.put("direction", builder.getDirection());
        }

//        tim kiem theo hang
        if (StringUtils.isNotBlank(builder.getLevel())) {
            where.append(" AND b.level = {level}");
            paramMap.put("level", builder.getLevel());
        }

//        tim kiem theo loai toa nha
        if (builder.getTypeArray() != null && builder.getTypeArray().length > 0) {
            where.append(" AND (");
            for (int i = 0; i < builder.getTypeArray().length; i++) {
                if (i != 0) {
                    where.append(" OR ");
                }
                String paramName = "type_" + i;
                where.append("FIND_IN_SET({" + paramName + "}, b.type)");
                paramMap.put(paramName, builder.getTypeArray()[i]);
            }
            where.append(")");
        }

        sqlBuilder.append(from).append(where);

//        sorting
        if (StringUtils.isNotBlank(pageable.getSorter().getPropertyName()) && StringUtils.isNotBlank(pageable.getSorter().getDirection())) {
            sqlBuilder.append(" ORDER BY {orderBy} {orderDirection}");
            paramMap.put("orderBy", pageable.getSorter().getPropertyName());
            paramMap.put("orderDirection", pageable.getSorter().getDirection());
        }

//        paging
        if (pageable.getOffset() != null && pageable.getLimit() != null) {
            sqlBuilder.append(" LIMIT {limit} OFFSET {offset}");
            paramMap.put("limit", pageable.getLimit());
            paramMap.put("offset", pageable.getOffset());
        }

        Session session = SessionFactory.openSession();
        List<BuildingEntity> buildingEntityList = new ArrayList<>();
        try {
            SQLQuery sqlQuery = session.createSQLQuery(sqlBuilder.toString());
            sqlQuery.setParamMap(paramMap);
            buildingEntityList = sqlQuery.setEntity(BuildingEntity.class).list();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return buildingEntityList;
    }
}
