<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
<script>
	window.USE_ACCEPTANCE_VUEXY_FORM = true;

	function setGridParam() {
		gridParam = {
			gridId : 'gridPeerReviewRequestList',
			formId : 'formPeerReviewRequest',
			url : '/inside/distribution/peerReview/selectList',
			size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
			page : 1,
			multiSelect : true,
			numbering : false,
			selectRowAction : 'check',
			layoutMode : 'invoice',
			fillColumns : true
		};
		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
		return '<a onclick="openPeerReviewFilePopup(\'' + rowdata["objectId"] + '\', \'' + (rowdata["peerReviewNo"] || cellValue) + '\', \'' + rowdata["requestNo"] + '\')">'+cellValue+'</a>';
	}

	function openPeerReviewFilePopup(objectId, peerReviewNo, requestNo) {
		var popupHeight = Math.min($(window).height() - 120, 720);
		openDialogPopup(
			"/inside/distribution/peerReview/peerReviewFilePopup",
			{ objectId: objectId, peerreviewNo: peerReviewNo, requestNo: requestNo },
			"popupDialog",
			'l',
			popupHeight,
			true,
			'popup-common popup-peerreview-file'
		);
	}

	function bindPeerReviewNoClick() {
		var $grid = $("#gridPeerReviewRequestList");
		if (!$grid.length) {
			return;
		}

		var selector = [
			'td[aria-describedby="gridPeerReviewRequestList_peerReviewNo"]',
			'td[aria-describedby="gridPeerReviewRequestList_peerreviewNo"]',
			'td[aria-describedby="gridPeerReviewRequestList_requestNo"]'
		].join(',');

		$grid.find(selector).each(function () {
			var $td = $(this);
			if ($td.data('peerReviewLinked')) {
				return;
			}
			var rowId = $td.closest('tr.jqgrow').attr('id');
			var text = $.trim($td.text());
			if (!rowId || text === '') {
				return;
			}

			var row = $("#gridPeerReviewRequestList").jqGrid('getLocalRow', rowId)
				|| $("#gridPeerReviewRequestList").jqGrid('getRowData', rowId)
				|| {};
			var objectId = row.objectId || row.OBJECT_ID || '';
			var peerReviewNo = row.peerReviewNo || row.peerreviewNo || row.requestNo || text;
			var requestNo = row.requestNo || '';

			$td.data('peerReviewLinked', true);
			$td.html(
				'<a href="javascript:void(0);" class="peerreview-no-link"'
				+ ' data-object-id="' + String(objectId).replace(/"/g, '&quot;') + '"'
				+ ' data-peerreview-no="' + String(peerReviewNo).replace(/"/g, '&quot;') + '"'
				+ ' data-request-no="' + String(requestNo).replace(/"/g, '&quot;') + '">'
				+ text + '</a>'
			);
		});
	}

	function upload() {
		openDialogPopup("/inside/distribution/peerReview/registerPopup", {}, "popupDialog", 'm', 320, true, 'popup-common');
	}

		function approvePeerReview() {
	var list = [];
	if ($("#gridPeerReviewRequestList").getGridParam('selarrrow').length < 1) {
		alertMessage('검토할 대상을 선택하세요');
		return false;
	}

	$.each($("#gridPeerReviewRequestList").getGridParam('selarrrow'), function (index, item) {
		var data = $("#gridPeerReviewRequestList").jqGrid('getLocalRow', item)
			|| $("#gridPeerReviewRequestList").jqGrid('getRowData', item)
			|| {};
		list.push({
			objectId: data.objectId || data.hiddenObjectId || data.OBJECT_ID || '',
			peerReviewNo: data.peerReviewNo || data.peerreviewNo || '',
			requestNo: data.requestNo || ''
		});
	});

	var param = { list: list };
	confirmMessage('검토하시겠습니까?', function () {
		$(this).dialog("close");
		callAjax(param, "/inside/distribution/peerReview/approve", approvePeerReviewCallback, 'json');
	});
}


	// toolbar媛 approveDrawing ?대쫫??李몄“?????덉뼱 alias ?좎?
	function approveDrawing() {
		approvePeerReview();
	}

	function approvePeerReviewCallback(response) {
		if (response.successCount > 0) {
			if (response.failCount > 0 && response.message) {
				alertMessage('일부 검토되었습니다. ' + response.message);
			} else {
				alertMessage('검토되었습니다.');
			}
			searchList(gridParam);
			return;
		}

		if (response.message) {
			alertMessage(response.message);
		} else {
			alertMessage('검토 처리에 실패했습니다.');
		}
	}

		function deletePeerReview() {
	var list = [];
	if ($("#gridPeerReviewRequestList").getGridParam('selarrrow').length < 1) {
		alertMessage('철회할 대상을 선택하세요');
		return false;
	}

	$.each($("#gridPeerReviewRequestList").getGridParam('selarrrow'), function (index, item) {
		var data = $("#gridPeerReviewRequestList").jqGrid('getLocalRow', item)
			|| $("#gridPeerReviewRequestList").jqGrid('getRowData', item)
			|| {};
		list.push({
			objectId: data.objectId || data.hiddenObjectId || data.OBJECT_ID || '',
			peerReviewNo: data.peerReviewNo || data.peerreviewNo || '',
			requestNo: data.requestNo || ''
		});
	});

	var param = { list: list };
	confirmMessage('철회하시겠습니까?', function () {
		$(this).dialog("close");
		callAjax(param, "/inside/distribution/peerReview/delete", deletePeerReviewCallback, 'json');
	});
}


	// toolbar媛 deleteDrawing ?대쫫??李몄“?????덉뼱 alias ?좎?
	function deleteDrawing() {
		deletePeerReview();
	}

	function deletePeerReviewCallback(response) {
		if (response.successCount > 0) {
			alertMessage('철회되었습니다.');
			searchList(gridParam);
		} else if (response.error) {
			alertMessage(response.error);
		} else {
			alertMessage('철회 처리에 실패했습니다.');
		}
	}

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
		$(document).on('click', '#gridPeerReviewRequestList .peerreview-no-link', function (e) {
			e.preventDefault();
			openPeerReviewFilePopup(
				$(this).data('objectId') || '',
				$(this).data('peerreviewNo') || $.trim($(this).text()),
				$(this).data('requestNo') || ''
			);
		});
		setInterval(bindPeerReviewNoClick, 400);
	});
</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridPeerReviewRequestList" />
	</div>
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>


