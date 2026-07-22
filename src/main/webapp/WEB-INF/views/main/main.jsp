<%@page import="kr.esob.fdms.commonlogic.value.Constant"%>
<%@page import="kr.esob.fdms.util.seed.seed.Seed128Cipher"%>
<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />

<!-- main -->
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Dashboard - CollabHub</title>
<script>
window.location.replace("${pageContext.request.contextPath}/inside/distribution/swRequest/");
</script>
	<style>
		#noticeAlert { display: none; }
		/* 공지 팝업은 기존 레이아웃 유지 + 닫기(X)만 노출 */
		.ui-dialog.noticeAlert .ui-dialog-titlebar {
			display: block !important;
			background: transparent !important;
			border: 0 !important;
			padding: 0 !important;
			height: 0 !important;
			min-height: 0 !important;
		}
		.ui-dialog.noticeAlert .ui-dialog-title {
			display: none !important;
		}
		.ui-dialog.noticeAlert .ui-dialog-titlebar-close {
			display: inline-block !important;
			visibility: visible !important;
			position: absolute !important;
			top: 10px !important;
			right: 10px !important;
			background: transparent !important;
			border-radius: 0 !important;
			border: 0 !important;
			width: 24px !important;
			height: 24px !important;
			padding: 0 !important;
			z-index: 2 !important;
		}
		.ui-dialog.noticeAlert .ui-dialog-titlebar-close .ui-icon {
			display: none !important;
		}
		.ui-dialog.noticeAlert .ui-dialog-titlebar-close::before {
			content: 'X';
			display: block;
			color: #444050;
			font-size: 18px;
			line-height: 24px;
			text-align: center;
			font-weight: 700;
		}
	
		/* Vuexy-like dashboard skin only for main page */
	.layout-wrapper.bodyWrap .content-wrapper {
		inline-size: calc(100% - 48px) !important;
		max-inline-size: none !important;
		min-inline-size: 0 !important;
		margin-inline: 24px !important;
	}
	.layout-wrapper.bodyWrap .content-wrapper .main-dashboard-vuexy.container-xxl {
		width: 100% !important;
		max-width: none !important;
		min-width: 0 !important;
		margin: 0 !important;
		padding: 1.5rem 0 !important;
		min-height: 0 !important;
		height: auto !important;
		font-size: 14px !important;
		line-height: 1.4 !important;
	}
	@media (max-width: 575.98px) {
		.layout-wrapper.bodyWrap .content-wrapper {
			inline-size: calc(100% - 32px) !important;
			margin-inline: 16px !important;
		}
	}
	.main-dashboard-vuexy .br {
		width: 100%;
		height: auto;
		margin-top: 0;
		display: grid !important;
		grid-template-columns: minmax(0, 1fr);
		grid-auto-rows: auto;
		gap: 1.5rem;
	}
	.main-dashboard-vuexy .section1,
	.main-dashboard-vuexy .section2 {
		width: 100%;
		height: auto !important;
		padding: 0 !important;
		display: grid !important;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 1.5rem;
		clear: both;
		position: relative;
		z-index: 1;
	}
	.main-dashboard-vuexy .section2 {
		grid-template-columns: minmax(0, 1fr) !important;
	}
	.section2 {margin-bottom: 24px !important;}
	.main-dashboard-vuexy .section1 .statusArea {
		grid-column: 1 / -1;
	}
	.main-dashboard-vuexy .section1 .deadlineArea {
		grid-column: 1 / -1;
	}
	.main-dashboard-vuexy .section1 > div,
	.main-dashboard-vuexy .section2 > div,
	.main-dashboard-vuexy .section1 > div:first-child,
	.main-dashboard-vuexy .section2 > div:first-child {
		width: auto !important;
		height: auto !important;
		margin: 0 !important;
		display: block !important;
	}
	.main-dashboard-vuexy .dashboard-panel {
		background: #fff;
		border: 0;
		border-radius: .6rem;
		box-shadow: 0 .375rem 1rem rgba(34, 41, 47, .10);
	}
	.main-dashboard-vuexy .section1 h4,
	.main-dashboard-vuexy .section2 h4 {
		border-bottom-color: #eef0f2;
	}
	.main-dashboard-vuexy .section1 h4 > span,
	.main-dashboard-vuexy .section2 h4 > span {
		color: #444050;
		font-weight: 600;
	}
	.main-dashboard-vuexy .section1 table thead th,
	.main-dashboard-vuexy .section2 table thead th {
		background: #f8f9fa;
		border-bottom-color: #e5e7eb;
	}
	.main-dashboard-vuexy .section1 table tbody tr:nth-child(2n) th,
	.main-dashboard-vuexy .section1 table tbody tr:nth-child(2n) td,
	.main-dashboard-vuexy .section2 table tbody tr:nth-child(2n) th,
	.main-dashboard-vuexy .section2 table tbody tr:nth-child(2n) td {
		background: #fbfcfd;
	}
	.main-dashboard-vuexy .section1 .deadlineArea {
		height: auto;
		display: flex !important;
		flex-wrap: wrap;
		align-items: stretch;
		column-gap: 1.5rem;
		row-gap: 1rem;
	}
	.main-dashboard-vuexy .section1 .deadlineArea > .printDeadlineArea,
	.main-dashboard-vuexy .section1 .deadlineArea > .destroyDeadlineArea {
		flex: 1 1 calc(50% - .75rem);
		max-width: calc(50% - .75rem);
	}
	.main-dashboard-vuexy .section1 .deadlineArea > div:only-child {
		flex-basis: 100%;
		max-width: 100%;
	}
	.main-dashboard-vuexy .section1 .printDeadlineArea,
	.main-dashboard-vuexy .section1 .destroyDeadlineArea {
		display: flex;
		flex-direction: column;
		min-height: 19rem;
	}
	.main-dashboard-vuexy .section1 .statusArea {
		display: flex;
		flex-direction: column;
		min-height: 0 !important;
	}
	.main-dashboard-vuexy .section1 h4 + div,
	.main-dashboard-vuexy .section1 h4 + ul {
		height: auto !important;
		overflow: visible !important;
	}
	.main-dashboard-vuexy .section1 .statusArea > ul,
	.main-dashboard-vuexy .section1 .printDeadlineArea > div,
	.main-dashboard-vuexy .section1 .destroyDeadlineArea > div {
		flex: 1 1 auto;
		height: auto !important;
		min-height: 0;
	}
	.main-dashboard-vuexy .section1 .statusArea > ul > li,
	.main-dashboard-vuexy .section1 .statusArea > ul > li > div,
	.main-dashboard-vuexy .section1 .statusArea > ul > li ul {
		height: auto !important;
	}
	.main-dashboard-vuexy .section1 .statusArea > ul > li ul {
		display: block;
	}
	.main-dashboard-vuexy .section1 .deadlineArea > div,
	.main-dashboard-vuexy .section1 .deadlineArea > div:first-child {
		width: auto !important;
		height: auto !important;
		margin: 0 !important;
	}
	.main-dashboard-vuexy .section1 .printDeadlineArea,
	.main-dashboard-vuexy .section1 .destroyDeadlineArea {
		min-height: 14rem;
	}
	.main-dashboard-vuexy .section1 table tbody td.noData,
	.main-dashboard-vuexy .section2 table tbody td.noData,
	.main-dashboard-vuexy .section1 table tbody .noData,
	.main-dashboard-vuexy .section2 table tbody .noData {
		position: static !important;
		top: auto !important;
		margin-top: 0 !important;
		height: 2.5rem !important;
		display: table-cell !important;
	}
	.main-dashboard-vuexy .section2 .quickMenuArea > ul {
		height: auto !important;
		min-height: 260px;
		margin: 0;
		padding: 1rem 0;
		display: flex;
		flex-wrap: nowrap;
	}
	.main-dashboard-vuexy .section2 .quickMenuArea > ul li {
		flex: 1 1 20%;
		width: auto !important;
		min-width: 0;
		height: auto !important;
		min-height: 220px;
		border-right: 1px solid #eef0f2;
	}
	.main-dashboard-vuexy .section2 .quickMenuArea > ul li:last-child {
		border-right: 0;
	}
	.main-dashboard-vuexy .section2 .quickMenuArea li .moreBtn {
		border-radius: .45rem;
		bottom: 3rem;
	}
	@media (max-width: 1199.98px) {
		.main-dashboard-vuexy .section1,
		.main-dashboard-vuexy .section2 {
			grid-template-columns: 1fr;
		}
		.main-dashboard-vuexy .section2 .quickMenuArea > ul {
			flex-wrap: wrap;
		}
		.main-dashboard-vuexy .section2 .quickMenuArea > ul li {
			flex-basis: 50%;
			width: 50%;
			border-right: 0;
			border-bottom: 1px solid #eef0f2;
		}
	}
	@media (max-width: 991.98px) {
		.main-dashboard-vuexy .section1 .deadlineArea > .printDeadlineArea,
		.main-dashboard-vuexy .section1 .deadlineArea > .destroyDeadlineArea {
			flex-basis: 100%;
			max-width: 100%;
		}
	}
	@media (max-width: 767.98px) {
		.main-dashboard-vuexy .section2 .quickMenuArea > ul li {
			width: 100%;
			min-height: 180px;
		}
	}
