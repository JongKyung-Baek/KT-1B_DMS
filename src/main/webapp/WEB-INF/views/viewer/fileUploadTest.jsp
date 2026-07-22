<%@page import="org.apache.tomcat.util.codec.binary.Base64"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script>
$(document).ready(function() {
	<%-- 업로드 컴포넌트 초기화 --%>
	$("#uploadActiveX")[0].InitializationUpload();
});

<%--
  - 파일 업로드 성공 시 처리
  --%>
function fnUploadSuccess(){
	alert("성공");
    
}

function fnUploadFail(){
<%--    alert("<spring:message code='145'/>");파일전송 실패 --%>
}

function fnUploadFinish(){
	alert("전송완료");
}

function fnDownloadSuccess(){
}

function fnDownloadFail(){
}

function fnDownloadFinish(bSuccessAll){
}

function fnUploadBeforeCheck(){
	
	return true;
}

function fnDownloadBeforeCheck(){
	return true;
}


</script>
        <!-- section(grid) -->
     <div class="section clearB">
         <!-- grid_area -->
<!--             <div class="grid_area"> -->
        <div style="border:#6B71BD solid 1px; margin:5px;">
            <OBJECT ID="uploadActiveX" CLASSID="CLSID:2D77F2E4-1C6F-475C-B83E-29EFEF9D1659" CODEBASE="http://192.168.0.225:8080/resources/install/ExtUpDown.cab#version=1,2015,524,2" width="552" height="250" style="margin-left: 5px; margin-right: 5px;">
                <PARAM NAME="uploadServerIp"       VALUE="192.168.0.65">
                <PARAM NAME="uploadServerPort"     VALUE="20000">
                <PARAM NAME="uploadServerPath"     VALUE="D:/External/uploadfiles">
                <PARAM NAME="uploadSecretKey"      VALUE="1234567890">
                <PARAM NAME="uploadLangCode"       VALUE="KO">
                <PARAM NAME="uploadIsSecurity"     VALUE="1">
                <PARAM NAME="userUploadSize"       VALUE="999999999999999">
                <PARAM NAME="userUploadUseSize"    VALUE="0">
                <PARAM NAME="userUploadRemainSize" VALUE="999999999999999">
                <PARAM NAME="useExtension"         VALUE="3dxml;7z;BAK;MHTML;WG;a;alz;bmp;catdrawing;catpart;catproduct;cdd;doc;docx;dsn;dwg;dxf;egg;egg;gif;hwp;iges;igs;jpeg;jpg;log;model;pdf;plt;png;ppt;pptx;smg;step;stp;tif;tiff;txt;xls;xlsx;xml;xps;zip;">
            </OBJECT>
        </div>
<!--             </div> -->
            <!-- //grid_area -->
     </div>
        <!-- //section(grid) -->
        <div>
            <span style="cursor: pointer; margin-left: 5px;"><label><input type="checkbox" id="chkUploadClose"> 업로드 완료 후 창 닫기</label></span>
        </div>
        <div class="btnArea popupBtn">
            <div class="fRight">
<!--                 <span class="btn_popup" id="requestPlan" ><a href="javascript:fnPlanRequest()">전송</a></span> -->
                <span class="btn_popup"><a href="javascript:$('#divPlan').close();">닫기</a></span>
            </div>
        </div>
    </div>
    <!-- layer_content -->
</div>

</script>