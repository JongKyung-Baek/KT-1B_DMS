<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script>
var popupGridParam;
var gridAddId = 'gridAddNotice';
var formAddId = 'formAddNotice';
var files = [];
$(function() {
	//settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');

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
	if($.trim($("#noticeTitle").val()) === ""){
		isValidDataEmpty("noticeTitle", "form.noticeTitle");
		return false;
	}
	if($.trim($("#insertUserNm").val()) === ""){
		isValidDataEmpty("insertUserNm", "form.insertUid");
		return false;
	}
	if($.trim($("#contents").val()) === ""){
		isValidDataEmpty("contents", "form.contents");
		return false;
	}
	return true;
}
var param = $(formAddId).serializeObject();
console.log(param);

function fileUpload(){
	$('#noticeFile').click();
}

function fileChange(){
	var fileName = $('#noticeFile').val();
	if(fileName.indexOf("\\") != -1){
		$('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
	}
}

function saveAddNotice(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	param.append('file', $("#noticeFile")[0].files[0]);
	param.append('bbsParam', JSON.stringify($('#formAddNotice').serializeObject()));
	callAjaxUpload(param, "/bbs/notice/bbsNotice", addNoticeCallback);
}

function addNoticeCallback(response) {
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
<style>
	textarea#contents{height:80px;}

	/* calendar popup text rendering fix (datepicker/select2 font fallback) */
	.ui-datepicker,
	.ui-datepicker *,
	.ui-datepicker .ui-datepicker-title,
	.ui-datepicker table th,
	.ui-datepicker table td,
	.ui-datepicker .ui-datepicker-buttonpane button,
	.ui-datepicker .select2-container,
	.ui-datepicker .select2-selection__rendered,
	.ui-datepicker .select2-results__option{
		font-family:dotum, tahoma, sans-serif !important;
		font-size:13px !important;
	}

	
/* Notice add popup: dialogBtnSet is outside dialogContent, center buttons */
/* .noticeAddPopup + .dialogBtnSet {
  margin-top: 6px;
  padding-top: 0;
}

.noticeAddPopup + .dialogBtnSet .left {
  display: none;
}

.noticeAddPopup + .dialogBtnSet .right {
  float: none !important;
  width: 100%;
  text-align: center;
} */

</style>
<div class="dialogContent noticeAddPopup drawingRegisterPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>공지 등록 정보</h2>
		<p>필수 항목을 입력하고 첨부파일을 등록해 주세요.</p>
	</div>
	<form id="formAddNotice" name="formAddNotice">
		<ul class="section popupCard popupFormGrid">
			<li class="full">
				<custom:popupInputText name="noticeTitle" label="form.noticeTitle" value="${title}" id="noticeTitle"  />
			</li>
			<li class="half">
				<custom:popupInputText name="insertUserNm" label="form.insertUid" value="${insertUserNm }" id="insertUserNm" readOnly="readOnly" />
			</li>
			<li class="full">
				<custom:popupCalendarTerm startId="startDt" endId="endDt" label="label.noticeAlertDt" defaultStartDate="today" defaultEndDate="today" saveFlag="I"/>
			</li>
			<li class="full">
				<custom:popupInputTextArea name="contents" label="form.contents" rows="10" value="${contents}" id="contents" />
			</li>
		</ul>
	</form>
	<ul class="section popupCard popupFormGrid uploadOnly">
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
		<%-- 등록 --%>
		<custom:popupButton function="saveAddNotice()" name="save" label="btnAdd" id="save"/>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>
