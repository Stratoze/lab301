package com.laptrinhjavaweb.orm.query.criteria;

import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.NamedParam;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Order;
import com.laptrinhjavaweb.orm.query.criteria.criterion.projection.Projection;

import java.util.List;
import java.util.Map;

public interface Criteria {
    Class getEntityClass();

    Map<String, NamedParam> getNamedParamMap();

    List list();

    Object uniqueResult();

    Criteria addSelection(String fieldName);

    Criteria addSelection(Projection projection);

    Criteria addCriterion(Criterion criterion);

    Criteria addGroupBy(String fieldName);

    Criteria addOrder(Order order);

    Criteria setMaxResults(int limit);

    Criteria setFirstResult(int offset);
}
