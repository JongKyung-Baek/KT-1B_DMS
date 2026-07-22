function printRequestPopup(){
	
	if(!validate()) {
		return false;
	}

	var param = $("#formPrintRequestPopup").serializeObject();
	var aJsonArray = new Array();
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		var aJson = new Object();
		aJson.objectId = data.objectId;
		aJson.objectType = data.objectType;
		aJsonArray.push(aJson);
	});
	param.printRequestList = aJsonArray;
	callAjax(param, '/inside/production/request/printRequest', requestCallback, 'json');
}


function validate(){
	if(!isValidObjEmpty($("#requestDesc"), 'form.applyReason')) return false;
	return true;
}

function requestCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.acceptanceSuccess'), function(){		//접수가 완료되었습니다.
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.acceptanceFailure"));				//접수가 실패했습니다.
	}
}

function toggleCheckboxValue(checkboxId) {
	var checkbox = document.getElementById(checkboxId);
	checkbox.value = checkbox.checked ? 'Y' : 'N';
	var sendEmailYn = document.getElementById("sendEmailYn");
	sendEmailYn.value = checkbox.value;
	console.log(sendEmailYn.value);
}