var drawingRequestTreeState = {
	treeId: "drawingRequestExplorerTree",
	formId: "formDrawingRequest",
	selectedLabel: "전체",
	nodeMap: {}
};

$(function() {
	initDrawingRequestFilters();
	initDrawingRequestExplorerTree();
});

function initDrawingRequestFilters() {
	if (document.getElementById("companyCd_select")) {
		document.getElementById("companyCd_select").disabled = "disabled";
	}
	if (document.getElementById("purchaserUid_select")) {
		document.getElementById("purchaserUid_select").disabled = "disabled";
	}

	$("#distributeTypeCd_select").change(function() {
		if ("hdQuickChangeAction" === $(this).val()) {
			if (document.getElementById("companyCd_select")) {
				document.getElementById("companyCd_select").disabled = "";
			}
			if (document.getElementById("purchaserUid_select")) {
				document.getElementById("purchaserUid_select").disabled = "";
			}
		} else {
			if (document.getElementById("companyCd_select")) {
				document.getElementById("companyCd_select").disabled = "disabled";
			}
			if (document.getElementById("purchaserUid_select")) {
				document.getElementById("purchaserUid_select").disabled = "disabled";
			}
		}
	});
}

function initDrawingRequestExplorerTree() {
	if (!$("#" + drawingRequestTreeState.treeId).length) {
		return;
	}

	waitForDrawingRequestForm(0);
}

function waitForDrawingRequestForm(tryCount) {
	if ($("#" + drawingRequestTreeState.formId).length) {
		loadDrawingRequestExplorerTree();
		return;
	}

	if (tryCount > 20) {
		return;
	}

	setTimeout(function() {
		waitForDrawingRequestForm(tryCount + 1);
	}, 150);
}



function loadDrawingRequestExplorerTree() {
	callAjax({}, "/inside/distribution/drawingRequest/selectTree", function(response) {
		renderDrawingRequestExplorerTree(response || []);
	});
}

function renderDrawingRequestExplorerTree(treeList) {
	if (!treeList.length) {
		renderDrawingRequestExplorerShellFallback();
		return;
	}


	renderDrawingRequestExplorerCustomTree(treeList);
	updateDrawingRequestTreeSelection("Documents", "Documents");
	applyDrawingRequestTreeFilter({
		treeCd: "",
		distributeTypeCd: "",
		label: "Documents",
		pathLabel: "Documents"
	});
}

function renderDrawingRequestExplorerShellFallback() {
	var $tree = $("#" + drawingRequestTreeState.treeId);
	$tree.empty().append(
		$("<div>", { "class": "distribution-tree-placeholder" }).text("표시할 트리 데이터가 없습니다")
	);
	updateDrawingRequestTreeSelection("전체", "Documents");
}

function bindDrawingRequestTreeEvent() {
	$("#" + drawingRequestTreeState.treeId).off("changed.drawingRequestTree");
	$("#" + drawingRequestTreeState.treeId).on("changed.drawingRequestTree", function(e, data) {
		if (!data || !data.node) {
			return;
		}

		var node = data.node.original || {};
		applyDrawingRequestTreeFilter(buildDrawingRequestTreeFilterInfo(node));
	});
}

function renderDrawingRequestExplorerCustomTree(treeList) {
	var $tree = $("#" + drawingRequestTreeState.treeId);
	var childrenMap = {};
	var nodeMap = {};
	var normalizedNodes = [];

	$.each(treeList, function() {
		var node = $.extend({}, this);
		node.id = normalizeDrawingTreeKey(node.id);
		node.parent = normalizeDrawingTreeKey(node.parent || "#");
		node.levelNo = parseInt(node.level, 10);
		if (isNaN(node.levelNo)) {
			node.levelNo = -1;
		}
		nodeMap[node.id] = node;
		normalizedNodes.push(node);
	});
	drawingRequestTreeState.nodeMap = nodeMap;

	$.each(normalizedNodes, function() {
		var node = this;
		if (!node.parent || node.parent === "#" || !nodeMap[node.parent]) {
			node.parent = "#";
			return;
		}

		var parentNode = nodeMap[node.parent];
		while (parentNode && node.levelNo >= 0 && parentNode.levelNo >= 0 && node.levelNo <= parentNode.levelNo) {
			node.parent = normalizeDrawingTreeKey(parentNode.parent || "#");
			if (node.parent === "#" || !nodeMap[node.parent]) {
				break;
			}
			parentNode = nodeMap[node.parent];
		}
	});

	$.each(normalizedNodes, function() {
		var parentId = normalizeDrawingTreeKey(this.parent || "#");
		if (!childrenMap[parentId]) {
			childrenMap[parentId] = [];
		}
		childrenMap[parentId].push(this);
	});

	$.each(childrenMap, function(parentId, childList) {
		childList.sort(function(a, b) {
			var sortA = parseInt(a.sort, 10);
			var sortB = parseInt(b.sort, 10);
			if (isNaN(sortA)) sortA = 999999;
			if (isNaN(sortB)) sortB = 999999;
			if (sortA !== sortB) {
				return sortA - sortB;
			}
			return (a.text || "").localeCompare(b.text || "");
		});
	});

	$tree
		.off(".drawingRequestTreeCustom")
		.empty()
		.append(buildDrawingRequestTreeList(childrenMap, "#", false, 0));

	$tree.on("click.drawingRequestTreeCustom", ".drawing-tree-toggle", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var $item = $(this).closest(".drawing-tree-item");
		if ($item.hasClass("is-leaf")) {
			return;
		}
		$item.toggleClass("is-open");
	});

	$tree.on("click.drawingRequestTreeCustom", ".drawing-tree-label", function(e) {
		e.preventDefault();
		var nodeId = $(this).data("nodeId");
		var node = nodeMap[nodeId];
		if (!node) {
			return;
		}

		$tree.find(".drawing-tree-label.is-selected").removeClass("is-selected");
		$(this).addClass("is-selected");

		applyDrawingRequestTreeFilter(buildDrawingRequestTreeFilterInfo(node));
	});

	$tree.find('.drawing-tree-label[data-node-id="DRAWING_TREE_ALL"]').addClass("is-selected");
}

