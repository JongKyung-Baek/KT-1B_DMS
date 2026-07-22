<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<script>
	var mainFileRows = ${ empty mainFileJson ?'[]': mainFileJson };
	var subFileRows = ${ empty subFileJson ?'[]': subFileJson };
	var popupRequestNo = "${param.requestNo}";
	var popupObjectId = "${objectId}";
	var MAIN_GRID_BODY_HEIGHT = 52;
	var SUB_GRID_BODY_HEIGHT = 180;
	var APPROVAL_GRID_BODY_HEIGHT = 140;

	function escapeAttr(value) {
		return String(value === undefined || value === null ? "" : value)
			.replace(/&/g, "&amp;")
			.replace(/"/g, "&quot;")
			.replace(/'/g, "&#39;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;");
	}

	function openDxfFileViewer(objectId, fileNo) {
		if (!objectId) {
			alertMessage("파일 정보를 찾을 수 없습니다.");
			return;
		}
		openFile("OBJECT", "DXF", popupRequestNo || null, objectId, fileNo || null, "N");
	}

	function isDownloadableDxfFile(rowdata) {
		if (!rowdata || !rowdata.objectId) {
			return false;
		}
		if (!rowdata.orgFileNm || rowdata.orgFileNm === "파일 등록중입니다.") {
			return false;
		}
		if (rowdata.fileExists === false || String(rowdata.fileExists).toLowerCase() === "false") {
			return false;
		}
		return true;
	}

	function formatDxfFileName(cellValue, rowdata, useSubFileNo) {
		var name = cellValue || "";
		if (name === "") {
			return "";
		}
		if (!isDownloadableDxfFile(rowdata || {})) {
			return escapeAttr(name);
		}
		var objectId = rowdata.objectId || "";
		var fileNo = useSubFileNo ? (rowdata.fileNo || "") : "";

		return '<a href="javascript:void(0);" class="dxf-file-link"'
			+ ' data-object-id="' + escapeAttr(objectId) + '"'
			+ ' data-file-no="' + escapeAttr(fileNo) + '">'
			+ escapeAttr(name) + '</a>';
	}

	function initDxfFileGrid(gridId, rows) {
		var $grid = $("#" + gridId);
		var useSubFileNo = gridId === "gridDxfSubFile";
		var gridBodyHeight = useSubFileNo ? SUB_GRID_BODY_HEIGHT : MAIN_GRID_BODY_HEIGHT;
		var gridTotalHeight = gridBodyHeight + 38;
		$grid.jqGrid({
			datatype: "local",
			data: rows || [],
			colModel: [
				{ name: "fileNo", label: "파일순번", width: 90, align: "center", sortable: false },
				{
					name: "orgFileNm",
					label: "파일명",
					width: 900,
					sortable: false,
					formatter: function (cellValue, options, rowdata) {
						return formatDxfFileName(cellValue, rowdata || {}, useSubFileNo);
					}
				},
				{ name: "fileSize", label: "파일크기(Byte)", width: 170, align: "right", sortable: false }
			],
			height: gridBodyHeight,
			autowidth: true,
			shrinkToFit: false,
			forceFit: false,
			scrollOffset: 18,
			rowNum: 9999,
			multiselect: true,
			multiboxonly: false,
			viewrecords: false,
			loadonce: true,
			beforeSelectRow: function (rowId, e) {
				return $(e.target).closest(".dxf-file-link").length === 0;
			}
		});

		$grid.closest(".ui-jqgrid-bdiv").css({
			height: gridBodyHeight + "px",
			overflowX: "auto",
			overflowY: "auto"
		});

		$grid.closest(".ui-jqgrid").css("height", gridTotalHeight + "px");
		$grid.closest(".ui-jqgrid-view").css("height", gridTotalHeight + "px");
	}

	function formatCommentCell(cellValue, options, rowdata, gridId) {
		var comment = cellValue && cellValue !== "-" ? cellValue : "승인하였습니다.";
		if (rowdata.editable !== "Y") {
			return escapeAttr(cellValue || "-");
		}
		return ''
			+ '<input type="text" class="approval-comment-input" data-grid-id="' + escapeAttr(gridId) + '" data-row-id="' + escapeAttr(options.rowId) + '"'
			+ ' value="' + escapeAttr(comment) + '" style="width:96%;" />';
	}

	function initApprovalStatusGrid(gridId, rows) {
		var $grid = $("#" + gridId);
		if ($grid[0] && $grid[0].grid) {
			$grid.jqGrid("GridUnload");
		}
		$grid.jqGrid({
			datatype: "local",
			data: rows || [],
			colModel: [
				{ name: "approver", label: "이름", width: 220, sortable: false },
				{ name: "status", label: "상태", width: 120, align: "center", sortable: false },
				{ name: "comment", label: "코멘트", width: 360, sortable: false, formatter: function (cellValue, options, rowdata) { return formatCommentCell(cellValue, options, rowdata, gridId); } }
			],
			height: APPROVAL_GRID_BODY_HEIGHT,
			autowidth: true,
			shrinkToFit: true,
			forceFit: true,
			scrollOffset: 0,
			rowNum: 9999,
			viewrecords: false,
			loadonce: true,
			loadComplete: function () {
				var $container = $grid.closest(".gridContainer");
				if ($container.length) {
					$grid.jqGrid("setGridWidth", $container.width(), true);
				}
			}
		});

		$grid.closest(".ui-jqgrid-bdiv").css({
			height: APPROVAL_GRID_BODY_HEIGHT + "px",
			overflowX: "auto",
			overflowY: "auto"
		});

		var $container = $grid.closest(".gridContainer");
		if ($container.length) {
			$grid.jqGrid("setGridWidth", $container.width(), true);
		}
	}

	function loadApprovalStatus() {
		var emptyRows = [{ approver: "-", status: "-", comment: "조회 대상 없음" }];
		if (!popupObjectId) {
			initApprovalStatusGrid("gridDxfApproverStatus", emptyRows);
			initApprovalStatusGrid("gridDxfReviewerStatus", emptyRows);
			return;
		}
		callAjax(
			{ objectId: popupObjectId },
			"/inside/distribution/dxfRequest/approveStatusRows",
			function (response) {
				var rows = response && response.rows ? response.rows : [];
				var approverRows = [];
				var reviewerRows = [];
				$.each(rows, function (idx, row) {
					if ((row.approvalType || "").toUpperCase() === "REVIEWER") { reviewerRows.push(row); } else { approverRows.push(row); }
				});
				if (!approverRows.length) { approverRows = emptyRows; }
				if (!reviewerRows.length) { reviewerRows = emptyRows; }
				initApprovalStatusGrid("gridDxfApproverStatus", approverRows);
				initApprovalStatusGrid("gridDxfReviewerStatus", reviewerRows);
			},
			"json"
		);
	}

	function saveApprovalComment(gridId, rowId) {
		var $input = $('.approval-comment-input[data-grid-id="' + gridId + '"][data-row-id="' + rowId + '"]');
		if (!$input.length) {
			return;
		}
		var comment = $.trim($input.val());
		if (!comment) {
			comment = "승인하였습니다.";
			$input.val(comment);
		}
		callAjax(
			{ objectId: popupObjectId, comment: comment },
			"/inside/distribution/dxfRequest/saveApprovalComment",
			function (response) {
				if (response && response.success) {
					alertMessage("코멘트가 저장되었습니다.", function () {
                        $("#alertMessage").dialog("close");
                        loadApprovalStatus();
					});
					return;
				}
				alertMessage((response && response.message) ? response.message : "코멘트 저장에 실패했습니다.");
			},
			"json"
		);
	}

	function saveApprovalCommentByGrid(gridId) {
		var $input = $('.approval-comment-input[data-grid-id="' + gridId + '"]').first();
		if (!$input.length) {
			alertMessage("저장 가능한 코멘트 행이 없습니다.");
			return;
		}
		saveApprovalComment(gridId, $input.data("rowId"));
	}

	function downloadSelectedDxfFile(gridId) {
		var $grid = $("#" + gridId);
		var selectedRows = $grid.jqGrid('getGridParam', 'selarrrow') || [];
		if (!selectedRows.length) {
			var singleRow = $grid.jqGrid('getGridParam', 'selrow');
			if (singleRow) {
				selectedRows = [singleRow];
			}
		}
		if (!selectedRows.length) {
			alertMessage(g_msg('msg.noSelectedItem'));
			return;
		}

		var requests = [];
		var blockedFiles = [];
		for (var i = 0; i < selectedRows.length; i++) {
			var data = $grid.jqGrid('getLocalRow', selectedRows[i]) || $grid.jqGrid('getRowData', selectedRows[i]);
			if (!isDownloadableDxfFile(data || {})) {
				blockedFiles.push((data && (data.fileViewNm || data.orgFileNm || data.fileNm || data.fileName)) || "알 수 없는 파일");
				continue;
			}
			var url = "/inside/distribution/dxfRequest/downloadFile?objectId=" + encodeURIComponent(data.objectId);
			if (data.fileNo !== undefined && data.fileNo !== null && String(data.fileNo) !== "") {
				url += "&fileNo=" + encodeURIComponent(data.fileNo);
			}
			requests.push({
				url: url,
				fileName: data.fileViewNm || data.orgFileNm || data.fileNm || data.fileName || ""
			});
		}
		if (!requests.length) {
			alertMessage("다운로드 가능한 파일이 없습니다.");
			return;
		}

		downloadDxfFilesSequentially(requests, 0, blockedFiles);
	}

	function downloadDxfFilesSequentially(requests, idx, blockedFiles) {
		if (!requests || idx >= requests.length) {
			if (blockedFiles && blockedFiles.length) {
				alertMessage("다운로드 불가능한 파일 " + blockedFiles.length + "건은 제외되었습니다.");
			}
			return;
		}
		var req = requests[idx];
		fetch(req.url, { method: "GET", credentials: "same-origin" })
			.then(function (response) {
				if (!response.ok) {
					if (response.status === 403) {
						blockedFiles.push(req.fileName || "알 수 없는 파일");
						return;
					}
					alertMessage("다운로드에 실패했습니다.");
					return;
				}
				var disposition = response.headers.get("Content-Disposition") || "";
				return response.blob().then(function (blob) {
					var downloadName = extractFileNameFromDisposition(disposition) || "download.bin";
					triggerBlobDownload(blob, downloadName);
				});
			})
			.catch(function () {
				alertMessage("다운로드에 실패했습니다.");
			})
			.finally(function () {
				setTimeout(function () {
					downloadDxfFilesSequentially(requests, idx + 1, blockedFiles);
				}, 150);
			});
	}

	function extractFileNameFromDisposition(disposition) {
		var utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i);
		if (utf8Match && utf8Match[1]) {
			try {
				return decodeURIComponent(utf8Match[1]);
			} catch (e) {}
		}
		var basicMatch = disposition.match(/filename=\"?([^\";]+)\"?/i);
		return basicMatch && basicMatch[1] ? basicMatch[1] : null;
	}

	function triggerBlobDownload(blob, fileName) {
		var blobUrl = window.URL.createObjectURL(blob);
		var a = document.createElement("a");
		a.href = blobUrl;
		a.download = fileName;
		document.body.appendChild(a);
		a.click();
		document.body.removeChild(a);
		window.URL.revokeObjectURL(blobUrl);
	}

	function isDxfFileProcessing(status) {
		var s = String(status || "").trim().toUpperCase();
		if (!s) {
			return false;
		}
		if (s === "DONE" || s === "SUCCESS" || s === "COMPLETED" || s === "완료") {
			return false;
		}
		return s.indexOf("PROCESS") >= 0 || s === "PENDING" || s === "WAITING";
	}

	function getDxfMainRowsForDisplay(rows) {
		var list = Array.isArray(rows) ? rows : [];
		if (!list.length) {
			return list;
		}
		var status = list[0] && list[0].processingStatus;
		if (!isDxfFileProcessing(status)) {
			return list;
		}
		return [{
			fileNo: "",
			orgFileNm: "파일 등록중입니다.",
			fileSize: "",
			objectId: ""
		}];
	}

	$(function () {
		mainFileRows = getDxfMainRowsForDisplay(mainFileRows);
		if (mainFileRows.length && mainFileRows[0].orgFileNm === "파일 등록중입니다.") {
			subFileRows = [];
		}
		loadApprovalStatus();
		initDxfFileGrid("gridDxfMainFile", mainFileRows);
		initDxfFileGrid("gridDxfSubFile", subFileRows);

		$(document).on("click", ".dxf-file-popup .dxf-file-link", function (e) {
			e.preventDefault();
			var $row = $(this).closest('tr.jqgrow');
			var rowId = $row.attr('id');
			var tableId = $(this).closest('table.ui-jqgrid-btable').attr('id');
			if (rowId && tableId) {
				$("#" + tableId).jqGrid('setSelection', rowId, false);
			}
			var objectId = $(this).data("objectId");
			var fileNo = $(this).data("fileNo");
			openDxfFileViewer(objectId, fileNo);
		});

		$(document).on("click", ".dxf-file-popup .approval-comment-save", function (e) {
			e.preventDefault();
			saveApprovalComment($(this).data("gridId"), $(this).data("rowId"));
		});
	});
</script>

<style>
	.dxf-file-popup .popupMeta {
		margin: 6px 0 14px;
		font-size: 14px;
		color: #2f3a55;
	}

	.dxf-file-popup .sectionBlock {
		margin-top: 12px;
	}

	.dxf-file-popup .popupCard {
		background: #fff;
		border: 1px solid #dce3ee;
		border-radius: 10px;
		padding: 0;
	}

	.dxf-file-popup .dialogToolbar {
		height: 42px;
		padding: 0 14px;
		border-bottom: 1px solid #e6ebf3;
		background: #f8fafd;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.dxf-file-popup .dialogToolbar .gridTitle {
		font-size: 14px;
		font-weight: 700;
		color: #2f3a55;
	}

	.dxf-file-popup .dialogToolbar .listCount {
		margin-left: 8px;
		color: #6b778c;
		font-size: 12px;
	}

	.dxf-file-popup .gridContainer {
		padding: 0;
	}

	.dxf-file-popup .gridContainer .ui-jqgrid {
		width: 100% !important;
		height: auto !important;
		border: 0;
	}

	.dxf-file-popup .gridContainer .ui-jqgrid-view,
	.dxf-file-popup .gridContainer .ui-jqgrid-hdiv,
	.dxf-file-popup .gridContainer .ui-jqgrid-bdiv {
		width: 100% !important;
	}

	.dxf-file-popup .gridContainer .ui-jqgrid-view {
		height: auto !important;
	}

	.dxf-file-popup .gridContainer .ui-jqgrid-bdiv {
		height: 174px !important;
		overflow-x: auto !important;
		overflow-y: auto !important;
	}

	.dxf-file-popup .mainFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 52px !important;
	}

	.dxf-file-popup .subFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 174px !important;
	}

	.dxf-file-popup .ui-jqgrid .ui-jqgrid-hdiv {
		background: #fafafa;
		border-color: #909090;
	}

	.dxf-file-popup .ui-jqgrid .ui-jqgrid-htable th {
		background: #fafafa;
		color: #333333;
		font-weight: 700;
		border-color: #e0e0e0;
		height: 38px;
	}

	.dxf-file-popup .ui-jqgrid tr.jqgrow td {
		height: 36px;
		border-color: #edf1f7;
	}

	.dxf-file-popup .ui-jqgrid .ui-jqgrid-bdiv tr.ui-state-hover,
	.dxf-file-popup .ui-jqgrid .ui-jqgrid-bdiv tr:hover,
	.dxf-file-popup .ui-jqgrid .ui-jqgrid-bdiv tr:nth-child(odd).ui-state-hover {
		background: rgba(0, 127, 175, 0.25);
	}

	.dxf-file-popup .ui-jqgrid .ui-jqgrid-htable th.ui-state-hover,
	.dxf-file-popup .ui-jqgrid .ui-jqgrid-htable th:hover,
	.dxf-file-popup .ui-jqgrid .ui-jqgrid-htable .ui-jqgrid-labels th.ui-state-hover,
	.dxf-file-popup .ui-jqgrid .ui-jqgrid-htable .ui-jqgrid-labels th:hover {
		border-color: rgba(0, 127, 175, 0.25);
		background: rgba(0, 127, 175, 0.25);
		color: #333333;
	}

	.dxf-file-popup .ui-jqgrid input.cbox,
	.dxf-file-popup .ui-jqgrid input.cbox:hover,
	.dxf-file-popup .ui-jqgrid input.cbox:focus,
	.dxf-file-popup .ui-jqgrid input.cbox:focus-visible,
	.dxf-file-popup .ui-jqgrid input.cbox:active {
		box-shadow: none !important;
		outline: none !important;
	}
