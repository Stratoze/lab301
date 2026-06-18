package com.laptrinhjavaweb.service.util;

import com.laptrinhjavaweb.service.*;
import com.laptrinhjavaweb.service.impl.*;

public class SingletonServiceUtil {
    private static AssignmentService assignmentService;
    private static BuildingService buildingService;
    private static DistrictService districtService;
    private static RentAreaService rentAreaService;
    private static RoleService roleService;
    private static UserService userService;
    private static WardService wardService;

    private SingletonServiceUtil() {

    }

    public static AssignmentService getAssignmentServiceInstance() {
        if (assignmentService == null) {
            assignmentService = new AssignmentServiceImpl();
        }
        return assignmentService;
    }

    public static BuildingService getBuildingServiceInstance() {
        if (buildingService == null) {
            buildingService = new BuildingServiceImpl();
        }
        return buildingService;
    }

    public static DistrictService getDistrictServiceInstance() {
        if (districtService == null) {
            districtService = new DistrictServiceImpl();
        }
        return districtService;
    }

    public static RentAreaService getRentAreaServiceInstance() {
        if (rentAreaService == null) {
            rentAreaService = new RentAreaServiceImpl();
        }
        return rentAreaService;
    }

    public static RoleService getRoleServiceInstance() {
        if (roleService == null) {
            roleService = new RoleServiceImpl();
        }
        return roleService;
    }

    public static UserService getUserServiceInstance() {
        if (userService == null) {
            userService = new UserServiceImpl();
        }
        return userService;
    }

    public static WardService getWardServiceInstance() {
        if (wardService == null) {
            wardService = new WardServiceImpl();
        }
        return wardService;
    }
}
