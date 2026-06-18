package com.laptrinhjavaweb.orm.mapper;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.util.ObjectAccessUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityMapperImpl implements EntityMapper {
    private Class<?> entityClass;

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Object toEntity(ResultSet resultSet) throws Exception {
        Object entity = this.entityClass.newInstance();
        Field[] fieldList = this.entityClass.getDeclaredFields();
        for (Field field : fieldList) {
//          get data from result set
            Object fieldData = this.getFieldDataFromResultSet(resultSet, field);
            ObjectAccessUtil.setFieldData(entity, fieldData, field);
        }
        return entity;
    }

    private Object getFieldDataFromResultSet(ResultSet resultSet, Field field) {
        String columnName = field.getAnnotation(Column.class).name();

        Object value = null;
        try {
            value = resultSet.getObject(columnName, field.getType());
        } catch (SQLException ignore) {
//            ignore columns we do not need to get
        }

        return value;
    }
}
