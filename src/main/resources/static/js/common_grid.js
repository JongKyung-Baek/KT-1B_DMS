/**
 * jqGrid 공통 js 파일
 */

var COL_MODEL;
var testModel;
var jsonData;

var baseWidth = 1000;
var totalGridWidth = {};

function isInvoiceLayout(param) {
	return !!(param && param.layoutMode === 'invoice');
}

function shouldFillColumns(param) {
	return !!(param && param.fillColumns === true);
}

function getInvoiceContainerWidth(param) {
	return $("#" + param.gridId).parents('.gridContainer').width() || 0;
}

function getInvoiceRequiredWidth(param) {
	var $gridBox = $("#gbox_" + param.gridId);
	var headerWidth = $gridBox.find(".ui-jqgrid-htable").outerWidth() || 0;
	var bodyWidth = $gridBox.find(".ui-jqgrid-btable").outerWidth() || 0;
	var configuredWidth = totalGridWidth[param.gridId] || 0;
	return Math.max(configuredWidth, headerWidth, bodyWidth);
}

function shouldExpandInvoiceColumns(param) {
	if (!isInvoiceLayout(param) || !shouldFillColumns(param)) {
		return false;
	}

	return getInvoiceContainerWidth(param) >= getInvoiceRequiredWidth(param);
}

function getInvoiceGridWidth(param) {
	var containerWidth = getInvoiceContainerWidth(param);
	var requiredWidth = getInvoiceRequiredWidth(param) || containerWidth;
	return Math.max(containerWidth, requiredWidth);
}

function syncInvoiceHeaderSpacing(param) {
	if (!isInvoiceLayout(param)) {
		return;
	}

	var $grid = $("#" + param.gridId);
	var $view = $grid.closest(".ui-jqgrid-view");
	var $body = $view.children(".ui-jqgrid-bdiv");
	var $headerBox = $view.find(".ui-jqgrid-hbox");
	var scrollbarWidth = 0;

	if ($body.length > 0) {
		scrollbarWidth = $body[0].offsetWidth - $body[0].clientWidth;
	}

	$headerBox.css("padding-right", Math.max(scrollbarWidth, 0) + "px");
}

