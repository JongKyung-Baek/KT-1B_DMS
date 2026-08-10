<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLanguage" />
<spring:message code="feature.documentGrade.general" text="일반" var="gradeGeneralText" />
<spring:message code="feature.documentGrade.internal" text="사내한" var="gradeInternalText" />
<spring:message code="feature.documentGrade.restricted" text="제한" var="gradeRestrictedText" />
<spring:message code="feature.documentGrade.confidential" text="대외비" var="gradeConfidentialText" />
<spring:message code="feature.documentGrade.unassigned" text="미지정" var="unassignedText" />
<spring:message code="feature.techDashboard.permission.allowed" text="허용" var="allowedText" />
<spring:message code="feature.techDashboard.permission.denied" text="미허용" var="deniedText" />
<spring:message code="feature.techDashboard.result.denied" text="거부" var="resultDeniedText" />
<spring:message code="feature.techDashboard.document.untitled" text="제목 없음" var="dashboardUntitledText" />
<!doctype html>
<html lang="${pageLanguage}">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title><spring:message code="feature.techDashboard.pageTitle" text="기술자료 대시보드" /> - ${tdmsBrand.systemName}</title>
	<link type="text/css" rel="stylesheet"
		  href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css?v=20260726.1">
	<link type="text/css" rel="stylesheet"
		  href="${pageContext.request.contextPath}/resources/css/pages/technical-data-dashboard.css?v=20260802.2">
	<script>
		function moveToTechnicalDataList() {
			location.href = "${pageContext.request.contextPath}/general/distribution/swRequest/";
		}

		function openDashboardDocument($item) {
			openDialogPopup(
				"/general/distribution/swRequest/swFilePopup",
				{
					objectId: $item.attr("data-object-id") || "",
					swNo: $item.attr("data-sw-no") || "",
					requestNo: $item.attr("data-request-no") || ""
				},
				"popupDialog",
				"l",
				720,
				true,
				"popup-common popup-sw-file"
			);
		}

		$(function () {
			$(".layout-wrapper.bodyWrap .content-wrapper > .container")
				.addClass("technical-dashboard-container");

			$(document).on("click", ".technical-dashboard .dashboard-document-item", function () {
				openDashboardDocument($(this));
			});
		});
	</script>
