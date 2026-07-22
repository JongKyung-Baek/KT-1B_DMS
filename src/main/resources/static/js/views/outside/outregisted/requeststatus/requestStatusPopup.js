function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridRequestStatusPopup',
			formId : 'formRequestStatusPopup',
			url : '/outside/outregisted/requeststatus/selectPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false,
			selectRowAction : 'check'
	}
	return popupGridParam;
}


$(function() {
	$('input[name=distributionType]').prettyCheckable('disable');
	if("Y"==printYn){		//Viewing/출력
		$('#distributionTypeviewPrint').prettyCheckable('check');
	}
	if( ("Y" == deployNormalYn) || ("Y" == deploySpecialYn)){
		$('#distributionTypefileDistribution').prettyCheckable('check');
		if("Y" == deployNormalYn){
			$('#fileDistributionTypegeneral').prettyCheckable('check');
		}
		if("Y" == deploySpecialYn){
			$('#fileDistributionTypegeneral').prettyCheckable('check');
		}
	}
});

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	$('#listCount').text($('#gridRequestStatusPopup').jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}

