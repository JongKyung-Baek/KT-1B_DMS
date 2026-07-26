<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script>
	var mainFileRows = ${ empty mainFileJson ?'[]': mainFileJson };
	var subFileRows = ${ empty subFileJson ?'[]': subFileJson };
	var popupRequestNo = "${param.requestNo}";
	var popupObjectId = "${objectId}";
	var MAIN_GRID_BODY_HEIGHT = 52;
	var SUB_GRID_BODY_HEIGHT = 174;
	var APPROVAL_GRID_BODY_HEIGHT = 140;

	function escapeAttr(value) {
		return String(value === undefined || value === null ? "" : value)
			.replace(/&/g, "&amp;")
			.replace(/"/g, "&quot;")
			.replace(/'/g, "&#39;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;");
	}

	function getSwGradePresentation(cellValue, rowdata) {
		var row = rowdata || {};
		var gradeName = $.trim(String(cellValue || row.gradeNm || ""));
		var gradeCode = $.trim(String(row.gradeCd || ""));
		var levelText = $.trim(String(row.gradeLevel === undefined || row.gradeLevel === null ? "" : row.gradeLevel));
		var level = parseInt(levelText, 10);
		var normalizedCode = gradeCode.toUpperCase();
		var tone = "general";
		if (!gradeName) {
			tone = "unassigned";
		} else if ((!isNaN(level) && level >= 40) || normalizedCode === "CONFIDENTIAL") {
			tone = "confidential";
		} else if ((!isNaN(level) && level >= 30) || normalizedCode === "RESTRICTED") {
			tone = "restricted";
		} else if ((!isNaN(level) && level >= 20) || normalizedCode === "INTERNAL") {
			tone = "internal";
		}
		return {
			name: gradeName || "미지정",
			tone: tone,
			title: gradeName
				? "적용등급: " + gradeName + (gradeCode ? " (" + gradeCode + (levelText ? ", " + levelText : "") + ")" : "")
				: "적용할 문서등급이 지정되지 않았습니다."
		};
	}

	function formatSwFileGrade(cellValue, options, rowdata) {
		var grade = getSwGradePresentation(cellValue, rowdata);
		return '<span class="document-grade-badge document-grade-badge--' + grade.tone
			+ '" title="' + escapeAttr(grade.title) + '">' + escapeAttr(grade.name) + '</span>';
	}

	function renderSwDocumentGrade(rowdata) {
		var grade = getSwGradePresentation(rowdata && rowdata.gradeNm, rowdata);
		$(".sw-document-grade-display")
			.attr("class", "sw-document-grade-display document-grade-badge document-grade-badge--" + grade.tone)
			.attr("title", grade.title)
			.text(grade.name);
	}

	function openSwFileViewer(objectId, fileNo) {
		if (!objectId) {
			alertMessage("파일 정보를 찾을 수 없습니다.");
			return;
		}
		openFile("OBJECT", "SW", popupRequestNo || null, objectId, fileNo || null, "N");
	}

	function isDownloadableSwFile(rowdata) {
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

	function formatSwFileName(cellValue, rowdata, useSubFileNo) {
		var name = cellValue || "";
		if (name === "") {
			return "";
		}
		if (!isDownloadableSwFile(rowdata || {})) {
			return escapeAttr(name);
		}
		var objectId = rowdata.objectId || "";
		var fileNo = useSubFileNo ? (rowdata.fileNo || "") : "";

		return '<a href="javascript:void(0);" class="sw-file-link"'
			+ ' data-object-id="' + escapeAttr(objectId) + '"'
			+ ' data-file-no="' + escapeAttr(fileNo) + '">'
			+ escapeAttr(name) + '</a>';
	}

	function initSwFileGrid(gridId, rows) {
		var $grid = $("#" + gridId);
		var useSubFileNo = gridId === "gridSwSubFile";
		var gridBodyHeight = useSubFileNo ? SUB_GRID_BODY_HEIGHT : MAIN_GRID_BODY_HEIGHT;
		var gridTotalHeight = gridBodyHeight + 38;
		$grid.jqGrid({
			datatype: "local",
			data: rows || [],
			colModel: [
				{ name: "fileNo", label: "파일순번", width: 90, align: "center", sortable: false },
				{
					name: "gradeNm",
					label: "적용등급",
					width: 110,
					align: "center",
					sortable: false,
					formatter: formatSwFileGrade
				},
				{
					name: "orgFileNm",
					label: "파일명",
					width: 790,
					sortable: false,
					formatter: function (cellValue, options, rowdata) {
						return formatSwFileName(cellValue, rowdata || {}, useSubFileNo);
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
				return $(e.target).closest(".sw-file-link").length === 0;
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
			initApprovalStatusGrid("gridSwApproverStatus", emptyRows);
			initApprovalStatusGrid("gridSwReviewerStatus", emptyRows);
			return;
		}
		callAjax(
			{ objectId: popupObjectId },
			"/inside/distribution/swRequest/approveStatusRows",
			function (response) {
				var rows = response && response.rows ? response.rows : [];
				var approverRows = [];
				var reviewerRows = [];
				$.each(rows, function (idx, row) {
					if ((row.approvalType || "").toUpperCase() === "REVIEWER") { reviewerRows.push(row); } else { approverRows.push(row); }
				});
				if (!approverRows.length) { approverRows = emptyRows; }
				if (!reviewerRows.length) { reviewerRows = emptyRows; }
				initApprovalStatusGrid("gridSwApproverStatus", approverRows);
				initApprovalStatusGrid("gridSwReviewerStatus", reviewerRows);
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
			"/inside/distribution/swRequest/saveApprovalComment",
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

	function downloadSelectedSwFile(gridId) {
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
			if (!isDownloadableSwFile(data || {})) {
				blockedFiles.push((data && (data.fileViewNm || data.orgFileNm || data.fileNm || data.fileName)) || "알 수 없는 파일");
				continue;
			}
			var url = "/inside/distribution/swRequest/downloadFile?objectId=" + encodeURIComponent(data.objectId);
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

		downloadSwFilesSequentially(requests, 0, blockedFiles);
	}

	function downloadSwFilesSequentially(requests, idx, blockedFiles) {
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
					downloadSwFilesSequentially(requests, idx + 1, blockedFiles);
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

	function isSwFileProcessing(status) {
		var s = String(status || "").trim().toUpperCase();
		if (!s) {
			return false;
		}
		if (s === "DONE" || s === "SUCCESS" || s === "COMPLETED" || s === "완료") {
			return false;
		}
		return s.indexOf("PROCESS") >= 0 || s === "PENDING" || s === "WAITING";
	}

	function getSwMainRowsForDisplay(rows) {
		var list = Array.isArray(rows) ? rows : [];
		if (!list.length) {
			return list;
		}
		var status = list[0] && list[0].processingStatus;
		if (!isSwFileProcessing(status)) {
			return list;
		}
		return [{
			fileNo: "",
			orgFileNm: "파일 등록중입니다.",
			fileSize: "",
			objectId: "",
			gradeCd: list[0].gradeCd || "",
			gradeNm: list[0].gradeNm || "",
			gradeLevel: list[0].gradeLevel
		}];
	}

	$(function () {
		mainFileRows = getSwMainRowsForDisplay(mainFileRows);
		if (mainFileRows.length && mainFileRows[0].orgFileNm === "파일 등록중입니다.") {
			subFileRows = [];
		}
		renderSwDocumentGrade(mainFileRows[0] || {});
		if ($("#gridSwApproverStatus, #gridSwReviewerStatus").length) {
			loadApprovalStatus();
		}
		initSwFileGrid("gridSwMainFile", mainFileRows);
		initSwFileGrid("gridSwSubFile", subFileRows);

		$(document)
			.off("click.swFilePopup", ".sw-file-popup .sw-file-link")
			.on("click.swFilePopup", ".sw-file-popup .sw-file-link", function (e) {
			e.preventDefault();
			var $row = $(this).closest('tr.jqgrow');
			var rowId = $row.attr('id');
			var tableId = $(this).closest('table.ui-jqgrid-btable').attr('id');
			if (rowId && tableId) {
				$("#" + tableId).jqGrid('setSelection', rowId, false);
			}
			var objectId = $(this).data("objectId");
			var fileNo = $(this).data("fileNo");
			openSwFileViewer(objectId, fileNo);
		});

		$(document)
			.off("click.swFilePopup", ".sw-file-popup .approval-comment-save")
			.on("click.swFilePopup", ".sw-file-popup .approval-comment-save", function (e) {
			e.preventDefault();
			saveApprovalComment($(this).data("gridId"), $(this).data("rowId"));
		});
	});
</script>

<style>
	.sw-file-popup .popupMeta {
		margin: 6px 0 14px;
		font-size: 14px;
		color: #2f3a55;
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 8px 20px;
	}

	.sw-file-popup .popupMetaItem {
		display: inline-flex;
		align-items: center;
		gap: 7px;
	}

	.sw-file-popup .sw-detail-hero p {
		margin: 0;
		color: #6b778c;
		font-size: 14px;
		line-height: 1.45;
		overflow-wrap: anywhere;
	}

	.sw-file-popup .sw-detail-summary {
		display: flex;
		justify-content: center;
		align-items: center;
		flex-wrap: wrap;
		gap: 8px;
		margin: 10px 0 14px;
	}

	.sw-file-popup .sw-detail-summary__chip {
		display: inline-flex;
		align-items: center;
		gap: 7px;
		min-height: 32px;
		padding: 5px 11px;
		border: 1px solid #dce5f0;
		border-radius: 999px;
		background: #f7faff;
		color: #536079;
		font-size: 12px;
		line-height: 1.3;
	}

	.sw-file-popup .sw-detail-summary__chip strong {
		max-width: 320px;
		color: #1f2a44;
		font-size: 13px;
		font-weight: 700;
		overflow-wrap: anywhere;
	}

	.sw-file-popup .sw-detail-panel {
		margin-top: 12px;
		overflow: hidden;
		border: 1px solid #dce3ee;
		border-radius: 10px;
		background: #fff;
	}

	.sw-file-popup .sw-detail-panel__header {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		gap: 12px;
		padding: 11px 14px;
		border-bottom: 1px solid #e7edf5;
		background: #f8fafd;
	}

	.sw-file-popup .sw-detail-panel__header h3 {
		margin: 0;
		color: #2f3a55;
		font-size: 14px;
		font-weight: 700;
		line-height: 1.3;
	}

	.sw-file-popup .sw-detail-panel__header span {
		color: #7a8599;
		font-size: 12px;
		line-height: 1.3;
	}

	.sw-file-popup .sw-detail-grid {
		display: grid;
		grid-template-columns: repeat(4, minmax(0, 1fr));
		gap: 8px;
		margin: 0;
		padding: 12px;
	}

	.sw-file-popup .sw-detail-item {
		min-width: 0;
		min-height: 56px;
		margin: 0;
		padding: 9px 10px;
		border: 1px solid #edf1f6;
		border-radius: 8px;
		background: #fbfcfe;
	}

	.sw-file-popup .sw-detail-item--span-2 {
		grid-column: span 2;
	}

	.sw-file-popup .sw-detail-item--span-3 {
		grid-column: span 3;
	}

	.sw-file-popup .sw-detail-item dt {
		margin: 0 0 5px;
		color: #798399;
		font-size: 11px;
		font-weight: 600;
		line-height: 1.2;
	}

	.sw-file-popup .sw-detail-item dd {
		margin: 0;
		color: #252d40;
		font-size: 13px;
		font-weight: 600;
		line-height: 1.4;
		overflow-wrap: anywhere;
		white-space: normal;
	}

	.sw-file-popup .sw-detail-state {
		display: inline-flex;
		align-items: center;
		min-height: 24px;
		padding: 3px 9px;
		border-radius: 999px;
		background: #eaf4ff;
		color: #075a9c;
		font-size: 12px;
		font-weight: 700;
		line-height: 1.3;
	}

	.sw-file-popup .sectionBlock {
		margin-top: 12px;
	}

	.sw-file-popup .popupCard {
		background: #fff;
		border: 1px solid #dce3ee;
		border-radius: 10px;
		padding: 0;
	}

	.sw-file-popup .dialogToolbar {
		height: 42px;
		padding: 0 14px;
		border-bottom: 1px solid #e6ebf3;
		background: #f8fafd;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.sw-file-popup .dialogToolbar .gridTitle {
		font-size: 14px;
		font-weight: 700;
		color: #2f3a55;
	}

	.sw-file-popup .dialogToolbar .listCount {
		margin-left: 8px;
		color: #6b778c;
		font-size: 12px;
	}

	.sw-file-popup .gridContainer {
		padding: 0;
	}

	.sw-file-popup .gridContainer .ui-jqgrid {
		width: 100% !important;
		height: auto !important;
		border: 0;
	}

	.sw-file-popup .gridContainer .ui-jqgrid-view,
	.sw-file-popup .gridContainer .ui-jqgrid-hdiv,
	.sw-file-popup .gridContainer .ui-jqgrid-bdiv {
		width: 100% !important;
	}

	.sw-file-popup .gridContainer .ui-jqgrid-view {
		height: auto !important;
	}

	.sw-file-popup .gridContainer .ui-jqgrid-bdiv {
		height: 174px !important;
		overflow-x: auto !important;
		overflow-y: auto !important;
	}

	.sw-file-popup .mainFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 52px !important;
	}

	.sw-file-popup .subFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 174px !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-hdiv {
		background: #fafafa;
		border-color: #909090;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th {
		background: #fafafa;
		color: #333333;
		font-weight: 700;
		border-color: #e0e0e0;
		height: 38px;
	}

	.sw-file-popup .ui-jqgrid tr.jqgrow td {
		height: 36px;
		border-color: #edf1f7;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-bdiv tr.ui-state-hover,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-bdiv tr:hover,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-bdiv tr:nth-child(odd).ui-state-hover {
		background: rgba(0, 127, 175, 0.25);
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th.ui-state-hover,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th:hover,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable .ui-jqgrid-labels th.ui-state-hover,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable .ui-jqgrid-labels th:hover {
		border-color: rgba(0, 127, 175, 0.25);
		background: rgba(0, 127, 175, 0.25);
		color: #333333;
	}

	.sw-file-popup .ui-jqgrid input.cbox,
	.sw-file-popup .ui-jqgrid input.cbox:hover,
	.sw-file-popup .ui-jqgrid input.cbox:focus,
	.sw-file-popup .ui-jqgrid input.cbox:focus-visible,
	.sw-file-popup .ui-jqgrid input.cbox:active {
		box-shadow: none !important;
		outline: none !important;
	}

	@media (max-width: 900px) {
		.sw-file-popup .sw-detail-grid {
			grid-template-columns: repeat(2, minmax(0, 1fr));
		}

		.sw-file-popup .sw-detail-item--span-3 {
			grid-column: span 2;
		}
	}

	@media (max-width: 600px) {
		.sw-file-popup .sw-detail-grid {
			grid-template-columns: minmax(0, 1fr);
		}

		.sw-file-popup .sw-detail-item--span-2,
		.sw-file-popup .sw-detail-item--span-3 {
			grid-column: auto;
		}

		.sw-file-popup .sw-detail-panel__header {
			align-items: flex-start;
			flex-direction: column;
			gap: 3px;
		}
	}
</style>

<div class="dialogContent sw-file-popup popup-base popup-actions-center popup-type-form-grid">
	<div class="popupHero sw-detail-hero">
		<h2>기술자료 상세</h2>
		<p><c:out value="${empty documentInfo.swNm ? '제목 미등록' : documentInfo.swNm}" /></p>
	</div>

	<div class="sw-detail-summary" role="list" aria-label="기술자료 요약">
		<span class="sw-detail-summary__chip" role="listitem">
			CCB번호
			<strong><c:out value="${empty documentInfo.swNo ? '-' : documentInfo.swNo}" /></strong>
		</span>
		<span class="sw-detail-summary__chip" role="listitem">
			문서등급:
			<span id="swPopupDocumentGrade"
				  class="sw-document-grade-display document-grade-badge document-grade-badge--unassigned">미지정</span>
		</span>
		<span class="sw-detail-summary__chip" role="listitem">
			진행상태
			<strong><c:out value="${empty documentInfo.status ? '-' : documentInfo.status}" /></strong>
		</span>
	</div>

	<section class="sw-detail-panel" aria-labelledby="swDetailBasicTitle">
		<div class="sw-detail-panel__header">
			<h3 id="swDetailBasicTitle">문서 정보</h3>
			<span>목록에서 숨겨진 기술자료 속성을 함께 표시합니다.</span>
		</div>
		<dl class="sw-detail-grid">
			<div class="sw-detail-item">
				<dt>CCB번호</dt>
				<dd><c:out value="${empty documentInfo.swNo ? '-' : documentInfo.swNo}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-3">
				<dt>CCB제목</dt>
				<dd><c:out value="${empty documentInfo.swNm ? '-' : documentInfo.swNm}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-2">
				<dt>자료분류</dt>
				<dd><c:out value="${empty documentInfo.classificationPath ? '-' : documentInfo.classificationPath}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>사업단계</dt>
				<dd><c:out value="${empty documentInfo.businessTypeNm ? '-' : documentInfo.businessTypeNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>파일유형</dt>
				<dd><c:out value="${empty documentInfo.distributeTypeNm ? '-' : documentInfo.distributeTypeNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>Revision</dt>
				<dd><c:out value="${empty documentInfo.revNo ? '-' : documentInfo.revNo}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>SW버전</dt>
				<dd><c:out value="${empty documentInfo.swVersionNo ? '-' : documentInfo.swVersionNo}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>SW분류</dt>
				<dd><c:out value="${empty documentInfo.swTypeNm ? '-' : documentInfo.swTypeNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>파일수</dt>
				<dd><c:out value="${empty documentInfo.fileCount ? '0' : documentInfo.fileCount}" />건</dd>
			</div>
			<div class="sw-detail-item">
				<dt>기종</dt>
				<dd><c:out value="${empty documentInfo.productNm ? '-' : documentInfo.productNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>사업장</dt>
				<dd><c:out value="${empty documentInfo.businessAreaNm ? '-' : documentInfo.businessAreaNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>CCB개최일</dt>
				<dd><c:out value="${empty documentInfo.ccbDate ? '-' : documentInfo.ccbDate}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>생성일</dt>
				<dd><c:out value="${empty documentInfo.createDt ? '-' : documentInfo.createDt}" /></dd>
			</div>
		</dl>
	</section>

	<section class="sw-detail-panel" aria-labelledby="swDetailHistoryTitle">
		<div class="sw-detail-panel__header">
			<h3 id="swDetailHistoryTitle">등록·변경 이력</h3>
			<span>등록, 수정, 인터페이스 및 승인일 정보를 표시합니다.</span>
		</div>
		<dl class="sw-detail-grid">
			<div class="sw-detail-item">
				<dt>의뢰자</dt>
				<dd><c:out value="${empty documentInfo.registerUser ? '-' : documentInfo.registerUser}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>등록자</dt>
				<dd><c:out value="${empty documentInfo.insertUserNm ? '-' : documentInfo.insertUserNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>등록팀</dt>
				<dd><c:out value="${empty documentInfo.insertDeptNm ? '-' : documentInfo.insertDeptNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>등록일</dt>
				<dd><c:out value="${empty documentInfo.insertDt ? '-' : documentInfo.insertDt}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>수정자</dt>
				<dd><c:out value="${empty documentInfo.updateUserNm ? '-' : documentInfo.updateUserNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>수정일</dt>
				<dd><c:out value="${empty documentInfo.updateDt ? '-' : documentInfo.updateDt}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>Interface일</dt>
				<dd><c:out value="${empty documentInfo.interfaceDt ? '-' : documentInfo.interfaceDt}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>CO번호</dt>
				<dd><c:out value="${empty documentInfo.ecnNo ? '-' : documentInfo.ecnNo}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>CO담당자</dt>
				<dd><c:out value="${empty documentInfo.ecnUserNm ? '-' : documentInfo.ecnUserNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>규격화승인일</dt>
				<dd><c:out value="${empty documentInfo.stdGappDt ? '-' : documentInfo.stdGappDt}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>기술변경승인일</dt>
				<dd><c:out value="${empty documentInfo.changeGappDt ? '-' : documentInfo.changeGappDt}" /></dd>
			</div>
		</dl>
	</section>

	<section class="sw-detail-panel" aria-labelledby="swDetailSecurityTitle">
		<div class="sw-detail-panel__header">
			<h3 id="swDetailSecurityTitle">보안·승인 정보</h3>
			<span>등급, 유효성 및 승인 참여 정보를 표시합니다.</span>
		</div>
		<dl class="sw-detail-grid">
			<div class="sw-detail-item">
				<dt>문서등급</dt>
				<dd>
					<span class="sw-document-grade-display document-grade-badge document-grade-badge--unassigned">미지정</span>
				</dd>
			</div>
			<div class="sw-detail-item">
				<dt>진행상태</dt>
				<dd><span class="sw-detail-state"><c:out value="${empty documentInfo.status ? '-' : documentInfo.status}" /></span></dd>
			</div>
			<div class="sw-detail-item">
				<dt>처리상태</dt>
				<dd><span class="sw-detail-state"><c:out value="${empty documentInfo.processingStatusNm ? '-' : documentInfo.processingStatusNm}" /></span></dd>
			</div>
			<div class="sw-detail-item">
				<dt>방산기술</dt>
				<dd><c:out value="${empty documentInfo.protectYnNm ? '미지정' : documentInfo.protectYnNm}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt>유효본</dt>
				<dd><c:out value="${empty documentInfo.validTypeNm ? '-' : documentInfo.validTypeNm}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-3">
				<dt>승인자</dt>
				<dd><c:out value="${empty documentInfo.approver ? '-' : documentInfo.approver}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-2">
				<dt>참여자</dt>
				<dd><c:out value="${empty documentInfo.reviewerUser ? '-' : documentInfo.reviewerUser}" /></dd>
			</div>
		</dl>
	</section>

	<!-- <div class="section popupCard sectionBlock">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">보드멤버 승인 상태</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="saveApprovalCommentByGrid('gridSwApproverStatus')">저장</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridSwApproverStatus"></table>
		</div>
	</div>

	<div class="section popupCard sectionBlock">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">참여자 승인 상태</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="saveApprovalCommentByGrid('gridSwReviewerStatus')">저장</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridSwReviewerStatus"></table>
		</div>
	</div> -->

	<div class="section popupCard sectionBlock mainFileSection">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">주파일 정보</span>
				<span class="listCount">총 ${mainFileList.size()}건</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="downloadSelectedSwFile('gridSwMainFile')">다운로드</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridSwMainFile"></table>
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
					onclick="downloadSelectedSwFile('gridSwSubFile')">다운로드</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridSwSubFile"></table>
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













