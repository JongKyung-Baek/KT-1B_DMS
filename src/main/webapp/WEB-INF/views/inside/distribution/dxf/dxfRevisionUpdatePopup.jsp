<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="/resources/js/views/inside/distribution/drawingRequest/drawingRegisterPopup.js"></script>


<!-- 등록 및 배포요청 팝업(미등록 자료등록 버튼) -->
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
        // var fileInput = document.getElementById("fileInput");
        var fileInput = document.getElementById("drawingRegisFile");
        var file = fileInput.files[0];
        if (file) {
            emptyArray2.push(file);
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

        for (var i = 0; i < emptyArray2.length; i++) {
            if (emptyArray2[i]) {
                emptyArray2[i].dataName = $('#dataName').val();
            }
        }

        curSendIndex = 0

        var dataText = document.getElementById('dataName').value;
        var orgFileNm = document.getElementById('fileName').value;
        var distributionPoints = $("#distributionPoint").val();
        var customerRevision = document.getElementById("customerRevision").value;
        var hiddenObjectId = document.getElementById("hiddenObjectId").value;

        var fileInput = document.getElementById("drawingRegisFile");


        var file = fileInput.files[0];
        var formData = new FormData();

        formData.append("file", file);
        formData.append("fileName", dataText);
        formData.append("orgFileNm", orgFileNm);
        formData.append("objectType", "DXF");
        formData.append("customerRevision", customerRevision);
        formData.append("distributionPoint", distributionPoints.join(","));
        formData.append("objectId", hiddenObjectId);
        formData.append("isNewRevision", "true");

        callAjaxUpload(  formData, "/inside/distribution/dxfRequest/uploadDxfRegisFile", requestCrXCallback);
    }

    function requestCrXCallback(response){
        if(response.success){
//		if (curSendIndex < emptyArray2.length)
//			callAjax(emptyArray2[curSendIndex++], "/inside/unregisted/request/saveUnregisterFileX", requestCrXCallback);
//		else
            alertMessage(g_msg('msg.registerComplete')) // 등록이 완료되었습니다
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


    $(document).ready(function(){
        $('#drawingRegisFile').change(function(){
            var fullFilename = $(this).val().split('\\').pop();
            var lastIndex = fullFilename.lastIndexOf(".");

            // If the filename has an extension, remove it, if not, keep it as is
            var filenameWithoutExtension = lastIndex >= 0 ? fullFilename.substring(0, lastIndex) : fullFilename;

            $('#fileName').val(fullFilename); // Preserve extension

            // Check if the #dataName field is empty
            if ($('#dataName').val() === '') {
                $('#dataName').val(filenameWithoutExtension); // Remove extension
            }
        });
    });


</script>
<style>
</style>
<div class="dialogContent">
    <form id="formDrawingRegisterPopup">
        <ul class="section">

            <li>
                <label>등록자</label><input type="text" value="${registerUser}" readonly>
            </li>
            <li>
                <label>등록일</label><input type="text" value="${date}" readonly>
            </li>

            <%--도번--%>
            <li>
                <label>도번</label><input type="text" value="${drawingNo}" readonly>
            </li>

            <%--자료명--%>
            <li>
                <custom:popupInputText name="dataName" id="dataName" label="form.dataName" value="${dataName}"/>
            </li>

            <%--고객 리비전--%>
            <li>
                <label>고객 리비전</label><input type="text" value="${customerRevision}" readonly>
            </li>

            <%--배포처--%>
            <li>
                <custom:popupMultiSelectBox name="distributionPoint" id="distributionPoint" label="form.distributionPoint" options="${distributionPoint}" selectedValue="${distributionPoint }" />
            </li>

            <%-- hiddenObjectId --%>
            <li style="display:none;">
                <input type="hidden" name="hiddenObjectId" id="hiddenObjectId" value="${hiddenObjectId}"/>
            </li>
        </ul>
    </form>
    <div class="section">
        <li class="singleFileUpload">
            <%--  파일추가 --%>
            <div>
                <form id="fileForm" name="fileForm" enctype="multipart/form-data">
                    <input type="file" id="drawingRegisFile" name="drawingRegisFile" onchange="fileChange()" style="display: none;" />
                </form>
                <input type="text" name="fileName" id="fileName" value="" readonly/><button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"><%-- <spring:message code="btn.fileUpload"/> --%></button>
            </div>
        </li>
    </div>






    <div class="dialogBtnSet">
        <div class="left"></div>
        <div class="right">
            <!-- 등록 -->
            <custom:popupButton function="saveX()" name="save" label="btn.register" id="save"/>
            <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
        </div>
    </div>