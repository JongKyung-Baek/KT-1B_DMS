var popupGridParam;
var gridId = 'gridCrRequestUploadPopup';
var emptyArray = [];
var files = [];
var drawingInfo;
var isInspection = false;

function applyCrPopupSelect2Theme() {
	var $popupParent = $('#popupDialoga');
	$('#crRequest select').each(function() {
		var $select = $(this);
		var optionCount = $select.find('option').length;
		var useSearch = optionCount >= 10;

		// Re-init select2 so dropdown is rendered under popup dialog container.
		if ($select.data('select2')) {
			$select.select2('destroy');
		}

		if (useSearch) {
			$select.select2({ dropdownParent: $popupParent });
		} else {
			$select.select2({ minimumResultsForSearch: -1, dropdownParent: $popupParent });
		}

		$select.off('select2-open.crTheme').on('select2-open.crTheme', function() {
			$('.select2-drop-active').addClass('drawing-like-dropdown');
			if (!useSearch) {
				$('.select2-drop-active').addClass('register-user-no-search');
			}
		});
	});
}

function fileUpload(){
	$('#crFile').click();
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function fileAppendEvent(){
    for (var i = 0; i < $('#crFile').get(0).files.length; ++i) {
    	var fileName = $('#crFile').get(0).files[i].name;
    	if(checkFileName(fileName)){
    		var rowid = guid() + i;
    		files.push({
    				id: rowid,
    				file : $('#crFile').get(0).files[i]
    		});
    		$('#'+gridId).jqGrid('addRowData', rowid, {fileName: fileName}, 'last');
    	}
    }
    $('#listCount').text($('#'+gridId).jqGrid('getGridParam', 'reccount'));
    dialogToolbarWidth()
}

function checkFileName(fileName){
	var fileExtIndex = fileName.lastIndexOf('.');
	var fileEndIndex = fileName.length;
	var fileExt = fileName.substring(fileExtIndex + 1, fileEndIndex);
//	if(fileExt != 'xls' && fileExt != 'xlsx'){
//		alertMessage(g_msg('msg.noExcelFile'));
//		return false;
//	}
	var ids = $('#'+gridId).jqGrid('getDataIDs');
	for(i=0; i < ids.length; i++){
		var data = $('#'+gridId).jqGrid('getRowData', ids[i]);
		if(data.fileName === fileName){
			alertMessage(g_msg('msg.duplicateFileNm'));
			return false;
		}
	}
	return true;
}

function fileDelete(){
	var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
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
		$('#'+gridId).jqGrid('delRowData', selrow[i]);
	}
	$('#listCount').text($('#'+gridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}


function inspection(){
	if(!isValidBusinessArea()){
		return;
	}
	var param = {
			businessAreaCd : $('#businessAreaCd').val(),
			partNo : $('#partNo').val()
	};
	callAjax(param, '/outside/cr/request/getDrawingInfo', requestCallback, 'json');
}

function requestCallback(response){
	if(response.length === 0){
		alertMessage('해당 품번이 없습니다.');
		return;
	}else{
		drawingInfo = response[0];
		$('#partNo').val(response[0].partNo);
		$('#materialNo').val(response[0].materialsCd);
		$('#revNo').val(response[0].revNo);
		$('#drawingNo').val(response[0].drawingNo);
		$('#drawingNm').val(response[0].drawingNm);
		$('#drawingInsertDt').val(response[0].crDrawingInsertDt);
		$('#insertDt').val(response[0].insertDt);
		$('#partNm').val(response[0].partNm);
		$('#stdPartNo').val(response[0].stdPartNo);
		$('#productNo').val(response[0].productCd).trigger("change");

		isInspection = true;
	}

}

$("#vendorUid").on("change", function(){
	var param = {
			userId: $("#vendorUid").val()
	};
	if($('#vendorUid').val() != ''){
		callAjax(param, '/outside/cr/request/selectUserInfo', setUserEmail, 'json');
	}else{
		$("#vendorEmailNm").val('');
	}

});

function setUserEmail(response){
	$("#vendorEmailNm").val(response.email);
	$("#vendorUidNm").val($("#vendorUid option:selected").text());
}

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
 * 사업장 변경 시 구매담당자 목록을 새로 불러오기 위한 이벤트
 * 구매담당자 리스트를 갱신하기 위해 서버를 호출하고, 목록 데이터를 초기화 합니다.
 * @returns
 */
$("#businessAreaCd").on("change", function(){
	loadAcceptanceUser();
	drawingInfo = null;
	$('#partNo').val('');
	$('#materialNo').val('');
	$('#revNo').val('');
	$('#drawingNm').val('');
	$('#drawingInsertDt').val('');
	$('#productNo').val('');
	isInspection = false;
	// 화면정보와 연관된 form data 초기화 필요
});


/**
 * 사업장 변경 시 구매담당자 리스트를 불러오기 위해 호출되는 함수
 * @returns
 */
function loadAcceptanceUser(){
	var businessAreaVal = $("#businessAreaCd").val();
	var param = {
			businessAreaCd : businessAreaVal
			};
	callAjax(param, "/outside/cr/request/getAcceptanceUser", setPurchaserUid, 'json');
}

/**
 * 구매담당자 목록 조회 결과를 구매담당자 selectBox에 세팅하는 함수
 * @param response
 * @returns
 */
function setPurchaserUid(response){
	$("#purchaserUid").empty().trigger('change');
	var newOption;
	var $purchaserUid = $("#purchaserUid");
	$.each(response, function(index, data){
		newOption = new Option(data.comboLabel, data.comboVal, false, data.selectedValue == data.comboVal);
		$purchaserUid.append(newOption);
	});
	$purchaserUid.select2('destroy');
	$purchaserUid.select2({minimumResultsForSearch: 1, dropdownParent: $('#popupDialoga')});
	$purchaserUid.off('select2-open.crTheme').on('select2-open.crTheme', function() {
		$('.select2-drop-active').addClass('drawing-like-dropdown');
	});
	$purchaserUid.trigger('change');
}

function crTemplateDownload(){
	$("form[name=tmpForm]")
	.attr("action","/outside/cr/request/getCrTemplate")
	.attr("target", "hiddenFrame")
	.submit();
//	location.href="/outside/cr/request/getCrTemplate";
}


/**
/**
 * CR 요청 validation 체크
 * 업체명, 담당자 Email, 사업장, 구매담당자 선택 여부를 체크합니다.
 */
function isValidation(){
	if($.trim($("#vendorUid").val()) === ""){
		isValidDataEmpty("vendorUid", "form.vendorUser");
		return false;
	}
//	if($.trim($("#vendorEmailNm").val()) === ""){
//		isValidDataEmpty("vendorEmailNm", "form.email");
//		return false;
//	}
	
	if($.trim($("#businessAreaCd").val()) === ""){
		isValidDataEmpty("businessAreaCd", "form.businessArea");
		return false;
	}
	if($("#vendorUid option:selected").text() === $("#approvalUser option:selected").text()){		
		alertMessage(g_msg("msg.crRequestFailure"));
		return false;
	}
	if($.trim($("#purchaserUid").val()) === ""){
		isValidDataEmpty("purchaserUid", "form.approvalUser");
		return false;
	}
	if($.trim($("#crTypeCd").val()) === ""){
		isValidDataEmpty("crTypeCd", "form.crTypeDesc");
		return false;
	}
	if($.trim($("#productNo").val()) === ""){
		isValidDataEmpty("productNo", "form.productCd");
		return false;
	}
	
	// 개발/양산 체크
	var strProductNo = $("#productNo option:selected").text();
	if(strProductNo.indexOf("[개발]") > 0){
		alertMessage(g_msg('msg.prodCheck')); // 양산 기종만 CR요청 가능합니다.
		return false;
	}
	
	if($.trim($("#deviceNm").val()) === ""){
		 if($.trim($("#businessAreaCd").val()) === "1210"){
			isValidDataEmpty("deviceNm", "form.deviceNm");
			return false;
		 }
	}
	if($.trim($("#crTitleNm").val()) === ""){
		isValidDataEmpty("crTitleNm", "form.title");
		return false;
	}
	if($.trim($("#asIsDesc").val()) === ""){
		isValidDataEmpty("asIsDesc", "form.asIsDesc");
		return false;
	}
	if($.trim($("#toBeDesc").val()) === ""){
		isValidDataEmpty("toBeDesc", "form.toBeDesc");
		return false;
	} 
	if(!isInspection){
		alertMessage(g_msg('msg.noInspectedItem'));
		return false;
	}
	if(files.length == 0){
		alertMessage(g_msg('msg.noCrFile'));
		return false;
	}
	return true;
}


function requestCr(){
	if(!isValidation()){
		return;
	}
	var param = new FormData();
	$.each(files, function(index, data){
		param.append('files', data.file);
	})
	param.append('crParam', JSON.stringify($('#crRequest').serializeObject()));
//	param.append('drawingInfo', JSON.stringify(drawingInfo));
	callAjaxUpload(param, "/outside/cr/request/crRequest", requestCrCallback);
}

/**
/**
 * CR 요청 결과 메시지 출력
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
function gridComplete(){
	$('#listCount').text($('#'+gridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}

/**
 * 저장 버튼 클릭 전 먼저 사업장 선택 여부
 * @returns
 */
function isValidBusinessArea(){
	if($.trim($("#businessAreaCd").val()) === ""){
		alertMessage(g_msg('msg.selectBusinessArea'));
		return false;
	}
	return true;
}

$(document).ready(function(){
	applyCrPopupSelect2Theme();
});
