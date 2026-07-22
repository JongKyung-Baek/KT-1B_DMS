var acceptFlag;
var twoDevelopment=false;
var popupGridParam;
var popupGridId = 'gridRequestUseEndYmdPopup';

function applyPopupSelect2Style($el, data) {
	if (!$el || !$el.length) return;

	if ($el.data('select2')) {
		$el.select2('destroy');
	}

	var $dropdownParent = $el.closest('.ui-dialog-content');
	if (!$dropdownParent.length) {
		$dropdownParent = $('#popupDialoga');
	}

	$el.select2({
		data: data || [],
		width: '100%',
		minimumResultsForSearch: 1,
		dropdownParent: $dropdownParent,
		dropdownCssClass: 'popupSelect2Dropdown',
		selectionCssClass: 'popupSelect2Selection'
	});
}

$(function() {
	//라디오 -> 버튼형식으로 변경
	$( "#radioset" ).buttonset();
	$( "#controlgroup" ).controlgroup();

	$('#businessAreaCd').val($('#'+gridId).jqGrid('getCell', 1, 'businessAreaCd'));
	//업체 선택 시 업체 사용자 selectbox에 해당 업체 사용자 리스트 불러오기
	//업체 구매 담당자 조회
	$('#companyCd').on('select2:select', function (e) {
		callAjax(param, "/inside/authorization/selectDistributionCompanyUserCombo?companyCd="+e.params.data.id+"&businessAreaCd="+$("#businessAreaCd").val(), function(data){
			$("#companyUserCd").html('');
			applyPopupSelect2Style($("#companyUserCd"), data.results);
		});

		//구매담당자
		var param;
		callAjax(param, "/inside/authorization/getPurchaseInfo?companyCd="+e.params.data.id + "&businessAreaCd=" + $("#businessAreaCd").val(), function(data){
			if(data.userCd == undefined){
				alertMessage(g_msg('msg.noPurchaser'));
				return;
			}

			$('#purchaseTeam').append(data.userNm);
			$('#purchaseTeam').append('<option value="'+data.userCd+'">' + data.userNm + '</option>');
			$("#purchaseTeamNm").val(data.userNm);
			$("#purchaseTeam").val(data.userCd).trigger('change');
		});

	});

	//업체 사용자 선택 시 Email 채우기
	$('#companyUserCd').on('select2:select', function (e) {
		$.ajax({
			url: "/inside/distribution/commonRequest/getUserEmail"
		  , type : "POST"
		  , cache: false
		  , dataType : "json"
		  , data : {
			  userCd : $('#companyUserCd').val()
		  }
		  , success : function(data){
			  $('#deployUserEmail').val(data.email);
		  }
		,error : function(e){
			}
		})
	});

	//용도 selectBox 변경시 유효기간 변경
	$('#requestPurpose').on('select2:select', function (e) {

		var v = e.params.data.id;
		var $grid = $("#"+popupGridId);
		var ids = $grid.jqGrid('getDataIDs');
		var i=0;

		for(i=0; i<ids.length; i++) {
			var data = $("#"+popupGridId).jqGrid('getRowData', ids[i]);
			var $tr = $("#gridRequestUseEndYmdPopup #" + ids[i]);

			$tr.find('input[name=requestPurposeTerm]').val(getDeployTerm(v, data.distributeTypeCd));
		}
	});

	var requestType = $("#requestType").val();
	if('SW' == requestType) {
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
	}
});

function setGridParam() {
	var html = [];
	var getObjectNo = function(data) {
		var requestType = $("#requestType").val();

		if('DOC' === requestType) {
			return data.documentNo;
		}
		else if('DRAWING' === requestType) {
			return data.drawingNo;
		}
		else if('SW' === requestType) {
			return data.swNo;
		}
		else {
			return data.objectNo;
		}
	};
	var getObjectNm = function(data) {
		var requestType = $("#requestType").val();

		if('DOC' === requestType) {
			return data.documentNm;
		}
		else if('DRAWING' === requestType) {
			return data.drawingNm;
		}
		else if('SW' === requestType) {
			return data.swNm;
		}
		else {
			return data.objectNm;
		}
	}

	var arr = [];
	var size=0;
	
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		size=item;
		var v = {
				distributeTypeNm: data.distributeTypeNm
				, distributeTypeCd: data.distributeTypeCd
				, objectNo: getObjectNo(data)
				, revNo: data.revNo
				, objectNm: getObjectNm(data)
				, fileOrgNm: data.orgFileNm
				, useEndYmd: '<input type="text" name="requestPurposeTerm" value="' + getDeployTerm($('#requestPurpose').val(), data.distributeTypeCd) + '"/>'
		};

		arr.push(v);
	});
	popupGridParam = {
			gridId:  popupGridId,
			numbering: false,
			data: arr,
			cellEdit: true,
			size: size,
			cellsubmit: 'clientArray'
	}
}

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
			var param = $("#formRequest").serializeObject();
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

function isValidRequestPurposeTerm() {
	//용도 변경 시 숫자 validation
	var result = true;
	$("input[name=requestPurposeTerm]").each(function() {
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


function save(type){
	if(!isValidObjEmpty($("#companyCd"), 'form.company')) return false;
	if(!isValidObjEmpty($("#companyUserCd"), 'form.companyUser')) return false;
	if(!isValidObjEmpty($("#deployUserEmail"), 'form.email')) return false;
	if(!isValidObjEmpty($("#purchaseTeam"), 'form.approvalUser')) return false;
	if(!isValidRequestPurposeTerm()) return false;
	if( !($("#distributionTypeviewPrint").is(':checked')) && !($("#distributionTypefileDistribution").is(':checked')) ){		//배포방식
		alertMessage(g_msg('form.distributionType'), function() {
			$(this).dialog("close");
		});
		return false;
	}
	if(!isValidObjEmpty($("#requestDesc"), 'form.applyReason')) return false;

	var param = $("#formRequest").serializeObject();

	delete param.distributeTypeCd;

	if("one" == type){
		param.protectUserUid = null;
	}
	var businessAreaCd;
	var jsonArr = [];
	//그리드에서 선택된 항목 가져오기
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);

		data['requestPurposeTerm'] = $('input[name=requestPurposeTerm]').eq(index).val();

		jsonArr.push(data);
	});

//	console.log(jsonArr);

	param.list = jsonArr;
	param.businessAreaCd = businessAreaCd;
	param.requestPurposeTerm = $('#requestPurposeTerm').val();
	param.approvalRequestType = 'DISTRIBUTION';
	param.distributeMethodCd = $("input[name=distributeMethodCd]:checked").attr('id');

	if(true === $("#distributionTypeviewPrint").is(':checked')){	//veiw&print check 여부
		param.printYn = 'Y';
	}else{
		param.printYn = 'N';
	}

	if(true === $("#distributionTypefileDistribution").is(':checked')){	//파일배포여부 check 여부
		param.fileDistributionType = $("input[name=fileDistributionType]:checked").attr('value');
	}

	param.distributionType = $("input[name=distributionType]:checked").attr('value');
	param.sendEmailYn = $("#sendEmailYn").val();
	console.log(param);

	callAjax(param, '/inside/distribution/commonRequest/approvalRequest', requestCallback)
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}

/**
 * 배포 승인 요청
 * @returns
 */
function distributionApprovalRequest(){
	if(!validate()) {
		return false;
	}
}
