<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<style>
	.ui-dialog ul.section li div.fileDownload { border: 0; }
	.fileDownBtn { margin: 0; margin-right: 3px; }
</style>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/notice/noticePopup.js"></script>
<script>
var popupGridParam;
var gridId='gridNoticePopup';
var updateflag;
var files = [];
$(function() {
	//settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
	$(".ui-dialog .ui-dialog-titlebar-close").click(function(event){
			console.log("close-popup");
			closePopup('popupDialog');
		});

	function applyNoticeCalendarUI() {
		['#startDt', '#endDt'].forEach(function(selector) {
			var $cal = $(selector);
			if (!$cal.length) return;

			if ($cal.data("datepicker")) {
				$cal.datepicker("option", {
					prevText: "\uC774\uC804 \uB2EC",
					nextText: "\uB2E4\uC74C \uB2EC",
					monthNames: ["1\uC6D4","2\uC6D4","3\uC6D4","4\uC6D4","5\uC6D4","6\uC6D4","7\uC6D4","8\uC6D4","9\uC6D4","10\uC6D4","11\uC6D4","12\uC6D4"],
					monthNamesShort: ["1\uC6D4","2\uC6D4","3\uC6D4","4\uC6D4","5\uC6D4","6\uC6D4","7\uC6D4","8\uC6D4","9\uC6D4","10\uC6D4","11\uC6D4","12\uC6D4"],
					dayNames: ["\uC77C\uC694\uC77C","\uC6D4\uC694\uC77C","\uD654\uC694\uC77C","\uC218\uC694\uC77C","\uBAA9\uC694\uC77C","\uAE08\uC694\uC77C","\uD1A0\uC694\uC77C"],
					dayNamesShort: ["\uC77C","\uC6D4","\uD654","\uC218","\uBAA9","\uAE08","\uD1A0"],
					dayNamesMin: ["\uC77C","\uC6D4","\uD654","\uC218","\uBAA9","\uAE08","\uD1A0"],
					yearSuffix: "\uB144"
				});
			}

			var $wrap = $cal.closest(".input-append.date");
			var $trigger = $wrap.find(".ui-datepicker-trigger").first();
			if (!$wrap.length || !$trigger.length) return;

			$wrap.css({ position: "relative", display: "inline-block" });
			$cal.css({ width: "100%", paddingRight: "34px", boxSizing: "border-box" });
			$trigger.css({
				position: "absolute",
				top: "5px",
				right: "1px",
				width: "30px",
				height: "23px",
				margin: "0",
				padding: "0",
				border: "0",
				borderRadius: "0 4px 4px 0",
				zIndex: "2"
			});
		});
	}

	function arrangeNoticeDateRange() {
		var $startWrap = $("#startDt").closest(".input-append.date");
		var $endWrap = $("#endDt").closest(".input-append.date");
		if (!$startWrap.length || !$endWrap.length) return;

		var $li = $startWrap.closest("li");
		if (!$li.length || $li.find(".noticeDateRange").length) return;

		var $sep = $li.find(".fromTo").first();
		var $range = $('<div class="noticeDateRange"></div>');
		$range.append($startWrap).append($sep).append($endWrap);
		$li.append($range);
	}

	setTimeout(applyNoticeCalendarUI, 50);
	setTimeout(applyNoticeCalendarUI, 200);
	setTimeout(arrangeNoticeDateRange, 10);
	setTimeout(arrangeNoticeDateRange, 80);
});

function fileUpload(){
	$('#noticeFile').click();
}

function fileChange(){
	var fileName = $('#noticeFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function fileDownload(noticeCd,fileNo,fileNm){
	console.log("pushed");
	$("form[name=tmpForm]")
	.attr("action","/bbs/notice/fileDownload?noticeCd="+noticeCd+"&fileNo="+fileNo)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();
}
</script>
<style>
	textarea#contents{height:80px;}
	.noticeDetailPopup.drawingRegisterPopup .popupFormGrid.singleFileUpload,
	.noticeDetailPopup.drawingRegisterPopup .popupFormGrid.singleFileUpload .singleFileUpload { display: none; }
</style>
<c:choose>
	<c:when test="${updateFlag == 'Y' }">
		<c:set var="disabled" value="disabled"/>
	</c:when>
</c:choose>
<div class="dialogContent noticeDetailPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>공지 상세 정보</h2>
		<p>공지 내용을 확인하고 필요 시 수정해 주세요.</p>
	</div>
	<form id="formNoticePopup">
		<input type="hidden" name="noticeCd" id="noticeCd" value="${noticeCd}">
		<input type="hidden" name="hitCount" id="hitCount" value="${data.hitCount}">
		<ul class="section popupCard popupFormGrid">
			<li class="full">
				<custom:popupInputText name="noticeTitle" label="form.noticeTitle" value="${data.noticeTitle }" id="noticeTitle" disabled="${disabled}"/>
			</li>
			<li class="half">
				<custom:popupInputText name="insertUserNm" label="form.insertUid" value="${data.insertUserNm }" id="insertUserNm" disabled="disabled"/>
			</li>
			<li class="full">
				<custom:popupCalendarTerm startId="startDt" endId="endDt" label="label.noticeAlertDt" defaultStartDate="${data.startDt }" defaultEndDate="${data.endDt }" saveFlag="U"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="10" value="${data.contents }" id="contents" disabled="${updateFlag}"/>
			</li>
		</ul>
		<c:if test="${not empty formData.fileList }">
		<ul class="section popupCard popupFormGrid appendFile uploadOnly">
		<li class="appendFile full">
			<label for=""><spring:message code="grid.appendFile" /></label>
			<div class="fileDownload">
				<c:forEach items="${formData.fileList }" var="fileInfo">
					<button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( ${fileInfo.noticeCd}, ${fileInfo.fileNo }, "${fileInfo.fileNm }")' ><span>${fileInfo.fileNm }</span></button>
				</c:forEach>
				<button type="button" class="ui-button ui-corner-all removeBtn" onclick='removeFile()' ><span></span></button>
			</div>
		</li>
		</ul>
		</c:if>
		<c:if test="${empty formData.fileList }">
		<script>
			$(".singleFileUpload").show();
		</script>
		</c:if>
	</form>
	<ul class="section popupCard popupFormGrid singleFileUpload uploadOnly">
		<li class="singleFileUpload full">
			<%-- 파일추가 --%>
			<label><spring:message code="grid.appendFile"/></label>
			<div class="uploadLine">
				<form id="fileForm" name="fileForm" enctype="multipart/form-data">
					<input type="file" id="noticeFile" name="noticeFile" onchange="fileChange()" style="display: none;"/>
				</form>
				<input type="text" name="fileName" id="fileName" readonly="readonly"/>
				<button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"><spring:message code="btn.fileUpload"/></button>
			</div>
		</li>
	</ul>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 수정 --%>
		<c:if test="${sessionUser.roleGroup eq 'RG_001'}">
		<custom:popupButton function="updateNotice()" name="save" label="btnUpdate" id="save"/>
		</c:if>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
	<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">
	</form>
