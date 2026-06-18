<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@include file="/common/taglib.jsp" %>
<!doctype html>
<html class="fixed js flexbox flexboxlegacy csstransforms csstransforms3d no-overflowscrolling no-mobile-device custom-scroll sidebar-left-collapsed">
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
    <link rel="stylesheet" href="<c:url value='/template/admin/vendor/magnific-popup/magnific-popup.css'/>"/>
    <link rel="stylesheet" href="<c:url value='/template/admin/vendor/pnotify/pnotify.custom.css'/>"/>
    <link rel="stylesheet" href="<c:url value='/template/admin/vendor/select2/select2.css'/>"/>
    <link rel="stylesheet"
          href="<c:url value='/template/admin/vendor/jquery-datatables-bs3/assets/css/datatables.css'/>"/>

    <!-- Specific Page Vendor CSS -->
    <decorator:getProperty property="page.specific_css"></decorator:getProperty>

    <!-- Theme CSS -->
    <link rel="stylesheet" href="<c:url value='/template/admin/stylesheets/theme.css'/>"/>

    <!-- Skin CSS -->
    <link rel="stylesheet" href="<c:url value='/template/admin/stylesheets/skins/default.css'/>"/>

    <!-- Theme Custom CSS -->
    <link rel="stylesheet" href="<c:url value='/template/admin/stylesheets/theme-custom.css'/>">

    <!-- Head Libs -->
    <script src="<c:url value='/template/admin/vendor/modernizr/modernizr.js'/>"></script>

    <decorator:head></decorator:head>
</head>
<body>
<section class="body">

    <%@include file="/common/admin/header.jsp" %>

    <div class="inner-wrapper">
        <section role="main" class="content-body">
            <%@include file="/common/admin/nav-bar.jsp" %>

            <!-- start: page -->
            <decorator:body></decorator:body>
            <!-- end: page -->
        </section>

        <decorator:getProperty property="page.specific_html"></decorator:getProperty>

    </div>

    <!-- Vendor -->
    <script src="<c:url value='/template/admin/vendor/jquery/jquery.min.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/jquery-browser-mobile/jquery.browser.mobile.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/bootstrap/js/bootstrap.min.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/nanoscroller/nanoscroller.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/magnific-popup/magnific-popup.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/pnotify/pnotify.custom.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/select2/select2.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/jquery-datatables/jquery.dataTables.min.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/jquery-datatables-bs3/assets/js/datatables.js'/>"></script>

    <!-- Specific Page Vendor -->
    <decorator:getProperty property="page.specific_script"></decorator:getProperty>

    <!-- Theme Base, Components and Settings -->
    <script src="<c:url value='/template/admin/javascripts/theme.js'/>"></script>

    <!-- Theme Custom -->
    <script src="<c:url value='/template/admin/javascripts/theme.custom.js'/>"></script>

    <!-- Theme Initialization Files -->
    <script src="<c:url value='/template/admin/javascripts/theme.init.js'/>"></script>

    <!-- Global JS -->
    <script src="<c:url value='/template/admin/javascripts/global.js'/>"></script>

    <decorator:getProperty property="page.local_script"></decorator:getProperty>
</section>
</body>
</html>