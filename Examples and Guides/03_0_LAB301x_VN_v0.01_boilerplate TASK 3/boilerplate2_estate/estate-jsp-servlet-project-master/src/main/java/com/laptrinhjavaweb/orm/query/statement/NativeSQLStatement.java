package com.laptrinhjavaweb.orm.query.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NativeSQLStatement {
    protected PreparedStatement preparedStatement;

    public NativeSQLStatement() {
    }

    public ResultSet executeQuery() throws SQLException {
        System.out.println(preparedStatement);
        ResultSet resultSet = this.preparedStatement.executeQuery();
        return resultSet;
    }

    public Integer executeUpdate() throws SQLException {
        System.out.println(preparedStatement);
        return this.preparedStatement.executeUpdate();
    }

    public Long executeInsert() throws SQLException {
        System.out.println(preparedStatement);
        this.preparedStatement.executeUpdate();
        ResultSet resultSet = this.preparedStatement.getGeneratedKeys();

        if (resultSet != null && resultSet.next()) {
            return resultSet.getLong(1);
        }

        return null;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setParamAt(int index, Object parameter) throws SQLException {
        preparedStatement.setObject(index, parameter);
    }
}
