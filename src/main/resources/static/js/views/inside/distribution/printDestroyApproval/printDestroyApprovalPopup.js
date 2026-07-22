function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridApproval',
			formId : 'formApprovalPopup',
			url : '/inside/distribution/printDestroyApproval/selecDestroyApprovalList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check'
	}

	return popupGridParam;
}

function saveDestroy(flag){

	if('R' === flag) {			//반려시 배포요청반려사유 반드시 입력
		if(!isValidObjEmpty($("#rejectDesc"), 'form.printDestoryRejectDesc')) {
			return false;
		}
	}

	var param = $("#formApprovalPopup").serializeObject();
	param.saveType = flag;

	callAjax(param, '/inside/distribution/printDestroyApproval/saveDestroyApproval', function(){
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

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	//리스트 갯수
	var $grid = $('#gridApproval');
	$('#listCount').html($grid.getGridParam('records'));
	dialogToolbarWidth()
}
