var emptyArray = [];
var files = [];

function swSupportingFileSelectionApi() {
    if (!window.SwSupportingFileSelection) {
        throw new Error("SwSupportingFileSelection is not loaded.");
    }
    return window.SwSupportingFileSelection;
}

function getSwAccumulatedSubFiles(input) {
    if (!input || !Array.isArray(input._tdmsAccumulatedSubFiles)) {
        return [];
    }
    return swSupportingFileSelectionApi().toArray(input._tdmsAccumulatedSubFiles);
}

function appendSwAccumulatedSubFiles(input, incomingFiles) {
    if (!input) {
        return [];
    }
    return setSwAccumulatedSubFiles(input, swSupportingFileSelectionApi().merge(
        getSwAccumulatedSubFiles(input),
        incomingFiles
    ));
}

function setSwAccumulatedSubFiles(input, selectedFiles) {
    if (!input) {
        return [];
    }

    input._tdmsAccumulatedSubFiles = swSupportingFileSelectionApi().toArray(selectedFiles);

    // Keep the native FileList in sync where the browser permits it. Submission
    // still reads the accumulated list directly, so browsers that reject a
    // programmatic FileList assignment retain the same behavior.
    try {
        if (typeof DataTransfer === "function") {
            var dataTransfer = new DataTransfer();
            input._tdmsAccumulatedSubFiles.forEach(function (file) {
                dataTransfer.items.add(file);
            });
            input.files = dataTransfer.files;
        }
    } catch (ignored) {
        // The accumulated list remains authoritative.
    }

    return getSwAccumulatedSubFiles(input);
}

function removeSwAccumulatedSubFile(input, index) {
    return setSwAccumulatedSubFiles(input, swSupportingFileSelectionApi().removeAt(
        getSwAccumulatedSubFiles(input),
        index
    ));
}

function appendSwAccumulatedSubFilesToFormData(formData, input, fieldName) {
    return swSupportingFileSelectionApi().appendToFormData(
        formData,
        fieldName || "subFiles",
        getSwAccumulatedSubFiles(input)
    );
}

function clearSwAccumulatedSubFiles(input) {
    if (!input) {
        return;
    }
    setSwAccumulatedSubFiles(input, []);
    input.value = "";
}

function fileUpload(){
    $('#swRegisFile').click();
}

function fileChange(){
    var fileName = $('#swRegisFile').val();
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

    if($.trim($("#swRegisFile").val()) === ""){
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
    param.append('file', $("#swRegisFile")[0].files[0]);
    param.append('formSwRegisterPopup', JSON.stringify($('#formSwRegisterPopup').serializeObject()));
    callAjaxUpload(param, "/general/distribution/saveSwRegisterFile", requestCrCallback);
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
