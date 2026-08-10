<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
		<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
		<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
			<c:set var="currentUrl" value="${requestScope['javax.servlet.forward.servlet_path']}" />
			<c:set var="matchedMenuLevel" value="0" />

			<c:forEach items="${sessionScope['scopedTarget.session'].menuSub }" var="menuSub">
				<c:if test="${currentUrl == menuSub.menuUrl && menuSub.menuLevel >= matchedMenuLevel}">
					<c:set var="menuTitle" value="${menuSub.menuNm}" />
					<c:set var="menuPath" value="${menuSub.menuPath}" />
					<c:set var="menuPathCd" value="${menuSub.menuPathCd}" />
					<c:set var="matchedMenuLevel" value="${menuSub.menuLevel}" />
				</c:if>
			</c:forEach>

			<script>
				var currentMenuNm = '${menuTitle}';
				var currentMenuPath = '${menuPath}';
				var currentMenuCd = '${menuCd}';
				var currentMenuPathCd = '${menuPathCd}';

				function getMenuPath() {
					var v = currentMenuPath.split(' > ');
					var result = [];

					$.each(v, function () {
						result.push('<span>' + this + '</span>');
					});

					return result;
				}

				function openMenu() {
					var arr = currentMenuPathCd.split(' > ');
					var i = 0;

					if ('' === arr[0]) {
						return;
					}

					for (i = 0; i < arr.length; i++) {
						if (i == arr.length - 1) {
							$("#" + arr[i]).addClass("active");
						}
					}

					$(".active").each(function () {
						$(this).parents("li.menu-item").addClass("open");
					});
				}

				$(document).ready(function () {
					if ('(구)배포이력' === currentMenuNm) {
						currentMenuNm += " (뷰, 출력 불가)";
					}

					$(".titleBox span").text(currentMenuNm);
					$(".navBox").html(getMenuPath());
					openMenu();
				});
			</script>

			<aside id="layout-menu" class="layout-menu menu-vertical menu">
				<div class="app-brand demo">
					<a href="${pageContext.request.contextPath}/general/distribution/swRequest/dashboard" class="app-brand-link"
					   aria-label="${tdmsBrand.alternate ? fn:escapeXml(tdmsBrand.logoAlt) : 'KAI'} dashboard">
						<c:choose>
							<c:when test="${tdmsBrand.alternate}">
								<img class="app-brand-logo app-brand-logo--wide"
									 src="${pageContext.request.contextPath}${tdmsBrand.logoLightPath}"
									 alt="${fn:escapeXml(tdmsBrand.logoAlt)}"
									 width="112" height="30" />
							</c:when>
							<c:otherwise>
								<img class="app-brand-kai-logo"
									 src="${pageContext.request.contextPath}/resources/images/brand/kai-logo.png"
									 width="72" height="44" alt="KAI" />
							</c:otherwise>
						</c:choose>
					</a>
						<a href="javascript:void(0);" class="layout-menu-toggle menu-link text-large ms-auto">
							<i class="icon-base ti menu-toggle-icon d-none d-xl-block"></i>
							<i class="icon-base ti tabler-x d-block d-xl-none"></i>
						</a>
				</div>

				<div class="menu-inner-shadow"></div>

				<ul class="menu-inner py-1" id="MENU_000">
					<c:forEach items="${sessionScope['scopedTarget.session'].menuTop }" var="menuTop">
						<c:if test="${menuTop.menuNm ne 'CR' }">
							<c:choose>
								<c:when test="${menuTop.menuCd eq 'MENU_013'}">
									<c:set var="menuIconClass" value="tabler-file-stack" />
								</c:when>
								<c:when test="${menuTop.menuCd eq 'MENU_229'}">
									<c:set var="menuIconClass" value="tabler-package-export" />
								</c:when>
								<c:when test="${menuTop.menuCd eq 'MENU_071'}">
									<c:set var="menuIconClass" value="tabler-users-group" />
								</c:when>
								<c:when test="${menuTop.menuCd eq 'MENU_214'}">
									<c:set var="menuIconClass" value="tabler-settings" />
								</c:when>
								<c:when test="${menuTop.menuCd eq 'MENU_223'}">
									<c:set var="menuIconClass" value="tabler-history" />
								</c:when>
								<c:otherwise>
									<c:set var="menuIconClass" value="tabler-folder" />
								</c:otherwise>
							</c:choose>
							<c:set var="hasTopChild" value="false" />
							<c:forEach items="${sessionScope['scopedTarget.session'].menuSub }" var="menuSubCheck">
								<c:if test="${menuTop.menuCd == menuSubCheck.parentMenuCd }">
									<c:set var="hasTopChild" value="true" />
								</c:if>
							</c:forEach>

							<li id="${menuTop.menuCd }" class="menu-item">
								<c:choose>
									<c:when test="${hasTopChild}">
										<a href="javascript:void(0);" class="menu-link menu-toggle" title="${menuTop.menuNm}">
											<span class="menu-top-icon" aria-hidden="true">
												<i class="icon-base ti ${menuIconClass}"></i>
											</span>
											<div>${menuTop.menuNm}</div>
										</a>
										<ul class="menu-sub">
											<c:forEach items="${sessionScope['scopedTarget.session'].menuSub }" var="menuSub">
												<c:if test="${menuTop.menuCd == menuSub.parentMenuCd }">
													<c:choose>
														<c:when test="${menuSub.treeType == 'leaf' }">
															<li id="${menuSub.menuCd }" class="menu-item">
																<a href="${menuSub.menuUrl }" class="menu-link">
																	<div>${menuSub.menuNm}</div>
																</a>
															</li>
														</c:when>
														<c:otherwise>
															<li class="menu-item">
																<a href="javascript:void(0);" class="menu-link menu-toggle">
																	<div>${menuSub.menuNm}</div>
																</a>
																<ul class="menu-sub">
																	<c:forEach
																		items="${sessionScope['scopedTarget.session'].menuSub }"
																		var="menuLeaf">
																		<c:if
																			test="${menuSub.menuCd == menuLeaf.parentMenuCd }">
																			<c:if test="${menuLeaf.menuType == 'M' }">
																				<li id="${menuLeaf.menuCd }" class="menu-item">
																					<a href="${menuLeaf.menuUrl }"
																						class="menu-link">
																						<div>${menuLeaf.menuNm}</div>
																					</a>
																				</li>
																			</c:if>
																		</c:if>
																	</c:forEach>
																</ul>
															</li>
														</c:otherwise>
													</c:choose>
												</c:if>
											</c:forEach>
										</ul>
									</c:when>
									<c:otherwise>
										<c:set var="topMenuUrl" value="${menuTop.menuUrl }" />
										<c:if test="${not empty topMenuUrl}">
											<c:set var="topMenuUrl" value="${fn:replace(topMenuUrl, '/**', '/')}" />
										</c:if>
										<a href="${topMenuUrl }" class="menu-link" title="${menuTop.menuNm}">
											<span class="menu-top-icon" aria-hidden="true">
												<i class="icon-base ti ${menuIconClass}"></i>
											</span>
											<div>${menuTop.menuNm}</div>
										</a>
									</c:otherwise>
								</c:choose>
							</li>
						</c:if>
					</c:forEach>
				</ul>
			</aside>
