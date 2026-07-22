$(function() {
    document.getElementById("companyCd_select").disabled = 'disabled';
    document.getElementById("purchaserUid_select").disabled = 'disabled';
//		$("#companyCd_select").select2("enable", false);
//		$("#purchaserUid_select").select2("enable", false);

    $("#distributeTypeCd_select").change(function() {
        if('hdQuickChangeAction' === $(this).val()) {
            document.getElementById("companyCd_select").disabled = '';
            document.getElementById("purchaserUid_select").disabled = '';
        }
        else {
            document.getElementById("companyCd_select").disabled = 'disabled';
            document.getElementById("purchaserUid_select").disabled = 'disabled';
        }
    });
});

function formatValidType(cellValue, options, rowdata, action){
    var rtn = "";
    if ( cellValue != undefined ) {
        rtn = cellValue;
    }
    return '<font color="red">'+ rtn +'</font>';
}

function formatViewFile(cellValue, options, rowdata, action){
    return '<a onclick="openFile(\'OBJECT\', \'VIDEO\', \'' + rowdata["requestNo"] + '\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
}




function requestDistribute(){
    requestInsideUser('DISTRIBUTION', 'VIDEO', 'gridVideoRequestList');
}

function requestPrint(){
    requestInsideUser('PRINT', 'VIDEO', 'gridVideoRequestList');
}

function viewing(){
    openDialogPopup("/inside/common/viewer/openViewerPopup", {}, "popupDialog", 'l', 600);
}

function searchAll(){
    openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'VIDEO'}, "searchAllPopup", 's', 600);
}

// function view3d(){
//     //openDialogPopup("/inside/distribution/drawingRequest/view3DFile", {type:'DRAWING'}, "popupDialog", 'xl', 700);
//     window.open("/inside/distribution/drawingRequest/view3DFile", "", 'width="100%", height="100%", resizable = yes');
// }
