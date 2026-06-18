package com.laptrinhjavaweb.orm.query.sqlquery;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface SQLQuery {
    List list();

    int executeUpdate() throws SQLException;

    void setParam(int index, Object parameter) throws SQLException;

    void setParam(String namedParam, Object parameter) throws SQLException;

    void setParamMap(Map<String, Object> paramMap) throws SQLException;

    SQLQuery setEntity(Class entityClass);
}
