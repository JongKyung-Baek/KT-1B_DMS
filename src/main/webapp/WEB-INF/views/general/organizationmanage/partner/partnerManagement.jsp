<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.partner.page.title" text="협력업체 관리" var="pageTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - ${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/partner-management.css?v=20260802.2" media="screen">
<script>
window.partnerManagementPage = {
        contextPath: '<spring:escapeBody htmlEscape="false" javaScriptEscape="true">${pageContext.request.contextPath}</spring:escapeBody>'
};
</script>
</head>
<body>
<main class="partner-management-page" aria-labelledby="partnerPageTitle">
    <section class="pm-card">
        <header class="pm-card__header">
            <div>
                <span class="pm-kicker"><spring:message code="feature.partner.page.kicker" text="DISTRIBUTION PARTNERS"/></span>
                <h2 id="partnerPageTitle"><spring:message code="feature.partner.page.title" text="협력업체 관리"/></h2>
                <p><spring:message code="feature.partner.page.description"
                    text="기술자료를 수신할 협력업체와 대표사용자·일반 사용자를 관리합니다."/></p>
            </div>
            <button type="button" class="pm-button pm-button--primary" id="partnerCreateButton">
                <i class="icon-base ti tabler-plus" aria-hidden="true"></i>
                <spring:message code="feature.partner.action.create" text="협력업체 등록"/>
            </button>
        </header>

        <form class="pm-search" id="partnerSearchForm" role="search" autocomplete="off">
            <label>
                <span><spring:message code="feature.partner.filter.keyword" text="통합검색"/></span>
                <input type="search" id="partnerKeyword" maxlength="100"
                    placeholder="<spring:message code='feature.partner.filter.placeholder' text='업체명, 업체코드, 사업자번호, 사용자 검색'/>"/>
            </label>
            <div class="pm-search__actions">
                <button type="button" class="pm-button pm-button--ghost" id="partnerResetButton">
                    <i class="icon-base ti tabler-refresh" aria-hidden="true"></i>
                    <spring:message code="feature.common.reset" text="초기화"/>
                </button>
                <button type="submit" class="pm-button pm-button--primary">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i>
                    <spring:message code="feature.common.search" text="조회"/>
                </button>
            </div>
        </form>

        <div class="pm-alert" id="partnerPageMessage" role="status" aria-live="polite" hidden></div>
        <div class="pm-table-wrap">
            <table class="pm-table" aria-label="${pageTitle}">
                <thead>
                <tr>
                    <th scope="col"><spring:message code="feature.partner.column.code" text="업체코드"/></th>
                    <th scope="col"><spring:message code="feature.partner.column.company" text="협력업체"/></th>
                    <th scope="col"><spring:message code="feature.partner.column.businessNo" text="사업자번호"/></th>
                    <th scope="col"><spring:message code="feature.partner.column.representative" text="대표사용자"/></th>
                    <th scope="col"><spring:message code="feature.partner.column.users" text="사용자 수"/></th>
                    <th scope="col"><spring:message code="feature.partner.column.use" text="사용여부"/></th>
                    <th scope="col"><spring:message code="feature.partner.column.actions" text="관리"/></th>
                </tr>
                </thead>
                <tbody id="partnerTableBody"></tbody>
            </table>
        </div>
        <footer class="pm-table-footer">
            <span id="partnerCount">0<spring:message code="feature.common.countSuffix" text="건"/></span>
            <span><spring:message code="feature.partner.footer.note"
                text="활성 협력업체 사용자만 배포 대상으로 선택할 수 있습니다."/></span>
        </footer>
    </section>

    <div class="pm-dialog" id="partnerDialog" hidden>
        <div class="pm-dialog__backdrop" data-partner-close></div>
        <section class="pm-dialog__panel" role="dialog" aria-modal="true"
                 aria-labelledby="partnerDialogTitle" tabindex="-1">
            <header class="pm-dialog__header">
                <div>
                    <span class="pm-kicker"><spring:message code="feature.partner.dialog.kicker" text="PARTNER PROFILE"/></span>
                    <h2 id="partnerDialogTitle"></h2>
                    <p><spring:message code="feature.partner.dialog.description"
                        text="대표사용자는 반드시 1명 지정해야 하며, 로그인 계정과는 별도로 관리됩니다."/></p>
                </div>
                <button type="button" class="pm-icon-button" data-partner-close
                        aria-label="<spring:message code='feature.partner.action.close' text='닫기'/>">
                    <i class="icon-base ti tabler-x" aria-hidden="true"></i>
                </button>
            </header>
            <div class="pm-dialog__body">
                <div class="pm-alert" id="partnerDialogMessage" role="status" aria-live="polite" hidden></div>
                <form id="partnerForm" autocomplete="off">
                    <div class="pm-form-grid">
                        <label class="pm-field">
                            <span><spring:message code="feature.partner.label.companyName" text="업체명"/> <em>*</em></span>
                            <input type="text" id="partnerCompanyName" maxlength="200" required>
                        </label>
                        <label class="pm-field">
                            <span><spring:message code="feature.partner.label.businessNo" text="사업자번호"/></span>
                            <input type="text" id="partnerBusinessNo" maxlength="30">
                        </label>
                        <label class="pm-field">
                            <span><spring:message code="feature.partner.label.companyEmail" text="대표 이메일"/></span>
                            <input type="email" id="partnerContactEmail" maxlength="254">
                        </label>
                        <label class="pm-field">
                            <span><spring:message code="feature.partner.label.companyPhone" text="대표 전화번호"/></span>
                            <input type="text" id="partnerContactPhone" maxlength="40">
                        </label>
                        <label class="pm-field pm-field--wide">
                            <span><spring:message code="feature.partner.label.address" text="주소"/></span>
                            <input type="text" id="partnerAddress" maxlength="500">
                        </label>
                        <label class="pm-field">
                            <span><spring:message code="feature.partner.label.use" text="사용여부"/></span>
                            <select id="partnerUseYn">
                                <option value="Y"><spring:message code="feature.partner.value.active" text="사용"/></option>
                                <option value="N"><spring:message code="feature.partner.value.inactive" text="미사용"/></option>
                            </select>
                        </label>
                    </div>
                </form>

                <section class="pm-users" aria-labelledby="partnerUsersTitle">
                    <header class="pm-users__header">
                        <div>
                            <h3 id="partnerUsersTitle"><spring:message code="feature.partner.users.title" text="협력업체 사용자"/></h3>
                            <p><spring:message code="feature.partner.users.description"
                                text="배포 수신자를 등록하고 대표사용자 1명을 지정하세요."/></p>
                        </div>
                        <button type="button" class="pm-button pm-button--soft" id="partnerAddUserButton">
                            <i class="icon-base ti tabler-user-plus" aria-hidden="true"></i>
                            <spring:message code="feature.partner.action.addUser" text="사용자 추가"/>
                        </button>
                    </header>
                    <div class="pm-table-wrap pm-user-table-wrap">
                        <table class="pm-table pm-user-table">
                            <thead>
                            <tr>
                                <th scope="col"><spring:message code="feature.partner.column.representativeShort" text="대표"/></th>
                                <th scope="col"><spring:message code="feature.partner.column.userName" text="사용자명"/></th>
                                <th scope="col"><spring:message code="feature.partner.column.email" text="이메일"/></th>
                                <th scope="col"><spring:message code="feature.partner.column.phone" text="전화번호"/></th>
                                <th scope="col"><spring:message code="feature.partner.column.position" text="직책"/></th>
                                <th scope="col"><spring:message code="feature.partner.column.use" text="사용여부"/></th>
                                <th scope="col"><spring:message code="feature.partner.column.actions" text="관리"/></th>
                            </tr>
                            </thead>
                            <tbody id="partnerUserTableBody"></tbody>
                        </table>
                    </div>
                </section>
            </div>
            <footer class="pm-dialog__footer">
                <button type="button" class="pm-button pm-button--danger-ghost" id="partnerDeleteButton" hidden>
                    <i class="icon-base ti tabler-trash" aria-hidden="true"></i>
                    <spring:message code="feature.partner.action.delete" text="업체 삭제"/>
                </button>
                <span></span>
                <button type="button" class="pm-button pm-button--ghost" data-partner-close>
                    <spring:message code="feature.partner.action.close" text="닫기"/>
                </button>
                <button type="button" class="pm-button pm-button--primary" id="partnerSaveButton">
                    <i class="icon-base ti tabler-device-floppy" aria-hidden="true"></i>
                    <spring:message code="feature.partner.action.save" text="저장"/>
                </button>
            </footer>
        </section>
    </div>
</main>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/general/organizationmanage/partner/partner-management.js?v=20260801.1"></script>
</body>
</html>
