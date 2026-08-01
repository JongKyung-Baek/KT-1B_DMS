<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<style>
	.popup-approval-status.ui-dialog,
	.popup-approval-status {
		width: 320px !important;
		max-width: 320px !important;
	}
	.approvalStatusPopup {
		padding: 6px 8px 0 8px;
	}
	.approvalStatusPopup .popupHero {
		padding: 4px 0 8px 0;
	}
	.approvalStatusPopup .popupHero h2 {
		margin-top: 6px !important;
		margin-bottom: 4px;
		font-size: 18px;
	}
	.approvalStatusPopup .popupHero p {
		margin: 0 0 6px 0;
		font-size: 13px;
	}
	.approvalStatusPopup #approvalStatusTableWrap {
		padding: 8px;
		max-height: 170px !important;
		min-height: 170px !important;
		overflow-y: auto;
	}
	.approvalStatusPopup + #approvalStatusBtnSet {
		margin-top: 8px;
		padding-top: 0;
	}
	.approvalStatusPopup.has-scroll + #approvalStatusBtnSet {
		margin-top: 12px;
	}
	.approvalStatusPopup + #approvalStatusBtnSet .left {
		display: none;
	}
	.approvalStatusPopup + #approvalStatusBtnSet .right {
		width: 100%;
		text-align: center;
	}
</style>
<div class="dialogContent approvalStatusPopup popup-base popup-actions-center">
	<div class="popupHero">
		<h2>승인 상태</h2>
		<p>승인 단계 진행 상태를 확인해 주세요.</p>
	</div>
	<div class="popupCard" id="approvalStatusTableWrap">
		<table style="width:100%; border-collapse: collapse; table-layout: fixed;">
			<thead>
				<tr>
					<th style="border:1px solid #d9d9d9; background:#f7f8fa; padding:8px;">승인자/검토자</th>
					<th style="border:1px solid #d9d9d9; background:#f7f8fa; padding:8px;">상태</th>
				</tr>
			</thead>
			<tbody id="approvalStatusPopupBody">
				<tr>
					<td colspan="2" style="border:1px solid #d9d9d9; padding:12px; text-align:center;">조회 중...</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>
<div class="dialogBtnSet" id="approvalStatusBtnSet">
	<div class="right">
		<button type="button" id="close" name="close" class="ui-button ui-corner-all bottomBtn popupActionBtn neutral" onclick="closePopup('popupDialog')">닫기</button>
	</div>
</div>
<script type="text/javascript">
	(function() {
		function escapeHtml(text) {
			return String(text || "").replace(/[&<>"']/g, function(ch) {
				return { "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;" }[ch];
			});
		}

		function parseRows(message) {
			var rows = [];
			var lines = String(message || "").split(/\r\n|\r|\n/g);
			$.each(lines, function(_, line) {
				var text = $.trim(line);
				if (!text) {
					return;
				}
				var split = text.indexOf(" : ") > -1 ? text.split(" : ") : text.split(":");
				var approver = $.trim(split.shift() || "-");
				var status = $.trim(split.join(":") || "-");
				rows.push({ approver: approver, status: status });
			});
			return rows;
		}

		function updatePopupLayout() {
			var $root = $(".approvalStatusPopup");
			var wrap = document.getElementById("approvalStatusTableWrap");
			if (!wrap) {
				return;
			}
			var hasScroll = wrap.scrollHeight > (wrap.clientHeight + 1);
			$root.toggleClass("has-scroll", hasScroll);
		}

		function renderRows(rows) {
			var html = [];
			if (!rows.length) {
				html.push('<tr><td colspan="2" style="border:1px solid #d9d9d9; padding:12px; text-align:center;">데이터가 없습니다.</td></tr>');
			} else {
				$.each(rows, function(_, row) {
					html.push('<tr>');
					html.push('<td style="border:1px solid #d9d9d9; padding:8px;">' + escapeHtml(row.approver) + '</td>');
					html.push('<td style="border:1px solid #d9d9d9; padding:8px; text-align:center;">' + escapeHtml(row.status) + '</td>');
					html.push('</tr>');
				});
			}
			$("#approvalStatusPopupBody").html(html.join(""));
			updatePopupLayout();
		}

		var objectId = "${fn:escapeXml(param.objectId)}";
		var approveUrl = "${fn:escapeXml(param.approveUrl)}";
		if (!objectId || !approveUrl) {
			renderRows([]);
			return;
		}

		callAjax({ objectId: objectId }, approveUrl, function(response) {
			renderRows(parseRows(response.message));
		}, "json");
	})();
</script>