</style>
<script>

	var approverYn = '${sessionUser.approverYn}';

	$(document).ready(function(){
		$('.noticeArea .noticeTab').click(function(e){
			e.preventDefault();

			$('.noticeArea h4').find('*').removeClass('on');
			$('.noticeArea table').css({'top':'300px', 'position':'absolute'});

			if($(this).hasClass('tab1') == 1){
				$('.noticeArea > h4').find('.tab1').addClass('on');
				$('#jqGridNotice').css({'top':'0', 'position':'static'});

			}else if($(this).hasClass('tab2') == 1){
				$('.noticeArea > h4').find('.tab2').addClass('on');
				$('#jqGridQnA').css({'top':'0', 'position':'static'});
			}
		});
		$('.noticeArea .noticeTab.tab1').trigger('click');
		if($('#sessionPwdExpiredYn').val() == "Y"){
			alertMessage(g_msg("msg.expiredPassword"));   //비밀번호 유효기간이 만료되었습니다. 새로운 비밀번호로 변경해 주십시오.
			return;
		}

		$("input[name=todayNotOpen]").prettyCheckable();

		var cookiedata = document.cookie;

			<c:if test="${not empty noticeAlert }">
			if(cookiedata.indexOf("noticeAlert=Y")<0){
				$("#noticeAlert").dialog({
					dialogClass: 'sizeS noticeAlert',
					height: 500,
					close: function () {
						if ($("#todayNotOpen").is(":checked")) {
							setCookie("noticeAlert", "Y", 1);
						} else {
							document.cookie = "noticeAlert=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/";
						}
					}
				});
			}
			</c:if>

				// 체크 상태만 유지하고, 닫기(X) 시점에만 하루간 숨김을 적용한다.
				$("#todayNotOpen").off("change");

		$("#noticeAlert").focus();
	});

	/**
		공지사항 파일 다운로드
	*/
	function fileDownload(noticeCd,fileNo){
		$("form[name=tmpForm]")
		.attr("action","/bbs/notice/fileDownload?noticeCd="+noticeCd+"&fileNo="+fileNo)
		.attr("target", "hiddenFrame")
		.attr("method", "post")
		.submit();
	}

	function moveTermLimitData(inout, objectType) {

		var url = '';

		if('I' === inout) {
			url = '/inside/distribution/printHistory/'
			$("input[name=termLimit]").val(objectType);
		}
		else {
			if(objectType.toLowerCase().indexOf("product") > -1) {
				objectType = "production";
			}

			url = '/outside/' + objectType.toLowerCase() + '/approvalStatus/'
			$("input[name=termLimit]").val("disposal");
		}


		$("#frmPopup")
			.attr("action", url).submit();
	}

	function moveDistributionPage(inout, objectType) {
		var prefix = ('E' === inout ? '/outside/' : '/inside/');

		if('I' === inout) {
			url = 'distribution/' + objectType + 'Request/';
		}
		else {
			url = objectType + '/request/';
		}

		$("#frmPopup")
			.attr("action", prefix + url).submit();
	}

	function movePage(inout, objectType, statusCd) {
		var url = '';
		var prefix = ('E' === inout ? '/outside/' : '/inside/');

		if('I' === inout) {
			// 내부 사용자			
			 if('distribution' === objectType) {
				// 배포요청
				if('APPROVAL' === statusCd) {
					if('Y' === approverYn) {
						url = 'distribution/approval/';
					}
					else {
						url = 'distribution/requeststatus/';
						$("input[name=statusCd]").val(statusCd);
					}
				}
				else if('REJECT' === statusCd) {
					url = 'distribution/history/';
					$("input[name=approvalUserCd]").val(USER_CD);
					$("input[name=approvalUserNm]").val(USER_NM);
					$("input[name=statusCd]").val(statusCd);
				}
				else if('ACCEPT' === statusCd) {
					url = 'distribution/acceptance/';
				}
				else {
					url = 'distribution/requeststatus/';
				}

				$("input[name=requestType]").val(objectType.toUpperCase());
			}
			else if('print' === objectType) {
				// 출력
				if('APPROVAL' === statusCd) {
					if('Y' === approverYn) {
						url = 'distribution/printApproval/';
					}
					else {
						url = 'distribution/requeststatus/';
						$("input[name=statusCd]").val(statusCd);
					}
				}
				else {
					url = 'distribution/requeststatus/';

					if('REJECT' === statusCd) {
						$("input[name=statusCd]").val(statusCd);
					}
				}

				$("input[name=requestType]").val(objectType.toUpperCase());
			}
			else if('printDisposal' === objectType) {
				// 출력물 폐기
				if('APPROVAL' === statusCd) {
					url = 'distribution/printDestroyApproval/';
				}
				else {
					url = 'distribution/printHistory/';
					$("input[name=destroyRequestUserCd]").val(USER_CD);
					$("input[name=destroyRequestUserNm]").val(USER_NM);
					$("input[name=destroyStatusCd]").val("1");
				}
			}
			else if('cr' === objectType) {
				if('APPROVAL' === statusCd) {
					url = 'cr/approval/';
				}
				else if('ACCEPT' === statusCd) {
					url = 'cr/acceptance/';
					$("input[name=purchaserUid]").val(USER_ID);

				}
			}
			else if('unreg' === objectType) {
				if('APPROVAL' === statusCd) {
					if('Y' === approverYn) {
						url = 'unregisted/approval/';
					}
					else {
						url = 'unregisted/requeststatus/';
						$("input[name=statusCd]").val(statusCd);
					}
				}
				else if('REJECT' === statusCd) {
					url = 'unregisted/requeststatus/';
					$("input[name=statusCd]").val(statusCd);
				}
			}
			else if('production' === objectType) {
				// 생산기술자료
				if('APPROVAL' === statusCd) {
					if('Y' === approverYn) {
						url = 'production/approval/';
					}
					else {
						url = 'production/requeststatus/';
						$("input[name=statusCd]").val('APPROVAL');
					}

					$("input[name=requestType]").val('DISTRIBUTION');
				}
				else if('REJECT' === statusCd) {
					if('Y' === approverYn) {
						url = 'production/approval/';
					}
					else {
						url = 'production/requeststatus/';
						$("input[name=statusCd]").val('REJECT');
					}

					$("input[name=requestType]").val('DISTRIBUTION');

					/* url = 'distribution/history/';
					$("input[name=approvalUserCd]").val(USER_CD);
					$("input[name=approvalUserNm]").val(USER_NM);
					$("input[name=statusCd]").val(statusCd); */
				}
				else if('ACCEPT' === statusCd) {
					url = 'production/acceptance/';
					/* url = 'distribution/requeststatus/'; */
				}
			}
			else if('productionDisposal' === objectType) {
				if('APPROVAL' === statusCd) {
					url = 'production/disposalApproval/';
				}
				else if('REJECT' === statusCd) {
					/* url = 'distribution/history/';
					$("input[name=approvalUserCd]").val(USER_CD);
					$("input[name=approvalUserNm]").val(USER_NM);
					$("input[name=statusCd]").val(statusCd); */
				}
				else {
					/* url = 'distribution/requeststatus/'; */
				}
			}
			else if('productionPrint' === objectType) {
				if('APPROVAL' === statusCd) {
					if('Y' === approverYn) {
						url = 'production/approval/';
					}
					else {
						url = 'production/requeststatus/';
						$("input[name=statusCd]").val('APPROVAL');
					}

					$("input[name=requestType]").val('PRINT');
				}
				else if('REJECT' === statusCd) {
					/* url = 'distribution/history/';
					$("input[name=approvalUserCd]").val(USER_CD);
					$("input[name=approvalUserNm]").val(USER_NM);
					$("input[name=statusCd]").val(statusCd); */
				}
				else {
					/* url = 'distribution/requeststatus/'; */
				}
			}

		}
		else {
			// 외부사용자			
			var middle = '';

			if('DRAWING' === objectType.toUpperCase() || 'SW' === objectType.toUpperCase() || 'DOC' === objectType.toUpperCase() || 'CR' === objectType.toUpperCase()) {
				middle = objectType.toLowerCase();
			}
			else {
				middle = 'production';
			}

			$("input[name=statusCd]").val(statusCd);

			var postfix = (('APPROVAL' === statusCd.toUpperCase() && 'CR' !== objectType.toUpperCase()) ? '/approvalStatus/' : '/request/');
			//var postfix = '/request/';

			url = middle + postfix;
		}

		url = prefix + url;

		//$("input[name=statusCd]").val(statusCd);

		$("#frmPopup")
			.attr("action", url).submit();
	}
