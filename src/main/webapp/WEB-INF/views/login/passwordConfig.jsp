<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>비밀번호 재설정 | 다목적실용위성8호사업단</title>
  <style>
    * {
      box-sizing: border-box;
    }

    html, body {
      margin: 0;
      width: 100%;
      min-height: 100%;
      font-family: "Malgun Gothic", "Apple SD Gothic Neo", Arial, sans-serif;
      color: #1f2937;
      background: #eef2f7;
    }

    body {
      min-height: 100vh;
      background:
        radial-gradient(circle at 12% 18%, rgba(41, 92, 153, 0.18), transparent 32%),
        radial-gradient(circle at 88% 82%, rgba(15, 47, 87, 0.20), transparent 34%),
        linear-gradient(135deg, #f5f7fb 0%, #e8eef6 44%, #dbe5f1 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 48px;
    }

    .wrap {
      width: 960px;
      min-height: 560px;
      background: #ffffff;
      border-radius: 24px;
      box-shadow: 0 26px 70px rgba(15, 35, 70, 0.22);
      overflow: hidden;
      display: grid;
      grid-template-columns: 45% 55%;
    }

    .visual {
      position: relative;
      padding: 48px 44px;
      background:
        linear-gradient(150deg, rgba(12, 38, 72, 0.96), rgba(18, 70, 125, 0.92)),
        url("");
      color: #ffffff;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }

    .visual::before {
      content: "";
      position: absolute;
      width: 360px;
      height: 360px;
      right: -170px;
      top: -120px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.08);
    }

    .visual::after {
      content: "";
      position: absolute;
      width: 260px;
      height: 260px;
      left: -110px;
      bottom: -80px;
      border-radius: 50%;
      border: 1px solid rgba(255, 255, 255, 0.14);
    }

    .brand {
      position: relative;
      z-index: 1;
    }

    .brand-ko {
      font-size: 22px;
      font-weight: 800;
      letter-spacing: -0.7px;
      line-height: 1.35;
    }

    .brand-en {
      margin-top: 8px;
      font-size: 14px;
      color: rgba(255, 255, 255, 0.75);
      letter-spacing: 0.3px;
    }

    .visual-title {
      position: relative;
      z-index: 1;
    }

    .visual-title .eyebrow {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.72);
      letter-spacing: 2px;
      text-transform: uppercase;
      margin-bottom: 14px;
    }

    .visual-title h2 {
      margin: 0;
      font-size: 34px;
      line-height: 1.28;
      letter-spacing: -1.3px;
      font-weight: 800;
    }

    .visual-title p {
      margin: 18px 0 0;
      font-size: 14px;
      line-height: 1.75;
      color: rgba(255, 255, 255, 0.74);
    }

    .visual-footer {
      position: relative;
      z-index: 1;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.55);
      line-height: 1.6;
    }

    .content {
      padding: 62px 66px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      background: #ffffff;
    }

    .page-title {
      margin-bottom: 30px;
    }

    .page-title h1 {
      margin: 0;
      font-size: 30px;
      font-weight: 800;
      letter-spacing: -1.1px;
      color: #101828;
    }

    .page-title p {
      margin: 12px 0 0;
      color: #667085;
      font-size: 14px;
      line-height: 1.7;
    }

    .account-panel {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 16px 18px;
      margin-bottom: 26px;
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 14px;
    }

    .account-label {
      font-size: 13px;
      font-weight: 700;
      color: #475467;
    }

    .account-value {
      font-size: 15px;
      font-weight: 800;
      color: #0f2f57;
      word-break: break-all;
      text-align: right;
    }

    .form-row {
      margin-bottom: 18px;
    }

    label {
      display: block;
      margin-bottom: 9px;
      font-size: 13px;
      font-weight: 800;
      color: #344054;
    }

    input[type="password"] {
      width: 100%;
      height: 50px;
      padding: 0 15px;
      border: 1px solid #d0d5dd;
      border-radius: 12px;
      font-size: 15px;
      color: #101828;
      outline: none;
      background: #ffffff;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    input[type="password"]:focus {
      border-color: #175c9e;
      box-shadow: 0 0 0 4px rgba(23, 92, 158, 0.12);
    }

    .guide {
      margin-top: 8px;
      font-size: 12px;
      line-height: 1.5;
      color: #8a95a5;
    }

    .submit-btn {
      width: 100%;
      height: 52px;
      margin-top: 10px;
      border: 0;
      border-radius: 13px;
      background: #0f2f57;
      color: #ffffff;
      font-size: 15px;
      font-weight: 800;
      cursor: pointer;
      box-shadow: 0 12px 24px rgba(15, 47, 87, 0.18);
      transition: transform 0.12s, background 0.2s, box-shadow 0.2s;
    }

    .submit-btn:hover {
      background: #174674;
      box-shadow: 0 14px 26px rgba(15, 47, 87, 0.24);
    }

    .submit-btn:active {
      transform: translateY(1px);
    }

    .notice {
      margin-top: 22px;
      padding-top: 18px;
      border-top: 1px solid #eef2f6;
      font-size: 12px;
      line-height: 1.65;
      color: #98a2b3;
    }

    .modal {
      display: none;
      position: fixed;
      z-index: 1000;
      inset: 0;
      background: rgba(15, 23, 42, 0.45);
      align-items: center;
      justify-content: center;
      padding: 20px;
    }

    .modal-content {
      width: min(360px, 100%);
      padding: 28px;
      border-radius: 16px;
      background: #ffffff;
      box-shadow: 0 22px 60px rgba(15, 35, 70, 0.20);
      text-align: center;
    }

    .modal-content p {
      margin: 0 0 22px;
      font-size: 15px;
      line-height: 1.6;
      color: #1f2937;
      white-space: pre-line;
    }

    .modal-content button {
      min-width: 92px;
      height: 40px;
      margin: 0 4px;
      border: 0;
      border-radius: 10px;
      background: #0f2f57;
      color: #ffffff;
      font-size: 13px;
      font-weight: 800;
      cursor: pointer;
    }

    .modal-content button.cancel {
      background: #eef2f6;
      color: #344054;
    }

    @media (max-width: 860px) {
      body {
        padding: 24px;
        align-items: flex-start;
      }

      .wrap {
        width: 100%;
        min-height: auto;
        grid-template-columns: 1fr;
      }

      .visual {
        min-height: 250px;
      }

      .content {
        padding: 40px 34px;
      }
    }

    @media (max-width: 480px) {
      body {
        padding: 14px;
      }

      .visual {
        padding: 34px 28px;
      }

      .visual-title h2 {
        font-size: 28px;
      }

      .content {
        padding: 34px 24px;
      }

      .account-panel {
        align-items: flex-start;
        flex-direction: column;
      }

      .account-value {
        text-align: left;
      }
    }
  </style>
