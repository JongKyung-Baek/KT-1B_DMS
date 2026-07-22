var nameColumnWidth;
var businessAreaCd;
var businessAreaNm;
var acceptanceUserMaxCnt = 5;

function setGridAcceptanceParam(){
	gridAcceptanceParam = {
			gridId: gridAcceptanceId,
			multiSelect : true,
			numbering: false,
			cellEdit: true,
			cellsubmit: 'clientArray'
	};
}

function setGridProductionParam(){
	var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
	var data = [];
	businessAreaCd = $('#'+gridId).jqGrid('getCell', selrow[0], 'businessAreaCd');
	businessAreaNm = $('#'+gridId).jqGrid('getCell', selrow[0], 'businessAreaNm');
	for(i=0; i<selrow.length; i++){
//		totalPageCnt += parseInt($('#'+gridId).jqGrid('getCell', selrow[i], 'totalPageCnt'));
		var gridData = $('#'+gridId).jqGrid('getRowData', selrow[i]);
		gridData.deployCount = gridData.totalPageCnt;
		data.push(gridData);
	}
	gridProductionParam = {
			gridId:  gridProductionId,
			numbering: false,
			data: data,
			cellEdit: true,
			cellsubmit: 'clientArray'
	}
	$('#productionListCount').text(selrow.length);
}
/**
 * row를 편집 시 호출되는 event함수 (그리드 클릭 시 input창으로 변경되면서 호출된다 )
 * input에서 focus가 벗어나면 자동으로 saveCell이 실행된다.
 * @param rowid
 * @param cellname
 * @param value
 * @param iRow
 * @param iCol
 * @returns
 */
function afterEditCellFunc(rowid, cellname, value, iRow, iCol){

	$('#'+iRow+'_'+cellname).bind('blur', function(){
		$('#'+gridProductionId).saveCell(iRow, iCol);
	});
	$('#'+iRow+'_'+cellname).bind('blur', function(){
		$('#'+gridAcceptanceId).saveCell(iRow, iCol);
	});
}

function afterSaveCellFunc(rowid, cellname, value, iRow, iCol){
	if(cellname == 'deployCount'){
		var totalPageCnt = $('#'+gridProductionId).jqGrid('getCell', rowid, 'totalPageCnt');
		if(Number(value) > Number(totalPageCnt)){
			value = totalPageCnt;
			$('#'+gridProductionId).jqGrid('setCell', rowid, 'deployCount', value);
		}
		calcTotalCount();
	}
	if(cellname == 'copy'){
		calcTotalCount();
	}
}

function calcTotalCount(){
	var ids = $('#'+gridProductionId).jqGrid('getDataIDs');
	var deployTotalCnt = 0;
	$.each(ids, function(index, data){
		deployTotalCnt += Number($('#'+gridProductionId).jqGrid('getCell', data, 'deployCount'));
	})
	var userIds = $('#'+gridAcceptanceId).jqGrid('getDataIDs');
	$.each(userIds, function(index, data){
		var copy = Number($('#'+gridAcceptanceId).jqGrid('getCell', data, 'copy'));
		$('#'+gridAcceptanceId).jqGrid('setCell', data, 'totalCount', deployTotalCnt * copy);
	})
}



function gridComplete(){
	$('#acceptanceListCount').text($('#'+gridAcceptanceId).jqGrid('getGridParam', 'reccount'));
}


/**
 * 배포처 목록 추가
 * @returns
 */
function addRow(){
	var id = $('#'+gridAcceptanceId).jqGrid('getGridParam', 'reccount');

	if(id >= acceptanceUserMaxCnt){
		alertMessage(g_msg('msg.maxCnt'));
		return;
	}
	$('#'+gridAcceptanceId).jqGrid('addRowData', id+1, {businessAreaCd: businessAreaNm, deployCount: 0}, 'first');
	setCheck(id+1);

}

