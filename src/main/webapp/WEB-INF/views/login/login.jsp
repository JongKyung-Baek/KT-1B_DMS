<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="kr.esob.fdms.commonlogic.message.LocaleUtil"%>
<!doctype html>
<html lang="kr" class="layout-wide customizer-hide" dir="ltr" data-skin="default" data-bs-theme="light">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>SDMS - Login </title>

<!-- vuexy CSS -->
  <!-- <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/vuexy/assets/img/favicon/favicon.ico" /> -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/fonts/iconify-icons.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr-themes.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/core.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/css/demo.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/pages/page-auth.css" />  

  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/helpers.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/template-customizer.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/js/config.js"></script>  

  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/kessinfo.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/polyfill.js"></script>  
  <script> loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}'); </script>  
<!-- vuexy CSS -->

<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
<script type="text/javascript" src="/resources/js/kessinfo.js"></script>
<script type="text/javascript" src="/resources/js/polyfill.js"></script>


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

    .kai-logo-lockup {
      width: 300px;
      display: flex;
      align-items: flex-end;
      justify-content: flex-end;
      gap: 14px;
      filter: drop-shadow(0 6px 14px rgba(0,0,0,0.18));
    }

    .kai-symbol-img {
      width: 136px;
      height: auto;
      display: block;
      flex: 0 0 auto;
    }

    .kai-english-name {
      text-align: left;
      color: #ffffff;
      font-family: "Pretendard", "Noto Sans KR", "Malgun Gothic", sans-serif;
      font-size: 19px;
      line-height: 1.08;
      font-weight: 800;
      letter-spacing: 0.09em;
      white-space: nowrap;
      text-transform: uppercase;
      transform: translateY(1px);
    }

    .kai-logo {
      width: 300px;
      max-width: 100%;
      height: auto;
      display: block;
      align-self: flex-end;
      filter: drop-shadow(0 6px 14px rgba(0,0,0,0.18));
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

      .kai-logo {
        width: 260px;
      }

      .kai-logo-lockup {
        width: 260px;
        gap: 12px;
        align-items: flex-end;
      }

      .kai-symbol-img {
        width: 118px;
      }

      .kai-english-name {
        font-size: 16px;
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
</style>
<script>
loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');
	$(document).ready(function(){
		console.log('${errorMsg}');
		var errorMsg = "${errorMsg}";
		if(errorMsg != undefined && errorMsg != ''){
			alertMessage(errorMsg, function(){
				errorMsg = '';
				location.href='/?url_type=I';
				$(this).dialog("close");
			});
		}
	});

	//const socket = new WebSocket("ws://localhost:39229/websocket");
	// 웹소켓 연결이 열린 경우
	//socket.onopen = function(event) {
	// alert("WebSocket 연결이 열렸습니다.ㅁㄴㅇ");
	//var ret = window.open('/inside/distribution/docPdfLinkRequest/selectItem2?objectType='+objectType+'&file='+objectId+'&requestNo='+requestNo+'&fileNo='+fileNo, '_blank', '', false);
	// 메시지 전송 예시
	//};
	//socket.onerror = function (event) {
	//	alert("설치 페이지로 이동 됩니다.");
		// window.location.href = "/CollabviewInstallPage.jsp"
	//	window.location.href = "/common/viewer/collabInstallPage"
	//}

	// 웹소켓으로부터 메시지를 수신한 경우
	//socket.onmessage = function(event) {
	//	const receivedMessage = event.data;
	//	alert("서버로부터 메시지 수신:", receivedMessage);
	//};

	// 웹소켓 연결이 닫힌 경우
	//socket.onclose = function(event) {};

    /*
    function popup(){
        var url = "/FDMS/p1_mydoc.html";
        var name = "popup test";
        var option = "width = 500, height = 500, top = 100, left = 200, location = no"
        window.open(url, name, option);
    }
	*/

</script>
</head>
<body>

  <main class="login-page">
    <section class="login-wrap" aria-label="KAI S-DMS Login Page">
      <div class="brand-area">
        <div class="brand-box">
          <img class="kai-logo" src="data:image/png;base64,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" alt="KAI 로고" />

          <svg class="sdms-logo" viewBox="28 0 360 132" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="KT-1B DMS 로고">
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
              <input type="hidden" id="url_type" name="url_type" value="I" />
          <div class="input-group">
            <span class="icon-box">
              <svg class="field-icon" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path fill="#FFFFFF" d="M16 16.2c4.15 0 7.5-3.35 7.5-7.5S20.15 1.2 16 1.2 8.5 4.55 8.5 8.7s3.35 7.5 7.5 7.5Z"/>
                <path fill="#FFFFFF" d="M3.8 30.2c.55-7.2 5.4-11.4 12.2-11.4s11.65 4.2 12.2 11.4c.05.55-.4 1-1 1H4.8c-.6 0-1.05-.45-1-1Z"/>
              </svg>
            </span>
            <span class="field-line"></span>
            <input type="text" id="userId" name="userId" placeholder="Username" autocomplete="username" autofocus />
          </div>

          <div class="input-group">
            <span class="icon-box">
              <svg class="field-icon" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <path fill="#FFFFFF" d="M23.3 12.6h-1.35V8.9C21.95 5.05 19.25 2.1 16 2.1S10.05 5.05 10.05 8.9v3.7H8.7c-2.05 0-3.7 1.65-3.7 3.7v9.85c0 2.05 1.65 3.7 3.7 3.7h14.6c2.05 0 3.7-1.65 3.7-3.7V16.3c0-2.05-1.65-3.7-3.7-3.7Zm-9.75-3.7c0-1.95 1.1-3.35 2.45-3.35s2.45 1.4 2.45 3.35v3.7h-4.9V8.9Zm3.95 12.25v3.2c0 .85-.65 1.5-1.5 1.5s-1.5-.65-1.5-1.5v-3.2c-.8-.5-1.3-1.35-1.3-2.35 0-1.55 1.25-2.8 2.8-2.8s2.8 1.25 2.8 2.8c0 1-.5 1.85-1.3 2.35Z"/>
              </svg>
            </span>
            <span class="field-line"></span>
            <input type="password" id="userPw" name="userPw" placeholder="Password" autocomplete="current-password" />
          </div>

          <div class="option-row">
            <label class="remember">
              <input type="checkbox" id="rememberId" name="rememberId" checked />
              <span>Remember me</span>
            </label>
          </div>

              <button class="login-button" type="submit">LOGIN</button>
            </form>
          </div>
        </div>

        <div class="compatibility-area">
          <div class="compatibility" aria-label="권장 이용 환경">
          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/KAI/windows.png" alt="Windows" />
            <span>Windows<br/>환경</span>
          </div>

          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/KAI/chrome.png" alt="Chrome" />
            <span>Google<br/>Chrome</span>
          </div>

          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/KAI/FireFox.png" alt="Firefox" />
            <span>Mozilla<br/>Firefox</span>
          </div>

          <div class="compat-item">
            <img class="compat-icon" src="${pageContext.request.contextPath}/resources/images/KAI/edge.png" alt="Edge" />
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
    var errorMsg = "${errorMsg}";
    if (errorMsg && errorMsg.trim() !== "") {
      alert(errorMsg);
      location.href='${pageContext.request.contextPath}/?url_type=I';
    }
  })();
  </script>

  <!-- 기존 유지 -->
  <script>
    var remoteServer = 'http://localhost:8088';
    GetIsKess(remoteServer, false)
      .then(function(ret){ console.log("result: install"); })
      .catch(function(error){ console.log("result: not install"); });
  </script>




