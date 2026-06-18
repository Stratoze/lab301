package com.laptrinhjavaweb.dao.util;

import com.laptrinhjavaweb.dao.*;
import com.laptrinhjavaweb.dao.impl.*;

public class SingletonDaoUtil {
    private static AssignmentDao assignmentDao;
    private static BuildingDao buildingDao;
    private static DistrictDao districtDao;
    private static RentAreaDao rentAreaDao;
    private static RoleDao roleDao;
    private static UserDao userDao;
    private static WardDao wardDao;

    private SingletonDaoUtil() {

    }

    public static AssignmentDao getAssignmentDaoInstance() {
        if (assignmentDao == null) {
            assignmentDao = new AssignmentDaoImpl();
        }
        return assignmentDao;
    }

    public static BuildingDao getBuildingDaoInstance() {
        if (buildingDao == null) {
            buildingDao = new BuildingDaoImpl();
        }
        return buildingDao;
    }

    public static DistrictDao getDistrictDaoInstance() {
        if (districtDao == null) {
            districtDao = new DistrictDaoImpl();
        }
        return districtDao;
    }

    public static RentAreaDao getRentAreaDaoInstance() {
        if (rentAreaDao == null) {
            rentAreaDao = new RentAreaDaoImpl();
        }
        return rentAreaDao;
    }

    public static RoleDao getRoleDaoInstance() {
        if (roleDao == null) {
            roleDao = new RoleDaoImpl();
        }
        return roleDao;
    }

    public static UserDao getUserDaoInstance() {
        if (userDao == null) {
            userDao = new UserDaoImpl();
        }
        return userDao;
    }

    public static WardDao getWardDaoInstance() {
        if (wardDao == null) {
            wardDao = new WardDaoImpl();
        }
        return wardDao;
    }
}
