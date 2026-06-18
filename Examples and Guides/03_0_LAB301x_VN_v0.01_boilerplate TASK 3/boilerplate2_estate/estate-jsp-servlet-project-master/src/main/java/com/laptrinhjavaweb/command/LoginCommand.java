package com.laptrinhjavaweb.command;

import com.laptrinhjavaweb.dto.UserDto;

public class LoginCommand extends AbstractCommand<UserDto> {

    private Boolean remember;

    public LoginCommand() {
        this.pojo = new UserDto();
    }

    public Boolean getRemember() {
        return remember;
    }

    public void setRemember(Boolean remember) {
        this.remember = remember;
    }
}
