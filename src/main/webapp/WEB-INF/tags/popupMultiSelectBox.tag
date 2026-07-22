<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="label" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="id" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="name" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="rows" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="count" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="selectedValue" required="false" rtexprvalue="true" description="" type="java.util.List"%>
<%@ attribute name="defaultText" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="options" required="true" rtexprvalue="true" description="" type="java.util.List" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="Disabled 여부" %>

<c:if test="${null != placeholder && '' != placeholder }">
	<spring:message code="${placeholder }" var="placeholder" />
</c:if>
<label for="${id}" ><spring:message code="${label }"/></label>
<div>
<select id="${id }" name="${name }" multiple style="width:${width }" >
<c:if test="${null != defaultText && '' != defaultText }">
	<option value=""><spring:message code="${defaultText }"/></option>
</c:if>
<c:if test="${null != options }">
	<c:forEach items="${options}" var="option" >
		<option value="${option.comboVal }" >${option.comboLabel }</option>
	</c:forEach>
</c:if>
</select>
<script>
	$("#${id}").select2({
		dropdownParent: $('#popupDialoga'),
		multiple: true,
// 		minimumResultsForSearch: 1,
		maximumSelectionLength : "${count}"
	});
	var arr = '${selectedValue}';
	arr = arr.substring(1, arr.length -1);
	var temp = $.trim(arr).split(', ');
	$("#${id}").val(temp).trigger('change');
</script>

<c:if test="${'Y' == disabled}">
	<script>
		document.getElementById("${id}").disabled = 'disabled';
	</script>
</c:if>
</div>
