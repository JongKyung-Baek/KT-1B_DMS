var ROOT_MENU_ID = "MENU_000";
var MENU_TREE_ID = "menuTree";
var selectedMenuId = null;
var menuTreeSearchTimer = null;

function menuPermissionText(key, fallback) {
  if (window.SdmsI18n && typeof window.SdmsI18n.t === "function") {
    return window.SdmsI18n.t(key, fallback);
  }
  return fallback;
}

function localizeMenuToolbar() {
  var labels = {
    addMenu: menuPermissionText("feature.common.button.add", "추가"),
    modMenu: menuPermissionText("feature.common.button.edit", "수정"),
    delMenu: menuPermissionText("feature.common.button.delete", "삭제"),
    saveMenu: menuPermissionText("feature.system.menu.action.saveOrder", "메뉴순서저장")
  };

  $("#menuBtnArea button").each(function () {
    var action = String($(this).attr("onclick") || "").split("(")[0];
    if (labels[action]) {
      $(this).text(labels[action]);
    }
  });
}

$(function () {
  settingToolbar(JSON.parse(toolbarInfo || "[]"), $("#menuBtnArea"));
  localizeMenuToolbar();
  bindMenuTreeSearch();
  setTree();
});

function setTree() {
  $("#" + MENU_TREE_ID).attr("aria-busy", "true");

  callAjax({}, "/general/system/menu/getTreeList", function (response) {
    menuTreeList = response || [];
    updateMenuTreeSummary();

    settingTree(MENU_TREE_ID, {
      list: menuTreeList,
      useCheckbox: false,
      dragDrop: true,
      openLevel: 2,
      customCheckbox: false,
      onReady: function (data) {
        var tree = data.instance;

        bindMenuTreeSelection();
        $("#" + MENU_TREE_ID).attr("aria-busy", "false");

        if (selectedMenuId && tree.get_node(selectedMenuId)) {
          tree.select_node(selectedMenuId);
        } else {
          selectedMenuId = null;
          resetMenuSelection();
        }

        searchMenuTree();
      }
    });
  });
}

function bindMenuTreeSearch() {
  $("#menuTreeSearchForm")
    .off("submit.menuPermission")
    .on("submit.menuPermission", function (event) {
      event.preventDefault();
      searchMenuTree();
    });

  $("#menuTreeSearch")
    .off("input.menuPermission")
    .on("input.menuPermission", function () {
      window.clearTimeout(menuTreeSearchTimer);
      menuTreeSearchTimer = window.setTimeout(searchMenuTree, 140);
    });
}

function searchMenuTree() {
  var tree = $("#" + MENU_TREE_ID).jstree(true);
  var keyword = $.trim($("#menuTreeSearch").val() || "");

  if (!tree) {
    return;
  }

  if (keyword) {
    tree.search(keyword);
  } else {
    tree.clear_search();
  }
}

function bindMenuTreeSelection() {
  $("#" + MENU_TREE_ID)
    .off(".menuPermission")
    .on("select_node.jstree.menuPermission", function (event, data) {
      selectedMenuId = data.node.id;
      renderMenuSelection(data.node, data.instance);
    })
    .on("deselect_all.jstree.menuPermission", function () {
      selectedMenuId = null;
      resetMenuSelection();
    });
}

function updateMenuTreeSummary() {
  var menus = $.grep(menuTreeList, function (menu) {
    return menu && menu.id !== ROOT_MENU_ID;
  });
  var activeMenus = $.grep(menus, function (menu) {
    return String(menu.useYn || "").toUpperCase() === "Y";
  });

  $("#menuTotalCount").text(menus.length);
  $("#menuActiveCount").text(activeMenus.length);
}

function renderMenuSelection(node, tree) {
  var menu = node.original || {};
  var parent = tree.get_node(node.parent);
  var useYn = String(menu.useYn || "-").toUpperCase();
  var type = String(menu.type || "-").toUpperCase();

  $("#selectedMenuName").text(node.text || "-");
  $("#selectedMenuCode").text(node.id || "-");
  $("#selectedMenuParent").text(
    parent && parent.id !== "#" ? parent.text : "-"
  );
  $("#selectedMenuLevel").text(menu.level || "-");
  $("#selectedMenuRole").text(menu.roleCd || "-");
  $("#selectedMenuUseYn").text(useYn);
  $("#selectedMenuType").text("TYPE " + type);
  $("#selectedMenuState")
    .text(useYn)
    .toggleClass("menu-permission-chip--active", useYn === "Y")
    .toggleClass("menu-permission-chip--inactive", useYn !== "Y");

  $("#menuSelectionEmpty").prop("hidden", true);
  $("#menuSelectionDetail").prop("hidden", false);
}

function resetMenuSelection() {
  $("#menuSelectionDetail").prop("hidden", true);
  $("#menuSelectionEmpty").prop("hidden", false);
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
