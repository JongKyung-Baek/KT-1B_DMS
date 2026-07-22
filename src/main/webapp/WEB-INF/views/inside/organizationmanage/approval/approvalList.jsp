<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!-- 배포요청 -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

	function setGridParam(){
		gridParam = {
				gridId : 'gridOrganizationmanageApprovalList',
				formId : 'formOrganizationmanageApproval',
				url : '/inside/organizationmanage/approval/selectList',
				pagerId: 'gridListPager',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false,
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	function loadVendorUser(){
		var companyCd = $("#companyCd_select").val();
		var param = {
				companyCd : companyCd
				};
		callAjax(param, "/inside/organizationmanage/approval/venderUser", setVendorUser, 'json');
	}

	/**
	 * 구매담당자 리스트 호출 후 결과값을 구매담당자 selectBox에 세팅하는 함수
	 * @param response
	 * @returns
	 */
	function setVendorUser(response){
		var newOption;
		$("#approvalUserCd_select").empty().trigger('change');
		$("#approvalUserCd_select").append(new Option("선택하세요","")).trigger('change');
		$.each(response, function(index, data){
			newOption = new Option(data.comboLabel, data.comboVal, false, data.selectedValue === data.comboVal);
			//$("#approvalUserCd_select").select2({minimumResultsForSearch: 1, dropdownParent: $('#popupDialoga')});
			$("#approvalUserCd_select").append(newOption).trigger('change');
		});

	}

	function changeVendorNm() {
		loadVendorUser();
	}

	function formatRequestStatus(cellValue, options, rowdata, action){
		return '<a href="javascript: viewDetail(\'' + cellValue + '\', \''+rowdata["statusCd"]+'\')">' + cellValue + '</button>';
	}

	function viewDetail(requestNo, statusCd){
		openDialogPopup("/inside/organizationmanage/approval/approvalPopup"
				, {requestNo: requestNo, statusCd: statusCd}
				, "popupDialog", 's', 600);
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridOrganizationmanageApprovalList"/>
	</div>
</body>
</html>
