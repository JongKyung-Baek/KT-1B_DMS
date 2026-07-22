var swRequestTreeState = {
	treeId: "swRequestExplorerTree",
	formId: "formSwRequest",
	selectedLabel: "전체",
	nodeMap: {},
	childrenMap: {},
	selectedNodes: {}
};

$(function() {
	initSwRequestExplorerTree();
});

function initSwRequestExplorerTree() {
	if (!$("#" + swRequestTreeState.treeId).length) {
		return;
	}

	waitForSwRequestForm(0);
}

function waitForSwRequestForm(tryCount) {
	if ($("#" + swRequestTreeState.formId).length) {
		loadSwRequestExplorerTree();
		return;
	}

	if (tryCount > 20) {
		return;
	}

	setTimeout(function() {
		waitForSwRequestForm(tryCount + 1);
	}, 150);
}

function loadSwRequestExplorerTree() {
	callAjax({}, "/inside/distribution/swRequest/selectTree", function(response) {
		renderSwRequestExplorerTree(response || []);
	});
}

function renderSwRequestExplorerTree(treeList) {
	if (!treeList.length) {
		renderSwRequestExplorerShellFallback();
		return;
	}

	renderSwRequestExplorerCustomTree(treeList);
	updateSwRequestTreeSelection("기술자료", "기술자료");
	clearSwRequestTreeSelection();
	applySwRequestTreeFilter(true);
}

function renderSwRequestExplorerShellFallback() {
	var $tree = $("#" + swRequestTreeState.treeId);
	$tree.empty().append(
		$("<div>", { "class": "distribution-tree-placeholder" }).text("표시할 트리 데이터가 없습니다")
	);
	updateSwRequestTreeSelection("전체", "기술자료");
}

function renderSwRequestExplorerCustomTree(treeList) {
	var $tree = $("#" + swRequestTreeState.treeId);
	var childrenMap = {};
	var nodeMap = {};

	$.each(treeList, function() {
		nodeMap[this.id] = this;
		var parentId = this.parent || "#";
		if (!childrenMap[parentId]) {
			childrenMap[parentId] = [];
		}
		childrenMap[parentId].push(this);
	});
	swRequestTreeState.nodeMap = nodeMap;
	swRequestTreeState.childrenMap = childrenMap;

	$.each(childrenMap, function(parentId, childList) {
		childList.sort(function(a, b) {
			var sortA = parseInt(a.sort || 0, 10);
			var sortB = parseInt(b.sort || 0, 10);
			if (sortA !== sortB) {
				return sortA - sortB;
			}
			return (a.text || "").localeCompare(b.text || "");
		});
	});

	$tree
		.off(".swRequestTreeCustom")
		.empty()
		.append(buildSwRequestTreeList(childrenMap, "#", false, 0));

	$tree.on("click.swRequestTreeCustom", ".drawing-tree-toggle", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var $item = $(this).closest(".drawing-tree-item");
		if ($item.hasClass("is-leaf")) {
			return;
		}
		$item.toggleClass("is-open");
	});

	$tree.on("click.swRequestTreeCustom", ".drawing-tree-label", function(e) {
		e.preventDefault();
		var nodeId = $(this).data("nodeId");
		var node = nodeMap[nodeId];
		if (!node) {
			return;
		}
		toggleSwRequestTreeSelection(nodeId);
		applySwRequestTreeFilter();
	});
}

function buildSwRequestTreeList(childrenMap, parentId, opened, depth) {
	var childList = childrenMap[parentId] || [];
	var depthNo = parseInt(depth, 10);
	if (isNaN(depthNo) || depthNo < 0) {
		depthNo = 0;
	}
	var $list = $("<ul>", {
		"class": parentId === "#" ? "drawing-tree-root" : "drawing-tree-children"
	});

	$.each(childList, function() {
		var node = this;
		var hasChildren = !!(childrenMap[node.id] && childrenMap[node.id].length);
		// ROOT 노드는 숨기고 하위 depth를 최상단에 노출
		if (shouldHideSwRootNode(node, depthNo)) {
			if (hasChildren) {
				var $rootChildren = buildSwRequestTreeList(childrenMap, node.id, opened, depthNo);
				$list.append($rootChildren.children());
			}
			return;
		}
		var isBoardNoNode = isSwBoardNoNode(node);
		// 맨 하위 board 노드는 트리에서 노출하지 않음
		if (isBoardNoNode && !hasChildren) {
			return;
		}
		var $item = $("<li>", {
			"class": "drawing-tree-item depth-" + depthNo
				+ (hasChildren && opened ? " is-open" : "")
				+ (!hasChildren ? " is-leaf" : "")
				+ (isBoardNoNode ? " is-board-no-node is-board-no-node-sw" : "")
		});
		var $row = $("<div>", { "class": "drawing-tree-row" });

		$row.append($("<button>", {
			type: "button",
			"class": "drawing-tree-toggle",
			"aria-label": "toggle"
		}));

		$row.append($("<a>", {
			href: "#",
			"class": "drawing-tree-label",
			"data-node-id": node.id,
			text: $.trim(node.text || "")
		}));

		$item.append($row);

		if (hasChildren) {
			$item.append(buildSwRequestTreeList(childrenMap, node.id, false, depthNo + 1));
		}

		$list.append($item);
	});

	return $list;
}

