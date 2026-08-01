var selectedGroupCd = "";
var selectedType = "";

function roleText(key, fallback) {
	var args = Array.prototype.slice.call(arguments, 2);
	if (window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
		return window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
	}
	return fallback;
}

function localizeRoleToolbar() {
	$(".role-management-toolbar button").each(function() {
		if (String($(this).attr("onclick") || "").indexOf("saveRole") !== -1) {
			$(this).text(roleText("feature.common.button.save", "저장"));
		}
	});
}

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
	localizeRoleToolbar();
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

	callAjax(param, '/general/system/role/getAssignedUser', function(response){
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

	callAjax(param, '/general/system/role/getAssignedDept', function(response){
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
	selectedGroupCd = $div.attr('data-group-cd') || $div.attr('groupCd');
	settingAssignedDeptGrid();
	settingAssignedUserGrid();
	$div.parent("li").addClass("current");
	$("#selectedGroupName").text($.trim($div.text()));
}

/**
 * 권한그룹 추가
 * @returns
 */
function addGroup() {
	openDialogPopup("/general/system/role/roleAddPopup"
			, {}
			, "popupDialog", 's', 230, true, 'popup-common popup-role');
}

function modGroup() {
	if('' === selectedGroupCd) {
		alertMessage(roleText('feature.system.role.message.selectForEdit', '수정할 사용자등급을 선택하세요.'));
		return;
	}

	openDialogPopup("/general/system/role/roleModPopup"
			, {groupCd: selectedGroupCd}
			, "popupDialog", 's', 230, true, 'popup-common popup-role');
}

function delGroup() {
	if('' === selectedGroupCd) {
		alertMessage(roleText('feature.system.role.message.selectForDelete', '삭제할 사용자등급을 선택하세요.'));
		return;
	}

	var param = {
			saveFlag: 'D',
			groupCd: selectedGroupCd
	}

	var groupName = $.trim($("#" + selectedGroupCd).text());
	confirmMessage(roleText(
		'feature.system.role.message.confirmDelete',
		'"{0}" 사용자등급을 삭제하시겠습니까?<br>삭제하면 이 등급의 권한을 더 이상 사용할 수 없습니다.',
		groupName
	), function(){
		$("#confirmMessage").dialog("close");

		callAjax(param, '/general/system/role/saveRoleGroup', function(response){
			if(response.success) {
				alertMessage(roleText('feature.system.role.message.deleted', '사용자등급을 삭제했습니다.'));
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

	callAjax(param, '/general/system/role/getRoleGroupList', function(response){
		var groupList = response || [];
		var $list = $(".listBox").empty();

		$.each(groupList, function() {
			var $button = $("<button>", {
				type: "button",
				class: "listName",
				id: this.groupCd,
				"data-group-cd": this.groupCd
			}).append($("<span>").text(this.groupNm));
			$list.append($("<li>").append($button));
		});

		$("#managerCount").text(groupList.length);

		if (groupList.length === 0) {
			selectedGroupCd = "";
			$("#selectedGroupName").text(roleText(
				'feature.system.role.assignment.selectGrade',
				'등급을 선택하세요'
			));
			$list.append(
				$("<li>", { class: "role-group-empty" }).text(roleText(
					'feature.system.role.group.empty',
					'등록된 사용자등급이 없습니다.'
				))
			);
			return;
		}

		$list.find(".listName").first().trigger('click');
	});
}

function saveRole() {
	if("" === selectedGroupCd) {
		alertMessage(roleText('feature.system.role.message.noSelectedGrade', '선택된 사용자등급이 없습니다.'));
		return;
	}
	var param = getParam();

	callAjax(param, '/general/system/role/saveRoleGroupMember', function(response){
		if(response.success) {
			alertMessage(roleText('feature.system.role.message.saved', '사용자등급 배정을 저장했습니다.'));
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
		gridId = 'gridRoleUserAssigned';
		targetParam = assignedUserParam;
		key = "userCd";
		searchType = 'assignedUser';
	}

	var checkedDataList = $("#" + gridId).getGridParam('selarrrow');
	if (!checkedDataList || checkedDataList.length === 0) {
		alertMessage(g_msg('msg.noSelectData'));
		return;
	}

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

	$.each(checkedDataList, function() {
		var checkedData = $("#" + gridId).jqGrid('getRowData', this);
		var isAlready = false;

		$.each(targetParam.data,function() {
			var ts = this;

			if(ts[key] === checkedData[key]) {
				isAlready = true;
				arrAlready.push(checkedData);
				return false;
			}
		});

		if(!isAlready) {
			targetParam.data.push(checkedData);
		}
	});

	if(arrAlready.length > 0) {
		alertMessage(roleText(
			'feature.system.role.message.duplicateAssignment',
			'이미 배정된 {0}건은 추가하지 않았습니다.',
			arrAlready.length
		));
	}

	$("#" + targetParam.formId).find('input[type=text]').val('');

	initLocalGrid(targetParam);
}
