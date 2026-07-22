/**
 * 폐기중
 * @param response
 * @returns
 */
function underOutsideDestroy(gridId) {
	var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
	var isSuccess=true;
	if(selrow.length <= 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}
	
	$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#" + gridId).jqGrid('getRowData', item);
		businessAreaCd = data.businessAreaCd;
		if("1" == data.destroyStatusCd || "2" == data.destroyStatusCd || "3" == data.destroyStatusCd  ) {
			isSuccess=false;
			alertMessage(g_msg('msg.isNoUnderDestroy'), function(){		//폐기중 대상이 아닙니다. 확인 바랍니다
				$(this).dialog("close");
			});
			return false;
		}
	});
		
	if(isSuccess){
		confirmMessage(g_msg("msg.confirmUnderDestroy"), function(){ //폐기중으로 변경 하시겠습니까?
			$(this).dialog("close");
			
			$.each($("#"+ gridId).getGridParam('selarrrow'), function(index, item){
				var data = $("#" + gridId).jqGrid('getRowData', item);
				var param = {
						  requestNo: data.requestNo
						, objectId: data.objectId
						, fileNo: data.fileNo
				}
				//console.log(param);
				callAjax(param, '/outside/commonDestroyRequest/underDestroy', underDestroyCallback);
			});
		});
	}
}

/**
 * 폐기요청
 * @param response
 * @returns
 */
function requestOutsideDestroy(gridId, objectType) {
	var selrow = $('#'+gridId).jqGrid('getGridParam', 'selarrrow');
	var isSuccess=true;
	var destroyType='';
	var destroyTypeNm='';
	var businessAreaCd;
	if(selrow.length <= 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}

	$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#" + gridId).jqGrid('getRowData', item);
		businessAreaCd = data.businessAreaCd;
		if("1" != data.destroyStatusCd){
			isSuccess=false;
			alertMessage(g_msg('msg.isOkUnderDestroy'), function(){			//폐기중 상태인 항목만 선택 가능합니다.
				$(this).dialog("close");
			});
			return false;
		}
		/*
		if(destroyType != data.destroyType){
			isSuccess=false;
			alertMessage(g_msg('msg.differentDestroyType'), function(){			//서로 다른 폐기 유형을 선택할 수 없습니다.
				$(this).dialog("close");
			});
			return false;
		}
		*/
	});

	if(isSuccess){
//		var data = [];
//
//		for(i=0; i<selrow.length; i++) {
//			var rowdata = $("#" + gridId).jqGrid('getRowData', selrow[i]);
//			data.push({
//				requestNo : rowdata.requestNo
//				, objectId : rowdata.objectId
//				, fileNo: rowdata.fileNo
//			});
//		}
		var param = {
				destroyTypeNm: destroyTypeNm
				, destroyType: destroyType
				, requestType: 'DISTRIBUTION'
				, businessAreaCd: businessAreaCd
//				, list: JSON.stringify(data)
		}


		openDialogPopup('/outside/commonDestroyRequest/destroyRequestPopup', param, 'popupDialog', 'm', '560', true, 'popup-common popup-destroy-request');
	}
}

/**
 * 결과 메시지 출력
 * @param response
 * @returns
 */
function underDestroyCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){		//요청이 완료되었습니다.
			searchList(gridParam);
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));					//요청이 실패했습니다
	}
}

function checkVersion(){
	checkVersionOutsideUser('DRAWING', 'gridOutsideDrawingApprovalStatusList');
}
function formatRequestNo(cellValue, options, rowdata, action){
	return '<a onclick="openAnnotationDialog(\'' + rowdata["objectId"] + '\')">'+cellValue+'</a>';}

function formatViewFile(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'OBJECT\', \'DRAWING\', null, \'' + rowdata["objectId"] +'\', null, \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	//alert('a');
	//return '<a onclick="openFile(\'OBJECT\', \'DRAWING\', null, \'' + rowdata["objectId"] +'\', null, \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	//return '<a onclick=window.open("/inside/distribution/drawingRequest/view3DFile", "", width="100%", height="100%", resizable = "yes");</a>';
	//return '<a onclick=javascript:window.open("/inside/distribution/drawingRequest/view3DFile?fname=' + cellValue + '")>' + cellValue+'</a>';
	////return '<a onclick=javascript:window.open("/inside/distribution/drawingRequest/view3DFile?fname=' + "/MODEL/toycar/vizw/toycar_wh.vizw" + '")>' + cellValue+'</a>';

	//return '<a onclick=javascript:window.open("/inside/distribution/drawingRequest/view3DFile/?filename='+ cellValue + '")>' + cellValue+'</a>';
}

function formatValidType(cellValue, options, rowdata, action){
	var rtn = "";
	if ( cellValue != undefined ) {
		rtn = cellValue;
	}
	return '<font color="red">'+ rtn +'</font>';
}