</script>
</head>
<body>
	<c:choose>
		<c:when test="${'Y' == sessionUser.approverYn}">
			<c:set var="approverClass" value="approver"></c:set>
		</c:when>
		<c:otherwise>
			<c:set var="approverClass" value=""></c:set>
		</c:otherwise>
	</c:choose>
	<c:choose>
		<c:when test="${'Y' == sessionUser.purchaseTeamYn}">
			<c:set var="purchaseClass" value="purchase"></c:set>
		</c:when>
		<c:otherwise>
			<c:set var="purchaseClass" value=""></c:set>
		</c:otherwise>
	</c:choose>


	<div class="container-xxl flex-grow-1 container-p-y whole main-dashboard-vuexy">
		<!--
		<div class="imageRollingArea">			
			<p><span>소스에 답이 있다</span><span>미래전투 환경에 대비하기 위한 연구개발에 역량을 집중합니다.</span></p> 
			
			<ul>
				<li class="roll1">image roll 1</li>
				<li class="roll2">image roll 2</li>
				<li class="roll3">image roll 3</li>
				<li class="roll4">image roll 4</li>
				<li class="roll5">image roll 5</li>
			</ul>
			
		</div>
		-->
		<div class="br ${purchaseClass } ${approverClass }"> <!--  / purchase / product / purchase approver -->
			<div class="section1">
				<div class="statusArea dashboard-panel">
					<!-- <h4><span><spring:message code="label.approvalStatus"/></span></h4> --> <!-- 결재현황 -->
					<c:if test="${false}">
					<ul>
						<c:if test="${not empty data.accept }">
							<li class="acceptance"> <!-- 접수 -->
								<h5><span><spring:message code="label.acceptance"/></span><span class="count">${data.accept.sum }</span></h5><!-- 접수 -->
								<div>
									<ul>
										<c:if test="${not empty data.accept.distribution }">
											<li><span><spring:message code="label.distribution"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'distribution', 'ACCEPT')" class="statusCount linkBtn">${data.accept.distribution }</a></li><!-- 자료배포 -->
										</c:if>
										<c:if test="${not empty data.accept.drawing }">
											<li><span><spring:message code="label.drawing"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'drawing', 'ACCEPT')" class="statusCount linkBtn">${data.accept.drawing }</a></li><!-- 도면 -->
										</c:if>
										<c:if test="${not empty data.accept.doc }">
											<li><span><spring:message code="label.doc"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'doc', 'ACCEPT')" class="statusCount linkBtn">${data.accept.doc }</a></li><!-- 문서 -->
										</c:if>
										<c:if test="${not empty data.accept.sw }">
											<li><span><spring:message code="label.sw"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'sw', 'ACCEPT')" class="statusCount linkBtn">${data.accept.sw }</a></li><!-- sw -->
										</c:if>
