var useNonActiveX = true;
/* ----- container & jqgrid height  ----- */
function containerHeight(){ // Set height value
	var tg1 = $('.bodyWrap .contentArea');
	if(tg1.siblings('.tabArea').length == 1){
		tg1.css({'height': tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true) - tg1.siblings('.tabArea').outerHeight(true) + 'px'});
	}else{
		tg1.css({'height': tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true) + 'px'});
	}

	var tg2 = $('.bodyWrap .gridArea');
	tg2.css({'height': tg2.parents('.contentArea').height() - tg2.siblings('.sbr').outerHeight(true) - tg2.siblings('.btnArea').outerHeight(true) + 'px'});

	var tg3 = $('.gridArea > .gridContainer .ui-jqgrid-view');
	if(tg3.siblings('.ui-jqgrid-pager').length == 1){
		 tg3.css({'height' : tg3.parents('.gridContainer').height() - tg3.siblings('.ui-jqgrid-pager').outerHeight(true) + 'px'});
	 }else{
		 tg3.css({'height' : tg3.parents('.gridContainer').height() + 'px'});
	 }

	var tg4 = $('.gridArea > .gridContainer .ui-jqgrid-bdiv');
	tg4.css({'height' : tg4.parent('.ui-jqgrid-view').height() - tg4.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});

	$('.contentArea.half').each(function(index){
		var brL = $(this).find('.halfGrid').length;
		for(i = 0 ; i < brL ; i++ ){
			var tghalf = $(this).find('.halfGrid').eq(i).find('.gridArea');
			tghalf.css({'height': $('.contentArea.half').height() - tghalf.siblings('.sbr').outerHeight(true) - tghalf.siblings('.btnArea').outerHeight(true) + 'px'});
			tghalf.find('.ui-jqgrid-view').css({'height' : 'calc(100% - 40px'});
			var tg5 = $(this).find('.halfGrid').eq(i).find('.gridArea > .gridContainer .ui-jqgrid-bdiv');
			tg5.css({'height' : tg5.parent('.ui-jqgrid-view').height() - tg5.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});
		}
	})
}
function dialogToolbarWidth(){
	var dtbL = $('.dialogContent .dialogToolbar').length;
	for(i = 0 ; i < dtbL ; i++ ){
		var dtbWL = $('.dialogContent .dialogToolbar').eq(i).find('.left').outerWidth(true);
		$('.dialogContent .dialogToolbar').eq(i).find('.right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
	}
}

$(document).ajaxStart(function(){
	//console.log('ajaxStart');
	$('.ui-widget-overlay.pageLoader').show();
})

$(document).ajaxComplete(function(){
	//console.log('ajaxComplete');
	$('.ui-widget-overlay.pageLoader').hide();
	dialogToolbarWidth();
	try{
		clearSessionTime();
	}catch(e){
		console.log(e);
	}
})
function clearSessionTime() {
	timeoutSecond = defaultSessionTime;
}

function initTimer(remainingTime) {initTimer
	clearTimeout(timerchecker); // 기존 타이머를 중지

	function updateTimer() {
		if (remainingTime > 0) {
			remainingTime--;
			var minutes = Math.floor(remainingTime / 60);
			var seconds = remainingTime % 60;
			$("#sessionTime").text(minutes + "분 " + (seconds < 10 ? "0" : "") + seconds + "초");
			timerchecker = setTimeout(updateTimer, 1000); // 1초 간격으로 타이머 업데이트
		} else {
			clearTimeout(timerchecker); // 타이머를 중지
			alertMessage(g_msg('msg.sessionTimeout'), function(){
				logout(); // 세션 만료 시 로그아웃 처리
				$(this).dialog("close");
			});
		}
	}
	updateTimer(); // 타이머 시작
}

$(document).ready(function(){
	var $overlay = $('<div class="ui-widget-overlay pageLoader"><iframe src="" width="100%" height="100%" marginwidth="0" marginheight="0" border="1px" frameborder="0"></iframe><div class="loading ui-state-default ui-state-active">LOADING</div></div>').hide().appendTo('body');
	containerHeight();
	dialogToolbarWidth();
	$(window).resize(function(){
		containerHeight();
		dialogToolbarWidth();
	});




/* ------ gnb ----- */
	$('.gnbToggle').click(function(e){
		e.preventDefault();
		if($(this).hasClass('open')){ // Close - when GNB is open
			$(this).removeClass('open');
			$('.gnb').removeClass('open');
			$('.gnbDepthArea ul > li').removeClass('open');
			containerHeight();
		}else{ // Open - when GNB is close
			$(this).addClass('open');
			$('.gnb').addClass('open');
			containerHeight();
		}
	});
	$('.gnbDepthArea ul > li > a').click(function(e){
			$('.gnb').addClass('open');
			$('.gnbToggle').addClass('open');

		if($(this).parent('li').children().is('ul') == 0){
			//alert($(this).attr('href'));
		}else{
			e.preventDefault();
			var pali = $(this).parent('li');
			pali.siblings('li').removeClass('open');
			pali.children('ul').children('li').removeClass('open');

			if(pali.hasClass('open')){
				pali.removeClass('open');
			}else if(pali.hasClass('open') == 0 && pali.children().is('ul') == 0){
				pali.removeClass('open');
			}else{
				pali.addClass('open');
			};
		}
	});
	$(".gnbDepthArea li").each(function(index){
		if($(this).children().is('ul') == 0){ // && $(this).parent('ul').hasClass('depth2') == 1
			$(this).children('a').children('span').eq(0).before('<span class="depthBlank"></span>');


		}else if($(this).children().is('ul') == 1 && $(this).parent('ul').hasClass('depth1')){
			$(this).children('a').children('span').eq(0).before('<span class="depthToggle"></span>');
			$(this).children('a').children('span').eq(0).after('<span class="depth1Icon"></span>');


		}else if($(this).children().is('ul') == 1){
			$(this).children('a').children('span').eq(0).before('<span class="depthToggle"></span>');
		}
	});
});


/**
 * 천단위 , 넣기
 * @param n
 * @returns
 */
function commify(n) {
	var reg = /(^[+-]?\d+)(\d{3})/;   // 정규식
	n += '';                          // 숫자를 문자열로 변환

	while (reg.test(n))
		n = n.replace(reg, '$1' + ',' + '$2');

	return n;
}

/**
 * XSS 방지
 * @param strTemp [필수] 크로스사이트 스크립팅을 검사할 문자열
 * @param level [옵션] 검사레벨
 * 			0 (기본) -> XSS취약한 문자 제거
 * 			1 (선택) -> 단순한 <, > 치환
 * @returns
 */
function changeXSS(strTemp, level) {
	if ( level == undefined || level == 0 ) {
		strTemp = strTemp.replace(/\<|\>|\"|\'|\%|\;|\(|\)|\&|\+|\-/g,"");
	}
	else if (level != undefined && level == 1 ) {
		strTemp = strTemp.replace(/\</g, "&lt;");
		strTemp = strTemp.replace(/\>/g, "&gt;");
	}
	return strTemp;
}

/**
 * 쿠키 가져오기
 * @param name
 * @returns
 */
function getCookie( name ) {
	var nameOfCookie = name + "=";
	var x = 0;
	while ( x <= document.cookie.length )
	{
		var y = (x+nameOfCookie.length);
		if ( document.cookie.substring( x, y ) == nameOfCookie ) {
			if ( (endOfCookie=document.cookie.indexOf( ";", y )) == -1 )
				endOfCookie = document.cookie.length;
			return unescape( document.cookie.substring( y, endOfCookie ) );
		}
		x = document.cookie.indexOf( " ", x ) + 1;
		if ( x == 0 )
			break;
	}
	return "";
}

/**
 * &#로 시작하는 HTML entity를 사람이 알아볼 수 있는 값으로 변경
 */
var decodeHtmlEntity = function(str) {
	return str.replace(/&#(\d+);/g, function(match, dec) {
		return String.fromCharCode(dec);
	});
};

/**
* ajax 호출
* @param param
* @param url
* @param callback
* @param dataType - text, html, xml, json, jsonp, script. default: json
* @returns
*/
/*function callAjax(param, url, callback, dataType){
	alert("2");
	if(undefined === dataType) {
		dataType = "json";
	}
	if("json" == dataType){
		param = JSON.stringify(param);
	}

	$.ajax({
		url: url
		, type : "POST"
		, cache: false
		, dataType : dataType
		, contentType: "application/json"
		, data : param
		, success : function(response){
			if('undefined' !== typeof callback) {
				callback(response);
			}
		}
		,error : function(e){
			if(e.status==401){
				parent.parent.location.href = "/login/logout";
			}else{
				alertMessage(g_msg("msg.error") + '[' + e + ']');
			}
		}
	});
}
*/



/**
 * 뷰어 오픈
 * @param gridId
 * @param objectType
 * @param objectId
 * @returns
 */
//function openViewer(objectId, objectType, requestType, requestNo){	
	//openDialogPopup("/common/viewer/openViewerPopup", {objectId : objectId, objectType : objectType, requestType : requestType, requestNo: requestNo}, "viewerDialog", 's', 450);
//}


/*
 * 다중 파일 일 경우
 */
function openViewerMulti(gridId, requestType, objectType){
	if($("#"+gridId).getGridParam('selarrrow').length < 1){
		alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
			$(this).dialog("close");
		});
	}else{
		var aJsonArray = new Array();
		$.each($("#"+gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#"+gridId).jqGrid('getRowData', item);
			var aJson = new Object();
			aJson.requestNo = data.requestNo;
			aJson.objectId = data.objectId;
			if( !(null==data.objectType) ) {
				aJson.objectType = data.objectType;
			}
			aJsonArray.push(aJson);
		});
		openDialogPopup("/common/viewer/openViewerPopup", {list:JSON.stringify(aJsonArray), requestType : requestType, objectType : objectType}, "viewerDialog", 's', 500);
	}
}

