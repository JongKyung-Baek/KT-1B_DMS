var popupGridParam;
var popupGridId = 'gridRequestPopup';
var emptyArray = [];


$(function() {
	//파일 배포 변경 이벤트
	$('input[name=distributionType]').on('change', function(){
		if(true === $("#distributionTypefileDistribution").is(':checked')) {		//파일 배포 사용시
			$.each($("input[name=fileDistributionType]"), function(index, item){
				$('#'+item.id).prettyCheckable('enable');
			});
		}else if(true === $("#distributionTypeviewPrint").is(':checked')) {
			$.each($("input[name=fileDistributionType]"), function(index, item){
				$('#'+item.id).prettyCheckable('disable');
			});
		}
	});
	
	setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
	$(window).off("resize.commonRequestGrid").on("resize.commonRequestGrid", function() {
		ensureCommonRequestGridHorizontalScroll();
	});
});

function ensureCommonRequestGridHorizontalScroll() {
	var $grid = $("#" + popupGridId);
	var $gview = $("#gview_" + popupGridId);
	var $bdiv = $gview.find(".ui-jqgrid-bdiv");
	var $hdiv = $gview.find(".ui-jqgrid-hdiv");
	var $hbox = $hdiv.find(".ui-jqgrid-hbox");
	var $htable = $hdiv.find(".ui-jqgrid-htable");
	var $btable = $bdiv.find(".ui-jqgrid-btable");
	var $emptyScrollSpacer = $bdiv.children(".emptyHorizontalScrollSpacer");
	var $headerCols = $htable.find("colgroup col");
	var $bodyCols = $btable.find("colgroup col");
	var $headerThs = $htable.find("thead tr.ui-jqgrid-labels th");
	var $firstRowTds = $btable.find("tr.jqgfirstrow td");
	var colModel = $grid.jqGrid("getGridParam", "colModel") || [];
	var sourceColumns = Array.isArray(window.gridInfo) && window.gridInfo.length ? window.gridInfo : colModel;
	var baseWidth = Math.floor($bdiv.width() || $gview.width() || 0);
	var recordCount = parseInt($grid.jqGrid("getGridParam", "reccount"), 10) || 0;
	var scrollWidth = 0;
	var forcedWidth = 0;
	var columnWidthTotal = 0;
	var headerCellWidthTotal = 0;
	var syncedColumnWidthTotal = 0;
	var endGapBuffer = 15;

	if ($grid.length === 0 || $gview.length === 0 || baseWidth <= 0) {
		return;
	}

	columnWidthTotal = sourceColumns.reduce(function(total, column) {
		if (column && column.hidden !== true) {
			return total + (parseInt(column.width, 10) || 0);
		}
		return total;
	}, 0);
	if ($grid.jqGrid("getGridParam", "multiselect") === true) {
		columnWidthTotal += 36;
	}
	columnWidthTotal += 24;
	headerCellWidthTotal = $htable.find("th:visible").toArray().reduce(function(total, th) {
		return total + Math.ceil($(th).outerWidth() || 0);
	}, 0);
	if ($headerCols.length && $bodyCols.length) {
		$headerCols.each(function(index) {
			var $headerTh = $headerThs.eq(index);
			var $headerDiv = $headerTh.find("div:not(.clearfix)").first();
			var headerTextWidth = Math.ceil($headerDiv[0] ? $headerDiv[0].scrollWidth || 0 : 0);
			var width = Math.max(
				Math.ceil(parseFloat(this.style.width) || 0),
				Math.ceil($(this).outerWidth() || 0),
				Math.ceil($headerTh.outerWidth() || 0),
				headerTextWidth + 28
			);
			if (width > 0) {
				syncedColumnWidthTotal += width;
				this.style.setProperty("width", width + "px", "important");
				this.style.setProperty("min-width", width + "px", "important");
				if ($headerTh.length) {
					$headerTh[0].style.setProperty("width", width + "px", "important");
					$headerTh[0].style.setProperty("min-width", width + "px", "important");
				}
				if ($headerDiv.length) {
					$headerDiv[0].style.setProperty("min-width", Math.max(headerTextWidth, width - 28) + "px", "important");
				}
				if ($bodyCols[index]) {
					$bodyCols[index].style.setProperty("width", width + "px", "important");
					$bodyCols[index].style.setProperty("min-width", width + "px", "important");
				}
				if ($firstRowTds[index]) {
					$firstRowTds[index].style.setProperty("width", width + "px", "important");
					$firstRowTds[index].style.setProperty("min-width", width + "px", "important");
				}
			}
		});
	}

	scrollWidth = Math.max(
		Math.floor($bdiv[0].scrollWidth || 0),
		Math.floor($htable[0] ? $htable[0].scrollWidth || 0 : 0),
		Math.floor($btable[0] ? $btable[0].scrollWidth || 0 : 0),
		Math.floor($htable.outerWidth() || 0),
		Math.floor($btable.outerWidth() || 0),
		syncedColumnWidthTotal,
		headerCellWidthTotal,
		columnWidthTotal,
		baseWidth
	);
	forcedWidth = Math.max(baseWidth, scrollWidth);
	if (recordCount === 0) {
		forcedWidth = Math.max(forcedWidth, columnWidthTotal, headerCellWidthTotal, syncedColumnWidthTotal);
		if (forcedWidth <= baseWidth) {
			forcedWidth = baseWidth + 120;
		}
	}

	if ($hbox.length) {
		$hbox[0].style.setProperty("width", forcedWidth + "px", "important");
		$hbox[0].style.setProperty("min-width", forcedWidth + "px", "important");
		$hbox[0].style.setProperty("padding-right", "0px", "important");
	}
	if ($htable.length) {
		$htable[0].style.setProperty("width", forcedWidth + "px", "important");
		$htable[0].style.setProperty("min-width", forcedWidth + "px", "important");
	}
	if ($btable.length) {
		$btable[0].style.setProperty("width", forcedWidth + "px", "important");
		$btable[0].style.setProperty("min-width", forcedWidth + "px", "important");
	}
	if ($bdiv.length) {
		$bdiv[0].style.setProperty("overflow-x", "scroll", "important");
		$bdiv[0].style.setProperty("overflow-y", "auto", "important");
	}
	if ($hdiv.length) {
		$hdiv[0].style.setProperty("overflow-x", "hidden", "important");
	}
	if ($gview.length) {
		$gview[0].style.setProperty("width", baseWidth + "px", "important");
	}

	$bdiv.off("scroll.commonRequestSync").on("scroll.commonRequestSync", function() {
		var maxScrollLeft = Math.max(this.scrollWidth - this.clientWidth - endGapBuffer, 0);
		if (this.scrollLeft > maxScrollLeft) {
			this.scrollLeft = maxScrollLeft;
		}
		$hdiv.scrollLeft(this.scrollLeft);
	});
	if (recordCount === 0 && $bdiv.length) {
		if ($emptyScrollSpacer.length === 0) {
			$emptyScrollSpacer = $('<div class="emptyHorizontalScrollSpacer" aria-hidden="true"></div>').appendTo($bdiv);
		}
		$emptyScrollSpacer.css({
			width: forcedWidth + "px",
			height: "1px",
			opacity: "0",
			pointerEvents: "none"
		});
	} else {
		$emptyScrollSpacer.remove();
	}
	$gview.find(".ui-jqgrid-resize").off("mouseup.commonRequestSync").on("mouseup.commonRequestSync", function() {
		setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
	});
}

