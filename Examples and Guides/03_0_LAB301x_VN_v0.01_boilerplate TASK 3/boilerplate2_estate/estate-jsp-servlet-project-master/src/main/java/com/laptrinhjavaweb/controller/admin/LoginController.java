package com.laptrinhjavaweb.controller.admin;

import com.laptrinhjavaweb.command.LoginCommand;
import com.laptrinhjavaweb.constant.SystemConstant;
import com.laptrinhjavaweb.dto.UserDto;
import com.laptrinhjavaweb.security.Authentication;
import com.laptrinhjavaweb.util.FormUtil;
import com.laptrinhjavaweb.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/auth/*")
public class LoginController extends HttpServlet {
    private final String viewRootPath = "/views/admin/";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String type = req.getPathInfo();
        if (type != null) {
            type = type.substring(1);
        }

        if (type.startsWith(SystemConstant.AUTH_LOGIN)) {
            req.getRequestDispatcher(viewRootPath.concat("login.jsp")).forward(req, resp);
        } else if (type.startsWith(SystemConstant.AUTH_LOGOUT)) {
            SessionUtil.getInstance().removeAttribute(req, SystemConstant.SESSION_USER);
            resp.sendRedirect("/auth/login");
        } else {
            resp.sendRedirect("/auth/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginCommand command = FormUtil.populate(LoginCommand.class, req);
        UserDto loginModel = command.getPojo();

        String redirectUrl = Authentication.of(loginModel.getUsername(), loginModel.getPassword()).getRedirectUrl(req);

        resp.sendRedirect(redirectUrl);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    }
}
