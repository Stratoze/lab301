package com.laptrinhjavaweb.orm.query.criteria.criterion;

import com.laptrinhjavaweb.orm.query.criteria.criterion.expression.*;

public class Restrictions {
    private String expressionPrefixLogical;
    private String expressionPropertyName;

    public Restrictions(String expressionPrefixLogical) {
        this.expressionPrefixLogical = expressionPrefixLogical;
    }

    public Restrictions(String prefixLogical, String expressionPropertyName) {
        this.expressionPrefixLogical = prefixLogical;
        this.expressionPropertyName = expressionPropertyName;
    }

    public Criterion eq(Object value) {
        if (value == null) {
            return this.isNull();
        }
        return new SimpleExpression(this.expressionPrefixLogical, this.expressionPropertyName, value, "=");
    }

    public Criterion ne(Object value) {
        if (value == null) {
            return this.isNotNull();
        }
        return new SimpleExpression(this.expressionPrefixLogical, this.expressionPropertyName, value, "<>");
    }

    public Criterion like(String value, MatchMode matchMode) {
        return new SimpleExpression(this.expressionPrefixLogical, expressionPropertyName, matchMode.toMatchString(value), "like");
    }

    public Criterion gt(Object value) {
        return new SimpleExpression(this.expressionPrefixLogical, this.expressionPropertyName, value, ">");
    }

    public Criterion lt(Object value) {
        return new SimpleExpression(this.expressionPrefixLogical, this.expressionPropertyName, value, "<");
    }

    public Criterion le(Object value) {
        return new SimpleExpression(this.expressionPrefixLogical, this.expressionPropertyName, value, "<=");
    }

    public Criterion ge(Object value) {
        return new SimpleExpression(this.expressionPrefixLogical, this.expressionPropertyName, value, ">=");
    }

    public Criterion between(Object low, Object high) {
        return new BetweenExpression(this.expressionPrefixLogical, this.expressionPropertyName, low, high);
    }

    public Criterion isNull() {
        return new NullExpression(this.expressionPrefixLogical, this.expressionPropertyName);
    }

    public Criterion isNotNull() {
        return new NotNullExpression(this.expressionPrefixLogical, this.expressionPropertyName);
    }

    public Criterion eqProperty(String otherPropertyName) {
        return new PropertyExpression(this.expressionPrefixLogical, this.expressionPropertyName, otherPropertyName, "=");
    }

    public Criterion neProperty(String otherPropertyName) {
        return new PropertyExpression(this.expressionPrefixLogical, this.expressionPropertyName, otherPropertyName, "<>");
    }

    public Criterion ltProperty(String otherPropertyName) {
        return new PropertyExpression(this.expressionPrefixLogical, this.expressionPropertyName, otherPropertyName, "<");
    }

    public Criterion leProperty(String otherPropertyName) {
        return new PropertyExpression(this.expressionPrefixLogical, this.expressionPropertyName, otherPropertyName, "<=");
    }

    public Criterion gtProperty(String otherPropertyName) {
        return new PropertyExpression(this.expressionPrefixLogical, this.expressionPropertyName, otherPropertyName, ">");
    }

    public Criterion geProperty(String otherPropertyName) {
        return new PropertyExpression(this.expressionPrefixLogical, this.expressionPropertyName, otherPropertyName, ">=");
    }

    public Criterion isEmpty() {
        return this.eq("");
    }

    public Criterion isNotEmpty() {
        return this.ne("");
    }
}
