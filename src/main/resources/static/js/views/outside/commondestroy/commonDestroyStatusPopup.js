function fileDownload(destroyRequestNo, destroyFileSeq){
	var url = popupInfo.fileDownloadUrl
		+ "?destroyRequestNo=" + encodeURIComponent(destroyRequestNo)
		+ "&destroyFileSeq=" + encodeURIComponent(destroyFileSeq);
	var frame = document.querySelector("iframe[name='hiddenFrame']");
	if (frame) {
		frame.src = url;
	}
}

