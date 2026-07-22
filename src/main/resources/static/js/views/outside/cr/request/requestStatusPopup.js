var acceptFlag;
function fileDownload(crNo, fileNo){
	$("form[name=tmpForm]")
	.attr("action","/outside/cr/request/crFileDownload?crNo="+crNo+"&fileNo="+fileNo)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();
//	location.href="/outside/cr/request/getCrTemplate";
}


function approve(){
	acceptFlag = 'APP';
	var param = $('#crForm').serializeObject();
	callAjax(param, '/outside/cr/request/approve', crCallback);
}

function approvalReject(crNo){
	if(!isValidObjEmpty($("#rejectDesc"), 'form.rejectRequestDesc')) {
		return false;
	}
	acceptFlag = 'REJECT';
	var param = $('#crForm').serializeObject();
	callAjax(param, '/outside/cr/request/approvalReject', crCallback);
}

function crCallback(response){
	if(response.success){
		if('REJECT' == acceptFlag){
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