$(function() {
	//라디오 -> 버튼형식으로 변경
	$( "#radioset" ).buttonset();
	$( "#controlgroup" ).controlgroup();


	//업체 선택 시 업체 사용자 selectbox에 해당 업체 사용자 리스트 불러오기
	//업체 구매 담당자 조회
	$('#companyCd').on('select2:select', function (e) {

		var option = {
				minimumResultsForSearch: 1,
				language: 'ko',
				ajax : {
					url: "/inside/authorization/selectCompanyUserCombo?companyCd="+e.params.data.id,
					delay: 250,
					dataType: 'json'
				}
			};

		$("#companyUserCd").select2(option);

		//업체 사용자 초기화
		var $newOption = $("<option selected='selected'></option>").val(null).text("선택하세요");
		$("#companyUserCd").append($newOption).trigger('change');

		//구매담당자
		var param;
		callAjax(param, "/inside/authorization/getPurchaseInfo?companyCd="+e.params.data.id + "&businessAreaCd=" + $("#businessAreaCd").val(), function(data){
			console.log(data);
			console.log(data.userCd);

			if(data.userCd == undefined){
				alertMessage(g_msg('msg.noPurchaser'));
				return;
			}
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
		switch(e.params.data.id){
			case 'CHECK' :
			case 'REFER' :
				$("#deployTerm").val('1');
				break;
			case 'ESTIMATE' :
				$("#deployTerm").val('3');
				break;
			case 'MANUAL' :
			case 'PRODUCT' :
				$("#deployTerm").val('6');
				break;
			default :
				break;
		}
	});

	//용도 변경 시 숫자 validation
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

	//파일 배포 변경 이벤트
	/*
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
	*/
});


function validate(){
	// 2023.07.03 (yskim) 주석처리
	// if($("#securityUserList").val() == ""){
	// 	alertMessage("보안팀 참조메일은 필수 입력 값입니다.");
	// 	return;
	// }
	if(!isValidObjEmpty($("#companyCd"), 'form.company')) return false;
	if(!isValidObjEmpty($("#companyUserCd"), 'form.companyUser')) return false;
	if(!isValidObjEmpty($("#purchaseTeam"), 'form.approvalUser')) return false;
	if(!isValidObjEmpty($("#deployUserEmail"), 'form.email')) return false;
	if(!isValidObjEmpty($("#requestPurposeTerm"), 'form.validateTerm')) return false;
	if( !($("input[value=viewPrint]").is(':checked')) && !($("input[value=fileDistribution]").is(':checked')) ){		//배포방식
//		if( !($("#viewPrint").is(':checked')) && !($("#fileDistribution").is(':checked')) ){		//배포방식
		alertMessage(g_msg("msg.requiredDataEmpty", g_msg('form.distributionType')), function() {
			$(this).dialog("close");
		});
		return false;
	}
	if(!isValidObjEmpty($("#requestDesc"), 'form.applyReason')) return false;
	return true;
}


/**
 * 배포 승인 요청
 * @returns
 */
function save(){
	if(!validate()) {
		return false;
	}

	var param = $("#formRequestPopup").serializeObject();

	var jsonArr = [];
	//그리드에서 선택된 항목 가져오기
	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
		// var aJson = new Object();
		// aJson.objectNo = data.objectNo;
		jsonArr.push(data);
	});
	param.list = jsonArr;
	param.deployTerm = $('#deployTerm').val();
	param.distributeMethodCd = $("input[name=distributeMethodCd]:checked").attr('id');
	console.log(param);
	if(true === $("#distributionTypeviewPrint").is(':checked')){	//veiw&print check 여부
		param.printYn = 'Y';
	}else{
		param.printYn = 'N';
	}

	if(true === $("#distributionTypefileDistribution").is(':checked')){	//파일배포여부 check 여부
		param.fileDistributionType = $("input[name=fileDistributionType]:checked").attr('value');
	}

	if('string' === typeof param.securityUserList) {
		var arr = [];

		arr.push(param.securityUserList);

		param.securityUserList = arr;
	}

//	param.distributionType = $("input[name=distributionType]:checked").attr('id');

	callAjax(param, '/inside/unregisted/request/distributionRequest', function(){
		alertMessage(g_msg('msg.requestComplete'), function(){			//요청이 완료됐습니다
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	})
}

function toggleCheckboxValue(checkboxId) {
	var checkbox = document.getElementById(checkboxId);
	checkbox.value = checkbox.checked ? 'Y' : 'N';
	var sendEmailYn = document.getElementById("sendEmailYn");
	sendEmailYn.value = checkbox.value;
	console.log(sendEmailYn.value);
}