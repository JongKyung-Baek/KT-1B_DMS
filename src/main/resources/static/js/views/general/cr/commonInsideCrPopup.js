var acceptFlag;
function fileDownload(crNo, fileNo){
	var frame = document.querySelector("iframe[name='hiddenFrame']");
	if (frame) {
		frame.src = "/general/cr/acceptance/crFileDownload?crNo=" + encodeURIComponent(crNo)
			+ "&fileNo=" + encodeURIComponent(fileNo);
	}
}

function approvalRequest(){
	acceptFlag = 'ACC';
	if(!isValidObjEmpty($("#reviewResult"), 'form.reviewComment')) return false;
	var param = $('#crForm').serializeObject();
	callAjax(param, '/general/cr/acceptance/approvalRequest', crCallback);
}

function acceptanceReject(crNo){
	acceptFlag = 'REJECT';
	if(!isValidObjEmpty($("#rejectReason"), 'form.rejectDesc')) return false;
	var param = $('#crForm').serializeObject();
	callAjax(param, '/general/cr/acceptance/acceptanceReject', crCallback);
}


function approve(){
	acceptFlag = 'APP';
	var param = $('#crForm').serializeObject();
	console.log(param);
	callAjax(param, '/general/cr/approval/approve', crCallback);
}

function approvalReject(crNo){
	acceptFlag = 'REJECT';
	var param = $('#crForm').serializeObject();
	callAjax(param, '/general/cr/approval/approvalReject', crCallback);
}


function crCallback(response){
	if(response.success){
		if('ACC' == acceptFlag){
			infoMessage(g_msg('msg.acceptanceSuccess'), function(){			//접수가 완료되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else if('REJECT' == acceptFlag){
			infoMessage(g_msg('msg.completeReject'), function(){			//반려되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else if('APP' == acceptFlag){
			infoMessage(g_msg('msg.completeApprove'), function(){			//승인되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}
	}else{
		alertMessage(g_msg('msg.failure'), function(){		//실패하였습니다.
			$(this).dialog("close");
		})
	}
}