<!--이전버전 백업용 -->
<%--
	<script>

		//var remoteServer = 'http://192.168.1.180:8443';
		//var remoteServer = 'https://nfthuman.duckdns.org';

		//var remoteServer = 'http://127.0.0.1:8443';
		//var remoteServer = 'http://js-lab.iptime.org';
		var remoteServer = 'http://localhost:8088';
		
		GetIsKess(remoteServer, false)  //연동대상 그룹웨어 url 주소 , 외부망 서버인지 여부 
		.then(function(ret){
			console.log("result: install");
			alert("install!");
		})
		.catch(function(error){
			console.log("result: not install");
			// 2023.2.13 개발서버 테스트용으로 임시로 막음 대체용이지만 막음2
			//var retVal = confirm("뷰어 프로그램이 설치되지 않았습니다.\n\r 설치 페이지로 이동할까요?");
			//if( retVal == true ){
			//	window.location.href = '/login/kess';
			//}
			// 2023.2.13 개발서버 테스트용으로 임시로 막음 대체용이지만 막음2
			
		// 2023.1.11 개발서버 테스트용으로 임시로 막음
		//console.log("result: not install");
		//alert("뷰어 프로그램이 설치되지 않았습니다.\n\r 설치 페이지로 이동합니다.");
		//window.location.href = '/login/kess';
		// 2023.1.11 개발서버 테스트용으로 임시로 막음
		});
		
		/*
		IsKESSInstall()
		.then(function(ret){
			console.log("result: install");
			alert("install!");
		})
		.catch(function(error){
			console.log("result: not install");
			alert("뷰어 프로그램이 설치되지 않았습니다.\n\r 설치 페이지로 이동합니다.");
			//window.location.href = '/login/kess';
		});
		*/

	</script>
	<div class="loginWrap inside">
		<div class="header">
			<div class="left">
				<h1 class="logo">
					<a href="#">logo</a>
					<span>CollabHub</span>
				</h1>
			</div>
			<div class="right">
			</div>
		</div>
		<div class="loginArea">
			<div class="loginBox">
				<h1>CollabHub</h1>
				<p><span>내부사용자용</span> 로그인 페이지</p>
				<form id="loginForm" name="loginForm" action="/login/loginProcess" method="POST">
						<input type="hidden" id="url_type" name="url_type" value="I" />
	<!-- 				<select id="url_type" name="url_type"><option value="I">내부사용자</option><option value="E">외부사용자</option></select> -->
					<ul>
						<li><label for="userId">ID</label>
							<input type="text" id="userId" name="userId" value="" placeholder="ID을 입력하세요" autofocus>
							<label for="userId" class="icon">I</label>
						</li>
						<li>
							<label for="userPw">Password</label>
							<input type="password" id="userPw"  name="userPw" placeholder="Password를 입력하세요">
							<label for="userPw" class="icon">P</label>
						</li>
					</ul>
					<button type="submit" class="loginSubmitBtn">LOGIN</button>
					<!--  <button type="button" id="btnHi"  class="loginSubmitBtn" onclick="popup();">Hi~</button> -->
				</form>
			</div>
			<!-- Secure Site Seal - DO NOT EDIT -->
			<div id="tls_seal_e6f644384d8e43a7971e74d3591458b8" data-id="e6f644384d8e43a7971e74d3591458b8" data-logosize="medium" data-logotype="static"></div>
			<script src="https://seal.turingsign.com/v1.1/e6f644384d8e43a7971e74d3591458b8/script.js" async defer></script>
			<!--- Secure Site Seal - DO NOT EDIT --->
		</div>
	</div>
	<!--
	<div class="loginWrap outside">
		<div class="header">
			<div class="left">
				<h1 class="logo">
					<a href="#">logo</a>
					<span>CollabHub</span>
				</h1>
			</div>
			<div class="right">
			</div>
		</div>
		<div class="loginArea">
			<div class="loginBox">
				<h1>CollabHub</h1>
				<p><span>외부업체용</span> 로그인 페이지</p>
				<form id="loginForm" name="loginForm" action="/login/loginProcess" method="POST">
					<ul>
						<li><label for="">사업자번호</label><input type="text" id="" name="" placeholder="사업자번호를 입력하세요" autofocus></li>
						<li><label for="userId">ID</label><input type="text" id="userId" name="userId" placeholder="ID을 입력하세요"><label for="userId" class="icon">I</label></li>
						<li><label for="userPw">Password</label><input type="password" id="userPw"  name="userPw" placeholder="Password를 입력하세요"><label for="userPw" class="icon">P</label></li>
					</ul>
					<button type="submit" class="loginSubmitBtn">LOGIN</button>
				</form>
				<p class="linkBtnArea">
					<a href="#" class="pwInquiry">비밀번호 찾기</a>
				</p>
			</div>
		</div>
	</div>
	-->
	<!-- <a href="#" class="selectLanguage en">English</a> -->
	<div id="alertMessage"></div>
--%>
<!--이전버전 백업용 -->


</body>


</html>
