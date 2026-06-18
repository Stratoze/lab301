package com.laptrinhjavaweb.orm.query.criteria.criterion;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.util.EntityUtil;

public class Order {
    private String fragment;
    private String propertyName;

    private Order(String fragment, String propertyName) {
        this.fragment = fragment;
        this.propertyName = propertyName;
    }

    public static Order asc(String propertyName) {
        return new Order(" {propertyName} ASC", propertyName);
    }

    public static Order desc(String propertyName) {
        return new Order(" {propertyName} DESC", propertyName);
    }

    public String toSqlString(Criteria criteria) {
//        lấy tên cột tương ứng với tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(criteria.getEntityClass(), propertyName);
        fragment = fragment.replace("{propertyName}", columnName);
        return fragment;
    }
}
