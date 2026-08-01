var ROOT_MENU_ID = "MENU_000";
var MENU_TREE_ID = "menuTree";

$(function () {
  settingToolbar(JSON.parse(toolbarInfo || "[]"), $("#menuBtnArea"));
  setTree();
});

function setTree() {
  callAjax({}, "/general/system/menu/getTreeList", function (response) {
    menuTreeList = response || [];
    settingTree(MENU_TREE_ID, {
      list: menuTreeList,
      useCheckbox: false,
      dragDrop: true,
      openLevel: 2,
      customCheckbox: false
    });
  });
}

function saveMenu() {
  var param = { list: getParam() };

  callAjax(param, "/general/system/menu/saveMenuSort", function (response) {
    if (!response.success) {
      alertMessage(response.failReason || g_msg("msg.error"));
      return;
    }

    alertMessage(g_msg("msg.completeSave"), function () {
      $(this).dialog("close");
    });
    setTree();
  });
}

function addMenu() {
  var selectedNode = $("#" + MENU_TREE_ID).jstree("get_selected");

  if (selectedNode.length === 0) {
    alertMessage(g_msg("msg.plzSelectParentMenu"), function () {
      $(this).dialog("close");
    });
    return;
  }

  openDialogPopup(
    "/general/system/menu/menuAddPopup",
    { menuCd: selectedNode[0] },
    "popupDialog",
    "m",
    270,
    true,
    "popup-common popup-menu"
  );
}

function modMenu() {
  var selectedNode = $("#" + MENU_TREE_ID).jstree("get_selected");

  if (selectedNode.length === 0 || selectedNode[0] === ROOT_MENU_ID) {
    alertMessage(g_msg("msg.plzSelectMenu"), function () {
      $(this).dialog("close");
    });
    return;
  }

  openDialogPopup(
    "/general/system/menu/menuModPopup",
    { menuCd: selectedNode[0] },
    "popupDialog",
    "m",
    270,
    true,
    "popup-common popup-menu"
  );
}

function delMenu() {
  var selectedNode = $("#" + MENU_TREE_ID).jstree("get_selected");

  if (selectedNode.length === 0 || selectedNode[0] === ROOT_MENU_ID) {
    alertMessage(g_msg("msg.plzSelectMenu"), function () {
      $(this).dialog("close");
    });
    return;
  }

  var node = $("#" + MENU_TREE_ID).jstree(true).get_node(selectedNode[0]);
  var param = {
    menuCd: selectedNode[0],
    children: node.children_d,
    saveFlag: "D",
    menuType: node.original.type
  };

  confirmMessage(g_msg("msg.confirmDeleteMenuTree"), function () {
    $("#confirmMessage").dialog("close");

    callAjax(param, "/general/system/menu/saveMenu", function (response) {
      if (!response.success) {
        alertMessage(response.failReason || g_msg("msg.error"));
        return;
      }

      alertMessage(g_msg("msg.completeSave"), function () {
        $(this).dialog("close");
      });
      setTree();
    });
  });
}

function getParam() {
  var root = $("#" + MENU_TREE_ID).jstree(true).get_node("#");
  var param = [];

  setRecursiveNode(root, param);
  return param;
}

function setRecursiveNode(node, param) {
  if ("#" !== node.id) {
    param.push({
      id: node.id,
      parent: node.parent,
      menuLevel: node.parents.length - 1
    });
  }

  $.each(node.children, function () {
    setRecursiveNode(
      $("#" + MENU_TREE_ID).jstree(true).get_node(this),
      param
    );
  });
}
