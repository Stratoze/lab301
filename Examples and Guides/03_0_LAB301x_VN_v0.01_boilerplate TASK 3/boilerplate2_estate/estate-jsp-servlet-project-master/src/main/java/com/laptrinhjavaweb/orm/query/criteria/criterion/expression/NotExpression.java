package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;

public class NotExpression extends AbstractExpression implements Criterion {
    private Criterion criterion;

    public NotExpression(String prefixLogical, Criterion criterion) {
        super(prefixLogical);
        this.criterion = criterion;
    }

    @Override
    void buildFragment(Criteria criteria) {
//        tạo fragment
        super.fragment.append(super.prefixLogical);
        super.fragment.append("NOT ");
        super.fragment.append(criterion.toSqlString(criteria));
    }
}
