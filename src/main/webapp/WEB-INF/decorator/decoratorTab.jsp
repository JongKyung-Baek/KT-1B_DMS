<%@page import="kr.esob.fdms.commonlogic.message.LocaleUtil"%>
<%@page import="kr.esob.fdms.controller.login.UserVO"%>
<%@page import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@page import="org.springframework.security.core.Authentication"%>

<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>
<%@page import="kr.esob.fdms.commonlogic.systemconfig.SystemConfig" %>

<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	UserVO userVo = (UserVO) auth.getPrincipal();
	String viewerCabUrl = null;

	if(userVo.getAuthSite().equals("E")) {
		viewerCabUrl = SystemConfig.getSystemConfigValue("VIEWER_CAB_URL_OUT");
	}else {
		viewerCabUrl = SystemConfig.getSystemConfigValue("VIEWER_CAB_URL");
	}
%>

<!doctype html>
<html lang="kr" class="layout-menu-fixed layout-compact" dir="ltr" data-skin="default" data-bs-theme="light">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />

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
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/popup-vuexy-edit-user.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
	<style>
		.layout-wrapper.bodyWrap .content-wrapper > .container {
			width: 100% !important;
			max-width: none !important;
			min-width: 0;
			margin: 0 !important;
			padding: 10px;
		}

		.layout-wrapper.bodyWrap .content-wrapper > .container.distribution-approval-tabs {
			padding: 10px 0 24px !important;
		}

		.distribution-approval-tabs .nav {
			margin: 0;
		}

		.distribution-approval-tabs .tabArea {
			margin: 0 0 1rem;
			border-bottom: 1px solid #d8dee8;
			overflow: visible;
		}

		.distribution-approval-tabs .tabArea ul {
			display: flex;
			gap: 0.5rem;
			margin: 0;
			padding: 0;
			list-style: none;
			position: static;
		}

		.distribution-approval-tabs .tabArea ul li {
			background: transparent;
			min-width: 0;
			height: auto;
			margin: 0;
			padding: 0;
			border: 0;
			border-bottom: 2px solid transparent;
			border-radius: 0;
			color: #6c757d;
			font-weight: 500;
		}

		.distribution-approval-tabs .tabArea ul li a {
			display: block;
			height: auto;
			padding: 0.75rem 1rem;
			color: inherit;
			text-decoration: none;
		}

		.distribution-approval-tabs .tabArea ul li.current {
			background: transparent;
			border-bottom-color: var(--bs-primary);
			color: var(--bs-primary);
		}

		.distribution-approval-tabs .tabArea ul li:not(.current):hover {
			background: transparent;
			border-bottom-color: rgba(var(--bs-primary-rgb), 0.25);
			color: var(--bs-primary);
		}

		.distribution-approval-tabs .tabArea ul li.current a,
		.distribution-approval-tabs .tabArea ul li:not(.current):hover a {
			color: inherit;
		}

		.distribution-approval-tabs .distribution-tab-content {
			margin: 0;
			padding: 0;
			border: 0;
			overflow: visible;
		}

		.distribution-approval-tabs .distribution-tab-content > .distribution-invoice-page {
			width: 100%;
		}

		@media (max-width: 575.98px) {
			.distribution-approval-tabs .nav,
			.distribution-approval-tabs .tabArea,
			.distribution-approval-tabs .distribution-tab-content {
				margin-left: 0;
				margin-right: 0;
			}

			.distribution-approval-tabs .distribution-tab-content {
				margin-left: 0;
				margin-right: 0;
			}

			.distribution-approval-tabs .tabArea ul {
				gap: 0.25rem;
				overflow-x: auto;
				padding-bottom: 0.25rem;
			}

			.distribution-approval-tabs .tabArea ul li a {
				padding: 0.75rem 0.875rem;
				white-space: nowrap;
			}
		}
	</style>

	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/helpers.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/template-customizer.js"></script>
	<script src="${pageContext.request.contextPath}/vuexy/assets/js/config.js"></script>

	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/grid.locale-kr.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/esapi.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/ESAPI_Standard_en_US.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/Base.esapi.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_util.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_validation.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_form.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_toolbar.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid_paging.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/select2-master/dist/js/select2.full.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-ui-i18n.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rsa.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jsbn.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/prng4.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rng.js"></script>
	<script>
	var CONTEXT_PATH = "${pageContext.request.contextPath}";
	var gridParam;
	var AUTH_SITE = '<%=userVo.getAuthSite() %>';
	var USER_CD = '<%=userVo.getUserCd() %>';
	var USER_NM = '<%=userVo.getUserNm() %>';
	var DEPT_CD = '<%=userVo.getDeptCd() %>';
	var BUSINESS_AREA_CD = '<%=userVo.getBusinessAreaCd() %>';
	var TEAM_LEADER_USER_CD = '<%=userVo.getTeamLeaderUid() %>';
	var PASS_USE_YN = '<%=userVo.getPassUseYn() %>';
	var VIEWER_URL = '${viewerUrl}';
	var STATUS_CD = '${statusCd}';
	var START_DT = '${startDt}';
	var END_DT = '${endDt}';
	var DESTROY_STATUS_CD = '${destroyStatusCd}';
	var REQUEST_TYPE = '${requestType}';
	var REQUEST_USER_CD = '${requestUserCd}';
	var REQUEST_USER_NM = '${requestUserNm}';
	var APPROVAL_USER_CD = '${approvalUserCd}';
	var APPROVAL_USER_NM = '${approvalUserNm}';
	var DESTROY_REQUEST_USER_CD = '${destroyRequestUserCd}';
	var DESTROY_REQUEST_USER_NM = '${destroyRequestUserNm}';
	var TERM_LIMIT = '${termLimit}';
	loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');

	$(document).ready(function(){
		$('.tabArea ul > li').removeClass('current');
		$('#MENU_021').addClass('active').parents('li.menu-item').addClass('open');

		setGridParam();
		settingForm('${formInfo }');
		settingToolbar(${toolbarInfo });
		settingGrid('${gridInfo }', gridParam, 'gridParam');
		searchList(gridParam);
		setTabIndex();
	});

	function setTabIndex(){
		var nav = [];
		if(gridParam.gridId === 'gridDistributionApproval'){
			$('.tabArea ul > li').eq(0).addClass('current');
			$('.titleBox span').text("배포승인");
			nav = '<span>자료배포</span><span>요청승인</span><span>배포승인</span>';
		}else if(gridParam.gridId === 'gridPrintApprovalList'){
			$('.tabArea ul > li').eq(1).addClass('current');
			$('.titleBox span').text("출력승인");
			nav = '<span>자료배포</span><span>요청승인</span><span>출력승인</span>';
		}else{
			$('.tabArea ul > li').eq(2).addClass('current');
			$('.titleBox span').text("출력폐기승인");
			nav = '<span>자료배포</span><span>요청승인</span><span>출력폐기승인</span>';
		}
		$(".navBox").html(nav);
	}
	</script>

	<sitemesh:write property='head'/>
