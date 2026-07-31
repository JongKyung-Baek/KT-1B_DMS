<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="kr.esob.fdms.commonlogic.message.LocaleUtil"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!doctype html>
<html lang="<%=LocaleUtil.getCurrentLanguage(request) %>" class="layout-wide customizer-hide" dir="ltr"
      data-skin="default"
      data-bs-theme="light"
      data-template="vertical-menu-template"
      data-assets-path="${pageContext.request.contextPath}/vuexy/assets/">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>SDMS - Login </title>
<%@ include file="/WEB-INF/jspf/csrf-meta.jspf" %>
<%@ include file="/WEB-INF/jspf/favicon.jspf" %>

<!-- vuexy CSS -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/fonts/iconify-icons.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr-themes.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/core.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/css/demo.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/pages/page-auth.css" />  

  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/helpers.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/js/config.js"></script>  

<!-- vuexy CSS -->

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/polyfill.js"></script>
<script>loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');</script>


<!-- <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css" media="screen" /> -->
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css" media="screen" />
<style>

    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      min-height: 100vh;
      font-family: "Pretendard", "Noto Sans KR", "Malgun Gothic", sans-serif;
      color: #ffffff;
      overflow: hidden;
      background: #123f65;
    }

    .login-page {
      position: relative;
      width: 100vw;
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background:
        linear-gradient(120deg, rgba(255,255,255,0.08) 0 18%, transparent 18% 100%),
        linear-gradient(330deg, transparent 0 64%, rgba(255,255,255,0.08) 64% 78%, transparent 78% 100%),
        linear-gradient(145deg, rgba(15,62,101,0.98), rgba(43,128,184,0.96));
    }

    .login-page::before,
    .login-page::after {
      content: "";
      position: absolute;
      inset: 0;
      pointer-events: none;
    }

    .login-page::before {
      background:
        linear-gradient(22deg, transparent 0 58%, rgba(255,255,255,0.10) 58% 66%, transparent 66% 100%),
        linear-gradient(153deg, transparent 0 70%, rgba(255,255,255,0.08) 70% 81%, transparent 81% 100%),
        radial-gradient(circle at 20% 20%, rgba(255,255,255,0.10), transparent 28%);
      opacity: 0.55;
    }

    .login-page::after {
      background-image:
        linear-gradient(rgba(255,255,255,0.045) 1px, transparent 1px),
        linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px);
      background-size: 42px 42px;
      opacity: 0.22;
    }

    .login-wrap {
      position: relative;
      z-index: 1;
      width: min(980px, 88vw);
      min-height: 420px;
      display: grid;
      grid-template-columns: 1fr 1fr;
      align-items: center;
      gap: 70px;
      margin-top: 99px;
    }

    .brand-area {
      position: relative;
      display: flex;
      align-items: flex-start;
      justify-content: center;
      min-height: 100%;
      padding-right: 68px;
    }

    .right-column {
      min-height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
    }

    .brand-area::after {
      content: "";
      position: absolute;
      right: 0;
      top: 46%;
      transform: translateY(-50%);
      width: 1px;
      height: calc(100% + 42px);
      background: rgba(255,255,255,0.32);
    }

    .brand-box {
      width: 300px;
      min-height: 300px;
      text-align: center;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      justify-content: center;
      transform: translateY(26px);
    }

    .auth-area {
      min-height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      padding-left: 10px;
      width: 100%;
    }

    .sdms-logo {
      width: 300px;
      height: auto;
      margin-top: 28px;
      display: block;
      align-self: flex-end;
      margin-right: 0;
      filter: drop-shadow(0 5px 12px rgba(0,0,0,0.16));
    }

    .login-area {
      display: flex;
      justify-content: center;
      align-items: flex-start;
      min-height: 250px;
      width: 100%;
    }

    .login-panel {
      width: 390px;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
      padding-top: 45px;
    }

    .compatibility-area {
      width: 100%;
      display: flex;
      justify-content: center;
      margin-top: 8px;
    }

    .input-group {
      position: relative;
      margin-bottom: 18px;
    }

    .input-group input {
      width: 100%;
      height: 48px;
      border: 0;
      border-radius: 4px;
      outline: none;
      padding: 0 18px 0 72px;
      font-size: 15px;
      color: #ffffff;
      background: rgba(255,255,255,0.34);
      box-shadow: inset 0 0 0 1px rgba(255,255,255,0.48), 0 5px 14px rgba(0,0,0,0.08);
    }

    .input-group input::placeholder {
      color: rgba(255,255,255,0.76);
    }

    .icon-box {
      position: absolute;
      left: 15px;
      top: 0;
      width: 40px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      pointer-events: none;
      font-size: 17px;
    }

    .field-icon-img,
    .field-icon {
      display: block;
      width: 20px;
      height: 20px;
      object-fit: contain;
      filter: brightness(0) invert(1);
      opacity: 0.95;
    }

    .field-line {
      position: absolute;
      left: 62px;
      top: 10px;
      width: 1.5px;
      height: 28px;
      background: rgba(255,255,255,0.92);
    }

    .remember-me,
    .option-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: -4px 0 14px;
      font-size: 13px;
      line-height: 1.2;
      color: rgba(255,255,255,0.92);
      user-select: none;
    }

    .remember-me label,
    .remember {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      white-space: nowrap;
      cursor: pointer;
    }

    .remember-me input[type="checkbox"],
    .remember input {
      width: 15px;
      height: 15px;
      margin: 0;
      accent-color: #2b80b8;
      cursor: pointer;
    }

    .login-button {
      width: 100%;
      height: 58px;
      border: 0;
      border-radius: 6px;
      color: #ffffff;
      font-size: 20px;
      font-weight: 400;
      letter-spacing: 0.08em;
      background: rgba(255,255,255,0.18);
      box-shadow: inset 0 0 0 1px rgba(255,255,255,0.12);
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .login-button:hover { background: rgba(255,255,255,0.27); transform: translateY(-1px); }

    .secondary-button {
      margin-top: 12px;
      width: 100%;
      height: 48px;
      border: 1px solid rgba(255,255,255,0.5);
      border-radius: 6px;
      color: #fff;
      background: transparent;
      cursor: pointer;
    }

    .otp-user-info {
      margin-bottom: 10px;
      color: rgba(255,255,255,0.88);
      font-size: 13px;
    }

    #alertMessage { display: none; }

    .compatibility {
      margin-top: 22px;
      display: flex;
      align-items: flex-start;
      justify-content: center;
      gap: 16px;
    }

    .compat-item {
      width: 72px;
      min-height: 86px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      color: rgba(255,255,255,0.92);
      font-size: 11px;
      font-weight: 600;
      line-height: 1.2;
      text-align: center;
      text-shadow: 0 2px 5px rgba(0,0,0,0.22);
    }

    .compat-icon {
      width: 42px;
      height: 42px;
      display: block;
      object-fit: contain;
      filter: drop-shadow(0 4px 8px rgba(0,0,0,0.18));
    }

    .compat-icon[alt="Edge"] {
      filter:
        drop-shadow(0 0 1.6px rgba(255,255,255,0.95))
        drop-shadow(0 0 1.2px rgba(255,255,255,0.9))
        drop-shadow(0 4px 8px rgba(0,0,0,0.18));
    }

    .compat-icon.edge-icon {
      width: 43px;
      height: 43px;
    }

    @media (max-width: 820px) {
      body {
        overflow: auto;
      }

      .login-page {
        min-height: 100vh;
        height: auto;
        padding: 42px 0;
      }

      .login-wrap {
        grid-template-columns: 1fr;
        gap: 34px;
        width: min(430px, 90vw);
        margin-top: 0px;
      }

      .brand-area {
        grid-column: auto;
        grid-row: auto;
        padding-right: 0;
        border-bottom: 1px solid rgba(255,255,255,0.25);
        min-height: auto;
        padding: 0 0 34px;
      }

      .brand-box{
        min-height: 0;
        transform: translateY(0);
      }

      .brand-area::after {
        display: none;
      }

      .auth-area {
        min-height: auto;
        padding-left: 0;
      }


      .login-area {
        min-height: auto;
      }

      .login-panel {
        width: 100%;
        padding-top: 0;
      }

      .sdms-logo {
        width: 260px;
        margin-right: 0;
      }

      .compat-item {
        width: 66px;
        min-height: 82px;
      }

      .compat-icon {
        width: 38px;
        height: 38px;
      }
    }
  
    .login-page::before,
    .login-page::after {
      z-index: 0 !important;
      pointer-events: none !important;
    }

    .login-wrap {
      z-index: 2 !important;
      pointer-events: auto !important;
    }

    .login-panel,
    .login-panel input,
    .login-panel button,
    .remember,
    .remember input {
      position: relative;
      z-index: 3;
      pointer-events: auto;
    }

    .login-language {
      position: fixed;
      z-index: 5;
      top: 22px;
      right: 28px;
      display: flex;
      gap: 4px;
      padding: 4px;
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 999px;
      background: rgba(7, 42, 72, 0.28);
      backdrop-filter: blur(8px);
    }

    .login-language button {
      border: 0;
      border-radius: 999px;
      padding: 7px 13px;
      color: rgba(255, 255, 255, 0.78);
      background: transparent;
      font: inherit;
      font-size: 13px;
      cursor: pointer;
    }

    .login-language button.active {
      color: #123f65;
      background: #fff;
      font-weight: 700;
    }
</style>
<script>
function changeLoginLanguage(language) {
  var target = new URL(window.location.href);
  target.searchParams.set('lang', language);
  window.location.assign(target.toString());
}


</script>
</head>
<body>
<div id="loginErrorMessage" hidden><c:out value="${errorMsg}"/></div>

<main class="login-page">
    <nav class="login-language" aria-label="<spring:message code='feature.language.selector'/>">
      <button type="button"
              class="<%= "ko".equals(LocaleUtil.getCurrentLanguage(request)) ? "active" : "" %>"
              lang="ko"
              onclick="changeLoginLanguage('ko')">한국어</button>
      <button type="button"
              class="<%= "en".equals(LocaleUtil.getCurrentLanguage(request)) ? "active" : "" %>"
              lang="en"
              onclick="changeLoginLanguage('en')">English</button>
    </nav>
    <section class="login-wrap" aria-label="<spring:message code='feature.login.pageLabel'/>">
      <div class="brand-area">
        <div class="brand-box">

          <svg class="sdms-logo" viewBox="28 0 360 132" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="<spring:message code='feature.login.logoLabel'/>">
            <defs>
              <linearGradient id="sdmsStroke" x1="20" y1="0" x2="410" y2="110" gradientUnits="userSpaceOnUse">
                <stop offset="0" stop-color="#FFFFFF"/>
                <stop offset="0.55" stop-color="#EAF8FF"/>
                <stop offset="1" stop-color="#BDEBFF"/>
              </linearGradient>
            </defs>
            <path d="M36 42 C55 13 99 9 132 21" fill="none" stroke="rgba(255,255,255,0.90)" stroke-width="15" stroke-linecap="round"/>
            <path d="M38 47 C75 28 121 33 154 47" fill="none" stroke="rgba(255,255,255,0.56)" stroke-width="5" stroke-linecap="round"/>
			<!-- <path d="M36 42 C86 8 220 8 326 25" fill="none" stroke="rgba(255,255,255,0.90)" stroke-width="15" stroke-linecap="round"/>
            <path d="M38 47 C118 25 250 31 344 50" fill="none" stroke="rgba(255,255,255,0.56)" stroke-width="5" stroke-linecap="round"/> -->
            <text x="42" y="84"
                  font-family="Segoe UI, Arial, sans-serif"
                  font-size="48"
                  font-style="italic"
                  font-weight="800"
                  letter-spacing="2"
                  fill="url(#sdmsStroke)">KT-1B DMS</text>
            <rect x="42" y="101" width="300" height="3.5" rx="1.75" fill="rgba(255,255,255,0.82)"/>
          </svg>
        </div>
      </div>

      <div class="right-column">
        <div class="auth-area">
          <div class="login-area">
            <form id="loginForm" name="loginForm" class="login-panel" action="${pageContext.request.contextPath}/login/loginProcess" method="POST">
              <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
              <input type="hidden" id="loginLanguage" name="lang" value="<%=LocaleUtil.getCurrentLanguage(request) %>" />
          <div class="input-group">
            <span class="icon-box">
              <svg class="field-icon" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path fill="#FFFFFF" d="M16 16.2c4.15 0 7.5-3.35 7.5-7.5S20.15 1.2 16 1.2 8.5 4.55 8.5 8.7s3.35 7.5 7.5 7.5Z"/>
                <path fill="#FFFFFF" d="M3.8 30.2c.55-7.2 5.4-11.4 12.2-11.4s11.65 4.2 12.2 11.4c.05.55-.4 1-1 1H4.8c-.6 0-1.05-.45-1-1Z"/>
              </svg>
            </span>
            <span class="field-line"></span>
            <input type="text" id="userId" name="userId" placeholder="<spring:message code='feature.login.username'/>" autocomplete="username" autofocus />
          </div>

          <div class="input-group">
            <span class="icon-box">
              <svg class="field-icon" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path fill="#FFFFFF" d="M23.3 12.6h-1.35V8.9C21.95 5.05 19.25 2.1 16 2.1S10.05 5.05 10.05 8.9v3.7H8.7c-2.05 0-3.7 1.65-3.7 3.7v9.85c0 2.05 1.65 3.7 3.7 3.7h14.6c2.05 0 3.7-1.65 3.7-3.7V16.3c0-2.05-1.65-3.7-3.7-3.7Zm-9.75-3.7c0-1.95 1.1-3.35 2.45-3.35s2.45 1.4 2.45 3.35v3.7h-4.9V8.9Zm3.95 12.25v3.2c0 .85-.65 1.5-1.5 1.5s-1.5-.65-1.5-1.5v-3.2c-.8-.5-1.3-1.35-1.3-2.35 0-1.55 1.25-2.8 2.8-2.8s2.8 1.25 2.8 2.8c0 1-.5 1.85-1.3 2.35Z"/>
              </svg>
            </span>
            <span class="field-line"></span>
            <input type="password" id="userPw" name="userPw" placeholder="<spring:message code='feature.login.password'/>" autocomplete="current-password" />
          </div>

          <div class="option-row">
            <label class="remember">
              <input type="checkbox" id="rememberId" name="rememberId" checked />
              <span><spring:message code="feature.login.remember"/></span>
            </label>
          </div>

              <button class="login-button" type="submit"><spring:message code="feature.login.submit"/></button>
            </form>
          </div>
        </div>

        <div class="compatibility-area">
          <div class="compatibility" aria-label="<spring:message code='feature.login.recommendedEnvironment'/>">
          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/platform/windows.png" alt="Windows" />
            <span>Windows<br/><spring:message code="feature.login.environment"/></span>
          </div>

          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/platform/chrome.png" alt="Chrome" />
            <span>Google<br/>Chrome</span>
          </div>

          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/platform/FireFox.png" alt="Firefox" />
            <span>Mozilla<br/>Firefox</span>
          </div>

          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/platform/edge.png" alt="Edge" />
            <span>Microsoft<br/>Edge</span>
          </div>
        </div>
      </div>
    </section>
  </main>

  <!-- Vuexy JS -->
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/popper/popper.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/bootstrap.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/hammer/hammer.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/i18n/i18n.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/menu.js"></script>

  <script src="${pageContext.request.contextPath}/vuexy/assets/js/main.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/js/pages-auth.js"></script>

  <!-- errorMsg 처리 -->
  <script>
  (function() {
    var errorElement = document.getElementById('loginErrorMessage');
    var errorMsg = errorElement ? errorElement.textContent : "";
    if (errorMsg && errorMsg.trim() !== "") {
      alert(errorMsg);
    }
  })();
  </script>

</body>


</html>
