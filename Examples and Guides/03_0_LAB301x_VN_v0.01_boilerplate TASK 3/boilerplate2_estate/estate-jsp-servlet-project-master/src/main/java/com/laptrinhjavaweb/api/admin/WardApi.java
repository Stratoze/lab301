package com.laptrinhjavaweb.api.admin;


import com.laptrinhjavaweb.dto.WardDto;
import com.laptrinhjavaweb.service.WardService;
import com.laptrinhjavaweb.service.util.SingletonServiceUtil;
import com.laptrinhjavaweb.util.HttpUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/ward")
public class WardApi extends HttpServlet {

    private WardService wardService;

    public WardApi() {
        wardService = SingletonServiceUtil.getWardServiceInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer districtId = Integer.parseInt(req.getParameter("districtId"));
        List<WardDto> wardDtoList = wardService.findAllByDistrictId(districtId);
        HttpUtil.writeValue(resp.getOutputStream(), wardDtoList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {

    }
}
