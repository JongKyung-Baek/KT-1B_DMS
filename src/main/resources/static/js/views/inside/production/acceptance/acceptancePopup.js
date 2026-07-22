function setPopupGridParam(){
	popupGridParam = {
			gridId : 'gridAcceptancePopup',
			formId : 'formAcceptancePopup',
			url : '/inside/production/acceptance/selectPopupList',
			pagerId : '',
			size : 9999,
			page : 1,
			multiSelect : false,
			numbering : false
	}
	return popupGridParam;
}

//
//function popupSettingGrid(gridData, param, gridParamId){
//	var aJsonArray = new Array();
//	$.each($("#"+gridParam.gridId).getGridParam('selarrrow'), function(index, item){
//		var data = $("#"+gridParam.gridId).jqGrid('getRowData', item);
//		var aJson = new Object();
//		aJson.requestNo = data.requestNo;
//		aJson.objectId = data.objectId;
//		aJsonArray.push(aJson);
//	});
//	var sJson = JSON.stringify({list:aJsonArray});
//
//	COL_MODEL = gridData.replace('"#NO_QUOT#', "");
//	COL_MODEL = COL_MODEL.replace('#NO_QUOT#"', "");
//	COL_MODEL = eval(COL_MODEL);
//	$.each(COL_MODEL, function(index, value){
//		if(""==value.formatter){
//			delete COL_MODEL[index].formatter;
//		}
//	});
//	//그리드 초기화
//	try {
//		if("grid" === $("#" + param.gridId).attr("role")) {
//			$("#" + param.gridId).jqGrid("clearGridData");
//			$.jgrid.gridUnload('#' + param.gridId);
//		}
//		else {
//			console.log("else");
//		}
//	}
//	catch(e) {
//		console.log(e);
//	}
//
//	try {
//		$.jgrid.gridUnload('#' + param.gridId);
//	} catch(e) { console.log(e); }
//
//	var pagerId = param.gridId + 'Pager';
//	var gridData = {
//		url: CONTEXT_PATH+param.url,
//		mtype: "POST",
//		datatype: "json",
//		postData: {	//form의 Data를 만들어서..
//      	size: param.size,
//      	page: param.page,
//      	list : JSON.stringify(aJsonArray)
//		},
//		jsonReader:{
//			total: "totalPage",
//            root: "contents",
//            page: "page",
//            records: "records",
//		},
//		colModel: COL_MODEL,
//		cellEdit: param.cellEdit,
//		cellsubmit: param.cellsubmit,
//		viewrecords: true,
//		gridview: true,
//		height: null,
//		width:null,
//		autoheight:true,
//		shrinkToFit : false, //grid width test
//		rowList: [10, 30, 100],
//		footerrow: false,
//		userDataOnFooter: false,
//		multiselectWidth : 35,
//		rowNum : param.size,
//		rownumbers:param.numbering,
//		multiselect:param.multiSelect,
//		gridComplete: function(){
//			$('.cbox').each(function() {
//				if("none" !== $(this).css("display")) {
//					$(this).prettyCheckable();
//					$(this).parent().addClass('noLabel');
//		        	$(this).change(function() {clickGridCheck($(this)) });
//				}
//		    });
//			if('function' === typeof gridComplete) {
//				gridComplete();
//			}
//
//			/* ----- body jqgrid height ----- */ // Set height value when loading page
//			var tg3 = $('.bodyWrap .ui-jqgrid .ui-jqgrid-view'); // ui-jqgrid-view height
//			tg3.css({'height' : tg3.parent().parent('.gridContainer').height() - tg3.siblings('.ui-jqgrid-pager').outerHeight(true) + 'px'});
//			var tg4 = $('.ui-jqgrid .ui-jqgrid-bdiv'); // ui-jqgrid-bdiv height
//			tg4.css({'height' : tg4.parent('.ui-jqgrid-view').height() - tg4.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});
//
//			/* ----- dialog jqgrid height ----- */
//			$('.dialogContent .gridContainer').each(function() {
//				var tgbdiv = $('#gbox_' + param.gridId + ' .ui-jqgrid-bdiv');
//				tgbdiv.css({'height' : tgbdiv.parent('.ui-jqgrid-view').height() - tgbdiv.parent('.ui-jqgrid-view').children('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});
//			});
//		},
//		loadComplete: function(data){
//			console.log(data);
//			var bDiv = this.grid.bDiv;
//			setTimeout(function () {
//				bDiv.scrollLeft = 0;
//				bDiv.scrollTop = 0;
//			}, 0);
//
//			initGridNoData(param.gridId, data);
//			if( !("" === pagerId) ){
//				initPage(param, pagerId, true, "TOT", gridParamId);
//			}
//			changeSortableIcon(data, param);
//
//			$("#jqgh_" + param.gridId + "_cb > .cbox").each(function() {
//				if("none" !== $(this).css("display")) {
//					$(this).prettyCheckable();
//					$(this).parent().addClass('noLabel');
//		        	$(this).change(function() {clickGridCheck($(this)) });
//				}
//		    });
//
//			if('function' === typeof loadComplete) {
//				loadComplete(data);
//			}
//        },
//		onSelectRow: function(rowid, status, e) {
//			if('check' == param.selectRowAction){
//				onSelectRow(rowid, status, e);
//			}
//			else{
//				onUnselectRow(rowid, param.gridId, e);
//			}
//		},
//		onSortCol: function (index, columnIndex, sortOrder) {
//			var param = { postData :
//				{
//			      	size: $(this).getGridParam('rowNum'),
//			      	page: 1,
//			      	sortColumn : index,
//			      	order : sortOrder
//				}
//			};
//			$(this).jqGrid('setGridParam', param).trigger("reloadGrid");
//		},
//		afterEditCell: function(rowid, cellname, value, iRow, iCol){
//			afterEditCellFunc(rowid, cellname, value, iRow, iCol);
//		},
//		beforeSaveCell: function(rowid, cellname, value, iRow, iCol){
//			beforeSaveCellFunc(rowid, cellname, value, iRow, iCol);
//		},
//	};
//
//	if(undefined === param.usePager || '' === param.usePager || true === param.usePager) {
//		gridData["pager"] = pagerId;
//	}
//	$("#"+param.gridId).jqGrid(gridData);
//}

