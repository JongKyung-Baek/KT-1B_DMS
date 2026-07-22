var productionRequestTreeState = {
	treeId: "productionRequestExplorerTree",
	formId: "formProductionRequest",
	selectedLabel: "전체",
	selectedTreeCd: "",
	nodeMap: {}
};

$(function() {
	initProductionRequestExplorerTree();
});

function initProductionRequestExplorerTree() {
	if (!$("#" + productionRequestTreeState.treeId).length) {
		return;
	}

	waitForProductionRequestForm(0);
}

function waitForProductionRequestForm(tryCount) {
	if ($("#" + productionRequestTreeState.formId).length) {
		loadProductionRequestExplorerTree();
		return;
	}

	if (tryCount > 20) {
		return;
	}

	setTimeout(function() {
		waitForProductionRequestForm(tryCount + 1);
	}, 150);
}

function loadProductionRequestExplorerTree() {
	callAjax({}, "/inside/distribution/productionRequest/selectTree", function(response) {
		renderProductionRequestExplorerTree(response || []);
	});
}

function renderProductionRequestExplorerTree(treeList) {
	if (!treeList.length) {
		renderProductionRequestExplorerShellFallback();
		return;
	}

	renderProductionRequestExplorerCustomTree(treeList);
	updateProductionRequestTreeSelection("MRB", "MRB");
	applyProductionRequestTreeFilter({ filterType: "", objectNoPrefix: "", distributeTypeCd: "", productionTreeCd: "", label: "MRB", pathLabel: "MRB" });
}

function renderProductionRequestExplorerShellFallback() {
	var $tree = $("#" + productionRequestTreeState.treeId);
	$tree.empty().append(
		$("<div>", { "class": "distribution-tree-placeholder" }).text("표시할 트리 데이터가 없습니다")
	);
	updateProductionRequestTreeSelection("전체", "MRB");
}

function renderProductionRequestExplorerCustomTree(treeList) {
	var $tree = $("#" + productionRequestTreeState.treeId);
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
	productionRequestTreeState.nodeMap = nodeMap;

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
		.off(".productionRequestTreeCustom")
		.empty()
		.append(buildProductionRequestTreeList(childrenMap, "#", false, 0));

	$tree.on("click.productionRequestTreeCustom", ".drawing-tree-toggle", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var $item = $(this).closest(".drawing-tree-item");
		if ($item.hasClass("is-leaf")) {
			return;
		}
		$item.toggleClass("is-open");
	});

	$tree.on("click.productionRequestTreeCustom", ".drawing-tree-label", function(e) {
		e.preventDefault();
		var nodeId = $(this).data("nodeId");
		var node = nodeMap[nodeId];
		if (!node) {
			return;
		}
		var filterTreeCd = resolveProductionTreeCdForFilter(node, nodeMap);

		$tree.find(".drawing-tree-label.is-selected").removeClass("is-selected");
		$(this).addClass("is-selected");

		applyProductionRequestTreeFilter({
			filterType: node.filterType || "",
			objectNoPrefix: node.objectNoPrefix || "",
			productionTreeCd: filterTreeCd || node.id || "",
			distributeTypeCd: node.distributeTypeCd || "",
			label: $.trim(node.text || "MRB"),
			pathLabel: buildProductionRequestTreePathLabel(node, nodeMap)
		});
	});

	$tree.find('.drawing-tree-label[data-node-id="PRODUCTION_TREE_ALL"]').addClass("is-selected");
}

function buildProductionRequestTreeList(childrenMap, parentId, opened, depth) {
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
		if (shouldHideProductionRootNode(node, depthNo)) {
			if (hasChildren) {
				var $rootChildren = buildProductionRequestTreeList(childrenMap, node.id, opened, depthNo);
				$list.append($rootChildren.children());
			}
			return;
		}
		var isBoardNoNode = isProductionBoardNoNode(node);
		// 맨 하위 board 노드는 트리에서 노출하지 않음
		if (isBoardNoNode && !hasChildren) {
			return;
		}
		var $item = $("<li>", {
			"class": "drawing-tree-item depth-" + depthNo
				+ (hasChildren && opened ? " is-open" : "")
				+ (!hasChildren ? " is-leaf" : "")
				+ (isBoardNoNode ? " is-board-no-node is-board-no-node-production" : "")
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
			$item.append(buildProductionRequestTreeList(childrenMap, node.id, false, depthNo + 1));
		}

		$list.append($item);
	});

	return $list;
}

function shouldHideProductionRootNode(node, depthNo) {
	if (!node || depthNo !== 0) {
		return false;
	}
	var nodeId = $.trim(String(node.id || "")).toUpperCase();
	var levelNo = parseInt(node.level, 10);
	return nodeId === "ROOT" || nodeId === "0" || levelNo === 0;
}

function isProductionBoardNoNode(node) {
	if (!node || node.text === undefined || node.text === null) {
		return false;
	}
	var text = $.trim(String(node.text)).toLowerCase().replace(/[^a-z0-9]/g, "");
	return text.indexOf("boardno") === 0;
}

