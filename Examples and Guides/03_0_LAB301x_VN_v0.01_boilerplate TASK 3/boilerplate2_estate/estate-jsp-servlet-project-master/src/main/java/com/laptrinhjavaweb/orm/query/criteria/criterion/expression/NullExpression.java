package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.util.EntityUtil;

public class NullExpression extends AbstractExpression implements Criterion {
    private final String propertyName;

    public NullExpression(String prefixLogical, String propertyName) {
        super(prefixLogical);
        this.propertyName = propertyName;
    }

    @Override
    void buildFragment(Criteria criteria) {
//        lấy tên cột tương ứng với tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(criteria.getEntityClass(), propertyName);

//        tạo fragment
        super.fragment.append(super.prefixLogical);
        super.fragment.append(columnName);
        super.fragment.append(" IS NULL");
    }
}
