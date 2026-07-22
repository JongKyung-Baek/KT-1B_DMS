$(function() {

	function formatDateByOffset(monthOffset) {
		var d = new Date();
		d.setMonth(d.getMonth() + monthOffset);
		var y = d.getFullYear();
		var m = ('0' + (d.getMonth() + 1)).slice(-2);
		var day = ('0' + d.getDate()).slice(-2);
		return y + '-' + m + '-' + day;
	}

	var today = formatDateByOffset(0);
	var maxDate = formatDateByOffset(3);
	var defaultDate = getDateValue('+1month');
	$('#destroyTodoYmd').attr('min', today).attr('max', maxDate);
	if (!$('#destroyTodoYmd').val()) {
		$('#destroyTodoYmd').val(defaultDate);
	}
	
	//용도 selectBox 변경시 폐기기간 변경
	$('#requestPurpose').on('select2:select', function (e) {
		if('SUBMIT'===e.params.data.id){
			$("#destroyTodoYmd").val('');
			$("#destroyTodoYmd").prop('disabled', true);
//			$("#destroyTodoYmd").attr("disabled",true);
		}else if('CHECK'===e.params.data.id){
			$("#destroyTodoYmd").val(getDateValue('+1month'));
			$("#destroyTodoYmd").prop('disabled', false);
		}
	});

});


function validate(){
	var development=0;
	var production=0;
	var protectUserCnt = 0;
	var protectYn=false;
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		if( ("Production" == data.businessTypeCd) && (0 == production) ) {
			production = 1;
		}
		if( ("Development" == data.businessTypeCd) && (0 == development) ) {
			development = 1;
		}
		if( "Y" == data.protectYn){
			protectYn = true;
		}
	});
	if(protectYn){
		protectUserCnt = production + development;
		if($('#protectUserUid').select2('data').length < protectUserCnt){	//개발
			alertMessage("방산기술보호 책임자를 " + protectUserCnt + "명 지정해야합니다.", function() {
				$(this).dialog("close");
			});
			return false;
		}

		//개발 두명 고르는건지 확인
		if(protectUserCnt == 2){
			var param = $("#printRequestForm").serializeObject();
			callAjax(param, '/inside/distribution/commonRequest/verificationProtectUser', protectUserCallback);
		}else{
			if(1 == production) {
				$("#productProtectUid").val($('#protectUserUid').val());
			}else if( 1 == development ) {
				$("#developmentProtectUserUid").val($('#protectUserUid').val());
			}
			save("one");
		}

//		if(twoDevelopment){
//			alertMessage(g_msg("msg.correctlySelectProtect"), function() {	// 방산기술보호 책임자를 올바르게 선택해 주시기 바랍니다.
//				$(this).dialog("close");
//			});
//			return false;
//		}
	}else{
		save("no");
	}







//	return true;
}



//방산기술보호 책임자를 두명 고를경우 에러
function protectUserCallback(response){
	if("twoDevelopment" == response.failReason){
		alertMessage(g_msg("msg.correctlySelectProtect"), function() {	// 방산기술보호 책임자를 올바르게 선택해 주시기 바랍니다.
			$(this).dialog("close");
		});
		return false;
	}else{
		save("two");
	}
}


function save(type){
	if(!isValidObjEmpty($("#requestDesc"), 'form.applyReason')) return false;

	var param = $("#printRequestForm").serializeObject();
	if("one" == type){
		param.protectUserUid = null;
	}
	var jsonArr = new Array();
	var businessAreaCd;
	//그리드에서 선택된 항목 가져오기
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		var aJson = new Object();
		jsonArr.push(data);
//		aJson.cnSerial = data.cnSerial;
//		aJson.objectId = data.objectId;
//		aJson.protectYn = data.protectYn;
//		aJson.businessTypeCd = data.businessTypeCd;
//		aJson.fileNo = data.fileNo;
//		businessAreaCd = data.businessAreaCd;
//		jsonArr.push(aJson);
	});

	param.businessAreaCd = businessAreaCd;
	param.list = jsonArr;
	param.approvalRequestType = 'PRINT';
	param.sendEmailYn = $('#sendEmailYn').val();

	console.log(param);

	callAjax(param, '/inside/distribution/commonRequest/approvalRequest', function(){
		alertMessage(g_msg('msg.requestComplete'), function(){			//요청이 완료됐습니다
			searchList(gridParam);
			$(this).dialog("close");
			closePopup('popupDialog');
		});
	})
}

/**
 * 배포 승인 요청
 * @returns
 */
function approvalRequest(){

	if(!validate()) {
		return false;
	}
//	var param = $("#printRequestForm").serializeObject();
//
//	var jsonArr = new Array();
//	var businessAreaCd;
//	//그리드에서 선택된 항목 가져오기
//	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
//		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
//		var aJson = new Object();
////		aJson.cnSerial = data.cnSerial;
//		aJson.objectId = data.objectId;
//		aJson.protectYn = data.protectYn;
//		aJson.businessTypeCd = data.businessTypeCd;
//		businessAreaCd = data.businessAreaCd;
//		jsonArr.push(aJson);
//	});
//
//	param.businessAreaCd = businessAreaCd;
//	param.list = jsonArr;
//	param.approvalRequestType = 'PRINT';

//	callAjax(param, '/inside/distribution/commonRequest/approvalRequest', function(){
//		alertMessage(g_msg('msg.requestComplete'), function(){			//요청이 완료됐습니다
//			$(this).dialog("close");
//			closePopup('popupDialog');
//		});
//	})
}