let isProcessing = false;
let lastClickTime = 0;

function openFile(requestType, objectType, requestNo, objectId, fileNo, protectYn) {
	const currentTime = new Date().getTime(); // 현재 시간 가져오기

	if (isProcessing || currentTime - lastClickTime < 2000) {
		console.log("두 번째 클릭 무시");
		return; // 2초 이내에 클릭하면 무시
	}

	lastClickTime = currentTime; // 마지막 클릭 시간 갱신
	isProcessing = true;  // 클릭 중으로 설정

	if (undefined === protectYn) {
		protectYn = 'N';
	}

	let bRet = checkBoolean({ objectId: objectId, objectType: objectType, requestType: requestType, requestNo: requestNo }, "/common/viewer/getDestroyStatus");
	let bRet_printHistory = checkBoolean({ objectId: objectId, objectType: objectType, requestType: requestType, requestNo: requestNo }, "/common/viewer/getDestroyStatus_printHistory");

	if (bRet === true) {
		alertMessage(g_msg('msg.printDestroy'));
		isProcessing = false;
		return;
	}

	if (bRet_printHistory === true) {
		alertMessage(g_msg('msg.printDestroy'));
		isProcessing = false;
		return;
	}

	if (!getOutsideCompanyAuthYn()) {
		alertMessage(g_msg('msg.stopDealCompany'));
		isProcessing = false;
		return;
	}

	if ("Y" === protectYn && "UNREG" !== requestType && "PRODUCT" !== requestType) {
		var protectList = [objectId];

		if ('E' === AUTH_SITE) {
			if (!getOutsideProtectYn()) {
				alertMessage(g_msg('msg.noAuthProtectRequest'));
				isProcessing = false;
				return;
			}
		} else {
			if (!getProtectAuthYn(objectType, protectList)) {
				isProcessing = false;
				return;
			}
		}
	}

	var param = {
		requestNo: requestNo,
		requestType: requestType,
		objectType: objectType,
		objectId: objectId,
		fileNo: fileNo
	};

	var width = window.innerWidth * 0.9;
	var height = window.innerHeight * 0.9;
	var left = (window.innerWidth - width) / 2;
	var top = (window.innerHeight - height) / 2;

	var windowFeatures = "width=" + width + ",height=" + height + ",left=" + left + ",top=" + top +
		",menubar=no,toolbar=no,location=no,status=no,scrollbars=yes,resizable=yes";

	if (objectType === "SW") {
		var ret = window.open('/inside/distribution/docPdfLinkRequest/selectItem2?objectType=' + objectType + '&file=' + objectId + '&requestNo=' + requestNo + '&fileNo=' + fileNo, '_blank', '', false);
	} else {
		var popupName = 'PopupWindow_' + objectId + '_' + fileNo;
		var url = '/inside/distribution/docPdfLinkRequest/selectItem2?objectType=' + objectType + '&file=' + objectId + '&requestNo=' + requestNo + '&fileNo=' + fileNo;
		var ret = window.open(url, popupName, windowFeatures);
	}

	isProcessing = false; // 처리 완료 후 상태 초기화
}





function openDownHistoryPopup(requestNo, objectId){
	openDialogPopup("/inside/distribution/downHistory/downHistoryPopup", {objectId: objectId, requestNo : requestNo}, "popupDialog", 'm', 350);
}

function openActLogPopup(downloadedName){
	openDialogPopup_onlyForActLog("/inside/distribution/downHistory/actLogPopup", {downloadedName:downloadedName}, "popupDialog", 'm', 350, true, 'popup-common popup-act-log');
}

function openActLogPopup_onlyForActLog(downloadedName){
	openDialogPopup_onlyForActLog("/inside/distribution/downHistory/actLogPopup", {downloadedName:downloadedName}, "popupDialog", 'm', 350, true, 'popup-common popup-act-log');
}

function getViewerOptionPrefix(extName) {
	var dwgdwfpltcgmOtpions = "-WM:WIDTH";       // 너비로 보기 옵션
	var docsOptions = "-TM:VIEW-WM:AUTO";         // 문서파일 너비로 보기 옵션
	var drawingOptions = "-WM:WIDTH-BC:WHITE";  // 기존 도면 배경색 흰색, 뷰어 띄웠을때 너비로 보기 옵션 적용
	if (extName == "DWG" || extName == "DWF" || extName == "PLT" || extName == "CGM"){// 검은색
	    return dwgdwfpltcgmOtpions;
	} else if (extName == "TXT" || extName == "PDF" || extName == "HWP" || extName == "DOC" || extName == "DOCX" || extName == "PPT" || extName == "PPTX" || extName == "XLS" || extName == "XLSX") {
	    return docsOptions;
	} else{
	    return drawingOptions;   // .SVG .TIFF .TIF
	}
}
/**
 * 다중 파일 출력
 * @param response
 * @returns
 */
 

