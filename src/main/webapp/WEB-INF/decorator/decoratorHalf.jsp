<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>
<%@page import="kr.esob.tdms.commonlogic.message.LocaleUtil"%>

<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!doctype html>
<html lang="<%=LocaleUtil.getCurrentLanguage(request) %>" class="layout-menu-fixed layout-compact" dir="ltr"
	data-skin="default"
	data-bs-theme="light"
	data-template="vertical-menu-template"
	data-assets-path="${pageContext.request.contextPath}/vuexy/assets/">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<%@ include file="/WEB-INF/jspf/csrf-meta.jspf" %>
	<%@ include file="/WEB-INF/jspf/favicon.jspf" %>

	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/fonts/iconify-icons.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr-themes.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/core.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/css/demo.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.css" />

	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.css" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/select2-master/dist/css/select2.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css?v=20260802.2" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/popup-common.css?v=20260802.2" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css?v=20260803.1" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/popup-vuexy-edit-user.css?v=20260802.2" media="screen" />
	<style>
		.layout-wrapper.bodyWrap .content-wrapper > .container {
			width: 100% !important;
			max-width: none !important;
			min-width: 0;
			margin: 0 !important;
			padding: 10px;
		}
	</style>

	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/helpers.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/js/config.js"></script>

	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/grid.locale-<%=LocaleUtil.getJqGridLanguage(request) %>.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap.min.js"></script> --%>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap-datetimepicker.js"></script> --%>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/esapi.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/ESAPI_Standard_en_US.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/Base.esapi.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/select2-master/dist/js/select2.full.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js?v=20260802.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-ui-i18n.min.js"></script>
	<script>
	loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');
	</script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_util.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_validation.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js?v=20260802.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_form.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_toolbar.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid_paging.js?v=20260726.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js?v=20260802.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rsa.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jsbn.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/prng4.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rng.js"></script>
	<script>
	var CONTEXT_PATH = "${pageContext.request.contextPath}";
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
					<div class="container">
						<div class="nav">
							<h3 class="titleBox"><span></span></h3>
							<p class="navBox">
								${menuPath }
							</p>
						</div>
						<div class="contentArea half">
							<sitemesh:write property='body' />
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<form id="frmPopup" name="frmPopup" action="" method="POST">
		<input type="hidden" name="jsonParam"/>
	</form>
	<div id="popupDialog" class="dialogContainer"></div>
	<div id="viewerDialog" class="dialogContainer"></div>
	<div id="alertMessage"></div>
	<div id="confirmMessage"></div>

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
