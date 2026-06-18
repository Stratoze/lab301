package com.laptrinhjavaweb.dao.impl;

import com.laptrinhjavaweb.dao.UserDao;
import com.laptrinhjavaweb.entity.UserEntity;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.query.criteria.criterion.Logical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDaoImpl extends AbstractDao<Integer, UserEntity> implements UserDao {

    @Override
    public List<UserEntity> findAllActiveStaffList() {
        Criterion criterion = Logical.and("roleId").eq(2);
        Criterion criterion1 = Logical.and("status").eq(true);
        return this.findAllByProperties(null, Arrays.asList(criterion, criterion1));
    }

    @Override
    public UserEntity findActiveUserByUsernameAndPassword(String username, String password) {
        List<Criterion> criterionList = new ArrayList<>();
        criterionList.add(Logical.and("username").eq(username));
        criterionList.add(Logical.and("password").eq(password));
        criterionList.add(Logical.and("status").eq(true));

        return this.findOneByProperties(criterionList);
    }
}
