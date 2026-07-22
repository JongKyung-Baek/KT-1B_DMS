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
var files = [];
	
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
function isValidation(){
	if($.trim($("#businessArea").val()) === ""){
		isValidDataEmpty("businessArea", "form.businessArea");
		return false;
	}

	if($.trim($("#defensePurNm").val()) === ""){
		isValidDataEmpty("defensePurNm", "form.defensePurnm");
		return false;
	}

	if($.trim($("#docsFile").val()) === ""){
		isValidDataEmpty("docsFile", "grid.appendFile");
		return false;
	}
	
	return true;
}

function fileUpload(){
	$('#docsFile').click();
}

function fileChange(){
	var fileName = $('#docsFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function saveAddDocs(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('file', $("#docsFile")[0].files[0]);
	param.append('docParam', JSON.stringify($('#formAddDocs').serializeObject()));
	callAjaxUpload(param, "/outside/duanzong/docs/duanzongDocs", addDocsCallback);
}

function addDocsCallback(response) {
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}
</script>
<div class="dialogContent">
	<form id="formAddDocs" name="formAddDocs">
		<ul class="section">
			<li>
				<custom:popupSelectBox name="businessAreaCd" options="${businessAreaCd}" defaultText="form.select" label="form.businessArea" id="businessArea"/>
			</li>
			<li>
				<custom:popupInputText name="vendorNm" label="form.companyNm" value="${ vendor.companyNm }" id="insertCompanyNm" readOnly="readOnly" />
			</li>
			<li>
				<custom:popupInputText name="vendorUserNm" label="form.vendorUser" value="${ vendor.userNm }" id="insertUserNm" readOnly="readOnly" />
			</li>
			<li>
				<custom:popupSelectBox name="defensePurNm" options="${defensePurs}" defaultText="form.select" label="form.defensePurnm" id="defensePurNm"  />
			</li>
		</ul>
	</form>
	<ul class="section">
		<li class="singleFileUpload">
			<%--  파일추가 --%>
			<label><spring:message code="grid.appendFile"/></label>
			<div>
				<form id="fileForm" name="fileForm" enctype="multipart/form-data">
					<input type="file" id="docsFile" name="docsFile" onchange="fileChange()" style="display: none;"/>
				</form>
				<input type="text" name="fileName" id="fileName" readonly="readonly"/>
				<button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"><spring:message code="btn.fileUpload"/></button>
			</div>
			<p class="textCaution"><spring:message code="title.duanzongDocsMsg" /></p>
		</li>
	</ul>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 등록 --%>
		<button class="ui-button ui-corner-all bottomBtn" onclick="saveAddDocs()"><spring:message code="btnAdd"/></button>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>