function gridHeightSizing(param){
	if (isInvoiceLayout(param)) {
		return;
	}

	if($('body').find('.dialogContent').length == 0){ // -- list grid height --
		var tg1 = $('.bodyWrap .contentArea');
		if(tg1.siblings('.tabArea').length == 1){
			tg1.css({'height': tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true) - tg1.siblings('.tabArea').outerHeight(true) + 'px'});
		}else{
			tg1.css({'height': tg1.parents('.container').height() - tg1.siblings('.nav').outerHeight(true) + 'px'});
		}
		var tg2 = $('.bodyWrap .gridArea');
		tg2.css({'height': tg2.parents('.contentArea').height() - tg2.siblings('.sbr').outerHeight(true) - tg2.siblings('.btnArea').outerHeight(true) + 'px'});

		var tg3 = $('.gridArea > .gridContainer .ui-jqgrid-view');
		var tg4 = $('.gridArea > .gridContainer .ui-jqgrid-bdiv');
		 if(tg3.siblings('.ui-jqgrid-pager').length == 1){ // list jqgrid view height = gridContainer - jqgrid pager
			 tg3.css({'height' : tg3.parents('.gridContainer').height() - tg3.siblings('.ui-jqgrid-pager').outerHeight(true) + 'px'});
		 }else{
			 tg3.css({'height' : tg3.parents('.gridContainer').height() + 'px'});
		 }
		tg4.css({'height' : tg4.parent('.ui-jqgrid-view').height() - tg4.siblings('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});  // list jqgrid bdiv height = jqgrid view - jqgrid hdiv
	}else if($('body').find('.dialogContent').length == 1){ // -- dialog grid height --
		$('.dialogContent .gridContainer').each(function() {
			var tgbdiv = $('#gbox_' + param.gridId + ' .ui-jqgrid-bdiv'); // ui-jqgrid-bdiv height
			tgbdiv.css({'height' : tgbdiv.parent('.ui-jqgrid-view').height() - tgbdiv.parent('.ui-jqgrid-view').children('.ui-jqgrid-hdiv').outerHeight(true) + 'px'});
		});
	}

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

function settingGrid(gridData, param, gridParamId){
	var rowList = [30, 50, 100];
	var i=0, existSize = false;

	COL_MODEL = gridData.split('"#NO_QUOT#').join("");
	COL_MODEL = COL_MODEL.split('#NO_QUOT#"').join("");
//	COL_MODEL = eval(COL_MODEL);
	COL_MODEL = Function('"use strict";return (' + COL_MODEL + ')')();


	for(i=0; i<rowList.length; i++) {
		if(parseInt(param.size, 10) == rowList[i]) {
			existSize = true;
		}
	}

	if(!existSize) {
		setCookie( "rowNum", rowList[0], 1000 );
		param.size = rowList[0];
	}

	var shrinkToFit = true;
	if (isInvoiceLayout(param)) {
		shrinkToFit = shouldExpandInvoiceColumns(param);
	}
	if(gridParamId != 'gridParam'){
		if(param.shrinkToFit == undefined){
			// Invoice layout grids should fill available width by default.
			shrinkToFit = isInvoiceLayout(param) ? true : false;
		}else{
			shrinkToFit = param.shrinkToFit;
		}
	}
	$.each(COL_MODEL, function(index, value){
		if(""==value.formatter){
			delete COL_MODEL[index].formatter;
		}
	});
	//그리드 초기화
	try {
		if("grid" === $("#" + param.gridId).attr("role")) {
			$("#" + param.gridId).jqGrid("clearGridData");
			$.jgrid.gridUnload('#' + param.gridId);
		}
		else {
			console.log("else");
		}
	}
	catch(e) {
		console.log(e);
	}

	try {
		$.jgrid.gridUnload('#' + param.gridId);
	} catch(e) { console.log(e); }

	var pagerId = param.gridId + 'Pager';
	totalGridWidth[param.gridId] = 0;
	$.each(COL_MODEL, function() {
		if (this.hidden === true) {
			return;
		}
		totalGridWidth[param.gridId] += this.width;
	});
	var	postData = {	//form의 Data를 만들어서..
      	size: param.size,
      	page: param.page
	}
	if(param.postData != undefined){
		postData = param.postData
	}


	var gridData = {
		url: CONTEXT_PATH+param.url+getSearchCondition(param),
		mtype: "POST",
		datatype: "json",
		postData: postData,
		jsonReader:{
			total: "totalPage",
            root: "contents",
            page: "page",
            records: "records",
		},
		colModel: COL_MODEL,
		cellEdit: param.cellEdit,
		cellsubmit: param.cellsubmit,
		viewrecords: true,
		gridview: true,
		height: null,
		width:null,
		autoheight:true,
		shrinkToFit : shrinkToFit, //grid width auto sizing
		rowList: rowList,
		footerrow: false,
		userDataOnFooter: false,
		multiselectWidth : 35,
		rowNum : param.size,
		rownumbers:param.numbering,
		loadui: 'disable',
		multiselect:param.multiSelect,
		gridComplete: function(){
			$('.cbox').each(function() {
				if("none" !== $(this).css("display")) {
					$(this).prettyCheckable();
					$(this).parent().addClass('noLabel');
		        	$(this).change(function() {clickGridCheck($(this)) });
				}
		    });
			if('function' === typeof gridComplete && gridParamId != 'gridParam') {
				gridComplete();
			}
		},
		loadComplete: function(data){
			var bDiv = this.grid.bDiv;
			setTimeout(function () {
				bDiv.scrollLeft = 0;
				bDiv.scrollTop = 0;
			}, 0);
			var $grid = $('#'+param.gridId);
			if((gridParamId != 'gridParam') && ($grid.getGridParam('records') == 0)){
				initGridNoData(param.gridId, data.records);
			}else{
				initGridNoData(param.gridId, data.length);
			}
			if( !("" === pagerId) ){
				initPage(param, pagerId, true, "TOT", gridParamId);
			}
			changeSortableIcon(data, param);

//			$('.cbox').each(function() {
//				if("none" !== $(this).css("display")) {
//					$(this).prettyCheckable();
//					$(this).parent().addClass('noLabel');
//		        	$(this).change(function() {clickGridCheck($(this)) });
//				}
//		    });

			$("#jqgh_" + param.gridId + "_cb > .cbox").each(function() {
//				$('.cbox').each(function() {
				if("none" !== $(this).css("display")) {
					$(this).prettyCheckable();
					$(this).parent().addClass('noLabel');
		        	$(this).change(function() {clickGridCheck($(this)) });
				}
		    });


			if('function' === typeof loadComplete) {
				loadComplete(data);
			}

			function autoWidthCustom(param){
				if (isInvoiceLayout(param)) {
					var invoiceWidth = getInvoiceGridWidth(param);
					var expandColumns = shouldExpandInvoiceColumns(param);
					$("#" + param.gridId).jqGrid('setGridWidth', invoiceWidth, expandColumns);
					syncInvoiceHeaderSpacing(param);
					return;
				}

				baseWidth = $("#" + param.gridId).parents('.gridContainer').width();
				if(baseWidth < totalGridWidth[param.gridId]) {
					if (param.shrinkToFit === true) {
						$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
					} else {
						$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: false, autowidth:false}).jqGrid('setGridWidth', baseWidth, false);
					}
				}else if(baseWidth >= totalGridWidth[param.gridId]) {
					if(gridParamId != 'gridParam' && param.shrinkToFit !== true){
						$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: false, autowidth:false}).jqGrid('setGridWidth', baseWidth, false);
					}else{
						$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
					}
				}
			}

			function resizeAutoWidthCustom(param){
				if (isInvoiceLayout(param)) {
					var invoiceWidth = getInvoiceGridWidth(param);
					var expandColumns = shouldExpandInvoiceColumns(param);
					$("#" + param.gridId).jqGrid('setGridWidth', invoiceWidth, expandColumns);
					syncInvoiceHeaderSpacing(param);
					return;
				}

				baseWidth = $("#" + param.gridId).parents('.gridContainer').width();
				if(baseWidth < totalGridWidth[param.gridId]) {
					$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: false, autowidth:false}).jqGrid('setGridWidth', baseWidth, false);
				}else if(baseWidth >= totalGridWidth[param.gridId]) {
					if(gridParamId != 'gridParam' && param.shrinkToFit !== true){
						$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: false, autowidth:false}).jqGrid('setGridWidth', baseWidth, false);
					}else{
						$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
					}
				}
			}


			autoWidthCustom(param);
			gridHeightSizing(param);

			$('.gnbToggle').click(function(e){
				e.preventDefault();
				gridHeightSizing(param);
				resizeAutoWidthCustom(param);
			});

			var resizeTimer;
			$(window).on('resize', function(e) {
				clearTimeout(resizeTimer);
				resizeTimer = setTimeout(function() {

					gridHeightSizing(param);
					resizeAutoWidthCustom(param);

				}, 500);
			});
        },
		onSelectRow: function(rowid, status, e) {
			if('check' == param.selectRowAction){
				onSelectRow(rowid, status, e);
			}
			else{
				onUnselectRow(rowid, param.gridId, e);
			}
		},
		onSortCol: function (index, columnIndex, sortOrder) {
			let sortColumn = $(this).jqGrid ('getGridParam', 'colModel')[columnIndex].sortColumnNm;
			if(sortColumn === '')sortColumn = index;
			// $(this).jqGrid('getGridParam', 'colModel')[columnIndex];
			var param = { postData :
				{
			      	size: $(this).getGridParam('rowNum'),
			      	page: 1,
			      	sortColumn : sortColumn,
			      	order : sortOrder
				}
			};
			$(this).jqGrid('setGridParam', param).trigger("reloadGrid");
		},
		afterEditCell: function(rowid, cellname, value, iRow, iCol){
			afterEditCellFunc(rowid, cellname, value, iRow, iCol);
		},
		beforeSaveCell: function(rowid, cellname, value, iRow, iCol){
			beforeSaveCellFunc(rowid, cellname, value, iRow, iCol);
		},
		ondblClickRow: function(rowId){
			if('function' === typeof ondblClickRowFunc) {
				ondblClickRowFunc(rowId);
			}
		},
		afterSaveCell: function(rowid, cellname, value, iRow, iCol){
			afterSaveCellFunc(rowid, cellname, value, iRow, iCol);
		}
	};
	if(undefined === param.usePager || '' === param.usePager || true === param.usePager) {
		gridData["pager"] = pagerId;
	}

	if (!isInvoiceLayout(param)) {
		baseWidth = $("#" + param.gridId).parents('.gridContainer').width();
		if(baseWidth < totalGridWidth[param.gridId]) {
			gridData['shrinkToFit'] = false;
			$("#" + param.gridId).trigger("reloadGrid");
		}else if(baseWidth >= totalGridWidth[param.gridId]) {
			if(gridParamId != 'gridParam'){
				gridData['shrinkToFit'] = false;
				$("#" + param.gridId).trigger("reloadGrid");
			}else{
				$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
			}
		}
	}
	$("#"+param.gridId).jqGrid(gridData);

	gridHeightSizing(param);
}

