<%--
  Created by IntelliJ IDEA.
  User: vothanhtai - Date: 2/28/19 - Time: 14:22
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@include file="/common/taglib.jsp" %>
<html>
<head>
    <title>
        <decorator:title default="Admin page"></decorator:title>
    </title>

    <!-- Basic -->
    <meta charset="UTF-8">

    <link rel="shortcut icon" href="">

    <link rel="shortcut icon" type="image/png"
          href="<c:url value='/template/admin/images/apple-touch-icon-precomposed.png'/>"/>

    <!-- Mobile Metas -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>

    <!-- Web Fonts  -->
    <link href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700,800|Shadows+Into+Light"
          rel="stylesheet" type="text/css">

    <!-- Vendor CSS -->
    <link rel="stylesheet" href="<c:url value='/template/admin/vendor/bootstrap/css/bootstrap.min.css'/>"/>
    <link rel="stylesheet" href="<c:url value='/template/admin/vendor/font-awesome/css/font-awesome.css'/>"/>

    <!-- Theme CSS -->
    <link rel="stylesheet" href="<c:url value='/template/admin/stylesheets/theme.css'/>"/>

    <!-- Skin CSS -->
    <link rel="stylesheet" href="<c:url value='/template/admin/stylesheets/skins/default.css'/>"/>

    <decorator:head></decorator:head>
</head>
<body>
<!-- start: page -->
<section class="body-sign">
    <div class="center-sign">
        <a href="/" class="logo pull-left">
            <img src="<c:url value='/template/admin/images/logo.png'/>" height="54" alt="Porto Admin"/>
        </a>

        <decorator:body></decorator:body>

        <p class="text-center text-muted mt-md mb-md">&copy; Copyright 2018. All rights reserved. Template by <a
                href="https://colorlib.com">Colorlib</a>.</p>
    </div>
</section>

<!-- Vendor -->
<script src="<c:url value='/template/admin/vendor/jquery/jquery.min.js'/>"></script>
<script src="<c:url value='/template/admin/vendor/jquery-browser-mobile/jquery.browser.mobile.js'/>"></script>
<script src="<c:url value='/template/admin/vendor/bootstrap/js/bootstrap.min.js'/>"></script>
</body>
</html>
