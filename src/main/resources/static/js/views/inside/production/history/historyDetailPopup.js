
function setGridProductionParam(){
	gridProductionParam = {
			gridId: gridProductionId,
			data: historyDetailInfo.productionInfoVoList
	};
}

function setGridRequestDetailParam(){
	gridRequestDetailParam = {
			gridId: gridRequestDetailId,
			data: historyDetailInfo.requestDetailVoList
	}
}

function setGridDetailInfoParam(gridDetailInfoId, data){
	gridDetailInfoParam = {
			gridId: gridDetailInfoId,
			data: data
	}
}


function gridComplete(gridId){
	console.log(gridId);
	if(gridId == 'gridProductionParam'){
		console.log('gridProduction');
		$('#productonListCount').text($('#'+gridProductionId).jqGrid('getGridParam', 'reccount'));
	}else if(gridId == 'gridRequestDetailParam'){
		$('#requestListCount').text($('#'+gridRequestDetailId).jqGrid('getGridParam', 'reccount'));
	}else if(gridId == 'gridDetailInfoParam'){
		$('#deployInfoCount'+detailInfoIdx).text($('#'+gridDetailInfoId+detailInfoIdx).jqGrid('getGridParam', 'reccount'));
	}

	var dtbL = $('.dialogContent .dialogToolbar').length;
	for(i = 0 ; i < dtbL ; i++ ){
		var dtbWL = $('.dialogContent .dialogToolbar').eq(i).find('.left').outerWidth(true);
		$('.dialogContent .dialogToolbar').eq(i).find('.right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
	}
}

function tabClickFunc(tabId){
	if(tabId == 'deployInfo'){
		settingGridWithData(gridProductionInfo, gridProductionParam, 'gridProductionParam');
	}else if(tabId == 'approveInfo'){
		settingGridWithData(gridRequestDetailInfo, gridRequestDetailParam, 'gridRequestDetailParam');
	}else {
		detailInfoIdx = tabId;
		var gridDeployInfo = requestDeployVoList[tabId - 1].deployInfoList;
		setGridDetailInfoParam(gridDetailInfoId+tabId, gridDeployInfo);
		settingGridWithData(gridDetailInfo, gridDetailInfoParam, 'gridDetailInfoParam');
	}
}