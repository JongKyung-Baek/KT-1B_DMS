<%@ page language="java" contentType="text/html; charset=utf-8"
		 pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<script>
	var defaultSessionTime = ${sessionScope['scopedTarget.session'].timeoutSecond};
	var timerchecker = null;
	var auditLogoutSkip = false;
	var auditInternalNavigation = false;
	var auditLeaveNotified = false;

	function markAuditInternalNavigation() {
		auditInternalNavigation = true;
	}

	function skipAuditLogoutOnLeave() {
		auditLogoutSkip = true;
	}

	function notifyLogoutOnLeave() {
		if (auditLeaveNotified || auditLogoutSkip || auditInternalNavigation) {
			return;
		}

		auditLeaveNotified = true;

		if (navigator.sendBeacon) {
			var logoutBlob = new Blob(['leave'], {type: 'text/plain;charset=UTF-8'});
			navigator.sendBeacon('/inside/organizationmanage/auditlog/notifyLogoutOnLeave', logoutBlob);
			return;
		}

		if (window.fetch) {
			fetch('/inside/organizationmanage/auditlog/notifyLogoutOnLeave', {
				method: 'POST',
				body: 'leave',
				headers: {'Content-Type': 'text/plain;charset=UTF-8'},
				keepalive: true
			});
		}
	}

	function clearPendingLogoutOnStay() {
		$.ajax({
			url: '/inside/organizationmanage/auditlog/clearPendingLogoutOnStay',
			type: 'GET'
		});
	}

	function isSameOriginNavigation(href) {
		try {
			return new URL(href, window.location.href).origin === window.location.origin;
		} catch (e) {
			return false;
		}
	}

	var SessionTimer = (function() {
		var expireAt = 0;

		function init(initialTime) {
			var timeoutSeconds = parseInt(initialTime, 10);
			if (!timeoutSeconds || timeoutSeconds < 1) {
				timeoutSeconds = defaultSessionTime;
			}
			expireAt = Date.now() + (timeoutSeconds * 1000);
			startTimer();
		}

		function startTimer() {
			clearSessionTime();
			tick();
		}

		function tick() {
			var target = document.getElementById("sessionTime");
			var remainingTime = Math.ceil((expireAt - Date.now()) / 1000);
			if (remainingTime <= 0) {
				if (target) {
					target.innerText = formatTime(0);
				}
				skipAuditLogoutOnLeave();
				alertMessage(g_msg('msg.sessionTimeout'), function() {
					logout();
					$(this).dialog("close");
				});
				return;
			}

			if (target) {
				target.innerText = formatTime(remainingTime);
			}
			timerchecker = setTimeout(tick, 1000);
		}

		function updateTime(newTime) {
			init(newTime);
		}

		function clearSessionTime() {
			if (timerchecker) {
				clearTimeout(timerchecker);
			}
		}

		function formatTime(seconds) {
			var minutes = Math.floor(seconds / 60);
			var remainingSeconds = seconds % 60;
			return String(minutes).padStart(2, '0') + 'm ' + String(remainingSeconds).padStart(2, '0') + 's';
		}

		return {
			init: init,
			updateTime: updateTime
		};
	})();

	$(document).ready(function(){
		$(document).on('click', 'a[href]', function() {
			var href = $(this).attr('href');
			if (!href || href.indexOf('javascript:') === 0 || href.indexOf('#') === 0) {
				return;
			}

			if (isSameOriginNavigation(href)) {
				markAuditInternalNavigation();
			}
		});

		$(document).on('submit', 'form', function() {
			markAuditInternalNavigation();
		});

		$(document).on('keydown', function(event) {
			if (event.key === 'F5' || ((event.ctrlKey || event.metaKey) && (event.key === 'r' || event.key === 'R'))) {
				markAuditInternalNavigation();
			}
		});

		window.addEventListener('pagehide', function(pageEvent) {
			if (pageEvent.persisted) {
				return;
			}

			notifyLogoutOnLeave();
		});

		window.addEventListener('pageshow', function() {
			auditLogoutSkip = false;
			auditInternalNavigation = false;
			auditLeaveNotified = false;
			clearPendingLogoutOnStay();
		});

		clearPendingLogoutOnStay();

		$.ajax({
			url: '/getRemainingSessionTime',
			type: 'GET',
			success: function (data) {
				SessionTimer.init(data.remainingTime);
			}
		});
	});

	function extendSession() {
		$.ajax({
			url: '/extendSession',
			type: 'POST',
			success: function (response) {
				if (response.success) {
					$.ajax({
						url: '/getRemainingSessionTime',
						type: 'GET',
						success: function (data) {
							SessionTimer.updateTime(data.remainingTime);
						}
					});
				} else {
					alert('연장에 실패했습니다.');
				}
			}
		});
	}

	function logout(){
		try { clearTimeout(timerchecker); } catch (e) {}
		try { skipAuditLogoutOnLeave(); } catch (e) {}
		try { markAuditInternalNavigation(); } catch (e) {}
		window.location.replace("/login/logout");
	}

	function checkSession() {
		$.ajax({
			url: '/checkSession',
			type: 'GET',
			success: function(response) {
				if(response.sessionExpired) {
					skipAuditLogoutOnLeave();
					alertMessage(g_msg('msg.sessionTimeout'), function() {
						window.location.href = '/login/loginPage';
						$(this).dialog("close");
					});
				} else {
					setTimeout(checkSession, 300000);
				}
			},
			error: function() {
				window.location.replace('/login/loginPage');
			}
		});
	}

	// Disabled: periodic session polling refreshes the server session and prevents timeout.
	// checkSession();
