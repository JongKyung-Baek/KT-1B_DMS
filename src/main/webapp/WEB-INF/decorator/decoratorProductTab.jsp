<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>
<%@page import="kr.esob.tdms.commonlogic.message.LocaleUtil"%>
<%@page import="kr.esob.tdms.controller.login.UserVO"%>
<%@page import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@page import="org.springframework.security.core.Authentication"%>

<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	UserVO userVo = (UserVO) auth.getPrincipal();
%>
<!doctype html>
<html lang="<%=LocaleUtil.getCurrentLanguage(request) %>">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<%@ include file="/WEB-INF/jspf/csrf-meta.jspf" %>
	<%@ include file="/WEB-INF/jspf/favicon.jspf" %>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/fonts/iconify-icons.css" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-combined.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-datetimepicker.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.css" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/select2-master/dist/css/select2.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css?v=20260802.2" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css?v=20260804.2" media="screen" />

	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/grid.locale-<%=LocaleUtil.getJqGridLanguage(request) %>.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap.min.js"></script> --%>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap-datetimepicker.js"></script> --%>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/esapi.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/ESAPI_Standard_en_US.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/Base.esapi.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_util.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_validation.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js?v=20260802.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_form.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_toolbar.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid_paging.js?v=20260726.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js?v=20260802.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/select2-master/dist/js/select2.full.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js?v=20260802.1"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-ui-i18n.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rsa.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jsbn.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/prng4.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rng.js"></script>
	<script>
	loadBundles('<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=LocaleUtil.getCurrentLanguage(request) %></spring:escapeBody>', '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${pageContext.request.contextPath}</spring:escapeBody>');
	</script>
	<script>
	var CONTEXT_PATH = "<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${pageContext.request.contextPath}</spring:escapeBody>";
	var gridParam;
	var USER_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=userVo.getUserCd() %></spring:escapeBody>';
	var USER_NM = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=userVo.getUserNm() %></spring:escapeBody>';
	var DEPT_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=userVo.getDeptCd() %></spring:escapeBody>';
	var BUSINESS_AREA_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=userVo.getBusinessAreaCd() %></spring:escapeBody>';
	var TEAM_LEADER_USER_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=userVo.getTeamLeaderUid() %></spring:escapeBody>';
	var PASS_USE_YN = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=userVo.getPassUseYn() %></spring:escapeBody>';
	var VIEWER_URL = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${viewerUrl}</spring:escapeBody>';
	var STATUS_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${statusCd}</spring:escapeBody>';
	var START_DT = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${startDt}</spring:escapeBody>';
	var END_DT = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${endDt}</spring:escapeBody>';
	var DESTROY_STATUS_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${destroyStatusCd}</spring:escapeBody>';
	var REQUEST_TYPE = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${requestType}</spring:escapeBody>';
	var REQUEST_USER_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${requestUserCd}</spring:escapeBody>';
	var REQUEST_USER_NM = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${requestUserNm}</spring:escapeBody>';
	var APPROVAL_USER_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${approvalUserCd}</spring:escapeBody>';
	var APPROVAL_USER_NM = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${approvalUserNm}</spring:escapeBody>';
	var DESTROY_REQUEST_USER_CD = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${destroyRequestUserCd}</spring:escapeBody>';
	var DESTROY_REQUEST_USER_NM = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${destroyRequestUserNm}</spring:escapeBody>';
	var TERM_LIMIT = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${termLimit}</spring:escapeBody>';
	var URL = '<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=request.getRequestURL()%></spring:escapeBody>';
	loadBundles('<spring:escapeBody htmlEscape="false" javaScriptEscape="true"><%=LocaleUtil.getCurrentLanguage(request) %></spring:escapeBody>', '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${pageContext.request.contextPath}</spring:escapeBody>');
	console.log("list");
	$(document).ready(function(){
// 		var $tab = $('.tabArea ul > li').on('click', function() { // show content that matches the index
// 		  var idx = $tab.index(this);
		$('.tabArea ul > li').removeClass('current');

// 		$('.gnbDepthArea .depth1 #MENU_001').addClass('open tgcp'); // left menu check
		$('.gnbDepthArea #MENU_059').addClass('current'); // left menu check
//  		  $('.tabArea ul > li').eq(idx).addClass('current');
// 		});
// 		$(".tabArea ul > li:first").trigger("click");
		setGridParam();
// 		setTabIndex();

		settingForm('<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${formInfo }</spring:escapeBody>');
		settingToolbar(JSON.parse('<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${empty toolbarInfo ? "[]" : toolbarInfo}</spring:escapeBody>'));
		settingGrid('<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${gridInfo }</spring:escapeBody>', gridParam, 'gridParam');
		searchList(gridParam);
	});
	function setTabIndex(){
		var nav = [];
		if(gridParam.gridId === 'gridProductionApprovalList'){
			$('.tabArea ul > li').eq(0).addClass('current');
			$(".titleBox span").text("배포승인");
			nav = '<span>생산기술자료 사내배포</span><span>배포/폐기 승인</span><span>배포승인</span>';
		}else if(gridParam.gridId === 'gridProductionDisposalApprovalList'){
			$('.tabArea ul > li').eq(1).addClass('current');
			$(".titleBox span").text("폐기승인");
			nav = '<span>생산기술자료 사내배포</span><span>배포/폐기 승인</span><span>폐기승인</span>';
		}
		$(".navBox").html(nav);
	}
	</script>


	<sitemesh:write property='head'/>
</head>
<body>
	<div id="viewerCab"></div>
	<div class="bodyWrap general">
		<jsp:include page="/header.jsp" flush="true"></jsp:include>
		<div class="wrap">
			<jsp:include page="/left.jsp" flush="true"></jsp:include>
			<div class="container containerTab"> <!-- Tab whole -->
				<div class="nav">
					<h3 class="titleBox"><span></span></h3>
					<p class="navBox">
					<script>
					$(document).ready(function(){
						setTabIndex();
					});
					</script>
					</p>
				</div>
				<div class="tabArea">
					<ul>
						<li id="distribute"><a href="/general/production/approval/">배포승인</a></li>
						<li id="print" class="current"><a href="/general/production/disposalApproval/">폐기승인</a></li>
					</ul>
				</div>
				<div class="contentArea whole tabContentArea">
					<sitemesh:write property='body' />
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
</body>
</html>