</head>
<body>
  <main class="wrap">
    <section class="visual">
      <div class="brand">
        <div class="brand-ko">KAI</div>
        <div class="brand-en">KT-1B</div>
      </div>

      <div class="visual-title">
        <div class="eyebrow">Secure Account</div>
        <h2>비밀번호<br />재설정</h2>
        <p>
          등록된 사용자 계정에 대한 새로운 비밀번호를 설정합니다.
          변경 완료 후 새 비밀번호로 다시 로그인해 주세요.
        </p>
      </div>

      <div class="visual-footer">
        Korea Aerospace Industries<br />
        Technical Data Management System
      </div>
    </section>

    <section class="content">
      <div class="page-title">
        <h1>새 비밀번호 설정</h1>
        <p>아래 계정의 비밀번호를 변경합니다.</p>
      </div>

      <div class="account-panel">
        <div class="account-label">사용자 아이디</div>
        <div class="account-value">
          <c:choose>
            <c:when test="${userVo.authSite == 'E'}">${userVo.userNm}</c:when>
            <c:otherwise>${userVo.userId}</c:otherwise>
          </c:choose>
        </div>
      </div>

      <form id="passwordForm">
        <input type="hidden" name="userId" value="${userVo.userId}" />
        <input type="hidden" name="userCd" value="${userVo.userCd}" />
        <input type="hidden" name="userNm" value="${userVo.userNm}" />
        <input type="hidden" name="deptCd" value="${userVo.deptCd}" />
        <input type="hidden" name="positionCd" value="${userVo.positionCd}" />
        <input type="hidden" name="roleGroupCd" value="${userVo.roleGroup}" />
        <input type="hidden" name="email" value="${userVo.email}" />
        <input type="hidden" name="saveFlag" value="E" />
        <input type="hidden" name="authSite" value="${userVo.authSite}" />

        <div class="form-row">
          <label for="newPassword">새 비밀번호</label>
          <input
            type="password"
            id="newPassword"
            name="userPwd"
            maxlength="20"
            placeholder="새 비밀번호 입력"
            autocomplete="new-password"
            required
          />
          <div class="guide">영문, 숫자, 특수문자를 조합하여 입력해 주세요.</div>
        </div>

        <div class="form-row">
          <label for="confirmPassword">새 비밀번호 확인</label>
          <input
            type="password"
            id="confirmPassword"
            maxlength="20"
            placeholder="새 비밀번호 재입력"
            autocomplete="new-password"
            required
          />
        </div>

        <button type="button" id="saveButton" class="submit-btn">비밀번호 변경</button>
      </form>

      <div class="notice">
        본 페이지는 비밀번호 재설정을 위한 전용 화면입니다.
        요청하지 않은 접근인 경우 관리자에게 문의해 주세요.
      </div>
    </section>
  </main>

  <div id="customModal" class="modal">
    <div class="modal-content">
      <p id="modalMessage"></p>
      <button type="button" id="closeModalButton">확인</button>
    </div>
  </div>

  <div id="confirmModal" class="modal">
    <div class="modal-content">
      <p id="confirmMessage"></p>
      <button type="button" id="confirmYesButton">예</button>
      <button type="button" id="confirmNoButton" class="cancel">아니오</button>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
  <script>
    var contextPath = "${pageContext.request.contextPath}";
    var basicPassword = "${basicPassword}";

    function showModal(message, callback) {
      $("#modalMessage").text(message);
      $("#customModal").css("display", "flex");
      $("#closeModalButton").off("click").on("click", function() {
        $("#customModal").hide();
        if (typeof callback === "function") {
          callback();
        }
      });
    }

    function showConfirm(message, onConfirm) {
      $("#confirmMessage").text(message);
      $("#confirmModal").css("display", "flex");
      $("#confirmYesButton").off("click").on("click", function() {
        $("#confirmModal").hide();
        onConfirm();
      });
      $("#confirmNoButton").off("click").on("click", function() {
        $("#confirmModal").hide();
      });
    }

    function submitPasswordForm() {
      var form = $("#passwordForm")[0];
      var formData = new FormData(form);
      var authSite = $("input[name='authSite']").val();
      var url = contextPath + (authSite === "E"
        ? "/inside/organizationmanage/outsideuser/saveRegisterUser"
        : "/inside/organizationmanage/insideuser/saveRegisterUser");

      $.ajax({
        url: url,
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
          if (response && response.success) {
            showModal("저장되었습니다. 다시 로그인해 주세요.", function() {
              window.location.href = contextPath + "/login/loginPage?url_type=" + encodeURIComponent(authSite);
            });
          } else {
            showModal("비밀번호 저장에 실패했습니다. 다시 시도해 주세요.");
          }
        },
        error: function() {
          showModal("서버 오류가 발생했습니다.");
        }
      });
    }

    $("#saveButton").on("click", function() {
      var newPassword = $("#newPassword").val();
      var confirmPassword = $("#confirmPassword").val();
      var passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

      if (newPassword !== confirmPassword) {
        showModal("비밀번호가 일치하지 않습니다.");
        return;
      }

      if (newPassword === basicPassword) {
        showModal("초기 비밀번호는 사용할 수 없습니다.");
        return;
      }

      if (/\s/.test(newPassword)) {
        showModal("비밀번호에는 공백을 포함할 수 없습니다.");
        return;
      }

      if (!passwordPattern.test(newPassword)) {
        showConfirm("비밀번호가 권장 규칙을 충족하지 않습니다.\n그래도 저장하시겠습니까?", submitPasswordForm);
        return;
      }

      showConfirm("변경 사항을 저장하시겠습니까?", submitPasswordForm);
    });
  </script>
</body>
</html>
