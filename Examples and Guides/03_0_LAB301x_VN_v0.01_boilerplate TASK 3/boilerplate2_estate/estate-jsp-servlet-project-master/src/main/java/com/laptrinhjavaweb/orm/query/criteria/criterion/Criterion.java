package com.laptrinhjavaweb.orm.query.criteria.criterion;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;

public interface Criterion {
    String getPrefixLogical();

    void setPrefixLogical(String prefixLogical);

    String toSqlString(Criteria criteria);
}