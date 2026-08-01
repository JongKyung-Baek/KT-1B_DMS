$(function() {
});

$(document).ready(function() {
    $('#dept_nm').on('input', function() {
        checkWhitespace(this);
    });
});



function saveUser() {
    curSendIndex = 0

    var deptNm = document.getElementById("dept_nm").value;
    var deptCd = document.getElementById("dept_cd").value;
    var useYn = document.getElementById("use_yn").checked ? "Y" : "N";
    var saveFlag = document.getElementById("saveFlag").value;

    var formData = new FormData();

    formData.append("deptNm", deptNm);
    formData.append("deptCd", deptCd);
    formData.append("useYn", useYn);
    formData.append("saveFlag", saveFlag);

    if (deptNm === "") {
        alertMessage(g_msg('msg.selectDeptNm'));
        return;  // 함수 종료, AJAX 호출하지 않음
    }

    callAjaxUpload(  formData, "/general/organizationmanage/insidedept/saveRegisterDept", requestCrXCallback);
}

function requestCrXCallback(response){
    if(response.success){
        alertMessage(g_msg('msg.registerComplete'), function() {
            closePopup('popupDialog');
        });
    }else{
        alertMessage(g_msg(response.message));
    }
}


function checkWhitespace(inputElement) {
    if (inputElement.value.includes(' ')) {
        alertMessage(g_msg('msg.NoSpaceBar'));
        inputElement.value = inputElement.value.replace(/\s+/g, '');
    }
}