/**
 * 생산기술 자료 자료 접수 시 필수 입력 내용 체크
 * @returns
 */
function validate(){
	if(!isValidObjEmpty($("#deployDesc"), 'form.eduReason')) return false;
	if(!isValidObjEmpty($("#deployDt"), 'form.eduDate')) return false;
	if(!isValidObjEmpty($("#deployTarget"), 'form.eduTarget')) return false;
	if(!isValidObjEmpty($("#deployResult"), 'form.eduResult')) return false;
	return true;
}


/**
 * 생산기술 자료 접수
 * @returns
 */
function acceptance(){

	if(!validate()) {
		return false;
	}

	var param = $("#formAcceptancePopup").serializeObject();

	var aJsonArray = new Array();
	$.each($("#"+popupGridParam.gridId).getRowData(), function(index, item){
		var aJson = new Object();
		aJson.requestNo = item.requestNo;
		aJson.objectId = item.objectId;
		aJson.objectNo = item.objectNo;
		aJson.objectType = item.objectType;
		aJson.deployUserCd = item.deployUserCd;
		aJsonArray.push(aJson);
	});
	param.list = aJsonArray;
	callAjax(param, '/inside/production/acceptance/saveAcceptance', acceptanceCallback);
}


/**
 * 접수 후 결과 메시지 출력
 * @param response
 * @returns
 */
function acceptanceCallback(response){
	if(response.success){
		infoMessage(g_msg('msg.acceptanceSuccess'), function(){	//접수가 완료되었습니다.
			searchList(gridParam);
			closePopup('popupDialog');
			$(this).dialog("close");
		});
	}else{
		alertMessage(g_msg("msg.acceptanceFailure"));				//접수가 실패했습니다.
	}
}

function dialogToolbarWidth(){
	var dtbWL = $('.dialogContent .dialogToolbar .left').outerWidth(true);
	$('.dialogContent .dialogToolbar .right').css({'width': 'calc(100% - ' + dtbWL + 'px)'});
}

function gridComplete(){
	$('#listCount').text($('#'+popupGridParam.gridId).jqGrid('getGridParam', 'reccount'));
	dialogToolbarWidth()
}
