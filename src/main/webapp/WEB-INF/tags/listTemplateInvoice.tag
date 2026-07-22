<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="gridId" required="true" rtexprvalue="true" description=""%>
<%@ attribute name="pager" required="false" rtexprvalue="true" description="pager usage true/false"%>
<%@ attribute name="treeId" required="false" rtexprvalue="true" description="optional side tree id"%>
<%@ attribute name="treeTitle" required="false" rtexprvalue="true" description="optional side tree title"%>

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
            <div id="${treeId}"></div>
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
