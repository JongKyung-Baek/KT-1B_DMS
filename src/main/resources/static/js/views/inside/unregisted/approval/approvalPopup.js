function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridRequestStatusPopup',
			formId : 'formRequestStatusPopup',
			url : '/inside/unregisted/approval/selectPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false   
	}
	return popupGridParam;
}


$(function() {
	$('input[name=distributionType]').prettyCheckable('disable');
	if("Y"==printYn){		//Viewing/출력
		$('#distributionTypeviewPrint').prettyCheckable('check');
	}
	if( ("Y" == deployNormalYn) || ("Y" == deploySpecialYn)){
		$('#distributionTypefileDistribution').prettyCheckable('check');
		if("Y" == deployNormalYn){
			$('#fileDistributionTypegeneral').prettyCheckable('check');
		}
		if("Y" == deploySpecialYn){
			$('#fileDistributionTypegeneral').prettyCheckable('check');
		}
	}
});


function saveApproval(flag){
	if('R' === flag) {			//반려시 반려사유 반드시 입력
		if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectDesc')) {
			return false;
		}
	}

	var param = $("#formRequestStatusPopup").serializeObject();
	param.saveType = flag;

	callAjax(param, '/inside/unregisted/approval/saveApproval', function(){
		if('A'==flag) {
			alertMessage(g_msg('msg.completeApprove'), function(){			//승인되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else if('R'==flag){
			alertMessage(g_msg('msg.completeReject'), function(){			//반려되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}
	})

}
