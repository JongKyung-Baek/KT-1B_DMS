var selectedRoleCode = "";
var selectedNodeList = {};

function roleassignText(key, fallback) {
  var args = Array.prototype.slice.call(arguments, 2);
  if (window.SdmsI18n && typeof window.SdmsI18n.t === "function") {
    return window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
  }
  return fallback;
}

function localizeRoleassignToolbar() {
  $(".roleassign-toolbar button").each(function () {
    if (String($(this).attr("onclick") || "").indexOf("saveRole") !== -1) {
      $(this).text(roleassignText("feature.common.button.save", "저장"));
    }
  });
}

(function ($, undefined) {
  "use strict";
  $.jstree.plugins.noclose = function () {
    this.close_node = $.noop;
  };
})(jQuery);

$(function () {
  bindEvent();
  settingToolbar(JSON.parse(toolbarInfo || "[]"), $(".roleassign-toolbar"));
  localizeRoleassignToolbar();
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

  $(document).on("keydown", "#menuTree .tree-checkbox", function (event) {
    var isEnter = event.key === "Enter" || event.keyCode === 13;
    var isSpace = event.key === " " || event.key === "Spacebar" || event.keyCode === 32;
    if (!isEnter && !isSpace) {
      return;
    }

    event.preventDefault();
    $(this).trigger("click");
  });
}

function selectUserGrade($button) {
  $(".role-group-list > li").removeClass("current");
  $button.closest("li").addClass("current");
  selectedRoleCode = $button.attr("data-group-cd");
  $("#selectedRoleName").text($.trim($button.text()));
  setMenuList(selectedRoleCode);
}

function setMenuList(groupCd) {
  callAjax(
    { groupCd: groupCd },
    "/general/system/roleassign/getAssignedMenuList",
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
  var $checkboxes = $("#menuTree .tree-checkbox");
  $checkboxes
    .removeClass("tree-checkbox-on")
    .attr({
      role: "checkbox",
      tabindex: "0",
      "aria-checked": "false"
    })
    .each(function () {
      var label = $.trim($(this).closest(".jstree-anchor").text());
      if (label) {
        $(this).attr("aria-label", label);
      }
    });

  var selectedCount = 0;
  $.each(selectedNodeList, function (id, value) {
    var $checkbox = $("#chk_" + id);
    if (value === "Y" && $checkbox.length === 1) {
      $checkbox
        .addClass("tree-checkbox-on")
        .attr("aria-checked", "true");
      selectedCount += 1;
    }
  });
  $("#selectedMenuCount").text(selectedCount);
}

function setUserGradeList() {
  callAjax({}, "/general/system/roleassign/getRoleGroupList", function (response) {
    var roleGroups = response || [];
    var $list = $(".role-group-list").empty();

    $.each(roleGroups, function () {
      var $button = $("<button>", {
        type: "button",
        class: "listName",
        "data-group-cd": this.groupCd
      }).append($("<span>").text(this.groupNm));

      $list.append($("<li>").append($button));
    });

    $("#managerCount").text(roleGroups.length);

    if (roleGroups.length === 0) {
      selectedRoleCode = "";
      selectedNodeList = {};
      $("#selectedRoleName").text(roleassignText(
        "feature.system.roleassign.menu.selectGrade",
        "등급을 선택하세요"
      ));
      $("#selectedMenuCount").text("0");
      $("#menuTree").empty();
      $list.append(
        $("<li>", { class: "role-group-empty" }).text(roleassignText(
          "feature.system.roleassign.grade.empty",
          "등록된 사용자등급이 없습니다."
        ))
      );
      return;
    }

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

  callAjax(param, "/general/system/roleassign/saveAssign", function (response) {
    if (!response.success) {
      alertMessage(response.failReason || g_msg("msg.error"));
      return;
    }

    alertMessage(roleassignText(
      "feature.system.roleassign.message.saved",
      "메뉴권한 배정을 저장했습니다."
    ));
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
