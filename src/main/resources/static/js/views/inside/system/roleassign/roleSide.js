var selectedRoleCode = "";
var selectedNodeList = {};

(function ($, undefined) {
  "use strict";
  $.jstree.plugins.noclose = function () {
    this.close_node = $.noop;
  };
})(jQuery);

$(function () {
  bindEvent();
  settingToolbar(JSON.parse(toolbarInfo || "[]"), $(".roleassign-toolbar"));
  setUserGradeList();
});

function bindEvent() {
  $(document).on("click", ".role-group-list .listName", function () {
    selectUserGrade($(this));
  });

  $(document).on("click", "#menuTree .tree-checkbox", function (event) {
    event.preventDefault();
    event.stopPropagation();

    var nodeId = this.id.substring(4);
    var tree = $("#menuTree").jstree(true);
    var node = tree.get_node(nodeId);
    var checked = !$(this).hasClass("tree-checkbox-on");

    setNodeAndDescendants(node, checked);
    syncAncestorNodes(node);
    renderCheckedNodes();
  });
}

function selectUserGrade($button) {
  $(".role-group-list > li").removeClass("current");
  $button.closest("li").addClass("current");
  selectedRoleCode = $button.attr("data-group-cd");
  setMenuList(selectedRoleCode);
}

function setMenuList(groupCd) {
  callAjax(
    { authSite: "I", groupCd: groupCd },
    "/inside/system/roleassign/getAssignedMenuList",
    function (response) {
      selectedNodeList = response.selectedValue || {};

      settingTree("menuTree", {
        list: response.menuList || [],
        useCheckbox: false,
        customCheckbox: true,
        dragDrop: false,
        openLevel: 2,
        noClose: false,
        multiple: false,
        onReady: renderCheckedNodes
      });
    }
  );
}

function setNodeAndDescendants(node, checked) {
  var nodeIds = [node.id].concat(node.children_d || []);
  $.each(nodeIds, function (_, nodeId) {
    if (nodeId !== "MENU_000") {
      selectedNodeList[nodeId] = checked ? "Y" : "N";
    }
  });
}

function syncAncestorNodes(node) {
  var tree = $("#menuTree").jstree(true);

  $.each(node.parents || [], function (_, parentId) {
    if (parentId === "#" || parentId === "MENU_000") {
      return;
    }

    var parentNode = tree.get_node(parentId);
    var hasCheckedDescendant = $.grep(
      parentNode.children_d || [],
      function (childId) {
        return selectedNodeList[childId] === "Y";
      }
    ).length > 0;

    selectedNodeList[parentId] = hasCheckedDescendant ? "Y" : "N";
  });
}

function renderCheckedNodes() {
  $("#menuTree .tree-checkbox").removeClass("tree-checkbox-on");
  $.each(selectedNodeList, function (id, value) {
    if (value === "Y") {
      $("#chk_" + id).addClass("tree-checkbox-on");
    }
  });
}

function setUserGradeList() {
  callAjax({}, "/inside/system/roleassign/getRoleGroupList", function (response) {
    var $list = $(".role-group-list").empty();

    $.each(response || [], function () {
      var $button = $("<button>", {
        type: "button",
        class: "listName",
        "data-group-cd": this.groupCd
      }).append($("<span>").text(this.groupNm));

      $list.append($("<li>").append($button));
    });

    $("#managerCount").text((response || []).length);

    var $firstButton = $list.find(".listName").first();
    if ($firstButton.length === 1) {
      selectUserGrade($firstButton);
    }
  });
}

function saveRole() {
  if (!selectedRoleCode) {
    alertMessage(g_msg("msg.notSelectedRoleGroup"));
    return;
  }

  var param = getParam();
  if (param.list.length === 0) {
    alertMessage(g_msg("msg.noSelectData"));
    return;
  }

  callAjax(param, "/inside/system/roleassign/saveAssign", function (response) {
    if (!response.success) {
      alertMessage(response.failReason || g_msg("msg.error"));
      return;
    }

    alertMessage(g_msg("msg.completeSave"));
    setMenuList(selectedRoleCode);
  });
}

function getParam() {
  var tree = $("#menuTree").jstree(true);
  if (!tree) {
    return { list: [], groupCd: selectedRoleCode };
  }

  var root = tree.get_node("#");
  var checkedNode = [];

  $.each(root.children_d || [], function (_, nodeId) {
    var node = tree.get_node(nodeId);
    if (!node.original.roleCd) {
      return;
    }

    checkedNode.push({
      menuCd: node.id,
      roleCd: node.original.roleCd,
      selectedYn: selectedNodeList[node.id] === "Y" ? "Y" : "N"
    });
  });

  return {
    list: checkedNode,
    groupCd: selectedRoleCode
  };
}