function callOpenViewer(response){
	
	var popup = window.open('http://localhost:8085/js/pdflegacy/web/viewer.html?file=aaa.pdf', 'pdfPopup', 'width=1000,height=800,scrollbars=yes');
	var popup2 = window.open('http://esobinfo.iptime.org:42481/web/viewer.html', 'pdfPopup2', 'width=1000,height=800,scrollbars=yes');
		
	//alert(response.filePath);
}	 
function callOpenViewer_bjm(response){
//	alert('aaa');
	try { console.log(response); } catch(e) { }
	if(response.success){
		//$('#viewerCab').html('<OBJECT classid="clsid:9E93A6E5-4247-416D-BA9C-7485ED08B23A" codebase="'+ response.viewerCabUrl + '" id="EDIActiveXT" style="display: none;"></OBJECT>');

		// Daview 수정 (2020.02.06)
		// 다뷰 실행 했을때 문서(HWP, DOC, DOCX, XLS, XLSX, PPT, PPTX, 확장자면 창크기에 맞추기[A] 적용 상태에서 뷰잉
		if('E' === AUTH_SITE) {
			//외부 사용자 - 뷰어 출력 버튼 X
			EDIActiveXT.Options = getViewerOptionPrefix(response.extName) + '-SC:001000010000-HC:1111111110';
		}
		else{
			//내부 사용자 - 뷰어 출력 버튼 X (생산기술자료만 뷰어 출력 버튼 O)
			if("32" == response.authLevel || "PRODUCT" == response.requestType ) {
				EDIActiveXT.Options = getViewerOptionPrefix(response.extName) + '-SC:001000010000';
			}else{
				EDIActiveXT.Options = getViewerOptionPrefix(response.extName) + '-SC:000000000000';
			}
		}

		try {console.log("watermarkInfo 뷰잉 : " + response.watermarkInfo);}catch(e) {}


		if(response.filePath.indexOf('|') === -1) {
			response.fileNm = response.fileNm.split('&amp;').join('&');
			EDIActiveXT.viewfilename = decodeURIComponent(response.fileNm.split('&').join('&&'));
		}

		response.filePath = response.filePath.split("&amp;").join("&");

		var arr = response.filePath.split('filename=$');
		EDIActiveXT.WaterMark = response.watermarkInfo;
		// Daview 수정 (2020.02.06)
		// 파일명에 &들어간 경우 해결하기 위한 코드.
		if(response.filePath.indexOf("filename=") > -1) {
			var arr = response.filePath.split('filename=$');
			var tmpPath = "";


			var reg = /(.*?)\.(dgn)$/;
		  	if(arr[1].match(reg)) {
					tmpPath = arr[0] + "filename=$" + arr[1];
			}
		  	else{
				try {
					tmpPath = arr[0] + "filename=$" + encodeURIComponent(arr[1]);
				}
				catch(e) { }
			}

			EDIActiveXT.Original = tmpPath;
			//EDIActiveXT.Original = arr[0] + "filename=$" + arr[1];
		}
		else {
//			console.log("http://192.168.0.65:8080/DaVuForEG/DaViewSvc?ediauto=T&filename=$" + encodeURIComponent(response.filePath));
			//EDIActiveXT.Original = "http://192.168.0.65:8080/DaVuForEG/DaViewSvc?ediauto=T&filename=$" + encodeURIComponent(response.filePath);
			//var fileUrl = "\\\\192.168.0.158\\share\\DOCS\\OUT\\1111_001.SVG|1111_002.SVG|1111_003.SVG|1111_004.SVG";
			//var fileUrl = "\\\\192.168.0.158\\share\\DOCS\\OUT\\1111.SVG";// 원본 파일이 같은 경로에 있을때 | 하이픈으로 구분
			//var fileUrl = "D:\\DOCS\\OUT\\[열람]QAR-60349773-RA.PDF||[열람]QAR-60349773-RA - 복사본.PDF";// 원본 파일이 같은 경로에 있을때 | 하이픈으로 구분
			EDIActiveXT.Original = encodeURIComponent(fileUrl);
		}
		EDIActiveXT.CreateAndView();
	}else{
		if('NO_SUPPORT_EXT' == response.failType) {
			alertMessage(response.failReason + g_msg('msg.noSuportExtPrint'), function(){			//파일은 출력을 지원하지 않습니다.
				$(this).dialog("close");
			});
		}else if('DESTROY' === response.failType) {
			alertMessage(response.failReason, function(){
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg('msg.noExistFile'), function(){										//파일이 존재하지 않습니다.
				$(this).dialog("close");
			});
		}
	}
}


var EXT = ['exe', 'zip'];

function openPrintViewer(requestType, gridId, userType){

	var param = {
		  requestType : requestType
	};
	var aJsonArray = new Array();
	var isExt = true;

	//var protectYn = "N";	// 방산기술자료 - 방산기술여부
	var protectList = [];	// 방산기술자료 - 방산기술자료 체크할 목록
	var auth = true;		// 방산기술자료 - 권한
	var objectType = '';	// 방산기술자료 - 자료유형
	var outsideAuth = getOutsideProtectYn();	// 방산기술자료 - 외부사용자 권한
	
	var objectId = '';

	var $grid = $("#" + gridId);
	var arr = $grid.getGridParam('selarrrow');

	//alert('arr.length = '+ arr.length);

	if(0 >= arr.length) {
		alertMessage(g_msg("msg.noSelectedItem"));
		return;
	} 
	
	//if( arr.length === 1 ) {
		// 1개면 바로 뷰어 띄움 테스트
		//	var ret = window.open('/inside/distribution/docPdfListRequest/selectItem2?objectType='+objectType+'&file='+objectId, '_blank', '', false);
	//}


	$.each(arr, function(index, item){
		var data = $grid.jqGrid('getRowData', item);
		var fileExt = data.fileOrgNm;

		if(undefined === fileExt) {
			fileExt = data.orgFileNm;
		}

		fileExt = fileExt.substring(fileExt.lastIndexOf(".") + 1, fileExt.length).toLowerCase();
		for(var i=0; i<EXT.length; i++){
			if(fileExt === EXT[i]) {
				isExt = false;
				break;
			}
		}

		// 23.07.07 (yskim) Check destroy status
		if(data.destroyStatusCd == '3'
			|| data.destroyStatusCd == '2'
			|| data.destroyStatusCd == '1'){
			alertMessage(g_msg('msg.printDestroy'));
			auth = false;

			return false;
		}

		if('I' === AUTH_SITE) {
			if(data.printCount >= 3){ // 0>1 , 1>2 , 2>3 .  2초과 또는 3이상일때 프린트 횟수 막아야함
			   alertMessage(g_msg('msg.printCount'));  // 출력횟수 초과
				auth = false;
				return false;
			}

			if(data.useEndYmdYn == 'Y'){
				alertMessage(g_msg('msg.printUseEndYmd')); // 출력기한 초과
				auth = false;
				return false;
			}

			if(USER_CD != data.requestUserCd){
				alertMessage(g_msg('msg.noAuthPrint')); // 권한 없음
				auth = false;
				return false;
			}
		}



		else if('E' === AUTH_SITE) {
			if(!getOutsideCompanyAuthYn()) {
				alertMessage(g_msg('msg.stopDealCompany'));
				auth = false;
				return false;
			}
		}

		if( "Y"===data.protectYn && "UNREG" !== requestType && "PRODUCT" !== requestType){
			if('E' === AUTH_SITE && !outsideAuth) {
				alertMessage(g_msg('msg.noAuthProtectRequest'));
				auth = false;
				return false;
			}
			else if('I' === AUTH_SITE) {
				if(requestType != "PRINT") {
					protectList = [data.objectId];
					auth = getProtectAuthYn(data.objectType, protectList);
					if(!auth) {
						return false;
					}
				}
			}
		}
	});

	if(!auth) {
		return;
	}
	
	var strArrObjectID = 'merge';
	var list = [];
	//alert(' strArrObjectID = ' +  strArrObjectID);
	if(isExt){		
		$.each(arr, function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			param.requestNo = data.requestNo;
			param.fileNo = data.fileNo;
			param.objectId = data.objectId;
			param.userType = userType;
			param.fileNo = data.fileNo;


			var wmType = '';
			if(userType === "IN"){
				if("Y" === data.protectYn){							// 방산인 경우엔 무조건 방산 워터마크로..
					wmType = 'PROTECT';
				}
				// 2020.01.23 윤정훈 - [고객 요구사항] 내부사용자일 경우 방산기술자료만 워터마크를 찍어야 함.
				// 2020.03.25 윤정훈 - 출력시에 반드시 워터마크를 찍어야 함.
				else {
					wmType = "CLASSIFIED";
				}
			}else if(userType === 'OUT') {
				if("Y" === data.protectYn){							// 방산인 경우엔 무조건 방산 워터마크로..
					wmType = 'PROTECT';
				}else {
					wmType = "GENERAL";
				}
			}

			param.watermarkType = wmType;

			console.log(param);
			
			//alert("data.protectYn= " + data.protectYn);
			//alert("wmType= " + wmType);
			//alert(' objectId = ' +  data.objectId);
			
	//				aJsonArray.push(data);
			//callAjax(param, '/common/viewer/getPrintInfo', callPrint, 'json');
			strArrObjectID = strArrObjectID + '__' + data.objectId + '_' + data.objectType + '_' + wmType;
		});
		
		param.objectId = strArrObjectID.replace('merge__','');
		//alert(' strArrObjectID = ' +  param.objectId);

		// 출력버튼 눌렀을때 grid의 출력횟수 증가하게 하기 위해서. 원래 콜백함수 호출, 그 이후에 페이지 새로고침.
		callAjax(param, '/common/viewer/getMergePrintInfo', function(response){
			callPrint(response);
			location.reload();
		}, 'json');


	} else {
		alertMessage(g_msg('msg.exceptExt') + g_msg('msg.noSuportExtPrintPlzConfirm'), function(){	//파일은 출력을 지원하지 않습니다. 확인해 주시기 바랍니다.
			$(this).dialog("close");
		});
	}

}

