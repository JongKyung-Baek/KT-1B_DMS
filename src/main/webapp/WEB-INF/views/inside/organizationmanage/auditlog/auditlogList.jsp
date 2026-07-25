<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>운영 감사 로그 - CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/audit-log.css?v=20260726.1" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
    window.USE_ACCEPTANCE_VUEXY_FORM = true;
    var gridId = 'gridInsideAuditLogList';
    var formId = 'formInsideAuditLog';
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/organizationmanage/auditlog/auditlogList.js"></script>
</head>
<body>
    <c:set var="summary" value="${dashboardSummary}" />
    <div class="distribution-invoice-page audit-log-page">
        <div class="audit-log-overview">
            <header class="audit-log-hero">
                <div class="audit-log-hero__content">
                    <div class="audit-log-eyebrow">
                        <span class="audit-log-eyebrow__dot" aria-hidden="true"></span>
                        통합 운영 감사
                    </div>
                    <h1>운영 감사 로그</h1>
                    <p>
                        인증, 메뉴 이용, 자료 접근과 권한 변경 이력을 한곳에서 확인하고
                        사용자부터 처리 결과까지의 흐름을 추적합니다.
                    </p>
                    <div class="audit-log-hero__chips" aria-label="감사 범위">
                        <span class="audit-log-chip">
                            <i class="icon-base ti tabler-shield-check" aria-hidden="true"></i>
                            보안 접근
                        </span>
                        <span class="audit-log-chip">
                            <i class="icon-base ti tabler-route" aria-hidden="true"></i>
                            메뉴 행위
                        </span>
                        <span class="audit-log-chip">
                            <i class="icon-base ti tabler-history" aria-hidden="true"></i>
                            변경 이력
                        </span>
                    </div>
                </div>
                <div class="audit-log-hero__visual" aria-hidden="true">
                    <span class="audit-log-hero__visual-ring">
                        <i class="icon-base ti tabler-shield-search"></i>
                    </span>
                    <strong>누가, 어디서, 무엇을</strong>
                    <span>하나의 감사 흐름으로 추적</span>
                </div>
            </header>

            <section class="audit-log-summary" aria-labelledby="auditLogSummaryTitle">
                <div class="audit-log-section-heading">
                    <div>
                        <span class="audit-log-section-kicker">Today</span>
                        <h2 id="auditLogSummaryTitle">오늘 요약</h2>
                    </div>
                    <span class="audit-log-section-note">오늘 00:00부터 현재까지</span>
                </div>
                <div class="audit-log-summary__grid">
                    <article class="audit-log-metric audit-log-metric--total">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-activity"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label">전체 이벤트</span>
                            <strong><fmt:formatNumber value="${empty summary.totalToday ? 0 : summary.totalToday}" /></strong>
                            <span class="audit-log-metric__note">요청·인증·보안판정 합계</span>
                        </div>
                    </article>
                    <article class="audit-log-metric audit-log-metric--success">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-circle-check"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label">성공·허용</span>
                            <strong><fmt:formatNumber value="${empty summary.successToday ? 0 : summary.successToday}" /></strong>
                            <span class="audit-log-metric__note">정상 처리 및 접근 허용</span>
                        </div>
                    </article>
                    <article class="audit-log-metric audit-log-metric--denied">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-ban"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label">거부</span>
                            <strong><fmt:formatNumber value="${empty summary.deniedToday ? 0 : summary.deniedToday}" /></strong>
                            <span class="audit-log-metric__note">정책에 의해 차단된 접근</span>
                        </div>
                    </article>
                    <article class="audit-log-metric audit-log-metric--failed">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-alert-triangle"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label">실패</span>
                            <strong><fmt:formatNumber value="${empty summary.failedToday ? 0 : summary.failedToday}" /></strong>
                            <span class="audit-log-metric__note">처리 중 오류가 발생한 기록</span>
                        </div>
                    </article>
                    <article class="audit-log-metric audit-log-metric--users">
                        <span class="audit-log-metric__icon" aria-hidden="true">
                            <i class="icon-base ti tabler-users"></i>
                        </span>
                        <div>
                            <span class="audit-log-metric__label">활동 사용자</span>
                            <strong><fmt:formatNumber value="${empty summary.activeUsersToday ? 0 : summary.activeUsersToday}" /></strong>
                            <span class="audit-log-metric__note">오늘 기록된 고유 사용자</span>
                        </div>
                    </article>
                </div>
            </section>

            <section class="audit-log-flow" aria-labelledby="auditLogFlowTitle">
                <div class="audit-log-flow__heading">
                    <span class="audit-log-section-kicker">Audit trail</span>
                    <h2 id="auditLogFlowTitle">운영 행위를 하나의 흐름으로 확인하세요</h2>
                    <p>각 기록은 행위 주체와 이용 메뉴, 수행 행위, 최종 처리 결과를 연결합니다.</p>
                </div>
                <ol class="audit-log-flow__steps">
                    <li>
                        <span class="audit-log-flow__icon audit-log-flow__icon--user" aria-hidden="true">
                            <i class="icon-base ti tabler-user"></i>
                        </span>
                        <span class="audit-log-flow__copy">
                            <small>Who</small>
                            <strong>사용자</strong>
                            <em>이름 · 계정</em>
                        </span>
                    </li>
                    <li>
                        <span class="audit-log-flow__icon audit-log-flow__icon--menu" aria-hidden="true">
                            <i class="icon-base ti tabler-layout-grid"></i>
                        </span>
                        <span class="audit-log-flow__copy">
                            <small>Where</small>
                            <strong>메뉴</strong>
                            <em>화면 · 요청 경로</em>
                        </span>
                    </li>
                    <li>
                        <span class="audit-log-flow__icon audit-log-flow__icon--action" aria-hidden="true">
                            <i class="icon-base ti tabler-bolt"></i>
                        </span>
                        <span class="audit-log-flow__copy">
                            <small>What</small>
                            <strong>행위</strong>
                            <em>열람 · 변경 · 출력</em>
                        </span>
                    </li>
                    <li>
                        <span class="audit-log-flow__icon audit-log-flow__icon--result" aria-hidden="true">
                            <i class="icon-base ti tabler-flag"></i>
                        </span>
                        <span class="audit-log-flow__copy">
                            <small>Outcome</small>
                            <strong>결과</strong>
                            <em>성공 · 거부 · 실패</em>
                        </span>
                    </li>
                </ol>
            </section>
        </div>

        <custom:listTemplateInvoice gridId="gridInsideAuditLogList"/>
    </div>
</body>
</html>