function settingGridWithData(gridData, param, gridParamId){
	COL_MODEL = gridData.split('"#NO_QUOT#').join("");
	COL_MODEL = COL_MODEL.split('#NO_QUOT#"').join("");
	COL_MODEL = Function('"use strict";return (' + COL_MODEL + ')')();

	if(param.data == undefined)param.data = [];
	var shrinkToFit = true;
//	if(gridParamId != 'gridParam'){
//		shrinkToFit = false;
//	}
	$.each(COL_MODEL, function(index, value){
		if(""==value.formatter){
			delete COL_MODEL[index].formatter;
		}
	});
	//그리드 초기화
	try {
		if("grid" === $("#" + param.gridId).attr("role")) {
			$("#" + param.gridId).jqGrid("clearGridData");
			$.jgrid.gridUnload('#' + param.gridId);
		}
		else {
			console.log("else");
		}
	}
	catch(e) {
		console.log(e);
	}

	try {
		$.jgrid.gridUnload('#' + param.gridId);
	} catch(e) { console.log(e); }

	var pagerId = param.gridId + 'Pager';
//	if(gridParamId == 'gridParam'){
		totalGridWidth[param.gridId] = 0;
		$.each(COL_MODEL, function() {
			totalGridWidth[param.gridId] += this.width;
		});
//	}

	var gridData = {
			datatype: "local",
			data: param.data,
			colModel: COL_MODEL,
			cellEdit: param.cellEdit,
			cellsubmit: param.cellsubmit,
			viewrecords: true,
			gridview: true,
			height: null,
			width:null,
			autoheight:true,
			shrinkToFit : shrinkToFit, //grid width auto sizing
			rowList: [30, 50, 100],
			footerrow: false,
			userDataOnFooter: false,
			multiselectWidth : 35,
			rowNum : param.size,
			rownumbers:true,
			multiselect:param.multiSelect,
			loadui: 'disable',
			gridComplete: function(){
				$('.cbox').each(function() {
					if("none" !== $(this).css("display")) {
						$(this).prettyCheckable();
						$(this).parent().addClass('noLabel');
						$(this).change(function() {clickGridCheck($(this)) });
					}
				});
				if('function' === typeof gridComplete && gridParamId != 'gridParam') {
					gridComplete(gridParamId);
				}


				var bDiv = this.grid.bDiv;
				setTimeout(function () {
					bDiv.scrollLeft = 0;
					bDiv.scrollTop = 0;
				}, 0);
				initGridNoData(param.gridId, param.data.length);
				if( !("" === pagerId) ){
					initPage(param, pagerId, true, "TOT", gridParamId);
				}
				changeSortableIcon(param.data, param);
				$("#jqgh_" + param.gridId + "_cb > .cbox").each(function() {
//					$('.cbox').each(function() {
						if("none" !== $(this).css("display")) {
							$(this).prettyCheckable();
							$(this).parent().addClass('noLabel');
							$(this).change(function() {clickGridCheck($(this)) });
						}
					});
					function autoWidthCustom(param){
						baseWidth = $("#" + param.gridId).parents('.gridContainer').width();
						if(baseWidth < totalGridWidth[param.gridId]) {
							gridData['shrinkToFit'] = false;
							$("#" + param.gridId).trigger("reloadGrid");
						}else if(baseWidth >= totalGridWidth[param.gridId]) {
//							if(gridParamId != 'gridParam'){
//								gridData['shrinkToFit'] = false;
//								$("#" + param.gridId).trigger("reloadGrid");
//							}else{
								$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
//							}
						}
					}

					function resizeAutoWidthCustom(param){
						baseWidth = $("#" + param.gridId).parents('.gridContainer').width();
						if(baseWidth < totalGridWidth[param.gridId]) {
							gridData['shrinkToFit'] = false;
							$("#" + param.gridId).trigger("reloadGrid");
							$("#" + param.gridId).parents('.ui-jqgrid-view').css({'width':'100%'});
							$("#" + param.gridId).parents('.ui-jqgrid-view').children('.ui-jqgrid-hdiv').css({'width':'100%'});
							$("#" + param.gridId).parents('.ui-jqgrid-bdiv').css({'width':'100%'});
						}else if(baseWidth >= totalGridWidth[param.gridId]) {
							$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
							$("#" + param.gridId).parents('.ui-jqgrid-view').css({'width':baseWidth + 'px'});
							$("#" + param.gridId).parents('.ui-jqgrid-view').children('.ui-jqgrid-hdiv').css({'width':baseWidth + 'px'});
							$("#" + param.gridId).parents('.ui-jqgrid-bdiv').css({'width':baseWidth + 'px'});
						}
					}


					autoWidthCustom(param);
					gridHeightSizing(param);

					$('.gnbToggle').click(function(e){
						e.preventDefault();
						gridHeightSizing(param);
						resizeAutoWidthCustom(param);
					});

					var resizeTimer;
					$(window).on('resize', function(e) {
						clearTimeout(resizeTimer);
						resizeTimer = setTimeout(function() {
							gridHeightSizing(param);
							resizeAutoWidthCustom(param);

						}, 500);
					});
			},
			onSelectRow: function(rowid, status, e) {
				if('check' == param.selectRowAction){
					onSelectRow(rowid, status, e);
				}
				else{
					onUnselectRow(rowid, param.gridId, e);
				}
			},
			onSortCol: function (index, columnIndex, sortOrder) {
				var param = { postData :
				{
					size: $(this).getGridParam('rowNum'),
					page: 1,
					sortColumn : index,
					order : sortOrder
				}
				};
				$(this).jqGrid('setGridParam', param).trigger("reloadGrid");
			},
			afterEditCell: function(rowid, cellname, value, iRow, iCol){
				afterEditCellFunc(rowid, cellname, value, iRow, iCol);
			},
			beforeSaveCell: function(rowid, cellname, value, iRow, iCol){
				beforeSaveCellFunc(rowid, cellname, value, iRow, iCol);
			},
			afterSaveCell: function(rowid, cellname, value, iRow, iCol){
				afterSaveCellFunc(rowid, cellname, value, iRow, iCol);
			}
	};
	if(undefined === param.usePager || '' === param.usePager || true === param.usePager) {
		gridData["pager"] = pagerId;
	}

	baseWidth = $("#" + param.gridId).parents('.gridContainer').width();
	if(baseWidth < totalGridWidth[param.gridId]) {
		gridData['shrinkToFit'] = false;
		$("#" + param.gridId).trigger("reloadGrid");
	}else if(baseWidth >= totalGridWidth[param.gridId]) {
		if(gridParamId != 'gridParam'){
			gridData['shrinkToFit'] = false;
			$("#" + param.gridId).trigger("reloadGrid");
		}else{
			$("#" + param.gridId).jqGrid('setGridParam',{shrinkToFit: true, autowidth:true}).jqGrid('setGridWidth', baseWidth).trigger("reloadGrid");
		}
	}
	$("#"+param.gridId).jqGrid(gridData);

	gridHeightSizing(param);
}

