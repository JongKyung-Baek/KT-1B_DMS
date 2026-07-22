$(function() {
});

function isValidBizNo() {
	var bizNo = $("#formPopup #bizNo").val();
	if(!isValidObjEmpty($("#formPopup #bizNo"), 'form.companyCd')) {
		return false;
	}

	if(bizNo.length != 10 || !is_number(bizNo)) {
		alertMessage(g_msg('msg.invalidBizNo'));
		return false;
	}

	return true;
}

/**
 * 모든 자릿수가 숫자인지 체크
 * @param x
 * @returns
 */
function is_number(x) {
    var reg = /^\d+$/;
    return reg.test(x);
}

function saveCompany(){

	if(!isValidObjEmpty($("#companyNm"), 'form.companyNm')) {
		return false;
	}

	if(!isValidBizNo()){
		return false;
	}
	
	var param = $("#formPopup").serializeObject();

	callAjax(param, '/inside/organizationmanage/outsideuser/selectCompanyCheck', CompanyCheckCallback);
}

function CompanyCheckCallback(response){
	if(response.success){
		var data = $("#formPopup").serializeObject();
		callAjax(data, '/inside/organizationmanage/outsideuser/saveCompany', function(response){
			if(response.success) {
				alertMessage(g_msg('msg.completeSave'), function() {
					searchList(gridParam);
					closePopup2('popupDialog');
					$(this).dialog("close");
				});
			}
			else {
				alertMessage(g_msg('msg.failSave'), function() {
				});

			}
		})
	}else{
		alertMessage(g_msg("msg.duplicateData"));   //중복된 데이터 입니다.
	}
}

function closePopup2(dialog){
	$('body').css({"overflow":"auto"});
	searchList(gridParam);
	popupGridParam = '';
	$('#'+dialog+'a').dialog('close');
}