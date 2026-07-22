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
						<link type="text/css" rel="stylesheet"
							href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css"
							media="screen" />
						<script type="text/javascript"
							src="${pageContext.request.contextPath}/resources/js/common_tree.js"></script>
						<script type="text/javascript"
							src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
						<script type="text/javascript"
							src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/production/productionRequestList.js"></script>
						<script>
							window.USE_ACCEPTANCE_VUEXY_FORM = true;

							var gridId = 'gridDistributionProductionRequestList';
							function setGridParam() {
								gridParam = {
									gridId: gridId,
									formId: 'formProductionRequest',
									url: '/inside/distribution/productionRequest/selectList',
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

							//배포 승인요청					
							function request() {
								var selrow = $('#' + gridId).jqGrid('getGridParam', 'selarrrow');
								if (selrow.length <= 0) {
									alertMessage(g_msg('msg.noSelectedItem'));
									return;
								}
								openDialogPopup('/inside/distribution/productionRequest/requestPopup', {}, 'popupDialog', 'l', '800');
							}

							//출력 승인요청									
							function printRequest() {
								var selrow = $('#' + gridId).jqGrid('getGridParam', 'selarrrow');
								if (selrow.length <= 0) {
									alertMessage(g_msg('msg.noSelectedItem'));
									return;
								}
								openDialogPopup('/inside/distribution/productionRequest/printRequestPopup', {}, 'popupDialog', 's', '330');
							}

							function formatViewFile(cellValue, options, rowdata, action) {
								return '<a onclick="openFile(\'PRODUCT\', \'Production\', \'' + rowdata["requestNo"] + '\', \'' + rowdata["objectId"] + '\', \'' + rowdata["fileNo"] + '\')">' + cellValue + '</a>';
							}

							//파일다운				
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

							// 2023.07.24 기범추가 ( 등록 버튼 생성 )		
							function upload() {
								var popupHeight = Math.min($(window).height() - 100, 600);
								openDialogPopup(
									"/inside/distribution/productionRequest/productionRegisterPopup",
									{ treeCd: (window.productionRequestTreeState && productionRequestTreeState.selectedTreeCd) || "" },
									"popupDialog",
									'l',
									popupHeight,
									true,
									'popup-common popup-production-register'
								);
							}

							function deletePrd() {
								var list = [];
								if ($("#gridDistributionProductionRequestList").getGridParam('selarrrow').length < 1) {
									alertMessage(g_msg('msg.noSelectedItem'));
									return false;
								}

								$.each($("#gridDistributionProductionRequestList").getGridParam('selarrrow'), function (index, item) {
									var data = $("#gridDistributionProductionRequestList").jqGrid('getRowData', item);
									list.push({ objectId: data.objectId });
								});

								var param = { list: list };
								confirmMessage(g_msg('msg.requestDelete'), function () {
									$(this).dialog("close");
									callAjax(param, "/inside/distribution/productionRequest/delete", deletePrdCallback, 'json');
								});
							}

							function deletePrdCallback(response) {
								if (response.successCount > 0) {
									if (response.failCount > 0 && response.message) {
										alertMessage(g_msg('msg.completeDelete') + '\n' + response.message);
									} else {
										alertMessage(g_msg('msg.completeDelete'));
									}
									searchList(gridParam);
								} else if (response.message) {
									alertMessage(response.message);
								} else {
									alertMessage(g_msg('msg.failDelete'));
								}
							}

							function approvePrd() {
								var list = [];
								if ($("#gridDistributionProductionRequestList").getGridParam('selarrrow').length < 1) {
									alertMessage(g_msg('msg.noSelectedItem'));
									return false;
								}

								$.each($("#gridDistributionProductionRequestList").getGridParam('selarrrow'), function (index, item) {
									var data = $("#gridDistributionProductionRequestList").jqGrid('getRowData', item);
									list.push({ objectId: data.objectId });
								});

								var param = { list: list };
								confirmMessage('승인하시겠습니까?', function () {
									$(this).dialog("close");
									callAjax(param, "/inside/distribution/productionRequest/approve", approvePrdCallback, 'json');
								});
							}

							function approveProduction() {
								approvePrd();
							}

							function approvePrdCallback(response) {
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

							function showPrdStatusDetail(rowId) {
								var rowdata = $("#gridDistributionProductionRequestList").jqGrid('getRowData', rowId);
								if (!rowdata) {
									return;
								}
								openDialogPopup(
									"/inside/distribution/commonRequest/approvalStatusPopup",
									{
										objectId: rowdata.objectId,
										approveUrl: "/inside/distribution/productionRequest/approveStatusMessage",
										requestType: "PRODUCTION"
									},
									"popupDialog",
				"s",
				300,
				true,
									"popup-common popup-approval-status"
								);
							}

							function openProductionFilePopup(objectId, objectNo, requestNo, objectType) {
								openDialogPopup(
									"/inside/distribution/productionRequest/productionFilePopup",
									{ objectId: objectId, objectNo: objectNo, requestNo: "", objectType: "Production" },
									"popupDialog",
									"l",
									720,
									true,
									"popup-common popup-production-file"
								);
							}

							function bindProductionNoClick() {
								var $grid = $("#gridDistributionProductionRequestList");
								if (!$grid.length) return;

								var selector = [
									'td[aria-describedby="gridDistributionProductionRequestList_objectNo"]',
									'td[aria-describedby="gridDistributionProductionRequestList_documentNo"]',
									'td[aria-describedby="gridDistributionProductionRequestList_drawingNo"]',
									'td[aria-describedby="gridDistributionProductionRequestList_requestNo"]'
								].join(', ');
								$grid.find(selector).each(function () {
									var $td = $(this);
									if ($td.data('productionNoLinked')) return;

									var rowId = $td.closest('tr.jqgrow').attr('id');
									var text = $.trim($td.text());
									if (!rowId || text === '') return;

									var rowdata = $("#gridDistributionProductionRequestList").jqGrid('getRowData', rowId) || {};
									var objectId = rowdata.objectId || '';
									var objectNo = rowdata.objectNo || rowdata.documentNo || rowdata.drawingNo || text;
									var objectIdAttr = String(objectId).replace(/"/g, '&quot;');
									var objectNoAttr = String(objectNo).replace(/"/g, '&quot;');

									$td.data('productionNoLinked', true);
									$td.html('<a href="javascript:void(0);" class="production-no-link" data-row-id="' + rowId + '" data-object-id="' + objectIdAttr + '" data-object-no="' + objectNoAttr + '">' + text + '</a>');
								});
							}

							function bindPrdStatusClick() {
								return;
							}

							function hasProcessingRowsProduction() {
								var $grid = $("#gridDistributionProductionRequestList");
								if (!$grid.length) return false;
								var rowIds = $grid.jqGrid('getDataIDs') || [];
								for (var i = 0; i < rowIds.length; i++) {
									var raw = $grid.jqGrid('getLocalRow', rowIds[i]) || {};
									var processingStatus = String(raw.processingStatus || raw.processingstatus || raw.PROCESSING_STATUS || '').toUpperCase();
									if (processingStatus === 'PROCESSING') return true;
								}
								return false;
							}

							function pollProductionProcessingStatus() {
								if (document.hidden) return;
								if (hasProcessingRowsProduction()) {
									searchList(gridParam);
								}
								decorateProductionStatusBadges();
							}

							function decorateProductionStatusBadges() {
								var $grid = $("#gridDistributionProductionRequestList");
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
								$(document).on('click', '#gridDistributionProductionRequestList .prd-status-link', function (e) {
									e.preventDefault();
									showPrdStatusDetail($(this).data('rowId'));
								});
								$(document).on('click', '#gridDistributionProductionRequestList .production-no-link', function (e) {
									e.preventDefault();
									var objectId = $(this).data('objectId');
									var objectNo = $(this).data('objectNo') || $.trim($(this).text());
									var rowId = $(this).data('rowId');
									var rowdata = $("#gridDistributionProductionRequestList").jqGrid('getRowData', rowId) || {};
									openProductionFilePopup(
										objectId || rowdata.objectId || '',
										objectNo || rowdata.objectNo || rowdata.documentNo || rowdata.drawingNo || '',
										rowdata.requestNo || '',
										rowdata.objectType || 'DOC'
									);
								});
								setInterval(bindPrdStatusClick, 400);
								setInterval(bindProductionNoClick, 400);
								setInterval(pollProductionProcessingStatus, 5000);
								setInterval(decorateProductionStatusBadges, 500);
							});
						</script>
					</head>

					<body>
						<input type="hidden" id="sessionRoleGroup" name="sessionRoleGroup"
							value="${sessionUser.roleGroup}" />
						<input type="hidden" id="sessionAuthLevel" name="sessionAuthLevel"
							value="${sessionUser.authLevel}" />
						<div class="distribution-invoice-page">
							<custom:listTemplateInvoice gridId="gridDistributionProductionRequestList"
								treeId="productionRequestExplorerTree" treeTitle="MRB" />
						</div>
						<div id="searchAllPopup" class="dialogContainer"></div>
					</body>

					</html>
