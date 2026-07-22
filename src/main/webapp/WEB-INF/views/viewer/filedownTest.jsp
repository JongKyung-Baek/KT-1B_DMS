<%@page import="org.apache.tomcat.util.codec.binary.Base64"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script>
$(document).ready(function() {
	<%-- 다운로드 초기화 --%>
	$("#downloadActiveX")[0].InitializationDownload();
	$("#downloadActiveX")[0].ShowArray("0|[열람]QAR-60349773-RA.PDF|[열람]QAR-60349773-RA.PDF|25462|D:/FDMS/Temp/|31|2019-12-02");
// 	$("#downloadActiveX")[0].ShowArray("0|94C780F8-1D86-44C0-B4B0-8CFB464798E3|문서1.xlsx|57473|D:/External/uploadfiles/20191202|31|2019-12-02");

// 	var arrFileList = new Array();
//     var arrFileInfo = new Array();
//     var strRowKey = "abcd" + "||";
//         arrFileInfo.push(strRowKey);
//         arrFileInfo.push("");
//         arrFileInfo.push("문서1.xlsx");
//         arrFileInfo.push("57473");
//         arrFileInfo.push(data[i].FILE_PATH);
//         arrFileInfo.push("");
//         arrFileInfo.push("");
//         arrFileList.push(arrFileInfo);
//     }
//     $("#downloadActiveX")[0].ShowArray(arrFileList);


});

<%--
  - 파일 다운로드 성공 시 처리
  --%>
function fnDownloadSuccess(){
    
}

function fnDownloadFail(){
    
}

function fnDownloadFinish(bSuccessAll){
    alert("다운로드 완료");
}

function fnDownloadBeforeCheck(){
	return true;
}
</script>
<div class="section clearB">
	<div style="border:#6B71BD solid 1px; margin:5px;">
       	<OBJECT ID="downloadActiveX" CLASSID="CLSID:45356ABB-F3F3-48A2-9511-B11F72CF0069" CODEBASE="${config.updownCabUrl }" width="552" height="300" style="margin-left: 5px; margin-right: 5px;">
<%--        	<OBJECT ID="downloadActiveX" CLASSID="CLSID:45356ABB-F3F3-48A2-9511-B11F72CF0069" CODEBASE="http://192.168.0.225:8080/resources/install/ExtUpDown.cab#version=1,2015,524,2" width="552" height="300" style="margin-left: 5px; margin-right: 5px;"> --%>
           	<PARAM NAME="downloadServerIp"    VALUE="${config.updownServerIp }">
            <PARAM NAME="downloadServerPort"  VALUE="${config.updownSrverPort }">
            <PARAM NAME="downloadLangCode"    VALUE="${config.updownServerLangCode }">
            <PARAM NAME="downloadUserAuth"    VALUE="${config.updownUserAuth }">
            <PARAM NAME="downloadSecretKey"   VALUE="${config.updownSecretKey}">
            <PARAM NAME="downloadIsSecurity"  VALUE="${config.updownIsSecurity}">
            <PARAM NAME="downloadVolume"      VALUE="${downloadVolume }">	<%--  --%>
<!--             <PARAM NAME="downloadLangCode"    VALUE="KO"> -->
<!--             <PARAM NAME="downloadUserAuth"    VALUE="32"> -->
<!--             <PARAM NAME="downloadIsSecurity"  VALUE="0"> -->
<!--             <PARAM NAME="downloadVolume"      VALUE="R"> -->
        </OBJECT>
	</div>
</div>
    <!-- layer_content -->
