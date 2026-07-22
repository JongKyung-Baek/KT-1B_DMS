<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>
<%@page import="kr.esob.fdms.commonlogic.message.LocaleUtil"%>
<%@page import="kr.esob.fdms.controller.login.UserVO"%>
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
<html lang="kr">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta http-equiv="X-UA-Compatible" content="IE=edge" />
	<!-- <link rel="shortcut icon"  type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/main/favicon.ico" /> -->
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-combined.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-datetimepicker.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.css" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/select2-master/dist/css/select2.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css" media="screen" />

	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/grid.locale-kr.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap.min.js"></script> --%>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap-datetimepicker.js"></script> --%>
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
	loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');
	</script>
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
	var URL = '<%=request.getRequestURL()%>';
	loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');
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

		settingForm('${formInfo }');
		settingToolbar(${toolbarInfo });
		settingGrid('${gridInfo }', gridParam, 'gridParam');
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
	<div class="bodyWrap inside">
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
						<li id="distribute"><a href="/inside/production/approval/">배포승인</a></li>
						<li id="print" class="current"><a href="/inside/production/disposalApproval/">폐기승인</a></li>
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
