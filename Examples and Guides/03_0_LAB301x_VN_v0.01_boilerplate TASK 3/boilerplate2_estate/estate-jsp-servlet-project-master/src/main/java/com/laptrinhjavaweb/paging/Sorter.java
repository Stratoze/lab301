package com.laptrinhjavaweb.paging;

import com.laptrinhjavaweb.constant.SystemConstant;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Order;

public class Sorter {
    private String propertyName;
    private String direction;

    public Sorter(String sortExpression, String sortDirection) {
        this.propertyName = sortExpression;
        this.direction = sortDirection;
    }

    public Order getOrder() {
        if (direction.equals(SystemConstant.SORT_ASC)) {
            return Order.asc(propertyName);
        } else {
            return Order.desc(propertyName);
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
