<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<!doctype html>
<html lang="kr">
<head>
	<meta charset="UTF-8">
	<title>CollabHub</title>
	<style>
		.ch-badge{display:inline-block;padding:2px 8px;border-radius:10px;font-size:11px;line-height:1.4}
		.ch-badge-processing{background:#fff4cc;color:#8a6d00}
		.ch-badge-done{background:#e7f7ec;color:#1d6b3a}
		.ch-badge-fail{background:#fdeaea;color:#a12828}
	</style>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_tree.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/docRequestList.js"></script>
	<script>
		window.USE_ACCEPTANCE_VUEXY_FORM = true;

		var gridId = 'gridDocRequestList';
		function setGridParam() {
			gridParam = {
				gridId: gridId,
				formId: 'formDocRequest',
				url: '/inside/distribution/docRequest/selectList',
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

		// 	function formatDocFileList(cellValue, options, rowdata, action){
		// 		return '<a onclick="openViewer(\''+rowdata["objectId"]+'\', \'DOC\', \'OBJECT\', null)">'+cellValue+'</a>';
		// 	}

		function formatValidType(cellValue, options, rowdata, action) {
			var rtn = "";
			if (cellValue != undefined) {
				rtn = cellValue;
			}
			return '<font color="red">' + rtn + '</font>';
		}

		function formatViewFile(cellValue, options, rowdata, action) {
			return '<a onclick="openFile(\'OBJECT\', \'DOC\', \'' + rowdata["requestNo"] + '\', \'' + rowdata["objectId"] + '\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">' + cellValue + '</a>';
			//return '<a onclick="openFile()">'+cellValue+'</a>';

		}

		// 2023.07.04 천기범
		function formatDocName(cellValue, options, rowdata, action) {
			return '<a onclick="openAnnotationDialog(\'' + rowdata["objectId"] + '\')">' + cellValue + '</a>';
		}

		function openAnnotationDialog(objectId) {
			console.log("test: " + objectId);
			openDialogPopup("/inside/distribution/annotationinfo/annotationPopup", { objectId: objectId, objectType: "DOC" }, "popupDialog", 'xl', 400, true, 'popup-common popup-annotation');
		}
		//

		function openDocFilePopup(objectId, documentNo, requestNo) {
			var popupHeight = Math.min($(window).height() - 120, 620);
			openDialogPopup(
				"/inside/distribution/docRequest/docFilePopup",
				{ objectId: objectId, documentNo: documentNo, requestNo: requestNo },
				"popupDialog",
				'l',
				popupHeight,
				true,
				'popup-common'
			);
		}

		// 2023.07.24 기범추가 ( 등록 버튼 생성 )
		function upload() {
			var popupHeight = Math.min($(window).height() - 100, 600);
			openDialogPopup("/inside/distribution/docRequest/docRegisterPopup", {}, "popupDialog", 'l', popupHeight, true, 'popup-common popup-doc-register');
		}


		function formatProtect(cellValue, options, rowdata, action) {
			if (cellValue == "Y") {
				return '<a onclick="openDialog(\'' + rowdata["objectId"] + '\')">' + cellValue + '</a>';
			} else {
				return cellValue;
			}
		}

		function openDialog(objectId) {
			openDialogPopup("/inside/distribution/commonRequest/protectPopup", { objectId: objectId, objectType: "DOC" }, "popupDialog", 'm', 360, true, 'popup-common popup-protect');
		}

		function deleteDoc() {
			var list = [];
			if ($("#gridDocRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return false;
			}

			$.each($("#gridDocRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridDocRequestList").jqGrid('getRowData', item);
				list.push({ objectId: data.objectId });
			});
			var param = { list: list };
			confirmMessage(g_msg('msg.requestDelete'), function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/docRequest/delete", deleteDocCallback, 'json');
			});
		}

		function deleteDocCallback(response) {
			if (response.successCount > 0) {
				alertMessage(g_msg('msg.completeDelete'));
				searchList(gridParam);
			} else if (response.error) {
				alertMessage(response.error);
			} else {
				alertMessage(g_msg('msg.failDelete'));
			}
		}

		function approveDocument() {
			var list = [];
			if ($("#gridDocRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return false;
			}

			$.each($("#gridDocRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridDocRequestList").jqGrid('getRowData', item);
				list.push({ objectId: data.objectId });
			});

			var param = { list: list };
			confirmMessage('승인하시겠습니까?', function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/docRequest/approve", approveDocumentCallback, 'json');
			});
		}

		function approveDocumentCallback(response) {
			if (response.successCount > 0) {
				if (response.failCount > 0 && response.message) {
					alertMessage('일부 승인되었습니다. ' + response.message);
				} else {
					alertMessage('승인되었습니다.');
				}
				searchList(gridParam);
				return;
			}

			if (response.message) {
				alertMessage(response.message);
			} else {
				alertMessage('승인 처리에 실패했습니다.');
			}
		}

		function bindDocNoClick() {
			var $grid = $("#gridDocRequestList");
			if (!$grid.length) {
				return;
			}

			var selector = 'td[aria-describedby="gridDocRequestList_documentNo"], td[aria-describedby="gridDocRequestList_requestNo"]';
			$grid.find(selector).each(function () {
				var $td = $(this);
				if ($td.data('docNoLinked')) {
					return;
				}

				var rowId = $td.closest('tr.jqgrow').attr('id');
				var text = $.trim($td.text());
				if (!rowId || text === '') {
					return;
				}

				var rowdata = $("#gridDocRequestList").jqGrid('getRowData', rowId) || {};
				var objectId = rowdata.objectId || '';
				var documentNo = rowdata.documentNo || text;
				var objectIdAttr = String(objectId).replace(/"/g, '&quot;');
				var docNoAttr = String(documentNo).replace(/"/g, '&quot;');

				$td.data('docNoLinked', true);
				$td.html(
					'<a href="javascript:void(0);" class="doc-no-link" data-row-id="' + rowId + '" data-object-id="' + objectIdAttr + '" data-document-no="' + docNoAttr + '">' + text + '</a>'
				);
			});
		}

		function hasProcessingRowsDoc() {
			var $grid = $("#gridDocRequestList");
			if (!$grid.length) return false;
			var rowIds = $grid.jqGrid('getDataIDs') || [];
			for (var i = 0; i < rowIds.length; i++) {
				var raw = $grid.jqGrid('getLocalRow', rowIds[i]) || {};
				var processingStatus = String(raw.processingStatus || raw.PROCESSING_STATUS || '').toUpperCase();
				if (processingStatus === 'PROCESSING') return true;
			}
			return false;
		}

		function pollDocProcessingStatus() {
			if (document.hidden) return;
			if (hasProcessingRowsDoc()) {
				searchList(gridParam);
			}
			decorateDocStatusBadges();
		}

		function decorateDocStatusBadges() {
			var $grid = $("#gridDocRequestList");
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
					$statusTd.append(' <span class="ch-badge ch-badge-processing ch-processing-badge">처리중</span>');
				} else if (processingStatus === 'FAIL') {
					$statusTd.append(' <span class="ch-badge ch-badge-fail ch-processing-badge">처리실패</span>');
				}
			});
		}

		$(function () {
			$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
			$(document).on('click', '#gridDocRequestList .doc-no-link', function (e) {
				e.preventDefault();
				var objectId = $(this).data('objectId');
				var documentNo = $(this).data('documentNo') || $.trim($(this).text());
				if (!objectId) {
					var rowId = $(this).data('rowId');
					var rowdata = $("#gridDocRequestList").jqGrid('getRowData', rowId) || {};
					objectId = rowdata.objectId || '';
					documentNo = rowdata.documentNo || documentNo;
					var requestNo = rowdata.requestNo || '';
					openDocFilePopup(objectId, documentNo, requestNo);
					return;
				}
				openDocFilePopup(objectId, documentNo, '');
			});
			setInterval(bindDocNoClick, 400);
			setInterval(pollDocProcessingStatus, 5000);
			setInterval(decorateDocStatusBadges, 500);
		});
	</script>
</head>

<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridDocRequestList" treeId="docRequestExplorerTree" treeTitle="IOC" />
	</div>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>