function buildDrawingRequestTreeList(childrenMap, parentId, opened, depth) {
	var normalizedParentId = normalizeDrawingTreeKey(parentId);
	var childList = childrenMap[normalizedParentId] || [];
	var depthNo = parseInt(depth, 10);
	if (isNaN(depthNo) || depthNo < 0) {
		depthNo = 0;
	}
	var $list = $("<ul>", {
		"class": normalizedParentId === "#" ? "drawing-tree-root" : "drawing-tree-children"
	});

	$.each(childList, function() {
		var node = this;
		var hasChildren = !!(childrenMap[node.id] && childrenMap[node.id].length);
		// ROOT(Function Code) 노드는 표시하지 않고 바로 하위 depth를 최상단으로 노출
		if (shouldHideDrawingRootNode(node, depthNo)) {
			if (hasChildren) {
				var $rootChildren = buildDrawingRequestTreeList(childrenMap, node.id, opened, depthNo);
				$list.append($rootChildren.children());
			}
			return;
		}
		var isDocTypeNode = isDrawingRequestTypeNode(node);
		var $item = $("<li>", {
			"class": "drawing-tree-item depth-" + depthNo
				+ (hasChildren && opened ? " is-open" : "")
				+ (!hasChildren ? " is-leaf" : "")
				+ (isDocTypeNode ? " is-doc-type-node is-doc-type-node-drawing" : "")
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

			$item.append(buildDrawingRequestTreeList(childrenMap, node.id, false, depthNo + 1));
		}

		$list.append($item);
	});

	return $list;
}

function shouldHideDrawingRootNode(node, depthNo) {
	if (!node) {
		return false;
	}
	var nodeId = normalizeDrawingTreeKey(node.id).toUpperCase();
	var nodeText = $.trim(String(node.text || "")).toUpperCase();
	return node.levelNo === 0 || nodeId === "ROOT" || nodeId === "0" || nodeText === "FUNCTION CODE";
}

function isDrawingRequestTypeNode(node) {
	if (!node || node.text === undefined || node.text === null) {
		return false;
	}
	var text = $.trim(String(node.text)).toUpperCase();
	return text === "SP" || text === "D0" || text === "D1" || text === "D2";
}

function normalizeDrawingTreeKey(value) {
	if (value === undefined || value === null) {
		return "";
	}
	return $.trim(String(value));
}

function applyDrawingRequestTreeFilter(filterInfo) {
	filterInfo = filterInfo || {};

	setTreeFilterValue("drawingTreeCd", filterInfo.treeCd || "");
	setTreeFilterValue("distributeTypeCd", filterInfo.distributeTypeCd || "");
	updateDrawingRequestTreeSelection(filterInfo.label || "Documents", filterInfo.pathLabel || filterInfo.label || "Documents");

	gridParam = setGridParam();
	searchList(gridParam);
}

function setTreeFilterValue(fieldName, value) {
	var $form = $("#" + drawingRequestTreeState.formId);
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

function buildDrawingRequestTreeFilterInfo(node) {
	return {
		treeCd: node.id || "",
		distributeTypeCd: node.distributeTypeCd || "",
		label: $.trim(node.text || "Documents"),
		pathLabel: buildDrawingRequestTreePathLabel(node)
	};
}

function updateDrawingRequestTreeSelection(label, pathLabel) {
	$("#" + drawingRequestTreeState.treeId + "Selection").text(label || "전체");
	renderToolbarNavigator(pathLabel || label || "전체");
}

function buildDrawingRequestTreePathLabel(node) {
	var path = [];
	var current = node;
	var guard = 0;
	var nodeMap = drawingRequestTreeState.nodeMap || {};
	while (current && guard < 32) {
		path.unshift($.trim(current.text || ""));
		var parentId = normalizeDrawingTreeKey(current.parent || "#");
		if (parentId === "#" || !nodeMap[parentId]) {
			break;
		}
		current = nodeMap[parentId];
		guard++;
	}
	return path.join(" > ");
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

function formatValidType(cellValue, options, rowdata, action){
	var rtn = "";
	if ( cellValue != undefined ) {
		rtn = cellValue;
	}
	return '<font color="red">'+ rtn +'</font>';
}

function formatViewFile(cellValue, options, rowdata, action){
	return '<a onclick="openFile(\'OBJECT\', \'DRAWING\', \'' + rowdata["requestNo"] + '\', \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
}

function requestDistribute(){
	requestInsideUser('DISTRIBUTION', 'DRAWING', 'gridDrawingRequestList');
}

function requestPrint(){
	requestInsideUser('PRINT', 'DRAWING', 'gridDrawingRequestList');
}

function viewing(){
	openDialogPopup("/inside/common/viewer/openViewerPopup", {}, "popupDialog", 'l', 600);
}

function searchAll(){
	openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'DRAWING'}, "searchAllPopup", 's', 600);
}

function view3d(){
	window.open("/inside/distribution/drawingRequest/view3DFile", "", 'width="100%", height="100%", resizable = yes');
}

function revisionUpdate(){
	revisionUpdateInsideUser('DRAWING', 'gridDrawingRequestList');
}

function checkVersion(){
	checkVersionInsideUser('DRAWING', 'gridDrawingRequestList');
}
