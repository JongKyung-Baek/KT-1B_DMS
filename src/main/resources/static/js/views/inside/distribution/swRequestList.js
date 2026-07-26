var swRequestTreeState = {
	treeId: "swRequestExplorerTree",
	formId: "formSwRequest",
	selectedLabel: swRequestMessage("feature.techList.tree.all", "전체"),
	nodeMap: {},
	childrenMap: {},
	selectedNodes: {},
	searchQuery: "",
	openNodesBeforeSearch: null
};

var swRequestTreeCategoryMeta = {
	TRB000002: { key: "drawing", icon: "tabler-pencil", tone: "violet" },
	TRB000003: { key: "spec", icon: "tabler-file-certificate", tone: "blue" },
	TRB000004: { key: "sow", icon: "tabler-clipboard-text", tone: "teal" },
	TRB000005: { key: "sdrl", icon: "tabler-package", tone: "amber" },
	TRB000006: { key: "programData", icon: "tabler-briefcase", tone: "violet" },
	TRB000007: { key: "sro", icon: "tabler-tool", tone: "rose" },
	TRB000008: { key: "testProcedure", icon: "tabler-flask", tone: "teal" },
	TRB000009: { key: "engineeringMemo", icon: "tabler-note", tone: "blue" },
	TRB000010: { key: "sourceData", icon: "tabler-database", tone: "slate" },
	TRB000011: { key: "etc", icon: "tabler-dots", tone: "slate" },
	TRB000012: { key: "mfgData", icon: "tabler-settings", tone: "amber" }
};

function swRequestMessage(key, fallback) {
	var args = Array.prototype.slice.call(arguments, 2);
	var message = fallback || key;
	if (window.SdmsPageMessages
			&& Object.prototype.hasOwnProperty.call(window.SdmsPageMessages, key)) {
		message = window.SdmsPageMessages[key];
	} else if (window.SdmsI18n && typeof window.SdmsI18n.t === "function") {
		message = window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
	}
	return args.reduce(function(message, value, index) {
		return String(message).replace(new RegExp("\\{" + index + "\\}", "g"), value);
	}, message);
}

function getSwRequestTreeCategoryMeta(node) {
	if (!node) {
		return null;
	}
	return swRequestTreeCategoryMeta[String(node.id || "").toUpperCase()] || null;
}

function getLocalizedSwRequestTreeText(node) {
	if (!node) {
		return "";
	}
	var rawText = $.trim(String(node.text || ""));
	var meta = getSwRequestTreeCategoryMeta(node);
	if (meta) {
		return swRequestMessage(
			"feature.techList.tree.category." + meta.key,
			rawText
		);
	}
	return rawText;
}

function getSwRequestTreeDocumentCount(node) {
	var count = parseInt(node && node.documentCount, 10);
	return isNaN(count) || count < 0 ? 0 : count;
}

function formatSwRequestTreeDocumentCount(count) {
	return swRequestMessage("feature.techList.tree.count", "{0}건", count);
}

function resolveSwRequestTreeNodeVisual(node, depthNo, hasChildren) {
	var meta = getSwRequestTreeCategoryMeta(node);
	if (meta) {
		return meta;
	}
	return {
		icon: hasChildren ? "tabler-folder" : "tabler-file",
		tone: depthNo > 0 ? "slate" : "blue"
	};
}

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
	updateSwRequestTreeSelection(
		swRequestMessage("feature.techList.tree.root", "기술자료"),
		swRequestMessage("feature.techList.tree.root", "기술자료")
	);
	clearSwRequestTreeSelection();
	applySwRequestTreeFilter(true);
}

function renderSwRequestExplorerShellFallback() {
	var $tree = $("#" + swRequestTreeState.treeId);
	$tree.empty().append(
		$("<div>", { "class": "distribution-tree-placeholder" })
			.text(swRequestMessage("feature.techList.tree.empty", "표시할 트리 데이터가 없습니다"))
	);
	updateSwRequestTreeSelection(
		swRequestMessage("feature.techList.tree.all", "전체"),
		swRequestMessage("feature.techList.tree.root", "기술자료")
	);
	updateSwRequestTreeSummary();
	bindSwRequestTreeExplorerControls();
}

