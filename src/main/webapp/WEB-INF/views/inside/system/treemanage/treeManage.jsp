<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>시스템 관리</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script>
	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/system/treemanage/treeManage.js"></script>
<script>
	// 캐시된 구버전 JS가 로드된 경우에도 모드 전환 버튼이 죽지 않도록 안전망
	window.switchManageMode = window.switchManageMode || function (mode) {
		window.currentManageType = mode || 'LEVEL';
		if (window.jQuery) {
			$('#modeDocBtn').toggleClass('active', window.currentManageType === 'DOC');
			$('#modeLevelBtn').toggleClass('active', window.currentManageType === 'LEVEL');
		}
		if (typeof window.loadFunction1 === 'function') {
			window.selected1 = null;
			window.selected2 = null;
			window.selectedDoc = null;
			window.loadFunction1();
		}
	};
</script>
<style>
	.system-manage-page .wrap { border: 1px solid #d9dee3; border-radius: 8px; background: #fff; padding: 1rem; }
	.system-manage-page .title { font-size: 1.05rem; font-weight: 700; margin-bottom: 0.8rem; }
	.system-manage-page .mode-switch { display: flex; gap: 0.5rem; margin-bottom: 0.9rem; }
	.system-manage-page .mode-btn { border: 1px solid #c9ced6; background: #f7f8fa; color: #2e3440; border-radius: 6px; padding: 0.45rem 0.8rem; cursor: pointer; font-weight: 600; }
	.system-manage-page .mode-btn.active { background: #034C8C; color: #fff; border-color: #034C8C; }
	.system-manage-page .layout { display: grid; grid-template-columns: 2fr 1fr; gap: 1rem; }
	.system-manage-page .panel { border: 1px solid #d9dee3; border-radius: 8px; padding: 0.75rem; }
	.system-manage-page .panel-title { font-weight: 700; margin-bottom: 0.6rem; text-align: center; }
	.system-manage-page .codes { display: grid; grid-template-columns: 1fr 1fr; gap: 0.7rem; }
	.system-manage-page .col { border: 1px solid #e5e7eb; border-radius: 6px; padding: 0.6rem; }
	.system-manage-page .col-title { font-weight: 600; margin-bottom: 0.45rem; text-align: center; }
	.system-manage-page .actions { display: flex; justify-content: center; gap: 0.35rem; margin-bottom: 0.45rem; }
	.system-manage-page .actions .ui-button { min-width: 62px; }
	.system-manage-page .list { border: 1px solid #d9dee3; border-radius: 6px; min-height: 300px; max-height: 420px; overflow: auto; background: #fff; }
	.system-manage-page .list-item { padding: 0.45rem 0.5rem; border-bottom: 1px solid #f0f1f3; cursor: pointer; }
	.system-manage-page .list-item:last-child { border-bottom: 0; }
	.system-manage-page .list-item.active { background: color-mix(in sRGB, var(--bs-paper-bg) var(--bs-bg-label-tint-amount), var(--bs-primary)); color: var(--bs-primary); }
	.system-manage-page.level-mode #treeMainTitle { display: none; }
	.system-manage-page.level-mode #leftTreePanel.level-flat { padding: 0; border: 0; background: transparent; }
	.system-manage-page.level-mode #leftTreePanel.level-flat .codes { gap: 0; }
	.system-manage-page.level-mode #leftTreePanel.level-flat .col { border: 0; padding: 0; }
	@media (max-width: 1200px){
		.system-manage-page .layout { grid-template-columns: 1fr; }
		.system-manage-page .codes { grid-template-columns: 1fr; }
	}
</style>
</head>
<body>
<div class="system-manage-page">
	<div class="wrap">
		<div class="title"><spring:message code='menu.sysmanage'></spring:message></div>
		<div class="mode-switch">
			<!-- <button type="button" id="modeDocBtn" class="mode-btn active" onclick="switchManageMode('DOC')">문서 코드 번호 등록 및 수정</button> -->
			<!-- <button type="button" id="modeLevelBtn" class="mode-btn active" onclick="switchManageMode('LEVEL')">Level 등록 및 수정</button> -->
		</div>
		<div id="treeManageLayout" class="layout">
			<div id="leftTreePanel" class="panel">
				<div id="treeMainTitle" class="panel-title">Level</div>
				<div class="codes">
					<div class="col" id="treeCol2Panel">
						<div id="treeCol1Title" class="col-title"><spring:message code='label.highlevel'></spring:message></div>
						<div id="levelActions" class="actions">
							<button type="button" class="ui-button ui-corner-all" onclick="addFunction1()"><spring:message code='btn.addBtn'></spring:message></button>
							<button type="button" class="ui-button ui-corner-all" onclick="editFunction1()"><spring:message code='btn.editBtn'></spring:message></button>
							<button type="button" class="ui-button ui-corner-all" onclick="deleteFunction1()"><spring:message code='btn.deleteBtn'></spring:message></button>
						</div>
						<div id="function1List" class="list"></div>
					</div>
					<div class="col">
						<div id="treeCol2Title" class="col-title"><spring:message code='label.sublevel'></spring:message></div>
						<div class="actions">
							<button type="button" class="ui-button ui-corner-all" onclick="addFunction2()"><spring:message code='btn.addBtn'></spring:message></button>
							<button type="button" class="ui-button ui-corner-all" onclick="editFunction2()"><spring:message code='btn.editBtn'></spring:message></button>
							<button type="button" class="ui-button ui-corner-all" onclick="deleteFunction2()"><spring:message code='btn.deleteBtn'></spring:message></button>
						</div>
						<div id="function2List" class="list"></div>
					</div>
				</div>
			</div>
			<div id="docTypePanel" class="panel">
				<div class="panel-title">Document Type Code</div>
				<div class="actions">
					<button type="button" class="ui-button ui-corner-all" onclick="addDocType()"><spring:message code='btn.addBtn'></spring:message></button>
					<button type="button" class="ui-button ui-corner-all" onclick="editDocType()"><spring:message code='btn.editBtn'></spring:message></button>
					<button type="button" class="ui-button ui-corner-all" onclick="deleteDocType()"><spring:message code='btn.deleteBtn'></spring:message></button>
				</div>
				<div id="docTypeList" class="list"></div>
			</div>
		</div>
	</div>
</div>
</body>
</html>