function shouldHideSwRootNode(node, depthNo) {
	if (!node || depthNo !== 0) {
		return false;
	}
	var nodeId = $.trim(String(node.id || "")).toUpperCase();
	var levelNo = parseInt(node.level, 10);
	return nodeId === "ROOT" || nodeId === "0" || levelNo === 0;
}

function isSwBoardNoNode(node) {
	if (!node || node.text === undefined || node.text === null) {
		return false;
	}
	var text = $.trim(String(node.text)).toLowerCase().replace(/[^a-z0-9]/g, "");
	return text.indexOf("boardno") === 0;
}

function resolveSwTreeCdForFilter(node, nodeMap) {
	if (!node) {
		return "";
	}
	if (!isSwBoardNoNode(node)) {
		return node.swTreeCd || node.id || "";
	}
	var parentId = node.parent;
	if (parentId && nodeMap[parentId]) {
		return nodeMap[parentId].swTreeCd || nodeMap[parentId].id || "";
	}
	return node.swTreeCd || node.id || "";
}

function buildSwRequestTreeFilterInfo(nodeId) {
	var node = swRequestTreeState.nodeMap[nodeId];
	if (!node) {
		return null;
	}
	return {
		filterType: node.filterType || "",
		swTreeCd: resolveSwTreeCdForFilter(node, swRequestTreeState.nodeMap) || node.swTreeCd || node.id || "",
		distributeTypeCd: node.distributeTypeCd || "",
		label: $.trim(node.text || "전체"),
		pathLabel: buildSwRequestTreePathLabel(node, swRequestTreeState.nodeMap)
	};
}

function collectSwRequestDescendantNodeIds(nodeId) {
	var result = [];
	var childrenMap = swRequestTreeState.childrenMap || {};
	var stack = (childrenMap[nodeId] || []).slice();
	while (stack.length) {
		var child = stack.shift();
		if (!child || !child.id) {
			continue;
		}
		if (!isSwBoardNoNode(child)) {
			result.push(child.id);
		}
		var childList = childrenMap[child.id] || [];
		for (var i = 0; i < childList.length; i++) {
			stack.push(childList[i]);
		}
	}
	return result;
}

function getSwRequestAncestorNodeIds(nodeId) {
	var result = [];
	var nodeMap = swRequestTreeState.nodeMap || {};
	var current = nodeMap[nodeId];
	var guard = 0;
	while (current && current.parent && current.parent !== "#" && guard < 32) {
		result.push(current.parent);
		current = nodeMap[current.parent];
		guard += 1;
	}
	return result;
}

function setSwRequestNodeSelected(nodeId, selected) {
	var filterInfo = buildSwRequestTreeFilterInfo(nodeId);
	if (!filterInfo || !$.trim(filterInfo.swTreeCd || "")) {
		return;
	}

	var $label = $("#" + swRequestTreeState.treeId).find(".drawing-tree-label[data-node-id='" + nodeId + "']");
	if (selected) {
		swRequestTreeState.selectedNodes[nodeId] = filterInfo;
		$label.addClass("is-selected");
	} else {
		delete swRequestTreeState.selectedNodes[nodeId];
		$label.removeClass("is-selected");
	}
}

function toggleSwRequestTreeSelection(nodeId) {
	var isSelected = !!swRequestTreeState.selectedNodes[nodeId];
	var descendantIds = collectSwRequestDescendantNodeIds(nodeId);
	var affectedIds = [nodeId].concat(descendantIds);

	if (isSelected) {
		$.each(getSwRequestAncestorNodeIds(nodeId), function(index, ancestorId) {
			setSwRequestNodeSelected(ancestorId, false);
		});
		$.each(affectedIds, function(index, affectedId) {
			setSwRequestNodeSelected(affectedId, false);
		});
		return;
	}

	$.each(getSwRequestAncestorNodeIds(nodeId), function(index, ancestorId) {
		setSwRequestNodeSelected(ancestorId, false);
	});
	$.each(affectedIds, function(index, affectedId) {
		setSwRequestNodeSelected(affectedId, true);
	});
}

function clearSwRequestTreeSelection() {
	swRequestTreeState.selectedNodes = {};
	$("#" + swRequestTreeState.treeId).find(".drawing-tree-label.is-selected").removeClass("is-selected");
}

