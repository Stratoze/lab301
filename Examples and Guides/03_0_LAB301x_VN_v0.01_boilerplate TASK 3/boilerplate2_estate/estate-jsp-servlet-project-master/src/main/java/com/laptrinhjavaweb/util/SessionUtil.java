package com.laptrinhjavaweb.util;

import javax.servlet.http.HttpServletRequest;

public class SessionUtil {
    private static SessionUtil sessionUtil = null;

    //    Applying Singleton Design Pattern
    public static SessionUtil getInstance() {
        if (sessionUtil == null) {
            sessionUtil = new SessionUtil();
        }
        return sessionUtil;
    }

    public void putAttribute(HttpServletRequest req, String key, Object value) {
        req.getSession().setAttribute(key, value);
    }

    public Object getAttribute(HttpServletRequest req, String key) {
        return req.getSession().getAttribute(key);
    }

    public void removeAttribute(HttpServletRequest req, String key) {
        req.getSession().removeAttribute(key);
    }
}
