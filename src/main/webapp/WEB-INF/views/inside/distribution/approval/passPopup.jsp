<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/approval/approvalPopup.js"></script>
<!-- 배포승인 - 도면/문서/SW 팝업(요청번호 상세보기)-->
<script>
var popupGridParam;

function pass() {
	var param = $("#formApprovalPopup").serializeObject();
	param.saveType = "P";

	callAjax(param, '/inside/distribution/savePass', function(){
		alertMessage(g_msg('msg.passComplete'), function(){			//이관되었습니다.
			$(this).dialog("close");
			closePopup('popupDialog');
		});
	})
}

</script>
<div class="dialogContent">
<form id="formApprovalPopup">
	<input id="requestNo" name="requestNo" value="${requestNo}" type="hidden"/>
	<ul class="section">
		<li>
			<custom:popupSelectBox options="${list}" name="passTarget" label="form.passTarget" id="passTarget"/>
		</li>
	</ul>
</form>
</div>
<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<%-- 이관 --%>
		<custom:popupButton function="pass()" name="pass" label="btn.pass" id="pass"/>
		<%-- 닫기 --%>
		<custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
	</div>
</div>