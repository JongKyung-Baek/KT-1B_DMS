
function validate(){
	if(!isValidObjEmpty($("#requestDesc"), 'form.applyReason')) return false;
	/*
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
			var param = $("#destoryRequest").serializeObject();
			callAjax(param, '/inside/distribution/commonRequest/verificationProtectUser', protectUserCallback);
			
	//		if(twoDevelopment){
	//			alertMessage(g_msg("msg.correctlySelectProtect"), function() {	// 방산기술보호 책임자를 올바르게 선택해 주시기 바랍니다.
	//				$(this).dialog("close");
	//			});
	//			return false;
	//		}
		}else{
			if(1 == production) {
				$("#productProtectUid").val($('#protectUserUid').val());
			}else if( 1 == development ) {
				$("#developmentProtectUserUid").val($('#protectUserUid').val());
			}
			save("one");
		}
	}else{
		save("no");
	}
	*/
	
	
	return true;
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
	
	var param = $("#destoryRequest").serializeObject();
	if("one" == type){
		param.protectUserUid = null;
	}
	var businessAreaCd;
	var jsonArr = new Array();
	//그리드에서 선택된 항목 가져오기
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		var aJson = new Object();
		aJson.requestNo = data.requestNo;
		aJson.objectId = data.objectId;
		aJson.protectYn = data.protectYn;
		aJson.businessTypeCd = data.businessTypeCd;
		businessAreaCd = data.businessAreaCd;
		jsonArr.push(aJson);
	});
	param.list = jsonArr;
	param.businessAreaCd = businessAreaCd;
	callAjax(param, '/inside/distribution/printHistory/destroyRequest', function(){
		alertMessage(g_msg('msg.requestComplete'), function(){			//요청이 완료됐습니다
			searchList(gridParam);
			$(this).dialog("close");
			closePopup('popupDialog');
		});
	})
	
}


/**
 * 출력물 폐기 승인 요청
 * @returns
 */
function destroyRequest(){
	
	if(!validate()) {
		return false;
	}
	
	var param = $("#destoryRequest").serializeObject();
	var businessAreaCd;
	var jsonArr = new Array();
	//그리드에서 선택된 항목 가져오기
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		var aJson = new Object();
		aJson.requestNo = data.requestNo;
		aJson.objectId = data.objectId;
		aJson.protectYn = data.protectYn;
		aJson.businessTypeCd = data.businessTypeCd;
		businessAreaCd = data.businessAreaCd;
		jsonArr.push(aJson);
	});
	param.list = jsonArr;
	param.businessAreaCd = businessAreaCd;
	callAjax(param, '/inside/distribution/printHistory/destroyRequest', function(){
		alertMessage(g_msg('msg.requestComplete'), function(){			//요청이 완료됐습니다
			$(this).dialog("close");
			closePopup('popupDialog');
		});
	})
}


function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'DISTRIBUTION\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\')">'+cellValue+'</a>';
}