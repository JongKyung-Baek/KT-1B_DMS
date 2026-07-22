
function paste(){
	if(getIsInternetExplorer()){
		pasteForIE();
	}else{
		pasteForChrome();
	}
}




/**
 * 붙여넣기
 * IE에서만 동작(추후 chrome이나 기타 browser에서 동작하는 방법 찾아야함)
 * 클립보드에서 데이터를 읽어온 후
 * 해당 데이터를 도면/문서/SW번호에 자동으로 입력
 * 기존 입력되어있던 목록은 삭제 되고 클립보드에 있던 목록만 신규로 입력된다.
 * @returns
 */
function pasteText(text){
//	var arrClipboard = window.clipboardData.getData("Text").split("\r\n");
	var arrClipboard = text.split("\r\n");
	if( arrClipboard.length > 501 ) {
		alertMessage(g_msg('msg.pasteWarning')); //붙여넣기 하신 갯수는 500 건을 초과할 수 없습니다. 200 건 이하로 붙여넣기 해주세요.
		return;
	}
	deleteAllRow();
	createGrid(arrClipboard, popupInfo.allSearch);
	gridParam.searchAllParam = $('#'+searchAllGridId).jqGrid('getRowData');
}

function createGrid(arrClipboard, allSearch){
	var gridData = [];
	if(allSearch){
		$.each(arrClipboard, function(index, item){
			arrByColumn = arrClipboard[index].split("\t");
			if(arrByColumn.length != '3')return;
			var data = {
				drawingNo: arrByColumn[0],
//				drawingRev: arrByColumn[1],
				documentNo: arrByColumn[1],
//				documentRev: arrByColumn[3],
				swNo: arrByColumn[2],
//				swRev: arrByColumn[5]
			};
			gridData.push(data);
		});
		$('#'+searchAllGridId).jqGrid('setGridParam',
				{
					datatype:'local',
					data: gridData,
					rowNum: 500
				})
				.trigger('reloadGrid');
	}else{
		$.each(arrClipboard, function(index, item){
			arrByColumn = arrClipboard[index].split("\t");
			if(arrByColumn.length != '1')return;
			if(arrByColumn =='')return;
			var data = {};
			data[popupInfo.objectId] = arrByColumn[0];
//			data['revNo'] = arrByColumn[1];
			gridData.push(data);
		});
		$('#'+searchAllGridId).jqGrid('setGridParam',
				{
					datatype:'local',
					data: gridData,
					rowNum: 500
				})
				.trigger('reloadGrid');
	}
}


/**
 * row 선택 후 삭제
 * 삭제 한 뒤에 gridParam의 searchAllParam을 다시 세팅
 * @returns
 */
function deleteRow(){
	var selrow = $('#'+searchAllGridId).jqGrid('getGridParam', 'selarrrow');
	if(selrow.length == 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}
	for(i=selrow.length - 1; i>=0; i--){
		$('#'+searchAllGridId).jqGrid('delRowData', selrow[i]);
	}
	gridParam.searchAllParam = $('#'+searchAllGridId).jqGrid('getRowData');
}

/**
 * 전체 삭제
 * gridParam의 searchAllParam도 지운다
 * @returns
 */
function deleteAllRow(){
	$('#'+searchAllGridId).jqGrid('clearGridData');
	$('#objectId').val('');
	gridParam.searchAllParam = '';
}

$('#partialMatch').change(function(){
	if($('#partialMatch').is(":checked")){
		gridParam.useLike = 'Y';
//		$('#useLike').val('Y');
	}else{
		gridParam.useLike = 'N';
//		$('#useLike').val('N');
	}
})

