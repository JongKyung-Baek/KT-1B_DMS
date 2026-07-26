<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!doctype html>
<html lang="<spring:message code='feature.securityAccess.page.language'/>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><spring:message code="feature.securityAccess.page.title"/></title>
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
            src="${pageContext.request.contextPath}/resources/js/inside/system/securityaccess/securityAccess.js?v=20260726.5"></script>
</head>
<body>
<main class="security-access-page" aria-labelledby="securityAccessTitle">
    <section class="sa-shell">
        <header class="sa-page-header">
            <div>
                <p class="sa-eyebrow"><spring:message code="feature.securityAccess.page.eyebrow"/></p>
                <h1 id="securityAccessTitle"><spring:message code="feature.securityAccess.page.title"/></h1>
                <p class="sa-page-description"><spring:message code="feature.securityAccess.page.description"/></p>
            </div>
            <div class="sa-policy-note" aria-label="<spring:message code='feature.securityAccess.policy.ariaLabel'/>">
                <span class="sa-policy-note__label"><spring:message code="feature.securityAccess.policy.label"/></span>
                <strong><spring:message code="feature.securityAccess.policy.rule"/></strong>
            </div>
        </header>

        <div id="securityAccessMessage" class="sa-message" role="status" aria-live="polite" hidden></div>

        <nav class="sa-tabs" id="securityAccessTabs" role="tablist"
             aria-label="<spring:message code='feature.securityAccess.tabs.ariaLabel'/>">
            <button type="button" class="sa-tab is-active" id="gradeTab"
                    role="tab" aria-selected="true" aria-controls="gradePanel" data-tab-target="gradePanel">
                <span class="sa-tab__step">1</span><spring:message code="feature.securityAccess.tabs.grade"/>
            </button>
            <button type="button" class="sa-tab" id="userTab"
                    role="tab" aria-selected="false" aria-controls="userPanel" data-tab-target="userPanel">
                <span class="sa-tab__step">2</span><spring:message code="feature.securityAccess.tabs.user"/>
            </button>
            <button type="button" class="sa-tab" id="fileTab"
                    role="tab" aria-selected="false" aria-controls="filePanel" data-tab-target="filePanel">
                <span class="sa-tab__step">3</span><spring:message code="feature.securityAccess.tabs.file"/>
            </button>
        </nav>

        <section id="gradePanel" class="sa-tab-panel is-active" role="tabpanel" aria-labelledby="gradeTab">
            <div class="sa-layout sa-layout--form-list sa-layout--balanced">
                <article class="sa-card">
                    <div class="sa-card-header sa-card-header--stack-mobile">
                        <div>
                            <h2><spring:message code="feature.securityAccess.grade.info.title"/></h2>
                            <p><spring:message code="feature.securityAccess.grade.info.description"/></p>
                        </div>
                        <button type="button" class="sa-button sa-button--ghost" id="gradeNewButton"><spring:message code="feature.securityAccess.grade.new"/></button>
                    </div>
                    <form id="gradeForm" class="sa-form" autocomplete="off">
                        <input type="hidden" id="gradeOriginalCd" value="">
                        <div class="sa-form-grid sa-form-grid--grade">
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.grade.code"/> <em>*</em></span>
                                <input type="text" id="gradeCd" maxlength="30"
                                       placeholder="<spring:message code='feature.securityAccess.grade.code.placeholder'/>" required>
                            </label>
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.grade.name"/> <em>*</em></span>
                                <input type="text" id="gradeNm" maxlength="80"
                                       placeholder="<spring:message code='feature.securityAccess.grade.name.placeholder'/>" required>
                            </label>
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.grade.level"/> <em>*</em></span>
                                <input type="number" id="gradeLevel" min="0" max="9999" step="1"
                                       placeholder="<spring:message code='feature.securityAccess.grade.level.placeholder'/>" required>
                            </label>
                            <label class="sa-field sa-field--full">
                                <span><spring:message code="feature.securityAccess.grade.description"/></span>
                                <textarea id="gradeDescription" maxlength="500" rows="4"
                                          placeholder="<spring:message code='feature.securityAccess.grade.description.placeholder'/>"></textarea>
                            </label>
                        </div>
                        <div class="sa-check-row">
                            <label class="sa-switch">
                                <input type="checkbox" id="gradeUseYn" checked>
                                <span><spring:message code="feature.securityAccess.common.active"/></span>
                            </label>
                            <label class="sa-switch">
                                <input type="checkbox" id="gradeDefaultYn">
                                <span><spring:message code="feature.securityAccess.grade.defaultNew"/></span>
                            </label>
                        </div>
                        <p class="sa-help"><spring:message code="feature.securityAccess.grade.defaultHelp"/></p>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary" id="gradeSaveButton"><spring:message code="feature.securityAccess.grade.save"/></button>
                        </div>
                    </form>
                </article>

                <article class="sa-card sa-card--table">
                    <div class="sa-card-header sa-card-header--stack-mobile">
                        <div>
                            <h2><spring:message code="feature.securityAccess.grade.list.title"/></h2>
                            <p><spring:message code="feature.securityAccess.grade.list.description"/></p>
                        </div>
                        <button type="button" class="sa-button sa-button--ghost" id="gradeReloadButton"><spring:message code="feature.securityAccess.common.refresh"/></button>
                    </div>
                    <div class="sa-table-wrap">
                        <table class="sa-table" aria-label="<spring:message code='feature.securityAccess.grade.list.ariaLabel'/>">
                            <thead>
                            <tr>
                                <th scope="col"><spring:message code="feature.securityAccess.grade.table.level"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.grade.table.code"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.grade.table.name"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.grade.table.default"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.grade.table.active"/></th>
                            </tr>
                            </thead>
                            <tbody id="gradeTableBody">
                            <tr class="sa-empty-row"><td colspan="5"><spring:message code="feature.securityAccess.grade.loading"/></td></tr>
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
                            <h2><spring:message code="feature.securityAccess.user.select.title"/></h2>
                            <p><spring:message code="feature.securityAccess.user.select.description"/></p>
                        </div>
                    </div>
                    <div class="sa-list-toolbar">
                        <form id="userSearchForm" class="sa-search" role="search">
                            <label class="sa-sr-only" for="userKeyword"><spring:message code="feature.securityAccess.user.search.label"/></label>
                            <input type="search" id="userKeyword" maxlength="100"
                                   placeholder="<spring:message code='feature.securityAccess.user.search.placeholder'/>">
                            <button type="submit" class="sa-button sa-button--secondary" id="userSearchButton"><spring:message code="feature.securityAccess.common.search"/></button>
                        </form>
                    </div>
                    <div class="sa-table-wrap sa-table-wrap--tall">
                        <table class="sa-table" aria-label="<spring:message code='feature.securityAccess.user.list.ariaLabel'/>">
                            <thead>
                            <tr>
                                <th scope="col"><spring:message code="feature.securityAccess.common.account"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.common.name"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.common.department"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.user.table.currentGrade"/></th>
                            </tr>
                            </thead>
                            <tbody id="userTableBody">
                            <tr class="sa-empty-row"><td colspan="4"><spring:message code="feature.securityAccess.user.initial"/></td></tr>
                            </tbody>
                        </table>
                    </div>
                </article>

                <article class="sa-card">
                    <div class="sa-card-header">
                        <div>
                            <h2><spring:message code="feature.securityAccess.user.clearance.title"/></h2>
                            <p><spring:message code="feature.securityAccess.user.clearance.description"/></p>
                        </div>
                    </div>
                    <div id="selectedUserSummary" class="sa-selection-summary is-empty">
                        <spring:message code="feature.securityAccess.user.selectPrompt"/>
                    </div>
                    <form id="clearanceForm" class="sa-form" autocomplete="off">
                        <label class="sa-field">
                            <span><spring:message code="feature.securityAccess.user.maxGrade"/> <em>*</em></span>
                            <select id="userGradeCd" required disabled>
                                <option value=""><spring:message code="feature.securityAccess.grade.select"/></option>
                            </select>
                        </label>

                        <fieldset class="sa-permission-fieldset" disabled>
                            <legend><spring:message code="feature.securityAccess.user.actionPermissions"/> <em>*</em></legend>
                            <div class="sa-permission-grid">
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="LIST"><span><strong>LIST</strong><small><spring:message code="feature.securityAccess.user.permission.list"/></small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="DETAIL"><span><strong>DETAIL</strong><small><spring:message code="feature.securityAccess.user.permission.detail"/></small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="VIEW"><span><strong>VIEW</strong><small><spring:message code="feature.securityAccess.user.permission.view"/></small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="DOWNLOAD_ORIGINAL"><span><strong>DOWNLOAD ORIGINAL</strong><small><spring:message code="feature.securityAccess.user.permission.downloadOriginal"/></small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="PRINT"><span><strong>PRINT</strong><small><spring:message code="feature.securityAccess.user.permission.print"/></small></span></label>
                                <label class="sa-permission"><input type="checkbox" name="accessPermission" value="MANAGE_ACL"><span><strong>MANAGE ACL</strong><small><spring:message code="feature.securityAccess.user.permission.manageAcl"/></small></span></label>
                            </div>
                        </fieldset>

                        <div class="sa-form-grid">
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.user.validFrom"/></span>
                                <input type="date" id="clearanceValidFrom" disabled>
                            </label>
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.user.validTo"/></span>
                                <input type="date" id="clearanceValidTo" disabled>
                            </label>
                            <label class="sa-field sa-field--full">
                                <span><spring:message code="feature.securityAccess.user.reason"/> <em>*</em></span>
                                <textarea id="clearanceGrantReason" maxlength="500" rows="3" disabled
                                          placeholder="<spring:message code='feature.securityAccess.user.reason.placeholder'/>"></textarea>
                            </label>
                        </div>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary" id="clearanceSaveButton" disabled><spring:message code="feature.securityAccess.user.save"/></button>
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
                            <h2><spring:message code="feature.securityAccess.file.select.title"/></h2>
                            <p><spring:message code="feature.securityAccess.file.select.description"/></p>
                        </div>
                        <form id="fileSearchForm" class="sa-search sa-search--wide" role="search">
                             <label class="sa-sr-only" for="fileObjectType"><spring:message code="feature.securityAccess.file.objectType.label"/></label>
                             <select id="fileObjectType">
                                <option value="SW"><spring:message code="feature.securityAccess.objectType.sw"/></option>
                                <option value="SW_SUB"><spring:message code="feature.securityAccess.objectType.swSub"/></option>
                                <option value="DOCUMENT"><spring:message code="feature.securityAccess.objectType.document"/></option>
                                 <option value="DRAWING"><spring:message code="feature.securityAccess.objectType.drawing"/></option>
                                <option value="PRODUCT_DOCUMENT"><spring:message code="feature.securityAccess.objectType.productDocument"/></option>
                                <option value="PRODUCT_SW"><spring:message code="feature.securityAccess.objectType.productSw"/></option>
                                 <option value="DXF"><spring:message code="feature.securityAccess.objectType.dxf"/></option>
                                <option value="PEER_REVIEW"><spring:message code="feature.securityAccess.objectType.peerReview"/></option>
                                <option value="DOCUMENT_SUB"><spring:message code="feature.securityAccess.objectType.documentSub"/></option>
                                <option value="DRAWING_SUB"><spring:message code="feature.securityAccess.objectType.drawingSub"/></option>
                                <option value="PRODUCT_DOCUMENT_SUB"><spring:message code="feature.securityAccess.objectType.productDocumentSub"/></option>
                                <option value="PRODUCT_SW_SUB"><spring:message code="feature.securityAccess.objectType.productSwSub"/></option>
                                <option value="DXF_SUB"><spring:message code="feature.securityAccess.objectType.dxfSub"/></option>
                             </select>
                            <label class="sa-sr-only" for="fileKeyword"><spring:message code="feature.securityAccess.file.search.label"/></label>
                            <input type="search" id="fileKeyword" maxlength="100"
                                   placeholder="<spring:message code='feature.securityAccess.file.search.placeholder'/>">
                            <button type="submit" class="sa-button sa-button--secondary" id="fileSearchButton"><spring:message code="feature.securityAccess.common.search"/></button>
                        </form>
                    </div>
                    <div class="sa-table-wrap sa-table-wrap--tall">
                        <table class="sa-table" aria-label="<spring:message code='feature.securityAccess.file.list.ariaLabel'/>">
                            <thead>
                            <tr>
                                 <th scope="col"><spring:message code="feature.securityAccess.file.table.objectType"/></th>
                                 <th scope="col"><spring:message code="feature.securityAccess.file.table.documentNo"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.table.fileName"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.table.fileNo"/></th>
                                 <th scope="col"><spring:message code="feature.securityAccess.file.table.currentGrade"/></th>
                            </tr>
                            </thead>
                            <tbody id="fileTableBody">
                            <tr class="sa-empty-row"><td colspan="5"><spring:message code="feature.securityAccess.file.initial"/></td></tr>
                            </tbody>
                        </table>
                    </div>
                </article>

                <article class="sa-card">
                    <div class="sa-card-header">
                        <div>
                            <h2><spring:message code="feature.securityAccess.file.label.title"/></h2>
                            <p><spring:message code="feature.securityAccess.file.label.description"/></p>
                        </div>
                    </div>
                    <div id="selectedFileSummary" class="sa-selection-summary is-empty">
                        <spring:message code="feature.securityAccess.file.selectPrompt"/>
                    </div>
                    <form id="fileLabelForm" class="sa-form" autocomplete="off">
                        <div class="sa-form-grid">
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.file.label.grade"/> <em>*</em></span>
                                <select id="fileGradeCd" required disabled>
                                    <option value=""><spring:message code="feature.securityAccess.grade.select"/></option>
                                </select>
                            </label>
                            <label class="sa-field">
                                <span><spring:message code="feature.securityAccess.file.table.fileNo"/></span>
                                <input type="text" id="fileNo" maxlength="30" value="*" disabled>
                            </label>
                            <label class="sa-field sa-field--full">
                                <span><spring:message code="feature.securityAccess.file.label.reason"/> <em>*</em></span>
                                <textarea id="labelReason" maxlength="500" rows="4" disabled
                                          placeholder="<spring:message code='feature.securityAccess.file.label.reason.placeholder'/>"></textarea>
                            </label>
                        </div>
                        <p class="sa-help"><code>*</code> <spring:message code="feature.securityAccess.file.label.help"/></p>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary" id="fileLabelSaveButton" disabled><spring:message code="feature.securityAccess.file.label.save"/></button>
                        </div>
                    </form>
                </article>

                <article class="sa-card sa-card--table sa-card--full is-disabled"
                         id="filePermissionCard" aria-labelledby="filePermissionTitle" aria-disabled="true">
                    <div class="sa-card-header">
                        <div>
                            <h2 id="filePermissionTitle"><spring:message code="feature.securityAccess.file.permission.title"/></h2>
                            <p><spring:message code="feature.securityAccess.file.permission.description"/></p>
                        </div>
                    </div>
                    <div id="filePermissionSummary" class="sa-selection-summary is-empty">
                        <spring:message code="feature.securityAccess.file.permission.selectPrompt"/>
                    </div>
                    <div class="sa-table-wrap sa-file-permission-table-wrap">
                        <table class="sa-table sa-file-permission-table"
                               aria-label="<spring:message code='feature.securityAccess.file.permission.ariaLabel'/>">
                            <thead>
                            <tr>
                                <th scope="col"><spring:message code="feature.securityAccess.common.account"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.common.name"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.common.department"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.permission.table.clearanceGrade"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.permission.table.gradeEligibility"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.permission.table.view"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.permission.table.download"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.permission.table.print"/></th>
                                <th scope="col"><spring:message code="feature.securityAccess.file.permission.table.revokeAll"/></th>
                            </tr>
                            </thead>
                            <tbody id="filePermissionTableBody">
                            <tr class="sa-empty-row"><td colspan="9"><spring:message code="feature.securityAccess.file.permission.initial"/></td></tr>
                            </tbody>
                        </table>
                    </div>
                    <form id="filePermissionForm" class="sa-form sa-file-permission-form" autocomplete="off">
                        <label class="sa-field">
                            <span><spring:message code="feature.securityAccess.file.permission.reason"/> <em>*</em></span>
                            <textarea id="filePermissionChangeReason" maxlength="500" rows="3" disabled
                                      placeholder="<spring:message code='feature.securityAccess.file.permission.reason.placeholder'/>"></textarea>
                        </label>
                        <p class="sa-help"><spring:message code="feature.securityAccess.file.permission.help"/></p>
                        <div class="sa-form-actions">
                            <button type="submit" class="sa-button sa-button--primary"
                                    id="filePermissionSaveButton" disabled><spring:message code="feature.securityAccess.file.permission.save"/></button>
                        </div>
                    </form>
                </article>
            </div>
        </section>

    </section>
</main>
</body>
</html>
