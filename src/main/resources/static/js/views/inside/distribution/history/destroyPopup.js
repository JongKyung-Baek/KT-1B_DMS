/* 사용하지 않음. 외부 사용자가 요청하도록 변경됨
 * 2019.12.04. 윤정훈 */
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


//function inspection(){
//	var param = {
//			businessAreaCd : $('#businessAreaCd').val(),
//			drawingNo : $('#drawingNo').val()
//	};
//	callAjax(param, '/outside/cr/request/getDrawingInfo', requestCallback, 'json');
//}

//function requestCallback(response){
//	console.log(response);
//	if(response.length === 0){
//		alertMessage('해당 도면이 없습니다.');
//		return;
//	}else{
//		drawingInfo = response[0];
//		$('#partNo').val(response[0].partNo);
//		$('#materialNo').val(response[0].materialsCd);
//		$('#revNo').val(response[0].revNo);
//		$('#drawingNm').val(response[0].drawingNm);
//		$('#drawingInsertDt').val(response[0].drawingInsertDt);
//		isInspection = true;
//	}
//
//}

//$("#vendorUid").on("change", function(){
//	var param = {
//			userId: $("#vendorUid").val()
//	};
//	callAjax(param, '/outside/cr/request/selectUserInfo', setUserEmail, 'json');
//});

//function setUserEmail(response){
//	$("#vendorEmailNm").val(response.email);
//}

//$("#drawingNo").on("propertychange change keyup paste input", function() {
//    var currentVal = $(this).val();
//    if(currentVal == oldVal) {
//        return;
//    }
//
//    oldVal = currentVal;
//    alert("changed!");
//});


/**
 * 사업장 변경 시 구매담당자 리스트를 새로 불러오기 위한 이벤트
 * 구매담장자리스트를 갱신하기 위해 서버를 호출하고, 목록 데이터를 전부 삭제한다.
 * @returns
 */
//$("#businessAreaCd").on("change", function(){
//	loadAcceptanceUser();
//	drawingInfo = null;
//	$('#partNo').val('');
//	$('#materialNo').val('');
//	$('#revNo').val('');
//	$('#drawingNm').val('');
//	$('#drawingInsertDt').val('');
//	isInspection = false;
//	//도면정보에 대한 form data들 초기화 필요
//});

//$("#crTypeCd").on("change", function(){
//	$('#crTypeDesc').val($('#crTypeCd option:selected').text());
//})

/**
 * 사업장 변경 시 구매담당자리스트를 불러오기 위해 호출되는 함수
 * @returns
 */
//function loadAcceptanceUser(){
//	var businessAreaVal = $("#businessAreaCd").val();
//	var param = {
//			businessAreaCd : businessAreaVal
//			};
//	callAjax(param, "/outside/cr/request/getAcceptanceUser", setPurchaserUid, 'json');
//}

/**
 * 구매담당자 리스트 호출 후 결과값을 구매담당자 selectBox에 세팅하는 함수
 * @param response
 * @returns
 */
//function setPurchaserUid(response){
//	var newOption;
//	$.each(response, function(index, data){
//		newOption = new Option(data.comboLabel, data.comboVal, false, false);
//	});
//
//	$("#purchaserUid").empty().trigger('change');
//	$("#purchaserUid").append(newOption).trigger('change');
//}

//function crTemplateDownload(){
//	location.href="/outside/cr/request/getCrTemplate";
//}


/**
 * 배포요청 시 validation체크
 * 용도, 업체명/담당자, Email, 사업장, 구매담당자가 선택되었는지 체크한다
 * @returns
 */
function isValidation(){
	if($.trim($("#destroyDesc").val()) === ""){
		isValidDataEmpty("destroyDesc", "form.destroyDesc");
		return false;
	}

	if(files.length == 0){
		alertMessage(g_msg('msg.noFile'));
		return false;
	}
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
	$.each($("#gridDistributionHistoryList").getGridParam('selarrrow'), function(index, item){
		var data = $("#gridDistributionHistoryList").jqGrid('getRowData', item);
		var destroyItem = {
				requestNo : data.requestNo
			  , objectId : data.objectId
		}
		aJsonArray.push(destroyItem);
	});
	param.append('list', JSON.stringify(aJsonArray));


//	param.append('drawingInfo', JSON.stringify(drawingInfo));
	callAjaxUpload(param, "/inside/distribution/history/destory", requestCrCallback);
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
