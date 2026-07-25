<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<title>기술자료 대시보드 - CollabHub</title>
	<link type="text/css" rel="stylesheet"
		  href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css?v=20260726.1">
	<link type="text/css" rel="stylesheet"
		  href="${pageContext.request.contextPath}/resources/css/pages/technical-data-dashboard.css?v=20260726.3">
	<script>
		function moveToTechnicalDataList() {
			location.href = "${pageContext.request.contextPath}/inside/distribution/swRequest/";
		}

		function openDashboardDocument($item) {
			openDialogPopup(
				"/inside/distribution/swRequest/swFilePopup",
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
			<div class="dashboard-eyebrow">
				<span class="dashboard-eyebrow-dot"></span>
				기술자료관리
			</div>
			<h1>내 접근 가능 범위</h1>
			<p>내 인가등급과 사용 권한으로 접근할 수 있는 기술자료, 승인 현황과 파일 활동을 확인합니다.</p>
			<div class="dashboard-context-chips">
				<span class="dashboard-chip dashboard-chip--neutral">
					<i class="icon-base ti tabler-clock"></i>
					${summary.generatedAt} 기준
				</span>
				<span class="dashboard-chip dashboard-chip--primary">
					<i class="icon-base ti tabler-user-check"></i>
					내 권한 기준
				</span>
			</div>
		</div>
		<div class="dashboard-hero-actions">
			<button type="button" class="dashboard-button dashboard-button--primary"
					onclick="moveToTechnicalDataList()">
				<i class="icon-base ti tabler-list-details"></i>
				문서 목록
			</button>
		</div>
	</header>

	<section class="dashboard-summary-grid" aria-label="핵심 현황">
		<article class="dashboard-metric-card dashboard-metric-card--documents">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-files"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label">접근 가능 기술자료</span>
				<strong><fmt:formatNumber value="${summary.totalDocumentCount}" /></strong>
				<span class="dashboard-metric-note">내 문서목록 권한 기준</span>
			</div>
		</article>

		<article class="dashboard-metric-card dashboard-metric-card--recent">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-calendar-plus"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label">최근 30일 등록</span>
				<strong><fmt:formatNumber value="${summary.recentDocumentCount}" /></strong>
				<span class="dashboard-metric-note">내 접근 가능 문서 중 신규</span>
			</div>
		</article>

		<article class="dashboard-metric-card dashboard-metric-card--approval">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-progress-check"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label">승인 진행</span>
				<strong><fmt:formatNumber value="${summary.approvalPendingCount}" /></strong>
				<span class="dashboard-metric-note">내 접근 가능 문서 기준</span>
			</div>
		</article>

		<article class="dashboard-metric-card dashboard-metric-card--clearance">
			<div class="dashboard-metric-icon"><i class="icon-base ti tabler-shield-check"></i></div>
			<div class="dashboard-metric-content">
				<span class="dashboard-metric-label">내 인가등급</span>
				<strong class="dashboard-metric-value--grade">
					<c:out value="${summary.userGradeNm}" default="미지정" />
				</strong>
				<c:choose>
					<c:when test="${not empty summary.clearanceValidTo}">
						<span class="dashboard-metric-note">유효기간 ${summary.clearanceValidTo}까지</span>
					</c:when>
					<c:when test="${not empty summary.userGradeNm}">
						<span class="dashboard-metric-note">별도 만료일 없음</span>
					</c:when>
					<c:otherwise>
						<span class="dashboard-metric-note">유효한 인가정보가 없습니다.</span>
					</c:otherwise>
				</c:choose>
			</div>
		</article>
	</section>

	<section class="dashboard-activity-strip dashboard-panel-card" aria-label="내 최근 30일 파일 활동">
		<div class="dashboard-panel-heading dashboard-panel-heading--compact">
			<div>
				<span class="dashboard-section-kicker">최근 30일</span>
				<h2>내 파일 활동</h2>
			</div>
		</div>
		<div class="dashboard-activity-chips">
			<span class="dashboard-activity-chip dashboard-activity-chip--view">
				<i class="icon-base ti tabler-eye"></i>
				<span>열람</span>
				<strong>${summary.viewCount30Days}</strong>
			</span>
			<span class="dashboard-activity-chip dashboard-activity-chip--download">
				<i class="icon-base ti tabler-download"></i>
				<span>원문 다운로드</span>
				<strong>${summary.downloadCount30Days}</strong>
			</span>
			<span class="dashboard-activity-chip dashboard-activity-chip--print">
				<i class="icon-base ti tabler-printer"></i>
				<span>출력</span>
				<strong>${summary.printCount30Days}</strong>
			</span>
		</div>
	</section>

	<section class="dashboard-middle-grid">
		<article class="dashboard-panel-card">
			<div class="dashboard-panel-heading">
				<div>
					<span class="dashboard-section-kicker">My accessible documents</span>
					<h2>내 문서등급 분포</h2>
				</div>
				<span class="dashboard-chip dashboard-chip--neutral">${summary.totalDocumentCount}건</span>
			</div>
			<div class="dashboard-grade-list">
				<c:choose>
					<c:when test="${empty gradeDistribution}">
						<div class="dashboard-empty-state">
							<i class="icon-base ti tabler-shield-off"></i>
							<span>접근 가능한 문서등급 자료가 없습니다.</span>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="grade" items="${gradeDistribution}">
							<c:set var="gradeTone" value="general" />
							<c:choose>
								<c:when test="${grade.gradeCd eq 'CONFIDENTIAL'}"><c:set var="gradeTone" value="confidential" /></c:when>
								<c:when test="${grade.gradeCd eq 'RESTRICTED'}"><c:set var="gradeTone" value="restricted" /></c:when>
								<c:when test="${grade.gradeCd eq 'INTERNAL'}"><c:set var="gradeTone" value="internal" /></c:when>
								<c:when test="${grade.gradeCd eq 'UNASSIGNED'}"><c:set var="gradeTone" value="unassigned" /></c:when>
							</c:choose>
							<div class="dashboard-grade-row dashboard-grade-row--${gradeTone}">
								<div class="dashboard-grade-meta">
									<span class="document-grade-badge document-grade-badge--${gradeTone}">
										<c:out value="${grade.gradeNm}" />
									</span>
									<strong>${grade.documentCount}건</strong>
									<span>${grade.percentage}%</span>
								</div>
								<div class="dashboard-grade-track" aria-label="${grade.gradeNm} ${grade.percentage}%">
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
					<span class="dashboard-section-kicker">My permissions</span>
					<h2>내 권한 및 확인 항목</h2>
				</div>
			</div>
			<div class="dashboard-permission-grid" aria-label="내 파일 사용 권한">
				<div class="dashboard-permission-chip ${summary.listAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.listAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>LIST</span>
						<strong>문서 목록 ${summary.listAllowedYn eq 'Y' ? '허용' : '미허용'}</strong>
					</div>
				</div>
				<div class="dashboard-permission-chip ${summary.viewAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.viewAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>VIEW</span>
						<strong>열람 ${summary.viewAllowedYn eq 'Y' ? '허용' : '미허용'}</strong>
					</div>
				</div>
				<div class="dashboard-permission-chip ${summary.downloadAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.downloadAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>DOWNLOAD_ORIGINAL</span>
						<strong>원문 다운로드 ${summary.downloadAllowedYn eq 'Y' ? '허용' : '미허용'}</strong>
					</div>
				</div>
				<div class="dashboard-permission-chip ${summary.printAllowedYn eq 'Y' ? 'dashboard-permission-chip--allowed' : 'dashboard-permission-chip--denied'}">
					<i class="icon-base ti ${summary.printAllowedYn eq 'Y' ? 'tabler-circle-check' : 'tabler-circle-x'}"></i>
					<div>
						<span>PRINT</span>
						<strong>출력 ${summary.printAllowedYn eq 'Y' ? '허용' : '미허용'}</strong>
					</div>
				</div>
			</div>
			<div class="dashboard-divider"></div>
			<div class="dashboard-subsection-heading">
				<span>내 접근 문서 상태</span>
				<small>현재 접근 가능한 기술자료 기준</small>
			</div>
			<div class="dashboard-status-chips">
				<c:choose>
					<c:when test="${empty statusDistribution}">
						<span class="dashboard-status-empty">표시할 문서 상태가 없습니다.</span>
					</c:when>
					<c:otherwise>
						<c:forEach var="status" items="${statusDistribution}">
							<span class="dashboard-status-chip">
								<c:out value="${status.statusNm}" />
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
						<span>승인 진행</span>
						<strong>${summary.approvalPendingCount}건</strong>
					</div>
				</div>
				<div class="dashboard-action-chip ${summary.recentDocumentCount gt 0 ? 'dashboard-action-chip--info' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.recentDocumentCount gt 0 ? 'tabler-calendar-plus' : 'tabler-circle-check'}"></i>
					<div>
						<span>최근 30일 신규</span>
						<strong>${summary.recentDocumentCount}건</strong>
					</div>
				</div>
				<div class="dashboard-action-chip ${summary.recentDeniedCount gt 0 ? 'dashboard-action-chip--danger' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.recentDeniedCount gt 0 ? 'tabler-shield-x' : 'tabler-shield-check'}"></i>
					<div>
						<span>내 최근 7일 거부</span>
						<strong>${summary.recentDeniedCount}건</strong>
					</div>
				</div>
				<div class="dashboard-action-chip ${summary.processingFailureCount gt 0 ? 'dashboard-action-chip--danger' : 'dashboard-action-chip--ok'}">
					<i class="icon-base ti ${summary.processingFailureCount gt 0 ? 'tabler-file-alert' : 'tabler-file-check'}"></i>
					<div>
						<span>파일 처리 실패</span>
						<strong>${summary.processingFailureCount}건</strong>
					</div>
				</div>
			</div>
		</article>
	</section>

	<section class="dashboard-bottom-grid">
		<article class="dashboard-panel-card">
			<div class="dashboard-panel-heading">
				<div>
					<span class="dashboard-section-kicker">My accessible documents</span>
					<h2>내 접근 가능 최근 자료</h2>
				</div>
				<button type="button" class="dashboard-text-button" onclick="moveToTechnicalDataList()">
					전체보기 <i class="icon-base ti tabler-chevron-right"></i>
				</button>
			</div>
			<div class="dashboard-document-list">
				<c:choose>
					<c:when test="${empty recentDocuments}">
						<div class="dashboard-empty-state">
							<i class="icon-base ti tabler-file-off"></i>
							<span>접근 가능한 기술자료가 없습니다.</span>
						</div>
					</c:when>
					<c:otherwise>
						<c:forEach var="document" items="${recentDocuments}">
							<c:set var="documentTone" value="general" />
							<c:choose>
								<c:when test="${document.gradeCd eq 'CONFIDENTIAL'}"><c:set var="documentTone" value="confidential" /></c:when>
								<c:when test="${document.gradeCd eq 'RESTRICTED'}"><c:set var="documentTone" value="restricted" /></c:when>
								<c:when test="${document.gradeCd eq 'INTERNAL'}"><c:set var="documentTone" value="internal" /></c:when>
								<c:when test="${document.gradeCd eq 'UNASSIGNED'}"><c:set var="documentTone" value="unassigned" /></c:when>
							</c:choose>
							<button type="button" class="dashboard-document-item"
									data-object-id="<c:out value='${document.objectId}' />"
									data-sw-no="<c:out value='${document.swNo}' />"
									data-request-no="<c:out value='${document.requestNo}' />">
								<span class="dashboard-document-icon"><i class="icon-base ti tabler-file-text"></i></span>
								<span class="dashboard-document-main">
									<span class="dashboard-document-title"><c:out value="${document.documentName}" /></span>
									<span class="dashboard-document-meta">
										<c:out value="${document.swNo}" /> ·
										<c:out value="${document.registerUser}" /> ·
										${document.registeredAt}
									</span>
								</span>
								<span class="dashboard-document-side">
									<span class="document-grade-badge document-grade-badge--${documentTone}">
										<c:out value="${document.gradeNm}" />
									</span>
									<span class="dashboard-file-count">${document.fileCount}개 파일</span>
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
					<span class="dashboard-section-kicker">My file activity</span>
					<h2>내 파일 활동</h2>
				</div>
				<span class="dashboard-chip dashboard-chip--neutral">최신 8건</span>
			</div>
			<div class="dashboard-audit-list">
				<c:choose>
					<c:when test="${empty recentActivities}">
						<div class="dashboard-empty-state">
							<i class="icon-base ti tabler-history-off"></i>
							<span>내 파일 활동 기록이 없습니다.</span>
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
									<c:set var="actionName" value="원문 다운로드" />
									<c:set var="actionIcon" value="tabler-download" />
								</c:when>
								<c:when test="${activity.actionType eq 'PRINT'}">
									<c:set var="actionTone" value="print" />
									<c:set var="actionName" value="출력" />
									<c:set var="actionIcon" value="tabler-printer" />
								</c:when>
								<c:when test="${activity.actionType eq 'VIEW'}">
									<c:set var="actionName" value="열람" />
								</c:when>
							</c:choose>
							<div class="dashboard-audit-item">
								<span class="dashboard-audit-icon dashboard-audit-icon--${actionTone}">
									<i class="icon-base ti ${actionIcon}"></i>
								</span>
								<span class="dashboard-audit-main">
									<span>
										<strong>내가</strong>
										<c:out value="${activity.documentName}" />을(를)
										${actionName}
									</span>
									<small><c:out value="${activity.swNo}" /> · ${activity.occurredAt}</small>
								</span>
								<span class="dashboard-result-chip ${activity.resultCd eq 'DENY' ? 'dashboard-result-chip--deny' : 'dashboard-result-chip--allow'}">
									${activity.resultCd eq 'DENY' ? '거부' : '허용'}
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
