var selectedGroupCd = "";
var selectedType = "";

var assignedDeptParam = {
		gridId: 'gridRoleDeptAssigned',
		formId: 'formRoleDeptAssign',
		data: [],
		searchData: [],
		gridColumnData: JSON.parse(gridRoleDeptAssigned)
};

var assignedUserParam = {
		gridId: 'gridRoleUserAssigned',
		formId: 'formRoleUserAssign',
		data: [],
		searchData: [],
		gridColumnData: JSON.parse(gridRoleUserAssigned)
};

function initWindow() {
	setManagerGroupList();

	var active = $( "#tabs" ).tabs( "option", "active" );
	var searchType = undefined;

	if(0 === active) {
		// 부서
		searchType = 'assignedDept';
	}
	else {
		// 사용자
		searchType = 'assignedUser';
	}

	searchList2(searchType);
}

$(function() {
	$("#tabs").tabs();
	settingToolbar(JSON.parse(toolbarInfo));
	setGridParam();
	settingGrid(gridRoleDept, deptGridParam, 'deptGridParam');
	settingGrid(gridRoleUser, userGridParam, 'userGridParam');
	settingAssignedDeptGrid();
	settingAssignedUserGrid();

	initWindow();

	bindEvent();
	bindTabResize();
	refreshVisibleRoleGrids();

	$("body").css({"visibility": "visible"});
});

function resizeGridToContainer(gridId) {
	var $grid = $("#" + gridId);
	if ($grid.length === 0 || $grid.closest(":visible").length === 0) {
		return;
	}

	var containerWidth = $grid.closest('.gridContainer').width();
	if (!containerWidth) {
		return;
	}

	try {
		$grid.jqGrid('setGridWidth', containerWidth, true);
	} catch (e) {}
}

function refreshVisibleRoleGrids() {
	var active = $("#tabs").tabs("option", "active");
	var gridIds = active === 0
		? ['gridRoleDept', 'gridRoleDeptAssigned']
		: ['gridRoleUser', 'gridRoleUserAssigned'];

	$.each(gridIds, function(_, gridId) {
		resizeGridToContainer(gridId);
	});
}

function bindTabResize() {
	$("#tabs").on("tabsactivate", function() {
		setTimeout(function() {
			refreshVisibleRoleGrids();
		}, 0);
	});

	var resizeTimer;
	$(window).on("resize.roleSideGrid", function() {
		clearTimeout(resizeTimer);
		resizeTimer = setTimeout(function() {
			refreshVisibleRoleGrids();
		}, 150);
	});
}

/**
 * grid에 사용할 colNames를 구한다.(컬럼명)
 * @param d
 * @returns
 */
function getColNames(data) {
	var colNames = [];

	$.each(data, function() {
		colNames.push(this.label);
	});

	return colNames;
}

function getColModel(data) {
	var colModel = [];

	$.each(data, function() {
		colModel.push({
			name: this.name,
			index: this.name,
			width: this.width,
			align: this.align,
			sortable: false,
			hidden: this.hidden
		});
	});

	return colModel;
}


/**
 * 그룹에 할당된 사용자 목록 grid를 setting
 * @returns
 */
function settingAssignedUserGrid() {
	var param = {
			groupCd: selectedGroupCd
	};

	callAjax(param, '/inside/system/role/getAssignedUser', function(response){
		assignedUserParam.data = response;
		assignedUserParam.searchData = [];
		initLocalGrid(assignedUserParam);
	})
}

/**
 * 그룹에 할당된 부서 목록 grid를 setting
 * @returns
 */
function settingAssignedDeptGrid() {
	var param = {
			groupCd: selectedGroupCd
	};

	callAjax(param, '/inside/system/role/getAssignedDept', function(response){
		assignedDeptParam.data = response;
		assignedDeptParam.searchData = [];
		initLocalGrid(assignedDeptParam);
	})
}

/**
 * 할당된 부서/사용자 grid를 search
 * @param type
 * @returns
 */
function searchList2(type) {
	var getParam = function(type) {
		if('assignedDept' === type) {
			return assignedDeptParam;
		}
		else {
			return assignedUserParam;
		}
	};

	var param = getParam(type);
	var searchText = $("#" + param.formId + " input[name=searchText]").val();
	var result = [];

	$.each(param.data, function() {
		var key = ('assignedDept' === type ? this.deptNm : this.userNm);

		if(key.indexOf(searchText) > -1) {
			result.push(this);
		}
	});

	param.searchData = result;

	initLocalGrid(param, 'search');
}

/**
 * 할당된 부서/사용자 grid를 init
 * @param param
 * @returns
 */
