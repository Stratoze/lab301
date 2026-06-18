package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.NamedParam;
import com.laptrinhjavaweb.orm.query.criteria.util.NamedParamHandlerUtil;

public class NoneExpression extends AbstractExpression implements Criterion {
    private final String propertyName;
    private final Object value;

    public NoneExpression(String prefixLogical, String propertyName, Object value) {
        super(prefixLogical);
        this.propertyName = propertyName;
        this.value = value;
    }

    @Override
    public void buildFragment(Criteria criteria) {
        NamedParam namedParam = NamedParamHandlerUtil.createNewNamedParam(criteria.getNamedParamMap(), propertyName, value);
        criteria.getNamedParamMap().put(namedParam.getPropertyName(), namedParam);
    }
}
