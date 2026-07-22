<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/docPdfRequestList.js"></script>
<script>
var gridId = 'gridDocPdfRequestList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formDocPdfRequest',
				url : '/inside/distribution/docPdfRequest/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

// 	function formatDocPdfFileList(cellValue, options, rowdata, action){
// 		return '<a onclick="openViewer(\''+rowdata["objectId"]+'\', \'DOCPDF\', \'OBJECT\', null)">'+cellValue+'</a>';
// 	}

function formatValidType(cellValue, options, rowdata, action){
	var rtn = "";
	if ( cellValue != undefined ) {
		rtn = cellValue;
	}
	return '<font color="red">'+ rtn +'</font>';
}

function formatConvert(cellValue, options, rowdata, action){
	return '<a onclick="alert('a');">'+cellValue+'</a>';
	//return '<a onclick="openFile(\'OBJECT\', \'DOCPDF\', null, \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	//return '<a onclick="openFile()">'+cellValue+'</a>';
	
}

function formatViewer(cellValue, options, rowdata, action){	
	return '<a onclick="alert('b');">'+cellValue+'</a>';
	//return '<a onclick="openFile(\'OBJECT\', \'DOCPDF\', null, \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	//return '<a onclick="openFile()">'+cellValue+'</a>';
	
}

function formatConvertViewer(cellValue, options, rowdata, action){
	return '<a onclick="alert('c');">'+cellValue+'</a>';	
	//return '<a onclick="openFile(\'OBJECT\', \'DOCPDF\', null, \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	//return '<a onclick="openFile()">'+cellValue+'</a>';
	
}


function formatProtect(cellValue, options, rowdata, action){
	if(cellValue == "Y"){
		return '<a onclick="openDialog(\'' + rowdata["objectId"] + '\')">'+cellValue+'</a>';
	}else{
		return cellValue;
	}
}

function openDialog(objectId){
	openDialogPopup("/inside/distribution/commonRequest/protectPopup", {objectId: objectId, objectType : "DOCPDF"}, "popupDialog", 'm', 360);
}

</script>
</head>
<body>
	<custom:listTemplate gridId="gridDocPdfRequestList"/>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>
