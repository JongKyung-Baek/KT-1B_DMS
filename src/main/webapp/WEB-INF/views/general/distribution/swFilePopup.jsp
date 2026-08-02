<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<spring:message code="feature.techDetail.untitled" text="제목 미등록" var="untitledText" />

<script>
	var mainFileRows = ${ empty mainFileJson ?'[]': mainFileJson };
	var subFileRows = ${ empty subFileJson ?'[]': subFileJson };
	var popupRequestNo = "${param.requestNo}";
	var MAIN_GRID_BODY_HEIGHT = 56;
	var SUB_GRID_BODY_HEIGHT = 168;

	function swFileMessage(key, fallback) {
		var args = Array.prototype.slice.call(arguments, 2);
		var message = fallback || key;
		if (window.SdmsI18n && typeof window.SdmsI18n.t === "function") {
			message = window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
		}
		return args.reduce(function(message, value, index) {
			return String(message).replace(new RegExp("\\{" + index + "\\}", "g"), value);
		}, message);
	}

	function swProcessingFileLabel() {
		return swFileMessage("feature.techDetail.file.processing", "파일 등록중입니다.");
	}

	function isSwProcessingFileLabel(value) {
		return value === swProcessingFileLabel() || value === "파일 등록중입니다.";
	}

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
		var sourceGradeName = $.trim(String(cellValue || row.gradeNm || ""));
		var gradeCode = $.trim(String(row.gradeCd || ""));
		var gradeName = sourceGradeName;
		switch (gradeCode.toUpperCase()) {
			case "GENERAL":
				gradeName = swFileMessage("feature.documentGrade.general", "일반");
				break;
			case "INTERNAL":
				gradeName = swFileMessage("feature.documentGrade.internal", "사내한");
				break;
			case "RESTRICTED":
				gradeName = swFileMessage("feature.documentGrade.restricted", "제한");
				break;
			case "CONFIDENTIAL":
				gradeName = swFileMessage("feature.documentGrade.confidential", "대외비");
				break;
			case "UNASSIGNED":
				gradeName = swFileMessage("feature.documentGrade.unassigned", "미지정");
				break;
			default:
				break;
		}
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
			name: gradeName || swFileMessage("feature.documentGrade.unassigned", "미지정"),
			tone: tone,
			title: gradeName
				? swFileMessage("feature.techDetail.grade.applied", "적용등급") + ": " + gradeName
					+ (gradeCode ? " (" + gradeCode + (levelText ? ", " + levelText : "") + ")" : "")
				: swFileMessage("feature.techDetail.grade.notAssigned", "적용할 문서등급이 지정되지 않았습니다.")
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
			alertMessage(swFileMessage("feature.techDetail.file.notFound", "파일 정보를 찾을 수 없습니다."));
			return;
		}
		openFile("OBJECT", "SW", popupRequestNo || null, objectId, fileNo || null, "N");
	}

	function isAvailableSwFile(rowdata) {
		if (!rowdata || !rowdata.objectId) {
			return false;
		}
		if (!rowdata.orgFileNm || isSwProcessingFileLabel(rowdata.orgFileNm)) {
			return false;
		}
		if (rowdata.fileExists === false || String(rowdata.fileExists).toLowerCase() === "false") {
			return false;
		}
		return true;
	}

	function isDownloadableSwFile(rowdata) {
		return isAvailableSwFile(rowdata)
			&& (rowdata.downloadAllowed === true || String(rowdata.downloadAllowed).toLowerCase() === "true");
	}

	function isSwPdfFile(rowdata, fallbackName) {
		var fileName = (rowdata && (rowdata.orgFileNm || rowdata.fileViewNm || rowdata.fileNm || rowdata.fileName))
			|| fallbackName || "";
		return /\.pdf$/i.test(String(fileName).trim());
	}

	function isSwStepFile(rowdata, fallbackName) {
		var fileName = (rowdata && (rowdata.orgFileNm || rowdata.fileViewNm || rowdata.fileNm || rowdata.fileName))
			|| fallbackName || "";
		return /\.(?:stp|step)$/i.test(String(fileName).trim());
	}

	function isSwViewerPreviewFile(rowdata, fallbackName) {
		return isSwPdfFile(rowdata, fallbackName) || isSwStepFile(rowdata, fallbackName);
	}

	function formatSwFileName(cellValue, rowdata, useSubFileNo) {
		var name = isSwProcessingFileLabel(cellValue) ? swProcessingFileLabel() : (cellValue || "");
		if (name === "") {
			return "";
		}
		if (!isAvailableSwFile(rowdata || {})) {
			return escapeAttr(name);
		}
		if (!isSwViewerPreviewFile(rowdata || {}, name)) {
			return '<span class="sw-file-name-static">' + escapeAttr(name) + '</span>'
				+ '<span class="sw-file-preview-unavailable">'
				+ escapeAttr(swFileMessage("feature.techDetail.file.previewUnavailable", "미리보기 미지원"))
				+ '</span>';
		}
		var objectId = rowdata.objectId || "";
		var fileNo = rowdata.fileNo || "";

		return '<a href="javascript:void(0);" class="sw-file-link"'
			+ ' data-object-id="' + escapeAttr(objectId) + '"'
			+ ' data-file-no="' + escapeAttr(fileNo) + '">'
			+ escapeAttr(name) + '</a>';
	}

	function initSwFileGrid(gridId, rows) {
		var $grid = $("#" + gridId);
		var useSubFileNo = gridId === "gridSwSubFile";
		var gridBodyHeight = useSubFileNo ? SUB_GRID_BODY_HEIGHT : MAIN_GRID_BODY_HEIGHT;
		var gridTotalHeight = gridBodyHeight + 42;
		$grid.jqGrid({
			datatype: "local",
			data: rows || [],
			colModel: [
				{
					name: "fileNo",
					label: swFileMessage("feature.techDetail.grid.fileNo", "파일순번"),
					width: 90,
					align: "center",
					sortable: false
				},
				{
					name: "gradeNm",
					label: swFileMessage("feature.techDetail.grid.grade", "적용등급"),
					width: 110,
					align: "center",
					sortable: false,
					formatter: formatSwFileGrade
				},
				{
					name: "orgFileNm",
					label: swFileMessage("feature.techDetail.grid.fileName", "파일명"),
					width: 790,
					sortable: false,
					formatter: function (cellValue, options, rowdata) {
						return formatSwFileName(cellValue, rowdata || {}, useSubFileNo);
					}
				},
				{
					name: "fileSize",
					label: swFileMessage("feature.techDetail.grid.fileSize", "파일크기(Byte)"),
					width: 170,
					align: "right",
					sortable: false
				}
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
				if ($(e.target).closest(".sw-file-link").length > 0) {
					return false;
				}
				return isDownloadableSwFile($grid.jqGrid("getLocalRow", rowId) || {});
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
			alertMessage(swFileMessage("feature.common.validation.noSelection", "선택된 항목이 없습니다."));
			return;
		}

		var requests = [];
		var blockedFiles = [];
		for (var i = 0; i < selectedRows.length; i++) {
			var data = $grid.jqGrid('getLocalRow', selectedRows[i]) || $grid.jqGrid('getRowData', selectedRows[i]);
			if (!isDownloadableSwFile(data || {})) {
				blockedFiles.push((data && (data.fileViewNm || data.orgFileNm || data.fileNm || data.fileName))
					|| swFileMessage("feature.techDetail.file.unknown", "알 수 없는 파일"));
				continue;
			}
			var url = "/general/distribution/swRequest/downloadFile?objectId=" + encodeURIComponent(data.objectId);
			if (data.fileNo !== undefined && data.fileNo !== null && String(data.fileNo) !== "") {
				url += "&fileNo=" + encodeURIComponent(data.fileNo);
			}
			requests.push({
				url: url,
				fileName: data.fileViewNm || data.orgFileNm || data.fileNm || data.fileName || ""
			});
		}
		if (!requests.length) {
			alertMessage(swFileMessage("feature.techDetail.download.noneAvailable", "다운로드 가능한 파일이 없습니다."));
			return;
		}

		downloadSwFilesSequentially(requests, 0, blockedFiles);
	}

	function downloadSwFilesSequentially(requests, idx, blockedFiles) {
		if (!requests || idx >= requests.length) {
			if (blockedFiles && blockedFiles.length) {
				alertMessage(swFileMessage(
					"feature.techDetail.download.excluded",
					"다운로드 불가능한 파일 {0}건은 제외되었습니다.",
					blockedFiles.length
				));
			}
			return;
		}
		var req = requests[idx];
		fetch(req.url, { method: "GET", credentials: "same-origin" })
			.then(function (response) {
				if (!response.ok) {
					if (response.status === 403) {
						blockedFiles.push(req.fileName
							|| swFileMessage("feature.techDetail.file.unknown", "알 수 없는 파일"));
						return;
					}
					alertMessage(swFileMessage("feature.techDetail.download.failed", "다운로드에 실패했습니다."));
					return;
				}
				var disposition = response.headers.get("Content-Disposition") || "";
				return response.blob().then(function (blob) {
					var downloadName = extractFileNameFromDisposition(disposition) || "download.bin";
					triggerBlobDownload(blob, downloadName);
				});
			})
			.catch(function () {
				alertMessage(swFileMessage("feature.techDetail.download.failed", "다운로드에 실패했습니다."));
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
			orgFileNm: swProcessingFileLabel(),
			fileSize: "",
			objectId: "",
			gradeCd: list[0].gradeCd || "",
			gradeNm: list[0].gradeNm || "",
			gradeLevel: list[0].gradeLevel
		}];
	}

	$(function () {
		mainFileRows = getSwMainRowsForDisplay(mainFileRows);
		if (mainFileRows.length && isSwProcessingFileLabel(mainFileRows[0].orgFileNm)) {
			subFileRows = [];
		}
		renderSwDocumentGrade(mainFileRows[0] || {});
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

	.sw-file-popup .sectionBlock {
		margin-top: 12px;
	}

	.sw-file-popup .popupCard {
		background: #fff;
		border: 1px solid #e7e5eb;
		border-radius: 14px;
		padding: 0;
		overflow: hidden;
		box-shadow: 0 5px 20px rgba(47, 43, 61, 0.055);
	}

	.sw-file-popup .dialogToolbar {
		height: 42px;
		padding: 0 14px;
		border-bottom: 1px solid #e7e5eb;
		background: #fff;
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
		height: 168px !important;
		overflow-x: auto !important;
		overflow-y: auto !important;
		background: #fff;
		scrollbar-color: #aaa7b0 transparent;
		scrollbar-width: thin;
	}

	.sw-file-popup .mainFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 56px !important;
	}

	.sw-file-popup .subFileSection .gridContainer .ui-jqgrid-bdiv {
		height: 168px !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-hdiv {
		border-bottom: 1px solid #ddd9e2 !important;
		background: #f4f2f6 !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable {
		border-collapse: separate;
		border-spacing: 0;
		background: #f4f2f6;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th.ui-th-column,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th.ui-th-column-header {
		height: 42px !important;
		min-height: 42px !important;
		padding: 9px 11px !important;
		border: 0 !important;
		background: #f4f2f6 !important;
		color: #5d596c !important;
		font-size: 11px !important;
		font-weight: 800 !important;
		text-align: center !important;
		vertical-align: middle !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th > div,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th > div.ui-jqgrid-sortable,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-htable th div:not(.clearfix) {
		min-height: 20px !important;
		padding: 0 !important;
		color: inherit !important;
		font-size: 11px !important;
		font-weight: 800 !important;
		line-height: 20px !important;
		text-align: center !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-btable {
		border-collapse: separate;
		border-spacing: 0;
		background: #fff;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-btable tr.jqgrow td {
		height: 56px !important;
		padding: 9px 11px !important;
		border-top: 0 !important;
		border-right: 0 !important;
		border-bottom: 1px solid #efedf1 !important;
		border-left: 0 !important;
		background: #fff !important;
		color: #4b465c !important;
		font-size: 11px !important;
		line-height: 1.42 !important;
		text-align: center !important;
		vertical-align: middle !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-btable tr.jqgrow:last-child td {
		border-bottom: 0 !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-btable tr.jqgrow:hover td {
		background: #f6fafe !important;
	}

	.sw-file-popup .ui-jqgrid .ui-jqgrid-btable tr.ui-state-highlight td,
	.sw-file-popup .ui-jqgrid .ui-jqgrid-btable tr[aria-selected="true"] td {
		background: #eaf3fb !important;
		color: #243b53 !important;
	}

	.sw-file-popup .sw-file-link {
		color: #034c8c !important;
		font-weight: 800;
		text-decoration: none;
	}

	.sw-file-popup .sw-file-link:hover,
	.sw-file-popup .sw-file-link:focus {
		color: #023e73 !important;
		text-decoration: underline;
	}

	.sw-file-popup .sw-file-name-static {
		display: inline-block;
		max-width: calc(100% - 118px);
		vertical-align: middle;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.sw-file-popup .sw-file-preview-unavailable {
		display: inline-flex;
		align-items: center;
		margin-left: 8px;
		padding: 2px 7px;
		border: 1px solid #d9e1ec;
		border-radius: 999px;
		background: #f6f8fb;
		color: #6b778c;
		font-size: 11px;
		font-weight: 700;
		line-height: 1.35;
		vertical-align: middle;
		white-space: nowrap;
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
		<h2><spring:message code="feature.techDetail.title" text="기술자료 상세" /></h2>
		<p><c:out value="${empty documentInfo.swNm ? untitledText : documentInfo.swNm}" /></p>
	</div>

	<div class="sw-detail-summary" role="list"
		 aria-label="<spring:message code='feature.techDetail.summary.aria' text='기술자료 요약' />">
		<span class="sw-detail-summary__chip" role="listitem">
			<spring:message code="feature.techDetail.transmittalNo" text="자료번호" />
			<strong><c:out value="${empty documentInfo.swNo ? '-' : documentInfo.swNo}" /></strong>
		</span>
		<span class="sw-detail-summary__chip" role="listitem">
			<spring:message code="feature.techDetail.documentGrade" text="문서등급" />:
			<span id="swPopupDocumentGrade"
				  class="sw-document-grade-display document-grade-badge document-grade-badge--unassigned"><spring:message
					code="feature.documentGrade.unassigned" text="미지정" /></span>
		</span>
	</div>

	<section class="sw-detail-panel" aria-labelledby="swDetailBasicTitle">
		<div class="sw-detail-panel__header">
			<h3 id="swDetailBasicTitle"><spring:message code="feature.techDetail.documentInfo.title" text="문서 정보" /></h3>
			<span><spring:message code="feature.techDetail.documentInfo.description"
					text="기술자료의 핵심 식별 정보를 표시합니다." /></span>
		</div>
		<dl class="sw-detail-grid">
			<div class="sw-detail-item">
				<dt><spring:message code="feature.techDetail.transmittalNo" text="자료번호" /></dt>
				<dd><c:out value="${empty documentInfo.swNo ? '-' : documentInfo.swNo}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-3">
				<dt><spring:message code="feature.techDetail.requestName" text="의뢰명" /></dt>
				<dd><c:out value="${empty documentInfo.swNm ? '-' : documentInfo.swNm}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-3">
				<dt><spring:message code="feature.techDetail.classification" text="자료분류" /></dt>
				<dd><c:out value="${empty documentInfo.classificationPath ? '-' : documentInfo.classificationPath}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt><spring:message code="feature.techDetail.fileCount" text="파일수" /></dt>
				<dd><spring:message code="feature.common.count.items" text="{0}건"
						arguments="${empty documentInfo.fileCount ? '0' : documentInfo.fileCount}" /></dd>
			</div>
		</dl>
	</section>

	<section class="sw-detail-panel" aria-labelledby="swDetailHistoryTitle">
		<div class="sw-detail-panel__header">
			<h3 id="swDetailHistoryTitle"><spring:message code="feature.techDetail.registrationInfo.title" text="의뢰·등록 정보" /></h3>
			<span><spring:message code="feature.techDetail.registrationInfo.description"
					text="의뢰자와 등록 정보를 표시합니다." /></span>
		</div>
		<dl class="sw-detail-grid">
			<div class="sw-detail-item">
				<dt><spring:message code="feature.techDetail.requester" text="의뢰자" /></dt>
				<dd><c:out value="${empty documentInfo.registerUser ? '-' : documentInfo.registerUser}" /></dd>
			</div>
			<div class="sw-detail-item">
				<dt><spring:message code="feature.techDetail.registrant" text="등록자" /></dt>
				<dd><c:out value="${empty documentInfo.insertUserNm ? '-' : documentInfo.insertUserNm}" /></dd>
			</div>
			<div class="sw-detail-item sw-detail-item--span-2">
				<dt><spring:message code="feature.techDetail.requestDate" text="의뢰일자" /></dt>
				<dd><c:out value="${empty documentInfo.insertDt ? '-' : documentInfo.insertDt}" /></dd>
			</div>
		</dl>
	</section>

	<div class="section popupCard sectionBlock mainFileSection">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code="feature.techDetail.mainFiles" text="주파일 정보" /></span>
				<span class="listCount"><spring:message code="feature.common.count.totalItems" text="총 {0}건"
						arguments="${mainFileList.size()}" /></span>
			</div>
			<div class="right">
				<c:if test="${mainDownloadAllowed}">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="downloadSelectedSwFile('gridSwMainFile')"><spring:message
						code="feature.common.button.download" text="다운로드" /></button>
				</c:if>
			</div>
		</div>
		<div class="gridContainer">
			<table id="gridSwMainFile"></table>
		</div>
	</div>

	<div class="section popupCard sectionBlock subFileSection">
		<div class="dialogToolbar">
			<div class="left">
				<span class="gridTitle"><spring:message code="feature.techDetail.supportingFiles" text="보조파일 정보" /></span>
				<span class="listCount"><spring:message code="feature.common.count.totalItems" text="총 {0}건"
						arguments="${subFileList.size()}" /></span>
			</div>
			<div class="right">
				<c:if test="${subDownloadAllowed}">
				<button type="button" class="ui-button ui-corner-all bottomBtn"
					onclick="downloadSelectedSwFile('gridSwSubFile')"><spring:message
						code="feature.common.button.download" text="다운로드" /></button>
				</c:if>
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













