var dxfRequestTreeState = {
	treeId: "dxfRequestExplorerTree",
	formId: "formDxfRequest",
	selectedLabel: "PMPCB",
	selectedTreeCd: "",
	nodeMap: {}
};

$(function() {
	initDxfRequestExplorerTree();
});

function initDxfRequestExplorerTree() {
	if (!$("#" + dxfRequestTreeState.treeId).length) {
		return;
	}

	waitForDxfRequestForm(0);
}

function waitForDxfRequestForm(tryCount) {
	if ($("#" + dxfRequestTreeState.formId).length) {
		loadDxfRequestExplorerTree();
		return;
	}

	if (tryCount > 20) {
		return;
	}

	setTimeout(function() {
		waitForDxfRequestForm(tryCount + 1);
	}, 150);
}

function loadDxfRequestExplorerTree() {
	callAjax({}, "/inside/distribution/dxfRequest/selectTree", function(response) {
		renderDxfRequestExplorerTree(response || []);
	});
}

function renderDxfRequestExplorerTree(treeList) {
	if (!treeList.length) {
		renderDxfRequestExplorerShellFallback();
		return;
	}

	renderDxfRequestExplorerCustomTree(treeList);
	updateDxfRequestTreeSelection("PMPCB", "PMPCB");
	applyDxfRequestTreeFilter({ filterType: "", drawingNoPrefix: "", distributeTypeCd: "", dxfTreeCd: "", label: "PMPCB", pathLabel: "PMPCB" });
}

function renderDxfRequestExplorerShellFallback() {
	var $tree = $("#" + dxfRequestTreeState.treeId);
	$tree.empty().append(
		$("<div>", { "class": "distribution-tree-placeholder" }).text("표시할 트리 데이터가 없습니다")
	);
	updateDxfRequestTreeSelection("PMPCB", "PMPCB");
}

function renderDxfRequestExplorerCustomTree(treeList) {
	var $tree = $("#" + dxfRequestTreeState.treeId);
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
	dxfRequestTreeState.nodeMap = nodeMap;

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
		.off(".dxfRequestTreeCustom")
		.empty()
		.append(buildDxfRequestTreeList(childrenMap, "#", false, 0));

	$tree.on("click.dxfRequestTreeCustom", ".drawing-tree-toggle", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var $item = $(this).closest(".drawing-tree-item");
		if ($item.hasClass("is-leaf")) {
			return;
		}
		$item.toggleClass("is-open");
	});

	$tree.on("click.dxfRequestTreeCustom", ".drawing-tree-label", function(e) {
		e.preventDefault();
		var nodeId = $(this).data("nodeId");
		var node = nodeMap[nodeId];
		if (!node) {
			return;
		}
		var isBoardNo = isDxfBoardNoNode(node);
		var filterTreeCd = resolveDxfTreeCdForFilter(node, nodeMap);

		$tree.find(".drawing-tree-label.is-selected").removeClass("is-selected");
		$(this).addClass("is-selected");

		applyDxfRequestTreeFilter({
			filterType: node.filterType || "",
			drawingNoPrefix: "",
			dxfTreeCd: filterTreeCd || node.id || "",
			distributeTypeCd: node.distributeTypeCd || "",
			label: getDxfNodeDisplayText(node) || "PMPCB",
			pathLabel: buildDxfRequestTreePathLabel(node, nodeMap)
		});
	});

	$tree.find('.drawing-tree-label[data-node-id="DXF_TREE_ALL"]').addClass("is-selected");
}

function buildDxfRequestTreeList(childrenMap, parentId, opened, depth) {
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
		if (shouldHideDxfRootNode(node, depthNo)) {
			if (hasChildren) {
				var $rootChildren = buildDxfRequestTreeList(childrenMap, node.id, opened, depthNo);
				$list.append($rootChildren.children());
			}
			return;
		}
		var isBoardNoNode = isDxfBoardNoNode(node);
		// 맨 하위 board 노드는 트리에서 노출하지 않음
		if (isBoardNoNode && !hasChildren) {
			return;
		}
		var $item = $("<li>", {
			"class": "drawing-tree-item depth-" + depthNo
				+ (hasChildren && opened ? " is-open" : "")
				+ (!hasChildren ? " is-leaf" : "")
				+ (isBoardNoNode ? " is-board-no-node is-board-no-node-dxf" : "")
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
			text: getDxfNodeDisplayText(node)
		}));

		$item.append($row);

		if (hasChildren) {
			$item.append(buildDxfRequestTreeList(childrenMap, node.id, false, depthNo + 1));
		}

		$list.append($item);
	});

	return $list;
}

function shouldHideDxfRootNode(node, depthNo) {
	if (!node || depthNo !== 0) {
		return false;
	}
	var nodeId = $.trim(String(node.id || "")).toUpperCase();
	var levelNo = parseInt(node.level, 10);
	return nodeId === "ROOT" || nodeId === "0" || levelNo === 0;
}

function getDxfNodeDisplayText(node) {
	var text = $.trim((node && node.text) || "");
	var level = $.trim(String((node && node.level) || ""));
	var parent = $.trim(String((node && node.parent) || "#"));
	if ((parent === "#" || level === "0") && text.toUpperCase() === "MRB") {
		return "PMPCB";
	}
	return text;
}

function isDxfBoardNoNode(node) {
	if (!node || node.text === undefined || node.text === null) {
		return false;
	}
	var text = $.trim(String(node.text)).toLowerCase().replace(/[^a-z0-9]/g, "");
	return text.indexOf("boardno") === 0;
}

