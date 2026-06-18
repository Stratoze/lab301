<%--
  Created by IntelliJ IDEA.
  User: vothanhtai
  Date: 3/5/19
  Time: 13:50
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@include file="/common/taglib.jsp" %>
<c:url var="submitFormUrl" value="/admin/building/"></c:url>
<c:url var="wardApi" value="/api/ward"></c:url>
<html>
<head>
    <title>
        <fmt:message bundle="${lang}" key="building.management"/>
    </title>
    <content tag="specific_css">
        <link rel="stylesheet"
              href="<c:url value='/template/admin/vendor/bootstrap-multiselect/bootstrap-multiselect.css'/>"/>
        <link rel="stylesheet"
              href="<c:url value='/template/admin/vendor/bootstrap-tagsinput/bootstrap-tagsinput.css'/>"/>
    </content>

    <style>
        #buildingImage {
            display: none;
        }

        .imagePreviewWrapper {
            width: 250px;
            height: 250px;
            background: #efefef url(<c:url value="/template/admin/images/building-avatar.png"></c:url>) center;
            background-size: cover;
            border: 2px solid #909090;
            cursor: pointer;
        }

        #imagePreview {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    </style>
</head>
<body>
<div class="row">
    <div class="col-lg-12">
        <section class="panel panel-featured">
            <header class="panel-heading">
                <h2 class="panel-title">
                    <fmt:message bundle="${lang}" key="building.info"/>
                </h2>
            </header>
            <div class="panel-body">
                <form class="form-horizontal buildingForm" enctype="multipart/form-data" id="buildingForm">
                    <c:if test="${not empty command.pojo.id}">
                        <input type="hidden" name="id" value="${command.pojo.id}">
                    </c:if>
                    <div class="form-group">
                        <div class="row mb-lg">
                            <div class="col-xs-12" style="display: flex; justify-content: center">
                                <input type="file" id="buildingImage" name="image">
                                <div class="imagePreviewWrapper">
                                    <c:if test="${not empty command.pojo.image}">
                                        <c:url value="/api/image/${command.pojo.image}" var="imageUrl"></c:url>
                                    </c:if>
                                    <img src="${imageUrl}" alt="" id="imagePreview">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.name"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="name" value="${command.pojo.name}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.structure"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="structure" value="${command.pojo.structure}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.numberOfBasement"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="number" name="numberOfBasement"
                                           value="${command.pojo.numberOfBasement}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.district"/>
                                </label>
                                <div class="col-md-9">
                                    <select data-plugin-selectTwo class="form-control populate" id="district-select">
                                        <c:forEach var="districtDto" items="${command.districtDtoList}">
                                            <option value="${districtDto.id}" ${districtDto.id eq command.pojo.districtId ? 'selected' : ''}>${districtDto.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.ward"/>
                                </label>
                                <div class="col-md-9">
                                    <select data-plugin-selectTwo name="wardId" class="form-control populate"
                                            id="ward-select">
                                        <c:forEach var="wardDto" items="${command.wardDtoList}">
                                            <option value="${wardDto.id}" ${wardDto.id eq command.pojo.wardId ? 'selected' : ''}>${wardDto.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.street"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="street" value="${command.pojo.street}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.buildingArea"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="number" name="buildingArea"
                                           value="${command.pojo.buildingArea}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.direction"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="direction"
                                           value="${command.pojo.direction}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.level"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="level"
                                           value="${command.pojo.level}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-6">
                            <div class="form-group">
                                <label for="tags-input-multiple" class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.rentalArea"/>
                                </label>
                                <div class="col-md-9">
                                    <select name="rentalAreaArray" id="tags-input-multiple" multiple
                                            data-role="tagsinput" data-tag-class="label label-primary">
                                        <c:forEach var="area" items="${command.pojo.rentalAreaArray}">
                                            <option value="${area}"></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-6">
                            <div class="form-group">
                                <label for="tags-input-multiple" class="col-md-2 control-label">
                                    <fmt:message bundle="${lang}" key="building.rentalCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="number" name="rentalCost"
                                           value="${command.pojo.rentalCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-6">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.areaDescription"/>
                                </label>
                                <div class="col-md-9">
                                        <textarea name="areaDescription" class="form-control" rows="2"
                                                  data-plugin-textarea-autosize>${command.pojo.areaDescription}</textarea>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-6">
                            <div class="form-group">
                                <label class="col-md-2 control-label">
                                    <fmt:message bundle="${lang}" key="building.costDescription"/>
                                </label>
                                <div class="col-md-9">
                                        <textarea name="costDescription" class="form-control" rows="2"
                                                  data-plugin-textarea-autosize>${command.pojo.costDescription}</textarea>
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.serviceCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="serviceCost"
                                           value="${command.pojo.serviceCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.carCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="carCost"
                                           value="${command.pojo.carCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.motorbikeCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="motorbikeCost"
                                           value="${command.pojo.motorbikeCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.commissionCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="commissionCost"
                                           value="${command.pojo.commissionCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.overtimeCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="overtimeCost"
                                           value="${command.pojo.overtimeCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.electricityCost"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="electricityCost"
                                           value="${command.pojo.electricityCost}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.deposit"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="deposit"
                                           value="${command.pojo.deposit}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.payment"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="payment"
                                           value="${command.pojo.payment}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.timeContract"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="timeContract"
                                           value="${command.pojo.timeContract}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.timeDecorator"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="timeDecorator"
                                           value="${command.pojo.timeDecorator}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.managerName"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="managerName"
                                           value="${command.pojo.managerName}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.managerPhone"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="managerPhone"
                                           value="${command.pojo.managerPhone}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.link"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="link"
                                           value="${command.pojo.link}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.location"/>
                                </label>
                                <div class="col-md-9">
                                    <input type="text" name="location"
                                           value="${command.pojo.location}"
                                           class="form-control">
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4">
                            <div class="form-group">
                                <label class="col-md-3 control-label">
                                    <fmt:message bundle="${lang}" key="building.type"/>
                                </label>
                                <div class="col-md-9 myMultiSelect">
                                    <select class="form-control" name="typeArray"
                                            multiple data-plugin-multiselect>
                                        <c:forEach var="buildingType" items="${command.buildingTypeArray}">
                                            <c:set var="found" value="false" scope="request"/>
                                            <c:forEach items="${command.pojo.typeArray}" var="type">
                                                <c:if test="${type == buildingType}">
                                                    <c:set var="found" value="true" scope="request"/>
                                                </c:if>
                                            </c:forEach>
                                            <option value="${buildingType}" ${found ? 'selected' : ''}>${buildingType.typeName}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="row">
                        <div class="col-xs-12">
                            <div class="form-group">
                                <label class="col-md-1 control-label">
                                    <fmt:message bundle="${lang}" key="building.note"/>
                                </label>
                                <div class="col-md-11">
                                        <textarea name="note" class="form-control" rows="3"
                                                  data-plugin-textarea-autosize>${command.pojo.note}</textarea>
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <div class="form-group text-center">
                        <button type="submit" class="mb-xs mt-xs mr-xs btn btn-primary" id="submitButton">
                            <fmt:message bundle="${lang}" key="${empty command.pojo.id ? 'insert' : 'update'}"/>
                        </button>
                        <button type="reset" class="mb-xs mt-xs mr-xs btn btn-default">
                            <fmt:message bundle="${lang}" key="reset"/>
                        </button>
                    </div>
                </form>
            </div>
        </section>
    </div>
</div>
<content tag="specific_script">
    <script src="<c:url value='/template/admin/vendor/bootstrap-multiselect/bootstrap-multiselect.js'/>"></script>
    <script src="<c:url value='/template/admin/vendor/bootstrap-tagsinput/bootstrap-tagsinput.js'/>"></script>
</content>

<content tag="local_script">
    <script type="application/javascript">
        $(document).ready(function () {
            setImagePreviewEvent();

            setFormSubmitEvent();

            processDistrictChange();
        });

        function processDistrictChange() {
            $("#district-select").change(function () {
                $.ajax({
                    type: 'GET',
                    url: '${wardApi}?districtId=' + $(this).val(),
                    dataType: 'json',
                    success: function (data) {
                        $("#ward-select").empty();
                        data.forEach(function (ward) {
                            var newOption = new Option(ward.name, ward.id, false, false);
                            $("#ward-select").append(newOption);
                        });
                        $("#ward-select").trigger('change');
                    }
                });
            });
        }

        function setImagePreviewEvent() {
            $(".imagePreviewWrapper").click(function () {
                $("#buildingImage").trigger('click');
            })

            $("#buildingImage").change(function () {
                if (this.files && this.files[0]) {
                    var reader = new FileReader();

                    reader.onload = function (e) {
                        $('#imagePreview').attr('src', reader.result);
                    }

                    reader.readAsDataURL(this.files[0]);
                }
            })
        }

        function setFormSubmitEvent() {
            $("#buildingForm").submit(async function (e) {
                e.preventDefault();
                var formData = $("#buildingForm").serializeObject();

                var input = document.getElementById("buildingImage");
                if (input.files && input.files[0]) {
                    await getBase64(input.files[0]).then(
                        data => formData.image = data
                    );
                }
                doSubmitForm(formData);
            });
        }

        <c:choose>
        <c:when test="${empty command.pojo.id}">

        function doSubmitForm(data) {
            $.ajax({
                url: '${submitFormUrl}',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(data),
                dataType: 'json',
                success: function (result) {
                    var pnotify = {
                        title: '<fmt:message bundle="${lang}" key="insert.success"/>',
                        text: '<fmt:message bundle="${lang}" key="building.insert.success"/>'.format(result.id),
                        type: 'success'
                    };
                    sessionStorage.setItem("pNotify", JSON.stringify(pnotify));

                    window.location.href = '${submitFormUrl}list';
                },
                error: function (error) {
                    new PNotify({
                        title: '<fmt:message bundle="${lang}" key="insert.error"/>',
                        text: '<fmt:message bundle="${lang}" key="input.check.again"/>',
                        type: 'error'
                    });
                },
            });
        }

        </c:when>
        <c:otherwise>

        function doSubmitForm(data) {
            $.ajax({
                url: '${submitFormUrl}',
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(data),
                dataType: 'json',
                success: function (result) {
                    var pnotify = {
                        title: '<fmt:message bundle="${lang}" key="update.success"/>',
                        text: '<fmt:message bundle="${lang}" key="building.update.success"/>'.format(result.id),
                        type: 'success'
                    };
                    sessionStorage.setItem("pNotify", JSON.stringify(pnotify));

                    window.location.href = '${submitFormUrl}list';
                },
                error: function (error) {
                    new PNotify({
                        title: '<fmt:message bundle="${lang}" key="update.error"/>',
                        text: '<fmt:message bundle="${lang}" key="input.check.again"/>',
                        type: 'error'
                    });
                },
            });
        }

        </c:otherwise>
        </c:choose>


    </script>
</content>
</body>
</html>
