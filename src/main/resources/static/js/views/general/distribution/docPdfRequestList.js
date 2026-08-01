function requestDistribute(){
	requestInsideUser('DISTRIBUTION', 'DOCPDF', 'gridDocPdfRequestList');
}

function requestPrint(){
	requestInsideUser('PRINT', 'DOCPDF', 'gridDocPdfRequestList');
}

function searchAll(){
	openDialogPopup("/general/distribution/commonRequest/searchAllPopup", {type:'DOCPDF'}, "searchAllPopup", 's', 600);
}