function openPrintViewer_old(requestType, gridId, userType){

//	$('#viewerCab').html('<OBJECT classid="clsid:9E93A6E5-4247-416D-BA9C-7485ED08B23A" codebase="http://192.168.0.65:8080/EDIActiveXT(NSU-8-99-4-0).cab#Version=8,99,4,0" id="EDIActiveXT" style="display: none;"></OBJECT>');
////	EDIActiveXT.Options = '- PRT';
////	EDIActiveXT.WaterMark = response.watermarkInfo;		// 네트워크 드라이브 워터마크	서버OS   네트워크 드라이브
//	EDIActiveXT.WaterMark = 'http://192.168.0.225\\hh.gif|50|50|Arial|-1|0000010|10#[경고]본 도면은 당사 자산으로 무단 사용 및 복제를 금하고,|50|50|Arial|-1|0000010|100#한화디펜스 사내용 입니다|70|70|Arial|-1|0000020|100';
////	EDIActiveXT.WaterMark = 'http://192.168.0.225\\hh.gif|50|50|Arial|-1|0000010|10#[경고]본 도면은 당사 자산으로 무단 사용 및 복제를 금하고,|50|50|Arial|-1|0000010|100#한화디펜스 사내용 입니다|70|70|Arial|-1|0000020|100';
//	EDIActiveXT.Options = '-WM:WIDTH-BC:WHITE';
//
//	EDIActiveXT.Original = 'http://192.168.0.65:8080/DaViewForEG/DaViewSvc?ediauto=T&filename=$Y:\\문서2.docx';
//	EDIActiveXT.CreateAndView();




	var param = {
		  requestType : requestType
	};
	var aJsonArray = new Array();
	var isExt = true;

	//var protectYn = "N";	// 방산기술자료 - 방산기술여부
	var protectList = [];	// 방산기술자료 - 방산기술자료 체크할 목록
	var auth = true;		// 방산기술자료 - 권한
	var objectType = '';	// 방산기술자료 - 자료유형
	var outsideAuth = getOutsideProtectYn();	// 방산기술자료 - 외부사용자 권한

	var $grid = $("#" + gridId);
	var arr = $grid.getGridParam('selarrrow');

	if(0 >= arr.length) {
		alertMessage(g_msg("msg.noSelectedItem"));
		return;
	}

	$.each(arr, function(index, item){
		var data = $grid.jqGrid('getRowData', item);
		var fileExt = data.fileOrgNm;

		if(undefined === fileExt) {
			fileExt = data.orgFileNm;
		}

		fileExt = fileExt.substring(fileExt.lastIndexOf(".") + 1, fileExt.length).toLowerCase();
		for(var i=0; i<EXT.length; i++){
			if(fileExt === EXT[i]) {
				isExt = false;
				break;
			}
		}

//		if('I' === AUTH_SITE) {
//			if(data.destroyStatusCd == '3'){
//			   alertMessage(g_msg('msg.printDestroy'));
//				auth = false;
//				return false;
//			}
//			if(data.printCount > 3){
//			   alertMessage(g_msg('msg.printCount'));
//				auth = false;
//				return false;
//			}
//
//			if(data.useEndYmdYn == 'Y'){
//				alertMessage(g_msg('msg.printUseEndYmd'));
//				auth = false;
//				return false;
//			}
//
//			if(USER_CD != data.requestUserCd){
//				alertMessage(g_msg('msg.noAuthPrint'));
//				auth = false;
//				return false;
//			}
//		}
//		else if('E' === AUTH_SITE) {
//			if(!getOutsideCompanyAuthYn()) {
//				alertMessage(g_msg('msg.stopDealCompany'));
//				auth = false;
//				return false;
//			}
//		}
//
//		if( "Y"===data.protectYn && "UNREG" !== requestType && "PRODUCT" !== requestType){
//			if('E' === AUTH_SITE && !outsideAuth) {
//				alertMessage(g_msg('msg.noAuthProtectRequest'));
//				auth = false;
//				return false;
//			}
//			else if('I' === AUTH_SITE) {
//				if(requestType != "PRINT") {
//					protectList = [data.objectId];
//					auth = getProtectAuthYn(data.objectType, protectList);
//					if(!auth) {
//						return false;
//					}
//				}
//			}
//		}
	});

//	if(!auth) {
//		return;
//	}

	if(isExt){
		$.each(arr, function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			param.requestNo = data.requestNo;
			param.fileNo = data.fileNo;
			param.objectId = data.objectId;
			param.userType = userType;

			var wmType = '';
			if(userType === "IN"){
				if("Y" === data.protectYn){							// 방산인 경우엔 무조건 방산 워터마크로..
					wmType = 'PROTECT';
				}
				// 2020.01.23 윤정훈 - [고객 요구사항] 내부사용자일 경우 방산기술자료만 워터마크를 찍어야 함.
				// 2020.03.25 윤정훈 - 출력시에 반드시 워터마크를 찍어야 함.
				else {
					wmType = "CLASSIFIED";
				}
			}else if(userType === 'OUT') {
				if("Y" === data.protectYn){							// 방산인 경우엔 무조건 방산 워터마크로..
					wmType = 'PROTECT';
				}else {
					wmType = "GENERAL";
				}
			}

			param.watermarkType = wmType;

			console.log(param);

	//				aJsonArray.push(data);
			callAjax(param, '/common/viewer/getPrintInfo', callPrint, 'json');
		});
	} else {
		alertMessage(g_msg('msg.exceptExt') + g_msg('msg.noSuportExtPrintPlzConfirm'), function(){	//파일은 출력을 지원하지 않습니다. 확인해 주시기 바랍니다.
			$(this).dialog("close");
		});
	}

//		param.list = aJsonArray;
//		param.usetType = usetType;
//		callAjax(param, '/common/viewer/getPrintInfo', callPrint, 'json');
}


/*
 * 출력
 */
 
function callPrint(response){
	try { console.log(response); } catch(e) { }
	if(response.success){
		
		objectType = 'merge';		
		file = response.objectId;
		wmType = response.watermarkInfo;

		var ret = window.open(response.filePath+'&wmType='+wmType, '_blank', '', false);
		//var ret = window.open('/inside/distribution/docPdfLinkRequest/selectItem2?objectType='+objectType+'&file='+objectId+'&wmType'+wmType, '_blank', '', false);
		//var ret = window.open('/inside/distribution/docPdfLinkRequest/selectItem2?objectType='+objectType+'&file='+objectId+'&wmType'+wmType, '_blank', '', false);
	}else{
		printSuccess = false;
		if('NO_SUPPORT_EXT' == response.failType) {
			alertMessage(response.failReason + g_msg('msg.noSuportExtPrint'), function(){	//파일은 출력을 지원하지 않습니다.
				$(this).dialog("close");
			});
		} else if('OVER_PRINT_COUNT' == response.failType) {
			alertMessage(response.failReason + g_msg('msg.noPrintOverPrintCount'), function(){	//파일의 출력횟수가 초과되어 출력 할 수 없습니다.
				$(this).dialog("close");
			});
		} else if('DESTROY' === response.failType) {
			alertMessage(response.failReason, function(){
				$(this).dialog("close");
			});
		} else{
			alertMessage(g_msg('msg.noExistFile'), function(){	//파일이 존재하지 않습니다.
				$(this).dialog("close");
			});
		}
	}
}

function callPrint_old(response){
	try { console.log(response); } catch(e) { }
	if(response.success){
		//$('#viewerCab').html('<OBJECT classid="clsid:9E93A6E5-4247-416D-BA9C-7485ED08B23A" codebase="'+ response.viewerCabUrl + '" id="EDIActiveXT" style="display: none;"></OBJECT>');
		EDIActiveXT.Options = '-PRT-HC:1111111110';
		EDIActiveXT.WaterMark = response.watermarkInfo;		// 네트워크 드라이브 워터마크	서버OS   네트워크 드라이브

		if(response.filePath.indexOf('|') === -1) {
			EDIActiveXT.viewfilename = decodeURIComponent(response.fileOrgNm.split('&').join('&&'));
		}

		EDIActiveXT.Original = response.filePath;
		EDIActiveXT.CreateAndView();
	}else{
		printSuccess = false;
		if('NO_SUPPORT_EXT' == response.failType) {
			alertMessage(response.failReason + g_msg('msg.noSuportExtPrint'), function(){	//파일은 출력을 지원하지 않습니다.
				$(this).dialog("close");
			});
		} else if('OVER_PRINT_COUNT' == response.failType) {
			alertMessage(response.failReason + g_msg('msg.noPrintOverPrintCount'), function(){	//파일의 출력횟수가 초과되어 출력 할 수 없습니다.
				$(this).dialog("close");
			});
		} else if('DESTROY' === response.failType) {
			alertMessage(response.failReason, function(){
				$(this).dialog("close");
			});
		} else{
			alertMessage(g_msg('msg.noExistFile'), function(){	//파일이 존재하지 않습니다.
				$(this).dialog("close");
			});
		}
	}
}


function download(gridId, type){

	if($("#"+gridId).getGridParam('selarrrow').length < 1){
		alertMessage(g_msg('msg.noSelectData'), function(){			//선택된 데이터가 없습니다.
			$(this).dialog("close");
		});
		return false;
	}

	var prvType = '';
	var nextType;
	var isSuccess = true;
	var jsonArr = new Array();

	$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#" + gridId).jqGrid('getRowData', item);
//		 && "UNREG" !== requestType && "PRODUCT" !== requestType
		var protectYn = data.protectYn;

		if(undefined == protectYn) {
			protectYn = 'N';
		}

		if('E' === AUTH_SITE) {
			if('Y' === protectYn && !getOutsideProtectYn()) {
				alertMessage(g_msg('msg.noAuthProtectRequest'));
				isSuccess = false;
				return false;
			}

			if(!getOutsideCompanyAuthYn()) {
				alertMessage(g_msg('msg.stopDealCompany'));
				isSuccess = false;
				return false;
			}
		}
		else {
			//auth = getProtectAuthYn(objectType, protectList);
		}

//		console.log(data);
		if("VIEWING" === data.distributeTypeCd) {
			alertMessage(g_msg('msg.includeViewFile'), function(){	//보기만 되는 파일이 포함되어있습니다. 확인해주시기 바랍니다.
				$(this).dialog("close");
			});
			isSuccess = false;
		}
		if('' === prvType) {
			prvType = data.distributeTypeCd;
		}else {
			nextType = data.distributeTypeCd;
			if(prvType!=nextType){
				alertMessage(g_msg('msg.notSameDistType'), function(){	//동일한 배포 유형끼리만 다운가능합니다. 확인해 주시기 바랍니다.
					$(this).dialog("close");
				});
				isSuccess = false;
			}
		}
		if ("이전버전" === data.destroyTypeNm ){
			alertMessage(g_msg('msg.beforeVersion'))
		}


		var htmlString = data.downloadCount;
		var dummyDiv = document.createElement('div');
		dummyDiv.innerHTML = htmlString;
		var downloadCount = dummyDiv.textContent || dummyDiv.innerText;
		var countNumber = parseInt(downloadCount, 10);
		console.log("countNumber >>>>>>>>>>>>>>>> " + countNumber);

		if (3 === countNumber){
			alertMessage(g_msg('msg.downloadCountOver'))
		}

		jsonArr.push(data);
	});
	if(isSuccess){
		if(useNonActiveX) {
			var param = {
				list : jsonArr
			   , reqType : type
			   , gridId : gridId
			   , distributeTypeCd : nextType
			   , DistributeTypeCd : prvType
			};
			console.log("nonActiveX");
			console.log(param);

			$.ajax({
				url: '/common/updown/downloadData'
				, type : "POST"
				, cache: false
				, dataType : 'json'
				, async: true
				, contentType: "application/json"
				, data : JSON.stringify(param)
				, success : function(response){
					console.log(response);
					callDownload(response);
				}
				,error : function(e){
					console.log("error");
					console.log(e);
					if(e.status==401){
						parent.parent.location.href = "/login/logout";
					}else if(e.status == 403){
						alertMessage(g_msg("msg.accessDenied"));
					}else{
						alertMessage(g_msg("msg.beforeVersion"));
						// 현재버전이 이전버전 입니다. 다시 배포해주세요.
						console.log(e);
					}
				}
			});
		}
		else {
			openDialogPopup("/common/updown/openFileDownPopup", {gridId : gridId, distributeTypeCd : nextType, DistributeTypeCd : prvType, reqType: type}, "popupDialog", 'm', 450);
		}
	}
}

