var emptyArray = [];
var files = [];

function fileUpload(){
    $('#productionRegisFile').click();
}

function fileChange(){
    var fileName = $('#productionRegisFile').val();
    if(fileName.indexOf("\\") != -1){
        $('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
    }
}



/**
 * 배포요청 시 validation체크
 * 용도, 업체명/담당자, Email, 사업장, 구매담당자가 선택되었는지 체크한다
 * @returns
 */
function isValidation(){


    if($.trim($("#dataName").val()) === ""){
        isValidDataEmpty("dataName", "form.dataName");
        return false;
    }

    if($.trim($("#productionRegisFile").val()) === ""){
        alertMessage(g_msg('msg.noFile'));	 //첨부된 파일이 없습니다.
        return false;
    }

    return true;
}


function save(){
    if(!isValidation()){
        return;
    }
    var param = new FormData();
    param.append('file', $("#productionRegisFile")[0].files[0]);
    param.append('formProductionRegisterPopup', JSON.stringify($('#formProductionRegisterPopup').serializeObject()));
    callAjaxUpload(param, "/inside/distribution/productionRequest/saveProductionRegisterFile", requestCrCallback);
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCrCallback(response){
    if(response.success){
        infoMessage(g_msg('msg.registerComplete'), function(){			//등록이 완료되었습니다.
            searchList(gridParam);
            closePopup('popupDialog');
            $(this).dialog("close");
        });
    }else{
        alertMessage(g_msg("msg.registerFailure"));						//등록이 실패했습니다.
    }
}
