<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.distributionAccountRequest.page.title" text="배포시스템 계정요청" var="pageTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - ${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/distribution-account-request.css?v=20260802.2" media="screen">
<script>
window.distributionAccountRequestPage = {
    contextPath: '${pageContext.request.contextPath}'
};
</script>
</head>
<body>
<main class="distribution-account-request-page" aria-labelledby="distributionAccountRequestTitle">
    <section class="dar-card">
        <header class="dar-heading">
            <div>
                <h2 id="distributionAccountRequestTitle">${pageTitle}</h2>
                <p><spring:message code="feature.distributionAccountRequest.page.description"
                    text="자사 또는 타사 기술자료배포시스템에서 접수된 계정 요청을 확인하고 승인 또는 반려합니다."/></p>
            </div>
            <button type="button" class="dar-button dar-button--ghost" id="accountRequestRefreshButton">
                <i class="icon-base ti tabler-refresh" aria-hidden="true"></i>
                <spring:message code="feature.common.refresh" text="새로고침"/>
            </button>
        </header>

        <form class="dar-search" id="accountRequestSearchForm" role="search" autocomplete="off">
            <label class="dar-field dar-field--keyword">
                <span><spring:message code="feature.distributionAccountRequest.filter.keyword" text="통합검색"/></span>
                <input type="search" id="accountRequestKeyword" maxlength="100"
                    placeholder="<spring:message code='feature.distributionAccountRequest.filter.keywordPlaceholder' text='연계시스템, 대표담당자, 대상 사용자 검색'/>"/>
            </label>
            <label class="dar-field">
                <span><spring:message code="feature.distributionAccountRequest.filter.sourceSystem" text="연계시스템"/></span>
                <input type="search" id="accountRequestSourceSystem" maxlength="100"
                    placeholder="<spring:message code='feature.common.all' text='전체'/>"/>
            </label>
            <label class="dar-field">
                <span><spring:message code="feature.distributionAccountRequest.filter.requestType" text="요청유형"/></span>
                <select id="accountRequestTypeFilter">
                    <option value=""><spring:message code="feature.common.all" text="전체"/></option>
                    <option value="REGISTER_USER"><spring:message code="feature.distributionAccountRequest.type.REGISTER_USER" text="사용자등록"/></option>
                    <option value="UNLOCK_ACCOUNT"><spring:message code="feature.distributionAccountRequest.type.UNLOCK_ACCOUNT" text="잠금해제"/></option>
                    <option value="RESET_PASSWORD"><spring:message code="feature.distributionAccountRequest.type.RESET_PASSWORD" text="비밀번호 초기화"/></option>
                </select>
            </label>
            <label class="dar-field">
                <span><spring:message code="feature.distributionAccountRequest.filter.status" text="상태"/></span>
                <select id="accountRequestStatusFilter">
                    <option value=""><spring:message code="feature.common.all" text="전체"/></option>
                    <option value="PENDING"><spring:message code="feature.distributionAccountRequest.status.PENDING" text="대기"/></option>
                    <option value="APPROVED"><spring:message code="feature.distributionAccountRequest.status.APPROVED" text="승인"/></option>
                    <option value="REJECTED"><spring:message code="feature.distributionAccountRequest.status.REJECTED" text="반려"/></option>
                </select>
            </label>
            <div class="dar-search__actions">
                <button type="button" class="dar-button dar-button--ghost" id="accountRequestResetButton">
                    <i class="icon-base ti tabler-rotate" aria-hidden="true"></i>
                    <spring:message code="feature.common.reset" text="초기화"/>
                </button>
                <button type="submit" class="dar-button dar-button--primary">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i>
                    <spring:message code="feature.common.search" text="조회"/>
                </button>
            </div>
        </form>

        <div class="dar-alert" id="accountRequestPageMessage" role="status" aria-live="polite" hidden></div>
        <div class="dar-table-wrap">
            <table class="dar-table" aria-label="${pageTitle}">
                <thead>
                <tr>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.sourceSystem" text="연계시스템"/></th>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.representative" text="대표담당자"/></th>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.targetUser" text="대상 사용자"/></th>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.requestType" text="요청유형"/></th>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.receivedAt" text="접수시각"/></th>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.status" text="상태"/></th>
                    <th scope="col"><spring:message code="feature.distributionAccountRequest.column.manage" text="관리"/></th>
                </tr>
                </thead>
                <tbody id="accountRequestTableBody"></tbody>
            </table>
        </div>
        <footer class="dar-table-footer">
            <span id="accountRequestCount"><spring:message code="feature.common.count" text="{0}건" arguments="0"/></span>
            <span><spring:message code="feature.distributionAccountRequest.footer.note"
                text="승인·반려 내역은 요청 정보와 함께 보존됩니다."/></span>
        </footer>
    </section>

    <div class="dar-dialog" id="accountRequestDialog" hidden>
        <div class="dar-dialog__backdrop" data-account-request-close></div>
        <section class="dar-dialog__panel" role="dialog" aria-modal="true"
                 aria-labelledby="accountRequestDialogTitle" tabindex="-1">
            <header class="dar-dialog__header">
                <div>
                    <h2 id="accountRequestDialogTitle"><spring:message
                        code="feature.distributionAccountRequest.dialog.title" text="계정요청 상세"/></h2>
                    <p><spring:message code="feature.distributionAccountRequest.dialog.description"
                        text="원 요청 정보와 대상 사용자를 확인한 뒤 처리사유를 입력하세요."/></p>
                </div>
                <button type="button" class="dar-icon-button" data-account-request-close
                        aria-label="<spring:message code='feature.common.close' text='닫기'/>">
                    <i class="icon-base ti tabler-x" aria-hidden="true"></i>
                </button>
            </header>
            <div class="dar-dialog__body">
                <div class="dar-alert" id="accountRequestDialogMessage" role="status" aria-live="polite" hidden></div>
                <dl class="dar-detail-grid">
                    <div><dt><spring:message code="feature.distributionAccountRequest.detail.requestId" text="요청번호"/></dt><dd id="accountRequestDetailId">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.column.status" text="상태"/></dt><dd id="accountRequestDetailStatus">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.column.sourceSystem" text="연계시스템"/></dt><dd id="accountRequestDetailSystem">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.column.receivedAt" text="접수시각"/></dt><dd id="accountRequestDetailReceivedAt">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.column.representative" text="대표담당자"/></dt><dd id="accountRequestDetailRepresentative">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.detail.representativeContact" text="대표담당자 연락처"/></dt><dd id="accountRequestDetailRepresentativeContact">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.column.targetUser" text="대상 사용자"/></dt><dd id="accountRequestDetailTargetUser">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.detail.targetUserContact" text="대상 사용자 연락처"/></dt><dd id="accountRequestDetailTargetUserContact">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.column.requestType" text="요청유형"/></dt><dd id="accountRequestDetailType">-</dd></div>
                    <div><dt><spring:message code="feature.distributionAccountRequest.detail.externalRequestId" text="연계 요청 ID"/></dt><dd id="accountRequestDetailExternalId">-</dd></div>
                    <div class="dar-detail-grid__wide"><dt><spring:message code="feature.distributionAccountRequest.detail.requestReason" text="요청사유"/></dt><dd id="accountRequestDetailReason">-</dd></div>
                    <div class="dar-detail-grid__wide" id="accountRequestReviewSummary" hidden><dt><spring:message code="feature.distributionAccountRequest.detail.review" text="관리자 처리"/></dt><dd id="accountRequestDetailReview">-</dd></div>
                </dl>

                <section class="dar-events" id="accountRequestEventsSection" hidden>
                    <h3><spring:message code="feature.distributionAccountRequest.events.title" text="처리이력"/></h3>
                    <ol id="accountRequestEventList"></ol>
                </section>

                <section class="dar-decision" id="accountRequestDecisionSection">
                    <label class="dar-field">
                        <span><spring:message code="feature.distributionAccountRequest.label.decisionReason" text="처리사유"/></span>
                        <textarea id="accountRequestDecisionReason" maxlength="1000" rows="4"
                            placeholder="<spring:message code='feature.distributionAccountRequest.placeholder.decisionReason' text='승인 의견은 선택이며, 반려 사유는 필수입니다.'/>" ></textarea>
                        <small><span id="accountRequestDecisionLength">0</span>/1000</small>
                    </label>
                </section>
            </div>
            <footer class="dar-dialog__footer">
                <button type="button" class="dar-button dar-button--ghost" data-account-request-close>
                    <spring:message code="feature.common.close" text="닫기"/>
                </button>
                <span></span>
                <button type="button" class="dar-button dar-button--danger" id="accountRequestRejectButton">
                    <i class="icon-base ti tabler-x" aria-hidden="true"></i>
                    <spring:message code="feature.distributionAccountRequest.action.reject" text="반려"/>
                </button>
                <button type="button" class="dar-button dar-button--primary" id="accountRequestApproveButton">
                    <i class="icon-base ti tabler-check" aria-hidden="true"></i>
                    <spring:message code="feature.distributionAccountRequest.action.approve" text="승인"/>
                </button>
            </footer>
        </section>
    </div>
</main>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/general/system/distributionaccountrequest/distribution-account-request.js?v=20260801.1"></script>
</body>
</html>