/**
 * 선택된 자료들이 방산 권한이 있는지 확인
 * @param objectType
 * @param protectList
 * @returns
 */
function getProtectAuthYn(objectType, protectList) {
	var protectParam = {
		objectId: protectList.join(',')
		, objectType: objectType
	};

	var auth = true;

	callAjax(protectParam, '/inside/authorization/checkProtectAuth', function(response){
		if(response.length > 0) {
			alertMessage(g_msg('msg.noAuthProtectRequest'));
			auth = false;
		}
	}, 'json', false);

	return auth;
}

/**
 * 출력요청, 배포요청 ( 내부사용자 )
 * @param objectType
 * @param requestType
 * @returns
 */
function requestInsideUser(requestType, objectType, gridId) {
	var protectYn = "N";
	var productProtectYn= "N";
	var developmentProtectYn= "N";
	var protectList = [];
	var url = '';

	if($("#" + gridId).getGridParam('selarrrow').length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}

	if('PRINT' === requestType) {
		url = '/inside/distribution/commonRequest/commonPrintRequestPopup';
	}
	else {
		url = '/inside/distribution/commonRequest/' + objectType.toLowerCase() + 'RequestPopup';
	}

	$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
		var data = $("#" + gridId).jqGrid('getRowData', item);
		if( "Y"===data.protectYn ){
			protectList.push(data.objectId);
			protectYn = "Y";
		}
		if( ("Development"===data.businessTypeCd) && ("Y" === data.protectYn) ){		//개발
			developmentProtectYn = "Y";
		}else if( ("Production"===data.businessTypeCd) && ("Y" === data.protectYn) ){	//양산
			productProtectYn = "Y";
		}
//			if( ( "Y" == productProtectYn) && ( "Y" == deployProtectYn) ){	//값 둘다 있는 경우 break
//				return false;
//			}
	});

//	방산기술 권한이 있는지 여부. 내부 사용자가 출력, 배포 요청을 할 경우에는 방산기술권한 여부를 판단하지 않는다.
//	var auth = true;
//
//	if("Y" === protectYn) {
//		auth = getProtectAuthYn(objectType, protectList);
//	}
//
//	if(auth) {
		var popupHeight = Math.min($(window).height() - 100, 800);
		var popupExtraClass = ('PRINT' === requestType) ? 'popup-common popup-print-request' : 'popup-common popup-request';
		openDialogPopup(url, {protectYn: protectYn, developmentProtectYn: developmentProtectYn, productProtectYn : productProtectYn, requestType: objectType, approvalRequestType : requestType}, "popupDialog", 'l', popupHeight, true, popupExtraClass);
//	}
}

/**
 * 리비전 갱신 요청 ( 내부사용자 )
 * @param objectType
 * @param requestType
 * @returns
 */
function revisionUpdateInsideUser(objectType, gridId) {
	// 선택된 행의 배열을 selectedRows 변수에 저장
	var selectedRows = $("#" + gridId).getGridParam('selarrrow');

	if(selectedRows.length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}

	if(selectedRows.length > 1){
		alertMessage(g_msg('msg.chooseOneDrawing'));
		return false;
	}

	var url = '/inside/distribution/commonRequest/' + objectType.toLowerCase() + 'RevisionUpdatePopup';
	var data = $("#" + gridId).jqGrid('getRowData', selectedRows[0]);
	console.log("data: ", data);

	var popupHeight = Math.min($(window).height() - 100, 620);
	openDialogPopup(url, data, "popupDialog", 'l', popupHeight, true, 'popup-common popup-revision-update');
}

function checkVersionInsideUser(objectType, gridId) {
	var selectedRows = $("#" + gridId).getGridParam('selarrrow');

	if(selectedRows.length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}

	if(selectedRows.length > 1){
		alertMessage(g_msg('msg.chooseOneDrawing'));
		return false;
	}

	var url = '/inside/distribution/commonRequest/' + objectType.toLowerCase() + 'VersionCheckPopup';
	var data = $("#" + gridId).jqGrid('getRowData', selectedRows[0]);
	console.log("data: ", data);

	var popupHeight = Math.min($(window).height() - 100, 500);
	openDialogPopup(url, data, "popupDialog", 'l', popupHeight, true, 'popup-common popup-version-check');
}

function checkVersionOutsideUser(objectType, gridId) {
	var selectedRows = $("#" + gridId).getGridParam('selarrrow');

	if(selectedRows.length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}

	if(selectedRows.length > 1){
		alertMessage(g_msg('msg.chooseOneDrawing'));
		return false;
	}

	var url = '/outside/drawing/approvalStatus/' + objectType.toLowerCase() + 'VersionCheckPopup';
	var data = $("#" + gridId).jqGrid('getRowData', selectedRows[0]);
	console.log("data: ", data);

	openDialogPopup(url, data, "popupDialog", 'm', 500, true, 'popup-common popup-version-check');
}

/**
 * 이용가능한 업체인지 확인
 * @returns
 */
function getOutsideCompanyAuthYn() {
	var protectParam = {};

	var auth = true;

	callAjax(protectParam, '/inside/authorization/getOutsideCompanyAuthYn', function(response){
		if("N" !== response.delYn) {
			auth = false;
		}
	}, 'json', false);

	return auth;
}

/**
 * 방산기술 권한이 있는지 여부 ( 외부사용자. true/false )
 * @returns
 */
function getOutsideProtectYn() {
	var protectParam = {};

	var auth = true;

	callAjax(protectParam, '/inside/authorization/checkOutsideProtectAuth', function(response){
		if("Y" !== response.protectYn) {
			auth = false;
		}
	}, 'json', false);

	return auth;
}

/**
 * 배포방식, 배포유형으로 유효기간을 구한다.
 * @param purpose - 배포유형
 * @param distributeTypeCd - 배포방식
 * @returns
 */
function getDeployTerm(purpose, distributeTypeCd) {
	if('CHECK' === purpose) { return '3'; }
	if('REFER' === purpose) { return '3'; }
	if('ESTIMATE' === purpose) { return '3'; }
	if('MANUAL' === purpose) {
		if('hdCADRegisteredDrawing' === distributeTypeCd || 'hdCADApproveRequestDrawing' === distributeTypeCd || 'hdQuickChangeAction' === distributeTypeCd) {
			return '6';
		}
		else {
			return '60';
		}
	}

	if('PRODUCT' === purpose) {
		if('hdCADRegisteredDrawing' === distributeTypeCd || 'hdCADApproveRequestDrawing' === distributeTypeCd || 'hdQuickChangeAction' === distributeTypeCd) {
			return '6';
		}
		else {
			return '60';
		}
	}

	return '3';
}
/*[KAI] DownLoad*/
var HEADER_FILE_DOWNLOAD = "0001";
var I_WS_SIZE_HEADER = 4;
var I_WS_SIZE_UUID = 32;
var I_WS_SIZE_DATA = 260;
var I_WS_FOLDER_NAME = 128;
var I_WS_TOTAL_LEN = I_WS_SIZE_HEADER + I_WS_SIZE_UUID + I_WS_SIZE_DATA + I_WS_FOLDER_NAME;
var downloadSeqList = [];
var downloadStateBySeq = {};
var downloadMetaBySeq = {};
var statusPollingBySeq = {};
var downloadStatusTimerBySeq = {};
var DOWNLOAD_STATUS_TIMEOUT_MS = 300000;
var downloadCleanupBound = false;
var downloadTotalCount = 0;
var downloadFinishedCount = 0;
var downloadHasFailure = false;
var downloadFinalNotified = false;
var suppressDownloadFinalAlert = false;

