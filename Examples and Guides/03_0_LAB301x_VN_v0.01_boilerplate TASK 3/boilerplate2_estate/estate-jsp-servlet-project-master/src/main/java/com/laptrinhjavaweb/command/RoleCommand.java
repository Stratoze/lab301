package com.laptrinhjavaweb.command;

import com.laptrinhjavaweb.dto.RoleDto;

public class RoleCommand extends AbstractCommand<RoleDto> {
    public RoleCommand() {
        this.pojo = new RoleDto();
    }
}