function getSelectedSwRequestTreeFilters() {
	var filters = [];
	$.each(swRequestTreeState.selectedNodes, function(nodeId, filterInfo) {
		if (filterInfo && $.trim(filterInfo.swTreeCd || "")) {
			filters.push(filterInfo);
		}
	});
	return filters;
}

function getSelectedSwRequestDisplayFilters() {
	var filters = [];
	$.each(swRequestTreeState.selectedNodes, function(nodeId, filterInfo) {
		if (!filterInfo || !$.trim(filterInfo.swTreeCd || "")) {
			return;
		}
		var hasSelectedAncestor = false;
		$.each(getSwRequestAncestorNodeIds(nodeId), function(index, ancestorId) {
			if (swRequestTreeState.selectedNodes[ancestorId]) {
				hasSelectedAncestor = true;
				return false;
			}
		});
		if (!hasSelectedAncestor) {
			filters.push(filterInfo);
		}
	});
	return filters;
}

function formatSwRequestPathLabels(pathLabels) {
	var paths = [];
	var seen = {};
	$.each(pathLabels || [], function(index, pathLabel) {
		var text = $.trim(pathLabel || "");
		if (text && !seen[text]) {
			seen[text] = true;
			paths.push(text);
		}
	});
	if (!paths.length) {
		return "기술자료";
	}
	if (paths.length === 1) {
		return paths[0];
	}

	var splitPaths = $.map(paths, function(path) {
		return [$.map(path.split(">"), function(part) {
			return $.trim(part || "");
		}).filter(function(part) {
			return part.length > 0;
		})];
	});
	var common = splitPaths[0].slice();
	$.each(splitPaths.slice(1), function(index, parts) {
		var nextCommon = [];
		for (var i = 0; i < common.length && i < parts.length; i++) {
			if (common[i] !== parts[i]) {
				break;
			}
			nextCommon.push(common[i]);
		}
		common = nextCommon;
	});

	if (!common.length) {
		return paths.join(", ");
	}

	var suffixes = $.map(splitPaths, function(parts) {
		var suffix = parts.slice(common.length).join(" > ");
		return suffix || parts[parts.length - 1] || "";
	}).filter(function(suffix) {
		return suffix.length > 0;
	});

	var uniqueSuffixes = [];
	var suffixSeen = {};
	$.each(suffixes, function(index, suffix) {
		if (!suffixSeen[suffix]) {
			suffixSeen[suffix] = true;
			uniqueSuffixes.push(suffix);
		}
	});

	if (!uniqueSuffixes.length) {
		return common.join(" > ");
	}
	return common.join(" > ") + " > " + uniqueSuffixes.join(", ");
}

function applySwRequestTreeFilter(immediate) {
	var filters = getSelectedSwRequestTreeFilters();
	var displayFilters = getSelectedSwRequestDisplayFilters();
	var swTreeCds = $.map(filters, function(filterInfo) {
		return filterInfo.swTreeCd;
	});
	var labels = $.map(displayFilters, function(filterInfo) {
		return filterInfo.label;
	});
	var pathLabels = $.map(displayFilters, function(filterInfo) {
		return filterInfo.pathLabel || filterInfo.label;
	});

	setSwTreeFilterValue("swTreeCd", swTreeCds.join(","));
	setSwTreeFilterValue("distributeTypeCd", "");
	updateSwRequestTreeSelection(labels.length ? labels.join(", ") : "전체", formatSwRequestPathLabels(pathLabels));

	/* gridParam = setGridParam();
	searchList(gridParam); */
	if (immediate === true) {
	runSwRequestSearch();
	} else {
	scheduleSwRequestSearch();
	}
}

function setSwTreeFilterValue(fieldName, value) {
	var $form = $("#" + swRequestTreeState.formId);
	if (!$form.length) {
		return;
	}

	var $field = $form.find("[name='" + fieldName + "_select']");
	if (!$field.length) {
		$field = $form.find("[name='" + fieldName + "']");
	}

	if ($field.length) {
		$field.val(value);
		if ($field.hasClass("select2-hidden-accessible")) {
			$field.trigger("change.select2");
		} else {
			$field.trigger("change");
		}
		return;
	}

	var hiddenId = fieldName + "_treeHidden";
	var $hidden = $("#" + hiddenId);
	if (!$hidden.length) {
		$hidden = $("<input>", {
			type: "hidden",
			id: hiddenId,
			name: fieldName
		}).appendTo($form);
	}

	$hidden.val(value);
}

function buildSwRequestTreePathLabel(node, nodeMap) {
	var path = [];
	var current = node;
	var guard = 0;

	while (current && guard < 32) {
		path.unshift($.trim(current.text || ""));
		var parentId = current.parent || "#";
		if (parentId === "#" || !nodeMap[parentId]) {
			break;
		}
		current = nodeMap[parentId];
		guard++;
	}

	return path.join(" > ");
}

