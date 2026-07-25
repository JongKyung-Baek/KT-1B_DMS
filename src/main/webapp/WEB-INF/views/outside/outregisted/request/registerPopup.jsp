<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!-- 등록 및 배포요청 팝업(자료등록 버튼) -->
<script>
var emptyArray2 = [];
var bUploadCheck = false;

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
	alertMessage('외부 업로드 연계 기능은 현재 비활성 상태입니다.');
	return false;
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
	.upload-integration-disabled {
		box-sizing: border-box;
		width: 100%;
		padding: 24px;
		border: 1px solid #d8dee8;
		border-radius: 4px;
		background: #f7f8fa;
		color: #4b5563;
		text-align: center;
	}
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
		<div class="upload-integration-disabled" role="status">
			외부 파일 업로드 연계는 후속 개발 단계에서 제공됩니다. 현재는 사용할 수 없습니다.
		</div>
	</div>

</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<!-- 등록 -->
		<button type="button" class="ui-button ui-corner-all bottomBtn" name="save" id="save"
				disabled="disabled" aria-disabled="true" title="외부 업로드 연계 준비 중">
			<spring:message code="btn.register"/>
		</button>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
