package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.NamedParam;
import com.laptrinhjavaweb.orm.query.criteria.util.NamedParamHandlerUtil;
import com.laptrinhjavaweb.orm.util.EntityUtil;

public class BetweenExpression extends AbstractExpression implements Criterion {
    private final String propertyName;
    private final Object low;
    private final Object high;

    public BetweenExpression(String prefixLogical, String propertyName, Object low, Object high) {
        super(prefixLogical);
        this.propertyName = propertyName;
        this.low = low;
        this.high = high;
    }

    @Override
    public void buildFragment(Criteria criteria) {
        NamedParam namedParamLow = NamedParamHandlerUtil.createNewNamedParam(criteria.getNamedParamMap(), propertyName, low);
        criteria.getNamedParamMap().put(namedParamLow.getPropertyName(), namedParamLow);

        NamedParam namedParamHigh = NamedParamHandlerUtil.createNewNamedParam(criteria.getNamedParamMap(), propertyName, high);
        criteria.getNamedParamMap().put(namedParamHigh.getPropertyName(), namedParamHigh);

//        lấy tên cột tương ứng với tên thuộc tính của entity
        String columnName = EntityUtil.getColumnName(criteria.getEntityClass(), propertyName);

//        tạo fragment
        super.fragment.append(super.prefixLogical);
        super.fragment.append(columnName);
        super.fragment.append(" BETWEEN");
        super.fragment.append(" {");
        super.fragment.append(namedParamLow);
        super.fragment.append("}");
        super.fragment.append(" AND");
        super.fragment.append(" {");
        super.fragment.append(namedParamHigh);
        super.fragment.append("}");
    }
}