function renderSwRequestExplorerCustomTree(treeList) {
	var $tree = $("#" + swRequestTreeState.treeId);
	var childrenMap = {};
	var nodeMap = {};

	$.each(treeList, function() {
		this.displayText = getLocalizedSwRequestTreeText(this);
		this.searchText = [
			this.displayText,
			$.trim(String(this.text || "")),
			$.trim(String(this.id || ""))
		].join(" ").toLocaleLowerCase();
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
			return (a.displayText || a.text || "").localeCompare(b.displayText || b.text || "");
		});
	});

	$tree
		.off(".swRequestTreeCustom")
		.empty()
		.append(buildSwRequestTreeList(childrenMap, "#", false, 0));
	updateSwRequestTreeSummary();
	bindSwRequestTreeExplorerControls();

	$tree.on("click.swRequestTreeCustom", ".drawing-tree-toggle", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var $item = $(this).closest(".drawing-tree-item");
		if ($item.hasClass("is-leaf")) {
			return;
		}
		var willOpen = !$item.hasClass("is-open");
		if (willOpen && $item.hasClass("depth-0") && !swRequestTreeState.searchQuery) {
			$item.siblings(".drawing-tree-item.depth-0").removeClass("is-open")
				.children(".drawing-tree-row")
				.find(".drawing-tree-toggle")
				.attr("aria-expanded", "false");
		}
		$item.toggleClass("is-open", willOpen);
		$(this).attr("aria-expanded", willOpen ? "true" : "false");
	});

	$tree.on("click.swRequestTreeCustom", ".drawing-tree-label", function(e) {
		e.preventDefault();
		var nodeId = $(this).data("nodeId");
		var node = nodeMap[nodeId];
		if (!node) {
			return;
		}
		var $item = $(this).closest(".drawing-tree-item");
		if (!$item.hasClass("is-leaf")) {
			$item.addClass("is-open");
			$item.children(".drawing-tree-row")
				.find(".drawing-tree-toggle")
				.attr("aria-expanded", "true");
		}
		toggleSwRequestTreeSelection(nodeId);
		applySwRequestTreeFilter();
	});
}

function updateSwRequestTreeSummary() {
	var nodeMap = swRequestTreeState.nodeMap || {};
	var totalCount = 0;
	var rootFound = false;

	$.each(nodeMap, function(nodeId, node) {
		var normalizedId = String(nodeId || "").toUpperCase();
		var levelNo = parseInt(node && node.level, 10);
		if (normalizedId === "ROOT" || normalizedId === "0" || levelNo === 0) {
			totalCount = getSwRequestTreeDocumentCount(node);
			rootFound = true;
			return false;
		}
	});

	if (!rootFound) {
		$.each(nodeMap, function(nodeId, node) {
			var parentId = String(node && node.parent || "#").toUpperCase();
			if (parentId === "#" || parentId === "ROOT" || parentId === "0") {
				totalCount += getSwRequestTreeDocumentCount(node);
			}
		});
	}

	var countText = formatSwRequestTreeDocumentCount(totalCount);
	$("#" + swRequestTreeState.treeId + "Total")
		.text(countText)
		.attr("aria-label", countText);
	$("#" + swRequestTreeState.treeId + "AllCount")
		.text(totalCount)
		.attr("aria-label", countText);
}

function bindSwRequestTreeExplorerControls() {
	var treeId = swRequestTreeState.treeId;
	var $search = $("#" + treeId + "Search");
	var $clear = $("#" + treeId + "SearchClear");
	var $all = $("#" + treeId + "All");

	$search
		.off(".swRequestTreeExplorer")
		.on("input.swRequestTreeExplorer", function() {
			applySwRequestTreeSearch($(this).val());
		});

	$clear
		.off(".swRequestTreeExplorer")
		.on("click.swRequestTreeExplorer", function() {
			$search.val("").trigger("input").trigger("focus");
		});

	$all
		.off(".swRequestTreeExplorer")
		.on("click.swRequestTreeExplorer", function() {
			clearSwRequestTreeSelection();
			applySwRequestTreeFilter();
		});

	$(document)
		.off("click.swRequestTreeNavigator", ".tree-toolbar-navigator-clear")
		.on("click.swRequestTreeNavigator", ".tree-toolbar-navigator-clear", function() {
			clearSwRequestTreeSelection();
			applySwRequestTreeFilter();
		});
}

function collectOpenSwRequestTreeNodeIds() {
	var result = [];
	$("#" + swRequestTreeState.treeId)
		.find(".drawing-tree-item.is-open")
		.each(function() {
			var nodeId = $(this).data("nodeId");
			if (nodeId) {
				result.push(String(nodeId));
			}
		});
	return result;
}