function searchList(param) {
	if(undefined === param){
		$("#"+param.gridId).jqGrid('setGridParam', {}).trigger("reloadGrid");
	}else{
		var searchParam = JSON.parse(JSON.stringify(param));
		searchParam.url = param.url + getSearchCondition(param);
		if(param.searchAllParam != undefined && param.searchAllParam != ''){
			searchParam.postData = {
					searchAllParam :  JSON.stringify(param.searchAllParam),
					useLike: param.useLike,
					page: param.page,
					size: param.size
			}
		}else{
			searchParam.postData = {
					page: param.page,
					size: param.size,
					useLike: '',
					searchAllParam: ''
			}
		}
		$("#"+param.gridId).jqGrid('setGridParam', searchParam).trigger("reloadGrid");
	}
}

/**
 * form 태그의 object들을 json parameter 형식으로 변경
 */
$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {

    	var name = this.name;
    	var index = this.name.indexOf("_select");

    	if(index > -1) {
    		name = this.name.substring(0, index);
    	}

        if (o[name] !== undefined) {
            if (!o[name].push) {
                o[name] = [o[name]];
            }
            o[name].push(this.value || '');
        } else {
            o[name] = this.value || '';
        }
    });
    return o;
};

function getSearchCondition(param){
	var searchCondition = "";
	if("" == param.formId){
		return "";
	}else{
		searchCondition = "?";
	}

	searchCondition += $('#'+param.formId).serialize();
	return searchCondition;
}