</head>
<body>
<c:set var="summary" value="${dashboardSummary}" />
<div class="technical-dashboard">
	<header class="dashboard-hero-card">
		<div class="dashboard-hero-copy">
			<div class="dashboard-hero-overline">
				<div class="dashboard-eyebrow">
					<span class="dashboard-eyebrow-dot"></span>
					<spring:message code="feature.techDashboard.eyebrow" text="기술자료관리" />
				</div>
				<span class="dashboard-generated-at">
					<i class="icon-base ti tabler-clock" aria-hidden="true"></i>
					<spring:message code="feature.techDashboard.generatedAt" text="{0} 기준"
									arguments="${summary.generatedAt}" />
				</span>
			</div>
			<h1><spring:message code="feature.techDashboard.title" text="내 접근 가능 범위" /></h1>
			<p><spring:message code="feature.techDashboard.description"
					text="내 인가등급과 사용 권한으로 접근할 수 있는 기술자료, 승인 현황과 파일 활동을 확인합니다." /></p>
		</div>
		<div class="dashboard-hero-actions">
			<span class="dashboard-chip dashboard-chip--primary">
				<i class="icon-base ti tabler-user-check" aria-hidden="true"></i>
				<spring:message code="feature.techDashboard.permissionBasis" text="내 권한 기준" />
			</span>
			<button type="button" class="dashboard-button dashboard-button--primary"
					onclick="moveToTechnicalDataList()">
				<i class="icon-base ti tabler-list-details"></i>
				<spring:message code="feature.techDashboard.documentList" text="문서 목록" />
			</button>
		</div>
	</header>

	<section class="dashboard-summary-grid"
			 aria-label="<spring:message code='feature.techDashboard.summary.aria' text='핵심 현황' />">
		<article class="dashboard-metric-card dashboard-metric-card--documents">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-files"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label"><spring:message code="feature.techDashboard.metric.accessible" text="접근 가능 기술자료" /></span>
				<strong><fmt:formatNumber value="${summary.totalDocumentCount}" /></strong>
				<span class="dashboard-metric-note"><spring:message code="feature.techDashboard.metric.accessibleNote" text="내 문서목록 권한 기준" /></span>
			</div>
		</article>

		<article class="dashboard-metric-card dashboard-metric-card--recent">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-calendar-plus"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label"><spring:message code="feature.techDashboard.metric.recent30" text="최근 30일 등록" /></span>
				<strong><fmt:formatNumber value="${summary.recentDocumentCount}" /></strong>
				<span class="dashboard-metric-note"><spring:message code="feature.techDashboard.metric.recent30Note" text="내 접근 가능 문서 중 신규" /></span>
			</div>
		</article>

		<article class="dashboard-metric-card dashboard-metric-card--approval">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-progress-check"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label"><spring:message code="feature.techDashboard.metric.approvalPending" text="승인 진행" /></span>
				<strong><fmt:formatNumber value="${summary.approvalPendingCount}" /></strong>
				<span class="dashboard-metric-note"><spring:message code="feature.techDashboard.metric.approvalPendingNote" text="내 접근 가능 문서 기준" /></span>
			</div>
		</article>

		<article class="dashboard-metric-card dashboard-metric-card--clearance">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-shield-check"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label"><spring:message code="feature.techDashboard.metric.clearance" text="내 인가등급" /></span>
				<c:set var="userGradeText" value="${summary.userGradeNm}" />
				<c:choose>
					<c:when test="${summary.userGradeCd eq 'GENERAL'}"><c:set var="userGradeText" value="${gradeGeneralText}" /></c:when>
					<c:when test="${summary.userGradeCd eq 'INTERNAL'}"><c:set var="userGradeText" value="${gradeInternalText}" /></c:when>
					<c:when test="${summary.userGradeCd eq 'RESTRICTED'}"><c:set var="userGradeText" value="${gradeRestrictedText}" /></c:when>
					<c:when test="${summary.userGradeCd eq 'CONFIDENTIAL'}"><c:set var="userGradeText" value="${gradeConfidentialText}" /></c:when>
					<c:when test="${summary.userGradeCd eq 'UNASSIGNED' or empty userGradeText}"><c:set var="userGradeText" value="${unassignedText}" /></c:when>
				</c:choose>
				<strong class="dashboard-metric-value--grade">
					<c:out value="${userGradeText}" default="${unassignedText}" />
				</strong>
				<c:choose>
					<c:when test="${not empty summary.clearanceValidTo}">
						<span class="dashboard-metric-note"><spring:message code="feature.techDashboard.metric.clearanceValidTo"
								text="유효기간 {0}까지" arguments="${summary.clearanceValidTo}" /></span>
					</c:when>
					<c:when test="${not empty summary.userGradeCd or not empty summary.userGradeNm}">
						<span class="dashboard-metric-note"><spring:message code="feature.techDashboard.metric.noExpiry" text="별도 만료일 없음" /></span>
					</c:when>
					<c:otherwise>
						<span class="dashboard-metric-note"><spring:message code="feature.techDashboard.metric.noClearance" text="유효한 인가정보가 없습니다." /></span>
					</c:otherwise>
				</c:choose>
			</div>
		</article>
	</section>

	<section class="dashboard-activity-strip dashboard-panel-card"
			 aria-label="<spring:message code='feature.techDashboard.activity.aria' text='내 최근 30일 파일 활동' />">
		<div class="dashboard-panel-heading dashboard-panel-heading--compact">
			<div>
				<span class="dashboard-section-kicker"><spring:message code="feature.techDashboard.period.recent30" text="최근 30일" /></span>
				<h2><spring:message code="feature.techDashboard.activity.title" text="내 파일 활동" /></h2>
			</div>
		</div>
		<div class="dashboard-activity-chips">
			<span class="dashboard-activity-chip dashboard-activity-chip--view">
				<i class="icon-base ti tabler-eye"></i>
				<span><spring:message code="feature.techDashboard.action.view" text="열람" /></span>
				<strong>${summary.viewCount30Days}</strong>
			</span>
			<span class="dashboard-activity-chip dashboard-activity-chip--download">
				<i class="icon-base ti tabler-download"></i>
				<span><spring:message code="feature.techDashboard.action.downloadOriginal" text="원문 다운로드" /></span>
				<strong>${summary.downloadCount30Days}</strong>
			</span>
			<span class="dashboard-activity-chip dashboard-activity-chip--print">
				<i class="icon-base ti tabler-printer"></i>
				<span><spring:message code="feature.techDashboard.action.print" text="출력" /></span>
				<strong>${summary.printCount30Days}</strong>
			</span>
		</div>
	</section>

	<section class="dashboard-middle-grid">
		<article class="dashboard-panel-card">
			<div class="dashboard-panel-heading">
				<div>
					<span class="dashboard-section-kicker"><spring:message code="feature.techDashboard.kicker.accessibleDocuments" text="접근 가능 문서"/></span>
					<h2><spring:message code="feature.techDashboard.gradeDistribution.title" text="내 문서등급 분포" /></h2>
				</div>
				<span class="dashboard-chip dashboard-chip--neutral"><spring:message code="feature.common.count.items"
						text="{0}건" arguments="${summary.totalDocumentCount}" /></span>
			</div>
			<div class="dashboard-grade-list">
				<c:choose>
					<c:when test="${empty gradeDistribution}">
						<div class="dashboard-empty-state">
							<i class="icon-base ti tabler-shield-off"></i>
							<span><spring:message code="feature.techDashboard.gradeDistribution.empty" text="접근 가능한 문서등급 자료가 없습니다." /></span>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="grade" items="${gradeDistribution}">
							<c:set var="gradeTone" value="general" />
							<c:set var="gradeText" value="${grade.gradeNm}" />
							<c:choose>
								<c:when test="${grade.gradeCd eq 'GENERAL'}"><c:set var="gradeText" value="${gradeGeneralText}" /></c:when>
								<c:when test="${grade.gradeCd eq 'CONFIDENTIAL'}">
									<c:set var="gradeTone" value="confidential" />
									<c:set var="gradeText" value="${gradeConfidentialText}" />
								</c:when>
								<c:when test="${grade.gradeCd eq 'RESTRICTED'}">
									<c:set var="gradeTone" value="restricted" />
									<c:set var="gradeText" value="${gradeRestrictedText}" />
								</c:when>
								<c:when test="${grade.gradeCd eq 'INTERNAL'}">
									<c:set var="gradeTone" value="internal" />
									<c:set var="gradeText" value="${gradeInternalText}" />
								</c:when>
								<c:when test="${grade.gradeCd eq 'UNASSIGNED'}">
									<c:set var="gradeTone" value="unassigned" />
									<c:set var="gradeText" value="${unassignedText}" />
								</c:when>
							</c:choose>
							<div class="dashboard-grade-row dashboard-grade-row--${gradeTone}">
								<div class="dashboard-grade-meta">
									<span class="document-grade-badge document-grade-badge--${gradeTone}">
										<c:out value="${gradeText}" />
									</span>
									<strong><spring:message code="feature.common.count.items"
											text="{0}건" arguments="${grade.documentCount}" /></strong>
									<span>${grade.percentage}%</span>
								</div>
								<div class="dashboard-grade-track" aria-label="${gradeText} ${grade.percentage}%">
									<span style="width: ${grade.percentage}%"></span>
								</div>
							</div>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</div>
		</article>

		<article class="dashboard-panel-card">
			<div class="dashboard-panel-heading">
				<div>
					<span class="dashboard-section-kicker"><spring:message code="feature.techDashboard.kicker.permissions" text="내 권한"/></span>
					<h2><spring:message code="feature.techDashboard.permissions.title" text="내 권한 및 확인 항목" /></h2>
				</div>
			</div>
			<div class="dashboard-permission-grid"
				 aria-label="<spring:message code='feature.techDashboard.permissions.aria' text='내 파일 사용 권한' />">
				<div class="dashboard-permission-chip ${summary.listAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.listAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>LIST</span>
						<strong><spring:message code="feature.techDashboard.permission.list" text="문서 목록" />
							${summary.listAllowedYn eq 'Y' ? allowedText : deniedText}</strong>
					</div>
				</div>
				<div class="dashboard-permission-chip ${summary.viewAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.viewAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>VIEW</span>
						<strong><spring:message code="feature.techDashboard.action.view" text="열람" />
							${summary.viewAllowedYn eq 'Y' ? allowedText : deniedText}</strong>
					</div>
				</div>
				<div class="dashboard-permission-chip ${summary.downloadAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.downloadAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>DOWNLOAD_ORIGINAL</span>
						<strong><spring:message code="feature.techDashboard.action.downloadOriginal" text="원문 다운로드" />
							${summary.downloadAllowedYn eq 'Y' ? allowedText : deniedText}</strong>
					</div>
				</div>
				<div class="dashboard-permission-chip ${summary.printAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.printAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>PRINT</span>
						<strong><spring:message code="feature.techDashboard.action.print" text="출력" />
							${summary.printAllowedYn eq 'Y' ? allowedText : deniedText}</strong>
					</div>
				</div>
			</div>
			<div class="dashboard-divider"></div>
			<div class="dashboard-subsection-heading">
				<span><spring:message code="feature.techDashboard.documentStatus.title" text="내 접근 문서 상태" /></span>
				<small><spring:message code="feature.techDashboard.documentStatus.note" text="현재 접근 가능한 기술자료 기준" /></small>
			</div>
			<div class="dashboard-status-chips">
				<c:choose>
					<c:when test="${empty statusDistribution}">
						<span class="dashboard-status-empty"><spring:message code="feature.techDashboard.documentStatus.empty" text="표시할 문서 상태가 없습니다." /></span>
					</c:when>
					<c:otherwise>
						<c:forEach var="status" items="${statusDistribution}">
							<c:choose>
								<c:when test="${status.statusCd eq 'APPROVED'}">
									<spring:message code="feature.techDashboard.status.approved" text="승인완료" var="statusText" />
								</c:when>
								<c:when test="${status.statusCd eq 'IN_PROGRESS'}">
									<spring:message code="feature.techDashboard.status.inProgress" text="승인진행중" var="statusText" />
								</c:when>
								<c:when test="${status.statusCd eq 'UNASSIGNED'}">
									<spring:message code="feature.techDashboard.status.unassigned" text="상태 미지정" var="statusText" />
								</c:when>
								<c:otherwise>
									<spring:message code="feature.techDashboard.status.other" text="기타" var="statusText" />
								</c:otherwise>
							</c:choose>
							<span class="dashboard-status-chip">
								<c:out value="${statusText}" />
								<strong>${status.documentCount}</strong>
								<small>${status.percentage}%</small>
							</span>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="dashboard-divider"></div>
			<div class="dashboard-action-grid">
				<div class="dashboard-action-chip ${summary.approvalPendingCount gt 0 ? 'dashboard-action-chip--warning' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.approvalPendingCount gt 0 ? 'tabler-progress-check' : 'tabler-circle-check'}"></i>
					<div>
						<span><spring:message code="feature.techDashboard.metric.approvalPending" text="승인 진행" /></span>
						<strong><spring:message code="feature.common.count.items" text="{0}건"
												arguments="${summary.approvalPendingCount}" /></strong>
					</div>
				</div>
				<div class="dashboard-action-chip ${summary.recentDocumentCount gt 0 ? 'dashboard-action-chip--info' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.recentDocumentCount gt 0 ? 'tabler-calendar-plus' : 'tabler-circle-check'}"></i>
					<div>
						<span><spring:message code="feature.techDashboard.action.recent30New" text="최근 30일 신규" /></span>
						<strong><spring:message code="feature.common.count.items" text="{0}건"
												arguments="${summary.recentDocumentCount}" /></strong>
					</div>
				</div>
				<div class="dashboard-action-chip ${summary.recentDeniedCount gt 0 ? 'dashboard-action-chip--danger' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.recentDeniedCount gt 0 ? 'tabler-shield-x' : 'tabler-shield-check'}"></i>
					<div>
						<span><spring:message code="feature.techDashboard.action.recent7Denied" text="내 최근 7일 거부" /></span>
						<strong><spring:message code="feature.common.count.items" text="{0}건"
												arguments="${summary.recentDeniedCount}" /></strong>
					</div>
				</div>
				<div class="dashboard-action-chip ${summary.processingFailureCount gt 0 ? 'dashboard-action-chip--danger' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.processingFailureCount gt 0 ? 'tabler-file-alert' : 'tabler-file-check'}"></i>
					<div>
						<span><spring:message code="feature.techDashboard.action.processingFailed" text="파일 처리 실패" /></span>
						<strong><spring:message code="feature.common.count.items" text="{0}건"
												arguments="${summary.processingFailureCount}" /></strong>
					</div>
				</div>
			</div>
		</article>
	</section>

	<section class="dashboard-bottom-grid">
		<article class="dashboard-panel-card">
			<div class="dashboard-panel-heading">
				<div>
					<span class="dashboard-section-kicker"><spring:message code="feature.techDashboard.kicker.accessibleDocuments" text="접근 가능 문서"/></span>
					<h2><spring:message code="feature.techDashboard.recentDocuments.title" text="내 접근 가능 최근 자료" /></h2>
				</div>
				<button type="button" class="dashboard-text-button" onclick="moveToTechnicalDataList()">
					<spring:message code="feature.common.viewAll" text="전체보기" /> <i class="icon-base ti tabler-chevron-right"></i>
				</button>
			</div>
			<div class="dashboard-document-list">
				<c:choose>
					<c:when test="${empty recentDocuments}">
						<div class="dashboard-empty-state">
							<i class="icon-base ti tabler-file-off"></i>
							<span><spring:message code="feature.techDashboard.recentDocuments.empty" text="접근 가능한 기술자료가 없습니다." /></span>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="document" items="${recentDocuments}">
							<c:set var="documentTone" value="general" />
							<c:set var="documentGradeText" value="${document.gradeNm}" />
							<c:choose>
								<c:when test="${document.gradeCd eq 'GENERAL'}"><c:set var="documentGradeText" value="${gradeGeneralText}" /></c:when>
								<c:when test="${document.gradeCd eq 'CONFIDENTIAL'}">
									<c:set var="documentTone" value="confidential" />
									<c:set var="documentGradeText" value="${gradeConfidentialText}" />
								</c:when>
								<c:when test="${document.gradeCd eq 'RESTRICTED'}">
									<c:set var="documentTone" value="restricted" />
									<c:set var="documentGradeText" value="${gradeRestrictedText}" />
								</c:when>
								<c:when test="${document.gradeCd eq 'INTERNAL'}">
									<c:set var="documentTone" value="internal" />
									<c:set var="documentGradeText" value="${gradeInternalText}" />
								</c:when>
								<c:when test="${document.gradeCd eq 'UNASSIGNED'}">
									<c:set var="documentTone" value="unassigned" />
									<c:set var="documentGradeText" value="${unassignedText}" />
								</c:when>
							</c:choose>
							<button type="button" class="dashboard-document-item"
									data-object-id="<c:out value='${document.objectId}' />"
									data-sw-no="<c:out value='${document.swNo}' />"
									data-request-no="<c:out value='${document.requestNo}' />">
								<span class="dashboard-document-icon"><i class="icon-base ti tabler-file-text"></i></span>
								<span class="dashboard-document-main">
									<span class="dashboard-document-title"><c:out
											value="${document.documentName eq '제목 없음' ? dashboardUntitledText : document.documentName}" /></span>
									<span class="dashboard-document-meta">
										<c:out value="${document.swNo}" /> ·
										<c:out value="${document.registerUser}" /> ·
										${document.registeredAt}
									</span>
								</span>
								<span class="dashboard-document-side">
									<span class="document-grade-badge document-grade-badge--${documentTone}">
										<c:out value="${documentGradeText}" />
									</span>
									<span class="dashboard-file-count"><spring:message code="feature.common.count.files"
											text="{0}개 파일" arguments="${document.fileCount}" /></span>
								</span>
								<i class="icon-base ti tabler-chevron-right dashboard-row-arrow"></i>
							</button>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</div>
		</article>

		<article class="dashboard-panel-card">
			<div class="dashboard-panel-heading">
				<div>
					<span class="dashboard-section-kicker"><spring:message code="feature.techDashboard.kicker.fileActivity" text="내 파일 활동"/></span>
					<h2><spring:message code="feature.techDashboard.activity.title" text="내 파일 활동" /></h2>
				</div>
				<span class="dashboard-chip dashboard-chip--neutral"><spring:message code="feature.techDashboard.activity.latest"
						text="최신 {0}건" arguments="8" /></span>
			</div>
			<div class="dashboard-audit-list">
				<c:choose>
					<c:when test="${empty recentActivities}">
						<div class="dashboard-empty-state">
							<i class="icon-base ti tabler-history-off"></i>
							<span><spring:message code="feature.techDashboard.activity.empty" text="내 파일 활동 기록이 없습니다." /></span>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="activity" items="${recentActivities}">
							<c:set var="actionTone" value="view" />
							<c:set var="actionName" value="${activity.actionType}" />
							<c:set var="actionIcon" value="tabler-eye" />
							<c:choose>
								<c:when test="${activity.actionType eq 'DOWNLOAD_ORIGINAL'}">
									<c:set var="actionTone" value="download" />
									<spring:message code="feature.techDashboard.action.downloadOriginal" text="원문 다운로드" var="actionName" />
									<c:set var="actionIcon" value="tabler-download" />
								</c:when>
								<c:when test="${activity.actionType eq 'PRINT'}">
									<c:set var="actionTone" value="print" />
									<spring:message code="feature.techDashboard.action.print" text="출력" var="actionName" />
									<c:set var="actionIcon" value="tabler-printer" />
								</c:when>
								<c:when test="${activity.actionType eq 'VIEW'}">
									<spring:message code="feature.techDashboard.action.view" text="열람" var="actionName" />
								</c:when>
							</c:choose>
							<div class="dashboard-audit-item">
								<span class="dashboard-audit-icon dashboard-audit-icon--${actionTone}">
									<i class="icon-base ti ${actionIcon}"></i>
								</span>
								<span class="dashboard-audit-main">
									<span>
										<strong>${actionName}</strong>
										<c:out value="${activity.documentName eq '제목 없음' ? dashboardUntitledText : activity.documentName}" />
									</span>
									<small><c:out value="${activity.swNo}" /> · ${activity.occurredAt}</small>
								</span>
								<span class="dashboard-result-chip ${activity.resultCd eq 'DENY' ? 'dashboard-result-chip--deny' : 'dashboard-result-chip--allow'}">
									${activity.resultCd eq 'DENY' ? resultDeniedText : allowedText}
								</span>
							</div>
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</div>
		</article>
	</section>
</div>
<div id="popupDialog" class="dialogContainer"></div>
</body>
</html>