function restoreOpenSwRequestTreeNodes(nodeIds) {
	var openMap = {};
	$.each(nodeIds || [], function(index, nodeId) {
		openMap[String(nodeId)] = true;
	});

	$("#" + swRequestTreeState.treeId)
		.find(".drawing-tree-item")
		.each(function() {
			var $item = $(this);
			var isOpen = !!openMap[String($item.data("nodeId") || "")];
			$item.toggleClass("is-open", isOpen);
			$item.children(".drawing-tree-row")
				.find(".drawing-tree-toggle")
				.attr("aria-expanded", isOpen ? "true" : "false");
		});
}

function applySwRequestTreeSearch(value) {
	var query = $.trim(String(value || "")).toLocaleLowerCase();
	var treeId = swRequestTreeState.treeId;
	var $tree = $("#" + treeId);
	var $items = $tree.find(".drawing-tree-item");
	var $clear = $("#" + treeId + "SearchClear");
	var visibleNodeIds = {};
	var matchCount = 0;

	if (query && swRequestTreeState.openNodesBeforeSearch === null) {
		swRequestTreeState.openNodesBeforeSearch = collectOpenSwRequestTreeNodeIds();
	}
	swRequestTreeState.searchQuery = query;
	$clear.toggleClass("is-visible", !!query);

	if (!query) {
		$items.removeClass("is-search-hidden");
		restoreOpenSwRequestTreeNodes(swRequestTreeState.openNodesBeforeSearch || []);
		swRequestTreeState.openNodesBeforeSearch = null;
		$("#" + treeId + "NoResults").prop("hidden", true).text("");
		return;
	}

	$.each(swRequestTreeState.nodeMap || {}, function(nodeId, node) {
		if (shouldHideSwRootNode(node, 0) || isSwBoardNoNode(node)) {
			return;
		}
		if (String(node.searchText || "").indexOf(query) === -1) {
			return;
		}
		matchCount++;
		visibleNodeIds[nodeId] = true;
		$.each(getSwRequestAncestorNodeIds(nodeId), function(index, ancestorId) {
			visibleNodeIds[ancestorId] = true;
		});
		$.each(collectSwRequestDescendantNodeIds(nodeId), function(index, descendantId) {
			visibleNodeIds[descendantId] = true;
		});
	});

	$items.each(function() {
		var $item = $(this);
		var nodeId = String($item.data("nodeId") || "");
		$item.toggleClass("is-search-hidden", !visibleNodeIds[nodeId]);
	});
	$items.each(function() {
		var $item = $(this);
		if ($item.hasClass("is-search-hidden")) {
			return;
		}
		var hasVisibleChild = $item.children(".drawing-tree-children")
			.children(".drawing-tree-item:not(.is-search-hidden)").length > 0;
		if (hasVisibleChild) {
			$item.addClass("is-open")
				.children(".drawing-tree-row")
				.find(".drawing-tree-toggle")
				.attr("aria-expanded", "true");
		}
	});

	var $noResults = $("#" + treeId + "NoResults")
		.prop("hidden", matchCount > 0)
		.empty();
	if (matchCount === 0) {
		$noResults
			.append($("<i>", {
				"class": "icon-base ti tabler-search-off",
				"aria-hidden": "true"
			}))
			.append($("<span>", {
				text: swRequestMessage(
					"feature.techList.tree.noMatches",
					"일치하는 분류가 없습니다"
				)
			}));
	}
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
		var visual = resolveSwRequestTreeNodeVisual(node, depthNo, hasChildren);
		var documentCount = getSwRequestTreeDocumentCount(node);
		var $item = $("<li>", {
			"class": "drawing-tree-item depth-" + depthNo
				+ (hasChildren && opened ? " is-open" : "")
				+ (!hasChildren ? " is-leaf" : "")
				+ (isBoardNoNode ? " is-board-no-node is-board-no-node-sw" : "")
				+ (documentCount === 0 ? " is-empty" : ""),
			"data-node-id": node.id
		});
		var $row = $("<div>", { "class": "drawing-tree-row" });

		var $toggle = $("<button>", {
			type: "button",
			"class": "drawing-tree-toggle",
			"aria-label": swRequestMessage("feature.techList.tree.toggle", "하위 분류 열기/닫기"),
			"aria-expanded": hasChildren && opened ? "true" : "false",
			disabled: !hasChildren
		}).append($("<i>", {
			"class": "icon-base ti " + (hasChildren ? "tabler-chevron-right" : "tabler-point"),
			"aria-hidden": "true"
		}));
		$row.append($toggle);

		var $label = $("<button>", {
			type: "button",
			"class": "drawing-tree-label",
			"data-node-id": node.id,
			"aria-selected": "false",
			title: node.displayText || $.trim(node.text || "")
		});
		$label
			.append($("<span>", {
				"class": "tree-node-icon tree-tone-" + visual.tone,
				"aria-hidden": "true"
			}).append($("<i>", {
				"class": "icon-base ti " + visual.icon
			})))
			.append($("<span>", {
				"class": "tree-node-text",
				text: node.displayText || $.trim(node.text || "")
			}))
			.append($("<span>", {
				"class": "tree-node-count",
				text: documentCount,
				title: formatSwRequestTreeDocumentCount(documentCount),
				"aria-label": formatSwRequestTreeDocumentCount(documentCount)
			}));
		$row.append($label);

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
		label: $.trim(node.displayText || node.text || swRequestMessage("feature.techList.tree.all", "전체")),
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
		$label.addClass("is-selected").attr("aria-selected", "true");
	} else {
		delete swRequestTreeState.selectedNodes[nodeId];
		$label.removeClass("is-selected").attr("aria-selected", "false");
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
	$("#" + swRequestTreeState.treeId).find(".drawing-tree-label.is-selected")
		.removeClass("is-selected")
		.attr("aria-selected", "false");
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
		return swRequestMessage("feature.techList.tree.root", "기술자료");
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
	var hasSelection = filters.length > 0;

	setSwTreeFilterValue("swTreeCd", swTreeCds.join(","));
	setSwTreeFilterValue("distributeTypeCd", "");
	$("#" + swRequestTreeState.treeId + "All")
		.toggleClass("is-active", !hasSelection)
		.attr("aria-pressed", hasSelection ? "false" : "true");
	updateSwRequestTreeSelection(
		labels.length ? labels.join(", ") : swRequestMessage("feature.techList.tree.all", "전체"),
		formatSwRequestPathLabels(pathLabels)
	);

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
		var currentId = String(current.id || "").toUpperCase();
		var currentLevel = parseInt(current.level, 10);
		if (currentId !== "ROOT" && currentId !== "0" && currentLevel !== 0) {
			path.unshift($.trim(current.displayText || current.text || ""));
		}
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
	var selectedPath = pathLabel || label || swRequestMessage("feature.techList.tree.root", "기술자료");
	$("#" + swRequestTreeState.treeId + "Selection")
		.text(label || swRequestMessage("feature.techList.tree.all", "전체"));
	renderToolbarNavigator(selectedPath);
}

