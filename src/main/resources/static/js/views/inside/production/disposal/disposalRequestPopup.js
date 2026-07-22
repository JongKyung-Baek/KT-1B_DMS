/**
 * 생산기술 자료 폐기 시 필수 입력 내용 체크
 * @returns
 */
function validate(){
	if(!isValidObjEmpty($("#requestDesc"), 'form.destroyDesc')) return false;
	return true;
}


/**
 * 생산기술 자료 페기 승인 요청
 * @returns
 */
function approvalRequest(){

	if(!validate()) {
		return false;
	}
	var param = $("#formDisposalRequestPopup").serializeObject();
	param.businessAreaCd = $('#'+gridParam.gridId).jqGrid('getCell', 1, 'businessAreaCd');
	var aJsonArray = new Array();
	console.log(param);
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		aJsonArray.push(data);
	});
	param.list = aJsonArray;
	callAjax(param, '/inside/production/disposal/destroyRequest', disposalRequestCallback);
}


/**
 * 요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function disposalRequestCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){		//요청이 완료되었습니다.
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));					//요청이 실패했습니다
	}
}

function toggleCheckboxValue(checkboxId) {
	var checkbox = document.getElementById(checkboxId);
	checkbox.value = checkbox.checked ? 'Y' : 'N';
	var sendEmailYn = document.getElementById("sendEmailYn");
	sendEmailYn.value = checkbox.value;
	console.log(sendEmailYn.value);
}