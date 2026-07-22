$(function() {
	
	//용도 selectBox 변경시 유효기간 변경
	$('#requestPurpose').on('select2:select', function (e) {
		switch(e.params.data.id){
			case 'CHECK' :
			case 'REFER' :
				$("#deployTerm").val('1');
				break;
			case 'ESTIMATE' :
				$("#deployTerm").val('3');
				break;
			case 'MANUAL' :
			case 'PRODUCT' :
				$("#deployTerm").val('6');
				break;
			default :
				break;
		}
	});

	//용도 변경 시 숫자 validation
	$("#deployTerm").on("change paste", function() {
		var regex = /^[1-9]{1}$|^[1-4]{1}[0-9]{1}$|^60$/
	    if(!regex.test($(this).val())){
	    	alertMessage(g_msg('label.onlyNumberOneToSixty'), function(){
	    		$(this).dialog("close");
	    	});
	    	$("#deployTerm").val('');
	    	$("#deployTerm").focus();
	    }
	});

});

$("#receiveUserCd").on("change", function(){
	var param = {
			userCd: $("#receiveUserCd").val()
	};
	if($('#receiveUserCd').val() != ''){
		callAjax(param, "/outside/commonRequest/selectUserInfo", setUserEmail, 'json');
	}else{
		$("#receiveUserEmail").val('');
	}
});

function setUserEmail(response){
	$("#receiveUserEmail").val(response.email);
}


function validate(){
	if(!isValidObjEmpty($("#deployUser"), 'form.companyUser')) return false;
	if(!isValidObjEmpty($("#receiveUserCd"), 'form.receiveUser')) return false;
	if(!isValidObjEmpty($("#receiveUserEmail"), 'form.email')) return false;
	if(!isValidObjEmpty($("#requestPurpose"), 'form.purpose')) return false;
	if(!isValidObjEmpty($("#deployTerm"), 'form.validateTerm')) return false;
	if(!isValidObjEmpty($("#transDesc"), 'form.transDesc')) return false;
	return true;
}


/**
 * 배포 승인 요청
 * @returns
 */
function save(){
	if(!validate()) {
		return false;
	}

	var param = $("#formRequestPopup").serializeObject();

	var jsonArr = [];
	//그리드에서 선택된 항목 가져오기
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		// var aJson = new Object();
		// aJson.objectNo = data.objectNo;
		jsonArr.push(data);
	});
	param.list = jsonArr;

	callAjax(param, '/outside/outregisted/request/distributionRequest', function(){
		alertMessage(g_msg('msg.requestComplete'), function(){			//요청이 완료됐습니다
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	})
}

function toggleCheckboxValue(checkboxId) {
	var checkbox = document.getElementById(checkboxId);
	checkbox.value = checkbox.checked ? 'Y' : 'N';
	var sendEmailYn = document.getElementById("sendEmailYn");
	sendEmailYn.value = checkbox.value;
	console.log(sendEmailYn.value);
}