</head>

<body>
	<OBJECT classid="clsid:9E93A6E5-4247-416D-BA9C-7485ED08B23A" codebase="<%=viewerCabUrl %>" id="EDIActiveXT" style="display: none;"></OBJECT>
	<div id="viewerCab"></div>

	<div class="layout-wrapper layout-content-navbar bodyWrap">
		<div class="layout-container">
			<jsp:include page="/left.jsp" flush="true"></jsp:include>
			<div class="layout-page">
				<jsp:include page="/header.jsp" flush="true"></jsp:include>
				<div class="content-wrapper">
					<div class="container distribution-invoice-container distribution-approval-tabs">
						<div class="nav">
							<h3 class="titleBox"><span></span></h3>
							<p class="navBox"></p>
						</div>
						<div class="tabArea">
							<ul>
								<li id="distribute"><a href="/inside/distribution/approval/">배포승인</a></li>
								<li id="print"><a href="/inside/distribution/printApproval/">출력승인</a></li>
								<li id="printDisposal"><a href="/inside/distribution/printDestroyApproval/">출력폐기승인</a></li>
							</ul>
						</div>
						<div class="contentArea distribution-tab-content">
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

	<form name="downloadFrm" id="downloadFrm">
		<input type="hidden" name="downloadServerIp" value="${config.updownServerIp }"/>
		<input type="hidden" name="downloadServerPort" value="${config.updownServerPort }"/>
		<input type="hidden" name="downloadLangCode" value="${config.updownLangCode }"/>
		<input type="hidden" name="downloadUserAuth" value="${config.updownUserAuth }"/>
		<input type="hidden" name="downloadSecretKey" value="${config.updownSecretKey}"/>
		<input type="hidden" name="downloadIsSecurity" value="${config.updownIsSecurity}"/>
		<input type="hidden" name="downloadVolume" value="R"/>
		<input type="hidden" name="ShowArray" value=""/>
	</form>

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
