package com.laptrinhjavaweb.orm.query.criteria.criterion;

import com.laptrinhjavaweb.orm.query.criteria.criterion.expression.GroupExpression;

public class Logical {
    private static final String prefixAnd = " AND";
    private static final String prefixOr = " OR";

    public static Restrictions and(String propertyName) {
        return new Restrictions(Logical.prefixAnd, propertyName);
    }

    public static Restrictions or(String propertyName) {
        return new Restrictions(Logical.prefixOr, propertyName);
    }

    public static GroupExpression andGroup() {
        return new GroupExpression(Logical.prefixAnd);
    }

    public static GroupExpression orGroup() {
        return new GroupExpression(Logical.prefixOr);
    }
}
