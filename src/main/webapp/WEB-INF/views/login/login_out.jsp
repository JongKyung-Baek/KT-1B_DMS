<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="kr.esob.fdms.commonlogic.message.LocaleUtil"%>
<!doctype html>
<html lang="kr" class="layout-wide customizer-hide" dir="ltr" data-skin="default" data-bs-theme="light">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>Login - CollabHub - external</title>

  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/fonts/iconify-icons.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/node-waves/node-waves.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/pickr/pickr-themes.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/core.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/css/demo.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/libs/perfect-scrollbar/perfect-scrollbar.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/vuexy/assets/vendor/css/pages/page-auth.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css" />

  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/helpers.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/vendor/js/template-customizer.js"></script>
  <script src="${pageContext.request.contextPath}/vuexy/assets/js/config.js"></script>

  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/kessinfo.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/polyfill.js"></script>
  <style>
    .external-login-copy {
      text-align: left;
    }

    .external-login-title {
      margin-bottom: 0.25rem;
      color: var(--bs-heading-color);
      font-size: 1.5rem;
      font-weight: 500;
      line-height: 1.375;
      text-align: left;
    }

    .external-login-subtitle {
      margin-bottom: 1.5rem;
      color: var(--bs-body-color);
      font-size: 0.9375rem;
      line-height: 1.5;
      text-align: left;
    }
  </style>
  <script>
    loadBundles('<%=LocaleUtil.getCurrentLanguage(request) %>', '${pageContext.request.contextPath}');
    $(document).ready(function(){
      var errorMsg = "${errorMsg}";
      if(errorMsg !== undefined && errorMsg !== ''){
        alertMessage(errorMsg, function(){
          errorMsg = '';
          location.href='${pageContext.request.contextPath}/?url_type=E';
          $(this).dialog("close");
        });
      }
    });
  </script>
</head>
<body class="login-page">
  <div class="container-xxl">
    <div class="authentication-wrapper authentication-basic container-p-y">
      <div class="authentication-inner py-6">
        <div class="card">
          <div class="card-body">

            <div class="app-brand justify-content-center mb-6">
              <a href="#" class="app-brand-link">
                <img src="${pageContext.request.contextPath}/resources/images/KARI/KARI_SDMS_LOGO.png" alt="KARI" class="login-logo">
              </a>
            </div>

            <div class="external-login-copy">
              <h4 class="external-login-title">기술자료관리시스템</h4>
              <p class="external-login-subtitle">계정 정보를 입력해 주세요.</p>
            </div>

            <form id="loginForm" name="loginForm" action="${pageContext.request.contextPath}/login/loginProcess" method="POST">
              <input type="hidden" name="url_type" id="url_type" value="E">

              <div class="mb-6">
                <label for="bizNo" class="form-label">사업자번호</label>
                <input type="text" class="form-control" 
                       id="bizNo" name="bizNo" placeholder="사업자번호를 입력하세요" autofocus />
              </div>

              <div class="mb-6">
                <label for="userId" class="form-label">이름</label>
                <input type="text" class="form-control" 
                       id="userId" name="userId" placeholder="이름을 입력하세요" />
              </div>

              <div class="mb-6 form-password-toggle">
                <label class="form-label" for="userPw">비밀번호</label>
                <div class="input-group input-group-merge">
                  <input type="password" class="form-control" 
                         id="userPw" name="userPw" placeholder="비밀번호를 입력하세요" />
                  <span class="input-group-text cursor-pointer">
                    <i class="icon-base ti tabler-eye-off"></i>
                  </span>
                </div>
              </div>

              <div class="mb-6">
                <button class="btn btn-primary d-grid w-100" type="submit">LOGIN</button>
              </div>
            </form>

          </div>
        </div>
      </div>
    </div>
  </div>

  <div id="alertMessage"></div>

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

  <script>
  (function() {
    var errorMsg = "${errorMsg}";
    if (errorMsg && errorMsg.trim() !== "") {
      alert(errorMsg);
      location.href='${pageContext.request.contextPath}/?url_type=E';
    }
  })();
  </script>

  <script>
  /* 23.03.17 kess 검증 루틴 막음
    var remoteServer = 'http://localhost:8087';

    GetIsKess(remoteServer, false)
    .then(function(ret){
      console.log("result: install");
      alert("install!");
    })
    .catch(function(error){
      console.log("result: not install");
      var retVal = confirm("뷰어 프로그램이 설치되지 않았습니다.\n\r 설치 페이지로 이동할까요?");
      if(retVal == true){
        window.location.href = '/login/kess';
      }
    });
  */
  </script>
</body>
</html>