function loadComplete() {
	setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
}

function gridComplete() {
	setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
}

/**
 * 목록 추가(도면,문서,SW 번호 입력 후 점검하기 위해서
 * inspectionYn은 검증여부로 처음에는 N으로 추가함
 * @returns
 */
function addRow(){
	if(!isValidBusinessArea()){
		return;
	}
	var id = $('#'+popupGridId).jqGrid('getGridParam', 'reccount');
	$('#'+popupGridId).jqGrid('addRowData', id+1, {inspectionYn: 'N', duplicateYn: 'N'}, 'first');
	setCheck(id+1);
	setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
}

/**
 * 점검 후 성공했을 시 처리 함수
 * 모든 삽입과정이 끝나면 목록에 count를 입력
 * @param response
 * @returns
 */
function successfunc_for_similar_inspection(response, userInputs) {
	console.log(response);  // 서버로부터 받은 데이터를 확인

	// 사용자가 입력한 값이 있다면 response에서 동일한 항목 제거
	if (userInputs && userInputs.length > 0) {
		console.log(userInputs); // 사용자 입력 값 확인
		console.log("userInputs && userInputs.length > 0"); // 사용자 입력 값 확인
		console.log(response);
		response = response.filter(function(row) {
			return !userInputs.includes(row.objectNo);  // 사용자가 입력한 값과 일치하는 항목 제거
		});
	}

	// 그리드 업데이트
	console.log("before 조건문")
	console.log(response);
	if (response.length === 0) {
		console.log("response.length === 0");
		$("#"+popupGridId).jqGrid('clearGridData');
		var data = {
			inspectionYn: 'N',
			[popupInfo.msgField]: g_msg('msg.dataNotFound')
		};
		$('#'+popupGridId).jqGrid('addRowData', 1, data);
		setCheck(1);
		setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
	} else {
		console.log("response.length !== 0");
		$("#"+popupGridId).jqGrid('clearGridData');
		$("#"+popupGridId).jqGrid('setGridParam', {
			datatype: 'local',
			data: response
		}).trigger('reloadGrid');
		checkAll();
		setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
	}
}


