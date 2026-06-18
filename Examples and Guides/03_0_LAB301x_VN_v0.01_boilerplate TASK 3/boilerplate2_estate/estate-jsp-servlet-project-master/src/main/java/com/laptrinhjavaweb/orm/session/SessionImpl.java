package com.laptrinhjavaweb.orm.session;

import com.laptrinhjavaweb.orm.builder.StatementBuilder;
import com.laptrinhjavaweb.orm.exception.IdentifierGenerationException;
import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.CriteriaImpl;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Logical;
import com.laptrinhjavaweb.orm.query.sqlquery.SQLQuery;
import com.laptrinhjavaweb.orm.query.sqlquery.SQLQueryImpl;
import com.laptrinhjavaweb.orm.query.statement.NamedParamStatement;
import com.laptrinhjavaweb.orm.session.util.CloseExecutorUtil;
import com.laptrinhjavaweb.orm.transaction.Transaction;
import com.laptrinhjavaweb.orm.transaction.TransactionImpl;
import com.laptrinhjavaweb.orm.util.EntityUtil;
import com.laptrinhjavaweb.orm.util.ObjectAccessUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class SessionImpl implements Session {
    private Connection connection;
    NamedParamStatement statement;

    public SessionImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Object get(Class entityClass, Serializable id) {
        String idFieldName = EntityUtil.getIdFieldName(entityClass);
        Criteria criteria = this.createCriteria(entityClass);
        criteria.addCriterion(Logical.and(idFieldName).eq(id));
        return criteria.uniqueResult();
    }

    @Override
    public void save(Object entity) throws SQLException {
        Class entityClass = entity.getClass();

//        Kiểm tra id có tự tăng hay không
        boolean isAutoIncrement = EntityUtil.isAutoIncrement(entityClass);
        Object idData = EntityUtil.getIdFieldData(entityClass, entity);
        if (isAutoIncrement && idData != null) {
            throw new IdentifierGenerationException(entityClass.getName() + ": Id must be null because it is auto increment");
        }

        String sql = StatementBuilder.buildInsertStatement(entity.getClass());
        statement = new NamedParamStatement(connection, sql);
        this.setEntityToStatement(entity, statement);
        Long generateId = statement.executeInsert();

//            lấy lại giá trị của entity trong trường hợp có những giá trị do trigger sinh ra.
        Object insertedEntity;
        if (generateId != null) {
            insertedEntity = this.get(entityClass, generateId);
        } else {
            Serializable id = (Serializable) EntityUtil.getIdFieldData(entityClass, entity);
            insertedEntity = this.get(entityClass, id);
        }

//        gán các giá trị mới vào entity
        try {
            ObjectAccessUtil.copyProperties(insertedEntity, entity);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Object entity) throws SQLException {
        Class entityClass = entity.getClass();
        String sql = StatementBuilder.buildUpdateStatement(entity.getClass());
        statement = new NamedParamStatement(connection, sql);
        this.setEntityToStatement(entity, statement);
        Integer rowsEffect = statement.executeUpdate();

//            lấy lại giá trị của entity trong trường hợp có những giá trị do trigger sinh ra.
        Object updatedEntity = null;
        if (rowsEffect > 0) {
            Serializable id = (Serializable) EntityUtil.getIdFieldData(entityClass, entity);
            updatedEntity = this.get(entityClass, id);
        }

//        gán các giá trị mới vào entity
        try {
            ObjectAccessUtil.copyProperties(updatedEntity, entity);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Object entity) throws SQLException {
        Class entityClass = entity.getClass();

//        lấy tên của field id và giá trị của id để set param cho câu statement
        Object id = EntityUtil.getIdFieldData(entityClass, entity);
        String idFieldName = EntityUtil.getIdFieldName(entityClass);

        String sql = StatementBuilder.buildDeleteStatement(entityClass);
        statement = new NamedParamStatement(connection, sql);
        statement.setNamedParam(idFieldName, id);
        statement.executeUpdate();
    }

    @Override
    public SQLQuery createSQLQuery(String sql) {
        return new SQLQueryImpl(connection, sql);
    }

    @Override
    public Criteria createCriteria(Class entityClass) {
        return new CriteriaImpl(connection, entityClass);
    }

    @Override
    public Transaction beginTransaction() {
        return new TransactionImpl(connection);
    }

    @Override
    public void close() {
        try {
            if (statement != null) {
                CloseExecutorUtil.closeStatement(statement.getPreparedStatement());
            }
            CloseExecutorUtil.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private <T> void setEntityToStatement(T entity, NamedParamStatement statement) throws SQLException {
        Class entityClass = entity.getClass();
        Field[] fieldList = entityClass.getDeclaredFields();

        for (Field field : fieldList) {
            Object fieldData;
            try {
                fieldData = ObjectAccessUtil.getFieldData(entity, field);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            statement.setNamedParam(field.getName(), fieldData);
        }
    }
}
