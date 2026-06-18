package com.laptrinhjavaweb.security;

import com.laptrinhjavaweb.constant.SystemConstant;
import com.laptrinhjavaweb.dto.UserDto;
import com.laptrinhjavaweb.service.UserService;
import com.laptrinhjavaweb.service.util.SingletonServiceUtil;
import com.laptrinhjavaweb.util.SessionUtil;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationImpl implements Authentication {
    private UserService userService;

    private String username;
    private String password;

    public AuthenticationImpl(String username, String password) {
        this.username = username;
        this.password = password;
        this.userService = SingletonServiceUtil.getUserServiceInstance();
    }

    @Override
    public String getRedirectUrl(HttpServletRequest request) {
        UserDto loginModel = userService.findLoginUser(this.username, this.password);
        if (loginModel != null) {
            SessionUtil.getInstance().putAttribute(request, SystemConstant.SESSION_USER, loginModel);
            return "/admin/building/list";
        } else {
            return "/auth/login?message=login_wrong";
        }
    }
}
