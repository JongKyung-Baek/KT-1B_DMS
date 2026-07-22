<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ attribute name="gridId" required="true" rtexprvalue="true" description=""%>
<%@ attribute name="pager" required="false" rtexprvalue="true" description="pager사용여부 true/false"%>

<c:if test="${null == pager || '' == pager }">
	<c:set var="pagerYn" value="true"></c:set>
</c:if>

<c:set var="pagerId" value="${gridId }Pager"></c:set>

<div class="sbr"></div>
<div class="btnArea"></div>
<div class="gridArea whole">
	<div class="gridContainer">
		<table id="${gridId }"></table>

		<c:if test="${pagerYn == 'true' }">
			<div id="${pagerId }"></div>
		</c:if>
	</div>
</div>

<script>


/* ----- container & jqgrid height  ----- */
function containerHeight(){ // Set height value
	var tg1 = $('.bodyWrap .contentArea');
	if(tg1.siblings('.tabArea').length == 1){
		tg1.css({'height': tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true) - tg1.siblings('.tabArea').outerHeight(true) + 'px'});
	}else{
		tg1.css({'height': tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true) + 'px'});
	}

	var tg2 = $('.bodyWrap .gridArea');
	tg2.css({'height': tg2.parents('.contentArea').height() - tg2.siblings('.sbr').outerHeight(true) - tg2.siblings('.btnArea').outerHeight(true) + 'px'});

	var tg3 = $('.gridArea > .gridContainer .ui-jqgrid-view');
	if(tg3.siblings('.ui-jqgrid-pager').length == 1){
		 tg3.css({'height' : tg3.parents('.gridContainer').height() - tg3.siblings('.ui-jqgrid-pager').outerHeight(true) + 'px'});
	 }else{
		 tg3.css({'height' : tg3.parents('.gridContainer').height() + 'px'});
	 }

	var tg4 = $('.gridArea > .gridContainer .ui-jqgrid-bdiv');
	tg4.css({'height' : tg4.parent('.ui-jqgrid-view').height() - tg4.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});

	$('.contentArea.half').each(function(index){
		var brL = $(this).find('.halfGrid').length;
		for(i = 0 ; i < brL ; i++ ){
			var tghalf = $(this).find('.halfGrid').eq(i).find('.gridArea');
			tghalf.css({'height': $('.contentArea.half').height() - tghalf.siblings('.sbr').outerHeight(true) - tghalf.siblings('.btnArea').outerHeight(true) + 'px'});
			tghalf.find('.ui-jqgrid-view').css({'height' : 'calc(100% - 40px'});
			var tg5 = $(this).find('.halfGrid').eq(i).find('.gridArea > .gridContainer .ui-jqgrid-bdiv');
			tg5.css({'height' : tg5.parent('.ui-jqgrid-view').height() - tg5.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});
		}
	})
}

$(document).ready(function(){
	containerHeight();
	$(window).resize(function(){
		containerHeight();
	});
});
</script>