// 13자리 문자열 생성
function makeLegacySequence(){
	// 최신 브라우저: randomUUID 사용 (호출 예외 시 fallback)
	try {
		if (window.crypto && window.crypto.randomUUID) {
			var uuid = window.crypto.randomUUID();
			if (uuid && typeof uuid === "string") { return uuid.replace(/-/g, ""); }
		}
	} catch (e) { console.log("randomUUID unavailable, fallback to hex sequence", e); }

	// 구형 브라우저 fallback: 32자리 hex 생성
	var bytes = null;
	if (window.crypto && typeof window.crypto.getRandomValues === "function" && typeof Uint8Array !== "undefined") {
		bytes = new Uint8Array(16);
		window.crypto.getRandomValues(bytes);
	} else {
		bytes = [];
		for (var i = 0; i < 16; i++) { bytes.push(Math.floor(Math.random() * 256)); }
	}

	var hex = "";
	for (var j = 0; j < 16; j++) { hex += ("0" + bytes[j].toString(16)).slice(-2); }
	return hex;
}
function getLegacyByteLength(str){return str.length;}
// 문자열 > ArrayBuffer 변환
function toLegacyArrayBuffer(str){
	var buffer = new ArrayBuffer(str.length);
	var byteView = new Uint8Array(buffer);
	for(var i = 0; i < str.length; i++){ byteView[i] = str.charCodeAt(i) & 0xff; }
	return buffer;
}
function resolveDownloadObjectType(item){
	if (item && item.objectType && item.objectType !== "") return String(item.objectType).toUpperCase();
	if (window.downloadRequestObjectType && window.downloadRequestObjectType !== "") return String(window.downloadRequestObjectType).toUpperCase();
	return "";
}
	function buildWsDownloadRequestName(item, startRes){
		if (startRes && startRes.downloadRequestKey) { return String(startRes.downloadRequestKey); }
		var objectType = resolveDownloadObjectType(item);
		if (objectType === "SW" || objectType === "SECP" || objectType === "SECP_PARTDOC") {
			return item && item.fileSeq ? String(item.fileSeq) : "";
		}
		if (objectType === "DOC" || objectType === "DRAWING") {
			return item && item.fileSeq ? String(item.fileSeq) : "";
		}
		if (item && item.fileNm) { return String(item.fileNm); }
	return startRes && startRes.savedFileName ? String(startRes.savedFileName) : "";
}
function buildLegacyDownloadPacket(data, uuid, folderName){
	data = data || "";
	uuid = uuid || "";
	folderName = folderName || "";

	if (getLegacyByteLength(uuid) !== I_WS_SIZE_UUID) { return { ok: false, error: "UUID must be 32 bytes." }; }

	if (getLegacyByteLength(data) > I_WS_SIZE_DATA) { return { ok: false, error: "DATA exceeds 260 bytes." }; }

	if (getLegacyByteLength(folderName) > I_WS_FOLDER_NAME) { return { ok: false, error: "FOLDER_NAME exceeds 128 bytes." }; }

	var packet =
		HEADER_FILE_DOWNLOAD +
		uuid +
		data.padEnd(I_WS_SIZE_DATA, '\0') +
		folderName.padEnd(I_WS_FOLDER_NAME, '\0');

	if (getLegacyByteLength(packet) !== I_WS_TOTAL_LEN) { return { ok: false, error: "Packet length is not " + I_WS_TOTAL_LEN + " bytes." }; }
	return { ok: true, packet: packet, seq: uuid };
}

function resetDownloadRuntimeState() {
	downloadSeqList = [];
	downloadStateBySeq = {};
	downloadMetaBySeq = {};
	statusPollingBySeq = {};
	downloadStatusTimerBySeq = {};
	downloadTotalCount = 0;
	downloadFinishedCount = 0;
	downloadHasFailure = false;
	downloadFinalNotified = false;
	suppressDownloadFinalAlert = false;
}

function isTerminalStatus(status) {
	return status === 'COMPLETED' || status === 'FAILED';
}

function reportFileStatus(wsSeq, status, extra) {
	var prevStatus = downloadStateBySeq[wsSeq];
	downloadStateBySeq[wsSeq] = status;
	if (prevStatus === status) {
		return;
	}
	if (typeof window.onDownloadStatusUpdate === 'function') {
		window.onDownloadStatusUpdate({
			wsSeq: wsSeq,
			status: status,
			meta: downloadMetaBySeq[wsSeq] || {},
			extra: extra || null
		});
	}
}

function completeOneFile(wsSeq, status, extra) {
	var prev = downloadStateBySeq[wsSeq];
	if (downloadStatusTimerBySeq[wsSeq]) {
		clearTimeout(downloadStatusTimerBySeq[wsSeq]);
		delete downloadStatusTimerBySeq[wsSeq];
	}
	statusPollingBySeq[wsSeq] = false;
	reportFileStatus(wsSeq, status, extra);

	if (!isTerminalStatus(prev) && isTerminalStatus(status)) {
		downloadFinishedCount++;
		if (status !== 'COMPLETED') { downloadHasFailure = true; }
		finalizeDownloadResult(wsSeq, status, extra);
	}

	if (!downloadFinalNotified && downloadTotalCount > 0 && downloadFinishedCount >= downloadTotalCount) {
		downloadFinalNotified = true;
		if (suppressDownloadFinalAlert) {
			return;
		}
		function safeMessage(msgKey, fallback) {
			try {
				if (typeof g_msg === "function") {
					var msg = g_msg(msgKey);
					if (msg != null && String(msg).length > 0) { return msg; }
				}
			} catch (e) { console.log("message resolve fail:", msgKey, e); }
			return fallback;
		}
		if (downloadHasFailure) {
			alertMessage(safeMessage('msg.downloadFail', '다운로드에 실패했습니다.'));
		} else {
			alertMessage(safeMessage('msg.downloadSuccess', '다운로드가 완료되었습니다.'));
		}
	}
}

function finalizeDownloadResult(wsSeq, status, extra) {
	if (!wsSeq) { return; }
	var errorMessage = "";
	if (extra) { errorMessage = extra.errorMessage || extra.message || extra.reason || ""; }
	callAjax(
		{
			wsSeq: wsSeq,
			status: status,
			errorMessage: errorMessage
		},
		'/common/updown/v2/finalize',
		function() {},
		'json',
		true,
		false
	);
}

function failPendingDownloads(reason) {
	for (var wsSeq in downloadStateBySeq) {
		if (!downloadStateBySeq.hasOwnProperty(wsSeq)) { continue; }
		if (isTerminalStatus(downloadStateBySeq[wsSeq])) { continue; }
		completeOneFile(wsSeq, 'FAILED', { reason: reason || 'download unavailable' });
	}
}

function failDownloadRowsWithoutSeq(fileList, reason) {
	if (!fileList || !fileList.length || typeof window.onDownloadStatusUpdate !== 'function') { return; }
	for (var i = 0; i < fileList.length; i++) {
		var rowKey = fileList[i] && fileList[i]._downloadRowKey ? fileList[i]._downloadRowKey : i;
		window.onDownloadStatusUpdate({
			wsSeq: "",
			status: 'FAILED',
			meta: { rowKey: rowKey },
			extra: { reason: reason || 'download unavailable' }
		});
	}
}

function bindDownloadCleanupOnUnload() {
	if (downloadCleanupBound) { return; }
	downloadCleanupBound = true;
	window.addEventListener('beforeunload', function() {
		for (var i = 0; i < downloadSeqList.length; i++) {
			cleanupDownload(downloadSeqList[i]);
		}
	});
}

