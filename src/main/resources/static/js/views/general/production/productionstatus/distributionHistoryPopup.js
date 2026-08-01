
function setGridHistoryParam(gridHistoryId, data){
	gridHistoryParam = {
			gridId: gridHistoryId,
			data: data
	};
}



function gridComplete(gridId){
	$('#detailHistory'+detailInfoIdx).text($('#'+gridHistoryId+detailInfoIdx).jqGrid('getGridParam', 'reccount'));

	var dtbL = $('.dialogContent .dialogToolbar').length;
	for(i = 0 ; i < dtbL ; i++ ){
		var dtbWL = $('.dialogContent .dialogToolbar').eq(i).find('.left').outerWidth(true);
		$('.dialogContent .dialogToolbar').eq(i).find('.right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
	}
}


function tabClickFunc(tabId){
	detailInfoIdx = tabId;
	var gridHistoryInfo = distributionHistoryList[tabId - 1].list;
	setGridHistoryParam(gridHistoryId+tabId, gridHistoryInfo);
	settingGridWithData(gridColmodel, gridHistoryParam, 'gridHistoryParam');
}