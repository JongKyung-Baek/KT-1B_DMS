//function fileDownload(cnSerial, fileNo, orgFileNm){
//	var xhr = new XMLHttpRequest();
//    xhr.open("GET", "/inside/cr/acceptance/crFileDownload?cnSerial="+cnSerial+"&fileNo="+fileNo, true);
//    xhr.responseType = "blob";
//    xhr.onload = function(){
//        var urlCreator = window.URL || window.webkitURL;
//        var imageUrl = urlCreator.createObjectURL(this.response);
//        var tag = document.createElement('a');
//        tag.href = imageUrl;
//        tag.download = orgFileNm;
//        document.body.appendChild(tag);
//        tag.click();
//        document.body.removeChild(tag);
//    }
//    xhr.send();
//}
var acceptFlag;
function fileDownload(cnSerial, fileNo, orgFileNm){
	$("form[name=tmpForm]")
	.attr("action","/inside/cr/acceptance/crFileDownload?cnSerial="+cnSerial+"&fileNo="+fileNo)
	.attr("target", "hiddenFrame")
	.attr("method", "post")
	.submit();
//	location.href="/outside/cr/request/getCrTemplate";
}

function approvalRequest(){
	acceptFlag = 'ACC';
	if(!isValidObjEmpty($("#reviewResult"), 'form.reviewComment')) return false;
	var param = $('#crForm').serializeObject();
	callAjax(param, '/inside/cr/acceptance/approvalRequest', crCallback);
}

function acceptanceReject(crNo){
	acceptFlag = 'REJECT';
	if(!isValidObjEmpty($("#rejectReason"), 'form.rejectDesc')) return false;
	var param = $('#crForm').serializeObject();
	callAjax(param, '/inside/cr/acceptance/acceptanceReject', crCallback);
}


function approve(){
	acceptFlag = 'APP';
	var param = $('#crForm').serializeObject();
	console.log(param);
	callAjax(param, '/inside/cr/approval/approve', crCallback);
}

function approvalReject(crNo){
	acceptFlag = 'REJECT';
	var param = $('#crForm').serializeObject();
	callAjax(param, '/inside/cr/approval/approvalReject', crCallback);
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
