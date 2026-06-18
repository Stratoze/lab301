package com.laptrinhjavaweb.controller.admin;

import com.laptrinhjavaweb.builder.BuildingBuilder;
import com.laptrinhjavaweb.command.BuildingCommand;
import com.laptrinhjavaweb.constant.SystemConstant;
import com.laptrinhjavaweb.dto.BuildingDto;
import com.laptrinhjavaweb.dto.BuildingSearchingDto;
import com.laptrinhjavaweb.dto.DistrictDto;
import com.laptrinhjavaweb.dto.UserDto;
import com.laptrinhjavaweb.paging.PageRequest;
import com.laptrinhjavaweb.paging.Pageable;
import com.laptrinhjavaweb.paging.Sorter;
import com.laptrinhjavaweb.service.BuildingService;
import com.laptrinhjavaweb.service.DistrictService;
import com.laptrinhjavaweb.service.UserService;
import com.laptrinhjavaweb.service.WardService;
import com.laptrinhjavaweb.service.util.SingletonServiceUtil;
import com.laptrinhjavaweb.util.FormUtil;
import com.laptrinhjavaweb.util.HttpUtil;
import com.laptrinhjavaweb.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/building/*")
public class BuildingController extends HttpServlet {
    private final String viewRootPath = "/views/admin/building/";

    private BuildingService buildingService;
    private DistrictService districtService;
    private WardService wardService;
    private UserService userService;

    public BuildingController() {
        buildingService = SingletonServiceUtil.getBuildingServiceInstance();
        districtService = SingletonServiceUtil.getDistrictServiceInstance();
        wardService = SingletonServiceUtil.getWardServiceInstance();
        userService = SingletonServiceUtil.getUserServiceInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getPathInfo();
        if (type != null) {
            type = type.substring(1);
        }

        try {
            BuildingCommand command = FormUtil.populate(BuildingCommand.class, req);

//            set district list to building command
            List<DistrictDto> districtDtoList = districtService.findAll();
            command.setDistrictDtoList(districtDtoList);

//            set staff list
            List<UserDto> staffDtoList = userService.findAllActiveStaff();
            command.setStaffDtoList(staffDtoList);

            if (type.startsWith(SystemConstant.TYPE_LIST)) {
                Pageable pageable = new PageRequest(command.getPage(), command.getMaxPageItems(),
                        new Sorter(command.getSortExpression(), command.getSortDirection()));

                BuildingSearchingDto search = command.getSearch();
                BuildingBuilder builder = new BuildingBuilder.Builder()
                        .setName(search.getName())
                        .setBuildingAreaFrom(search.getBuildingAreaFrom())
                        .setBuildingAreaTo(search.getBuildingAreaTo())
                        .setDistrictId(search.getDistrictId())
                        .setWardId(search.getWardId())
                        .setStreet(search.getStreet())
                        .setManagerName(search.getManagerName())
                        .setManagerPhone(search.getManagerPhone())
                        .setStaffArray(search.getStaffArray())
                        .setRentAreaFrom(search.getRentAreaFrom())
                        .setRentAreaTo(search.getRentAreaTo())
                        .setRentalCostFrom(search.getRentalCostFrom())
                        .setRentalCostTo(search.getRentalCostTo())
                        .setNumberOfBasement(search.getNumberOfBasement())
                        .setDirection(search.getDirection())
                        .setLevel(search.getLevel())
                        .setTypeArray(search.getTypeArray()).build();

                command.setListResult(buildingService.findAll(pageable, builder));
                req.setAttribute(SystemConstant.COMMAND, command);
                req.getRequestDispatcher(viewRootPath.concat("list.jsp")).forward(req, resp);

            } else if (type.startsWith(SystemConstant.TYPE_EDIT)) {
                String buildingStrId = type.substring(SystemConstant.TYPE_EDIT.length());

                if (!buildingStrId.isEmpty()) {
                    buildingStrId = buildingStrId.substring(1);
                    Integer buildingId = Integer.parseInt(buildingStrId);

                    BuildingDto buildingDto = buildingService.findOneById(buildingId);
                    command.setPojo(buildingDto);

                    command.setWardDtoList(wardService.findAllByDistrictId(buildingDto.getDistrictId()));
                } else {
                    command.setWardDtoList(wardService.findAllByDistrictId(districtDtoList.get(0).getId()));
                }

                req.setAttribute(SystemConstant.COMMAND, command);
                req.getRequestDispatcher(viewRootPath.concat("edit.jsp")).forward(req, resp);

            } else {
                resp.sendRedirect("/admin/building/list");
            }
        } catch (Exception e) {
            throw e;
//            e.printStackTrace();
//            resp.sendRedirect("/admin/building/list");
//            return;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BuildingDto buildingDto = HttpUtil.of(req.getReader()).toObject(BuildingDto.class);
        try {
            String createdBy = ((UserDto) SessionUtil.getInstance().getAttribute(req, SystemConstant.SESSION_USER)).getUsername();
            buildingDto.setCreatedBy(createdBy);

            buildingDto = buildingService.save(buildingDto);
            HttpUtil.writeValue(resp.getOutputStream(), buildingDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BuildingDto buildingDto = HttpUtil.of(req.getReader()).toObject(BuildingDto.class);
        try {
            String modifyBy = ((UserDto) SessionUtil.getInstance().getAttribute(req, SystemConstant.SESSION_USER)).getUsername();
            buildingDto.setModifiedBy(modifyBy);

            buildingDto = buildingService.update(buildingDto);
            HttpUtil.writeValue(resp.getOutputStream(), buildingDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BuildingCommand command = HttpUtil.of(req.getReader()).toObject(BuildingCommand.class);
        Integer[] intArray = new Integer[command.getCheckList().length];
        int i = 0;
        for (String strId : command.getCheckList()) {
            intArray[i] = Integer.parseInt(strId.trim());
            i++;
        }
        try {
            buildingService.deleteById(intArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
