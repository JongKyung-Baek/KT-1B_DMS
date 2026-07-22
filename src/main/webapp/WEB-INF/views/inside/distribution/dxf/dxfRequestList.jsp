<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
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
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/dxf/dxfRequestList.js"></script>
	<script>
		window.USE_ACCEPTANCE_VUEXY_FORM = true;

		var gridId = 'gridDxfRequestList';
		function setGridParam() {
			gridParam = {
				gridId: gridId,
				formId: 'formDxfRequest',
				url: '/inside/distribution/dxfRequest/selectList',
				size: "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page: 1,
				multiSelect: true,
				numbering: false,
				selectRowAction: 'check',
				layoutMode: 'invoice',
				fillColumns: true
			};

			return gridParam;
		}

		function request() {
			var selrow = $('#' + gridId).jqGrid('getGridParam', 'selarrrow');
			if (selrow.length <= 0) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return;
			}
			openDialogPopup('/inside/distribution/dxfRequest/requestPopup', {}, 'popupDialog', 'l', '800');
		}

		function printRequest() {
			var selrow = $('#' + gridId).jqGrid('getGridParam', 'selarrrow');
			if (selrow.length <= 0) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return;
			}
			openDialogPopup('/inside/distribution/dxfRequest/printRequestPopup', {}, 'popupDialog', 's', '330');
		}

		function formatViewFile(cellValue, options, rowdata, action) {
			return '<a onclick="openDialogPopup(\'/inside/distribution/dxfRequest/dxfFilePopup\', { objectId: \'' + rowdata["objectId"] + '\', objectNo: \'' + rowdata["objectNo"] + '\', requestNo: \'' + rowdata["requestNo"] + '\' }, \'popupDialog\', \'l\', 720, true, \'popup-common popup-dxf-file\')">' + cellValue + '</a>';
		}

		function openFileDown() {
			if ($('#sessionRoleGroup').val() == 'RG_007' || $('#sessionAuthLevel').val() == 32) {
				if ($("#" + gridId).getGridParam('selarrrow').length < 1) {
					alertMessage(g_msg('msg.noSelectData'), function () {
						$(this).dialog("close");
					});
					return false;
				}
				openDialogPopup("/common/down/openFileDownPopup", { gridId: gridId }, "popupDialog", 'm', 450);
			} else {
				alertMessage(g_msg("msg.ProductTeamFileDown"));
				return;
			}
		}

		function upload() {
			var popupHeight = Math.min($(window).height() - 100, 600);
			openDialogPopup(
				"/inside/distribution/dxfRequest/dxfRegisterPopup",
				{ treeCd: (window.dxfRequestTreeState && dxfRequestTreeState.selectedTreeCd) || "" },
				"popupDialog",
				'l',
				popupHeight,
				true,
				'popup-common popup-dxf-register'
			);
		}

		function deleteDxf() {
			var list = [];
			if ($("#gridDxfRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return false;
			}

			$.each($("#gridDxfRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridDxfRequestList").jqGrid('getRowData', item);
				list.push({ objectId: data.objectId });
			});

			var param = { list: list };
			confirmMessage(g_msg('msg.requestDelete'), function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/dxfRequest/delete", deleteDxfCallback, 'json');
			});
		}

		function deleteDxfCallback(response) {
			if (response.successCount > 0) {
				alertMessage(g_msg('msg.completeDelete'));
				searchList(gridParam);
				return;
			}

			if (response.error) {
				alertMessage(response.error);
			} else {
				alertMessage(g_msg('msg.failDelete'));
			}
		}

		function approveDxf() {
			var list = [];
			if ($("#gridDxfRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return false;
			}

			$.each($("#gridDxfRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridDxfRequestList").jqGrid('getRowData', item);
				list.push({ objectId: data.objectId });
			});

			var param = { list: list };
			confirmMessage('승인하시겠습니까?', function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/dxfRequest/approve", approveDxfCallback, 'json');
			});
		}

		function approveDXF() {
			approveDxf();
		}

		function approveDxfCallback(response) {
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

		function showDxfStatusDetail(rowId) {
			var rowdata = $("#gridDxfRequestList").jqGrid('getRowData', rowId);
			if (!rowdata) {
				return;
			}
			openDialogPopup(
				"/inside/distribution/commonRequest/approvalStatusPopup",
				{
					objectId: rowdata.objectId,
					approveUrl: "/inside/distribution/dxfRequest/approveStatusMessage",
					requestType: "DXF"
				},
				"popupDialog",
				"s",
				300,
				true,
				"popup-common popup-approval-status"
			);
		}

		function openDxfFilePopup(objectId, objectNo, requestNo) {
			openDialogPopup(
				"/inside/distribution/dxfRequest/dxfFilePopup",
				{ objectId: objectId, objectNo: objectNo, requestNo: requestNo || "" },
				"popupDialog",
				"l",
				720,
				true,
				"popup-common popup-dxf-file"
			);
		}

		function bindDxfNoClick() {
			var $grid = $("#gridDxfRequestList");
			if (!$grid.length) return;

			var selector = [
				'td[aria-describedby="gridDxfRequestList_objectNo"]',
				'td[aria-describedby="gridDxfRequestList_documentNo"]',
				'td[aria-describedby="gridDxfRequestList_drawingNo"]',
				'td[aria-describedby="gridDxfRequestList_requestNo"]'
			].join(', ');
			$grid.find(selector).each(function () {
				var $td = $(this);
				if ($td.data('dxfNoLinked')) return;

				var rowId = $td.closest('tr.jqgrow').attr('id');
				var text = $.trim($td.text());
				if (!rowId || text === '') return;

				var rowdata = $("#gridDxfRequestList").jqGrid('getRowData', rowId) || {};
				var objectId = rowdata.objectId || '';
				var objectNo = rowdata.objectNo || rowdata.documentNo || rowdata.drawingNo || text;
				var objectIdAttr = String(objectId).replace(/"/g, '&quot;');
				var objectNoAttr = String(objectNo).replace(/"/g, '&quot;');

				$td.data('dxfNoLinked', true);
				$td.html('<a href="javascript:void(0);" class="dxf-no-link" data-row-id="' + rowId + '" data-object-id="' + objectIdAttr + '" data-object-no="' + objectNoAttr + '">' + text + '</a>');
			});
		}

		function bindDxfStatusClick() {
			return;
		}

		function hasProcessingRowsDxf() {
			var $grid = $("#gridDxfRequestList");
			if (!$grid.length) return false;
			var rowIds = $grid.jqGrid('getDataIDs') || [];
			for (var i = 0; i < rowIds.length; i++) {
				var raw = $grid.jqGrid('getLocalRow', rowIds[i]) || {};
				var processingStatus = String(raw.processingStatus || raw.processingstatus || raw.PROCESSING_STATUS || '').toUpperCase();
				if (processingStatus === 'PROCESSING') return true;
			}
			return false;
		}

		function pollDxfProcessingStatus() {
			if (document.hidden) return;
			if (hasProcessingRowsDxf()) {
				searchList(gridParam);
			}
			decorateDxfStatusBadges();
		}

		function decorateDxfStatusBadges() {
			var $grid = $("#gridDxfRequestList");
			if (!$grid.length) return;
			$grid.find('tr.jqgrow').each(function () {
				var rowId = $(this).attr('id');
				if (!rowId) return;
				var raw = $grid.jqGrid('getLocalRow', rowId) || {};
				var processingStatus = String(raw.processingStatus || raw.processingstatus || raw.PROCESSING_STATUS || '').toUpperCase();
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
			$(document).on('click', '#gridDxfRequestList .dxf-status-link', function (e) {
				e.preventDefault();
				showDxfStatusDetail($(this).data('rowId'));
			});
			$(document).on('click', '#gridDxfRequestList .dxf-no-link', function (e) {
				e.preventDefault();
				var objectId = $(this).data('objectId');
				var objectNo = $(this).data('objectNo') || $.trim($(this).text());
				var rowId = $(this).data('rowId');
				var rowdata = $("#gridDxfRequestList").jqGrid('getRowData', rowId) || {};
				openDxfFilePopup(objectId || rowdata.objectId || '', objectNo || rowdata.objectNo || rowdata.documentNo || rowdata.drawingNo || '', rowdata.requestNo || '');
			});
			setInterval(bindDxfStatusClick, 400);
			setInterval(bindDxfNoClick, 400);
			setInterval(pollDxfProcessingStatus, 5000);
			setInterval(decorateDxfStatusBadges, 500);
		});
	</script>
</head>

<body>
	<input type="hidden" id="sessionRoleGroup" name="sessionRoleGroup" value="${sessionUser.roleGroup}" />
	<input type="hidden" id="sessionAuthLevel" name="sessionAuthLevel" value="${sessionUser.authLevel}" />
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridDxfRequestList" treeId="dxfRequestExplorerTree" treeTitle="PMPCB" />
	</div>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>

</html>
