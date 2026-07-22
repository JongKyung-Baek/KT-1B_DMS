<%@page import="java.util.Enumeration"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page import="org.apache.tomcat.util.codec.binary.Base64"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script>
var userAuth = "${config.updownUserAuth}";

$(document).ready(function() {
	<%-- 다운로드 초기화 --%>
	$("#downloadActiveX")[0].InitializationDownload();
// 	$("#downloadActiveX")[0].ShowArray("0|a.jpg|a.jpg|29106|X:/FDMS/OUT/|31|2019-12-02");
	var jsonArr = new Array();
	var gridId = "${gridId}";

	$.each($("#${gridId}").getGridParam('selarrrow'), function(index, item){
		var data = $("#" + gridId).jqGrid('getRowData', item);
		jsonArr.push(data);
	});
	var param = {
			list : jsonArr
	};

	callAjax(param, '/common/down/selectList', callDownload, 'json');
});


function callDownload(response){
	console.log("callDownload response");
	console.log(response);
	var arrFileList = new Array();
    for(var i=0 ; i<response.length ; i++){
        var arrFileInfo = new Array();
        var strRowKey = i + "||";
        arrFileInfo.push(strRowKey);
        arrFileInfo.push(response[i].fileNm);		// uuid
        arrFileInfo.push(response[i].orgFileNm);	// 실제 파일 이름
        arrFileInfo.push(response[i].fileSize);
        arrFileInfo.push(response[i].filePathNm);
        arrFileInfo.push("31");

        /* arrFileInfo.push(strRowKey);
        arrFileInfo.push(response[i].fileNm);		// uuid
        arrFileInfo.push(response[i].orgFileNm);	// 실제 파일 이름
        arrFileInfo.push(response[i].fileSize);
        arrFileInfo.push(response[i].endDate);
        arrFileInfo.push("31");
        arrFileInfo.push(response[i].filePathNm); */

        arrFileList.push(arrFileInfo);


//         arrFileInfo.push("60818112R2_165943_1005.plt");
//      	arrFileInfo.push("60818112R2_165943_1005.plt");
//      	arrFileInfo.push("29106");
//      	arrFileInfo.push("D:/FDMS/OUT/");
//      	arrFileInfo.push("31");
//      	arrFileInfo.push("2019-12-05");
//      	arrFileList.push(arrFileInfo);
    }
	$("#downloadActiveX")[0].ShowArray(arrFileList);
	console.log("callDownload arrFileList");
    console.log(arrFileList);
}



/*
 * 파일 개별 성공 시
 */
function fnDownloadSuccess(){
}

/*
 * 파일 다운로드 실패시
 */
function fnDownloadFail(){
	console.log("실패");

}

/*
 * 모든 파일 전송 완료시
 */
function fnDownloadFinish(bSuccessAll){
	console.log("완료");
}

/*
 * 파일 다운로드 전
 */
function fnDownloadBeforeCheck(){
	return true;
}
</script>

<div class="dialogContent popup-base popup-actions-center">
	<div class="section">

				<OBJECT ID="downloadActiveX" CLASSID="CLSID:45356ABB-F3F3-48A2-9511-B11F72CF0069" CODEBASE="${config.updownCabUrl }" width="706" height="320">
	           		<PARAM NAME="downloadServerIp"    VALUE="${config.updownServerIp }">
	            	<PARAM NAME="downloadServerPort"  VALUE="${config.updownServerPort }">
	            	<PARAM NAME="downloadLangCode"    VALUE="${config.updownLangCode }">
	            	<PARAM NAME="downloadUserAuth"    VALUE="${config.updownUserAuth }">
	            	<PARAM NAME="downloadSecretKey"   VALUE="${config.updownSecretKey}">
	            	<PARAM NAME="downloadIsSecurity"  VALUE="${config.updownIsSecurity}">
	            <PARAM NAME="downloadVolume"      VALUE="R">
	        	</OBJECT>								
	       	
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