</script>

<nav
	class="layout-navbar container-xxl navbar-detached navbar navbar-expand-xl align-items-center bg-navbar-theme"
	id="layout-navbar">
	<div class="layout-menu-toggle navbar-nav align-items-xl-center me-3 me-xl-0 d-xl-none">
		<a class="nav-item nav-link px-0 me-xl-6" href="javascript:void(0)">
			<i class="icon-base ti tabler-menu-2 icon-md"></i>
		</a>
	</div>

	<div class="navbar-title-area">
		<span class="system-title"><spring:message code='label.tdms'></spring:message></span>
	</div>

	<div class="navbar-nav-right d-flex align-items-center justify-content-end" id="navbar-collapse" style="padding: 5px 0;">
		<ul class="navbar-nav d-flex flex-row flex-wrap align-items-center justify-content-end ms-md-auto">
			<li class="nav-item me-2">
				<!-- <button type="button" class="btn btn-sm btn-outline-primary"><spring:message code='msg.systemInquiry'/></button> -->
			</li>
			<li class="nav-item me-3 text-body-secondary small">
				<span id="sessionTime"></span>
			</li>
			<li class="nav-item me-2">
				<button type="button" class="btn btn-sm btn-label-primary" onclick="extendSession()"><spring:message code='btn.extend'></spring:message></button>
			</li>
			<li class="nav-item navbar-dropdown dropdown-user dropdown">
				<a class="nav-link dropdown-toggle hide-arrow p-0" href="javascript:void(0);" data-bs-toggle="dropdown" aria-expanded="false">
					<div class="avatar avatar-online">
						<span class="avatar-initial rounded-circle bg-label-primary">${sessionUser.userNm}</span>
					</div>
				</a>
				<ul class="dropdown-menu dropdown-menu-end">
					<li>
						<div class="dropdown-item mt-0">
							<div class="d-flex align-items-center">
								<div class="flex-shrink-0 me-2">
									<div class="avatar avatar-online">
										<span class="avatar-initial rounded-circle bg-label-primary">${sessionUser.userNm}</span>
									</div>
								</div>
								<div class="flex-grow-1">
									<h6 class="mb-0">${sessionUser.userNm}</h6>
									<!-- <small class="text-body-secondary">${sessionUser.userId}</small> -->
								</div>
							</div>
						</div>
					</li>
					<li><div class="dropdown-divider my-1 mx-n2"></div></li>
					<c:if test="${'RG_001' == sessionUser.roleGroup}">
						<li>
							<a class="dropdown-item" href="javascript:void(0);" onclick="userChange()">
								<i class="icon-base ti tabler-user-cog me-3 icon-md"></i>
								<span class="align-middle"><spring:message code='label.userchange'></spring:message></span>
							</a>
						</li>
					</c:if>
					<li>
						<div class="d-grid px-2 pt-2 pb-1">
							<a class="btn btn-sm btn-danger d-flex align-items-center justify-content-center" href="javascript:void(0);" onclick="logout()">
								<small class="align-middle"><spring:message code='btn.logout'/></small>
								<i class="icon-base ti tabler-logout ms-2 icon-14px"></i>
							</a>
						</div>
					</li>
				</ul>
			</li>
		</ul>
	</div>
</nav>

<div id="popupDialog" class="dialogContainer"></div>
