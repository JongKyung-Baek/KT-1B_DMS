var docRequestTreeState = {
	treeId: "docRequestExplorerTree",
	formId: "formDocRequest",
	selectedLabel: "전체"
};

$(function() {
	initDocRequestExplorerTree();
});

function initDocRequestExplorerTree() {
	if (!$("#" + docRequestTreeState.treeId).length) {
		return;
	}

	waitForDocRequestForm(0);
}

function waitForDocRequestForm(tryCount) {
	if ($("#" + docRequestTreeState.formId).length) {
		loadDocRequestExplorerTree();
		return;
	}

	if (tryCount > 20) {
		return;
	}

	setTimeout(function() {
		waitForDocRequestForm(tryCount + 1);
	}, 150);
}

function loadDocRequestExplorerTree() {
	callAjax({}, "/inside/distribution/docRequest/selectTree", function(response) {
		renderDocRequestExplorerTree(response || []);
	});
}

function renderDocRequestExplorerTree(treeList) {
	if (!treeList.length) {
		renderDocRequestExplorerShellFallback();
		return;
	}

	renderDocRequestExplorerCustomTree(treeList);
	applyDocRequestTreeFilter({ treeCd: "", label: "IOC", pathLabel: "IOC" });
}

function renderDocRequestExplorerShellFallback() {
	var $tree = $("#" + docRequestTreeState.treeId);
	$tree.empty().append(
		$("<div>", { "class": "distribution-tree-placeholder" }).text("표시할 트리 데이터가 없습니다")
	);
	updateDocRequestTreeSelection("전체", "IOC");
}

function renderDocRequestExplorerCustomTree(treeList) {
	var $tree = $("#" + docRequestTreeState.treeId);
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
		.off(".docRequestTreeCustom")
		.empty()
		.append(buildDocRequestTreeList(childrenMap, "#", false, 0));

	$tree.on("click.docRequestTreeCustom", ".drawing-tree-toggle", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var $item = $(this).closest(".drawing-tree-item");
		if ($item.hasClass("is-leaf")) {
			return;
		}
		$item.toggleClass("is-open");
	});

	$tree.on("click.docRequestTreeCustom", ".drawing-tree-label", function(e) {
		e.preventDefault();
		var nodeId = $(this).data("nodeId");
		var node = nodeMap[nodeId];
		if (!node) {
			return;
		}

		$tree.find(".drawing-tree-label.is-selected").removeClass("is-selected");
		$(this).addClass("is-selected");

		applyDocRequestTreeFilter({
			treeCd: node.id || "",
			label: $.trim(node.text || "IOC"),
			pathLabel: buildDocRequestTreePathLabel(node, nodeMap)
		});
	});

	$tree.find('.drawing-tree-label[data-node-id="DOC_TREE_ALL"]').addClass("is-selected");
}

function buildDocRequestTreeList(childrenMap, parentId, opened, depth) {
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
		if (shouldHideDocRootNode(node, depthNo)) {
			if (hasChildren) {
				var $rootChildren = buildDocRequestTreeList(childrenMap, node.id, opened, depthNo);
				$list.append($rootChildren.children());
			}
			return;
		}
		var isDocTypeNode = isDocRequestTypeNode(node);
		var $item = $("<li>", {
			"class": "drawing-tree-item depth-" + depthNo
				+ (hasChildren && opened ? " is-open" : "")
				+ (!hasChildren ? " is-leaf" : "")
				+ (isDocTypeNode ? " is-doc-type-node is-doc-type-node-doc" : "")
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
			$item.append(buildDocRequestTreeList(childrenMap, node.id, false, depthNo + 1));
		}

		$list.append($item);
	});

	return $list;
}

function shouldHideDocRootNode(node, depthNo) {
	if (!node || depthNo !== 0) {
		return false;
	}
	var nodeId = $.trim(String(node.id || "")).toUpperCase();
	var levelNo = parseInt(node.level, 10);
	return nodeId === "ROOT" || nodeId === "0" || levelNo === 0;
}

function isDocRequestTypeNode(node) {
	if (!node || node.text === undefined || node.text === null) {
		return false;
	}
	var text = $.trim(String(node.text)).toUpperCase();
	return text === "SP" || text === "D0" || text === "D1" || text === "D2";
}

function buildDocRequestTreePathLabel(node, nodeMap) {
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

	if (path.length) {
		var first = $.trim(path[0] || "");
		if (/^function\s*code$/i.test(first) || first === "ROOT" || first === "0") {
			path[0] = "IOC";
		}
	}

	return path.join(" > ");
}

function applyDocRequestTreeFilter(filterInfo) {
	filterInfo = filterInfo || {};

	setDocTreeFilterValue("docTreeCd", filterInfo.treeCd || "");
	updateDocRequestTreeSelection(filterInfo.label || "IOC", filterInfo.pathLabel || filterInfo.label || "IOC");

	gridParam = setGridParam();
	searchList(gridParam);
}

function setDocTreeFilterValue(fieldName, value) {
	var $form = $("#" + docRequestTreeState.formId);
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

function updateDocRequestTreeSelection(label, pathLabel) {
	$("#" + docRequestTreeState.treeId + "Selection").text(label || "전체");
	renderToolbarNavigator(pathLabel || label || "전체");
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
	requestInsideUser('DISTRIBUTION', 'DOC', 'gridDocRequestList');
}

function requestPrint(){
	requestInsideUser('PRINT', 'DOC', 'gridDocRequestList');
}

function searchAll(){
	openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'DOC'}, "searchAllPopup", 's', 600);
}
