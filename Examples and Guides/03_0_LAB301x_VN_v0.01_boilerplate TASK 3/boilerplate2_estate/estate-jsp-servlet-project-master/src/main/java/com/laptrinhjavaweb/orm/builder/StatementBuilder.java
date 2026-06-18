package com.laptrinhjavaweb.orm.builder;

import com.laptrinhjavaweb.orm.annotation.Column;
import com.laptrinhjavaweb.orm.annotation.Id;
import com.laptrinhjavaweb.orm.util.EntityUtil;

import java.lang.reflect.Field;

public class StatementBuilder {
    public static String buildSelectQuery(Class<?> entityClass) {
        StringBuilder statement = new StringBuilder("SELECT * FROM ");
        statement.append(EntityUtil.getTableName(entityClass));

        return statement.toString();
    }

    public static String buildSelectByIdQuery(Class<?> entityClass) {
        String idColumnName = EntityUtil.getIdColumnName(entityClass);
        String selectStatement = buildSelectQuery(entityClass);

        StringBuilder statement = new StringBuilder(selectStatement);
        statement.append(" WHERE ");
        statement.append(idColumnName);
        statement.append(" = {");
        statement.append(EntityUtil.getIdFieldName(entityClass));
        statement.append("}");
        return statement.toString();
    }

    public static String buildInsertStatement(Class<?> entityClass) {
        Field[] fieldList = entityClass.getDeclaredFields();

        StringBuilder statement = new StringBuilder("INSERT INTO ");
        statement.append(EntityUtil.getTableName(entityClass));

        statement.append(" (");
        for (int i = 0; i < fieldList.length; i++) {
            statement.append(fieldList[i].getAnnotation(Column.class).name());

            if (i < fieldList.length - 1) {
                statement.append(", ");
            }
        }

        statement.append(") VALUES (");
        for (int i = 0; i < fieldList.length; i++) {
            statement.append("{");
            statement.append(fieldList[i].getName());

            if (i < fieldList.length - 1) {
                statement.append("}, ");
            }
        }

        statement.append("})");

        return statement.toString();
    }

    public static String buildUpdateStatement(Class<?> entityClass) {
        StringBuilder statement = new StringBuilder("UPDATE ");
        statement.append(EntityUtil.getTableName(entityClass));
        statement.append(" SET ");

        Field[] fieldList = entityClass.getDeclaredFields();
        for (int i = 0; i < fieldList.length; i++) {
//            skip id column
            if (fieldList[i].isAnnotationPresent(Id.class)) {
                continue;
            }

            String columnName = fieldList[i].getAnnotation(Column.class).name();
            statement.append(columnName);
            statement.append(" = {");
            statement.append(fieldList[i].getName());

            if (i < fieldList.length - 1) {
                statement.append("}, ");
            }
        }

        statement.append("} WHERE ");
        statement.append(EntityUtil.getIdColumnName(entityClass));
        statement.append(" = {");
        statement.append(EntityUtil.getIdFieldName(entityClass));
        statement.append("}");

        return statement.toString();
    }

    public static String buildDeleteStatement(Class<?> entityClass) {
        StringBuilder statement = new StringBuilder("DELETE FROM ");
        statement.append(EntityUtil.getTableName(entityClass));
        statement.append(" WHERE ");
        statement.append(EntityUtil.getIdColumnName(entityClass));
        statement.append(" = {");
        statement.append(EntityUtil.getIdFieldName(entityClass));
        statement.append("}");

        return statement.toString();
    }
}
