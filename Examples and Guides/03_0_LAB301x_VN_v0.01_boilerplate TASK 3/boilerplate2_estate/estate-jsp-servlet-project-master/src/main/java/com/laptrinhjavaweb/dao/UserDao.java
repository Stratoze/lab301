package com.laptrinhjavaweb.dao;

import com.laptrinhjavaweb.entity.UserEntity;

import java.util.List;

public interface UserDao extends GenericDao<Integer, UserEntity> {
    List<UserEntity> findAllActiveStaffList();

    UserEntity findActiveUserByUsernameAndPassword(String username, String password);
}
