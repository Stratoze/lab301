package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.util.EntityUtil;

public class PropertyExpression extends AbstractExpression implements Criterion {
    private final String propertyName;
    private final String otherPropertyName;
    private final String op;

    public PropertyExpression(String prefixLogical, String propertyName, String otherPropertyName, String op) {
        super(prefixLogical);
        this.propertyName = propertyName;
        this.otherPropertyName = otherPropertyName;
        this.op = op;
    }

    @Override
    void buildFragment(Criteria criteria) {
//        lấy tên cột tương ứng với tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(criteria.getEntityClass(), propertyName);

//        lấy tên cột tương ứng với tên thuộc tính của entity
        String otharColumnName = EntityUtil.getColumnName(criteria.getEntityClass(), otherPropertyName);

//        tạo fragment
        super.fragment.append(super.prefixLogical);
        super.fragment.append(columnName);
        super.fragment.append(" ");
        super.fragment.append(this.op);
        super.fragment.append(" ");
        super.fragment.append(otharColumnName);
    }
}
