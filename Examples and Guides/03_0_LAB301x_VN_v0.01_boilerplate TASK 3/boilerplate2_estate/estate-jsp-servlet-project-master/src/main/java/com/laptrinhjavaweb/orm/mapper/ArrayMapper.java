package com.laptrinhjavaweb.orm.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArrayMapper {
    public static Object[] toArray(ResultSet resultSet) throws SQLException {
        int columnsCount = resultSet.getMetaData().getColumnCount();
        List<Object> objectList = new ArrayList<>();
        for (int i = 1; i <= columnsCount; i++) {
            objectList.add(resultSet.getObject(i));
        }
        return objectList.toArray();
    }
}
