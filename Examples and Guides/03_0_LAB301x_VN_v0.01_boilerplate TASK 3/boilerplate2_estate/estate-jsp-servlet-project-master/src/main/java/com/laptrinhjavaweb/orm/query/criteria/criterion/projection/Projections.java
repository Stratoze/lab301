package com.laptrinhjavaweb.orm.query.criteria.criterion.projection;

public class Projections {
    public static Projection rowCount() {
        return new RowCountProjection();
    }

    public static Projection count(String propertyName) {
        return new AggregateProjection("count", propertyName);
    }

    public static Projection countDistinct(String propertyName) {
        return new AggregateProjection("count", propertyName).setDistinct();
    }

    public static Projection max(String propertyName) {
        return new AggregateProjection("max", propertyName);
    }

    public static Projection min(String propertyName) {
        return new AggregateProjection("min", propertyName);
    }

    public static Projection avg(String propertyName) {
        return new AggregateProjection("avg", propertyName);
    }

    public static Projection avgDistinct(String propertyName) {
        return new AggregateProjection("avg", propertyName).setDistinct();
    }

    public static Projection sum(String propertyName) {
        return new AggregateProjection("sum", propertyName);
    }

    public static Projection sumDistinct(String propertyName) {
        return new AggregateProjection("sum", propertyName).setDistinct();
    }
}