function setCheck(id){
	var selectId = "#" + id + "_userCd";
	console.log("setCheck");
	initSelectRemoteData(selectId, "/inside/authorization/selectJikUserComboList");
	$(selectId).on('select2:select', function(e){selectBoxChange(e)});

	$('#'+gridAcceptanceId).jqGrid('setSelection', id);
	$("#jqg_" + gridAcceptanceId + "_" + id).each(function() {
		$(this).prettyCheckable("check");
	});
}
function deleteRow(){
	var selrow = $('#'+gridAcceptanceId).jqGrid('getGridParam', 'selarrrow');
	if(selrow.length == 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}
	for(i=selrow.length - 1; i>=0; i--){
		$('#'+gridAcceptanceId).jqGrid('delRowData', selrow[i]);
	}
}
/* 그리드에 selectbox 추가 */
//function formatSelectBox(cellValue, options, rowdata, action){
//	var param = {
//			businessAreaCd: businessAreaCd
//	}
//	var rtn;
//	callAjax(param, "/inside/authorization/selectDeptComboListByBusinessArea", function(response){
//		rtn = selectBoxCallback(response, options);
//	}, 'json', false);
//	return rtn;
//}

function formatUserSelectBox(cellValue, options, rowdata, action){
//	var param = {
//			businessAreaCd: businessAreaCd
//	}
//	var rtn;
//	callAjax(param, "/inside/authorization/selectJikUserComboList", function(response){
//		rtn = selectBoxCallback(response, options);
//	}, 'json', false);
//	return rtn;

	console.log(options.rowId);
	console.log(rowdata);

	return '<select id="' + options.rowId + '_userCd" style="width: 90%;"></select>';
}

function selectBoxChange(e){
	var id = e.target.id;
	var data = e.params.data;
	if(data.id === '')data.id = null;
	var rowid = id.replace('_userCd', '');

	callAjax({userCd: data.id}, '/inside/authorization/selectDept', function(response) {
		$("#" + gridAcceptanceId + " #" + rowid + " td").each(function() {
			if(gridAcceptanceId + "_deptNm" === $(this).attr("aria-describedby")) {
				$(this).text(response.comboLabel);
				$('#'+gridAcceptanceId).jqGrid('setCell', rowid, 'deptCd', response.comboVal);
			}
		});
	}, 'json');

	$('#'+gridAcceptanceId).jqGrid('setCell', rowid, 'inspectionYn', 'Y');
	$('#'+gridAcceptanceId).jqGrid('setCell', rowid, 'userCd', data.id);
	$('#'+gridAcceptanceId).jqGrid('setCell', rowid, 'userNmView', data.text);
	
}

function setComboList(response, targetId){
	var newOption;
	$("#"+targetId).empty().trigger('change');
	newOption = new Option(g_msg('form.select'), '', false, false);
	$("#"+targetId).append(newOption).trigger('change');
	$.each(response, function(index, data){
		newOption = new Option(data.comboLabel, data.comboVal, false, false);
		$("#"+targetId).append(newOption).trigger('change');
	});
}


/**
 * 배포요청 시 validation체크
 * 용도, 업체명/담당자, Email, 사업장, 구매담당자가 선택되었는지 체크한다
 * @returns
 */
