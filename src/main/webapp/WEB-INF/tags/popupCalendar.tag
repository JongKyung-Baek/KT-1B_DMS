<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="label" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="id" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="name" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="defaultDate" required="true" rtexprvalue="true" description="기본 날짜" %>
<%@ attribute name="maxDate" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="minDate" required="false" rtexprvalue="true" description="" %>



<label for="${id}" }><spring:message code="${label }"/></label>
<div class='input-append date'><input id="${id }" name="${name }" value="" type="text" />
<script>
$(function() {
	var defaultDate = '${defaultDate}';
	if(defaultDate.indexOf("today") !=  -1){
		var myDate = new Date();
		var month = "00" + (myDate.getMonth() + 1);
		var day = "00" + (myDate.getDate());
		$('#${id }').val(myDate.getFullYear() + '-' + month.slice(-2) + '-' + day.slice(-2));
	}else{
		$('#${id }').val(getDateValue('${defaultDate}'));
	}


	$("#${id}").datepicker({	//시작일
		dateFormat: 'yy-mm-dd'
		, prevText: g_msg('label.previousMonth')
	    , nextText: g_msg('label.nextMonth')
	    , maxDate: "${maxDate}"
	    , minDate: "${minDate}"
	    , monthNames: [g_msg('label.january'), g_msg('label.february'), g_msg('label.march'), g_msg('label.april'), g_msg('label.may'), g_msg('label.june'), g_msg('label.july'), g_msg('label.august'), g_msg('label.september'), g_msg('label.october'), g_msg('label.november'), g_msg('label.december')]
	    , monthNamesShort: [g_msg('label.january'), g_msg('label.february'), g_msg('label.march'), g_msg('label.april'), g_msg('label.may'), g_msg('label.june'), g_msg('label.july'), g_msg('label.august'), g_msg('label.september'), g_msg('label.october'), g_msg('label.november'), g_msg('label.december')]
	    , dayNames: [g_msg('label.sunday'), g_msg('label.monday'), g_msg('label.tuesday'), g_msg('label.wednesday'), g_msg('label.thursday'), g_msg('label.friday'), g_msg('label.saturday')]
	    , dayNamesShort: [g_msg('label.sundayShort'), g_msg('label.mondayShort'), g_msg('label.tuesdayShort'), g_msg('label.wednesdayShort'), g_msg('label.thursdayShort'), g_msg('label.fridayShort'), g_msg('label.saturdayShort')]
	    , dayNamesMin: [g_msg('label.sundayShort'), g_msg('label.mondayShort'), g_msg('label.tuesdayShort'), g_msg('label.wednesdayShort'), g_msg('label.thursdayShort'), g_msg('label.fridayShort'), g_msg('label.saturdayShort')]
	    , yearSuffix: g_msg('label.year')
	    , showOn: "button"
    	, changeYear: true
	    , changeMonth: true
	    , yearRange: '-100:+100'
	    , beforeShow: function() {
	        setTimeout(function(){
		        $(".ui-datepicker select").select2();
	            $('.ui-datepicker').css('z-index', 2147483002);
	        }, 0);
	    }
	});

	$("#${id}").click(function() {
		$("#${id}").datepicker('show');
	 });
});
</script>
</div>