function renderToolbarNavigator(pathLabel) {
	var $btnArea = $(".distribution-invoice-layout > .btnArea");
	var $right = $btnArea.find(".right");
	var hasSelection = getSelectedSwRequestTreeFilters().length > 0;
	if (!$right.length) {
		setTimeout(function() { renderToolbarNavigator(pathLabel); }, 200);
		return;
	}

	var $nav = $right.find(".tree-toolbar-navigator");
	if (!$nav.length) {
		$nav = $("<div>", { "class": "tree-toolbar-navigator" })
			.append($("<span>", {
				"class": "tree-toolbar-navigator-icon",
				"aria-hidden": "true"
			}).append($("<i>", {
				"class": "icon-base ti tabler-category"
			})))
			.append($("<span>", {
				"class": "tree-toolbar-navigator-label",
				text: swRequestMessage("feature.techList.tree.selection", "선택 분류")
			}))
			.append($("<span>", { "class": "tree-toolbar-navigator-path" }))
			.append($("<button>", {
				type: "button",
				"class": "tree-toolbar-navigator-clear",
				"aria-label": swRequestMessage("feature.techList.tree.clearSelection", "분류 선택 해제")
			}).append($("<i>", {
				"class": "icon-base ti tabler-x",
				"aria-hidden": "true"
			})));
		$right.append($nav);
	}
	$right.show();

	$nav.toggleClass("has-selection", hasSelection);
	$nav.find(".tree-toolbar-navigator-path")
		.text(pathLabel || swRequestMessage("feature.techList.tree.all", "전체"));
	$nav.find(".tree-toolbar-navigator-clear")
		.prop("hidden", !hasSelection);
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
		alertMessage(swRequestMessage("feature.common.validation.noSelection", "선택된 데이터가 없습니다."));
		return false;
	}
	if(selectedRows.length > 1){
		alertMessage(swRequestMessage("feature.techList.validation.singleSelection", "1개만 선택 가능합니다."));
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
