<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Collabhub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
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
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridInsideDeptList"/>
	</div>
</body>
</html>
