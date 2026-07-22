
function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridRequestStatus',
			formId : 'formRequestStatusPopup',
			url : '/inside/distribution/requeststatus/requestStatusPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}


$(function() {
	if('DRAWING'==objectType){
		$("#gridRequestStatus").jqGrid("hideCol", "swVersion").trigger('reloadGrid');
	}else if('DOC'==objectType){
		$("#gridRequestStatus").jqGrid("hideCol", "swVersion");
		$("#gridRequestStatus").jqGrid("hideCol", "drawingType");
		$("#gridRequestStatusPopup").jqGrid("hideCol", "drawingType");
	}else if('SW'==objectType){
		$("#gridRequestStatus").jqGrid("hideCol", ["totalPage","page"]);
		$("#gridRequestStatus").jqGrid("hideCol", "drawingType");
	}
});

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	//리스트 갯수
	var $grid = $('#gridRequestStatus');
	$('#listCount').html($grid.getGridParam('records'));
	dialogToolbarWidth()
}

