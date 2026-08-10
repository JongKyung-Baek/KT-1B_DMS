<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.organization.user.browserTitle" text="사용자 관리" var="pageTitle"/>
<spring:message code="feature.organization.user.resultsAria" text="사용자 검색 및 목록" var="resultsAria"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - ${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/organization-management.css?v=20260802.2" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/distribution/acceptance/common-form-vuexy.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/organizationmanage/organization-management.js?v=20260726.1"></script>
<script>
window.USE_ACCEPTANCE_VUEXY_FORM = true;

var gridId = 'gridInsideUserList';
var formId = 'formInsideUser';

	function organizationText(key, fallback) {
		return window.SdmsI18n && typeof window.SdmsI18n.t === 'function'
			? window.SdmsI18n.t(key, fallback)
			: fallback;
	}

	function escapeOrganizationHtml(value) {
		return String(value === undefined || value === null ? '' : value)
			.replace(/&/g, '&amp;')
			.replace(/</g, '&lt;')
			.replace(/>/g, '&gt;')
			.replace(/"/g, '&quot;')
			.replace(/'/g, '&#39;');
	}

	function localizeUserClearanceGrade(gradeCode, gradeName) {
		var normalizedCode = $.trim(String(gradeCode || '')).toUpperCase();
		switch (normalizedCode) {
			case 'GENERAL':
				return organizationText('feature.documentGrade.general', '일반');
			case 'INTERNAL':
				return organizationText('feature.documentGrade.internal', '사내한');
			case 'RESTRICTED':
				return organizationText('feature.documentGrade.restricted', '제한');
			case 'CONFIDENTIAL':
				return organizationText('feature.documentGrade.confidential', '대외비');
			default:
				return $.trim(String(gradeName || ''));
		}
	}

	function resolveUserClearanceTone(rowdata) {
		var level = parseInt(rowdata && rowdata.clearanceGradeLevel, 10);
		var code = $.trim(String(rowdata && rowdata.clearanceGradeCd || '')).toUpperCase();
		if ((!isNaN(level) && level >= 40) || code === 'CONFIDENTIAL') return 'confidential';
		if ((!isNaN(level) && level >= 30) || code === 'RESTRICTED') return 'restricted';
		if ((!isNaN(level) && level >= 20) || code === 'INTERNAL') return 'internal';
		return 'general';
	}

	function formatUserClearance(cellValue, options, rowdata) {
		var row = rowdata || {};
		var status = $.trim(String(row.clearanceStatus || 'UNASSIGNED')).toUpperCase();
		var gradeName = localizeUserClearanceGrade(
			row.clearanceGradeCd,
			cellValue || row.clearanceGradeNm
		);
		var displayName = gradeName;
		var tone = resolveUserClearanceTone(row);
		var title = organizationText(
			'feature.securityAccess.user.table.currentGrade',
			'현재 인가등급'
		);

		if (status !== 'ACTIVE' || !gradeName) {
			switch (status) {
				case 'EXPIRED':
					displayName = organizationText(
						'feature.organization.user.clearance.expired',
						'만료'
					);
					tone = 'confidential';
					break;
				case 'SCHEDULED':
					displayName = organizationText(
						'feature.organization.user.clearance.scheduled',
						'적용 예정'
					);
					tone = 'internal';
					break;
				case 'INACTIVE_GRADE':
					displayName = organizationText(
						'feature.organization.user.clearance.inactiveGrade',
						'중지 등급'
					);
					tone = 'restricted';
					break;
				default:
					displayName = organizationText(
						'feature.organization.user.clearance.unassigned',
						'미인가'
					);
					tone = 'unassigned';
			}
			title = displayName;
			if (gradeName) title += ' · ' + gradeName;
		} else {
			title += ': ' + gradeName;
		}

		if (row.clearanceValidTo) {
			var validUntil = organizationText(
				'feature.organization.user.clearance.validUntil',
				'유효기간 {0}까지'
			).replace('{0}', row.clearanceValidTo);
			title += ' · ' + validUntil;
		}

		return '<span class="document-grade-badge document-grade-badge--' + tone
			+ '" title="' + escapeOrganizationHtml(title)
			+ '" aria-label="' + escapeOrganizationHtml(title) + '">'
			+ escapeOrganizationHtml(displayName) + '</span>';
	}

	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : formId,
				url : '/general/organizationmanage/insideuser/selectList',
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

	function formatRequestNo(cellValue, options, rowdata, action){
		console.log(rowdata["requestPurpose"]);
		if("PRINT" == rowdata["requestPurpose"]){
			return '<a href="javascript: viewPrintDetail(\'' + cellValue + '\', \'' + rowdata['requestPurpose'] +'\')">' + cellValue + '</button>';
		}else{
			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] +'\')">' + cellValue + '</button>';
		}
	}

	function unlockAccount(){
		if($("#"+ gridId).getGridParam('selarrrow').length < 1){
			alertMessage(organizationText('feature.common.noSelection', '선택된 데이터가 없습니다.'), function(){
				$(this).dialog("close");
			});
			return false;
		}else{
			$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#" + gridId).jqGrid('getRowData', item);
				//if(data.accountLockYn === 'Y'){
					var param = { userCd: data.userCd };
					callAjax(param, '/general/organizationmanage/insideuser/update', unlockAccountCallback);
				//}
			});
		}
	}
	function resetPwd(){
		if($("#"+ gridId).getGridParam('selarrrow').length < 1){
			alertMessage(organizationText('feature.common.noSelection', '선택된 데이터가 없습니다.'), function(){
				$(this).dialog("close");
			});
			return false;
		}else{
			$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#" + gridId).jqGrid('getRowData', item);
				var param = { userCd: data.userCd };
				callAjax(param, '/general/organizationmanage/insideuser/resetPwd', resetPwdCallback);
			});
		}
	}

	/**
	 * 요청 후 결과 메시지 출력
	 * @param response
	 * @returns
	 */
	function unlockAccountCallback(response){
		if(response.success){
			infoMessage(organizationText('feature.common.requestCompleted', '요청이 완료되었습니다.'), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(organizationText('feature.common.requestFailed', '요청이 실패했습니다.'));
		}
	}


	function resetPwdCallback(response){
		if(response.success){
			infoMessage((function(){
				var initialPassword = escapeOrganizationHtml(response.data || '');
				var resetPwdMessage = '초기화가 완료되었습니다. <br/>초기 비밀번호는 "'
					+ initialPassword + '" 입니다.';
				if(window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
					resetPwdMessage = window.SdmsI18n.t(
						'feature.organization.user.passwordReset.completed',
						resetPwdMessage,
						initialPassword
					);
				}
				return resetPwdMessage;
			})(), function(){
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else{
			alertMessage(organizationText(
				response && response.message
					? response.message
					: 'feature.common.requestFailed',
				'요청이 실패했습니다.'
			));
		}
	}



	// 2023.09.11 기범추가 ( 신규 , 생성  버튼 생성 )
	// 신규 생성
	function registerUser(){
		openDialogPopup("/general/organizationmanage/insideuser/registerUserPopup", {}, "popupDialog", 's', 500 );
	}
	// 수정
	// function editUser(){
	// 	openDialogPopup("/general/organizationmanage/insideuser/editUser", {}, "popupDialog", 's', 360 );
	// }

	function formatUserNm(cellValue, options, rowdata, action){
		// console.log("rowdata >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", rowdata)
		return '<a onclick="openUserInfo(\''+rowdata["userCd"]+'\')">'+cellValue+'</a>';
	}


	function openUserInfo(userCd) {
		var param = {};

		if(undefined !== userCd) {
			param["userCd"] = userCd;
		}

		openDialogPopup("/general/organizationmanage/insideuser/editUserPopup", param, "popupDialog", 's', 500);
	}

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});


</script>
</head>
<body>
	<div class="distribution-invoice-page organization-management-page">
		<section class="organization-management-results-card" aria-label="${resultsAria}">
			<custom:listTemplateInvoice gridId="gridInsideUserList"/>
		</section>
	</div>
</body>
</html>
