var selectedNodeList = [];

//(function ($, undefined) {
//    "use strict";
//    $.jstree.plugins.noclose = function () {
//        this.close_node = $.noop;
//    };
//})(jQuery);

$(function() {
	settingToolbar(JSON.parse(insideToolbarInfo), $("#insideBtnArea"));
	settingToolbar(JSON.parse(outsideToolbarInfo), $("#outsideBtnArea"));
	setTree();
	bindEvent();
});

function bindEvent() {
//	$("#menuTree").on("select_node.jstree", function (e, data) {
//
//		alert("node_id: " + data.node.id);
//	})
}

function addInsideMenu() { addMenu('I'); }
function modInsideMenu() { modMenu('I'); }
function delInsideMenu() { delMenu('I'); }
function saveInsideMenu() { saveMenu('I'); }

function addOutsideMenu() { addMenu('E'); }
function modOutsideMenu() { modMenu('E'); }
function delOutsideMenu() { delMenu('E'); }
function saveOutsideMenu() { saveMenu('E'); }

function setTree() {
	callAjax({authSite: 'I'}, '/inside/system/menu/getTreeList', function(response){
		insideTreeList = response;

		var insideTreeParam = {
				list: insideTreeList,
				useCheckbox: false,
				dragDrop: true,
				openLevel: 2,
				customCheckbox: false
				//selectedValue: selectedNodeList
		};

		settingTree("insideMenuTree", insideTreeParam);
	});

	callAjax({authSite: 'E'}, '/inside/system/menu/getTreeList', function(response){
		outsideTreeList = response;

		var outsideTreeParam = {
				list: outsideTreeList,
				useCheckbox: false,
				dragDrop: true,
				openLevel: 2,
				customCheckbox: false
				//selectedValue: selectedNodeList
		};

		settingTree("outsideMenuTree", outsideTreeParam);
	});
}

/**
 * Tree의 ID를 구한다.
 * @param auth I/E
 * @returns
 */
function getTreeId(auth) {
	if('I' === auth) return "insideMenuTree";
	else return "outsideMenuTree";

}

/**
 * 메뉴 순서 저장
 * @param auth
 * @returns
 */
function saveMenu(auth) {
	var treeId = getTreeId(auth)

	var param = {list: getParam(treeId)};

	callAjax(param, '/inside/system/menu/saveMenuSort', function(){
		alertMessage(g_msg('msg.completeSave'), function(){			//저장되었습니다.
			$(this).dialog("close");
		});

		setTree();
	})
}

function addMenu(auth) {
	var treeId = getTreeId(auth)
	var selectedNode = $("#" + treeId).jstree("get_selected");

	if(selectedNode.length === 0) {
		alertMessage(g_msg('msg.plzSelectParentMenu'), function() {
			$(this).dialog("close");
		});
		return;
	}

	openDialogPopup("/inside/system/menu/menuAddPopup"
			, {menuCd: selectedNode[0], authSite: auth}
			, "popupDialog", 'm', 270, true, 'popup-common popup-menu');
}

function modMenu(auth) {
	var treeId = getTreeId(auth)
	var selectedNode = $("#" + treeId).jstree("get_selected");

	if(selectedNode.length === 0) {
		alertMessage(g_msg('msg.plzSelectMenu'), function() {
			$(this).dialog("close");
		});
		return;
	}

	openDialogPopup("/inside/system/menu/menuModPopup"
			, {menuCd: selectedNode[0], authSite: auth}
			, "popupDialog", 'm', 270, true, 'popup-common popup-menu');
}

function delMenu(auth) {
	var treeId = getTreeId(auth)
	var selectedNode = $("#" + treeId).jstree("get_selected");

	if(selectedNode.length === 0) {
		alertMessage(g_msg('msg.plzSelectMenu'), function() {
			$(this).dialog("close");
		});
		return;
	}

	var node = $("#" + treeId).jstree(true).get_node(selectedNode[0]);
	var menuType = node.original.type;

	var param = {
			menuCd: selectedNode[0]
			, children: node.children_d
			, saveFlag: 'D'
			, menuType: menuType
	};

	confirmMessage("선택하신 메뉴와 그 하위메뉴를 모두 삭제하시겠습니까?", function(){
		$("#confirmMessage").dialog("close");

		callAjax(param, '/inside/system/menu/saveMenu', function(){
			alertMessage(g_msg('msg.completeSave'), function(){			//요청이 완료됐습니다
				//searchList(gridParam);
				//closePopup('popupDialog');
				$(this).dialog("close");
			});

			setTree();
		});
	});


}

function getParam(treeId) {
	var node = $("#" + treeId).jstree(true).get_node('#');
	var param = [];

	setRecursiveNode(treeId, node, param);

	console.log(param);

	return param;
}


function setRecursiveNode(treeId, node, param) {
	console.log(node);
	if('#' !== node.id) {
		var t = {id: node.id, parent: node.parent, menuLevel: node.parents.length - 1};

		param.push(t);
	}

	if(node.children.length > -1) {
		$.each(node.children, function() {
			setRecursiveNode(treeId, $("#" + treeId).jstree(true).get_node(this), param);
		});
	}
}
