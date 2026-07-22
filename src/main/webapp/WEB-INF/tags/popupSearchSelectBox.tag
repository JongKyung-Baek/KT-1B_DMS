<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="label" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="id" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="name" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="rows" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="selectedValue" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="maxSelection" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="selectedText" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="defaultText" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="options" required="false" rtexprvalue="true" description="" type="java.util.List" %>
<%@ attribute name="searchUrl" required="true" rtexprvalue="true" description="조회할 url" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="Disabled 여부" %>


<c:if test="${null != placeholder && '' != placeholder }">
	<spring:message code="${placeholder }" var="placeholder" />
</c:if>
<label for="${id}"><spring:message code="${label }"/></label>
<div>
<select id="${id }" name="${name }" style="width:${width }" >
<c:if test="${null != defaultText && '' != defaultText }">
	<option value=""><spring:message code="${defaultText }"/></option>
</c:if>
<c:forEach items="${options}" var="option" >
	<c:choose>
		<c:when test="${selectedValue == option.comboVal }">
			<option value="${option.comboVal }" selected>${option.comboLabel }</option>
		</c:when>
		<c:otherwise>
			<option value="${option.comboVal }" >${option.comboLabel }</option>
		</c:otherwise>
	</c:choose>
</c:forEach>
</select>
<script>
	var $popupDialog = $('#popupDialoga');
	var $dropdownParent = $popupDialog.closest('.ui-dialog');
	if(!$dropdownParent.length) {
		$dropdownParent = $popupDialog;
	}
	var option = {
			minimumResultsForSearch: 1,
			dropdownParent: $dropdownParent,
			language: 'ko',
			ajax : {
				url: "${searchUrl}",
				delay: 250,
				dataType: 'json',
				contentType: "application/json",
			}
		};

	var maxSelection = '${maxSelection}';

	if('' !== maxSelection) {
		option["maximumSelectionLength"] = maxSelection;
		option["multiple"] = true;
	}

	$("#${id}").select2(option);

	var $ts = $("#${id}")
	var selectedCode = [];
	var selectedValue = '${selectedValue}';

	if('' !== selectedValue) {
		var $newOption = $("<option></option>").val('${selectedValue}').text('${selectedText}');
		$ts.append($newOption);
		selectedCode.push('${selectedValue}');
		$ts.val(selectedCode).trigger("change");
	}

	$($('#${id}').data('select2').$container).addClass('searchSelect');
</script>

<c:if test="${'Y' == disabled}">
	<script>
		document.getElementById("${id}").disabled = 'disabled';
	</script>
</c:if>
</div>
