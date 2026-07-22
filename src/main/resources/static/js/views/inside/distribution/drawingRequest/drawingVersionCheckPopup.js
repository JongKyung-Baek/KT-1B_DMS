var popupGridParam;
var popupGridId = 'gridRequestPopup';
var emptyArray = [];

/**
 * 이미지 비교
 * 선택된 목록 데이터중 두 개의 이미지를 비교하는 인터페이스 호출
 * @returns
 */
function compareImageInside() {
    // 선택된 행의 ID 배열 가져오기
    var selrow = $('#' + popupGridId).jqGrid('getGridParam', 'selarrrow');
    console.log("선택된 row 갯수: " + selrow.length);

    // 반드시 두 개의 항목만 선택되어야 함
    if (selrow.length !== 2) {
        alertMessage(g_msg('msg.chooseTwoDrawing'));
        return;
    }

    // 선택된 두 개의 데이터 가져오기
    var dataA = $('#' + popupGridId).jqGrid('getRowData', selrow[0]);
    var dataB = $('#' + popupGridId).jqGrid('getRowData', selrow[1]);
    console.log("dataA: ", dataA);
    console.log("dataB: ", dataB);

    if(dataA.drawingType==="3D" || dataB.drawingType==="3D"){
        alertMessage(g_msg('msg.choose2DDrawing'));
        return;
    }

    // 총 페이지가 다르다면 비교 불가(총 페이지를 크게 신경쓰지 않으므로 비활성화)
    // if(dataA.totalPageNo !== dataB.totalPageNo) {
    //     alertMessage(g_msg('msg.differentTotalPageNo'));
    //     return;
    // }

    var curObjectId, prevObjectId, curRevNo, prevRevNo, curFilePath, prevFilePath;

    var revNoA = parseFloat(dataA.revNo);
    var revNoB = parseFloat(dataB.revNo);

    // revNo 비교: 더 높은 revNo를 현재 데이터, 낮은 revNo를 이전 데이터로 설정
    if (revNoA > revNoB) {
        curObjectId = dataA.objectId;
        prevObjectId = dataB.objectId;
        curRevNo = "REV:" + dataA.revNo;
        prevRevNo = "REV:" + dataB.revNo;
        curFilePath = dataA.filePath;
        prevFilePath = dataB.filePath;
    } else {
        curObjectId = dataB.objectId;
        prevObjectId = dataA.objectId;
        curRevNo = "REV:" + dataB.revNo;
        prevRevNo = "REV:" + dataA.revNo;
        curFilePath = dataB.filePath;
        prevFilePath = dataA.filePath;
    }

    // ajax 요청 파라미터 구성
    var param = {
        curObjectId: curObjectId,
        prevObjectId: prevObjectId,
        curRevNo: curRevNo,
        prevRevNo: prevRevNo,
        curFilePath: curFilePath,
        prevFilePath: prevFilePath,
        objectType: 'DRAWING'
    };

    console.log("Ajax 요청 파라미터: ", param);

    // ajax 요청 호출
    callAjax(param, '/inside/distribution/commonRequest/compareImageRequest', requestCallback, 'json');
}

function compareImageOutside() {
    // 선택된 행의 ID 배열 가져오기
    var selrow = $('#' + popupGridId).jqGrid('getGridParam', 'selarrrow');
    console.log("선택된 row 갯수: " + selrow.length);

    // 반드시 두 개의 항목만 선택되어야 함
    if (selrow.length !== 2) {
        alertMessage(g_msg('msg.chooseTwoDrawing'));
        return;
    }

    // 선택된 두 개의 데이터 가져오기
    var dataA = $('#' + popupGridId).jqGrid('getRowData', selrow[0]);
    var dataB = $('#' + popupGridId).jqGrid('getRowData', selrow[1]);
    console.log("dataA: ", dataA);
    console.log("dataB: ", dataB);

    if(dataA.drawingType==="3D" || dataB.drawingType==="3D"){
        alertMessage(g_msg('msg.choose2DDrawing'));
        return;
    }

    // 도면이 폐기 단계에 있으면 이미지 비교 불가(총 페이지를 크게 신경쓰지 않으므로 비활성화)
    // if (dataA.destroyStatusCd !== "" || dataB.destroyStatusCd !== "") {
    //     alertMessage(g_msg('msg.destroyedDrawing'));
    //     return;
    // }


    // 총 페이지가 다르다면 이미지 비교 불가(총 페이지를 크게 신경쓰지 않으므로 비활성화)
    // if(dataA.totalPageNo !== dataB.totalPageNo) {
    //     alertMessage(g_msg('msg.differentTotalPageNo'));
    //     return;
    // }

    var curObjectId, prevObjectId, curRevNo, prevRevNo, curFilePath, prevFilePath;

    var revNoA = parseFloat(dataA.revNo);
    var revNoB = parseFloat(dataB.revNo);

    // revNo 비교: 더 높은 revNo를 현재 데이터, 낮은 revNo를 이전 데이터로 설정
    if (revNoA > revNoB) {
        curObjectId = dataA.objectId;
        prevObjectId = dataB.objectId;
        curRevNo = "REV:" + dataA.revNo;
        prevRevNo = "REV:" + dataB.revNo;
        curFilePath = dataA.filePath;
        prevFilePath = dataB.filePath;
    } else {
        curObjectId = dataB.objectId;
        prevObjectId = dataA.objectId;
        curRevNo = "REV:" + dataB.revNo;
        prevRevNo = "REV:" + dataA.revNo;
        curFilePath = dataB.filePath;
        prevFilePath = dataA.filePath;
    }

    // ajax 요청 파라미터 구성
    var param = {
        curObjectId: curObjectId,
        prevObjectId: prevObjectId,
        curRevNo: curRevNo,
        prevRevNo: prevRevNo,
        curFilePath: curFilePath,
        prevFilePath: prevFilePath,
        objectType: 'DRAWING'
    };

    console.log("Ajax 요청 파라미터: ", param);

    // ajax 요청 호출
    callAjax(param, '/inside/distribution/commonRequest/compareImageRequest', requestCallback, 'json');
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCallback(response){
    if(response.success){
        if(response.redirectUrl) {
            // 리다이렉트 URL이 있으면 브라우저를 해당 URL로 이동
            window.location.href = response.redirectUrl;
        } else {
            infoMessage(g_msg('msg.requestComplete'), function(){
                searchList(gridParam);
                closePopup('popupDialog');
                $(this).dialog("close");
            });
        }
    } else if(response.failReason === 'prev_failed') { alertMessage(g_msg("msg.failPrevRev")) }
     else if(response.failReason === 'cur_failed') { alertMessage(g_msg("msg.failCurRev")) }
     else if(response.failReason === 'both_failed') { alertMessage(g_msg("msg.failBothRev")) }
     else { alertMessage(g_msg("msg.requestFailure")); }
}