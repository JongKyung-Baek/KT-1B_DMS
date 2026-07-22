var nameColumnWidth;
var totalPageCnt = 0;
var businessAreaCd;
var businessAreaNm;

function setGridAcceptanceParam(){
	gridAcceptanceParam = {
			gridId : gridAcceptanceId,
			formId : 'formApprovalInfoPopup',
			url : '/inside/production/requeststatus/selectAcceptanceUserList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}
	return gridAcceptanceParam;
}

function formatOpenFileList(cellValue, options, rowdata, action){
	if(undefined === cellValue) {
		return '';
	}else{
		return '<a onclick="openFileList(\'' + rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["objectTypeData"] + '\')">'+cellValue+'</a>';
	}
}



function setGridProductionParam(){
	gridProductionParam = {
			gridId : gridProductionId,
			formId : 'formApprovalInfoPopup',
			url : '/inside/production/requeststatus/selectProductionList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}
	return gridProductionParam;
}


function gridComplete(){
	//리스트 갯수
	var $grid = $('#'+gridAcceptanceId);
	$('#acceptanceListCount').html($grid.getGridParam('records'));
	$grid = $('#'+gridProductionId);
	$('#productionListCount').html($grid.getGridParam('records'));
}