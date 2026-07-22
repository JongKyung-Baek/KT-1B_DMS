<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>
<%@page import="kr.esob.fdms.commonlogic.message.LocaleUtil"%>
<%@page import="kr.esob.fdms.controller.login.UserVO"%>
<%@page import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@page import="org.springframework.security.core.Authentication"%>

<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<!doctype html>
<html lang="kr" class="layout-menu-fixed layout-compact" dir="ltr" data-skin="default" data-bs-theme="light">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<!-- <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/main/favicon.ico" /> -->

	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/fonts/iconify-icons.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr-themes.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/core.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/css/demo.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.css" />

	<!-- <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-combined.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-datetimepicker.min.css" media="screen" /> -->
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.css" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/select2-master/dist/css/select2.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-common.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/popup-vuexy-edit-user.css" media="screen" />
	<style>
		/* Legacy layout compatibility under Vuexy wrapper */
		.layout-wrapper.bodyWrap .content-wrapper > .container {
			width: 100% !important;
			max-width: none !important;
			min-width: 0;
			margin: 0 !important;
			padding: 10px;
		}
	</style>

	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/helpers.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/template-customizer.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/js/config.js"></script>

	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/grid.locale-kr.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap-datetimepicker.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-ui-i18n.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/esapi.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/ESAPI_Standard_en_US.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/Base.esapi.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/select2-master/dist/js/select2.full.min.js"></script>
	<script>
	loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');
	</script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_util.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_form.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_toolbar.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid_paging.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rsa.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jsbn.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/prng4.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rng.js"></script>

	<script>
	var CONTEXT_PATH = "${pageContext.request.contextPath}";
	var USER_ID = '${sessionUser.userId}';
	var USER_CD = '${sessionUser.userCd}';
	var USER_NM = '${sessionUser.userNm}';
	var APPROVER_YN = '${sessionUser.approverYn}';
	var gridParam;
	console.log("main");
	$(document).ready(function(){
	});
	</script>

	<sitemesh:write property='head'/>
</head>
<body>
	<div class="layout-wrapper layout-content-navbar bodyWrap">
		<div class="layout-container">
			<jsp:include page="/left.jsp" flush="true"></jsp:include>
			<div class="layout-page">
				<jsp:include page="/header.jsp" flush="true"></jsp:include>
				<div class="content-wrapper">
					<sitemesh:write property='body' />
				</div>
			</div>
		</div>
	</div>
	<form id="frmPopup" name="frmPopup" action="" method="POST">
		<input type="hidden" name="jsonParam"/>
		<input type="hidden" name="statusCd"/>
		<input type="hidden" name="requestType"/>
		<input type="hidden" name="purchaserUid"/>
		<input type="hidden" name="requestUserCd"/>
		<input type="hidden" name="requestUserNm"/>
		<input type="hidden" name="destroyRequestUserCd"/>
		<input type="hidden" name="destroyRequestUserNm"/>
		<input type="hidden" name="destroyStatusCd"/>
		<input type="hidden" name="approvalUserCd"/>
		<input type="hidden" name="approvalUserNm"/>
		<input type="hidden" name="termLimit"/>
		<input type="hidden" name="startDt" value="${startDt }"/>
		<input type="hidden" name="endDt" value="${endDt }"/>
	</form>
	<div id="popupDialog" class="dialogContainer"></div>
	<div id="alertMessage"></div>

	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/popper/popper.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/bootstrap.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/hammer/hammer.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/i18n/i18n.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/menu.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/js/main.js"></script>
</body>
</html>


