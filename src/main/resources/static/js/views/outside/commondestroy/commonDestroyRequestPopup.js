var popupGridParam;
var popupGridId = 'gridDestroyUploadPopup';
var emptyArray = [];
var files = [];
//var drawingInfo;
//var isInspection = false;

function fileUpload(){
	$('#destroyFile').click();
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function fileAppendEvent(){
    for (var i = 0; i < $('#destroyFile').get(0).files.length; ++i) {
    	var fileName = $('#destroyFile').get(0).files[i].name;
    	if(checkFileName(fileName)){
    		var rowid = guid() + i;
    		files.push({
    				id: fileName + i,
    				file : $('#destroyFile').get(0).files[i]
    		});

    		$('#'+popupGridId).jqGrid('addRowData', rowid, {fileName: fileName}, 'first');
    	}
    }
    $('#listCount').text($('#'+popupGridId).jqGrid('getGridParam', 'reccount'));
    dialogToolbarWidth()
}

function checkFileName(fileName){
	var fileExtIndex = fileName.lastIndexOf('.');
	var fileEndIndex = fileName.length;
	var fileExt = fileName.substring(fileExtIndex + 1, fileEndIndex);
	if(fileExt != 'xls' || fileExt != 'xlsx'){
//		return false;
	}
	var ids = $('#'+popupGridId).jqGrid('getDataIDs');
	for(i=0; i < ids.length; i++){
		var data = $('#'+popupGridId).jqGrid('getRowData', ids[i]);
		if(data.fileName === fileName){
			alertMessage(g_msg('msg.duplicateFileNm'));
			return false;
		}
	}
	return true;
}

function fileDelete(){
	var selrow = $('#'+popupGridId).jqGrid('getGridParam', 'selarrrow');
	if(selrow.length == 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}

	for(i=selrow.length - 1; i>=0; i--){
		for(j=files.length -1 ; j>=0; j--){
			if(files[j].id == selrow[i]){
				files.splice(j, 1);
			}
		}
		$('#'+popupGridId).jqGrid('delRowData', selrow[i]);
	}
	$('#listCount').text($('#'+popupGridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}

/**
 * 배포요청 시 validation체크
 * @returns
 */
function isValidation(){
	if($.trim($("#destroyDesc").val()) === ""){
		isValidDataEmpty("destroyDesc", "form.destroyDesc");
		return false;
	}

	// if(files.length == 0){
	// 	alertMessage(g_msg('msg.noFile'));
	// 	return false;
	// }
	return true;
}

function saveDestroy(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	$.each(files, function(index, data){
		param.append('files', data.file);
	})
//	delete drawingInfo.sessionUser;
	param.append('destroyParam', JSON.stringify($('#formDestoryPopup').serializeObject()));

	var aJsonArray = new Array();
	$.each($("#" + g_gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#" + g_gridId).jqGrid('getRowData', item);
		var destroyItem = {
				requestNo : data.requestNo
			  , objectId : data.objectId
			  , objectNo : data.objectNo
			  , fileNo : data.fileNo
			  , businessAreaCd : data.businessAreaCd
		}
		aJsonArray.push(destroyItem);
	});
	console.log(JSON.stringify(aJsonArray));
	param.append('list', JSON.stringify(aJsonArray));
	callAjaxUpload(param, "/outside/commonDestroyRequest/destoryRequest", requestCrCallback);
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCrCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}