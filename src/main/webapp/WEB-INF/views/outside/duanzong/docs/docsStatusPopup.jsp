<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<style>
	.dialogContent ul.section li:last-child label { width: auto !important; }
	.dialogContent ul.section li:last-child div { width: calc(100% - 210px) !important; }
</style>
<script>
var popupGridParam;
var gridAddId = '';
var formAddId = '';

function setPopupGridParam(){
	popupGridParam = {
		gridId : gridAddId,
		formId : formAddId,
		size : 9999,
		page : 1,
		multiSelect : false,
		numbering : false,
		selectRowAction : 'check'
	}

	return popupGridParam;
}

function fileDownload(managementNo) {
	$("form[name=tmpForm]")
	.attr("action","/outside/duanzong/fdms/docsFileDownload?managementNo="+managementNo)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();	
}

</script>
<div class="dialogContent">
	<form id="formStatusDocs" name="formStatusDocs">
		<ul class="section">
			<li>
				<custom:popupInputText name="businessArea" label="form.businessArea" value="${ duanzongDocsInfo.businessAreaCd }" id="businessArea" readOnly="readOnly" />
			</li>
			<li>
				<custom:popupInputText name="vendorNm" label="form.companyNm" value="${ duanzongDocsInfo.vendorNm }" id="insertCompanyNm" readOnly="readOnly" />
			</li>
			<li>
				<custom:popupInputText name="vendorUserNm" label="form.vendorUser" value="${ duanzongDocsInfo.vendorUsernm }" id="insertUserNm" readOnly="readOnly" />
			</li>
			<li>
				<custom:popupInputText name="defensePurNm" label="form.defensePurnm" value="${ duanzongDocsInfo.defensePurNm }" id="defensePurNm" readOnly="readOnly" />
			</li>
		</ul>
		
	</form>
	
	<ul class="section">
		<li class="singleFileUpload">
			<%--  파일추가 --%>
			<label><spring:message code="grid.appendFile"/></label>
			<div class="fileDownload">
				<button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( "${duanzongDocsInfo.managementNo}")' >
				<span>${duanzongDocsInfo.fileNm }</span></button>
			</div>
		</li>
	</ul>
	
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
<form name="tmpForm"></form>