<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<script type="text/javascript" src="/resources/js/views/outside/commonrequest/commonRequestPopup.js"></script>
<script type="text/javascript" src="/resources/js/views/inside/common/clipboard.js"></script>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
<script>
var popupInfo = ${popupInfo};
var dataType = '${dataType}';
var gridInfo = ${gridInfo };
  $(document).ready(function(){
        setGridParam();
        settingGrid('${gridInfo }', popupGridParam, 'popupGridParam');
        $("#"+popupGridId).jqGrid('clearGridData');
        $('#deployUserCd').attr('disabled','true');

        $('#distributionTypeviewPrint').prettyCheckable({labelPosition: 'right'});
        $('#distributionTypefileDistribution').prettyCheckable({labelPosition: 'right'});
        $('#fileDistributionTypegeneral').prettyCheckable({labelPosition: 'right'});
        $('#fileDistributionTypesecurity').prettyCheckable({labelPosition: 'right'});
  });

function setGridParam(){
	popupGridParam = {
			gridId : popupGridId,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check',
			cellEdit: true,
			cellsubmit: 'clientArray',
			shrinkToFit: false
	};
}
</script>
	<!-- 배포요청 팝업(배포요청 버튼) -->
		<div class="dialogContent commonRequestPopup popup-base popup-actions-center popup-type-form-grid popup-overflow-visible">
			<div class="popupHero">
				<h2><spring:message code="btn.distributionRequest" /></h2>
				<p>
				<c:choose>
					<c:when test="${'DRAWING' eq dataType }">
						<spring:message code='title.drawingRequestMsg' />
					</c:when>
					<c:when test="${'DOC' eq dataType }">
						<spring:message code='title.docRequestMsg' />
					</c:when>
					<c:when test="${'SW' eq dataType }">
						<spring:message code='title.swRequestMsg' />
					</c:when>
					<c:otherwise>
						<spring:message code='title.productionRequestMsg' />
					</c:otherwise>
				</c:choose>
				</p>
			</div>
			<form name="requestInfo" id="requestInfo">
			<ul class="section popupCard popupFormGrid">
				<input type="hidden" id="purchaserUserCd" name="purchaserUserCd" value="">
				<input type="hidden" id="deployUserEmail" name="deployUserEmail" value="${sessionUser.email }">

			<%-- 
				<!--  배포방식 -->
				<c:if test="${'SW' ne dataType }">
				<li class="distributionCheck full">
					<label for=""><spring:message code="form.distributionType" /></label>
					<div>
						<custom:popupRadio labelSide="right"
						options="${distributionMethod }" name="distributionType"
						checkedValue="viewPrint" />
						<!-- Viewing/출력 , 파일배포 생성태그 -->

						<div class="fileDistributionType">
							<custom:popupRadio labelSide="right"
								options="${distributionFileType }" name="fileDistributionType"
								disabled="disabled" checkedValue="security" />
							<!-- 파일배포-일반영역, 보안영역 -->
						</div>
					</div>
				</li>
				</c:if>
			--%>				
			<!-- 배포방식(새 구조) -->
			 <c:if test="${'SW' ne dataType }">
				<li class="distributionCheck full">
					<label for=""><spring:message code="form.distributionType" /></label>
					<div class="distributionTypeGroup">
						

						<div class="distributionTypeOption">
							<input type="radio" id="distributionTypeviewPrint" name="distributionType" value="viewPrint" checked>
							<label for="distributionTypeviewPrint">Viewing/출력</label>
						</div>

						<div class="distributionTypeOption distributionTypeOptionFile">
							<div class="distributionTypeOptionMain">
								<input type="radio" id="distributionTypefileDistribution" name="distributionType" value="fileDistribution">
								<label for="distributionTypefileDistribution">파일배포</label>
							</div>

							<div class="fileDistributionType">
								<input type="radio" id="fileDistributionTypegeneral" name="fileDistributionType" value="general" disabled>
								<label for="fileDistributionTypegeneral">일반영역</label>

								<input type="radio" id="fileDistributionTypesecurity" name="fileDistributionType" value="security" checked disabled>
								<label for="fileDistributionTypesecurity">보안영역</label>
							</div>
						</div>


					</div>
				</li>
			 </c:if>


			<li class="third">
				<custom:popupSelectBox options="${requestPurpose }" defaultText="form.select" name="requestPurpose" label="form.purpose" id="requestPurpose"/>
			</li>
			<c:if test="${'PRODUCTION' == dataType}">
				<li class="third">
						<custom:popupSelectBox options="${objectType }" name="objectType" label="form.objectType" id="objectType" />
				</li>
			</c:if>
			<li class="third">
				<custom:popupSelectBox options="${businessAreaCd }" defaultText="form.select" selectedValue="" name="businessAreaCd" label="form.businessArea" id="businessAreaCd"/>
			</li>
			<li class="third">
				<custom:popupSelectBox options="${emptyArray }" selectedValue="" name="acceptanceUserCd" label="form.approvalUser" id="acceptanceUserCd"/>
			</li>
			<li class="third">
				<custom:popupSelectBox options="${deployUser }" selectedValue="${sessionUser.userCd}" defaultText="form.select" name="deployUserCd" label="form.companyUser" id="deployUserCd"/>
			</li>
<!-- 			<li class="half"> -->
<%-- 				<custom:popupInputText name="requestNo" label="form.requestNo" value='${autoIncrement}' id="requestNo" disabled="disabled"/> --%>
<!-- 			</li> -->
				<li class="full">
					<custom:popupInputTextArea name="requestDesc" label="form.requestReason" value="" rows="3" id="requestDesc"/>
				</li>
			</ul>
			</form>
			<div class="section popupCard">
				<div class="dialogToolbar">
					<div class="left">
						<span class="gridTitle"><spring:message code='${listTitle }' /></span><span class="listCount" id="listCount"></span>
				</div>
				<div class="right">
					<button class="ui-button ui-corner-all " onclick="exportExcel()">엑셀 다운로드</button>
					<button class="ui-button ui-corner-all " onclick="similar_inspection()"><spring:message code="toolbar.similar_inspection" /></button>
					<button class="ui-button ui-corner-all " onclick="inspection()"><spring:message code="toolbar.inspection" /></button>
					<button class="ui-button ui-corner-all " onclick="addRow()"><spring:message code="toolbar.add" /></button>
					<button class="ui-button ui-corner-all " onclick="paste()"><spring:message code="toolbar.paste" /></button>
					<button class="ui-button ui-corner-all " onclick="deleteRow()"><spring:message code="toolbar.delete" /></button>
					<button class="ui-button ui-corner-all " onclick="deleteAllRow()"><spring:message code="toolbar.deleteAll" /></button>
				</div>
			</div>
			<div class="gridContainer">
				<table id="gridRequestPopup"></table>
			</div>
		</div>
	</div>
	<div class="dialogBtnSet">
		<div class="left"></div>
		<div class="right">
			<button id="req" class="ui-button ui-corner-all bottomBtn" onclick="deployRequest()"><spring:message code="btn.distributionRequest" /></button>
			<button id="close" name="close" class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')"><spring:message code="btn.close" /></button>
		</div>
	</div>
<div id="detailPopupDialog" class="dialogContainer"></div>
