package com.laptrinhjavaweb.orm.session;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.sqlquery.SQLQuery;
import com.laptrinhjavaweb.orm.transaction.Transaction;

import java.io.Serializable;
import java.sql.SQLException;

public interface Session {
    <T> T get(Class<T> entityClass, Serializable id);

    void save(Object entity) throws SQLException;

    void update(Object entity) throws SQLException;

    void delete(Object entity) throws SQLException;

    SQLQuery createSQLQuery(String sql) throws SQLException;

    Criteria createCriteria(Class entityClass);

    Transaction beginTransaction();

    void close();
}