</style>

<div class="dialogContent dxf-file-popup popup-base popup-actions-center popup-type-form-grid">
	<div class="popupHero">
		<h2>파일 정보</h2>
	</div>

	<div class="popupMeta">
		문서번호: <strong>${dxfNo}</strong>
	</div>

	<div class="section popupCard sectionBlock">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">보드멤버 승인 상태</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="saveApprovalCommentByGrid('gridDxfApproverStatus')">저장</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridDxfApproverStatus"></table>
		</div>
	</div>

	<div class="section popupCard sectionBlock">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">참여자 승인 상태</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="saveApprovalCommentByGrid('gridDxfReviewerStatus')">저장</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridDxfReviewerStatus"></table>
		</div>
	</div>

	<div class="section popupCard sectionBlock mainFileSection">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">주파일 정보</span>
				<span class="listCount">총 ${mainFileList.size()}건</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="downloadSelectedDxfFile('gridDxfMainFile')">다운로드</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridDxfMainFile"></table>
		</div>
	</div>

	<div class="section popupCard sectionBlock subFileSection">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">보조파일 정보</span>
				<span class="listCount">총 ${subFileList.size()}건</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="downloadSelectedDxfFile('gridDxfSubFile')">다운로드</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridDxfSubFile"></table>
		</div>
	</div>
</div>

<div class="dialogBtnSet">
	<div class="left"></div>
	<div class="right">
		<button class="ui-button ui-corner-all bottomBtn" onclick="closePopup('popupDialog')">
			<spring:message code="btn.close" />
		</button>
	</div>
</div>













