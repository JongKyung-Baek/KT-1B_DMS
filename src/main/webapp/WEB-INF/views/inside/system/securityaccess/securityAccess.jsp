<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>보안 접근통제 관리</title>
    <c:if test="${not empty _csrf}">
        <meta name="_csrf" content="${_csrf.token}">
        <meta name="_csrf_header" content="${_csrf.headerName}">
    </c:if>
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen">
    <link type="text/css" rel="stylesheet"
          href="${pageContext.request.contextPath}/resources/css/inside/system/securityaccess/securityAccess.css?v=20260726.2" media="screen">
    <script>
        window.securityAccessConfig = {
            contextPath: '${pageContext.request.contextPath}'
        };
    </script>
    <script type="text/javascript"
            src="${pageContext.request.contextPath}/resources/js/inside/system/securityaccess/securityAccess.js?v=20260726.3"></script>
</head>
<body>
<main class="security-access-page" aria-labelledby="securityAccessTitle">
    <section class="sa-shell">
        <header class="sa-page-header">
            <div>
                <p class="sa-eyebrow">Security access control</p>
                <h1 id="securityAccessTitle">보안 접근통제 관리</h1>
                <p class="sa-page-description">자료 보안등급, 사용자 인가범위와 문서별 접근 사용자를 관리합니다.</p>
            </div>
            <div class="sa-policy-note" aria-label="접근 판정 원칙">
                <span class="sa-policy-note__label">접근 판정</span>
                <strong>인가등급 + 전역 행위권한 + 파일등급 + 문서별 사용자 권한</strong>
            </div>
        </header>

        <div id="securityAccessMessage" class="sa-message" role="status" aria-live="polite" hidden></div>

        <nav class="sa-tabs" id="securityAccessTabs" role="tablist" aria-label="보안 접근통제 관리 항목">
            <button type="button" class="sa-tab is-active" id="gradeTab"
                    role="tab" aria-selected="true" aria-controls="gradePanel" data-tab-target="gradePanel">
                <span class="sa-tab__step">1</span>보안등급
            </button>
            <button type="button" class="sa-tab" id="userTab"
                    role="tab" aria-selected="false" aria-controls="userPanel" data-tab-target="userPanel">
                <span class="sa-tab__step">2</span>사용자 인가
            </button>
            <button type="button" class="sa-tab" id="fileTab"
                    role="tab" aria-selected="false" aria-controls="filePanel" data-tab-target="filePanel">
                <span class="sa-tab__step">3</span>문서등급/권한
            </button>
        </nav>

        <section id="gradePanel" class="sa-tab-panel is-active" role="tabpanel" aria-labelledby="gradeTab">
            <div class="sa-layout sa-layout--form-list sa-layout--balanced">
                <article class="sa-card">
                    <div class="sa-card-header sa-card-header--stack-mobile">
                        <div>
                            <h2>등급 정보</h2>
                            <p>숫자가 클수록 높은 보안등급입니다.</p>
                        </div>
                        <button type="button" class="sa-button sa-button--ghost" id="gradeNewButton">신규 입력</button>
                    </div>
                    <form id="gradeForm" class="sa-form" autocomplete="off">
                        <input type="hidden" id="gradeOriginalCd" value="">
                        <div class="sa-form-grid sa-form-grid--grade">
                            <label class="sa-field">
                                <span>등급 코드 <em>*</em></span>
                                <input type="text" id="gradeCd" maxlength="30" placeholder="예: CONFIDENTIAL" required>
                            </label>
                            <label class="sa-field">
                                <span>등급명 <em>*</em></span>
                                <input type="text" id="gradeNm" maxlength="80" placeholder="예: 대외비" required>
                            </label>
                            <label class="sa-field">
                                <span>등급 순위 <em>*</em></span>
                                <input type="number" id="gradeLevel" min="0" max="9999" step="1" placeholder="예: 20" required>
                            </label>
                            <label class="sa-field sa-field--full">
                                <span>설명</span>
                                <textarea id="gradeDescription" maxlength="500" rows="4"
                                          placeholder="등급 적용 기준과 취급 원칙을 입력하세요."></textarea>
                            </label>
                        </div>
                        <div class="sa-check-row">
                            <label class="sa-switch">
                                <input type="checkbox" id="gradeUseYn" checked>
                                <span>사용</span>
                            </label>
                            <label class="sa-switch">
                                <input type="checkbox" id="gradeDefaultYn">
                                <span>신규 자료 기본등급</span>
                            </label>
                        </div>
                        <p class="sa-help">기본등급은 하나만 지정할 수 있으며, 변경 시 기존 기본등급은 자동 해제됩니다.</p>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary" id="gradeSaveButton">등급 저장</button>
                        </div>
                    </form>
                </article>

                <article class="sa-card sa-card--table">
                    <div class="sa-card-header sa-card-header--stack-mobile">
                        <div>
                            <h2>등록된 보안등급</h2>
                            <p>행을 선택하면 왼쪽에서 수정할 수 있습니다.</p>
                        </div>
                        <button type="button" class="sa-button sa-button--ghost" id="gradeReloadButton">새로고침</button>
                    </div>
                    <div class="sa-table-wrap">
                        <table class="sa-table" aria-label="등록된 보안등급">
                            <thead>
                            <tr>
                                <th scope="col">순위</th>
                                <th scope="col">코드</th>
                                <th scope="col">등급명</th>
                                <th scope="col">기본</th>
                                <th scope="col">사용</th>
                            </tr>
                            </thead>
                            <tbody id="gradeTableBody">
                            <tr class="sa-empty-row"><td colspan="5">등급을 불러오는 중입니다.</td></tr>
                            </tbody>
                        </table>
                    </div>
                </article>
            </div>
        </section>

        <section id="userPanel" class="sa-tab-panel" role="tabpanel" aria-labelledby="userTab" hidden>
            <div class="sa-layout sa-layout--split sa-layout--balanced">
                <article class="sa-card sa-card--table">
                    <div class="sa-card-header">
                        <div>
                            <h2>사용자 선택</h2>
                            <p>사용자를 선택해 최대 인가등급과 행위권한을 설정합니다.</p>
                        </div>
                    </div>
                    <div class="sa-list-toolbar">
                        <form id="userSearchForm" class="sa-search" role="search">
                            <label class="sa-sr-only" for="userKeyword">사용자 검색어</label>
                            <input type="search" id="userKeyword" maxlength="100" placeholder="계정, 이름, 부서 검색">
                            <button type="submit" class="sa-button sa-button--secondary" id="userSearchButton">조회</button>
                        </form>
                    </div>
                    <div class="sa-table-wrap sa-table-wrap--tall">
                        <table class="sa-table" aria-label="사용자 목록">
                            <thead>
                            <tr>
                                <th scope="col">계정</th>
                                <th scope="col">이름</th>
                                <th scope="col">부서</th>
                                <th scope="col">현재 인가등급</th>
                            </tr>
                            </thead>
                            <tbody id="userTableBody">
                            <tr class="sa-empty-row"><td colspan="4">사용자를 조회해 주세요.</td></tr>
                            </tbody>
                        </table>
                    </div>
                </article>

                <article class="sa-card">
                    <div class="sa-card-header">
                        <div>
                            <h2>사용자 인가 설정</h2>
                            <p>선택한 사용자의 유효기간과 허용 행위를 지정합니다.</p>
                        </div>
                    </div>
                    <div id="selectedUserSummary" class="sa-selection-summary is-empty">
                        왼쪽 목록에서 사용자를 선택하세요.
                    </div>
                    <form id="clearanceForm" class="sa-form" autocomplete="off">
                        <label class="sa-field">
                            <span>최대 인가등급 <em>*</em></span>
                            <select id="userGradeCd" required disabled>
                                <option value="">등급 선택</option>
                            </select>
                        </label>

                        <fieldset class="sa-permission-fieldset" disabled>
                            <legend>행위권한 <em>*</em></legend>
                            <div class="sa-permission-grid">
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="LIST"><span><strong>LIST</strong><small>목록 노출</small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="DETAIL"><span><strong>DETAIL</strong><small>상세 조회</small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="VIEW"><span><strong>VIEW</strong><small>보안 뷰어 열람</small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="DOWNLOAD_ORIGINAL"><span><strong>DOWNLOAD ORIGINAL</strong><small>원본 파일 사용</small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="PRINT"><span><strong>PRINT</strong><small>출력 요청</small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="MANAGE_ACL"><span><strong>MANAGE ACL</strong><small>접근권한 관리</small></span></label>
                            </div>
                        </fieldset>

                        <div class="sa-form-grid">
                            <label class="sa-field">
                                <span>유효 시작일</span>
                                <input type="date" id="clearanceValidFrom" disabled>
                            </label>
                            <label class="sa-field">
                                <span>유효 종료일</span>
                                <input type="date" id="clearanceValidTo" disabled>
                            </label>
                            <label class="sa-field sa-field--full">
                                <span>부여·변경 사유 <em>*</em></span>
                                <textarea id="clearanceGrantReason" maxlength="500" rows="3" disabled
                                          placeholder="업무 목적과 승인 근거를 입력하세요."></textarea>
                            </label>
                        </div>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary" id="clearanceSaveButton" disabled>인가정보 저장</button>
                        </div>
                    </form>
                </article>

            </div>
        </section>

        <section id="filePanel" class="sa-tab-panel" role="tabpanel" aria-labelledby="fileTab" hidden>
            <div class="sa-layout sa-layout--split">
                <article class="sa-card sa-card--table">
                    <div class="sa-card-header sa-card-header--stack-mobile">
                        <div>
                            <h2>파일 선택</h2>
                            <p>자료유형과 검색어로 등급을 지정할 파일을 찾습니다.</p>
                        </div>
                        <form id="fileSearchForm" class="sa-search sa-search--wide" role="search">
                             <label class="sa-sr-only" for="fileObjectType">자료유형</label>
                             <select id="fileObjectType">
                                <option value="SW">기술자료 (주파일)</option>
                                <option value="SW_SUB">기술자료 (보조파일)</option>
                                <option value="DOCUMENT">일반문서</option>
                                 <option value="DRAWING">도면</option>
                                <option value="PRODUCT_DOCUMENT">생산기술문서</option>
                                <option value="PRODUCT_SW">생산기술 소프트웨어</option>
                                 <option value="DXF">DXF</option>
                                <option value="PEER_REVIEW">Peer Review</option>
                                <option value="DOCUMENT_SUB">Document sub-file</option>
                                <option value="DRAWING_SUB">Drawing sub-file</option>
                                <option value="PRODUCT_DOCUMENT_SUB">Production document sub-file</option>
                                <option value="PRODUCT_SW_SUB">Production SW sub-file</option>
                                <option value="DXF_SUB">DXF sub-file</option>
                             </select>
                            <label class="sa-sr-only" for="fileKeyword">파일 검색어</label>
                            <input type="search" id="fileKeyword" maxlength="100" placeholder="자료번호, 제목, 파일명 검색">
                            <button type="submit" class="sa-button sa-button--secondary" id="fileSearchButton">조회</button>
                        </form>
                    </div>
                    <div class="sa-table-wrap sa-table-wrap--tall">
                        <table class="sa-table" aria-label="파일 목록">
                            <thead>
                            <tr>
                                 <th scope="col">자료유형</th>
                                 <th scope="col">자료번호</th>
                                <th scope="col">파일명</th>
                                <th scope="col">파일번호</th>
                                 <th scope="col">현재 등급</th>
                            </tr>
                            </thead>
                            <tbody id="fileTableBody">
                            <tr class="sa-empty-row"><td colspan="5">파일을 조회해 주세요.</td></tr>
                            </tbody>
                        </table>
                    </div>
                </article>

                <article class="sa-card">
                    <div class="sa-card-header">
                        <div>
                            <h2>파일 보안등급 지정</h2>
                            <p>파일번호를 생략하면 해당 자료의 모든 파일에 적용합니다.</p>
                        </div>
                    </div>
                    <div id="selectedFileSummary" class="sa-selection-summary is-empty">
                        왼쪽 목록에서 파일을 선택하세요.
                    </div>
                    <form id="fileLabelForm" class="sa-form" autocomplete="off">
                        <div class="sa-form-grid">
                            <label class="sa-field">
                                <span>적용 등급 <em>*</em></span>
                                <select id="fileGradeCd" required disabled>
                                    <option value="">등급 선택</option>
                                </select>
                            </label>
                            <label class="sa-field">
                                <span>파일번호</span>
                                <input type="text" id="fileNo" maxlength="30" value="*" disabled>
                            </label>
                            <label class="sa-field sa-field--full">
                                <span>지정·변경 사유 <em>*</em></span>
                                <textarea id="labelReason" maxlength="500" rows="4" disabled
                                          placeholder="보안등급 지정 또는 변경 사유를 입력하세요."></textarea>
                            </label>
                        </div>
                        <p class="sa-help"><code>*</code>는 주파일·변환 PDF·보조파일의 기본등급입니다. 보조파일에 더 높은 개별등급이 있으면 높은 등급을 우선 적용합니다.</p>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary" id="fileLabelSaveButton" disabled>파일등급 저장</button>
                        </div>
                    </form>
                </article>

                <article class="sa-card sa-card--table sa-card--full is-disabled"
                         id="filePermissionCard" aria-labelledby="filePermissionTitle" aria-disabled="true">
                    <div class="sa-card-header">
                        <div>
                            <h2 id="filePermissionTitle">문서별 사용자 권한</h2>
                            <p>선택 파일이 속한 문서에 열람·원본 다운로드·출력 사용자를 지정합니다.</p>
                        </div>
                    </div>
                    <div id="filePermissionSummary" class="sa-selection-summary is-empty">
                        위 목록에서 파일을 선택하세요.
                    </div>
                    <div class="sa-table-wrap sa-file-permission-table-wrap">
                        <table class="sa-table sa-file-permission-table" aria-label="문서별 사용자 권한">
                            <thead>
                            <tr>
                                <th scope="col">계정</th>
                                <th scope="col">이름</th>
                                <th scope="col">부서</th>
                                <th scope="col">인가등급</th>
                                <th scope="col">등급충족</th>
                                <th scope="col">열람</th>
                                <th scope="col">다운로드</th>
                                <th scope="col">출력</th>
                                <th scope="col">전체 회수</th>
                            </tr>
                            </thead>
                            <tbody id="filePermissionTableBody">
                            <tr class="sa-empty-row"><td colspan="9">파일을 선택하면 사용자 권한을 조회합니다.</td></tr>
                            </tbody>
                        </table>
                    </div>
                    <form id="filePermissionForm" class="sa-form sa-file-permission-form" autocomplete="off">
                        <label class="sa-field">
                            <span>권한 변경 사유 <em>*</em></span>
                            <textarea id="filePermissionChangeReason" maxlength="500" rows="3" disabled
                                      placeholder="사용자별 권한 부여 또는 변경 사유를 입력하세요."></textarea>
                        </label>
                        <p class="sa-help">권한은 문서의 모든 파일에 적용되며, 각 파일의 보안등급과 사용자 전역권한도 함께 충족해야 합니다.</p>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary"
                                    id="filePermissionSaveButton" disabled>사용자 권한 저장</button>
                        </div>
                    </form>
                </article>
            </div>
        </section>

    </section>
</main>
</body>
</html>
