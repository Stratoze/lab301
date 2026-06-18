package com.laptrinhjavaweb.command;

import com.laptrinhjavaweb.dto.DistrictDto;

public class DistrictCommand extends AbstractCommand<DistrictDto> {
    public DistrictCommand() {
        this.pojo = new DistrictDto();
    }
}