function initLocalGrid(param, search) {
	try { $("#" + param.gridId).jqGrid('clearGridData'); } catch(e) { }
	try { $.jgrid.gridUnload('#' + param.gridId); } catch(e) { }
	try { $("#" + param.gridId).jqGrid('GridDestroy'); } catch(e) { }

	var data = undefined;

	if(undefined === search) {
		data = param.data;
//		data = param.searchData.length > 0 ? param.searchData : param.data;
	}
	else {
		if("search" === search) {
			data = param.searchData;
		}
		else {
			data = param.data;
		}
	}

	if('gridRoleDeptAssigned' === param.gridId) {
		$("#assignedDeptCount").text(param.data.length);
	}
	else {
		$("#assignedUserCount").text(param.data.length);
	}

	$("#" + param.gridId).jqGrid({
		datatype: "local",
		data: data,
		colNames: getColNames(param.gridColumnData),
		colModel:getColModel(param.gridColumnData),
		autowidth: true,
		shrinkToFit: true,
		multiselect: true,
		//width : null,
		//height : null,
		//autoheight : true,
		//shrinkToFit : false,
		rowNum : 100,
		//rownumbers : true,
		caption : false,
		//loadtext : /*'<img src=''/>'*/ 'loading~~',
//		pager : '#pagerArea',
	    //rowList: [15,30,50,100],        // disable page size dropdown
	    //pgbuttons: true,
        //viewsortcols : [ false, 'horizontal', true ],
	    viewrecords: true,
	    loadComplete: function(data) {
//	    	initPage("jqGrid", "pagerArea", true, "TOT");
	    },
	    gridComplete: function(){
			$('.cbox').each(function() {
				if("none" !== $(this).css("display")) {
					$(this).prettyCheckable();
					$(this).parent().addClass('noLabel');
		        	$(this).change(function() {clickGridCheck($(this)) });
				}
		    });
		},
		loadComplete: function(data){
			$("#jqgh_" + param.gridId + "_cb > .cbox").each(function() {
//				$('.cbox').each(function() {
				if("none" !== $(this).css("display")) {
					$(this).prettyCheckable();
					$(this).parent().addClass('noLabel');
		        	$(this).change(function() {clickGridCheck($(this)) });
				}
		    });
			resizeGridToContainer(param.gridId);
		},
		ondblClickRow: function(rowId){
			ondblClickRowLocal(rowId);
		},
		onSelectRow: function(rowid, status, e) {
			onSelectRow(rowid, status, e);
		},
	});
}

function bindEvent() {
	$(document).on('click', '.listName', function() {
//	$(".listName").click(function() {
		clickGroup($(this));
	});
}

function clickGroup($div) {
	$(".listBox > li").removeClass("current");
	selectedGroupCd = $div.attr('groupCd');
	settingAssignedDeptGrid();
	settingAssignedUserGrid();
	$div.parent("li").addClass("current");
}

/**
 * 권한그룹 추가
 * @returns
 */
function addGroup() {
	openDialogPopup("/inside/system/role/roleAddPopup"
			, {}
			, "popupDialog", 's', 170, true, 'popup-common popup-role');
}

function modGroup() {
	if('' === selectedGroupCd) {
		alertMessage("수정할 그룹을 선택해주세요");
		return;
	}

	openDialogPopup("/inside/system/role/roleModPopup"
			, {groupCd: selectedGroupCd}
			, "popupDialog", 's', 170, true, 'popup-common popup-role');
}

function delGroup() {
	if('' === selectedGroupCd) {
		alertMessage("삭제할 그룹을 선택해주세요");
		return;
	}

	var param = {
			saveFlag: 'D',
			groupCd: selectedGroupCd
	}

	confirmMessage($("div#" + selectedGroupCd + " > span").text() + "을(를) 삭제하시겠습니까?<br>삭제시 권한 사용이 불가능하오니 참고하세요.", function(){
		$("#confirmMessage").dialog("close");

		callAjax(param, '/inside/system/role/saveRoleGroup', function(response){
			if(response.success) {
				alertMessage(g_msg("msg.completeDelete"));
				initWindow();
			}
			else {
				alertMessage(response.failReason);
			}
		});
	});
}

/**
 * 권한그룹 목록을 화면에 표시
 * @returns
 */
function setManagerGroupList() {

	var param = {};

	callAjax(param, '/inside/system/role/getRoleGroupList', function(response){
  		var html = [];
  		$.each(response, function() {
  			html.push('<li>');
  			html.push('	<div class="listName" id="' + this.groupCd + '" groupCd="' + this.groupCd + '"><span>' + this.groupNm + '</span></div>');
  			html.push('</li>');
  		});

  		$(".listBox").html(html.join(''));
  		$("#managerCount").text(response.length);

  		$(".listName").eq(0).trigger('click');
	});
}

function saveRole() {
	if("" === selectedGroupCd) {
		alertMessage("선택된 그룹이 없습니다.");
		return;
	}
	var param = getParam();

	callAjax(param, '/inside/system/role/saveRoleGroupMember', function(response){
		if(response.success) {
			alertMessage(g_msg("msg.completeSave"));
			initWindow();
		}
		else {
			alertMessage(g_msg(response.message));
		}
	});
}

/**
 * 저장할 param을 구함.
 * @returns
 */
function getParam() {
	var assignedDept = [];
	var assignedUser = [];

	$.each(assignedDeptParam.data, function() {
		assignedDept.push(this.deptCd);
	});

	$.each(assignedUserParam.data, function() {
		assignedUser.push(this.userCd);
	});

	return {
		groupCd: selectedGroupCd
		,assignedDept: assignedDept
		,assignedUser: assignedUser
	};
}

