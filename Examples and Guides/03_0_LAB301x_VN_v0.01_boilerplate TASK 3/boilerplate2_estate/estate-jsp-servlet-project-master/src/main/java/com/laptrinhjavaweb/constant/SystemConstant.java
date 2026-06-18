package com.laptrinhjavaweb.constant;

import java.io.File;

public class SystemConstant {
    public static final String BASE_UPLOAD_PATH = "/Users/vothanhtai/Documents/WorkSpace/Java/LRHTeam/estate-project/resources".replace("/", File.separator);
    public static final String BUILDING_UPLOAD_PATH = "building";

    public static final String REDIRECT_URL = "redirect:/";
    public static final String COMMAND = "command";
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
    public static final String MESSAGE_RESPONSE = "messageResponse";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String PNOTIFY = "pNotify";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_EDIT = "edit";
    public static final String AUTH_LOGIN = "login";
    public static final String AUTH_LOGOUT = "logout";

    public static final String SESSION_USER = "session_user";
    public static final Integer ROLE_MANAGER = 1;
    public static final Integer ROLE_STAFF = 2;

    public static final String JPG = ".jpg";
    public static final String JPEG = ".jpeg";
    public static final String PNG = ".png";
    public static final String GIF = ".gif";
}
