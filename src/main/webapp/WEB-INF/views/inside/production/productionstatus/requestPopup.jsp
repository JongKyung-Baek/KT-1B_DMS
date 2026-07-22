<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<spring:message code='combo.productionDeployReplace' var='deployReplace'/>

<script type="text/javascript" src="/resources/js/views/inside/production/productionstatus/requestPopup.js"></script>
<!-- 기존대체 팝업(생산기술자료 배포 승인요청 버튼)-->
<script>
var gridProductionId = 'gridProductionReplaceRequest';

var gridDocumentParam;
$(document).ready(function (){
	setGridProductionParam();
	settingGrid('${gridProduction}', gridProductionParam, 'gridProductionParam');

});


</script>
<style>
	.ui-dialog.sizeXL ul.section li.third > label:not(.ui-controlgroup-item) + div.has-pretty-child,
	.ui-dialog.sizeXL ul.section li.third > div.has-pretty-child:nth-child(4):last-child{width:calc(33.2% - 160px);}
</style>
<div class="dialogContent">
	<form id="requestInfo" name="requestInfo">
	<ul class="section">
		<li class="third">
			<input type="hidden" name="deployType" id="deployType" value="NEW"/>
			<input type="hidden" name="requestUser" id="requestUser" value="sessionUser.userCd" />
			<custom:popupInputText name="deployTypeNm" label="form.deployType" value="${deployReplace}" id="deployTypeNm" disabled="disabled"/>
			<custom:popupInputText name="requestUserNm" label="form.requestUserName" value="${sessionUser.userNm }" id="requestUserNm" readOnly="readOnly"/>
			<custom:popupSelectBox name="teamLeader" options="${teamLeaderList }" selectedValue="${sessionUser.teamLeaderUid }" defaultText="form.select" label="form.authorizePerson" id="teamLeader"/>
		</li>
		<li class="third">
			<custom:popupCalendar name="deployDate" label="form.deployDate" defaultDate="today" id="deployDate" maxDate="+1M" minDate="today"/>
			<custom:popupCalendar name="validDate" label="form.deployDateTerm" defaultDate="today" id="validDate"  minDate="today"/>
		</li>
		<li class="third">
			<label for=""><spring:message code="form.watermarkDeployDtYn"/></label>
			<custom:popupRadio labelSide="right" options="${watermarkDeployDtYn }" name="watermarkDeployDtYn" checkedValue="N"/>
			<label for=""><spring:message code="form.watermarkPrintYn"/></label>
			<custom:popupRadio labelSide="right" options="${watermarkYn }" name="watermarkYn" checkedValue="N"/>
		</li>
		<li>
			<custom:popupInputTextArea name="requestDesc" label="form.requestReason" value="" rows="3" id="requestDesc"/>
		</li>
	</ul>
	<input type="hidden" id="sendEmailYn" name="sendEmailYn" value="Y"/>
	</form>
	<div class="section">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code='form.deployDocumentList'/></span><span class="listCount" id="productionListCount"></span>
			</div>
			<div class="right"></div>
		</div>
		<div class="gridContainer">
			<table id="gridProductionReplaceRequest"></table>
		</div>
	</div>
</div>
<div class="dialogBtnSet">
	<div class="left">
<%--		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y"  checked disabled>--%>
		<input type="checkbox" id="emailCheck" name="emailCheck" value="Y" checked onclick="toggleCheckboxValue('emailCheck')">
		<label for="emailCheck"><spring:message code='form.sendEmailYn'/></label>
		<script>
		$("#emailCheck").prettyCheckable({labelPosition: 'left'});
		</script>
	</div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" onclick='deployRequest()'><spring:message code='btn.productionRequest'/></button>
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code='btn.close'/></button>
	</div>
</div>