function resolveProductionTreeCdForFilter(node, nodeMap) {
	if (!node) {
		return "";
	}
	if (!isProductionBoardNoNode(node)) {
		return node.id || "";
	}
	var parentId = node.parent;
	if (parentId && nodeMap[parentId]) {
		return nodeMap[parentId].id || "";
	}
	return node.id || "";
}

function applyProductionRequestTreeFilter(filterInfo) {
	filterInfo = filterInfo || {};
	productionRequestTreeState.selectedTreeCd = filterInfo.productionTreeCd || "";

	setProductionTreeFilterValue("treeCd", filterInfo.productionTreeCd || "");
	setProductionTreeFilterValue("objectNoPrefix", filterInfo.objectNoPrefix || "");
	setProductionTreeFilterValue("distributeTypeCd", filterInfo.distributeTypeCd || "");
	updateProductionRequestTreeSelection(filterInfo.label || "MRB", filterInfo.pathLabel || filterInfo.label || "MRB");

	gridParam = setGridParam();
	searchList(gridParam);
}

function setProductionTreeFilterValue(fieldName, value) {
	var $form = $("#" + productionRequestTreeState.formId);
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

function buildProductionRequestTreePathLabel(node, nodeMap) {
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

function updateProductionRequestTreeSelection(label, pathLabel) {
	$("#" + productionRequestTreeState.treeId + "Selection").text(label || "전체");
	renderToolbarNavigator(pathLabel || label || "MRB");
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
		openDialogPopup("/inside/distribution/commonRequest/productionRequestPopup", {protectYn: protectYn, requestType: requestType, approvalRequestType : "DISTRIBUTION"}, "popupDialog", 'l', 685, true, 'popup-common popup-request');
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
	return '<a onclick="openDialogPopup(\'/inside/distribution/productionRequest/productionFilePopup\', { objectId: \'' + rowdata["objectId"] + '\', objectNo: \'' + rowdata["objectNo"] + '\', requestNo: \'' + rowdata["requestNo"] + '\' }, \'popupDialog\', \'l\', 720, true, \'popup-common popup-production-file\')">' + cellValue + '</a>';
}

function revisionUpdate(){
	updateFile();
}

// Toolbar 설정 함수명 호환
function updatePrd(){ updateFile(); }
function updateProduction(){ updateFile(); }
function revisionUpdatePrd(){ updateFile(); }
function revisionUpdateProduction(){ updateFile(); }

function updateFile(){
	var gridId = 'gridDistributionProductionRequestList';
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
	console.log('[production updateFile] rowId=', rowId);
	console.log('[production updateFile] rawData=', rawData);
	console.log('[production updateFile] rowData=', rowData);
	console.log('[production updateFile] merged data reviewer keys=', {
		reviewerUser: data.reviewerUser,
		revieweruser: data.revieweruser,
		reviewer_user: data.reviewer_user,
		REVIEWERUSER: data.REVIEWERUSER,
		REVIEWER_USER: data.REVIEWER_USER
	});
	var stripHtml = function(v){ return String(v || '').replace(/<[^>]*>/g, '').trim(); };
	var productionNo = stripHtml(data.objectNo || data.productionNo || data.documentNo || data.drawingNo);
	var dataName = stripHtml(data.objectNm || data.fileNm || data.documentNm || data.dataName);
	var rowTreeCd = stripHtml(data.treeCd || data.TREE_CD);
	var selectedTreeCd = (window.productionRequestTreeState && productionRequestTreeState.selectedTreeCd) || "";
	var popupHeight = Math.min($(window).height() - 100, 600);
	var popupParam = {
		treeCd: rowTreeCd || selectedTreeCd,
		isNewRevision: "true",
		objectId: data.objectId || "",
		revNo: data.revNo || "00",
		productionNo: productionNo,
		dataName: dataName,
		businessTypeCd: data.businessTypeCd || "",
		swTypeCd: data.swTypeCd || data.approver || "",
		reviewerUser: data.reviewerUser || data.revieweruser || "",
		distributeTypeCd: data.distributeTypeCd || "",
		mrbIssueDt: data.mrbIssueDt || ""
	};

	var openPopup = function(param) {
		openDialogPopup(
			"/inside/distribution/productionRequest/productionRegisterPopup",
			param,
			"popupDialog",
			'l',
			popupHeight,
			true,
			'popup-common popup-production-register'
		);
	};

	if ($.trim(popupParam.objectId) !== "") {
		callAjax(
			{ objectId: popupParam.objectId },
			"/inside/distribution/productionRequest/selectRevisionUsers",
			function(res){
				console.log('[production updateFile] selectRevisionUsers response=', res);
				if (res) {
					if ($.trim(popupParam.swTypeCd) === "") {
						popupParam.swTypeCd = res.approver || "";
					}
					if ($.trim(popupParam.reviewerUser) === "") {
						popupParam.reviewerUser = res.reviewerUser || "";
					}
				}
				console.log('[production updateFile] popupParam before open=', popupParam);
				openPopup(popupParam);
			},
			"json"
		);
	} else {
		openPopup(popupParam);
	}
}
