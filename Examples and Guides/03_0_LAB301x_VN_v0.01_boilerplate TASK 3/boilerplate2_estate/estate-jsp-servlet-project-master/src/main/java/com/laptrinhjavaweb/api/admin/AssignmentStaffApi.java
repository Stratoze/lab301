package com.laptrinhjavaweb.api.admin;


import com.laptrinhjavaweb.command.AssignmentStaffCommand;
import com.laptrinhjavaweb.constant.SystemConstant;
import com.laptrinhjavaweb.dto.AssignmentDto;
import com.laptrinhjavaweb.dto.AssignmentStaffDto;
import com.laptrinhjavaweb.dto.UserDto;
import com.laptrinhjavaweb.service.AssignmentService;
import com.laptrinhjavaweb.service.UserService;
import com.laptrinhjavaweb.service.util.SingletonServiceUtil;
import com.laptrinhjavaweb.util.HttpUtil;
import com.laptrinhjavaweb.util.SessionUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/assignment/staff")
public class AssignmentStaffApi extends HttpServlet {

    private UserService userService;
    private AssignmentService assignmentService;

    public AssignmentStaffApi() {
        userService = SingletonServiceUtil.getUserServiceInstance();
        assignmentService = SingletonServiceUtil.getAssignmentServiceInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer buildingId = Integer.parseInt(req.getParameter("buildingId"));
        List<AssignmentStaffDto> assignmentStaffDtoList = userService.getStaffAssignmentListByBuildingId(buildingId);
        HttpUtil.writeValue(resp.getOutputStream(), assignmentStaffDtoList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AssignmentStaffCommand command = HttpUtil.of(req.getReader()).toObject(AssignmentStaffCommand.class);

        if (command.getCheckList() != null && command.getBuildingId() != null) {
            try {
                assignmentService.deleteAllByBuildingId(command.getBuildingId());
                for (String staffStrId : command.getCheckList()) {
                    Integer staffId = Integer.valueOf(staffStrId);
                    AssignmentDto assignmentDto = new AssignmentDto();
                    assignmentDto.setUserId(staffId);
                    assignmentDto.setBuildingId(command.getBuildingId());

                    String createdBy = ((UserDto) SessionUtil.getInstance().getAttribute(req, SystemConstant.SESSION_USER)).getUsername();
                    assignmentDto.setCreatedBy(createdBy);

                    assignmentService.save(assignmentDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {

    }
}
