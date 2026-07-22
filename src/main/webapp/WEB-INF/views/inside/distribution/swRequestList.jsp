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
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/swRequestList.js"></script>
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
				alertMessage(g_msg('msg.noSelectedItem'));
				return false;
			}

			$.each($("#gridSwRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridSwRequestList").jqGrid('getRowData', item);
				console.log(data.objectId);
				list.push({ objectId: data.objectId });
			});
			var param = { list: list };
			console.log(param);
			confirmMessage(g_msg('msg.requestDelete'), function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/swRequest/delete", deleteSWCallback, 'json');
			});
		}

		function deleteSWCallback(response) {
			if (response.successCount > 0) {
				alertMessage(g_msg('msg.completeDelete'));
				searchList(gridParam);
			} else if (response.error) {
				alertMessage(response.error); //"삭제 권한이 없습니다."
			} else {
				alertMessage(g_msg('msg.failDelete')); // 기타 실패
			}

		}

		function approveSW() {
			var list = [];
			if ($("#gridSwRequestList").getGridParam('selarrrow').length < 1) {
				alertMessage(g_msg('msg.noSelectedItem'));
				return false;
			}

			$.each($("#gridSwRequestList").getGridParam('selarrrow'), function (index, item) {
				var data = $("#gridSwRequestList").jqGrid('getRowData', item);
				list.push({ objectId: data.objectId });
			});

			var param = { list: list };
			confirmMessage('승인하시겠습니까?', function () {
				$(this).dialog("close");
				callAjax(param, "/inside/distribution/swRequest/approve", approveSWCallback, 'json');
			});
		}

		function approveSWCallback(response) {
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
					$statusTd.append(' <span class="ch-badge ch-badge-processing ch-processing-badge">처리중</span>');
				} else if (processingStatus === 'FAIL') {
					$statusTd.append(' <span class="ch-badge ch-badge-fail ch-processing-badge">처리실패</span>');
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
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridSwRequestList" treeId="swRequestExplorerTree" treeTitle="CCB" />
	</div>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>

</html>
