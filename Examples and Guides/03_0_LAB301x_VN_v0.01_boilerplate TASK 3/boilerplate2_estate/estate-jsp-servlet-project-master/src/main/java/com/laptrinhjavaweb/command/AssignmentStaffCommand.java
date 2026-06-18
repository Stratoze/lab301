package com.laptrinhjavaweb.command;

import com.laptrinhjavaweb.dto.AssignmentStaffDto;

public class AssignmentStaffCommand extends AbstractCommand<AssignmentStaffDto> {
    private Integer buildingId;

    public AssignmentStaffCommand() {
        this.pojo = new AssignmentStaffDto();
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }
}
