var selectedRoleCode = "";
var selectedNodeList = [];

(function ($, undefined) {
"use strict";
$.jstree.plugins.noclose = function () {
  this.close_node = $.noop;
};
})(jQuery);

$(function() {
	setManagerGroupList();
	//setMenuList();
	settingToolbar(JSON.parse(toolbarInfo));
	bindEvent();
});

function bindEvent() {
	$(document).on('click', '.listName', function() {
		$(".listBox > li").removeClass("current");
		selectedRoleCode = $(this).attr('groupCd');
		setMenuList($(this).attr('groupCd'));
		$(this).parent("li").addClass("current");
	});

	$(document).on('click', ".tree-checkbox", function() {
		$(this).toggleClass("tree-checkbox-on");

		var tsClass = $(this).attr("class");
		var id = $(this).attr("id");

		if(tsClass.indexOf("tree-checkbox-on") > -1) {
			selectedNodeList[id.substring(4, id.length)] = 'Y';
		}
		else {
			selectedNodeList[id.substring(4, id.length)] = 'N';
		}

		var node = $("#menuTree").jstree(true).get_node(id.substring(4, id.length));

		if(node.children.length > 0) {
			$.each(node.children, function() {
				$("#chk_" + this).removeClass("tree-checkbox-on");
				selectedNodeList[this] = 'N';

				if(tsClass.indexOf("tree-checkbox-on") > -1) {
					$("#chk_" + this).addClass("tree-checkbox-on");
					selectedNodeList[this] = 'Y';
				}
			});
		}
	});
}

/**
 * 메뉴 tree를 화면에 표시
 * @returns
 */
function setMenuList(groupCd) {
	var param = {
			authSite: 'I'	/* 내부 목록만 표시*/
			, groupCd: groupCd
	};

	//console.log(param);

	callAjax(param, '/inside/system/roleassign/getAssignedMenuList', function(response){
		var treeParam = {
				list: response.menuList,
				useCheckbox: false,
				customCheckbox: true,
				dragDrop: false,
				openLevel: 0,
				noClose: false,
				multiple: false,
				//selectedValue: response.selectedValue
		};

		settingTree("menuTree", treeParam);

		selectedNodeList = response.selectedValue;

		setValue();
	});
}

function setValue() {
	$.each(selectedNodeList, function(id, value) {
		//console.log(id + ", " + value);
		if('Y' === value) {
			$("#chk_" + id).addClass("tree-checkbox-on");
		}
	});
}

/**
 * 권한그룹 목록을 화면에 표시
 * @returns
 */
function setManagerGroupList() {

	var param = {};

	callAjax(param, '/inside/system/roleassign/getRoleGroupList', function(response){
  		var html = [];
  		$.each(response, function() {
  			html.push('<li>');
  			html.push('	<div class="listName" groupCd="' + this.groupCd + '"><span>' + this.groupNm + '</span></div>');
//  			html.push('	<button type="button" class="detailBtn" onclick="modGroup(\'' + this.groupCd + '\')">상세보기</button>');
  			html.push('</li>');
  		});

  		$(".listBox").html(html.join(''));
  		$("#managerCount").text(response.length);
	})
}

function saveRole() {
	if("" === selectedRoleCode) {
		alertMessage("선택된 그룹이 없습니다.");
		return;
	}
	var param = getParam();

	//console.log(param);

//	return;

	callAjax(param, '/inside/system/roleassign/saveAssign', function(response){
		if(response.success) {
			alertMessage(g_msg("msg.completeSave"));
			setMenuList(selectedRoleCode);
		}
		else {
			alertMessage(response.failReason);
		}
	});
}

/**
 * 저장할 param을 구함.
 * @returns
 */
function getParam() {
	var checkedNode = [];
	var i=0;

	var root = $('#menuTree').jstree(true).get_node('#');
	var arrAllNode = root.children_d;
	var getSelected = function(node) {
		if(node.state.selected) {
			return "Y";
		}

		// 체크상태가 아니더라도, child node가 하나라도 체크되어 있다면 Y이어야 함.
		// jstree-undetermined class가 존재한다면 child node가 하나라도 있는 것임
		if($("#" + node.id + "_anchor > i").eq(0).attr("class").indexOf("jstree-undetermined") > -1) {
			return "Y";
		}

		return "N";

	};
	//var node = $('#menuTree').jstree(true).get_node('MENU_001');

	for(i=0; i<arrAllNode.length; i++) {
		var node = $('#menuTree').jstree(true).get_node(arrAllNode[i]);

		//console.log(node);

		checkedNode.push({
			menuCd: node.id,
			roleCd: node.original.roleCd,
			selectedYn: undefined === selectedNodeList[node.id] ? 'N' : selectedNodeList[node.id]
		});
	}

	return {
		list: checkedNode,
		groupCd: selectedRoleCode
	};
}

