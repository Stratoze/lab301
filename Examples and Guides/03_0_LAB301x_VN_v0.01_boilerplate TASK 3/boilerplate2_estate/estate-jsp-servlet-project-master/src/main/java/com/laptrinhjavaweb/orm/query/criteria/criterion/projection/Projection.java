package com.laptrinhjavaweb.orm.query.criteria.criterion.projection;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;

public interface Projection {
    Projection as(String alias);

    String toSqlString(Criteria criteria);
}
