<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="/resources/js/views/outside/outregisted/request/registerPopup.js"></script>
<!-- 등록 및 배포요청 팝업(자료등록 버튼) -->
<script>
var emptyArray2 = [];
var bUploadCheck = false;

$(document).ready(function() {
	$("#uploadActiveX")[0].InitializationUpload();
//	console.log("----------")
});


var curSendIndex = 0

function isValidationX(){


	if($.trim($("#dataName").val()) === ""){
		//isValidDataEmpty("dataName", "form.dataName");
		alert('자료명을 입력하세요');
		return false;
	}

	if(emptyArray2.length <= 0){
		alert(g_msg('msg.noFile'));	 //첨부된 파일이 없습니다.
		return false;
	}

	return true;
}


function saveX() {
	if(!isValidationX()){
		return;
	}

	for(var i=0; i<emptyArray2.length; i++) {
		emptyArray2[i].dataName  = $('#dataName').val();
	}

	curSendIndex = 0
	callAjax( { paramList : emptyArray2 }, "/outside/outregisted/request/saveUnregisterFileX", requestCrXCallback);
}

function requestCrXCallback(response){
	if(response.success){
//		if (curSendIndex < emptyArray2.length)
//			callAjax(emptyArray2[curSendIndex++], "/inside/unregisted/request/saveUnregisterFileX", requestCrXCallback);
//		else
		alertMessage(g_msg('msg.registerComplete'))
		searchList(gridParam);
		closePopup('popupDialog');
		$(this).dialog("close");
	}else{
		alertMessage(g_msg("msg.registerFailure"));						//등록이 실패했습니다.
	}
}

function fnUploadBeforeCheck(){
    bUploadCheck = true;
//    console.log("fnUploadBeforeCheck")

    return true;
}

function fnUploadFail(){
//	console.log("fnUploadFail")
	<%--    alert("<spring:message code='145'/>");파일전송 실패 --%>
}

function fnUploadFinish(){
//	console.log("fnUploadFinish")
    bUploadCheck = false;
}


function fnUploadSuccess(){
    var nFileCount   = arguments[0];
    var nFileIndex   = arguments[1];
    var strFileId    = arguments[2];
    var strFileName  = arguments[3];
    var strFilePath  = arguments[4];
    var nFileSize    = arguments[5];  // byte
    var strErrorCode = arguments[6];
    var strErrorMsg  = arguments[7];
    var strMFileCode = arguments[8];
    var strMFileCode1 = arguments[9];

    var fileLen = strFileName.length;
    var lastDot = strFileName.lastIndexOf('.');
    var ext = strFileName.substring(lastDot, fileLen);

    //console.log("strMFileCode1 : " + strMFileCode1);

    var strParam = "";
    strParam += "파일갯수   : " + nFileCount   + "\n";
    strParam += "파일인덱스 : " + nFileIndex   + "\n";
    strParam += "파일아이디 : " + strFileId    + "\n";
    strParam += "파일명     : " + strFileName  + "\n";
    strParam += "파일경로   : " + strFilePath  + "\n";
    strParam += "파일크기   : " + nFileSize    + "\n";
    strParam += "에러코드   : " + strErrorCode + "\n";
    strParam += "에러메시지 : " + strErrorMsg  + "\n";
    strParam += "마스터코드 : " + strMFileCode + "\n";
    strParam += "마스터코드 : " + strMFileCode1 + "\n";
    strParam += "확장자 : " + ext + "\n";

	try { console.log("fnUploadSuccess", strParam); } catch(e) { }

	var param = new Object();
	param.fileNm = strFileName;
	param.filePath = strFilePath+'/'+strFileId + ext;
	param.fileSize = nFileSize;
//	console.log("param.filePath : ", param.filePath)

	//callAjax(param, "/inside/unregisted/request/saveUnregisterFileX", saveUnregisterFileXCallback);
	emptyArray2.push(param);
}
</script>
<style>
</style>
<div class="dialogContent">
	<form id="formUnRegisterPopup">
		<ul class="section">
			<li>
				<!-- 자료명 -->
				<custom:popupInputText name="dataName" id="dataName" label="form.dataName" value=""/>
			</li>
<!-- 			<li> -->
<!-- 				자료 유형 -->
<%-- 				<custom:popupSelectBox options="${objectType }" name="objectType" label="form.dataType" id="objectType"/> --%>
<!-- 			</li> -->
		</ul>
	</form>
	<div class="section">
		<!--
		<li class="singleFileUpload">
			<%--  파일추가 --%>
			<label><spring:message code="form.fileUpload"/></label>
			<div>
				<form id="fileForm" name="fileForm" enctype="multipart/form-data">
					<input type="file" id="unregFile" name="unregFile" onchange="fileChange()" style="display: none;"/>
				</form>
				<input type="text" name="fileName" id="fileName"/>
				<button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"><spring:message code="btn.fileUpload"/></button>
			</div>
		</li>
		 -->
		<OBJECT ID="uploadActiveX" CODEBASE="${updownCabUrl }" CLASSID="CLSID:2D77F2E4-1C6F-475C-B83E-29EFEF9D1659" width="670" height="320" style="margin: auto;"> 
<%--            <OBJECT ID="uploadActiveX" CODEBASE="https://localhost:8080/resources/install/ExtUpDown.cab#version=1,2015,527,1" CLASSID="CLSID:2D77F2E4-1C6F-475C-B83E-29EFEF9D1659" width="680" height="320" style="">--%>
                <%-- <PARAM NAME="uploadServerIp"       VALUE="${updownCabUrl }"> --%>
                <PARAM NAME="uploadServerIp"       VALUE="${updownServerIp }">
                <PARAM NAME="uploadServerPort"     VALUE="${updownServerPort }">
                <%--<PARAM NAME="uploadServerPath"     VALUE="${updownPath }"> --%>
                <PARAM NAME="uploadServerPath"     VALUE="D:/FDMS/OUTREG">
                <PARAM NAME="uploadSecretKey"      VALUE="${updownSecretKey }">
                <PARAM NAME="uploadLangCode"       VALUE="${updownLangCode }">
                <PARAM NAME="uploadIsSecurity"     VALUE="${updownIsSecurity }">
                <PARAM NAME="userUploadSize"       VALUE="2050846883.84">
                <PARAM NAME="userUploadUseSize"    VALUE="0">
                <PARAM NAME="userUploadRemainSize" VALUE="2050846883.84">
                <PARAM NAME="useExtension"         VALUE="${updownExtension }">
                <PARAM NAME="revision"         	   VALUE="FALSE">
            </OBJECT>
	</div>

</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<!-- 등록 -->
		<custom:popupButton function="saveX()" name="save" label="btn.register" id="save"/>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>