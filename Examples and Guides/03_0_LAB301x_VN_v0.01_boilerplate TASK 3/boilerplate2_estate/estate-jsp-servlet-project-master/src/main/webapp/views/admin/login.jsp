<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@include file="/common/taglib.jsp" %>
<c:url var="loginUrl" value="/auth/"></c:url>
<html>
<head>
    <title>
        <fmt:message bundle="${lang}" key="login"/>
    </title>
</head>
<body>
<div class="panel panel-sign">
    <div class="panel-title-sign mt-xl text-right">
        <h2 class="title text-uppercase text-bold m-none"><i class="fa fa-user mr-xs"></i> Sign In</h2>
    </div>
    <div class="panel-body">
        <c:if test="${not empty param.message}">
            <div class="alert alert-danger">
                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                <strong>
                    <fmt:message bundle="${lang}" key="login.failed"/>
                </strong>
                <br/>
                <fmt:message bundle="${lang}" key="${param.message}"/>
            </div>
        </c:if>
        <form action="${loginUrl}" method="post">
            <div class="form-group mb-lg">
                <label>Username</label>
                <div class="input-group input-group-icon">
                    <input name="pojo.username" type="text" class="form-control input-lg" autofocus tabindex="1"/>
                    <span class="input-group-addon">
										<span class="icon icon-lg" style="display: inline-flex;align-items: center;">
											<i class="fa fa-user"></i>
										</span>
									</span>
                </div>
            </div>

            <div class="form-group mb-lg">
                <div class="clearfix">s
                    <label class="pull-left">Password</label>
                    <a href="pages-recover-password.html" class="pull-right">Lost Password?</a>
                </div>
                <div class="input-group input-group-icon">
                    <input name="pojo.password" type="password" class="form-control input-lg" tabindex="2"/>
                    <span class="input-group-addon">
										<span class="icon icon-lg" style="display: inline-flex;align-items: center;">
											<i class="fa fa-lock"></i>
										</span>
									</span>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-8">
                    <div class="checkbox-custom checkbox-default">
                        <input id="RememberMe" name="remember" type="checkbox"/>
                        <label for="RememberMe">Remember Me</label>
                    </div>
                </div>
                <div class="col-sm-4 text-right">
                    <button type="submit" class="btn btn-primary hidden-xs">Sign In</button>
                    <button type="submit" class="btn btn-primary btn-block btn-lg visible-xs mt-lg">Sign In
                    </button>
                </div>
            </div>

            <span class="mt-lg mb-lg line-thru text-center text-uppercase">
								<span>or</span>
							</span>

            <div class="mb-xs text-center">
                <a class="btn btn-facebook mb-md ml-xs mr-xs">Connect with <i class="fa fa-facebook"></i></a>
                <a class="btn btn-twitter mb-md ml-xs mr-xs">Connect with <i class="fa fa-twitter"></i></a>
            </div>

            <p class="text-center">Don't have an account yet? <a href="pages-signup.html">Sign Up!</a>

        </form>
    </div>
</div>
</body>
</html>