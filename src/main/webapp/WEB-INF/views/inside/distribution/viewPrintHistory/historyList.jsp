<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>접근이력 - CollabHub</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen">
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/access-history.css?v=20260726.1" media="screen">
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
    window.USE_ACCEPTANCE_VUEXY_FORM = true;
    window.accessHistoryConfig = {
        contextPath: '${pageContext.request.contextPath}'
    };

    var gridId = 'gridViewPrintHistoryList';
    var formId = 'formViewPrintHistoryList';

    function setGridParam() {
        gridParam = {
            gridId: gridId,
            formId: formId,
            url: '/inside/distribution/viewPrintHistory/selectList',
            size: '' === $.trim(getCookie('rowNum')) ? 10 : $.trim(getCookie('rowNum')),
            page: 1,
            multiSelect: false,
            numbering: false,
            selectRowAction: '',
            layoutMode: 'invoice',
            fillColumns: true
        };
        return gridParam;
    }

    $(function () {
        $('.layout-wrapper.bodyWrap .content-wrapper > .container')
            .addClass('distribution-invoice-container');
    });
</script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/viewPrintHistory/access-history.js?v=20260726.1"></script>
</head>
<body>
<main class="distribution-invoice-page access-history-page" aria-labelledby="accessHistoryTitle">
    <header class="ah-hero">
        <div class="ah-hero__content">
            <p class="ah-eyebrow">
                <span aria-hidden="true"></span>
                Document access trail
            </p>
            <h1 id="accessHistoryTitle">접근이력</h1>
            <p class="ah-hero__description">
                누가 어느 메뉴에서 어떤 자료를 열람·다운로드·출력했는지와
                접근 허용·차단 및 권한 변경 결과를 확인합니다.
            </p>
            <div class="ah-chip-row" aria-label="조회 범위">
                <span class="ah-chip"><i class="icon-base ti tabler-eye" aria-hidden="true"></i>열람</span>
                <span class="ah-chip"><i class="icon-base ti tabler-download" aria-hidden="true"></i>다운로드</span>
                <span class="ah-chip"><i class="icon-base ti tabler-printer" aria-hidden="true"></i>출력</span>
                <span class="ah-chip"><i class="icon-base ti tabler-shield-lock" aria-hidden="true"></i>권한 변경</span>
            </div>
        </div>
        <div class="ah-hero__visual" aria-hidden="true">
            <span class="ah-hero__visual-icon"><i class="icon-base ti tabler-shield-search"></i></span>
            <div>
                <strong>사용자 · 메뉴 · 행위 · 결과</strong>
                <span>자료 접근의 전체 흐름을 한 화면에서 추적</span>
            </div>
        </div>
    </header>

    <section class="ah-summary" aria-labelledby="accessSummaryTitle">
        <div class="ah-section-heading">
            <div>
                <span>Current result</span>
                <h2 id="accessSummaryTitle">조회 결과 요약</h2>
            </div>
            <p id="accessHistoryUpdatedAt">데이터를 불러오는 중입니다.</p>
        </div>
        <div class="ah-summary__grid">
            <article class="ah-metric ah-metric--total">
                <span class="ah-metric__icon"><i class="icon-base ti tabler-activity"></i></span>
                <div><span>전체 이력</span><strong id="accessMetricTotal">-</strong><small>현재 검색조건 기준</small></div>
            </article>
            <article class="ah-metric ah-metric--success">
                <span class="ah-metric__icon"><i class="icon-base ti tabler-circle-check"></i></span>
                <div><span>허용·성공</span><strong id="accessMetricSuccess">-</strong><small>정상 접근 및 처리</small></div>
            </article>
            <article class="ah-metric ah-metric--denied">
                <span class="ah-metric__icon"><i class="icon-base ti tabler-ban"></i></span>
                <div><span>거부·실패</span><strong id="accessMetricDenied">-</strong><small>정책 차단 및 오류</small></div>
            </article>
            <article class="ah-metric ah-metric--users">
                <span class="ah-metric__icon"><i class="icon-base ti tabler-users"></i></span>
                <div><span>활동 사용자</span><strong id="accessMetricUsers">-</strong><small>고유 사용자 수</small></div>
            </article>
        </div>
    </section>

    <section class="ah-log-card" aria-labelledby="accessLogTitle">
        <div class="ah-log-card__header">
            <div>
                <span class="ah-section-kicker">Access &amp; permission</span>
                <h2 id="accessLogTitle">접근·권한 변경 이력</h2>
                <p>서버에 영속 저장된 접근 판정과 실제 처리 결과를 최신순으로 표시합니다.</p>
            </div>
            <span class="ah-count" id="accessHistoryCount">0건</span>
        </div>

        <form id="accessHistorySearchForm" class="ah-search" role="search" autocomplete="off">
            <label>
                <span>이력 유형</span>
                <select id="accessEventType">
                    <option value="">전체</option>
                    <option value="FILE_ACCESS">자료 접근</option>
                    <option value="DOWNLOAD_RESULT">다운로드 결과</option>
                    <option value="PRINT_RESULT">출력 결과</option>
                    <option value="ACL_CHANGE">권한 변경</option>
                </select>
            </label>
            <label>
                <span>처리 결과</span>
                <select id="accessResultCd">
                    <option value="">전체</option>
                    <option value="ALLOW">허용</option>
                    <option value="DENY">차단</option>
                    <option value="SUCCESS">성공</option>
                    <option value="FAIL">실패</option>
                    <option value="FAILED">출력 실패</option>
                    <option value="CANCELLED">취소</option>
                    <option value="STARTED">시작</option>
                </select>
            </label>
            <label class="ah-search__keyword">
                <span>통합검색</span>
                <input type="search" id="accessKeyword" maxlength="100"
                       placeholder="사용자, 자료 ID, 요청번호, 추적 ID">
            </label>
            <div class="ah-search__actions">
                <button type="button" class="ah-button ah-button--ghost" id="accessResetButton">
                    <i class="icon-base ti tabler-refresh" aria-hidden="true"></i>초기화
                </button>
                <button type="submit" class="ah-button ah-button--primary" id="accessSearchButton">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i>조회
                </button>
            </div>
        </form>

        <div id="accessHistoryMessage" class="ah-message" role="status" aria-live="polite" hidden></div>

        <div class="ah-table-wrap">
            <table class="ah-table" aria-label="접근이력 목록">
                <colgroup>
                    <col class="ah-col-time"><col class="ah-col-user"><col class="ah-col-menu">
                    <col class="ah-col-action"><col class="ah-col-result"><col class="ah-col-resource">
                    <col class="ah-col-reason"><col class="ah-col-ip">
                </colgroup>
                <thead>
                <tr>
                    <th scope="col">일시</th>
                    <th scope="col">사용자</th>
                    <th scope="col">메뉴</th>
                    <th scope="col">행위</th>
                    <th scope="col">결과</th>
                    <th scope="col">대상 자료</th>
                    <th scope="col">사유·메시지</th>
                    <th scope="col">IP</th>
                </tr>
                </thead>
                <tbody id="accessHistoryTableBody">
                <tr class="ah-loading-row">
                    <td colspan="8"><span class="ah-spinner" aria-hidden="true"></span>접근이력을 불러오는 중입니다.</td>
                </tr>
                </tbody>
            </table>
        </div>
        <footer class="ah-table-footer">
            <span><i class="icon-base ti tabler-info-circle" aria-hidden="true"></i>최대 1,000건을 최신순으로 조회합니다.</span>
            <span>민감한 접근기록은 메뉴 권한이 있는 사용자만 조회할 수 있습니다.</span>
        </footer>
    </section>

    <section class="ah-legacy-card" aria-labelledby="legacyHistoryTitle">
        <div class="ah-legacy-card__header">
            <div>
                <span class="ah-section-kicker">Legacy records</span>
                <h2 id="legacyHistoryTitle">기존 열람·출력 기록</h2>
                <p>기존 시스템의 DOCS_HISTORY 기록을 보존하여 함께 조회할 수 있습니다.</p>
            </div>
            <span class="ah-legacy-chip"><i class="icon-base ti tabler-database" aria-hidden="true"></i>과거 기록</span>
        </div>
        <custom:listTemplateInvoice gridId="gridViewPrintHistoryList"/>
    </section>
</main>
</body>
</html>
