<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="label" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="startId" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="endId" required="true" rtexprvalue="true" description="" %>
<%@ attribute name="defaultStartDate" required="true" rtexprvalue="true" description="기본 날짜" %>
<%@ attribute name="defaultEndDate" required="true" rtexprvalue="true" description="기본 날짜" %>
<%@ attribute name="disabled" required="false" rtexprvalue="true" description="" %>
<%@ attribute name="saveFlag" required="true" rtexprvalue="true" description="" %>

<label for="${startId}"><spring:message code="${label }"/></label>
<div class='input-append date'><input id="${startId }" name="${startId }" value="" type="text" ${disabled }/>
<script>
$(function() {
	if('${saveFlag}' === 'I') {
		$('#${startId }').val(getDateValue('${defaultStartDate}'));
	}
	else {
		$('#${startId }').val('${defaultStartDate}');
	}

	$("#${startId}").datepicker({	//시작일
		dateFormat: 'yy-mm-dd'
		, prevText: g_msg('label.previousMonth')
	    , nextText: g_msg('label.nextMonth')
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

	$("#${startId}").click(function() {
		$("#${startId}").datepicker('show');
	 });
});
</script>
</div>
<span class="fromTo">~</span>
<div class='input-append date'><input id="${endId }" name="${endId }" value="" type="text" />
<script>
$(function() {
	if('${saveFlag}' === 'I') {
		$('#${endId }').val(getDateValue('${defaultEndDate}'));
	}
	else {
		$('#${endId }').val('${defaultEndDate}');
	}

	$("#${endId}").datepicker({	//시작일
		dateFormat: 'yy-mm-dd'
		, prevText: g_msg('label.previousMonth')
	    , nextText: g_msg('label.nextMonth')
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

	$("#${endId}").click(function() {
		$("#${endId}").datepicker('show');
	 });
});
</script>
</div>
