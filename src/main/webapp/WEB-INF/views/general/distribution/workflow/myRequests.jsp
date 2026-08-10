<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<spring:message code="feature.distributionWorkflow.page.mine.title" text="내 배포요청" var="pageTitle"/>
<!doctype html>
<html lang="${pageLocale}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle} - ${tdmsBrand.systemName}</title>
<link type="text/css" rel="stylesheet"
      href="${pageContext.request.contextPath}/resources/css/pages/distribution-workflow.css?v=20260802.2" media="screen">
<script>
window.distributionWorkflowPage = {
    contextPath: '${pageContext.request.contextPath}',
    mode: 'mine',
    openCreateOnLoad: ${workflowOpenCreate or not empty initialItems}
};
</script>
</head>
<body>
<main class="distribution-workflow-page" data-workflow-mode="mine" aria-labelledby="workflowPageTitle">
    <section class="dw-card">
        <header class="dw-card__header">
            <div>
                <span class="dw-section-kicker" data-i18n-key="feature.distributionWorkflow.page.mine.kicker">
                    <spring:message code="feature.distributionWorkflow.page.mine.kicker" text="MY DISTRIBUTION REQUESTS"/>
                </span>
                <h2 id="workflowPageTitle"><c:out value="${pageTitle}"/></h2>
                <p data-i18n-key="feature.distributionWorkflow.page.mine.description">
                    <spring:message code="feature.distributionWorkflow.page.mine.description"
                                    text="기술자료 배포요청을 작성하고 승인 진행상태를 확인합니다."/>
                </p>
            </div>
            <div class="dw-card__header-actions">
                <span class="dw-count" id="workflowCount">0<spring:message code="feature.common.countSuffix" text="건"/></span>
                <button type="button" class="dw-button dw-button--primary" id="workflowCreateButton">
                    <i class="icon-base ti tabler-plus" aria-hidden="true"></i>
                    <span data-i18n-key="feature.distributionWorkflow.action.createRequest">
                        <spring:message code="feature.distributionWorkflow.action.createRequest" text="배포요청 작성"/>
                    </span>
                </button>
            </div>
        </header>

        <form class="dw-search" id="workflowSearchForm" role="search" autocomplete="off">
            <label>
                <span data-i18n-key="feature.distributionWorkflow.filter.status">
                    <spring:message code="feature.distributionWorkflow.filter.status" text="진행상태"/>
                </span>
                <select id="workflowStatusFilter">
                    <option value=""><spring:message code="feature.distributionWorkflow.filter.allStatuses" text="전체 상태"/></option>
                    <option value="DRAFT"><spring:message code="feature.distributionWorkflow.status.DRAFT" text="작성중"/></option>
                    <option value="PENDING_APPROVAL"><spring:message code="feature.distributionWorkflow.status.PENDING_APPROVAL" text="승인대기"/></option>
                    <option value="APPROVED"><spring:message code="feature.distributionWorkflow.status.APPROVED" text="승인완료"/></option>
                    <option value="REJECTED"><spring:message code="feature.distributionWorkflow.status.REJECTED" text="반려"/></option>
                    <option value="CANCELLED"><spring:message code="feature.distributionWorkflow.status.CANCELLED" text="취소"/></option>
                    <option value="EXPIRED"><spring:message code="feature.distributionWorkflow.status.EXPIRED" text="배포만료"/></option>
                </select>
            </label>
            <label class="dw-search__keyword">
                <span data-i18n-key="feature.distributionWorkflow.filter.keyword">
                    <spring:message code="feature.distributionWorkflow.filter.keyword" text="통합검색"/>
                </span>
                <input type="search" id="workflowKeyword" maxlength="100"
                       placeholder="<spring:message code='feature.distributionWorkflow.filter.keywordPlaceholder' text='요청번호, 제목, 요청자 검색'/>">
            </label>
            <div class="dw-search__actions">
                <button type="button" class="dw-button dw-button--ghost" id="workflowResetButton">
                    <i class="icon-base ti tabler-refresh" aria-hidden="true"></i>
                    <spring:message code="feature.common.reset" text="초기화"/>
                </button>
                <button type="submit" class="dw-button dw-button--primary">
                    <i class="icon-base ti tabler-search" aria-hidden="true"></i>
                    <spring:message code="feature.common.search" text="조회"/>
                </button>
            </div>
        </form>

        <div class="dw-alert" id="workflowPageMessage" role="status" aria-live="polite" hidden></div>
        <div class="dw-table-wrap">
            <table class="dw-table dw-request-table" aria-label="${pageTitle}">
                <thead>
                <tr>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.requestNo" text="요청번호"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.title" text="요청 제목"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.distributionTarget" text="배포대상"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.status" text="진행상태"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.itemCount" text="파일건수"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.distributionPeriod" text="배포기간"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.assignedApprover" text="지정 승인자"/></th>
                    <th scope="col"><spring:message code="feature.distributionWorkflow.column.actions" text="관리"/></th>
                </tr>
                </thead>
                <tbody id="workflowTableBody"></tbody>
            </table>
        </div>
        <footer class="dw-table-footer">
            <span><i class="icon-base ti tabler-info-circle" aria-hidden="true"></i>
                <spring:message code="feature.distributionWorkflow.footer.limit" text="최근 요청을 최대 100건까지 조회합니다."/>
            </span>
        </footer>
    </section>

    <div id="workflowInitialItems" hidden>
        <c:forEach items="${initialItems}" var="item">
            <span class="dw-initial-item"
                  data-object-type="<c:out value='${item.objectType}'/>"
                  data-object-id="<c:out value='${item.objectId}'/>"
                  data-file-no="<c:out value='${item.fileNo}'/>"
                  data-material-no="<c:out value='${item.materialNo}'/>"
                  data-material-name="<c:out value='${item.materialName}'/>"
                  data-file-name="<c:out value='${item.originalFileName}'/>"
                  data-file-size="<c:out value='${item.fileSize}'/>"
                  data-grade-cd="<c:out value='${item.gradeCd}'/>"
                  data-grade-nm="<c:out value='${item.gradeNm}'/>"
                  data-tree-cd="<c:out value='${item.treeCd}'/>"
                  data-tree-nm="<c:out value='${item.treeNm}'/>"
                  data-parent-tree-cd="<c:out value='${item.parentTreeCd}'/>"
                  data-parent-tree-nm="<c:out value='${item.parentTreeNm}'/>"></span>
        </c:forEach>
    </div>
    <div id="workflowCategoryOptions" hidden>
        <c:forEach items="${workflowCategoryParents}" var="category">
            <span class="dw-category-parent-option"
                  data-value="<c:out value='${category.comboVal}'/>"
                  data-label="<c:out value='${category.comboLabel}'/>"></span>
        </c:forEach>
        <c:forEach items="${workflowCategoryChildren}" var="category">
            <span class="dw-category-child-option"
                  data-value="<c:out value='${category.comboVal}'/>"
                  data-label="<c:out value='${category.comboLabel}'/>"
                  data-parent="<c:out value='${category.comboTooltip}'/>"></span>
        </c:forEach>
    </div>
    <%@ include file="workflowDialog.jspf" %>
</main>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/views/general/distribution/workflow/distribution-workflow.js?v=20260801.4"></script>
</body>
</html>