function successfunc(response){
	var data = {};
	data.inspectionYn = 'N';
	data[popupInfo.msgField] = g_msg('msg.dataNotFound');
	console.log(response);
	var ids = $("#"+popupGridId).jqGrid('getDataIDs');
	for(i=0; i<ids.length; i++){
		var currData = $("#"+popupGridId).jqGrid('getRowData', ids[i]);
		console.log("currData >>  :" + currData );
		if(currData.inspectionYn === 'Y'){
			response.push(currData);
		}
	}
	if(response.length === 0){
		$("#"+popupGridId).jqGrid('clearGridData');
		// $('#'+popupGridId).jqGrid('delRowData', row);
		$('#'+popupGridId).jqGrid('addRowData', 1, data);
		setCheck(1);
		setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
	}else{
		$("#"+popupGridId).jqGrid('clearGridData');
		$("#"+popupGridId).jqGrid('setGridParam', {
			datatype:'local',
			data: response,
			// rowNum: 200
		})
		.trigger('reloadGrid');
		checkAll();
		setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
		// $('#'+popupGridId).jqGrid('delRowData', row);
		// for(var i=0; i<response.length; i++){
		// 	var rowId = response[i].objectNo + i;
		// 	$('#'+popupGridId).jqGrid('addRowData', rowId, response[i], 'first');
		// 	setCheck(rowId);
		// }
	}
	return true;
}

function exportExcel() {
//	$("#gridRequestPopup").jqGrid("exportToExcel",{
//		includeLabels : true,
//		includeGroupHeader : true,
//		includeFooter: true,
//		fileName : "jqGridExport.xlsx",
//		maxlength : 40 // maxlength for visible string data
//	})

	var rowDatas = $("#gridRequestPopup").jqGrid('getRowData');

	var param = {
			//colModels: gridInfo
			//,list: rowDatas
			gridId: popupGridId
	}

	$.each(gridInfo, function(i) {
		var column = this;

		$.each(column, function(key, value) {
			param['colModels[' + i + '].' + key] = value;
		});
	});

	$.each(rowDatas, function(i) {
		var row = this;
		$.each(row, function(key, value) {
			param['list[' + i + '].' + key] = value;
		});
	});

	confirmMessage(g_msg("msg.confirmDownload"), function(){
		$(this).dialog("close");

		$.ajax({
	        url: CONTEXT_PATH + "/common/createExcel/createExcelFromLocalGrid"
	       ,type  : "post"
	       ,cache : false
	       ,async : true
	       ,data  : param
		   ,beforeSend:function(){
//			   $("#progress").show();
			   //objLayout.progressOn();
		   }
		   , error:function(e){
				alertMessage(g_msg("msg.error") + '[' + e + ']');
		   }
	   }).done(function(data){
		   if(data.result == 'fail'){
				alertMessage(data.reason);
			}else{
				downloadExcel(data.url);
//				$("#progress").hide();
			}
	   });
	});

}


/**
 * 점검 버튼클릭에 대한 함수
 * 선택한 도면/문서/SW 에 대한 정보를 서버에서 얻어오는 과정
 * 도면/문서/SW번호 외에 추가적으로 사업장 코드도 함께 파라미터로 사용한다.
 * 성공시에는 successfunc함수를 호출
 * @returns
 */
