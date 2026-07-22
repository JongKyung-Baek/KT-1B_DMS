<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="/resources/js/views/outside/commondestroy/commonDestroyStatusPopup.js"></script>
<!-- 배포승인현황 팝업(첨부파일 상세보기) - 폐기내역 -->
<script>
var popupInfo = ${popupInfo};
</script>
<div class="dialogContent commonDestroyStatusPopup popup-base popup-actions-center popup-type-detail">
	<div class="popupHero">
		<h2>폐기 내역</h2>
		<p>요청된 폐기 사유와 첨부 파일 정보를 확인할 수 있습니다.</p>
	</div>
	<ul class="section popupCard popupFormGrid popup-grid-1">
		<li>
			<custom:popupInputText name="destroyType" label="form.destroyType" value='${info.destroyType}' id="requestNo" disabled="disabled"/>
		</li>
		<li>
			<custom:popupInputTextArea name="destroyDesc" label="form.destroyDesc" value="${info.requestDesc }" rows="3" id="destroyDesc" disabled="Y"/>
		</li>
		<li class="full">
			<label for=""><spring:message code="form.uploadFileList" /></label>
			<div class="fileDownloadList">
				<c:forEach items="${info.fileList }" var="fileInfo">
					<div class="fileDownload"><button type="button" class="ui-button ui-corner-all fileDownBtn" onclick='fileDownload( ${fileInfo.destroyNo}, ${fileInfo.destroyFileSeq }, "${fileInfo.fileName }")' ><span>${fileInfo.fileName }</span></button></div>
				</c:forEach>
			</div>
		</li>
	</ul>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')">닫기</button>
	</div>
</div>
<iframe name="hiddenFrame" style="display: none;"></iframe>
	<form name="tmpForm">

	</form>