function getGridColumn(gridId){

	$.ajax(
		    {
		        type: "POST",
		        url: CONTEXT_PATH + "/gridInfo/selectGridColumn",
		        data: {"gridId": gridId},
		        dataType: "json",
		        async: false,
		        success: function(result){
		        	COL_MODEL = result;
		        },
		        error: function(x, e){
		             alert(x.readyState + " "+ x.status +" "+ e.msg);
		        }
		     });
}


function initGridNoData(gridId, records) {
	var $bdiv = $("#gbox_" + gridId + " .ui-jqgrid-bdiv");
	var html = '<div class="noData">데이터가 없습니다.</div>';

	$bdiv.find(".noData").remove();
	$bdiv.removeClass("grid-empty");

	if(0 === records) {
		$bdiv.addClass("grid-empty").append(html);
	}
}

/**
 * 체크박스가 체크되면 grid의 row를 select 한다.
 * @param $o
 */
function clickGridCheck($o) {
	var i=0;
	var id = $o.attr("id");
	var arr = undefined;
	var checked = $o.is(":checked");
	var gridId = undefined;

	if("cb" === id.substring(0, 2)) {
		// 전체 체크박스가 체크 되었을 경우
		gridId = id.substring(3, id.length);

		var checked = $.trim($("#jqgh_" + gridId + "_cb .prettycheckbox a").attr("class"));
		$("#" + gridId).resetSelection();
		if("" === checked) {
			// 비어있으면 체크를 해야 한다.
			var ids = $("#" + gridId).jqGrid('getDataIDs');

			for(i=0; i<ids.length; i++) {
				$("#" + gridId).jqGrid("setSelection", ids[i]);
			}

			$("#" + gridId + " > tbody > tr > td > div > .cbox").each(function() {
//				$("#jqgh_" + gridId + "_cb > .cbox").each(function() {
				var itemId = $(this).attr("id");
				if("cb" !== itemId.substring(0, 2)) {
					$(this).prettyCheckable("check");
				}
			});
		}
		else {
			$("#" + gridId + " > tbody > tr > td > div > .cbox").each(function() {
//				$("#jqgh_" + gridId + "_cb > .cbox").each(function() {
				var itemId = $(this).attr("id");

				if("cb" !== itemId.substring(0, 2)) {
					$(this).prettyCheckable("uncheck");
				}
			});
		}
	}
	else {
		// 개별 체크박스가 체크되었을 경우
		id = id.substring(4, id.length);
		var arr = id.split("_");

		gridId = [];
		var gridRowId = "";

		for(i=0; i<arr.length - 1; i++) {
			gridId.push(arr[i]);
		}

		gridRowId = arr[arr.length - 1];

		$("#" + gridId.join('_')).jqGrid("setSelection", gridRowId);
	}
}


