<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<script>
	var mainFileRows = ${ empty mainFileJson ?'[]': mainFileJson };
	var popupRequestNo = "${param.requestNo}";
	var popupObjectId = "${objectId}";
	var popupPeerReviewNo = "${peerReviewNo}";
	var MAIN_GRID_BODY_HEIGHT = 52;
	var APPROVAL_GRID_BODY_HEIGHT = 140;

	function pickValue(obj) {
		for (var i = 1; i < arguments.length; i++) {
			var k = arguments[i];
			if (obj && obj[k] !== undefined && obj[k] !== null && String(obj[k]) !== "") {
				return obj[k];
			}
		}
		return "";
	}

	function normalizeMainFileRows(rows) {
		var source = rows || [];
		var normalized = [];
		for (var i = 0; i < source.length; i++) {
			var r = source[i] || {};
			normalized.push({
				objectId: pickValue(r, "objectId", "objectid", "OBJECTID", "OBJECT_ID", "object_id"),
				fileNo: pickValue(r, "fileNo", "fileno", "FILENO", "FILE_NO", "file_no", "rnum", "RNUM", 1) || 1,
				orgFileNm: pickValue(r, "orgFileNm", "orgfilenm", "ORGFILENM", "ORG_FILE_NM", "org_file_nm", "fileNm", "filenm", "FILE_NM", "peerreviewNm", "peerreviewnm", "PEERREVIEW_NM"),
				fileSize: pickValue(r, "fileSize", "filesize", "FILESIZE", "FILE_SIZE", "file_size", "0"),
				fileExists: pickValue(r, "fileExists", "fileexists", "FILE_EXISTS", "file_exists"),
				processingStatus: pickValue(r, "processingStatus", "processingstatus", "PROCESSING_STATUS", "processing_status")
			});
		}
		return normalized;
	}

	function isPeerReviewFileProcessing(status) {
		var s = String(status || "").trim().toUpperCase();
		if (!s) {
			return false;
		}
		if (s === "DONE" || s === "SUCCESS" || s === "COMPLETED" || s === "완료") {
			return false;
		}
		return s.indexOf("PROCESS") >= 0 || s === "PENDING" || s === "WAITING";
	}

	function getPeerReviewMainRowsForDisplay(rows) {
		var list = Array.isArray(rows) ? rows : [];
		if (!list.length) {
			return list;
		}
		var status = list[0] && list[0].processingStatus;
		if (!isPeerReviewFileProcessing(status)) {
			return list;
		}
		return [{
			objectId: "",
			fileNo: "",
			orgFileNm: "파일 등록중입니다.",
			fileSize: ""
		}];
	}

	function escapeAttr(value) {
		return String(value === undefined || value === null ? "" : value)
			.replace(/&/g, "&amp;")
			.replace(/"/g, "&quot;")
			.replace(/'/g, "&#39;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;");
	}

	function openPeerReviewFileViewer(objectId, fileNo) {
		if (!objectId) {
			alertMessage("파일 정보를 찾을 수 없습니다.");
			return;
		}
		openFile("OBJECT", "PEERREVIEW", popupRequestNo || null, objectId, fileNo || null, "N");
	}

	function isDownloadablePeerReviewFile(rowdata) {
		var objectId = rowdata && rowdata.objectId ? String(rowdata.objectId) : "";
		var orgFileNm = rowdata && rowdata.orgFileNm ? String(rowdata.orgFileNm) : "";
		if (!objectId) {
			return false;
		}
		if (!orgFileNm || orgFileNm === "파일 등록중입니다.") {
			return false;
		}
		if (rowdata.fileExists === false || String(rowdata.fileExists).toLowerCase() === "false") {
			return false;
		}
		return true;
	}

	function formatPeerReviewFileName(cellValue, rowdata) {
		var name = cellValue || "";
		if (name === "") {
			return "";
		}
		if (!isDownloadablePeerReviewFile(rowdata || {})) {
			return escapeAttr(name);
		}
		var objectId = rowdata.objectId || "";
		var fileNo = rowdata.fileNo || "";
		return '<a href="javascript:void(0);" class="peerreview-file-link"'
			+ ' data-object-id="' + escapeAttr(objectId) + '"'
			+ ' data-file-no="' + escapeAttr(fileNo) + '">'
			+ escapeAttr(name) + '</a>';
	}

	function initPeerReviewFileGrid(gridId, rows) {
		var $grid = $("#" + gridId);
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
						return formatPeerReviewFileName(cellValue, rowdata || {});
					}
				},
				{ name: "fileSize", label: "파일크기(Byte)", width: 170, align: "right", sortable: false }
			],
			height: MAIN_GRID_BODY_HEIGHT,
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
				return $(e.target).closest(".peerreview-file-link").length === 0;
			}
		});
	}

	function formatCommentCell(cellValue, options, rowdata, gridId) {
		var comment = cellValue && cellValue !== "-" ? cellValue : "승인하였습니다";
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
				{
					name: "comment",
					label: "코멘트",
					width: 360,
					sortable: false,
					formatter: function (cellValue, options, rowdata) {
						return formatCommentCell(cellValue, options, rowdata, gridId);
					}
				}
			],
			height: APPROVAL_GRID_BODY_HEIGHT,
			autowidth: true,
			shrinkToFit: true,
			forceFit: true,
			scrollOffset: 0,
			rowNum: 9999,
			viewrecords: false,
			loadonce: true
		});
	}

	function loadApprovalStatus() {
		var emptyRows = [{ approver: "-", status: "-", comment: "-", editable: "N" }];
		if (!popupObjectId) {
			initApprovalStatusGrid("gridPeerReviewApproverStatus", emptyRows);
			initApprovalStatusGrid("gridPeerReviewReviewerStatus", emptyRows);
			return;
		}
		callAjax(
			{ objectId: popupObjectId },
			"/inside/distribution/peerReview/approveStatusRows",
			function (response) {
				var rows = response && response.rows ? response.rows : [];
				var approverRows = [];
				var reviewerRows = [];
				$.each(rows, function (_, row) {
					if ((row.approvalType || "").toUpperCase() === "REVIEWER") {
						reviewerRows.push(row);
					} else {
						approverRows.push(row);
					}
				});
				if (!approverRows.length) { approverRows = emptyRows; }
				if (!reviewerRows.length) { reviewerRows = emptyRows; }
				initApprovalStatusGrid("gridPeerReviewApproverStatus", approverRows);
				initApprovalStatusGrid("gridPeerReviewReviewerStatus", reviewerRows);
			},
			"json"
		);
	}

	function saveApprovalComment(gridId, rowId) {
		var $input = $('.approval-comment-input[data-grid-id="' + gridId + '"][data-row-id="' + rowId + '"]');
		if (!$input.length) { return; }
		var comment = $.trim($input.val());
		if (!comment) {
			comment = "승인하였습니다";
			$input.val(comment);
		}
		callAjax(
			{ objectId: popupObjectId, comment: comment },
			"/inside/distribution/peerReview/saveApprovalComment",
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

	function downloadSelectedPeerReviewFile(gridId) {
		var $grid = $("#" + gridId);
		var selectedRows = $grid.jqGrid("getGridParam", "selarrrow") || [];
		if (!selectedRows.length) {
			var singleRow = $grid.jqGrid("getGridParam", "selrow");
			if (singleRow) {
				selectedRows = [singleRow];
			}
		}
		if (!selectedRows.length) {
			alertMessage(g_msg("msg.noSelectedItem"));
			return;
		}

		var requests = [];
		var blockedFiles = [];
		$.each(selectedRows, function (idx, rowId) {
			var row = $grid.jqGrid("getLocalRow", rowId) || $grid.jqGrid("getRowData", rowId);
			if (!isDownloadablePeerReviewFile(row || {})) {
				blockedFiles.push((row && (row.orgFileNm || row.fileViewNm || row.fileNm || row.fileName)) || "알 수 없는 파일");
				return;
			}
			var objectId = row.objectId;
			var fileNo = row && row.fileNo ? row.fileNo : "";

			var url = "/inside/distribution/peerReview/downloadFile?objectId=" + encodeURIComponent(objectId);
			if (fileNo) {
				url += "&fileNo=" + encodeURIComponent(fileNo);
			}
			if (popupPeerReviewNo) {
				url += "&peerReviewNo=" + encodeURIComponent(popupPeerReviewNo);
			}
			url += "&watermarkYn=Y";

			requests.push({
				url: url,
				fileName: (row && (row.fileViewNm || row.orgFileNm || row.fileNm || row.fileName)) ? (row.fileViewNm || row.orgFileNm || row.fileNm || row.fileName) : ""
			});
		});

		if (!requests.length) {
			alertMessage("다운로드 가능한 파일이 없습니다.");
			return;
		}

		downloadPeerReviewFilesSequentially(requests, 0, blockedFiles);
	}

	function downloadPeerReviewFilesSequentially(requests, idx, blockedFiles) {
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
					downloadPeerReviewFilesSequentially(requests, idx + 1, blockedFiles);
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

	$(function () {
		mainFileRows = normalizeMainFileRows(mainFileRows);
		mainFileRows = getPeerReviewMainRowsForDisplay(mainFileRows);
		loadApprovalStatus();
		initPeerReviewFileGrid("gridPeerReviewMainFile", mainFileRows);

		$(document).on("click", ".peerreview-file-popup .peerreview-file-link", function (e) {
			e.preventDefault();
			var objectId = $(this).data("objectId");
			var fileNo = $(this).data("fileNo");
			openPeerReviewFileViewer(objectId, fileNo);
		});
	});
</script>

<style>
	.peerreview-file-popup .popupMeta {
		margin: 6px 0 14px;
		font-size: 14px;
		color: #2f3a55;
	}
	.peerreview-file-popup .sectionBlock { margin-top: 12px; }
	.peerreview-file-popup .popupCard {
		background: #fff;
		border: 1px solid #dce3ee;
		border-radius: 10px;
		padding: 0;
	}
	.peerreview-file-popup .dialogToolbar {
		height: 42px;
		padding: 0 14px;
		border-bottom: 1px solid #e6ebf3;
		background: #f8fafd;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.peerreview-file-popup .dialogToolbar .gridTitle {
		font-size: 14px;
		font-weight: 700;
		color: #2f3a55;
	}
	.peerreview-file-popup .dialogToolbar .listCount {
		margin-left: 8px;
		color: #6b778c;
		font-size: 12px;
	}
	.peerreview-file-popup .gridContainer { padding: 0; }
	.peerreview-file-popup .gridContainer .ui-jqgrid {
		width: 100% !important;
		height: auto !important;
		border: 0;
	}
	.peerreview-file-popup .gridContainer .ui-jqgrid-view,
	.peerreview-file-popup .gridContainer .ui-jqgrid-hdiv,
	.peerreview-file-popup .gridContainer .ui-jqgrid-bdiv {
		width: 100% !important;
	}
	.peerreview-file-popup .gridContainer .ui-jqgrid-bdiv {
		overflow-x: auto !important;
		overflow-y: auto !important;
	}
	.peerreview-file-popup .mainFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 52px !important;
	}
	.peerreview-file-popup .ui-jqgrid .ui-jqgrid-hdiv {
		background: #fafafa;
		border-color: #909090;
	}
	.peerreview-file-popup .ui-jqgrid .ui-jqgrid-htable th {
		background: #fafafa;
		color: #333333;
		font-weight: 700;
		border-color: #e0e0e0;
		height: 38px;
	}
	.peerreview-file-popup .ui-jqgrid tr.jqgrow td {
		height: 36px;
		border-color: #edf1f7;
	}
</style>

<div class="dialogContent peerreview-file-popup popup-base popup-actions-center popup-type-form-grid">
	<div class="popupHero">
		<h2>파일 정보</h2>
	</div>

	<div class="popupMeta">
		문서번호: <strong>${peerReviewNo}</strong>
	</div>

	<div class="section popupCard sectionBlock">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">검토자 승인 상태</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn" onclick="saveApprovalCommentByGrid('gridPeerReviewApproverStatus')">저장</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridPeerReviewApproverStatus"></table>
		</div>
	</div>

	<div class="section popupCard sectionBlock mainFileSection">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle">주파일 정보</span>
				<span class="listCount">총 ${mainFileList.size()}건</span>
			</div>
			<div class="right">
				<button type="button" class="ui-button ui-corner-all bottomBtn" onclick="downloadSelectedPeerReviewFile('gridPeerReviewMainFile')">다운로드</button>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridPeerReviewMainFile"></table>
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
