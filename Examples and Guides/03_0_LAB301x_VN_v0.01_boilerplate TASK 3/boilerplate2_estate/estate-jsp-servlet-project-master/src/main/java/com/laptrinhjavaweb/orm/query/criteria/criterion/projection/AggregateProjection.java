package com.laptrinhjavaweb.orm.query.criteria.criterion.projection;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.util.EntityUtil;

public class AggregateProjection extends AbstractAggregate implements Projection {
    private final String functionName;
    protected final String propertyName;

    public AggregateProjection(String functionName, String propertyName) {
        this.functionName = functionName;
        this.propertyName = propertyName;
    }

    @Override
    public String toAggregateString(Criteria criteria) {
//        lấy tên cột tương ứng với tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(criteria.getEntityClass(), propertyName);

        return this.functionName + "(" + columnName + ')';
    }
}