<%--										<c:if test="${not empty data.accept.cr }">--%>
<%--											<li><span><spring:message code="label.cr"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'cr', 'ACCEPT')" class="statusCount linkBtn">${data.accept.cr }</a></li><!-- CR -->--%>
<%--										</c:if>--%>
										<c:if test="${not empty data.accept.production }">
											<li><span><spring:message code="label.product"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'production', 'ACCEPT')" class="statusCount linkBtn">${data.accept.production }</a></li><!-- 생산기술자료 -->
										</c:if>
									</ul>
								</div>
							</li>
						</c:if>
						<c:if test="${not empty data.request }">
							<li class="request"> <!-- 요청 -->
								<h5><span><spring:message code="label.request"/></span><span class="count">${data.request.sum }</span></h5><!-- 요청 -->
								<div>
									<ul>
										<c:if test="${not empty data.request.distribution }">
											<li><span><spring:message code="label.distribution"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'distribution', '')" class="statusCount linkBtn">${data.request.distribution }</a></li><!-- 자료배포 -->
										</c:if>
										<c:if test="${not empty data.request.print }">
											<li><span><spring:message code="label.print"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'print', '')" class="statusCount linkBtn">${data.request.print }</a></li><!-- 출력 -->
										</c:if>
										<c:if test="${not empty data.request.printDisposal }">
											<li><span><spring:message code="label.printDisposal"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'printDisposal', '')" class="statusCount linkBtn">${data.request.printDisposal }</a></li><!-- 출력물 폐기 -->
										</c:if>
									</ul>
								</div>
							</li>
						</c:if>
						<li class="approval"> <!-- 승인 -->
							<h5><span><spring:message code="label.approval"/></span><span class="count">${data.approval.sum }</span></h5>
							<div>
								<ul>
									<c:if test="${not empty data.approval.distribution }">
										<li><span><spring:message code="label.distribution"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'distribution', 'APPROVAL')" class="statusCount linkBtn">${data.approval.distribution }</a></li><!-- 자료배포 -->
									</c:if>
									<c:if test="${not empty data.approval.drawing }">
										<li><span><spring:message code="label.drawing"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'drawing', 'APPROVAL')" class="statusCount linkBtn">${data.approval.drawing }</a></li><!-- 도면 -->
									</c:if>
									<c:if test="${not empty data.approval.doc }">
										<li><span><spring:message code="label.doc"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'doc', 'APPROVAL')" class="statusCount linkBtn">${data.approval.doc }</a></li><!-- 문서 -->
									</c:if>
									<c:if test="${not empty data.approval.sw }">
										<li><span><spring:message code="label.sw"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'sw', 'APPROVAL')" class="statusCount linkBtn">${data.approval.sw }</a></li><!-- sw -->
									</c:if>