function registerDownloadStart(wsSeq, item, reqType, onSuccess, onFail){
	//v2 start API 연결 시 활성화 - 다운로드 시작점/매칭키 전달
	function resolveObjectType(item) {
		if (item.objectType && item.objectType !== "") return item.objectType;
		if (item.dataOfferDocSeq && item.dataOfferDocSeq !== "") return "DOC";
		if (item.delvyCnfirmDocSeq && item.delvyCnfirmDocSeq !== "") return "DRAWING";
		if (window.downloadRequestObjectType && window.downloadRequestObjectType !== "") return window.downloadRequestObjectType;
		return "";
	}

	function resolveDocSeq(item) {
		// approvalStatus 그리드별 컬럼 흡수
		if (item.docSeq && item.docSeq !== "") return item.docSeq;
		if (item.dataOfferDocSeq && item.dataOfferDocSeq !== "") return item.dataOfferDocSeq;      // DOC
		if (item.delvyCnfirmDocSeq && item.delvyCnfirmDocSeq !== "") return item.delvyCnfirmDocSeq; // DRAWING
		return "";
	}

	var payload = {
		wsSeq: wsSeq,
			reqType: reqType || 'DISTRIBUTION',
			requestNo: item.requestNo || "",
			dataNo: item.dataNo || "",
			docSeq: resolveDocSeq(item),
			objectType: resolveObjectType(item),	// 기존
			fileNo: item.fileNo || "",
			fileSeq: item.fileSeq || "",
		fileExt: item.fileExt || "",
		fileNm: item.fileNm || "",
		orgFileNm: item.orgFileNm || ""
	};
	/* [DOWNLOAD-DEBUG-START] 웹소켓 패킷 생성 전 JS -> JAVA 전달값 확인용 로그 */
	console.log("[DOWNLOAD-DEBUG][V2-START][REQ]", {
		wsSeq: payload.wsSeq,
		reqType: payload.reqType,
		requestNo: payload.requestNo,
		dataNo: payload.dataNo,
		docSeq: payload.docSeq,
		objectType: payload.objectType,
		fileNo: payload.fileNo,
		fileSeq: payload.fileSeq,
		fileNm: payload.fileNm,
		orgFileNm: payload.orgFileNm
	});
	/* [DOWNLOAD-DEBUG-END] 웹소켓 패킷 생성 전 JS -> JAVA 전달값 확인용 로그 */

	$.ajax({
		url: '/common/updown/v2/start',
		type: 'POST',
		cache: false,
		dataType: 'json',
		async: true,
		global: false,
		contentType: 'application/json',
		data: JSON.stringify(payload),
		success: function(res) {
			console.log("[V2-START][RES]", wsSeq, res);
			if (res && res.success) {
				if (downloadSeqList.indexOf(wsSeq) === -1) {
					downloadSeqList.push(wsSeq);
				}
				if (typeof onSuccess === 'function') {
					onSuccess(res);
				}
				return;
			}
			if (typeof onFail === 'function') {
				onFail(res && res.message ? res.message : 'start failed');
			}
		},
		error: function(e) {
			console.log("[V2-START][ERR]", wsSeq, e && e.status, e && e.responseText, e);
			if (typeof onFail === 'function') {
				onFail('start request error: ' + (e && e.responseText ? e.responseText : 'no response body'));
			}
			if (e && e.status === 401) {
				parent.parent.location.href = "/login/logout";
			}
		}
	});
}

function notifyWsResultToServer(messageText) {
    callAjax(
        { message: messageText },
        '/common/updown/v2/ws-result',
        function(res) {
        	console.log("[WS-CHECK][WS-RESULT-ACK]", {
        		messageLength: messageText ? messageText.length : 0,
        		success: res && res.success,
        		status: res && res.status,
        		resultCode: res && res.resultCode,
        		message: res && res.message
        	});
        	if (!res || !res.success) {
        		console.warn("[WS-CHECK][WS-RESULT-ACK-FAIL]", {
        			rawMessage: messageText,
        			response: res
        		});
        	}
        },
        'json',
        true,
        false
    );
}

function pollDownloadStatus(wsSeq, onDone) {
	if (statusPollingBySeq[wsSeq]) {
		return;
	}
	statusPollingBySeq[wsSeq] = true;
	if (downloadStatusTimerBySeq[wsSeq]) {
		clearTimeout(downloadStatusTimerBySeq[wsSeq]);
	}
	downloadStatusTimerBySeq[wsSeq] = setTimeout(function() {
		if (!isTerminalStatus(downloadStateBySeq[wsSeq])) {
			console.warn("[V2-STATUS][TIMEOUT]", wsSeq);
			completeOneFile(wsSeq, 'FAILED', { reason: 'download timeout' });
			cleanupDownload(wsSeq);
			if (onDone) {
				onDone({ success: false, status: 'FAILED', message: 'download timeout' });
			}
		}
	}, DOWNLOAD_STATUS_TIMEOUT_MS);

	    var timer = setInterval(function() {
        callAjax(
            { wsSeq: wsSeq },
	            '/common/updown/v2/status',
	            function(res) {
	            	if (!res || !res.success) {
	            		console.warn("[V2-STATUS][FAIL]", wsSeq, res);
	            		return;
	            	}
	            	console.log("[V2-STATUS]", wsSeq, res);

                reportFileStatus(wsSeq, res.status, res);

	                if (res.status === 'COMPLETED' || res.status === 'FAILED') {
	                	clearInterval(timer);
						statusPollingBySeq[wsSeq] = false;
						if (downloadStatusTimerBySeq[wsSeq]) {
							clearTimeout(downloadStatusTimerBySeq[wsSeq]);
							delete downloadStatusTimerBySeq[wsSeq];
						}
	                    if (onDone) onDone(res);
	                }
            },
            'json',
            true,
            false
        );
    }, 1000);
}

function cleanupDownload(wsSeq) {
    callAjax(
        { wsSeq: wsSeq },
        '/common/updown/v2/cleanup',
        function() {},
        'json',
        true,
        false
    );
}