function onSelectRow(rowId, status, e) {
	if(e) {
		var gridId = e.currentTarget.id;

		$("#jqg_" + gridId + "_" + rowId).each(function() {
			if(status) {
				$(this).prettyCheckable("check");
			}
			else {
				$(this).prettyCheckable("uncheck");
			}

		});

		if('undefined' !== typeof onCustomSelectRow) {
			onCustomSelectRow(rowId, status, e);
		}
	}
}


function onUnselectRow(rowId, gridId, e) {
	if(e) {
		$("#" + gridId).jqGrid("setSelection", rowId);
	}
}

function changeSortableIcon(data, param) {
	$.each(COL_MODEL, function() {
		if(this.sortable) {
			$("#" + param.gridId + "_" + this.index + " > .ui-jqgrid-sortable > .s-ico").addClass("sortable");
		}
	});
}

function formatTest(cellValue, options, rowdata, action){
	return '<a onclick="test(1)">'+cellValue+'</a>';
}

function getExcelCondition(param, listId) {
	var o = $("#" + param.formId).serializeObject();

	$.each(o, function(key, value) {
		param[key] = value;
	});

	param["listId"] = listId;
	if (param.gridId === "gridDxfRequestList" && param.listId === "sql.DistributionProductionRequest.selectList") {
		param.listId = "sql.DxfRequest.selectList";
	}
	if (param.gridId === "gridDxfRequestList" && !param.objectType) {
		param.objectType = "DXF";
	}
	if (param.gridId === "gridPeerReviewRequestList") {
		param.listId = "sql.PeerReview.selectList";
	}
	if (param.gridId === "gridDistributionProductionRequestList" && !param.objectType) {
		param.objectType = "PRODUCT";
	}

	return param;
}


