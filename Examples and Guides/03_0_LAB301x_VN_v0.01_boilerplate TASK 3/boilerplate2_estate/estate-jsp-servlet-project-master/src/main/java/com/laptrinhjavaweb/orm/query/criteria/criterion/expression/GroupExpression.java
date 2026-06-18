package com.laptrinhjavaweb.orm.query.criteria.criterion.expression;

import com.laptrinhjavaweb.orm.query.criteria.Criteria;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;

import java.util.ArrayList;
import java.util.List;

public class GroupExpression extends AbstractExpression implements Criterion {
    private final List<Criterion> criterionList = new ArrayList<>();

    public GroupExpression(String prefixLogical) {
        super(prefixLogical);
    }

    public GroupExpression add(Criterion criterion) {
        criterionList.add(criterion);
        return this;
    }

    @Override
    public void buildFragment(Criteria criteria) {
//        tạo fragment
        super.fragment.append(super.prefixLogical);
        super.fragment.append("(");
        for (int i = 0; i < criterionList.size(); i++) {
            if (i == 0) {
                criterionList.get(i).setPrefixLogical("");
            }

            super.fragment.append(criterionList.get(i).toSqlString(criteria));
        }

        super.fragment.append(")");
    }
}