function ondblClickRowLocal(rowId) {
	var active = $( "#tabs" ).tabs( "option", "active" );
	var gridId = undefined;
	var targetParam = undefined;
	var key = undefined;
	var searchType = undefined;
	var i=0;

	if(0 === active) {
		// 부서
		gridId = 'gridRoleDeptAssigned';
		targetParam = assignedDeptParam;
		key = "deptCd";
		searchType = 'assignedDept';
	}
	else {
		// 사용자
		gridId = 'gridRoleUserAssigned';
		targetParam = assignedUserParam;
		key = "userCd";
		searchType = 'assignedUser';
	}

	var clickedData = $("#" + gridId).jqGrid('getRowData', rowId);

	for(i=0; i<targetParam.data.length; i++) {
		var ts = targetParam.data[i];

		if(ts[key] === clickedData[key]) {
			targetParam.data.splice(i, 1);
			break;
		}
	}

	initLocalGrid(targetParam);
}

/**
 * 부서/사용자 전체 목록 grid를 더블클릭 했을 경우 할당쪽으로 이동
 * @param rowId
 * @returns
 */
function ondblClickRowFunc(rowId) {
	var active = $( "#tabs" ).tabs( "option", "active" );
	var gridId = undefined;
	var targetParam = undefined;
	var key = undefined;
	var searchType = undefined;

	if(0 === active) {
		// 부서
		gridId = 'gridRoleDept';
		targetParam = assignedDeptParam;
		key = "deptCd";
		searchType = 'assignedDept';
	}
	else {
		// 사용자
		gridId = 'gridRoleUser';
		targetParam = assignedUserParam;
		key = "userCd";
		searchType = 'assignedUser';
	}

	var clickedData = $("#" + gridId).jqGrid('getRowData', rowId);
	var isAlready = false;

	$.each(targetParam.data,function() {
		var ts = this;
		if(ts[key] === clickedData[key]) {
			alertMessage("이미 할당되어있습니다.");
			isAlready = true;
			return false;
		}
	});

	if(isAlready) { return; }

	targetParam.data.push(clickedData);
	targetParam.searchData = [];			// 검색조건 초기화

	$("#" + targetParam.formId).find('input[type=text]').val('');	// 검색조건 초기화

//	if('assignedDept' === searchType) {
		initLocalGrid(targetParam);
/*	}
	else {
		settingAssignedUserGrid();

	}
*/
//	searchList2(searchType);

	//console.log(active);
}

function delList() {
	var active = $( "#tabs" ).tabs( "option", "active" );
	var gridId = undefined;
	var targetParam = undefined;
	var key = undefined;
	var searchType = undefined;
	var i=0;

	if(0 === active) {
		// 부서
		gridId = 'gridRoleDeptAssigned';
		targetParam = assignedDeptParam;
		key = "deptCd";
		searchType = 'assignedDept';
	}
	else {
		// 사용자
		gridId = 'gridRoleDeptAssigned';
		targetParam = assignedUserParam;
		key = "userCd";
		searchType = 'assignedUser';
	}

	var checkedDataList = $("#" + gridId).getGridParam('selarrrow');

	$.each(checkedDataList, function() {
		var checkedData = $("#" + gridId).jqGrid('getRowData', this);
		var isAlready = false;

		for(i=0; i<targetParam.data.length; i++) {
			var ts = targetParam.data[i];

			if(ts[key] === checkedData[key]) {
				targetParam.data.splice(i, 1);
				break;
			}
		}
	});

	initLocalGrid(targetParam);
}

function addList() {
	var active = $( "#tabs" ).tabs( "option", "active" );
	var gridId = undefined;
	var targetParam = undefined;
	var key = undefined;
	var searchType = undefined;

	if(0 === active) {
		// 부서
		gridId = 'gridRoleDept';
		targetParam = assignedDeptParam;
		key = "deptCd";
		searchType = 'assignedDept';
	}
	else {
		// 사용자
		gridId = 'gridRoleUser';
		targetParam = assignedUserParam;
		key = "userCd";
		searchType = 'assignedUser';
	}

	var checkedDataList = $("#" + gridId).getGridParam('selarrrow');

	if(checkedDataList.length == 0) {
		alertMessage(g_msg('msg.noSelectData'));
		return;
	}

	var arrAlready = [];

	console.log(checkedDataList);
	console.log(targetParam);

	$.each(checkedDataList, function() {
		var checkedData = $("#" + gridId).jqGrid('getRowData', this);
		var isAlready = false;

		$.each(targetParam.data,function() {
			var ts = this;

			if(ts[key] === checkedData[key]) {
				isAlready = true;
				console.log("already");
				arrAlready.push(checkedData);
				return false;
			}
		});

		if(!isAlready) {
			targetParam.data.push(checkedData);
		}
	});

	if(arrAlready.length > 0) {
		alertMessage("이미 할당된 " + arrAlready.length + "건의 데이터는 추가되지 않았습니다.");
	}

	$("#" + targetParam.formId).find('input[type=text]').val('');

	initLocalGrid(targetParam);
}
