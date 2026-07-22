//function fileDownload2(destroyNo, destroyFileSeq, fileName){
//    var xhr = new XMLHttpRequest();
//    xhr.open("GET", popupInfo.fileDownloadUrl+"?destroyNo="+destroyNo+"&destroyFileSeq="+destroyFileSeq, true);
//    xhr.responseType = "blob";
//    xhr.onload = function(){
//        var urlCreator = window.URL || window.webkitURL;
//        var imageUrl = urlCreator.createObjectURL(this.response);
//        var tag = document.createElement('a');
//        tag.href = imageUrl;
//        tag.download = fileName;
//        document.body.appendChild(tag);
//        tag.click();
//        document.body.removeChild(tag);
//    }
//    xhr.send();
//}
//
//function fileDownload(destroyNo, destroyFileSeq, fileName){
//
//	console.log(destroyNo);
//	console.log(destroyFileSeq);
//	console.log(fileName);
//	console.log(popupInfo.fileDownloadUrl);
//	console.log(popupInfo.fileDownloadUrl + "?destroyNo="+destroyNo+"&destroyFileSeq="+destroyFileSeq);
//	$("form[name=tmpForm]")
//	.attr("action", popupInfo.fileDownloadUrl + "?destroyNo="+destroyNo+"&destroyFileSeq="+destroyFileSeq)
//	.attr("target", "hiddenFrame")
//	.attr("method", "post")
//	.submit();
////	location.href="/outside/cr/request/getCrTemplate";
//}

function fileDownload(destroyNo, destroyFileSeq, fileName){
	var link = document.createElement("a");
    link.setAttribute("id", destroyFileSeq);
    link.setAttribute("href", popupInfo.fileDownloadUrl + "?destroyNo="+destroyNo+"&destroyFileSeq="+destroyFileSeq);
//    var NameToDownload = desired name here;

    link.innerHTML = fileName;
    link.click();
}