function similar_inspection() {
	console.log("similar_inspection");

	// 선택된 행 가져오기
	var selrow = $('#'+popupGridId).jqGrid('getGridParam', 'selarrrow');
	var checkrow = [];
	var userInputs = [];  // 사용자가 입력한 값을 저장할 배열

	$.each(selrow, function(index, item) {
		var rowData = $('#'+popupGridId).jqGrid('getRowData', item);

		// inspectionYn이 'N'인 항목만 점검
		if (rowData.inspectionYn === 'N') {
			// 입력된 도면 번호(objectNo)가 비어 있는지 확인
			if (!rowData.objectNo || rowData.objectNo.trim() === "") {
				alertMessage(g_msg('msg.enterValidDrawingNo'));  // "유효한 도면 번호를 입력하세요."
				return;  // 빈 값일 경우 함수 실행 중지
			}
			checkrow.push(rowData);
			userInputs.push(rowData.objectNo.trim());  // 입력한 도면 번호를 배열에 추가
			console.log("데이터 추가");
		}
	});

	// 점검할 항목이 없는 경우 처리
	if (checkrow.length == 0) {
		alertMessage(g_msg('msg.noItemForInspection'));
		return;
	}

	// 파라미터 설정 및 서버 요청
	var param = {};
	param.list = checkrow;
	param.businessAreaCd = $("#businessAreaCd").val();
	if (dataType === 'PRODUCTION') {
		param.objectType = 'PRODUCT_' + $("#objectType").val();
	} else {
		param.objectType = dataType;
	}

	// AJAX 요청 시 success callback에 사용자가 입력한 값을 전달
	callAjax(param, '/outside/commonRequest/selectSimilarInspectionInfo', function(response) {
		successfunc_for_similar_inspection(response, userInputs);
	});
}



function inspection(){
	var selrow = $('#'+popupGridId).jqGrid('getGridParam', 'selarrrow');
	var checkrow = [];
	$.each(selrow, function(index, item){
		if($('#'+popupGridId).jqGrid('getRowData', item).inspectionYn === 'N'){
			checkrow.push($('#'+popupGridId).jqGrid('getRowData', item));
		}
	});
	if(checkrow.length == 0){
		alertMessage(g_msg('msg.noItemForInspection'));
		return;
	}
	var param = {};
	param.list = checkrow;
	param.businessAreaCd = $("#businessAreaCd").val();
	if(dataType === 'PRODUCTION'){
		param.objectType = 'PRODUCT_' + $("#objectType").val();
	}else{
		param.objectType = dataType;
	}
	callAjax(param, '/outside/commonRequest/selectInspectionInfo', successfunc);
	// var extraparam = {"businessAreaCd": $("#businessAreaCd").val()};
	// if(dataType === 'PRODUCTION'){
	// 	extraparam.objectType = 'PRODUCT_' + $("#objectType").val();
	// }else{
	// 	extraparam.objectType = dataType;
	// }
	// var saveparameters = {
	// 	"successfunc" : successfunc,
	// 	"url" : "/outside/commonRequest/selectInspectionInfo",
	// 	"extraparam" : extraparam
	// }
	// $.each(checkrow, function(index, item){
	// 	row = item;
	// 	var data = $('#'+popupGridId).jqGrid('getRowData', item);
	// 	if(data.duplicateYn === 'N'){
	// 		$('#'+popupGridId).jqGrid('editRow', item);
	// 		$('#'+popupGridId).jqGrid('saveRow', item, saveparameters);
	// 	}
	// });
}



/**
 * 배포요청
 * 선택된 목록 데이터중 점검이 완료된 데이터(inspectionYn = Y)만 배포요청
 * @returns
 */