<%--									<c:if test="${not empty data.approval.cr }">--%>
<%--										<li><span>CR</span><a href="javascript: movePage('${sessionUser.authSite }', 'cr', 'APPROVAL')" class="statusCount linkBtn">${data.approval.cr }</a></li>--%>
<%--									</c:if>--%>
									<c:if test="${not empty data.approval.print }">
										<li><span><spring:message code="label.print"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'print', 'APPROVAL')" class="statusCount linkBtn">${data.approval.print }</a></li><!-- 출력 -->
									</c:if>
									<c:if test="${not empty data.approval.printDisposal }">
										<li><span><spring:message code="label.printDisposal"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'printDisposal', 'APPROVAL')" class="statusCount linkBtn">${data.approval.printDisposal }</a></li><!-- 출력물 폐기 -->
									</c:if>
<%--									<c:if test="${not empty data.approval.unreg }">--%>
<%--										<li><span><spring:message code="label.unReg"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'unreg', 'APPROVAL')" class="statusCount linkBtn">${data.approval.unreg }</a></li><!-- 미등록자료 -->--%>
<%--									</c:if>--%>
									<c:if test="${not empty data.approval.production }">
										<li><span><spring:message code="label.product"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'production', 'APPROVAL')" class="statusCount linkBtn">${data.approval.production }</a></li> <!-- 생산기술자료 -->
									</c:if>
									<c:if test="${not empty data.approval.productionPrint }">
										<li><span><spring:message code="label.productPrint"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'productionPrint', 'APPROVAL')" class="statusCount linkBtn">${data.approval.productionPrint }</a></li> <!-- 생산기술자료 출력 -->
									</c:if>
									<c:if test="${not empty data.approval.productionDisposal }">
										<li><span><spring:message code="label.productDisposal"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'productionDisposal', 'APPROVAL')" class="statusCount linkBtn">${data.approval.productionDisposal }</a></li> <!-- 생산기술자료 폐기 -->
									</c:if>
								</ul>
							</div>
						</li>
						<c:if test="${not empty data.reject }">
							<li class="reject">
								<h5><span><spring:message code="btn.reject"/></span><span class="count">${data.reject.sum }</span></h5> <!-- 반려 -->
								<div>
									<ul>
										<c:if test="${not empty data.reject.distribution }">
											<li><span><spring:message code="label.distribution"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'distribution', 'REJECT')" class="statusCount linkBtn">${data.reject.distribution }</a></li>
										</c:if>
										<c:if test="${not empty data.reject.drawing }">
											<li><span><spring:message code="label.drawing"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'drawing', 'REJECT')" class="statusCount linkBtn">${data.reject.drawing }</a></li><!-- 도면 -->
										</c:if>
										<c:if test="${not empty data.reject.doc }">
											<li><span><spring:message code="label.doc"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'doc', 'REJECT')" class="statusCount linkBtn">${data.reject.doc }</a></li><!-- 문서 -->
										</c:if>
										<c:if test="${not empty data.reject.sw }">
											<li><span><spring:message code="label.sw"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'sw', 'REJECT')" class="statusCount linkBtn">${data.reject.sw }</a></li><!-- sw -->
										</c:if>
