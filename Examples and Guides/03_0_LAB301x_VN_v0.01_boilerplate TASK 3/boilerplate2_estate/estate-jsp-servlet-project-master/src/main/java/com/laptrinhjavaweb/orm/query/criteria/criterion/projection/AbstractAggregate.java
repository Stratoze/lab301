package com.laptrinhjavaweb.orm.query.criteria.criterion.projection;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;

public abstract class AbstractAggregate implements Projection {
    protected String alias = "";
    private boolean distinct;

    @Override
    public Projection as(String alias) {
        this.alias += " AS " + alias;
        return this;
    }

    public Projection setDistinct() {
        this.distinct = true;
        return this;
    }

    @Override
    public String toSqlString(Criteria criteria) {
        String aggregateString = this.toAggregateString(criteria);
        StringBuilder sqlString = new StringBuilder(aggregateString + this.alias);

        if (this.distinct) {
            sqlString.insert(sqlString.indexOf("(") + 1, "DISTINCT ");
        }
        return sqlString.toString();
    }

    abstract String toAggregateString(Criteria criteria);
}
