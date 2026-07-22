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
				<link type="text/css" rel="stylesheet"
					href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css"
					media="screen" />
				<script type="text/javascript"
					src="${pageContext.request.contextPath}/resources/js/common_tree.js"></script>
				<script type="text/javascript"
					src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
				<script type="text/javascript"
					src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/drawingRequest/drawingRequestList.js"></script>
				<script>
					window.USE_ACCEPTANCE_VUEXY_FORM = true;

					var gridId = 'gridDrawingRequestList';
					function setGridParam() {
						gridParam = {
							gridId: gridId,
							formId: 'formDrawingRequest',
							url: '/inside/distribution/drawingRequest/selectList',
							size: "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
							page: 1,
							multiSelect: true,
							numbering: false,
							selectRowAction: 'check',
							layoutMode: 'invoice'
						}


						return gridParam;
					}

					// 2023.07.04 천기범 추가
					function formatRequestNo(cellValue, options, rowdata, action) {
						return '<a onclick="openDrawingFilePopup(\'' + rowdata["objectId"] + '\', \'' + rowdata["drawingNo"] + '\', \'' + rowdata["requestNo"] + '\')">' + cellValue + '</a>';
					}

					function openDrawingFilePopup(objectId, drawingNo, requestNo) {
						var popupHeight = Math.min($(window).height() - 120, 620);
						openDialogPopup(
							"/inside/distribution/drawingRequest/drawingFilePopup",
							{ objectId: objectId, drawingNo: drawingNo, requestNo: requestNo },
							"popupDialog",
							'l',
							popupHeight,
							true,
							'popup-common'
						);
					}

					function openAnnotationDialog(objectId) {
						openDialogPopup("/inside/distribution/annotationinfo/annotationPopup", { objectId: objectId, objectType: "DRAWING" }, "popupDialog", 'xl', 400, true, 'popup-common popup-annotation');
					}

					//
					// 2023.07.10 기범추가 ( 등록 버튼 생성, db toolbar에도 생성 )
					function upload() {
						var popupHeight = Math.min($(window).height() - 120, 680);
						openDialogPopup("/inside/distribution/drawingRequest/drawingRegisterPopup", {}, "popupDialog", 'l', popupHeight, true, 'popup-common popup-drawing-register');
					}


					function formatProtect(cellValue, options, rowdata, action) {
						if (cellValue == "Y") {
							return '<a onclick="openDialog(\'' + rowdata["objectId"] + '\')">' + cellValue + '</a>';
						} else {
							return cellValue;
						}
					}

					function openDialog(objectId) {
						openDialogPopup("/inside/distribution/commonRequest/protectPopup", { objectId: objectId, objectType: "DRAWING" }, "popupDialog", 'm', 360, true, 'popup-common popup-protect');
					}

					function deleteDrawing() {
						var list = [];
						if ($("#gridDrawingRequestList").getGridParam('selarrrow').length < 1) {
							alertMessage(g_msg('msg.noSelectedItem'));
							return false;
						}

						$.each($("#gridDrawingRequestList").getGridParam('selarrrow'), function (index, item) {
							var data = $("#gridDrawingRequestList").jqGrid('getRowData', item);
							console.log(data.objectId);
							list.push({ objectId: data.objectId });
						});
						var param = { list: list };
						console.log(param);
						confirmMessage(g_msg('msg.requestDelete'), function () {
							$(this).dialog("close");
							callAjax(param, "/inside/distribution/drawingRequest/delete", deleteDrawingCallback, 'json');
						});

					}

					function deleteDrawingCallback(response) {
						if (response.successCount > 0) {
							alertMessage(g_msg('msg.completeDelete'));
							searchList(gridParam);
						} else if (response.error) {
							alertMessage(response.error);
						} else {
							alertMessage(g_msg('msg.failDelete')); // 기타 실패
						}

					}

					function bindDrawingNoClick() {
						var $grid = $("#gridDrawingRequestList");
						if (!$grid.length) {
							return;
						}

						var selector = 'td[aria-describedby="gridDrawingRequestList_drawingNo"], td[aria-describedby="gridDrawingRequestList_requestNo"]';
						$grid.find(selector).each(function () {
							var $td = $(this);
							if ($td.data('drawingNoLinked')) {
								return;
							}

							var rowId = $td.closest('tr.jqgrow').attr('id');
							var text = $.trim($td.text());
							if (!rowId || text === '') {
								return;
							}

							var rowdata = $("#gridDrawingRequestList").jqGrid('getRowData', rowId) || {};
							var objectId = rowdata.objectId || '';
							var drawingNo = rowdata.drawingNo || text;
							var objectIdAttr = String(objectId).replace(/"/g, '&quot;');
							var drawingNoAttr = String(drawingNo).replace(/"/g, '&quot;');

							$td.data('drawingNoLinked', true);
							$td.html(
								'<a href="javascript:void(0);" class="drawing-no-link" data-row-id="' + rowId + '" data-object-id="' + objectIdAttr + '" data-drawing-no="' + drawingNoAttr + '">' + text + '</a>'
							);
						});
					}

					function hasProcessingRowsDrawing() {
						var $grid = $("#gridDrawingRequestList");
						if (!$grid.length) return false;
						var rowIds = $grid.jqGrid('getDataIDs') || [];
						for (var i = 0; i < rowIds.length; i++) {
							var raw = $grid.jqGrid('getLocalRow', rowIds[i]) || {};
							var processingStatus = String(raw.processingStatus || raw.PROCESSING_STATUS || '').toUpperCase();
							if (processingStatus === 'PROCESSING') return true;
						}
						return false;
					}

					function pollDrawingProcessingStatus() {
						if (document.hidden) return;
						if (hasProcessingRowsDrawing()) {
							searchList(gridParam);
						}
						decorateDrawingStatusBadges();
					}

					function decorateDrawingStatusBadges() {
						var $grid = $("#gridDrawingRequestList");
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

					function approveDrawing() {
						var list = [];
						if ($("#gridDrawingRequestList").getGridParam('selarrrow').length < 1) {
							alertMessage(g_msg('msg.noSelectedItem'));
							return false;
						}

						$.each($("#gridDrawingRequestList").getGridParam('selarrrow'), function (index, item) {
							var data = $("#gridDrawingRequestList").jqGrid('getRowData', item);
							list.push({ objectId: data.objectId });
						});

						var param = { list: list };
						confirmMessage('승인하시겠습니까?', function () {
							$(this).dialog("close");
							callAjax(param, "/inside/distribution/drawingRequest/approve", approveDrawingCallback, 'json');
						});
					}

					function approveDrawingCallback(response) {
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

					$(function () {
						$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
						$(document).on('click', '#gridDrawingRequestList .drawing-no-link', function (e) {
							e.preventDefault();
							var objectId = $(this).data('objectId');
							var drawingNo = $(this).data('drawingNo') || $.trim($(this).text());
							if (!objectId) {
								var rowId = $(this).data('rowId');
								var rowdata = $("#gridDrawingRequestList").jqGrid('getRowData', rowId) || {};
								objectId = rowdata.objectId || '';
								drawingNo = rowdata.drawingNo || drawingNo;
								var requestNo = rowdata.requestNo || '';
								openDrawingFilePopup(objectId, drawingNo, requestNo);
								return;
							}
							openDrawingFilePopup(objectId, drawingNo, '');
						});
						setInterval(bindDrawingNoClick, 400);
						setInterval(pollDrawingProcessingStatus, 5000);
						setInterval(decorateDrawingStatusBadges, 500);
					});
				</script>
			</head>

			<body>
				<div class="distribution-invoice-page">
					<custom:listTemplateInvoice gridId="gridDrawingRequestList" treeId="drawingRequestExplorerTree"
						treeTitle="Documents" />
				</div>
				<div id="searchAllPopup" class="dialogContainer"></div>
			</body>

			</html>
