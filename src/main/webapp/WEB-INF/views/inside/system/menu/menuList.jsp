<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script>
	var insideTreeList = '${insideTreeList}';
	var outsideTreeList = '${outsideTreeList}';
	var insideToolbarInfo = '${insideToolbarInfo }';
	var outsideToolbarInfo = '${outsideToolbarInfo }';

	$(function () {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});
</script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/system/menu/menuList-vuexy.js"></script>
<style>
	.system-menu-page .system-menu-card {
		border: 0;
	}

	.system-menu-page .system-menu-card .card-body {
		padding: 1.25rem;
	}

	.system-menu-page .nav-align-top > .nav {
		gap: 0.5rem;
		border-bottom: 0;
		margin-bottom: 10px;
	}

	.system-menu-page .nav-pills .nav-link {
		border: 1px solid transparent;
		border-radius: 999px;
		padding: 0.55rem 1rem;
		color: var(--bs-secondary-color);
		font-weight: 600;
	}

	.system-menu-page .nav-pills .nav-link.active {
		background: rgba(var(--bs-primary-rgb), 0.12);
		color: var(--bs-primary);
		border-color: rgba(var(--bs-primary-rgb), 0.2);
		box-shadow: none;
	}

	.system-menu-page .tab-content {
		padding: 1.25rem 0 0;
		box-shadow: none !important;
	}

	.system-menu-page .menu-pane {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		min-width: 0;
	}

	.system-menu-page .menu-pane .btnArea {
		margin: 0;
	}

	.system-menu-page .menu-pane .btnArea:not(.is-empty) {
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 0.5rem;
	}

	.system-menu-page .menu-pane .btnArea .left,
	.system-menu-page .menu-pane .btnArea .right {
		display: flex;
		gap: 0.5rem;
		width: auto !important;
	}

	.system-menu-page .menu-pane .btnArea .right {
		margin-left: auto;
	}

	.system-menu-page .menu-pane .btnArea .ui-button {
		min-width: 5rem;
		padding: 0.4rem 0.9rem;
		border: 1px solid var(--bs-primary);
		border-radius: var(--bs-border-radius);
		background: transparent;
		color: var(--bs-primary);
		font-size: 0.8125rem;
		font-weight: 500;
		line-height: 1.2;
		box-shadow: none;
	}

	.system-menu-page .menu-pane .tree-card {
		border: 1px solid var(--bs-border-color);
		border-radius: var(--bs-border-radius-lg);
		background: linear-gradient(180deg, rgba(var(--bs-body-color-rgb), 0.02) 0%, rgba(var(--bs-body-bg-rgb), 0.98) 100%);
		padding: 1rem 1rem 1.25rem;
		min-height: 32rem;
		max-height: calc(100vh - 320px);
		overflow: auto;
		/* box-shadow: 0 0.25rem 1rem rgba(34, 48, 62, 0.08); */
		text-align: left;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default {
		min-width: 100%;
		font-size: 0.875rem;
		color: var(--bs-body-color);
		text-align: left;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-container-ul {
		width: 100%;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-node {
		position: relative;
		margin-left: 1.5rem;
		min-height: 2.25rem;
		line-height: 2.25rem;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-anchor {
		height: 2.25rem;
		line-height: 2.25rem;
		padding: 0 0.75rem 0 0.25rem;
		border-radius: 0.75rem;
		font-weight: 500;
		transition: background-color 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-hovered,
	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-wholerow-hovered {
		background: rgba(var(--bs-primary-rgb), 0.08);
		box-shadow: none;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-clicked,
	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-wholerow-clicked {
		background: rgba(var(--bs-primary-rgb), 0.14);
		color: var(--bs-primary);
		/* box-shadow: inset 0 0 0 1px rgba(var(--bs-primary-rgb), 0.18); */
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-icon,
	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-icon:empty {
		width: 2rem;
		height: 2.25rem;
		line-height: 2.25rem;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-ocl {
		opacity: 0.7;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-wholerow {
		height: 2.25rem;
		border-radius: 0.75rem;
		background: transparent !important;
		box-shadow: none !important;
		border: 0 !important;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-wholerow-hovered,
	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-wholerow-clicked {
		background: transparent !important;
		box-shadow: none !important;
		border: 0 !important;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-themeicon {
		margin-right: 0.25rem;
	}

	.system-menu-page .menu-pane .tree-card .jstree-default .jstree-anchor:hover {
		color: var(--bs-primary);
	}

	.system-menu-page #jstree-marker {
		border-left-color: var(--bs-primary);
	}

	.system-menu-page #jstree-dnd {
		padding: 0.45rem 0.65rem;
		border: 1px solid rgba(var(--bs-primary-rgb), 0.18);
		border-radius: 0.75rem;
		background: var(--bs-body-bg);
		box-shadow: 0 0.375rem 1rem rgba(34, 48, 62, 0.14);
		color: var(--bs-body-color);
	}

	@media (max-height: 900px) {
		.system-menu-page .menu-pane .tree-card {
			max-height: calc(100vh - 280px);
		}
	}

	@media (max-width: 991.98px) {
		.system-menu-page .menu-pane .tree-card {
			min-height: 24rem;
			max-height: 24rem;
		}
	}
</style>
</head>
<body>
	<div class="system-menu-page">
		<div class="card system-menu-card">
			<div class="card-body">
				<div class="nav-align-top">
					<ul class="nav nav-pills" role="tablist">
						<li class="nav-item" role="presentation">
							<button type="button" class="nav-link active" id="inside-menu-tab" data-bs-toggle="tab" data-bs-target="#inside-menu-pane" role="tab" aria-controls="inside-menu-pane" aria-selected="true">내부메뉴 수정</button>
						</li>
						<li class="nav-item" role="presentation">
							<button type="button" class="nav-link" id="outside-menu-tab" data-bs-toggle="tab" data-bs-target="#outside-menu-pane" role="tab" aria-controls="outside-menu-pane" aria-selected="false">외부메뉴 수정</button>
						</li>
					</ul>
					<div class="tab-content">
						<div class="tab-pane fade show active" id="inside-menu-pane" role="tabpanel" aria-labelledby="inside-menu-tab">
							<div class="menu-pane">
								<div class="btnArea" id="insideBtnArea"></div>
								<div class="tree-card">
									<div id="insideMenuTree"></div>
								</div>
							</div>
						</div>
						<div class="tab-pane fade" id="outside-menu-pane" role="tabpanel" aria-labelledby="outside-menu-tab">
							<div class="menu-pane">
								<div class="btnArea" id="outsideBtnArea"></div>
								<div class="tree-card">
									<div id="outsideMenuTree"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