function isValidation(){
	if($.trim($("#teamLeader").val()) === ""){
		isValidDataEmpty("teamLeader", "form.authorizePerson");
		return false;
	}
	if($('#'+gridAcceptanceId).jqGrid('getRowData').length == 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return false;
	}else{
		let selIds = $('#'+gridAcceptanceId).jqGrid('getGridParam', 'selarrrow');
		for(i=0; i<selIds.length; i++){
			if($('#'+gridAcceptanceId).jqGrid('getCell', selIds[i], 'userCd') === ""){
				alertMessage(g_msg('msg.noSelectedUser'));
				return false;
			}
		}
		var ids = $('#'+gridAcceptanceId).jqGrid('getDataIDs');
		var uniqueArr = [];
		for(i=0; i<ids.length; i++){
			if($.inArray($('#'+gridAcceptanceId).jqGrid('getCell', ids[i], 'userCd'), uniqueArr) === -1){
				uniqueArr.push($('#'+gridAcceptanceId).jqGrid('getCell', ids[i], 'userCd'));
			}else{
				alertMessage(g_msg('msg.duplicateUser'));
				return false;
			}
			if($.inArray($('#'+gridAcceptanceId).jqGrid('getCell', ids[i], 'deptCd'), uniqueArr) === -1){
				uniqueArr.push($('#'+gridAcceptanceId).jqGrid('getCell', ids[i], 'deptCd'));
			}else{
				alertMessage(g_msg('msg.duplicateDept'));
				return false;
			}
			if('1310' === currentBusinessArea){
				if($('#'+gridAcceptanceId).jqGrid('getCell', ids[i], 'copy') === undefined || $('#'+gridAcceptanceId).jqGrid('getCell', ids[i], 'copy') === 0){
					alertMessage(g_msg('msg.inputCopy'));
					return false;
				}
			}
		}
	}
	return true;
}

function deployRequest(){
	var isSuccess=true;
	if(!isValidation()){
		return false;
	}
	var param = $("#requestInfo").serializeObject();
	var ids = $('#'+gridAcceptanceId).jqGrid('getDataIDs');
	var ids2 = $('#'+gridProductionId).jqGrid('getDataIDs');
	var acceptanceList = [];
	for(i=1; i<=ids.length; i++){
		if($('#'+gridAcceptanceId).jqGrid('getCell', i, 'inspectionYn') == 'Y'){
			acceptanceList.push($('#'+gridAcceptanceId).jqGrid('getRowData', i));
		}
	}
	
	$.each(ids, function(index, data){
		var dataAccept = $("#" + gridAcceptanceId).jqGrid('getRowData', data);
		$.each(ids2, function(index, data){
			var dataProduct = $("#" + gridProductionId).jqGrid('getRowData', data);
			var param = {
					  objectNo: dataProduct.objectNo
					, deployUserCd: dataAccept.userCd
					, deployDeptCd: dataAccept.deptCd
			}
			callAjax(param, '/inside/production/request/selectDeployAcceptCheck', function(response){
				if(!response.success) {
					isSuccess=false;
					alertMessage(g_msg('msg.isNoDeployAccept')+"<br>문서번호:"+dataProduct.objectNo+"<br>접수자:"+dataAccept.userNmView, function(){  //승인요청 할 수 없습니다.<br>배포 미접수 건이 존재합니다.
						$(this).dialog("close");
					});
					return false;
				}
			},'json',false);
		})
	})
	
	param.businessAreaCd = businessAreaCd;
	param.deployType = 'NEW';
	param.watermarkYn = $("input[name=watermarkYn]:checked").attr('value');
	param.watermarkDeployDtYn = $("input[name=watermarkDeployDtYn]:checked").attr('value');
	param.acceptanceList = acceptanceList;
	param.productionList = $('#'+gridProductionId).jqGrid('getGridParam', 'data');

	if(isSuccess){
		confirmMessage(g_msg('confirm.productionRequest'), function(){
			$("#confirmMessage").dialog("close");
			runDeploy(param)
		});
	}
}

function runDeploy(param){
	callAjax(param, '/inside/production/request/distributionRequest', requestCallback, 'json');
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else if(response.failReason === 'destroyInProgress'){
		alertMessage(g_msg("msg.destroyInProgress", response.data));
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}


$('input[type=radio][name=deployType]').change(function(){
	if(this.id == 'REPLACE'){
		$('#'+gridProductionId).jqGrid('setColProp', 'destroyCount', {editable: true});
	}else{
		$('#'+gridProductionId).jqGrid('setColProp', 'destroyCount', {editable: false});
	}
})

function toggleCheckboxValue(checkboxId) {
	var checkbox = document.getElementById(checkboxId);
	checkbox.value = checkbox.checked ? 'Y' : 'N';
	var sendEmailYn = document.getElementById("sendEmailYn");
	sendEmailYn.value = checkbox.value;
	console.log(sendEmailYn.value);
}