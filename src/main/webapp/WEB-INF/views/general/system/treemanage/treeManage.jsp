<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:message code="feature.locale.code" text="ko" var="pageLanguage" />
<spring:message code="feature.treeManage.pageTitle" text="분류/레벨 관리 | 기술자료관리시스템" var="pageTitle" />
<spring:message code="feature.treeManage.title" text="분류/레벨 관리" var="pageHeading" />
<spring:message code="feature.treeManage.level.title" text="Level" var="levelTitle" />
<spring:message code="feature.treeManage.level.parent" text="상위 Level" var="parentLevelTitle" />
<spring:message code="feature.treeManage.level.child" text="하위 Level" var="childLevelTitle" />
<spring:message code="feature.treeManage.documentType.title" text="Document Type Code" var="documentTypeTitle" />
<spring:message code="feature.common.countSuffix" text="건" var="countSuffix" />
<!doctype html>
<html lang="${pageLanguage}">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${pageTitle}</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/tree-management.css?v=20260803.1" media="screen" />
<script>
	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/general/system/treemanage/treeManage.js?v=20260803.1"></script>
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
</head>
<body>
<main class="system-manage-page" aria-labelledby="treeManagePageTitle">
	<header class="tm-page-header">
		<div class="tm-page-heading">
			<span class="tm-kicker">
				<i class="icon-base ti tabler-hierarchy-2" aria-hidden="true"></i>
				${levelTitle}
			</span>
			<h1 id="treeManagePageTitle">${pageHeading}</h1>
		</div>
		<span class="tm-context-chip">
			<i class="icon-base ti tabler-adjustments-code" aria-hidden="true"></i>
			${levelTitle}
		</span>
	</header>

	<section class="tm-workspace-card" aria-label="${pageHeading}">
		<div id="treeManageLayout" class="layout">
			<section id="leftTreePanel" class="panel" aria-labelledby="treeMainTitle">
				<header class="tm-section-header">
					<span class="tm-section-icon" aria-hidden="true">
						<i class="icon-base ti tabler-sitemap"></i>
					</span>
					<h2 id="treeMainTitle" class="panel-title">${levelTitle}</h2>
				</header>

				<div class="codes">
					<article class="col tm-tree-column" id="treeCol2Panel" aria-labelledby="treeCol1Title">
						<header class="tm-column-header">
							<div>
								<span class="tm-step-chip" aria-hidden="true">1</span>
								<h3 id="treeCol1Title" class="col-title">${parentLevelTitle}</h3>
							</div>
							<span id="function1Count" class="tm-count-chip">0${countSuffix}</span>
						</header>
						<div id="levelActions" class="actions">
							<button type="button" class="ui-button ui-corner-all tm-button tm-button--primary" onclick="addFunction1()" aria-controls="function1List">
								<i class="icon-base ti tabler-plus" aria-hidden="true"></i>
								<spring:message code="feature.common.button.add" text="추가" />
							</button>
							<button type="button" class="ui-button ui-corner-all tm-button tm-button--ghost" onclick="editFunction1()" aria-controls="function1List">
								<i class="icon-base ti tabler-edit" aria-hidden="true"></i>
								<spring:message code="feature.common.button.edit" text="수정" />
							</button>
							<button type="button" class="ui-button ui-corner-all tm-button tm-button--danger" onclick="deleteFunction1()" aria-controls="function1List">
								<i class="icon-base ti tabler-trash" aria-hidden="true"></i>
								<spring:message code="feature.common.button.delete" text="삭제" />
							</button>
						</div>
						<div id="function1List" class="list" role="listbox" aria-labelledby="treeCol1Title"></div>
					</article>

					<article class="col tm-tree-column" aria-labelledby="treeCol2Title">
						<header class="tm-column-header">
							<div>
								<span class="tm-step-chip" aria-hidden="true">2</span>
								<h3 id="treeCol2Title" class="col-title">${childLevelTitle}</h3>
							</div>
							<span id="function2Count" class="tm-count-chip">0${countSuffix}</span>
						</header>
						<div class="actions">
							<button type="button" class="ui-button ui-corner-all tm-button tm-button--primary" onclick="addFunction2()" aria-controls="function2List">
								<i class="icon-base ti tabler-plus" aria-hidden="true"></i>
								<spring:message code="feature.common.button.add" text="추가" />
							</button>
							<button type="button" class="ui-button ui-corner-all tm-button tm-button--ghost" onclick="editFunction2()" aria-controls="function2List">
								<i class="icon-base ti tabler-edit" aria-hidden="true"></i>
								<spring:message code="feature.common.button.edit" text="수정" />
							</button>
							<button type="button" class="ui-button ui-corner-all tm-button tm-button--danger" onclick="deleteFunction2()" aria-controls="function2List">
								<i class="icon-base ti tabler-trash" aria-hidden="true"></i>
								<spring:message code="feature.common.button.delete" text="삭제" />
							</button>
						</div>
						<div id="function2List" class="list" role="listbox" aria-labelledby="treeCol2Title"></div>
					</article>
				</div>
			</section>

			<section id="docTypePanel" class="panel tm-tree-column" aria-labelledby="docTypeTitle">
				<header class="tm-column-header">
					<div>
						<span class="tm-step-chip" aria-hidden="true">3</span>
						<h2 id="docTypeTitle" class="panel-title">${documentTypeTitle}</h2>
					</div>
					<span id="docTypeCount" class="tm-count-chip">0${countSuffix}</span>
				</header>
				<div class="actions">
					<button type="button" class="ui-button ui-corner-all tm-button tm-button--primary" onclick="addDocType()" aria-controls="docTypeList">
						<i class="icon-base ti tabler-plus" aria-hidden="true"></i>
						<spring:message code="feature.common.button.add" text="추가" />
					</button>
					<button type="button" class="ui-button ui-corner-all tm-button tm-button--ghost" onclick="editDocType()" aria-controls="docTypeList">
						<i class="icon-base ti tabler-edit" aria-hidden="true"></i>
						<spring:message code="feature.common.button.edit" text="수정" />
					</button>
					<button type="button" class="ui-button ui-corner-all tm-button tm-button--danger" onclick="deleteDocType()" aria-controls="docTypeList">
						<i class="icon-base ti tabler-trash" aria-hidden="true"></i>
						<spring:message code="feature.common.button.delete" text="삭제" />
					</button>
				</div>
				<div id="docTypeList" class="list" role="listbox" aria-labelledby="docTypeTitle"></div>
			</section>
		</div>
	</section>
</main>
</body>
</html>
