package com.laptrinhjavaweb.orm.query.criteria.criterion.projection;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;

public class RowCountProjection implements Projection {
    protected String alias = "";

    @Override
    public Projection as(String alias) {
        this.alias += " AS " + alias;
        return this;
    }

    @Override
    public String toSqlString(Criteria criteria) {
        return "count(*)" + this.alias;
    }
}
