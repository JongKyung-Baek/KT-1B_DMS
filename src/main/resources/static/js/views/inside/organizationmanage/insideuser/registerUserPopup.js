$(function() {
});

$(document).ready(function() {
    $('#user_Id, #userPwd, #user_Nm, #email').on('input', function() {
        checkWhitespace(this);
    });

    // Keep select2 dropdowns inside the current popup dialog to avoid clipping.
    var $popupParent = $('#popupDialog').closest('.ui-dialog');
    if ($popupParent.length === 0) {
        $popupParent = $('.ui-dialog:has(.registerUserPopup):visible').last();
    }
    if ($popupParent.length === 0) {
        $popupParent = $('.ui-dialog:visible').last();
    }

    $popupParent.addClass('popup-inside-user-register');
    $popupParent.find('.ui-dialog-content').css('overflow', 'visible');

    ['deptCd', 'positionCd', 'roleGroupCd', 'distributionAuthCd', 'businessAreaCd'].forEach(function(id) {
        var $el = $('#' + id);
        if (!$el.length || !$el.is('select')) {
            return;
        }

        var existing = $el.data('select2');
        var options = { dropdownParent: $popupParent, dropdownCssClass: 'drawing-like-dropdown' };

        if (existing && existing.options && existing.options.options) {
            options = $.extend(true, {}, existing.options.options, options);
        } else if (id === 'businessAreaCd') {
            options.minimumResultsForSearch = -1;
        } else {
            options.minimumResultsForSearch = 1;
        }

        if (id === 'businessAreaCd') {
            options.dropdownCssClass = 'drawing-like-dropdown register-user-no-search';
        }

        if (existing) {
            $el.select2('destroy');
        }
        $el.select2(options);
    });
});

function saveUser() {
    curSendIndex = 0;

    var userId = document.getElementById('user_Id').value;
    // var userPwd = document.getElementById('userPwd').value;
    var userNm = document.getElementById("user_Nm").value;
    var email = document.getElementById("email").value;
    var deptCd = document.getElementById("deptCd").value;
    var positionCd = document.getElementById("positionCd").value;
    var roleGroupCd = document.getElementById("roleGroupCd").value;

    var distributionAuthEl = document.getElementById("distributionAuthCd");
    var distributionAuthCd = distributionAuthEl ? distributionAuthEl.value : "";
    var businessAreaEl = document.getElementById("businessAreaCd");
    var businessArea = businessAreaEl ? businessAreaEl.value : "";

    var saveFlag = document.getElementById("saveFlag").value;
    var userCd = document.getElementById("userCd").value;

    var formData = new FormData();
    formData.append("userId", userId);
    formData.append("userCd", userCd);
    // formData.append("userPwd", userPwd);
    formData.append("userNm", userNm);
    formData.append("email", email);
    formData.append("deptCd", deptCd);
    formData.append("positionCd", positionCd);
    formData.append("roleGroupCd", roleGroupCd);
    formData.append("saveFlag", saveFlag);

    if (distributionAuthEl) {
        formData.append("distributionAuthCd", distributionAuthCd);
    }
    if (businessAreaEl) {
        formData.append("businessArea", businessArea);
    }

    if (deptCd === "" || positionCd === "" || roleGroupCd === "" || userId === "" || userNm === "" || email === "") {
        alertMessage(g_msg('msg.selectValues'));
        return;
    }

    callAjaxUpload(formData, "/inside/organizationmanage/insideuser/saveRegisterUser", requestCrXCallback);
}

function requestCrXCallback(response){
    if(response.success){
        alertMessage(g_msg('msg.registerComplete'), function() {
            searchList(gridParam);
            closePopup('popupDialog');
            $(this).dialog("close");
        });
    }else{
        alertMessage(g_msg(response.message));	// InsideuserService에서 띄어주는 이유 출력
    }
}


function checkWhitespace(inputElement) {
    if (inputElement.value.includes(' ')) {
        alertMessage(g_msg('msg.NoSpaceBar'));
        inputElement.value = inputElement.value.replace(/\s+/g, '');
    }
}
