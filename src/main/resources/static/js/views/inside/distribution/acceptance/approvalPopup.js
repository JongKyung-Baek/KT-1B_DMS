$(function() {


	//파일 배포 변경 이벤트
	$('input[name=distributionType]').on('change', function(){
		if(true === $("#distributionTypefileDistribution").is(':checked')) {		//파일 배포 사용시
			$.each($("input[name=fileDistributionType]"), function(index, item){
				$('#'+item.id).prettyCheckable('enable');
			});
		}else if(true === $("#distributionTypeviewPrint").is(':checked')) {
			$.each($("input[name=fileDistributionType]"), function(index, item){
				$('#'+item.id).prettyCheckable('disable');
			});
		}
	});

	//유효기간 입력 시 숫자 검증 (최대 60개월)
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

function getList() {
	var $grid = $("#"+popupGridId);
	var ids = $grid.jqGrid('getDataIDs');
	var i=0;
	var arr = [];

	for(i=0; i<ids.length; i++) {
		var data = $grid.jqGrid('getRowData', ids[i]);
		var $tr = $("#" + popupGridId + " #" + ids[i]);

		data.deployTerm = $tr.find('input[name=deployTerm]').val();
		arr.push(data);
	}

	return arr;
}

function isValidDeployTerm() {
	//용도 변경 시 숫자 validation
	var result = true;
	$("input[name=deployTerm]").each(function() {
		var $ts = $(this);
		var regex = /^[1-9]{1}$|^[1-4]{1}[0-9]{1}$|^60$/
	    if(!regex.test($(this).val())){
	    	alertMessage(g_msg('form.validateTerm') + "은 " + g_msg('label.onlyNumberOneToSixty'), function(){
	    		$(this).dialog("close");
	    		$ts.focus();
	    	});
	    	$(this).val('');

	    	result = false;
	    	return false;
	    }
	});

	return result;
}


function saveAcceptance(type){
	if('R' === type) {			//반려시 배포요 청반려사유 반드시 입력
		if(!isValidObjEmpty($("#rejectRequestDesc"), 'form.rejectRequestDesc')) {
			return false;
		}
	}else if('A' === type) {	//승인 시 배포방식 반드시 선택
		var requestPurpose = $("#requestPurpose").val();

		if(!isValidDeployTerm()) {
			return false;
		}
//		if( !($("#viewPrint").is(':checked')) && !($("#fileDistribution").is(':checked')) ){		//배포방식
//			alertMessage(g_msg("msg.requiredDataEmpty", g_msg('form.distributionType')), function() {
//				$(this).dialog("close");
//			});
//			return false;
//		}
	}

	var param = $("#formAcceptancePopup").serializeObject();

	// 불필요한 param 삭제
	delete param.deployTerm;

	param['list'] = getList();

	param.saveType = type;
	
	/*
	if(true === $("#distributionTypeviewPrint").is(':checked')){	//veiw&print check 여부
		param.printYn = 'Y';
	}else{
		param.printYn = 'N';
	}

	if(true === $("#distributionTypefileDistribution").is(':checked')){	//파일배포여부 check 여부
		param.fileDistributionType = $("input[name=fileDistributionType]:checked").attr('value');
	}
	*/

	callAjax(param, '/inside/distribution/acceptance/saveAcceptance', function(){
		if('A'==type) {
			alertMessage(g_msg('msg.completeApproalRequest'), function(){			//승인 요청이 완료되었습니다.
				searchList(gridParam);
				closePopup('popupDialog');
				$(this).dialog("close");
			});
		}else if('R'==type){
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
	var $grid = $('#gridDistributionApprovalPopup');
	$('#listCount').html($grid.getGridParam('records'));
	dialogToolbarWidth()
	var requestPurpose = $("#requestPurposeData").val();
	var objectType = $("#objectType").val();
	if(objectType.indexOf('PRODUCT') === -1){
		if(('MANUAL' === requestPurpose) || ('PRODUCT' ===requestPurpose)){

		}else{
			$("#"+popupGridId).jqGrid("hideCol", "useEnd");
		}
	}else{
		$("#"+popupGridId).jqGrid("hideCol", "useEnd");
	}
}

function formatDeployTerm(cellValue, options, rowdata, action){
	return '<input type="text" name="deployTerm" value="' + getDeployTerm(rowdata.requestPurpose, rowdata.distributeTypeCode) + '"/>';
}
