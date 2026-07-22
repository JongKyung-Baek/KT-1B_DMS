var selectedNodeList = [];

$(function () {
  settingToolbar(JSON.parse(insideToolbarInfo), $("#insideBtnArea"));
  settingToolbar(JSON.parse(outsideToolbarInfo), $("#outsideBtnArea"));
  setTree();
  bindEvent();
});

function bindEvent() {
  $('button[data-bs-toggle="tab"]').on("shown.bs.tab", function (event) {
    var targetSelector = $(event.target).data("bsTarget");
    var treeId = targetSelector === "#inside-menu-pane" ? "insideMenuTree" : "outsideMenuTree";
    refreshTree(treeId);
  });
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
  callAjax({ authSite: 'I' }, '/inside/system/menu/getTreeList', function (response) {
    insideTreeList = response;

    var insideTreeParam = {
      list: insideTreeList,
      useCheckbox: false,
      dragDrop: true,
      openLevel: 2,
      customCheckbox: false
    };

    settingTree("insideMenuTree", insideTreeParam);
    refreshTree("insideMenuTree");
  });

  callAjax({ authSite: 'E' }, '/inside/system/menu/getTreeList', function (response) {
    outsideTreeList = response;

    var outsideTreeParam = {
      list: outsideTreeList,
      useCheckbox: false,
      dragDrop: true,
      openLevel: 2,
      customCheckbox: false
    };

    settingTree("outsideMenuTree", outsideTreeParam);
  });
}

function refreshTree(treeId) {
  var tree = $("#" + treeId).jstree(true);
  if (tree) {
    tree.redraw(true);
  }
}

function getTreeId(auth) {
  if ('I' === auth) return "insideMenuTree";
  return "outsideMenuTree";
}

function saveMenu(auth) {
  var treeId = getTreeId(auth);
  var param = { list: getParam(treeId) };

  callAjax(param, '/inside/system/menu/saveMenuSort', function () {
    alertMessage(g_msg('msg.completeSave'), function () {
      $(this).dialog("close");
    });

    setTree();
  });
}

function addMenu(auth) {
  var treeId = getTreeId(auth);
  var selectedNode = $("#" + treeId).jstree("get_selected");

  if (selectedNode.length === 0) {
    alertMessage(g_msg('msg.plzSelectParentMenu'), function () {
      $(this).dialog("close");
    });
    return;
  }

  openDialogPopup("/inside/system/menu/menuAddPopup", { menuCd: selectedNode[0], authSite: auth }, "popupDialog", 'm', 270, true, 'popup-common popup-menu');
}

function modMenu(auth) {
  var treeId = getTreeId(auth);
  var selectedNode = $("#" + treeId).jstree("get_selected");

  if (selectedNode.length === 0) {
    alertMessage(g_msg('msg.plzSelectMenu'), function () {
      $(this).dialog("close");
    });
    return;
  }

  openDialogPopup("/inside/system/menu/menuModPopup", { menuCd: selectedNode[0], authSite: auth }, "popupDialog", 'm', 270, true, 'popup-common popup-menu');
}

function delMenu(auth) {
  var treeId = getTreeId(auth);
  var selectedNode = $("#" + treeId).jstree("get_selected");

  if (selectedNode.length === 0) {
    alertMessage(g_msg('msg.plzSelectMenu'), function () {
      $(this).dialog("close");
    });
    return;
  }

  var node = $("#" + treeId).jstree(true).get_node(selectedNode[0]);
  var menuType = node.original.type;

  var param = {
    menuCd: selectedNode[0],
    children: node.children_d,
    saveFlag: 'D',
    menuType: menuType
  };

  confirmMessage("선택하신 메뉴와 그 하위메뉴를 모두 삭제하시겠습니까?", function () {
    $("#confirmMessage").dialog("close");

    callAjax(param, '/inside/system/menu/saveMenu', function () {
      alertMessage(g_msg('msg.completeSave'), function () {
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
  return param;
}

function setRecursiveNode(treeId, node, param) {
  if ('#' !== node.id) {
    param.push({ id: node.id, parent: node.parent, menuLevel: node.parents.length - 1 });
  }

  if (node.children.length > -1) {
    $.each(node.children, function () {
      setRecursiveNode(treeId, $("#" + treeId).jstree(true).get_node(this), param);
    });
  }
}
