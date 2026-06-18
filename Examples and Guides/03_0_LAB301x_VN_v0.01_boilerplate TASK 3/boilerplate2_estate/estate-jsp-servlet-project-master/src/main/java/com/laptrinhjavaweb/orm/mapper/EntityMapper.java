package com.laptrinhjavaweb.orm.mapper;

import java.sql.ResultSet;

public interface EntityMapper {
    EntityMapperImpl ROW_MAPPER = new EntityMapperImpl();

    Object toEntity(ResultSet resultSet) throws Exception;

    static EntityMapper of(Class<?> entityClass) {
        ROW_MAPPER.setEntityClass(entityClass);
        return ROW_MAPPER;
    }
}
