<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.organization.department.browserTitle" text="내부 부서 관리" var="pageTitle"/>
<spring:message code="feature.organization.department.resultsAria" text="내부 부서 검색 및 목록" var="resultsAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/organization-management.css?v=20260726.2" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/organization-management.js?v=20260726.1"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var gridId = 'gridInsideDeptList';
var formId = 'formInsideDept';

	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/inside/organizationmanage/insidedept/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check',
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	// 신규 생성
	function registerDept(){
		openDialogPopup("/inside/organizationmanage/insidedept/registerDeptPopup", {}, "popupDialog", 's', 0, true, 'popup-common popup-inside-dept-register');
	}
	// 수정
	// function editDept(){
	// 	openDialogPopup("/inside/organizationmanage/insidedept/editDeptPopup", {}, "popupDialog", 's', 360 );
	// }

	function formatDeptNm(cellValue, options, rowdata, action){
		// console.log("rowdata >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", rowdata)
		return '<a onclick="openDeptInfo(\''+rowdata["deptCd"]+'\')">'+cellValue+'</a>';
	}


	function openDeptInfo(deptCd) {
		var param = {};

		if(undefined !== deptCd) {
			param["deptCd"] = deptCd;
		}

		openDialogPopup("/inside/organizationmanage/insidedept/editDeptPopup", param, "popupDialog", 's', 0, true, 'popup-common popup-inside-dept-register');
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page organization-management-page">
		<section class="organization-management-results-card" aria-label="${resultsAria}">
			<custom:listTemplateInvoice gridId="gridInsideDeptList"/>
		</section>
	</div>
</body>
</html>
