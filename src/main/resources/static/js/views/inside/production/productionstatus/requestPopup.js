var nameColumnWidth;
var businessAreaCd;
var businessAreaNm;
var acceptanceUserMaxCnt = 5;


function setGridProductionParam(){
	var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
	var postData = [];
	for(i=0; i<selrow.length; i++){
		let objectNo = $('#'+gridId).jqGrid('getCell', selrow[i], 'hiddenObjectNo');
		let deptCd = $('#'+gridId).jqGrid('getCell', selrow[i], 'deptCd');
		let data = {objectNo: objectNo, deptCd: deptCd};
		postData.push(data);
	}
	var objectType = $('#'+gridId).jqGrid('getCell', selrow[0], 'objectType');
	gridProductionParam = {
			gridId:  gridProductionId,
			url: "/inside/production/productionstatus/selectProductionReplace",
			numbering: false,
			postData: {param: JSON.stringify(postData), objectType: objectType},
			cellEdit: true,
			cellsubmit: 'clientArray'
	}
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
	if(cellname == 'destroyCount'){
		var currentCount = $('#'+gridProductionId).jqGrid('getCell', rowid, 'currentCount');
		if(Number(value) > Number(currentCount)){
			value = currentCount;
			$('#'+gridProductionId).jqGrid('setCell', rowid, 'destroyCount', value);
		}
	}
	if(cellname == 'copy'){
		calcTotalCount();
	}
}

function calcTotalCount(){
	var ids = $('#'+gridProductionId).jqGrid('getDataIDs');
	$.each(ids, function(index, data){
		var deployTotalCnt = Number($('#'+gridProductionId).jqGrid('getCell', data, 'deployCount'));
		var copy = Number($('#'+gridProductionId).jqGrid('getCell', data, 'copy'));
		$('#'+gridProductionId).jqGrid('setCell', data, 'totalCount', deployTotalCnt * copy);
	})
}



function gridComplete(){
	$('#productionListCount').text($('#'+gridProductionId).jqGrid('getGridParam', 'reccount'));
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
	var ids = $('#'+gridProductionId).jqGrid('getDataIDs');
	for(i=0; i<ids.length; i++){
		console.log($('#'+gridProductionId).jqGrid('getCell', ids[i], 'deployCount'));
		if($('#'+gridProductionId).jqGrid('getCell', ids[i], 'deployCount') == 0){
			alertMessage(g_msg('msg.inputDeployCount'));
			return false;
		}
		if($('#'+gridProductionId).jqGrid('getCell', ids[i], 'copy') == 0){
			alertMessage(g_msg('msg.inputCopy'));
			return false;
		}
	}
	return true;
}

function deployRequest(){
	var isSuccess=true;
	if(!isValidation()){
		return false;
	}
	var ids = $('#'+gridProductionId).jqGrid('getDataIDs');
	let replaceList = [];
	$.each(ids, function(index, data){
		var dataChk = $("#" + gridProductionId).jqGrid('getRowData', data);
		if("N" == dataChk.deployAcceptYn) {
			isSuccess=false;
			alertMessage(g_msg('msg.isNoDeployAccept')+"<br>문서번호:"+dataChk.objectNo+"<br>접수자:"+dataChk.userNm, function(){  //승인요청 할 수 없습니다.<br>배포 미접수 건이 존재합니다.
				$(this).dialog("close");
			});
			return false;
		}
		replaceList.push($('#'+gridProductionId).jqGrid('getRowData', data));
	})
	var param = $("#requestInfo").serializeObject();
	param.businessAreaCd = businessAreaCd;
	param.deployType = 'REPLACE';
	param.watermarkYn = $("input[name=watermarkYn]:checked").attr('value');
	param.watermarkDeployDtYn = $("input[name=watermarkDeployDtYn]:checked").attr('value');
	param.replaceList = replaceList;
	if(isSuccess){
		confirmMessage(g_msg('confirm.productionRequest'), function(){
			$("#confirmMessage").dialog("close");
			runDeploy(param)
		});
	}
}

function runDeploy(param){
	callAjax(param, '/inside/production/productionstatus/distributionRequest', requestCallback, 'json');
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