function deployRequest(){
	if(!isValidation()){
		return;
	}
	var selrow = $('#'+popupGridId).jqGrid('getGridParam', 'selarrrow');
	var data = [];
	var protectAuth = getOutsideProtectYn();
	for(i=0; i<selrow.length; i++){
		if($('#'+popupGridId).jqGrid('getCell', selrow[i], 'inspectionYn') == 'Y'){
			var tempData = $('#'+popupGridId).jqGrid('getRowData', selrow[i]);

			if("Y" === tempData.protectYn && !protectAuth) {
				alertMessage(g_msg('msg.noAuthProtectRequest'));	/* 방산기술 권한이 없습니다.*/
				return;
			}

			tempData.useStartYmd = getDate(new Date());
			var endDate = new Date();
			var purpose = $("#requestPurpose").val();
			var distributeTypeCd = $("#distributeTypeCd").val();
			var addMonth = 0;
			switch (purpose){
				case 'PRODUCT' :
				case 'MANUAL' :
					if('hdCADApprovedDrawing' == distributeTypeCd) {
						addMonth = 60;
					}else {
						addMonth = 6;
					}
					break;
				case 'CHECK' :
				case 'REFER' :
					// addMonth = 1;
					// break;
				case 'ESTIMATE' :
					addMonth = 3;
					break;
				default :
					break;
			}
			endDate.setMonth(endDate.getMonth() + addMonth);
			tempData.useEndYmd = getDate(endDate);
			data.push(tempData);
		}
	}
	if(data.length == 0){
		alertMessage(g_msg('msg.noRequestItem'));
		return;
	}
	confirmMessage(g_msg('confirm.deployRequest'), function(){
		$("#confirmMessage").dialog("close");
		runDeploy(data);
	});
}

function getDate(date){
	var year = date.getFullYear();
	var month = (date.getMonth() + 1) + '';
	var day = date.getDate();
	if(month.length < 2) {
		month = '0' + month;
	}
	if(day < 10) {
		day = '0' + day;
	}
	return year + month + day;
}

function runDeploy(data){
	var param = $("#requestInfo").serializeObject();
	param.list = data;
	param.deployUserCd = $("#deployUserCd").val();
	
	if(true === $("#distributionTypeviewPrint").is(':checked')){	//veiw&print check 여부
		param.printYn = 'Y';
	}else{
		param.printYn = 'N';
	}

	if(true === $("#distributionTypefileDistribution").is(':checked')){	//파일배포여부 check 여부
		param.fileDistributionType = $("input[name=fileDistributionType]:checked").attr('value');
	}
	param.distributionType = $("input[name=distributionType]:checked").attr('value');

	callAjax(param, popupInfo.urlInfo + "distributionRequest", requestCallback, 'json');
}

/**
 * 추가버튼 클릭 시 먼저 점검되는 사업장 선택여부
 * 도면/문서/SW점검 시 사업장 CD가 필수 파라미터이기때문에 먼저 사업장선택여부를 확인한다.
 * 사업장을 재 선택할 경우 목록은 전부 삭제된다.
 * @returns
 */
function isValidBusinessArea(){
	if($.trim($("#businessAreaCd").val()) === ""){
		alertMessage(g_msg('msg.selectBusinessArea'));
		return false;
	}
	return true;
}

/**
 * row를 편집 시 호출되는 event함수 (그리드 클릭 시 input창으로 변경되면서 호출된다 )
 * input에서 focus가 벗어나면 자동으로 saveCell이 실행된다.
 * @param rowid
 * @param cellname
 * @param value
 * @param iRow
 * @param iCol
 * @returns
 */
function afterEditCellFunc(rowid, cellname, value, iRow, iCol){
	$('#'+iRow+'_'+cellname).bind('blur', function(){
		$('#'+popupGridId).saveCell(iRow, iCol);
	});
}

/**
 * addrowdata하고 input영역에서 벗어날 시 savecell이 실행되는데 그 전에 호출되는 함수
 * 여기서 중복체크가 진행된다
 * @param rowid
 * @param cellname
 * @param value
 * @param iRow
 * @param iCol
 * @returns
 */
function beforeSaveCellFunc(rowid, cellname, value, iRow, iCol){
	var ids = $('#'+popupGridId).jqGrid('getDataIDs');
	var isUnique = false;
	for(i=0; i < ids.length; i++){
		var orgdata = $('#'+popupGridId).jqGrid('getRowData', ids[i]);
		if(orgdata.objectNo === value){
			if(orgdata.inspectionYn === 'Y'){
				$('#'+popupGridId).jqGrid('restoreCell', iRow, iCol);
			}else{
				$('#'+popupGridId).jqGrid('delRowData', rowid);
			}
			alertMessage(g_msg('msg.duplicateData'));
			return;
		}else{

			isUnique = true;
		}
	}
	if(isUnique){
		if($('#'+popupGridId).jqGrid('getCell', rowid, 'inspectionYn') === 'Y'){
			$('#'+popupGridId).jqGrid('setRowData', rowid, {duplicateYn: 'N', inspectionYn: 'N'}, 'incomplete');
		}else{
			$('#'+popupGridId).jqGrid('setRowData', rowid, {duplicateYn: 'N', inspectionYn: 'N'});
		}
	}
}