function exportExcel(listId){
	confirmMessage(g_msg("msg.confirmDownload"), function(){
		$(this).dialog("close");

		$.ajax({
	        url: CONTEXT_PATH + "/common/createExcel/createExcel"
	       ,type  : "post"
	       ,cache : false
	       ,async : true
	       ,data  : getExcelCondition(gridParam, listId)
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
				$("#progress").hide();
			}
	   });
	});
}

function duanzongExcel() {
	confirmMessage(g_msg("msg.confirmDownload"), function(){
		$(this).dialog("close");

		$.ajax({
	        url: CONTEXT_PATH + "/common/createExcel/createExcelDuanzongPdm"
	       ,type  : "post"
	       ,cache : false
	       ,async : true
	       ,data  : getExcelCondition(gridParam, 'sql.DuanzongPdm.selectList')
		   ,beforeSend:function(){
		   }
		   , error:function(e){
				alertMessage(g_msg("msg.error") + '[' + e + ']');
		   }
	   }).done(function(data){
		   if(data.result == 'fail'){
				alertMessage(data.reason);
			}else{
				downloadExcel(data.url);
			}
	   });
	});
}

function downloadExcel(url){
	jQuery('<form action="'+encodeForURL(url)+'" method="get"></form>')
	.appendTo('body').submit().remove();
}

function formatDistributionApproval(cellValue, options, rowdata, action){
	return '<a onclick="viewDistributionApproval(\''+cellValue+'\', \''+rowdata['approvalLineId']+'\', \''+rowdata['currentProcessSeqNo']+'\', \''+rowdata['objectType']+'\')">'+cellValue+'</a>';
}

function formatViewPrintApproval(cellValue, options, rowdata, action){
	return '<a onclick="viewPrintApprovalPopup(\''+cellValue+'\', \'' + rowdata['objectType'] + '\')">'+cellValue+'</a>';
}

