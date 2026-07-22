var emptyArray = [];
var files = [];

function fileUpload(){
    $('#videoRegisFile').click();
}

function fileChange(){
    var fileName = $('#videoRegisFile').val();
    if(fileName.indexOf("\\") != -1){
        $('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
    }
}

function isValidation(){

    if($.trim($("#dataName").val()) === ""){
        isValidDataEmpty("dataName", "form.dataName");
        return false;
    }

    if($.trim($("#videoRegisFile").val()) === ""){
        alertMessage(g_msg('msg.noFile'));	 //첨부된 파일이 없습니다.
        return false;
    }

    return true;
}


// function save(){
//     if(!isValidation()){
//         return;
//     }
//
//     var param = new FormData();
//     param.append('file', $("#videoRegisFile")[0].files[0]);
//     param.append('formVideoRegisterPopup', JSON.stringify($('#formVideoRegisterPopup').serializeObject()));
//     callAjaxUpload(param, "/inside/distribution/video/saveVideoRegisterFile", requestCrCallback);
// }

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