/**
 * 배포요청 시 validation체크
 * 용도, 업체명/담당자, Email, 사업장, 구매담당자가 선택되었는지 체크한다
 * @returns
 */
function isValidation(){
	if($.trim($("#requestPurpose").val()) === ""){
		isValidDataEmpty("requestPurpose", "form.purpose");
		return false;
	}
	if($.trim($("#businessAreaCd").val()) === ""){
		isValidDataEmpty("businessAreaCd", "form.businessArea");
		return false;
	}
	if($.trim($("#acceptanceUserCd").val()) === ""){
		isValidDataEmpty("acceptanceUserCd", "form.approvalUser");
		return false;
	}
	if($.trim($("#deployUserCd").val()) === ""){
		isValidDataEmpty("deployUserCd", "form.requestUser");
		return false;
	}
//	if($.trim($("#deployUserEmail").val()) === ""){
//		isValidDataEmpty("deployUserEmail", "form.email");
//		return false;
//	}
	if($.trim($("#requestDesc").val()) === ""){
		isValidDataEmpty("requestDesc", "form.requestReason");
		return false;
	}
	return true;
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.requestComplete'), function(){
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.requestFailure"));
	}
}

/**
 * 사업장 변경 시 구매담당자 리스트를 새로 불러오기 위한 이벤트
 * 구매담장자리스트를 갱신하기 위해 서버를 호출하고, 목록 데이터를 전부 삭제한다.
 * @returns
 */
$("#businessAreaCd").on("change", function(){
	loadAcceptanceUser();
	$("#"+popupGridId).jqGrid('clearGridData');
});


/**
 * 자료유형 변경 시 목록 데이터를 전부 삭제한다.
 * @returns
 */
$("#objectType").on("change", function(){
	$("#"+popupGridId).jqGrid('clearGridData');
});

$("#deployUserCd").on("change", function(){
	var param = {
			userCd: $("#deployUserCd").val()
	};
	if($('#deployUserCd').val() != ''){
		callAjax(param, "/outside/commonRequest/selectUserInfo", setUserEmail, 'json');
	}else{
		$("#deployUserEmail").val('');
	}
});

function setUserEmail(response){
	$("#deployUserEmail").val(response.email);
}



/**
 * 사업장 변경 시 구매담당자리스트를 불러오기 위해 호출되는 함수
 * @returns
 */
function loadAcceptanceUser(){
	var businessAreaVal = $("#businessAreaCd").val();
	var param = {
			businessAreaCd : businessAreaVal
			};
	callAjax(param, "/outside/commonRequest/selectAcceptanceUser", setAcceptanceUser, 'json');
}

/**
 * 구매담당자 리스트 호출 후 결과값을 구매담당자 selectBox에 세팅하는 함수
 * @param response
 * @returns
 */
function setAcceptanceUser(response){
	var newOption;
	$("#acceptanceUserCd").empty().trigger('change');
	$.each(response, function(index, data){
		newOption = new Option(data.comboLabel, data.comboVal, false, data.selectedValue === data.comboVal);
		$("#acceptanceUserCd").select2({minimumResultsForSearch: 1, dropdownParent: $('#popupDialoga')});
		$("#acceptanceUserCd").append(newOption).trigger('change');
	});
	$($("#acceptanceUserCd").data('select2').$container).addClass('searchSelect');

}

/**
 * 삭제버튼 클릭 시 호출
 * 선택된 목록을 삭제한다.
 * @returns
 */
function deleteRow(){
	var selrow = $('#'+popupGridId).jqGrid('getGridParam', 'selarrrow');
	if(selrow.length == 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}
	for(i=selrow.length - 1; i>=0; i--){
		$('#'+popupGridId).jqGrid('delRowData', selrow[i]);
	}
	setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
}

/**
 * 전체 삭제
 * 전체삭제 버튼은 사용하지 않을거지만 사업장 변경 시 목록을 전부 삭제하기 위해 생성한 함수
 * @returns
 */
function deleteAllRow(){
	$('#'+popupGridId).jqGrid('clearGridData');
	setTimeout(ensureCommonRequestGridHorizontalScroll, 0);
}