/* 출력물 폐기 승인/반려 팝업 */
function formatViewPrintDestroyApproval(cellValue, options, rowdata, action){
	return '<a onclick="viewPrintDestroyApprovalPopup(\''+cellValue+'\')">'+cellValue+'</a>';
}

/* 미등록자료 배포 > 배포요청현황 > 배포 요청 상세보기 팝업 */
function formatNoregDistributionStatus(cellValue, options, rowdata, action){
	return '<a onclick="viewNoregDistributionStatus(\''+cellValue+'\')">'+cellValue+'</a>';
}

/* 자료전송 접수 상세보기 팝업 */
function formatOutregDistributionStatus(cellValue, options, rowdata, action){
	return '<a onclick="viewOutregDistributionStatus(\''+cellValue+'\')">'+cellValue+'</a>';
}
/* 미등록자료 베포 > 배포승인 > 배포 승인 팝업 */
function formatUnregDistributionApproval(cellValue, options, rowdata, action){
	if("REQUEST"==rowdata["statusCd"]){
		return '<a onclick="viewNoregDistributionApproval(\''+cellValue+'\')">'+cellValue+'</a>';
	}else{
		return cellValue;
	}
}

/* 생산기술자료 폐기 승인 */
function formatDisposalApproval(cellValue, options, rowdata, action){
	return '<a onclick="viewDisposalApproval(\''+cellValue+'\')">'+cellValue+'</a>';
}


/* 그리드에 selectbox 추가 */
function formatSelectBox(cellValue, options, rowdata, action){
	var param = {
			comboCd: options.colModel.name
	}
	var rtn;
	callAjax(param, "/combo/selectComboList", function(response){
		rtn = selectBoxCallback(response, options);
	}, 'json', false);
	return rtn;
}

/* select에 option 추가 option 선택 시 selectBoxChange function이 실행됨 */
function selectBoxCallback(response, options){
	var rtn;
	var width = options.colModel.width - 10;
	rtn = "<select id='"+ options.colModel.name + options.rowId + "' name='"+ options.colModel.name + options.rowId + "' title='"+ options.colModel.label + "'>";
	rtn += "<option value=''>"+g_msg('form.select')+"</option>";
	$.each(response, function(index, data){
		rtn += "<option value='"+ data.comboVal+"'>"+data.comboLabel+"</option>";
	});
	rtn += "</select>";
	if(response.length >= 10){
		rtn += "<script>$('#"+options.colModel.name + options.rowId + "').select2({width: "+ width +", dropdownParent: $('#popupDialoga')});</script>";
	}else{
		rtn += "<script>$('#"+options.colModel.name + options.rowId + "').select2({width: "+ width +", minimumResultsForSearch: -1});</script>";
	}
	rtn += "<script>$('#"+options.colModel.name + options.rowId + "').on('select2:select', function(e){selectBoxChange(e)});</script>";
	return rtn;
}

/* grid에 빈 selectbox 추가 */
function formatEmptySelectBox(cellValue, options, rowdata, action){
	var rtn;
	var width = options.colModel.width - 10;
	nameColumnWidth = width;
	rtn = "<select id='"+ options.colModel.name + options.rowId + "' name='"+ options.colModel.name + options.rowId + "' title='"+ options.colModel.label + "'></select>";
	rtn += "<script>$('#"+options.colModel.name + options.rowId + "').select2({width: "+ width +", minimumResultsForSearch: -1});</script>";
	return rtn;
}


function afterEditCellFunc(rowid, cellname, value, iRow, iCol){}
function beforeSaveCellFunc(rowid, cellname, value, iRow, iCol){}
function afterSaveCellFunc(rowid, cellname, value, iRow, iCol){}


function formatOpenViewer(cellValue, options, rowdata, action){
	return '<a onclick="openViewer(\''+rowdata["objectId"]+'\', \''+rowdata["objectType"]+'\', \'' +rowdata["requestType"] +'\', \'' + rowdata["requestNo"] +'\')">'+cellValue+'</a>';
}
