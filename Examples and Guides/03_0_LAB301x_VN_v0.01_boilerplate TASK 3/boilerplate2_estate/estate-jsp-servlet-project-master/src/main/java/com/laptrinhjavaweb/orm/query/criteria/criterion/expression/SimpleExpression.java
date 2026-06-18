package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.NamedParam;
import com.laptrinhjavaweb.orm.query.criteria.util.NamedParamHandlerUtil;
import com.laptrinhjavaweb.orm.util.EntityUtil;

public class SimpleExpression extends AbstractExpression implements Criterion {
    private final String propertyName;
    private final Object value;
    private final String op;

    public SimpleExpression(String prefixLogical, String propertyName, Object value, String op) {
        super(prefixLogical);
        this.propertyName = propertyName;
        this.value = value;
        this.op = op;
    }

    @Override
    public void buildFragment(Criteria criteria) {
        NamedParam namedParam = NamedParamHandlerUtil.createNewNamedParam(criteria.getNamedParamMap(), propertyName, value);
        criteria.getNamedParamMap().put(namedParam.getPropertyName(), namedParam);

//        lấy tên cột tương ứng với tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(criteria.getEntityClass(), propertyName);

//        tạo fragment
        super.fragment.append(super.prefixLogical);
        super.fragment.append(columnName);
        super.fragment.append(" ");
        super.fragment.append(this.op);
        super.fragment.append(" {");
        super.fragment.append(namedParam);
        super.fragment.append("}");
    }
}