function callDownload(response){
	resetDownloadRuntimeState();
	bindDownloadCleanupOnUnload();
	console.log("callDownload response", response);
	/* [DOWNLOAD-DEBUG-START] 웹소켓 연결 전 다운로드 대상 원본 데이터 확인용 로그 */
	console.log("[DOWNLOAD-DEBUG][CALL-DOWNLOAD][PRE-WS]", {
		userNm: response && response.userNm ? response.userNm : "",
		downloadVolume: response && response.downloadVolume ? response.downloadVolume : "",
		listSize: response && response.list ? response.list.length : 0,
		list: response && response.list ? response.list.map(function(item, rowIndex) {
			return {
				rowIndex: rowIndex,
				requestNo: item && item.requestNo ? item.requestNo : "",
				dataNo: item && item.dataNo ? item.dataNo : "",
				docSeq: item && (item.docSeq || item.dataOfferDocSeq || item.delvyCnfirmDocSeq) ? (item.docSeq || item.dataOfferDocSeq || item.delvyCnfirmDocSeq) : "",
				objectType: resolveDownloadObjectType(item),
				fileNo: item && item.fileNo ? item.fileNo : "",
				fileSeq: item && item.fileSeq ? item.fileSeq : "",
				fileNm: item && item.fileNm ? item.fileNm : "",
				orgFileNm: item && item.orgFileNm ? item.orgFileNm : "",
				folderName: item && item.folderName ? item.folderName : ""
			};
		}) : []
	});
	/* [DOWNLOAD-DEBUG-END] 웹소켓 연결 전 다운로드 대상 원본 데이터 확인용 로그 */
	var webSocket = new WebSocket("ws://localhost:39229");
	var webSocketOpened = false;
	var webSocketFailureHandled = false;

	function handleWebSocketConnectionFailure(reason) {
		/* [DOWNLOAD-DEBUG-START] 웹소켓 연결 실패 시 패킷 후보값 확인용 로그 */
		if (response && response.list) {
			console.warn("[DOWNLOAD-DEBUG][CALL-DOWNLOAD][WS-FAIL-PACKET-CANDIDATE]", response.list.map(function(item, rowIndex) {
				var wsSeqCandidate = makeLegacySequence();
				var wsDownloadFileNameCandidate = buildWsDownloadRequestName(item, null);
				var wsFolderNameCandidate = item && item.folderName ? item.folderName : "";
				var packetCandidate = buildLegacyDownloadPacket(wsDownloadFileNameCandidate, wsSeqCandidate, wsFolderNameCandidate);
				return {
					rowIndex: rowIndex,
					wsSeqCandidate: wsSeqCandidate,
					packetDataCandidate: wsDownloadFileNameCandidate,
					objectType: resolveDownloadObjectType(item),
					fileSeq: item && item.fileSeq ? item.fileSeq : "",
					fileNm: item && item.fileNm ? item.fileNm : "",
					orgFileNm: item && item.orgFileNm ? item.orgFileNm : "",
					folderName: wsFolderNameCandidate,
					packetBuildOk: packetCandidate.ok,
					packetBuildError: packetCandidate.ok ? "" : packetCandidate.error,
					packetLength: packetCandidate.ok && packetCandidate.packet ? packetCandidate.packet.length : 0
				};
			}));
		}
		/* [DOWNLOAD-DEBUG-END] 웹소켓 연결 실패 시 패킷 후보값 확인용 로그 */
		if (webSocketFailureHandled) {
			return;
		}
		webSocketFailureHandled = true;
		downloadFinalNotified = true;
		suppressDownloadFinalAlert = true;
		webSocket.onerror = null;
		webSocket.onclose = null;
		webSocket.onmessage = null;
		try {
			webSocket.close();
		} catch (e) {
			console.log("[WS-CLOSE-IGNORE]", e);
		}
		failDownloadRowsWithoutSeq(response && response.list ? response.list : [], reason);
		failPendingDownloads(reason);
		alertMessage(g_msg('msg.websockerFailed'), function() {
			$(this).dialog("close");
			if (typeof window.closeDownloadPopup === 'function') {
				window.closeDownloadPopup();
				return;
			}
			if (typeof closePopup === 'function') {
				closePopup('popupDialog');
			}
		});
	}

	webSocket.onerror = function() {
		if (!webSocketOpened) {
			handleWebSocketConnectionFailure('websocket connection failed');
			return;
		}
		failPendingDownloads('websocket connection failed');
		alertMessage(g_msg('msg.websockerFailed'));
	};

	webSocket.onclose = function() {
		if (!webSocketOpened) {
			handleWebSocketConnectionFailure('websocket connection closed');
			return;
		}
		failPendingDownloads('websocket connection closed');
	};

	webSocket.onopen = function(){
		webSocketOpened = true;
		console.log("Connected");

		$("input[name=downloadServerIp]").val(response.config.updownServerIp);
		$("input[name=downloadServerPort]").val(response.config.updownServerPort);
		$("input[name=downloadLangCode]").val(response.config.updownLangCode);
		$("input[name=downloadUserAuth]").val(response.config.updownUserAuth);
		$("input[name=downloadSecretKey]").val(response.config.updownSecretKey);
		$("input[name=downloadIsSecurity]").val(response.config.updownIsSecurity);
		$("input[name=downloadVolume]").val(response.downloadVolume);

			var arrFileList = [];

			for (var i = 0; i < response.list.length; i++) {
				(function(item, rowIndex) {
					var arrFileInfo = [];
					arrFileInfo.push(rowIndex);
					arrFileInfo.push(item.fileNm);
					arrFileInfo.push(item.orgFileNm);
					arrFileInfo.push(item.fileSize);
					arrFileInfo.push(item.filePathNm);
					arrFileInfo.push("31");
					arrFileInfo.push(item.endDate);
					arrFileList.push(arrFileInfo.join('|'));

					var wsSeq = makeLegacySequence();
					var fileLabel = item.orgFileNm || item.fileNm || ("row-" + rowIndex);
					downloadMetaBySeq[wsSeq] = {
						rowKey: item._downloadRowKey || rowIndex,
						fileNm: item.fileNm || "",
						orgFileNm: item.orgFileNm || "",
						requestNo: item.requestNo || "",
						dataNo: item.dataNo || "",
						docSeq: item.docSeq || item.dataOfferDocSeq || item.delvyCnfirmDocSeq || "",
						folderName: item.folderName || ""
					};

					downloadTotalCount++;
					reportFileStatus(wsSeq, 'QUEUED');

					// 서버가 requestNo/docSeq/fileNo 등으로 REST 요청용 seq 조회/파일 수신
					registerDownloadStart(wsSeq, item, 'DISTRIBUTION', function(startRes) {
						// 웹소켓 DATA 구간에는 URL-safe 원본 파일명을 우선 사용하고,
						// 원본명이 비어 있을 때만 temp 저장 파일명으로 fallback 한다.
						var wsDownloadFileName = buildWsDownloadRequestName(item, startRes);
						var wsFolderName = item.folderName || "";
						downloadMetaBySeq[wsSeq].savedFileName = startRes && startRes.savedFileName ? startRes.savedFileName : "";
						if (!wsDownloadFileName) {
							console.error("download requestName missing:", startRes, item);
							completeOneFile(wsSeq, 'FAILED', { reason: 'download requestName missing', fileLabel: fileLabel });
							return;
						}
						var packetResult = buildLegacyDownloadPacket(wsDownloadFileName, wsSeq, wsFolderName);
						if (!packetResult.ok) {
							console.error("download packet build failed:", packetResult.error, wsDownloadFileName);
							completeOneFile(wsSeq, 'FAILED', { reason: packetResult.error, fileLabel: fileLabel });
							return;
						}
						reportFileStatus(wsSeq, 'DOWNLOADING');
						var buffer = toLegacyArrayBuffer(packetResult.packet);
						/* [DOWNLOAD-DEBUG-START] 웹소켓 송신 직전 패킷 데이터 확인용 로그 */
						console.log("[DOWNLOAD-DEBUG][WS-SEND]", {
							wsSeq: wsSeq,
							packetData: wsDownloadFileName || "",
							packetFull: packetResult.packet || "",
							packetHeader: packetResult.packet ? packetResult.packet.substring(0, I_WS_SIZE_HEADER) : "",
							packetPreview: packetResult.packet ? packetResult.packet.substring(0, I_WS_SIZE_HEADER + I_WS_SIZE_UUID + 40) : "",
							savedFileName: startRes && startRes.savedFileName ? startRes.savedFileName : "",
							fileSeq: item.fileSeq || "",
							orgFileNm: item.orgFileNm || "",
							fileNm: item.fileNm || "",
							objectType: resolveDownloadObjectType(item),
							folderName: wsFolderName,
							packetLength: packetResult.packet ? packetResult.packet.length : 0
						});
						/* [DOWNLOAD-DEBUG-END] 웹소켓 송신 직전 패킷 데이터 확인용 로그 */
						webSocket.send(buffer);
						pollDownloadStatus(wsSeq, function(finalState) {
							if (finalState && finalState.status) {
								completeOneFile(wsSeq, finalState.status, finalState);
							} else {
								completeOneFile(wsSeq, 'FAILED', { reason: 'status empty' });
							}
						});
					}, function(errorMessage) {
						completeOneFile(wsSeq, 'FAILED', { reason: errorMessage || 'start failed' });
					});

					sleep(100);
				})(response.list[i], i);
			}

			$("input[name=ShowArray]").val(arrFileList.join('|'));

			webSocket.onmessage = function(event) {
				// event.data 예: "174710000012300C:/temp/a.svg"
				var msg = typeof event.data === 'string' ? event.data : '';
				// 웹소켓 송수신 확인용: 수신 원문 로그
				console.log("[WS-CHECK][RECV]", msg);
				if (!msg || msg.length < 34) {
					console.warn("[WS-CHECK][RECV-IGNORED]", {
						reason: "message length < 36",
						length: msg ? msg.length : 0,
						rawMessage: msg
					});
					return;
				}

				notifyWsResultToServer(msg);
			};
	}
}
/* function callDownload(response) {
	console.log(" callDownload function 입장 ~ >> > >> > > > > > >> > > > > >>  > ");
	console.log("callDownload response");
	console.log(response);
	console.log("response.list.length >>> " + response.list.length);

	var webSocket = new WebSocket("ws://localhost:39229/websocketendpoint");

	webSocket.onopen = function(event) {
		console.log("Connected");
	$("input[name=downloadServerIp]").val(response.config.updownServerIp);
    $("input[name=downloadServerPort]").val(response.config.updownServerPort);
    $("input[name=downloadLangCode]").val(response.config.updownLangCode);
    $("input[name=downloadUserAuth]").val(response.config.updownUserAuth);
    $("input[name=downloadSecretKey]").val(response.config.updownSecretKey);
    $("input[name=downloadIsSecurity]").val(response.config.updownIsSecurity);
    $("input[name=downloadVolume]").val(response.downloadVolume); // 일반영역, 보안영역
//    $("input[name=downloadVolume]").val("R"); // 일반영역, 보안영역

	var arrFileList = new Array();

	console.log("response.list.length >>> " + response.list.length);
	list_length = response.list.length;

	// if (list_length == '0'){
	// 	console.log("횟수 초과 메세지 출력 ")
	// 	alertMessage(g_msg('msg.downloadCountOver'));
	// }

    for(var i=0 ; i < response.list.length ; i++){
        var arrFileInfo = new Array();
        var strRowKey = i;
        arrFileInfo.push(strRowKey);
        arrFileInfo.push(response.list[i].fileNm);		// uuid
        arrFileInfo.push(response.list[i].orgFileNm);	// 실제 파일 이름
        arrFileInfo.push(response.list[i].fileSize);
        arrFileInfo.push(response.list[i].filePathNm);
        arrFileInfo.push("31");
        arrFileInfo.push(response.list[i].endDate);
        arrFileList.push(arrFileInfo.join('|'));

		var flag = '0001';
		var fileName = response.list[i].fileNm;
		var paddedFileName = fileName.padEnd(260,'\0');
		var dataToSend = flag + paddedFileName;

		var buffer = new ArrayBuffer(dataToSend.length);
		var byteView = new Uint8Array(buffer);
		for (var j=0; j<dataToSend.length; j++){
			byteView[j] = dataToSend.charCodeAt(j);
			}

		// 다운로드 성공의 확인 버튼을 누르면 새로고침 => grid의 다운 횟수 증가를 보여주기 위해
		alertMessage(g_msg('msg.downloadSuccess'), function() {
			location.reload();
		});

		console.log(i);
		console.log(buffer);
		webSocket.send(buffer);
		sleep(100);
		}

		$("input[name=ShowArray]").val(arrFileList.join('|'));
		//$("input[name=ShowArray]").val(v);
		console.log("callDownload arrFileList");
		// console.log(arrFileList);
		console.log("##############");
		console.log($('#download').serialize());
	};

    // $.ajax({
    // 	url: 'http://localhost:30100/',
    // 	type: 'post',
    // 	cache: false,
    // 	async: true,
    // 	data: $('#downloadFrm').serialize(),
    // 	success: function(response) {
    // 		console.log(response);
    // 	},
    // 	error: function(e) {
    // 		console.log(e);
    // 	}
    // });
	webSocket.onerror = function (event){
		alertMessage(g_msg('msg.websockerFailed'));
		console.log("UnConnected");
	};
} */