<%--										<c:if test="${not empty data.reject.cr }">--%>
<%--											<li><span>CR</span><a href="javascript: movePage('${sessionUser.authSite }', 'cr', 'REJECT')" class="statusCount linkBtn">${data.reject.cr }</a></li>--%>
<%--										</c:if>--%>
										<c:if test="${not empty data.reject.print }">
											<li><span><spring:message code="label.print"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'print', 'REJECT')" class="statusCount linkBtn">${data.reject.print }</a></li>
										</c:if>
										<c:if test="${not empty data.reject.printDisposal }">
											<li><span><spring:message code="label.printDisposal"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'printDisposal', 'REJECT')" class="statusCount linkBtn">${data.reject.printDisposal }</a></li>
										</c:if>
										<c:if test="${not empty data.reject.unreg }">
											<li><span><spring:message code="label.unReg"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'unreg', 'REJECT')" class="statusCount linkBtn">${data.reject.unreg }</a></li>
										</c:if>
										<c:if test="${not empty data.reject.production }">
											<li><span><spring:message code="label.product"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'production', 'REJECT')" class="statusCount linkBtn">${data.reject.production }</a></li>
										</c:if>
										<c:if test="${not empty data.reject.productionPrint }">
											<li><span><spring:message code="label.productPrint"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'productionPrint', 'REJECT')" class="statusCount linkBtn">${data.reject.productionPrint }</a></li>
										</c:if>
										<c:if test="${not empty data.reject.productionDisposal }">
											<li><span><spring:message code="label.productDisposal"/></span><a href="javascript: movePage('${sessionUser.authSite }', 'productionDisposal', 'REJECT')" class="statusCount linkBtn">${data.reject.productionDisposal }</a></li>
										</c:if>
									</ul>
								</div>
							</li>
						</c:if>
					</ul>
					</c:if>
				</div>
				<div class="deadlineArea">
					<c:if test="${'I' == sessionUser.authSite}">
						<div class="printDeadlineArea dashboard-panel">
							<h4>
								<span>미승인 목록</span><!-- <button class="ui-button ui-corner-all moreBtn" onclick="javascript: moveTermLimitData('${sessionUser.authSite }', 'print')"><spring:message code="label.more"/> --><!-- 더보기 --><!-- </button> -->
							</h4>
							<div>
								<table id="jqGridPrintDeadline">
									<colgroup>
										<col width="80"/>
										<col width="120"/>
										<col />
									</colgroup>
									<tbody>
										<c:if test="${empty pendingApprovalList }">
											<tr>
												<td colspan="3" class="noData">데이터가 없습니다.</td>
											</tr>
										</c:if>
										<c:if test="${not empty pendingApprovalList }">
											<c:forEach items="${pendingApprovalList }" var="item">
												<tr>
													<td>${item.useEndYmd }</td><!-- 출력기한 -->
													<td>${item.objectTypeNm }</td><!-- 자료유형 -->
													<td>${item.objectNo }</td><!-- 자료번호(대표) -->
												</tr>
											</c:forEach>
										</c:if>
									</tbody>
								</table>
							</div>
						</div>
					</c:if>
					<div class="destroyDeadlineArea dashboard-panel">
						<h4>
							<span>
								<c:choose>
									<c:when test="${'I' == sessionUser.authSite}">
										미검토 목록
									</c:when>
									<c:otherwise>
										미검토 목록
									</c:otherwise>
								</c:choose>
							</span>
							<c:choose>
								<c:when test="${'I' == sessionUser.authSite}">
									<!-- <button class="ui-button ui-corner-all moreBtn" onclick="javascript: moveTermLimitData('${sessionUser.authSite }', 'disposal')"> -->
								</c:when>
								<c:otherwise>
									<!-- <button class="ui-button ui-corner-all moreBtn" onclick="javascript: moveTermLimitData('${sessionUser.authSite }', 'disposal')"> -->
								</c:otherwise>
							</c:choose>
							<!-- <spring:message code="label.more"/> --><!-- 더보기 -->
							</button>
						</h4>
						<div>
							<table id="jqGridDestroyDeadline">
								<c:choose>
									<c:when test="${'I' == sessionUser.authSite}">
										<colgroup>
											<col width="80"/>
											<col width="120"/>
											<col />
										</colgroup>
									</c:when>
									<c:otherwise>
										<colgroup>
											<col width="80"/>
											<col width="120"/>
											<col width="100"/>		<!-- 배포유형 -->
											<col width="80"/>		<!-- 요청일 -->
											<col />
										</colgroup>
										<thead>
											<tr>
												<th>유효기간</th>
												<th>자료유형</th>
												<th>배포유형</th>
												<th>요청일</th>
												<th>대표문서명</th>
											</tr>
										</thead>
									</c:otherwise>
								</c:choose>
								<tbody>
									<c:if test="${empty pendingReviewList }">
										<tr>
											<c:choose>
												<c:when test="${'I' == sessionUser.authSite}">
													<td colspan="3" class="noData">데이터가 없습니다.</td>
												</c:when>
												<c:otherwise>
													<td colspan="5" class="noData">데이터가 없습니다.</td>
												</c:otherwise>
											</c:choose>
										</tr>
									</c:if>
									<c:if test="${not empty pendingReviewList }">
										<c:forEach items="${pendingReviewList }" var="item">
											<tr>
												<td>${item.useEndYmd }</td><!-- 폐기기한 -->
												<c:choose>
													<c:when test="${'I' == sessionUser.authSite}">
														<td>${item.objectTypeNm }</td><!-- 자료유형 -->
													</c:when>
													<c:otherwise>
														<td>
															<a href="javascript: moveTermLimitData('${sessionUser.authSite }', '${item.objectType }')" class="">${item.objectTypeNm }</a>
														</td><!-- 자료유형 -->
													</c:otherwise>
												</c:choose>
												<c:if test="${'E' == sessionUser.authSite}">
													<td>${item.deployTypeNm }</td>
													<td>${item.requestDt }</td>
												</c:if>
												<td>${item.objectNo }</td><!-- 자료번호(대표) -->
											</tr>
										</c:forEach>
									</c:if>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
			<div class="section2">
				<c:if test="${false}">
				<div class="noticeArea dashboard-panel">
					<h4>
						<span class="noticeTab tab1 on"><spring:message code="label.notice"/><!-- 공지사항 --></span>
						<span class="sideBorder"></span>
						<span class="noticeTab tab2">QnA</span>
						<button class="ui-button ui-corner-all moreBtn tab1 on" onclick="location.href='/bbs/notice/'"><spring:message code="label.more"/><!-- 더보기 --></button>
						<button class="ui-button ui-corner-all moreBtn tab2" onclick="location.href='/bbs/qna/'"><spring:message code="label.more"/><!-- 더보기 --></button>
					</h4>
					<div>
						<table id="jqGridNotice">
							<colgroup>
								<col width="30"/>
								<col />
							</colgroup>
							<thead>
								<tr>
									<th>No.</th>
									<th>제목</th>
								</tr>
							</thead>
							<tbody>
								<c:if test="${empty notice }">
									<tr>
										<td colspan="2" class="noData">데이터가 없습니다.</td>
									</tr>
								</c:if>
								<c:if test="${not empty notice }">
									<c:forEach items="${notice }" var="item">
										<tr>
											<th>${item.noticeCd }</th>
											<td style="text-align: left;">${item.title }</td>
										</tr>
									</c:forEach>
								</c:if>
							</tbody>
						</table>
						<table id="jqGridQnA">
							<colgroup>
								<col width="30"/>
								<col />
							</colgroup>
							<thead>
								<tr>
									<th>No.</th>
									<th>제목</th>
								</tr>
							</thead>
							<tbody>
								<c:if test="${empty qna }">
									<tr>
										<td colspan="2" class="noData">데이터가 없습니다.</td>
									</tr>
								</c:if>
								<c:if test="${not empty qna }">
									<c:forEach items="${qna }" var="item">
										<tr>
											<c:choose>
												<c:when test="${not empty item.parentQnaCd }">
													<td colspan="2" style="text-align: left;">&nbsp;&nbsp;&nbsp;&nbsp;└ ${item.title }</td>
												</c:when>
												<c:otherwise>
													<th>${item.qnaCd }</th>
													<td style="text-align: left;">${item.title }</td>
												</c:otherwise>
											</c:choose>
										</tr>
									</c:forEach>
								</c:if>
							</tbody>
						</table>
					</div>
				</div>
				</c:if>
				<div class="quickMenuArea">
					<ul class="dashboard-panel">
						<li class="quickMenu drawing">
							<p>Documents</p>
							
							<button class="ui-button ui-corner-all moreBtn" onclick="moveDistributionPage('${sessionUser.authSite }', 'drawing')">바로가기</button>
						</li>
						<li class="quickMenu document">
							<p>IOC</p>
							
							<button class="ui-button ui-corner-all moreBtn" onclick="moveDistributionPage('${sessionUser.authSite }', 'doc')">바로가기</button>
						</li>
						<li class="quickMenu software">
							<p>CCB</p>
							
							<button class="ui-button ui-corner-all moreBtn" onclick="moveDistributionPage('${sessionUser.authSite }', 'sw')">바로가기</button>
						</li>
						<li class="quickMenu production">
							<p>MRB</p>
						
							<button class="ui-button ui-corner-all moreBtn" onclick="moveDistributionPage('${sessionUser.authSite }', 'production')">바로가기</button>
						</li>
						<li class="quickMenu dxf">
							<p>PMPCB</p>
							
							<button class="ui-button ui-corner-all moreBtn" onclick="moveDistributionPage('${sessionUser.authSite }', 'dxf')">바로가기</button>
						</li>
					</ul>
