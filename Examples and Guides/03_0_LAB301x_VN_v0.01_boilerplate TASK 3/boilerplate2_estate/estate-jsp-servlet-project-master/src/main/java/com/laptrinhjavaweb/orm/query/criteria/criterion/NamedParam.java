package com.laptrinhjavaweb.orm.query.criteria.criterion;

public class NamedParam {
    private String propertyName;
    private Object value;

    public NamedParam(String propertyName, Object value) {
        this.propertyName = propertyName;
        this.value = value;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.propertyName;
    }
}