/**
 * 붙여넣기
 * IE에서만 동작(추후 chrome이나 기타 browser에서 동작하는 방법 찾아야함)
 * 클립보드에서 데이터를 읽어온 후(대부분 엑셀)
 * 해당 데이터를 도면/문서/SW번호에 자동으로 입력
 * 기존 입력되어있던 목록은 삭제 되고 클립보드에 있던 목록만 신규로 입력된다.
 * 중복제거과정도 추가되어있음
 * @returns
 */
function paste(){
	if($.trim($("#businessAreaCd").val()) === ""){
		alertMessage(g_msg('msg.selectBusinessArea'));
		return false;
	}
	if(getIsInternetExplorer()){
		pasteForIE();
	}else{
		pasteForChrome();
	}
}



function pasteText(text){
	var arrClipboard = text.split("\r\n");
	var uniqueArr = [];
	$.each(arrClipboard, function(index, item){
		if($.inArray(item.trim(), uniqueArr) === -1) {
			uniqueArr.push(item);
		}
	});
	if( uniqueArr.length > 201 ) {
		alertMessage(g_msg('msg.pasteWarning')); //붙여넣기 하신 갯수는 200 건을 초과할 수 없습니다. 200 건 이하로 붙여넣기 해주세요.
		return;
	}

	deleteAllRow();
	var gridData = [];
	var pasteRtn = false;
	$.each(uniqueArr, function(index, value){
		arrByColumn = uniqueArr[index].split("\t");
		if(arrByColumn.length != 1){
			return;
		}
		if(arrByColumn == '')return;
		var data = {};
		data.objectNo = arrByColumn[0];
		data.inspectionYn = 'N';
		data.duplicateYn = 'N';
		gridData.push(data);
		pasteRtn = true;
	});
	if(!pasteRtn)alertMessage('잘못된 데이터 형식 입니다.');
	$('#'+popupGridId).jqGrid('setGridParam',
			{
				datatype:'local',
				data: gridData,
				rowNum: 200,
			}
	).trigger('reloadGrid');
	checkAll();
}

/**
 * 붙여넣기 실행 시 클립보드에서 읽어 온 데이터를 목록에 addRow하는 과정
 * @param columnName
 * @param value
 * @returns
 */
function addRowWithData(columnName, value){
	var data = {};
	data[columnName] = value;
	data.inspectionYn = 'N';
	data.duplicateYn = 'N';
	var id = $('#'+popupGridId).jqGrid('getGridParam', 'reccount');
	$('#'+popupGridId).jqGrid('addRowData', id+1, data, 'last');
	setCheck(id+1);
}

function setCheck(id){
	$('#'+popupGridId).jqGrid('setSelection', id);
	$("#jqg_" + popupGridId + "_" + id).each(function() {
		$(this).prettyCheckable("check");
	});
}

function checkAll(){
	var ids = $('#'+popupGridId).jqGrid('getDataIDs');
	$.each(ids, function(index, data){
		setCheck(data);
	})
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	$('#listCount').text($('#'+popupGridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth();
	ensureCommonRequestGridHorizontalScroll();
}

	// function ondblClickRowFunc(rowId){
	// 	var param = getDataParam(rowId);
	// 	if(!param){
	// 		return;
	// 	}
	// 	openDialogPopup(
	// 		popupInfo.urlInfo + "itemDetailPopup",
	// 		param,
	// 		"detailPopupDialog",
	// 		'l',
	// 		610,
	// 		true,
	// 		'popup-common popup-request-detail'
	// 	);
	// }
	// 
	// function getDataParam(rowId){
	// 	var param = {};
	// 	var gridItem = $('#'+popupGridId).jqGrid('getRowData', rowId);
	// 	if(!gridItem || gridItem.inspectionYn !== 'Y'){
	// 		return;
	// 	}
	// 	if(popupInfo.dataType == 'DRAWING'){
	// 		param['drawingNo'] = gridItem.drawingNo;
	// 		param['revNo'] = gridItem.revNo;
	// 		param['drawingRev'] = gridItem.drawingRev;
	// 		param['rowId'] = rowId;
	// 		return param;
	// 	}
	// 	if(popupInfo.dataType == 'PRODUCTION'){
	// 		param['objectNo'] = gridItem.objectNo;
	// 		param['revNo'] = gridItem.revNo;
	// 		param['rowId'] = rowId;
	// 		return param;
	// 	}
	// 	return;
	// }
