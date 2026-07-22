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
<%@ attribute name="defaultText" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="options" required="true" rtexprvalue="true" description="" type="java.util.List" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="Disabled 여부" %>


<c:if test="${null != placeholder && '' != placeholder }">
	<spring:message code="${placeholder }" var="placeholder" />
</c:if>
<label for="${id}" }><spring:message code="${label }"/></label>
<div>
<select id="${id }" name="${name }" style="width:${width }" >
<c:if test="${null != defaultText && '' != defaultText }">
	<option value=""><spring:message code="${defaultText }"/></option>
</c:if>
<c:if test="${null != options }">
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
</c:if>
</select>
<c:choose>
	<c:when test="${options.size() >= 10 }">
		<script>
			var $popupDialog = $('#popupDialoga');
			var $dropdownParent = $popupDialog.closest('.ui-dialog');
			if(!$dropdownParent.length) {
				$dropdownParent = $popupDialog;
			}
			$("#${id}").select2({dropdownParent: $dropdownParent});
			$($("#${id}").data('select2').$container).addClass('searchSelect');
		</script>
	</c:when>
	<c:otherwise>
		<script>
			var $popupDialog = $('#popupDialoga');
			var $dropdownParent = $popupDialog.closest('.ui-dialog');
			if(!$dropdownParent.length) {
				$dropdownParent = $popupDialog;
			}
			$("#${id}").select2({
				minimumResultsForSearch: -1,
				dropdownParent: $dropdownParent
			});
		</script>
	</c:otherwise>
</c:choose>

<c:if test="${'Y' == disabled}">
	<script>
		document.getElementById("${id}").disabled = 'disabled';
	</script>
</c:if>
</div>
