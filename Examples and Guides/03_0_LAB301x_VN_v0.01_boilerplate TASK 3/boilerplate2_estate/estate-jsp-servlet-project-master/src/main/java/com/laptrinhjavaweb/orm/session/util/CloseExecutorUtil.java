package com.laptrinhjavaweb.orm.session.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CloseExecutorUtil {
    public static void closeStatement(Statement statement) throws SQLException {
        if (statement != null && !statement.isClosed()) {
            ResultSet resultSet = statement.getResultSet();
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }

            statement.close();
        }
    }

    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
