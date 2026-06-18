package com.laptrinhjavaweb.command;

import com.laptrinhjavaweb.dto.UserDto;

public class UserCommand extends AbstractCommand<UserDto> {
    public UserCommand() {
        this.pojo = new UserDto();
    }
}
