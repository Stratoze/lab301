package com.laptrinhjavaweb.orm.query.criteria;

import com.laptrinhjavaweb.orm.mapper.ArrayMapper;
import com.laptrinhjavaweb.orm.mapper.EntityMapper;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.NamedParam;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Order;
import com.laptrinhjavaweb.orm.query.criteria.criterion.projection.Projection;
import com.laptrinhjavaweb.orm.query.statement.NamedParamStatement;
import com.laptrinhjavaweb.orm.session.util.CloseExecutorUtil;
import com.laptrinhjavaweb.orm.util.EntityUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CriteriaImpl implements Criteria {
    private Connection connection;
    private Class entityClass;
    private NamedParamStatement statement;

    private StringBuilder sqlQuery = new StringBuilder();
    private StringBuilder selectColumns = new StringBuilder();
    private String tableName = "";
    private StringBuilder where = new StringBuilder();
    private StringBuilder groupBy = new StringBuilder();
    private StringBuilder having = new StringBuilder();
    private StringBuilder orderBy = new StringBuilder();
    private String limit = "";
    private String offset = "";
    private boolean isMappedToEntity = true;

    private Map<String, NamedParam> namedParamMap = new TreeMap<>();

    public CriteriaImpl(Connection connection, Class entityClass) {
        this.connection = connection;
        this.entityClass = entityClass;
        tableName = EntityUtil.getTableName(entityClass);
    }

    @Override
    public Class getEntityClass() {
        return entityClass;
    }

    @Override
    public Map<String, NamedParam> getNamedParamMap() {
        return namedParamMap;
    }

    @Override
    public List list() {
        ResultSet resultSet;
        List resultList = new ArrayList<>();
        try {
            resultSet = this.executeQuery();
            while (resultSet.next()) {
                if (isMappedToEntity) {
                    resultList.add(EntityMapper.of(entityClass).toEntity(resultSet));
                } else {
                    resultList.add(ArrayMapper.toArray(resultSet));
                }
            }

            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close();
        }

        return resultList;
    }

    @Override
    public Object uniqueResult() {
        ResultSet resultSet;
        Object object = null;
        try {
            this.setMaxResults(1);
            this.setFirstResult(0);
            resultSet = this.executeQuery();

            if (resultSet.next()) {
                if (isMappedToEntity) {
                    object = EntityMapper.of(entityClass).toEntity(resultSet);
                } else {
                    object = ArrayMapper.toArray(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close();
        }

        return object;
    }

    @Override
    public Criteria addSelection(String fieldName) {
        if (selectColumns.length() != 0) {
            selectColumns.append(", ");
        }

//        lấy column name dựa trên tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(entityClass, fieldName);
        selectColumns.append(columnName);
        return this;
    }

    @Override
    public Criteria addSelection(Projection projection) {
        if (selectColumns.length() != 0) {
            selectColumns.append(", ");
        }
        selectColumns.append(projection.toSqlString(this));

        isMappedToEntity = false;
        return this;
    }

    @Override
    public Criteria addCriterion(Criterion criterion) {
        if (where.length() == 0) {
            where.append(" WHERE");
            criterion.setPrefixLogical(" ");
        }
        where.append(criterion.toSqlString(this));
        return this;
    }

    @Override
    public Criteria addGroupBy(String fieldName) {
        if (groupBy.length() == 0) {
            groupBy.append(" GROUP BY ");
        } else {
            groupBy.append(", ");
        }
//        lấy column name dựa trên tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(entityClass, fieldName);
        groupBy.append(columnName);

        isMappedToEntity = false;
        return this;
    }

    @Override
    public Criteria addOrder(Order order) {
        if (orderBy.length() == 0) {
            orderBy.append(" ORDER BY");
        } else {
            orderBy.append(", ");
        }
        orderBy.append(order.toSqlString(this));
        return this;
    }

    @Override
    public Criteria setMaxResults(int limit) {
        this.limit = " LIMIT " + limit;
        return this;
    }

    @Override
    public Criteria setFirstResult(int offset) {
        this.offset = " OFFSET " + offset;
        return this;
    }

    private void close() {
        try {
            CloseExecutorUtil.closeStatement(statement.getPreparedStatement());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String buildSqlQuery() {
        sqlQuery.append("SELECT ");
        sqlQuery.append(selectColumns.length() == 0 ? "*" : selectColumns);
        sqlQuery.append(" FROM ");
        sqlQuery.append(tableName);
        sqlQuery.append(where);
        sqlQuery.append(groupBy);
        sqlQuery.append(having);
        sqlQuery.append(orderBy);
        sqlQuery.append(limit);
        sqlQuery.append(offset);
        return sqlQuery.toString();
    }

    private ResultSet executeQuery() throws SQLException {
        ResultSet resultSet;
        String sql = this.buildSqlQuery();
        statement = new NamedParamStatement(connection, sql);
        statement.setNamedParamMap(namedParamMap);
        resultSet = statement.executeQuery();
        return resultSet;
    }
}