function resolveDxfTreeCdForFilter(node, nodeMap) {
	if (!node) {
		return "";
	}
	if (!isDxfBoardNoNode(node)) {
		return node.id || "";
	}
	var parentId = node.parent;
	if (parentId && nodeMap[parentId]) {
		var parentNode = nodeMap[parentId];
		var parentParentId = parentNode.parent || "#";
		if (parentParentId === "#") {
			return node.id || "";
		}
		return parentNode.id || "";
	}
	return node.id || "";
}

function applyDxfRequestTreeFilter(filterInfo) {
	filterInfo = filterInfo || {};
	dxfRequestTreeState.selectedTreeCd = filterInfo.dxfTreeCd || "";

	setDxfTreeFilterValue("treeCd", filterInfo.dxfTreeCd || "");
	setDxfTreeFilterValue("drawingNo", filterInfo.drawingNoPrefix || "");
	setDxfTreeFilterValue("distributeTypeCd", filterInfo.distributeTypeCd || "");
	updateDxfRequestTreeSelection(filterInfo.label || "PMPCB", filterInfo.pathLabel || filterInfo.label || "PMPCB");

	gridParam = setGridParam();
	searchList(gridParam);
}

function setDxfTreeFilterValue(fieldName, value) {
	var $form = $("#" + dxfRequestTreeState.formId);
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

function buildDxfRequestTreePathLabel(node, nodeMap) {
	var path = [];
	var current = node;
	var guard = 0;

	while (current && guard < 32) {
		path.unshift(getDxfNodeDisplayText(current));
		var parentId = current.parent || "#";
		if (parentId === "#" || !nodeMap[parentId]) {
			break;
		}
		current = nodeMap[parentId];
		guard++;
	}

	return path.join(" > ");
}

function updateDxfRequestTreeSelection(label, pathLabel) {
	$("#" + dxfRequestTreeState.treeId + "Selection").text(label || "PMPCB");
	renderToolbarNavigator(pathLabel || label || "PMPCB");
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

	$nav.find(".tree-toolbar-navigator-path").text(pathLabel || "PMPCB");
}

function requestDistribute(){
	var protectYn = "N";
	var requestType = "PRODUCT_";
	if($("#"+gridId).getGridParam('selarrrow').length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}else {
		$.each($("#"+gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#"+gridId).jqGrid('getRowData', item);
		});
		requestType += $("#"+gridId).jqGrid('getCell', 1, 'objectType');
		openDialogPopup("/inside/distribution/commonRequest/dxfRequestPopup", {protectYn: protectYn, requestType: requestType, approvalRequestType : "DISTRIBUTION"}, "popupDialog", 'l', 685, true, 'popup-common popup-request');
	}
}

function requestPrint(){
	var protectYn = "N";
	var requestType = "PRODUCT_";
	if($("#"+gridId).getGridParam('selarrrow').length < 1){
		alertMessage(g_msg('msg.noSelectData'));
		return false;
	}else {
		$.each($("#"+gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#"+gridId).jqGrid('getRowData', item);
		});
		requestType += $("#"+gridId).jqGrid('getCell', 1, 'objectType');
		var popupHeight = Math.min($(window).height() - 100, 800);
		openDialogPopup("/inside/distribution/commonRequest/commonPrintRequestPopup", {protectYn: protectYn, requestType: requestType, approvalRequestType : "DISTRIBUTION"}, "popupDialog", 'l', popupHeight, true, 'popup-common popup-print-request');
	}
}

function changeObjectClass() {

}


function viewing(){
	openDialogPopup("/inside/common/viewer/openViewerPopup", {}, "popupDialog", 'l', 600);
}

function searchAll(){
	openDialogPopup("/inside/distribution/commonRequest/searchAllPopup", {type:'PRODUCTION'}, "searchAllPopup", 's', 600);
}

function formatOpenView(cellValue, options, rowdata, action){
	return '<a onclick="openDialogPopup(\'/inside/distribution/dxfRequest/dxfFilePopup\', { objectId: \'' + rowdata["objectId"] + '\', objectNo: \'' + rowdata["objectNo"] + '\', requestNo: \'' + rowdata["requestNo"] + '\' }, \'popupDialog\', \'l\', 720, true, \'popup-common popup-dxf-file\')">' + cellValue + '</a>';
}

function revisionUpdate(){
	updateFile();
}

// Toolbar 설정 함수명 호환
function updateDxf(){ updateFile(); }
function updateDXF(){ updateFile(); }
function revisionUpdateDxf(){ updateFile(); }
function revisionUpdateDXF(){ updateFile(); }

function updateFile(){
	var gridId = 'gridDxfRequestList';
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
	var dxfNo = stripHtml(data.objectNo || data.dxfNo || data.documentNo || data.drawingNo);
	var dataName = stripHtml(data.objectNm || data.fileNm || data.documentNm || data.dataName);
	var rowTreeCd = stripHtml(data.treeCd || data.TREE_CD);
	var selectedTreeCd = (window.dxfRequestTreeState && dxfRequestTreeState.selectedTreeCd) || "";
	var popupHeight = Math.min($(window).height() - 100, 600);
	openDialogPopup(
		"/inside/distribution/dxfRequest/dxfRegisterPopup",
		{
			treeCd: rowTreeCd || selectedTreeCd,
			isNewRevision: "true",
			objectId: data.objectId || "",
			revNo: data.revNo || "00",
			dxfNo: dxfNo,
			dataName: dataName,
			businessTypeCd: data.businessTypeCd || "",
			swTypeCd: data.swTypeCd || data.approver || data.APPROVER || "",
			reviewerUser: data.reviewerUser || data.revieweruser || data.REVIEWERUSER || "",
			distributeTypeCd: data.distributeTypeCd || "",
			pmpcbIssueDt: data.pmpcbIssueDt || ""
		},
		"popupDialog",
		'l',
		popupHeight,
		true,
		'popup-common popup-dxf-register'
	);
}
