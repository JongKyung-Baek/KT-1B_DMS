<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="gridId" required="true" rtexprvalue="true" description=""%>
<%@ attribute name="pager" required="false" rtexprvalue="true" description="pager usage true/false"%>
<%@ attribute name="treeId" required="false" rtexprvalue="true" description="optional side tree id"%>
<%@ attribute name="treeTitle" required="false" rtexprvalue="true" description="optional side tree title"%>
<%@ attribute name="treeDescription" required="false" rtexprvalue="true" description="optional enhanced tree description"%>
<%@ attribute name="treeSearchPlaceholder" required="false" rtexprvalue="true" description="optional enhanced tree search placeholder"%>
<%@ attribute name="treeAllLabel" required="false" rtexprvalue="true" description="optional enhanced tree all-items label"%>

<c:if test="${null == pager || '' == pager }">
  <c:set var="pagerYn" value="true"></c:set>
</c:if>

<c:set var="pagerId" value="${gridId }Pager"></c:set>

<div class="distribution-invoice-layout">
  <div class="card distribution-filter-card mb-4">
    <div class="card-body">
      <div class="sbr"></div>
    </div>
  </div>

  <div class="btnArea"></div>

  <div class="distribution-content-row<c:if test='${not empty treeId}'> has-side-tree</c:if>">
    <c:if test="${not empty treeId}">
      <div class="card distribution-tree-card">
        <div class="card-body">
          <div class="distribution-tree-body">
            <c:choose>
              <c:when test="${not empty treeSearchPlaceholder}">
                <div class="technical-tree-header">
                  <span class="technical-tree-icon" aria-hidden="true">
                    <i class="icon-base ti tabler-folders"></i>
                  </span>
                  <span class="technical-tree-heading">
                    <strong><c:out value="${treeTitle}" /></strong>
                    <span><c:out value="${treeDescription}" /></span>
                  </span>
                  <span id="${treeId}Total" class="technical-tree-total" aria-live="polite">0</span>
                </div>

                <div class="technical-tree-search">
                  <i class="icon-base ti tabler-search" aria-hidden="true"></i>
                  <input id="${treeId}Search"
                         class="technical-tree-search-input"
                         type="search"
                         autocomplete="off"
                         placeholder="${treeSearchPlaceholder}"
                         aria-label="${treeSearchPlaceholder}" />
                  <button id="${treeId}SearchClear"
                          class="technical-tree-search-clear"
                          type="button"
                          aria-label="${treeSearchPlaceholder}">
                    <i class="icon-base ti tabler-x" aria-hidden="true"></i>
                  </button>
                </div>

                <button id="${treeId}All" class="technical-tree-all is-active" type="button">
                  <span class="technical-tree-all-icon" aria-hidden="true">
                    <i class="icon-base ti tabler-files"></i>
                  </span>
                  <span class="technical-tree-all-label"><c:out value="${treeAllLabel}" /></span>
                  <span id="${treeId}AllCount" class="technical-tree-all-count">0</span>
                </button>

                <div class="technical-tree-scroll">
                  <div id="${treeId}"></div>
                  <div id="${treeId}NoResults"
                       class="technical-tree-no-results"
                       role="status"
                       aria-live="polite"
                       hidden></div>
                </div>
              </c:when>
              <c:otherwise>
                <div id="${treeId}"></div>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
    </c:if>

    <div class="card distribution-grid-card">
      <div class="card-datatable table-responsive">
        <div class="gridArea whole">
          <div class="gridContainer">
            <table id="${gridId }"></table>
            <c:if test="${pagerYn == 'true' }">
              <div id="${pagerId }"></div>
            </c:if>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