<%--					<div class="installArea">--%>
<%--						<c:choose>--%>
<%--							<c:when test="${'E' == sessionUser.authSite}">--%>
<%--								<!-- <a href="/fileDownload?fileName=install/DaVuEDIActiveXT(NSU-8-99-5-0).exe">Viewer 수동설치</a> -->--%>
<%--								<a href="/fileDownload?fileName=<%=URLEncoder.encode(Seed128Cipher.encrypt("install/DocsAutoSetUp_ExOut.exe", Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)) %>">보안드라이브 수동설치</a>--%>
<%--								<a href="/fileDownload?fileName=<%=URLEncoder.encode(Seed128Cipher.encrypt("install/ExtUpDown.exe", Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)) %>">업다운로더 수동설치</a>--%>
<%--							</c:when>--%>
<%--							<c:otherwise>--%>
<%--								<a href="/fileDownload?fileName=<%=URLEncoder.encode(Seed128Cipher.encrypt("install/ExtUpDown.exe", Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)) %>">업다운로더 수동설치</a>--%>
<%--							</c:otherwise>--%>
<%--						</c:choose>--%>
<%--					</div>--%>
<%--					<script>--%>
<%--					$('.installArea a').each(function(e){--%>
<%--						var anum = $('.installArea a').length;--%>
<%--						var anumw = 100 / anum;--%>
<%--						if(anum == '1'){--%>
<%--							$(this).css({'display':'block', 'width':'100%', 'margin-right':'0'});--%>
<%--						}else if(anum == '2'){--%>
<%--							$(this).css({'width':'calc(' + anumw + '% - 5px)', 'margin-right':'10px'});--%>
<%--							$('.installArea a:last-of-type').css({'margin-right':'0'});--%>
<%--						}--%>
<%--					});--%>
<%--					</script>--%>
				</div>
				</div>
		</div>
	</div>

	<div id="noticeAlert" title="공지사항">
		<div class="noticeAlertWrap">
			<h3 class="title">${noticeAlert.noticeTitle }</h3>
			<c:if test="${not empty formData }">
				<c:if test="${not empty formData.fileList }">
				<div class="fileDownload">
					<label><spring:message code="grid.appendFile" /></label>
					<c:forEach items="${formData.fileList }" var="fileInfo">
					<button type="button" class="ui-corner-all fileDownBtn" onclick="fileDownload('${fileInfo.noticeCd}', '${fileInfo.fileNo }')" ><span>${fileInfo.fileNm }</span></button>
					</c:forEach>
				</div>
				</c:if>
			</c:if>
			<div class="contents">
				<div class="preContent">${noticeAlert.contents }</div>
			</div>
		</div>
		<div class="todayCheckBox">
			<input type="checkbox" name="todayNotOpen" id="todayNotOpen"/><label for="todayNotOpen">하루간 열지 않기</label>
		</div>
	</div>
	<form name="tmpForm"></form>
</body>
</html>