function updateSwRequestTreeSelection(label, pathLabel) {
	$("#" + swRequestTreeState.treeId + "Selection").text(label || "전체");
	renderToolbarNavigator(pathLabel || label || "기술자료");
}

function renderToolbarNavigator(pathLabel) {
	var $btnArea = $(".distribution-invoice-layout > .btnArea");
	var $right = $btnArea.find(".right");
	if (!$right.length) {
		setTimeout(function() { renderToolbarNavigator(pathLabel); }, 200);
		return;
	}

	var $nav = $right.find(".tree-toolbar-navigator");
	if (!$nav.length) {
		$nav = $("<div>", { "class": "tree-toolbar-navigator" })
			.append($("<span>", { "class": "tree-toolbar-navigator-label", text: "" }))
			.append($("<span>", { "class": "tree-toolbar-navigator-path" }));
		$right.append($nav);
	}
	$right.show();

	$nav.find(".tree-toolbar-navigator-path").text(pathLabel || "전체");
}

function requestDistribute(){
	requestInsideUser('DISTRIBUTION', 'SW', 'gridSwRequestList');
}

function requestPrint(){
	requestInsideUser('PRINT', 'SW', 'gridSwRequestList');
}

function updateFile(){
	var gridId = 'gridSwRequestList';
	var selectedRows = $("#" + gridId).getGridParam('selarrrow');
	if(selectedRows.length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}
	if(selectedRows.length > 1){
		alertMessage("1개만 선택 가능합니다.");
		return false;
	}
	var rowId = selectedRows[0];
	var grid = $("#" + gridId);
	var rawData = grid.jqGrid('getLocalRow', rowId) || {};
	var rowData = grid.jqGrid('getRowData', rowId) || {};
	var data = $.extend({}, rowData, rawData);
	var stripHtml = function(v){ return String(v || '').replace(/<[^>]*>/g, '').trim(); };
	var swNo = stripHtml(data.swNo);
	var dataName = stripHtml(data.objectNm || data.fileNm || data.swNm || data.dataName);
	var popupHeight = Math.min($(window).height() - 100, 600);
	openDialogPopup(
		"/inside/distribution/swRequest/swRegisterPopup",
		{
			treeCd: $("#formSwRequest [name='swTreeCd']").val() || $("#formSwRequest [name='swTreeCd_treeHidden']").val() || "",
			isNewRevision: "true",
			objectId: data.objectId || "",
			revNo: data.revNo || "00",
			swNo: swNo,
			dataName: dataName,
			businessTypeCd: data.businessTypeCd || "",
			swTypeCd: data.swTypeCd || data.approver || "",
			reviewerUser: data.reviewerUser || "",
			distributeTypeCd: data.distributeTypeCd || "",
			ccbIssueDt: data.ccbIssueDt || ""
		},
		"popupDialog",
		'l',
		popupHeight,
		true,
		'popup-common popup-sw-register'
	);
}

function revisionUpdate(){
	updateFile();
}

function searchAll(){
	openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'SW'}, "searchAllPopup", 's', 600);
}

var swRequestSearchTimer = null;
var swRequestSearchDelay = 150;

function beginSwRequestGridRefresh() {
	var $gridBox = $("#gbox_" + gridId);
	$("body").addClass("sw-request-silent-refresh");

	if (!$gridBox.length) {
		return;
	}

	var currentHeight = $gridBox.outerHeight();
	if (currentHeight > 0) {
		$gridBox.css("min-height", currentHeight + "px");
	}

	$gridBox.addClass("is-sw-refreshing");
}

function finishSwRequestGridRefresh() {
	var $gridBox = $("#gbox_" + gridId);
	if (!$gridBox.length) {
		$("body").removeClass("sw-request-silent-refresh");
		return;
	}

	setTimeout(function() {
		$gridBox.removeClass("is-sw-refreshing");
		$gridBox.css("min-height", "");
		$("body").removeClass("sw-request-silent-refresh");
	}, 80);
}

function reloadSwRequestGrid() {
	beginSwRequestGridRefresh();

	gridParam = setGridParam();
	searchList(gridParam);
}

function runSwRequestSearch() {
	if (swRequestSearchTimer) {
		clearTimeout(swRequestSearchTimer);
		swRequestSearchTimer = null;
	}

	/* gridParam = setGridParam();
	searchList(gridParam); */
	reloadSwRequestGrid();
}

function scheduleSwRequestSearch() {
	if (swRequestSearchTimer) {
		clearTimeout(swRequestSearchTimer);
	}

	swRequestSearchTimer = setTimeout(function() {
		swRequestSearchTimer = null;
		/* gridParam = setGridParam();
		searchList(gridParam); */
		reloadSwRequestGrid();
	}, swRequestSearchDelay);
}
