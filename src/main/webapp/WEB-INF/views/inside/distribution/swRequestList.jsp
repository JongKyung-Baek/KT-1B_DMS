<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code="feature.locale.code" text="ko" var="pageLanguage" />
<spring:message code="feature.techList.tree.title" text="자료 분류" var="treePanelTitle" />
<spring:message code="feature.techList.tree.description" text="분류를 선택해 자료를 좁혀보세요" var="treePanelDescription" />
<spring:message code="feature.techList.tree.searchPlaceholder" text="분류 검색" var="treeSearchPlaceholder" />
<spring:message code="feature.techList.tree.allDocuments" text="전체 자료" var="treeAllLabel" />
<!doctype html>
<html lang="${pageLanguage}">

<head>
	<meta charset="UTF-8">
	<title>KT-1B DMS</title>
	<style>
		.ch-badge{display:inline-block;padding:2px 8px;border-radius:10px;font-size:11px;line-height:1.4}
		.ch-badge-processing{background:#fff4cc;color:#8a6d00}
		.ch-badge-done{background:#e7f7ec;color:#1d6b3a}
		.ch-badge-fail{background:#fdeaea;color:#a12828}
	</style>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css?v=20260726.1" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/technical-data-list.css?v=20260801.1" media="screen" />
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_tree.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
	<script>
		window.SdmsPageMessages = {
			"feature.techList.tree.all": "<spring:message code='feature.techList.tree.all' text='전체' javaScriptEscape='true' />",
			"feature.techList.tree.empty": "<spring:message code='feature.techList.tree.empty' text='표시할 트리 데이터가 없습니다' javaScriptEscape='true' />",
			"feature.techList.tree.root": "<spring:message code='feature.techList.tree.root' text='기술자료' javaScriptEscape='true' />",
			"feature.techList.tree.toggle": "<spring:message code='feature.techList.tree.toggle' text='하위 분류 열기/닫기' javaScriptEscape='true' />",
			"feature.techList.tree.count": "<spring:message code='feature.techList.tree.count' text='{0}건' javaScriptEscape='true' />",
			"feature.techList.tree.noMatches": "<spring:message code='feature.techList.tree.noMatches' text='일치하는 분류가 없습니다' javaScriptEscape='true' />",
			"feature.techList.tree.selection": "<spring:message code='feature.techList.tree.selection' text='선택 분류' javaScriptEscape='true' />",
			"feature.techList.tree.clearSelection": "<spring:message code='feature.techList.tree.clearSelection' text='분류 선택 해제' javaScriptEscape='true' />",
			"feature.techList.tree.category.drawing": "<spring:message code='feature.techList.tree.category.drawing' text='도면' javaScriptEscape='true' />",
			"feature.techList.tree.category.spec": "<spring:message code='feature.techList.tree.category.spec' text='규격서' javaScriptEscape='true' />",
			"feature.techList.tree.category.sow": "<spring:message code='feature.techList.tree.category.sow' text='업무기술서' javaScriptEscape='true' />",
			"feature.techList.tree.category.sdrl": "<spring:message code='feature.techList.tree.category.sdrl' text='납품자료' javaScriptEscape='true' />",
			"feature.techList.tree.category.programData": "<spring:message code='feature.techList.tree.category.programData' text='사업문서' javaScriptEscape='true' />",
			"feature.techList.tree.category.sro": "<spring:message code='feature.techList.tree.category.sro' text='특별작업지시서' javaScriptEscape='true' />",
			"feature.techList.tree.category.testProcedure": "<spring:message code='feature.techList.tree.category.testProcedure' text='시험 절차서' javaScriptEscape='true' />",
			"feature.techList.tree.category.engineeringMemo": "<spring:message code='feature.techList.tree.category.engineeringMemo' text='개발 업무메모' javaScriptEscape='true' />",
			"feature.techList.tree.category.sourceData": "<spring:message code='feature.techList.tree.category.sourceData' text='원천자료' javaScriptEscape='true' />",
			"feature.techList.tree.category.etc": "<spring:message code='feature.techList.tree.category.etc' text='기타자료' javaScriptEscape='true' />",
			"feature.techList.tree.category.mfgData": "<spring:message code='feature.techList.tree.category.mfgData' text='생산기술자료' javaScriptEscape='true' />",
			"feature.documentGrade.general": "<spring:message code='feature.documentGrade.general' text='일반' javaScriptEscape='true' />",
			"feature.documentGrade.internal": "<spring:message code='feature.documentGrade.internal' text='사내한' javaScriptEscape='true' />",
			"feature.documentGrade.restricted": "<spring:message code='feature.documentGrade.restricted' text='제한' javaScriptEscape='true' />",
			"feature.documentGrade.confidential": "<spring:message code='feature.documentGrade.confidential' text='대외비' javaScriptEscape='true' />",
			"feature.documentGrade.unassigned": "<spring:message code='feature.documentGrade.unassigned' text='미지정' javaScriptEscape='true' />",
			"feature.techList.grade.label": "<spring:message code='feature.techList.grade.label' text='문서등급' javaScriptEscape='true' />",
			"feature.techList.grade.notAssigned": "<spring:message code='feature.techList.grade.notAssigned' text='문서등급이 지정되지 않았습니다.' javaScriptEscape='true' />",
			"feature.grid.pager.firstPage": "<spring:message code='feature.grid.pager.firstPage' text='첫 번째 페이지로 이동' javaScriptEscape='true' />",
			"feature.grid.pager.lastPage": "<spring:message code='feature.grid.pager.lastPage' text='마지막 페이지로 이동' javaScriptEscape='true' />",
			"feature.grid.pager.goToPage": "<spring:message code='feature.grid.pager.goToPage' text='{0}페이지로 이동' javaScriptEscape='true' />",
			"feature.grid.pager.goToPageRange": "<spring:message code='feature.grid.pager.goToPageRange' text='{0}페이지에서 {1}페이지까지 이동' javaScriptEscape='true' />",
			"feature.grid.pager.noData": "<spring:message code='feature.grid.pager.noData' text='데이터가 없습니다.' javaScriptEscape='true' />",
			"feature.grid.pager.rowsPerPage": "<spring:message code='feature.grid.pager.rowsPerPage' text='건 표시' javaScriptEscape='true' />",
			"feature.grid.pager.total": "<spring:message code='feature.grid.pager.total' text='총 {0}' javaScriptEscape='true' />"
		};
	</script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/swRequestList.js?v=20260726.4"></script>
	<script>
		window.USE_ACCEPTANCE_VUEXY_FORM = true;

		var gridId = 'gridSwRequestList';
		function setGridParam() {
			gridParam = {
				gridId: gridId,
				formId: 'formSwRequest',
				url: '/inside/distribution/swRequest/selectList',
				size: "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page: 1,
				multiSelect: true,
				numbering: false,
				selectRowAction: 'check',
				layoutMode: 'invoice',
				fillColumns: true
			}

			return gridParam;
		}

		function loadComplete(data) {
			finishSwRequestGridRefresh();
		}

		//function formatSwFile(cellValue, options, rowdata, action){
		//	return '<a onclick="openViewer(\''+rowdata["objectId"]+'\', \'SW\', \'OBJECT\', null)">'+cellValue+'</a>';
		//}

		function formatValidType(cellValue, options, rowdata, action) {
			var rtn = "";
			if (cellValue != undefined) {
				rtn = cellValue;
			}
			return '<font color="red">' + rtn + '</font>';
		}

		function escapeSwGridHtml(value) {
			return String(value === undefined || value === null ? "" : value)
				.replace(/&/g, "&amp;")
				.replace(/</g, "&lt;")
				.replace(/>/g, "&gt;")
				.replace(/"/g, "&quot;")
				.replace(/'/g, "&#39;");
		}

		function resolveDocumentGradeTone(rowdata, gradeName) {
			var level = parseInt(rowdata && rowdata.gradeLevel, 10);
			var code = String(rowdata && rowdata.gradeCd || "").toUpperCase();
			if (!gradeName) return "unassigned";
			if ((!isNaN(level) && level >= 40) || code === "CONFIDENTIAL") return "confidential";
			if ((!isNaN(level) && level >= 30) || code === "RESTRICTED") return "restricted";
			if ((!isNaN(level) && level >= 20) || code === "INTERNAL") return "internal";
			return "general";
		}

		function localizeDocumentGradeName(gradeCode, gradeName) {
			var normalizedCode = String(gradeCode || "").toUpperCase();
			var normalizedName = $.trim(String(gradeName || ""))
				.replace(/\s+/g, "")
				.toUpperCase();
			switch (normalizedCode) {
				case "GENERAL":
					return swRequestMessage("feature.documentGrade.general", "일반");
				case "INTERNAL":
					return swRequestMessage("feature.documentGrade.internal", "사내한");
				case "RESTRICTED":
					return swRequestMessage("feature.documentGrade.restricted", "제한");
				case "CONFIDENTIAL":
					return swRequestMessage("feature.documentGrade.confidential", "대외비");
				case "UNASSIGNED":
					return swRequestMessage("feature.documentGrade.unassigned", "미지정");
			}
			switch (normalizedName) {
				case "일반":
				case "GENERAL":
					return swRequestMessage("feature.documentGrade.general", "일반");
				case "사내한":
				case "INTERNALUSEONLY":
				case "INTERNAL":
					return swRequestMessage("feature.documentGrade.internal", "사내한");
				case "제한":
				case "RESTRICTED":
					return swRequestMessage("feature.documentGrade.restricted", "제한");
				case "대외비":
				case "CONFIDENTIAL":
					return swRequestMessage("feature.documentGrade.confidential", "대외비");
				case "미지정":
				case "UNASSIGNED":
					return swRequestMessage("feature.documentGrade.unassigned", "미지정");
				default:
					return gradeName;
			}
		}

		function formatDocumentGrade(cellValue, options, rowdata) {
			var row = rowdata || {};
			var sourceGradeName = $.trim(String(cellValue || row.gradeNm || ""));
			var gradeCode = $.trim(String(row.gradeCd || ""));
			var gradeLevel = $.trim(String(row.gradeLevel === undefined || row.gradeLevel === null ? "" : row.gradeLevel));
			var gradeName = localizeDocumentGradeName(gradeCode, sourceGradeName);
			var displayName = gradeName || swRequestMessage("feature.documentGrade.unassigned", "미지정");
			var tone = resolveDocumentGradeTone(row, gradeName);
			var detail = gradeName
				? swRequestMessage("feature.techList.grade.label", "문서등급") + ": " + gradeName
					+ (gradeCode ? " (" + gradeCode + (gradeLevel ? ", " + gradeLevel : "") + ")" : "")
				: swRequestMessage("feature.techList.grade.notAssigned", "문서등급이 지정되지 않았습니다.");
			return '<span class="document-grade-badge document-grade-badge--' + tone
				+ '" title="' + escapeSwGridHtml(detail)
				+ '" aria-label="' + escapeSwGridHtml(detail) + '">'
				+ escapeSwGridHtml(displayName) + '</span>';
		}

		function formatFileExtensions(cellValue) {
			var extensions = $.map(String(cellValue || '').split(','), function (value) {
				var extension = $.trim(value).toUpperCase();
				return extension ? extension : null;
			});
			if (!extensions.length) {
				return '<span class="file-extension-empty">-</span>';
			}
			var title = escapeSwGridHtml(extensions.join(', '));
			var badges = $.map(extensions, function (extension) {
				return '<span class="file-extension-badge">'
					+ escapeSwGridHtml(extension) + '</span>';
			}).join('');
			return '<span class="file-extension-list" title="' + title
				+ '" aria-label="' + title + '">' + badges + '</span>';
		}

		function formatViewFile(cellValue, options, rowdata, action) {
			return '<a onclick="openDialogPopup(\'/inside/distribution/swRequest/swFilePopup\', { objectId: \'' + rowdata["objectId"] + '\', swNo: \'' + rowdata["swNo"] + '\', requestNo: \'' + rowdata["requestNo"] + '\' }, \'popupDialog\', \'l\', 720, true, \'popup-common popup-sw-file\')">' + cellValue + '</a>';
		}


		function formatProtect(cellValue, options, rowdata, action) {
			if (cellValue == "Y") {
				return '<a onclick="openDialog(\'' + rowdata["objectId"] + '\')">' + cellValue + '</a>';
			} else {
				return cellValue;
			}
		}

					function openDialog(objectId) {
						openDialogPopup("/inside/distribution/commonRequest/protectPopup", { objectId: objectId, objectType: "SW" }, "popupDialog", 'm', 360, true, 'popup-common popup-protect');
					}

					// 2023.07.24 기범추가 ( 등록 버튼 생성 )
					function upload() {
						var treeCd = $("#formSwRequest [name='swTreeCd']").val() || $("#formSwRequest [name='swTreeCd_treeHidden']").val() || "";
						var url = "/inside/distribution/swRequest/regist";
						if ($.trim(treeCd) !== "" && treeCd.indexOf(",") === -1) {
							url += "?treeCd=" + encodeURIComponent(treeCd);
						}
						location.href = url;
					}

		function deleteSW() {
			var list = [];
			if ($("#gridSwRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(swRequestMessage("feature.common.validation.noSelection", "선택된 항목이 없습니다."));
				return false;
			}

			$.each($("#gridSwRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridSwRequestList").jqGrid('getRowData', item);
				console.log(data.objectId);
				list.push({ objectId: data.objectId });
			});
			var param = { list: list };
			console.log(param);
			confirmMessage(swRequestMessage("feature.techList.withdraw.confirm", "철회하시겠습니까?"), function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/swRequest/delete", deleteSWCallback, 'json');
			});
		}

		function deleteSWCallback(response) {
			if (response.successCount > 0) {
				alertMessage(swRequestMessage("feature.techList.withdraw.complete", "철회되었습니다."));
				searchList(gridParam);
			} else if (response.error) {
				alertMessage(response.error); //"삭제 권한이 없습니다."
			} else {
				alertMessage(swRequestMessage("feature.techList.withdraw.failed", "철회에 실패했습니다."));
			}

		}

		function approveSW() {
			var list = [];
			if ($("#gridSwRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(swRequestMessage("feature.common.validation.noSelection", "선택된 항목이 없습니다."));
				return false;
			}

			$.each($("#gridSwRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridSwRequestList").jqGrid('getRowData', item);
				list.push({ objectId: data.objectId });
			});

			var param = { list: list };
			confirmMessage(swRequestMessage("feature.techList.approval.confirm", "승인하시겠습니까?"), function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/swRequest/approve", approveSWCallback, 'json');
			});
		}

		function approveSWCallback(response) {
			if (response.successCount > 0) {
				if (response.failCount > 0 && response.message) {
					alertMessage(swRequestMessage("feature.techList.approval.partial", "일부 승인되었습니다.")
						+ " " + response.message);
				} else {
					alertMessage(swRequestMessage("feature.techList.approval.complete", "승인되었습니다."));
				}
				searchList(gridParam);
				return;
			}

			if (response.message) {
				alertMessage(response.message);
			} else {
				alertMessage(swRequestMessage("feature.techList.approval.failed", "승인 처리에 실패했습니다."));
			}
		}

		function showSWStatusDetail(rowId) {
			var rowdata = $("#gridSwRequestList").jqGrid('getRowData', rowId);
			if (!rowdata) {
				return;
			}
			openDialogPopup(
				"/inside/distribution/commonRequest/approvalStatusPopup",
				{
					objectId: rowdata.objectId,
					approveUrl: "/inside/distribution/swRequest/approveStatusMessage",
					requestType: "SW"
				},
				"popupDialog",
				"s",
				300,
				true,
				"popup-common popup-approval-status"
			);
		}

		function openSwFilePopup(objectId, swNo, requestNo) {
			openDialogPopup(
				"/inside/distribution/swRequest/swFilePopup",
				{ objectId: objectId, swNo: swNo, requestNo: requestNo || "" },
				"popupDialog",
				"l",
				720,
				true,
				"popup-common popup-sw-file"
			);
		}

		function bindSwNoClick() {
			var $grid = $("#gridSwRequestList");
			if (!$grid.length) return;

			var selector = 'td[aria-describedby="gridSwRequestList_swNo"], td[aria-describedby="gridSwRequestList_requestNo"]';
			$grid.find(selector).each(function () {
				var $td = $(this);
				if ($td.data('swNoLinked')) return;

				var rowId = $td.closest('tr.jqgrow').attr('id');
				var text = $.trim($td.text());
				if (!rowId || text === '') return;

				var rowdata = $("#gridSwRequestList").jqGrid('getRowData', rowId) || {};
				var objectId = rowdata.objectId || '';
				var swNo = rowdata.swNo || text;
				var objectIdAttr = String(objectId).replace(/"/g, '&quot;');
				var swNoAttr = String(swNo).replace(/"/g, '&quot;');

				$td.data('swNoLinked', true);
				$td.html('<a href="javascript:void(0);" class="sw-no-link" data-row-id="' + rowId + '" data-object-id="' + objectIdAttr + '" data-sw-no="' + swNoAttr + '">' + text + '</a>');
			});
		}

		function bindSWStatusClick() {
			return;
		}

		function hasProcessingRowsSw() {
			var $grid = $("#gridSwRequestList");
			if (!$grid.length) return false;
			var rowIds = $grid.jqGrid('getDataIDs') || [];
			for (var i = 0; i < rowIds.length; i++) {
				var raw = $grid.jqGrid('getLocalRow', rowIds[i]) || {};
				var processingStatus = String(raw.processingStatus || raw.PROCESSING_STATUS || '').toUpperCase();
				if (processingStatus === 'PROCESSING') return true;
			}
			return false;
		}

		function pollSwProcessingStatus() {
			if (document.hidden) return;
			if (hasProcessingRowsSw()) {
				searchList(gridParam);
			}
			decorateSwStatusBadges();
		}

		function decorateSwStatusBadges() {
			var $grid = $("#gridSwRequestList");
			if (!$grid.length) return;
			$grid.find('tr.jqgrow').each(function () {
				var rowId = $(this).attr('id');
				if (!rowId) return;
				var raw = $grid.jqGrid('getLocalRow', rowId) || {};
				var processingStatus = String(raw.processingStatus || raw.PROCESSING_STATUS || '').toUpperCase();
				var $statusTd = $(this).find('td[aria-describedby$="_status"], td[aria-describedby$="_statusNm"]').first();
				if (!$statusTd.length) return;
				$statusTd.find('.ch-processing-badge').remove();
				if (processingStatus === 'PROCESSING') {
					$statusTd.append(' <span class="ch-badge ch-badge-processing ch-processing-badge">'
						+ swRequestMessage("feature.techList.processing.inProgress", "처리중") + '</span>');
				} else if (processingStatus === 'FAIL') {
					$statusTd.append(' <span class="ch-badge ch-badge-fail ch-processing-badge">'
						+ swRequestMessage("feature.techList.processing.failed", "처리실패") + '</span>');
				}
			});
		}


		$(function () {
			$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
			$(document).on('click', '#gridSwRequestList .sw-status-link', function (e) {
				e.preventDefault();
				showSWStatusDetail($(this).data('rowId'));
			});
			$(document).on('click', '#gridSwRequestList .sw-no-link', function (e) {
				e.preventDefault();
				var objectId = $(this).data('objectId');
				var swNo = $(this).data('swNo') || $.trim($(this).text());
				var rowId = $(this).data('rowId');
				var rowdata = $("#gridSwRequestList").jqGrid('getRowData', rowId) || {};
				openSwFilePopup(objectId || rowdata.objectId || '', swNo || rowdata.swNo || '', rowdata.requestNo || '');
			});
			setInterval(bindSWStatusClick, 400);
			setInterval(bindSwNoClick, 400);
			setInterval(pollSwProcessingStatus, 5000);
			setInterval(decorateSwStatusBadges, 500);
		});

	</script>
</head>
<body>
	<main class="distribution-invoice-page technical-data-list-page"
		  aria-label="<spring:message code='feature.techList.aria.page' text='기술자료 조회' />">
		<section class="technical-data-results-card"
				 aria-label="<spring:message code='feature.techList.aria.results' text='기술자료 검색 및 목록' />">
			<custom:listTemplateInvoice
				gridId="gridSwRequestList"
				treeId="swRequestExplorerTree"
				treeTitle="${treePanelTitle}"
				treeDescription="${treePanelDescription}"
				treeSearchPlaceholder="${treeSearchPlaceholder}"
				treeAllLabel="${treeAllLabel}" />
		</section>
	</main>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>

</html>
