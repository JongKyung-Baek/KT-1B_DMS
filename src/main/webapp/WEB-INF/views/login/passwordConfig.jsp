<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:message code="feature.locale.code" text="ko" var="pageLocale"/>
<!DOCTYPE html>
<html lang="${pageLocale}">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title><spring:message code="feature.password.browserTitle" text="비밀번호 재설정"/> | <c:choose><c:when test="${tdmsBrand.alternate}"><c:out value="${tdmsBrand.systemName}" /></c:when><c:otherwise>KT-1B</c:otherwise></c:choose></title>
  <%@ include file="/WEB-INF/jspf/csrf-meta.jspf" %>
  <%@ include file="/WEB-INF/jspf/favicon.jspf" %>
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

    .password-policy {
      margin-top: 12px;
      padding: 14px;
      border: 1px solid #e4eaf1;
      border-radius: 12px;
      background: #f8fafc;
    }

    .password-policy__title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-bottom: 10px;
      font-size: 12px;
      font-weight: 800;
      color: #344054;
    }

    .password-policy__count {
      color: #667085;
      font-weight: 700;
    }

    .password-policy__list {
      display: grid;
      gap: 7px;
      margin: 0;
      padding: 0;
      list-style: none;
    }

    .password-policy__item {
      display: flex;
      align-items: center;
      gap: 7px;
      font-size: 12px;
      line-height: 1.4;
      color: #667085;
    }

    .password-policy__item::before {
      width: 18px;
      height: 18px;
      border: 1px solid #d0d5dd;
      border-radius: 50%;
      color: transparent;
      content: "✓";
      flex: 0 0 auto;
      font-size: 11px;
      font-weight: 900;
      line-height: 16px;
      text-align: center;
    }

    .password-policy__item.is-met {
      color: #067647;
      font-weight: 700;
    }

    .password-policy__item.is-met::before {
      border-color: #12b76a;
      background: #ecfdf3;
      color: #067647;
    }

    .password-policy__or {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 26px;
      height: 18px;
      margin-right: 2px;
      border-radius: 999px;
      background: #eef4ff;
      color: #175cd3;
      font-size: 10px;
      font-weight: 900;
    }

    .password-match {
      min-height: 18px;
      margin-top: 7px;
      font-size: 12px;
      line-height: 1.5;
      color: #667085;
    }

    .password-match.is-match {
      color: #067647;
      font-weight: 700;
    }

    .password-match.is-mismatch {
      color: #b42318;
      font-weight: 700;
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

    .submit-btn:disabled {
      background: #98a2b3;
      box-shadow: none;
      cursor: wait;
      transform: none;
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
        <c:choose>
          <c:when test="${tdmsBrand.alternate}">
            <div class="brand-ko"><c:out value="${tdmsBrand.systemName}" /></div>
            <div class="brand-en"><c:out value="${tdmsBrand.companyName}" /></div>
          </c:when>
          <c:otherwise>
            <div class="brand-ko">KT-1B</div>
            <div class="brand-en">DMS</div>
          </c:otherwise>
        </c:choose>
      </div>

      <div class="visual-title">
        <div class="eyebrow"><spring:message code="feature.password.eyebrow" text="안전한 계정"/></div>
        <h2><spring:message code="feature.password.visualTitle" text="비밀번호 재설정"/></h2>
        <p>
          <spring:message code="feature.password.visualDescription"
                          text="등록된 사용자 계정에 대한 새로운 비밀번호를 설정합니다. 변경 완료 후 새 비밀번호로 다시 로그인해 주세요."/>
        </p>
      </div>

      <div class="visual-footer">
        <c:choose>
          <c:when test="${tdmsBrand.alternate}">
            <c:out value="${tdmsBrand.systemName}" /><br />
            <c:out value="${tdmsBrand.companyName}" />
          </c:when>
          <c:otherwise>
            KT-1B<br />
            Technical Data Management System
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <section class="content">
      <div class="page-title">
        <h1><spring:message code="feature.password.form.title" text="새 비밀번호 설정"/></h1>
        <p><spring:message code="feature.password.form.description" text="아래 계정의 비밀번호를 변경합니다."/></p>
      </div>

      <div class="account-panel">
        <div class="account-label"><spring:message code="feature.password.account.label" text="사용자 아이디"/></div>
      <div class="account-value">${userVo.userId}</div>
      </div>

      <form id="passwordForm" novalidate>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

        <div class="form-row">
          <label for="newPassword"><spring:message code="feature.password.new.label" text="새 비밀번호"/></label>
          <input
            type="password"
            id="newPassword"
            name="userPwd"
            maxlength="20"
            placeholder="<spring:message code="feature.password.new.placeholder" text="새 비밀번호 입력"/>"
            autocomplete="new-password"
            required
          />
          <div class="guide"><spring:message code="feature.password.policy.guide"
                                              text="영문과 숫자를 모두 포함하고 아래 두 조합 중 하나를 충족해야 합니다."/></div>
          <div class="password-policy" aria-live="polite">
            <div class="password-policy__title">
              <span><spring:message code="feature.password.policy.title" text="비밀번호 규칙"/></span>
              <span id="passwordCount" class="password-policy__count"><spring:message
                      code="feature.password.policy.count" text="영숫자 {0} · 특수 {1}" arguments="0,0"/></span>
            </div>
            <ul class="password-policy__list">
              <li id="policyCharacters" class="password-policy__item"><spring:message
                      code="feature.password.policy.characters" text="영문과 숫자를 각각 1개 이상 포함"/></li>
              <li id="policyLength" class="password-policy__item"><spring:message
                      code="feature.password.policy.length" text="공백 없이 전체 20자 이하"/></li>
              <li id="policyOptionA" class="password-policy__item"><spring:message
                      code="feature.password.policy.optionA" text="영숫자 8자 이상 + 특수문자 3자 이상"/></li>
              <li id="policyOptionB" class="password-policy__item">
                <span class="password-policy__or"><spring:message code="feature.password.policy.or" text="또는"/></span>
                <spring:message code="feature.password.policy.optionB"
                                text="영숫자 10자 이상 + 특수문자 2자 이상"/>
              </li>
            </ul>
          </div>
        </div>

        <div class="form-row">
          <label for="confirmPassword"><spring:message code="feature.password.confirm.label" text="새 비밀번호 확인"/></label>
          <input
            type="password"
            id="confirmPassword"
            maxlength="20"
            placeholder="<spring:message code="feature.password.confirm.placeholder" text="새 비밀번호 재입력"/>"
            autocomplete="new-password"
            required
          />
          <div id="passwordMatch" class="password-match" aria-live="polite"></div>
        </div>

        <button type="submit" id="saveButton" class="submit-btn"><spring:message
                code="feature.password.action.change" text="비밀번호 변경"/></button>
      </form>

      <div class="notice">
        <spring:message code="feature.password.notice"
                        text="본 페이지는 비밀번호 재설정을 위한 전용 화면입니다. 요청하지 않은 접근인 경우 관리자에게 문의해 주세요."/>
      </div>
    </section>
  </main>

  <div id="customModal" class="modal">
    <div class="modal-content">
      <p id="modalMessage"></p>
      <button type="button" id="closeModalButton"><spring:message code="feature.common.confirm" text="확인"/></button>
    </div>
  </div>

  <div id="confirmModal" class="modal">
    <div class="modal-content">
      <p id="confirmMessage"></p>
      <button type="button" id="confirmYesButton"><spring:message code="feature.common.yes" text="예"/></button>
      <button type="button" id="confirmNoButton" class="cancel"><spring:message code="feature.common.no" text="아니오"/></button>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
  <script>
    var contextPath = "${pageContext.request.contextPath}";
    var isSubmitting = false;
    var passwordMessages = {
      defaultSubmitLabel: '<spring:message code="feature.password.action.change" text="비밀번호 변경" javaScriptEscape="true"/>',
      count: '<spring:message code="feature.password.policy.count" text="영숫자 {0} · 특수 {1}" javaScriptEscape="true"/>',
      matched: '<spring:message code="feature.password.match.success" text="비밀번호가 일치합니다." javaScriptEscape="true"/>',
      mismatched: '<spring:message code="feature.password.match.failure" text="비밀번호가 일치하지 않습니다." javaScriptEscape="true"/>',
      required: '<spring:message code="feature.password.validation.required" text="새 비밀번호를 입력해 주세요." javaScriptEscape="true"/>',
      whitespace: '<spring:message code="feature.password.validation.whitespace" text="비밀번호에는 공백을 포함할 수 없습니다." javaScriptEscape="true"/>',
      invalidCharacters: '<spring:message code="feature.password.validation.invalidCharacters" text="비밀번호에는 영문, 숫자, ASCII 특수문자만 사용할 수 있습니다." javaScriptEscape="true"/>',
      maxLength: '<spring:message code="feature.password.validation.maxLength" text="비밀번호는 전체 20자 이하로 입력해 주세요." javaScriptEscape="true"/>',
      requiredCharacters: '<spring:message code="feature.password.validation.requiredCharacters" text="비밀번호에는 영문과 숫자가 각각 1개 이상 필요합니다." javaScriptEscape="true"/>',
      combination: '<spring:message code="feature.password.validation.combination" text="영숫자 8자 이상과 특수문자 3자 이상 또는 영숫자 10자 이상과 특수문자 2자 이상을 입력해 주세요." javaScriptEscape="true"/>',
      invalidPolicy: '<spring:message code="feature.password.error.invalidPolicy" text="비밀번호 규칙을 충족하지 않습니다." javaScriptEscape="true"/>',
      sessionExpired: '<spring:message code="feature.password.error.sessionExpired" text="로그인 정보가 만료되었습니다. 다시 로그인해 주세요." javaScriptEscape="true"/>',
      saveError: '<spring:message code="feature.password.error.save" text="비밀번호 저장 중 오류가 발생했습니다." javaScriptEscape="true"/>',
      changing: '<spring:message code="feature.password.action.changing" text="변경 중..." javaScriptEscape="true"/>',
      completed: '<spring:message code="feature.password.action.completed" text="변경 완료" javaScriptEscape="true"/>',
      success: '<spring:message code="feature.password.message.success" text="저장되었습니다. 다시 로그인해 주세요." javaScriptEscape="true"/>',
      saveFailed: '<spring:message code="feature.password.message.saveFailed" text="비밀번호 저장에 실패했습니다. 다시 시도해 주세요." javaScriptEscape="true"/>',
      serverError: '<spring:message code="feature.password.message.serverError" text="서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요." javaScriptEscape="true"/>',
      confirmSave: '<spring:message code="feature.password.message.confirmSave" text="변경 사항을 저장하시겠습니까?" javaScriptEscape="true"/>'
    };
    var defaultSubmitLabel = passwordMessages.defaultSubmitLabel;

    function formatPasswordMessage(pattern) {
      var args = Array.prototype.slice.call(arguments, 1);
      return String(pattern || "").replace(/\{(\d+)\}/g, function(match, index) {
        return args[Number(index)] === undefined ? match : args[Number(index)];
      });
    }

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

    function isAsciiLetter(character) {
      return /^[A-Za-z]$/.test(character);
    }

    function isAsciiNumber(character) {
      return /^[0-9]$/.test(character);
    }

    function isAsciiSpecial(character) {
      var code = character.charCodeAt(0);
      return (code >= 33 && code <= 47)
        || (code >= 58 && code <= 64)
        || (code >= 91 && code <= 96)
        || (code >= 123 && code <= 126);
    }

    function analyzePassword(password) {
      var result = {
        alphanumericCount: 0,
        specialCount: 0,
        hasLetter: false,
        hasNumber: false,
        hasInvalidCharacter: false,
        hasWhitespace: false,
        withinMaxLength: password.length <= 20
      };

      for (var i = 0; i < password.length; i++) {
        var character = password.charAt(i);
        if (isAsciiLetter(character)) {
          result.hasLetter = true;
          result.alphanumericCount += 1;
        } else if (isAsciiNumber(character)) {
          result.hasNumber = true;
          result.alphanumericCount += 1;
        } else if (/\s/.test(character)) {
          result.hasWhitespace = true;
        } else if (isAsciiSpecial(character)) {
          result.specialCount += 1;
        } else {
          result.hasInvalidCharacter = true;
        }
      }

      result.hasRequiredCharacters = result.hasLetter && result.hasNumber;
      result.hasValidCharacters = !result.hasWhitespace && !result.hasInvalidCharacter;
      result.optionA = result.alphanumericCount >= 8 && result.specialCount >= 3;
      result.optionB = result.alphanumericCount >= 10 && result.specialCount >= 2;
      result.isValid = password.length > 0
        && result.withinMaxLength
        && result.hasValidCharacters
        && result.hasRequiredCharacters
        && (result.optionA || result.optionB);
      return result;
    }

    function setPolicyState(selector, isMet) {
      $(selector).toggleClass("is-met", Boolean(isMet));
    }

    function updatePasswordStatus() {
      var password = $("#newPassword").val();
      var confirmation = $("#confirmPassword").val();
      var policy = analyzePassword(password);

      $("#passwordCount").text(
        formatPasswordMessage(
          passwordMessages.count,
          policy.alphanumericCount,
          policy.specialCount
        )
      );
      setPolicyState("#policyCharacters", policy.hasRequiredCharacters);
      setPolicyState(
        "#policyLength",
        password.length > 0 && policy.withinMaxLength && policy.hasValidCharacters
      );
      setPolicyState("#policyOptionA", policy.optionA);
      setPolicyState("#policyOptionB", policy.optionB);

      var $match = $("#passwordMatch");
      $match.removeClass("is-match is-mismatch");
      if (!confirmation) {
        $match.text("");
      } else if (password === confirmation) {
        $match.addClass("is-match").text(passwordMessages.matched);
      } else {
        $match.addClass("is-mismatch").text(passwordMessages.mismatched);
      }
      return policy;
    }

    function passwordValidationMessage(password, confirmation, policy) {
      if (!password) {
        return passwordMessages.required;
      }
      if (password !== confirmation) {
        return passwordMessages.mismatched;
      }
      if (policy.hasWhitespace) {
        return passwordMessages.whitespace;
      }
      if (policy.hasInvalidCharacter) {
        return passwordMessages.invalidCharacters;
      }
      if (!policy.withinMaxLength) {
        return passwordMessages.maxLength;
      }
      if (!policy.hasRequiredCharacters) {
        return passwordMessages.requiredCharacters;
      }
      if (!policy.optionA && !policy.optionB) {
        return passwordMessages.combination;
      }
      return "";
    }

    function serverMessage(response, fallbackMessage) {
      var message = response && (response.message || response.failReason);
      var knownMessages = {
        "feature.password.error.invalidPolicy": passwordMessages.invalidPolicy,
        "feature.password.error.sessionExpired": passwordMessages.sessionExpired,
        "feature.password.error.save": passwordMessages.saveError
      };

      if (knownMessages[message]) {
        return knownMessages[message];
      }
      return fallbackMessage;
    }

    function setSubmitting(submitting) {
      isSubmitting = submitting;
      $("#saveButton")
        .prop("disabled", submitting)
        .text(submitting ? passwordMessages.changing : defaultSubmitLabel);
    }

    function submitPasswordForm() {
      if (isSubmitting) {
        return;
      }

      var password = $("#newPassword").val();
      var confirmation = $("#confirmPassword").val();
      var policy = analyzePassword(password);
      var validationMessage = passwordValidationMessage(password, confirmation, policy);
      if (validationMessage) {
        showModal(validationMessage, function() {
          $("#newPassword").trigger("focus");
        });
        return;
      }

      var url = contextPath + "/login/password";
      setSubmitting(true);

      $.ajax({
        url: url,
        type: "POST",
        data: $("#passwordForm").serialize(),
        contentType: "application/x-www-form-urlencoded; charset=UTF-8",
        dataType: "json",
        success: function(response) {
          if (response && response.success) {
            $("#saveButton").text(passwordMessages.completed);
            showModal(passwordMessages.success, function() {
              window.location.href = contextPath + "/login/loginPage";
            });
          } else {
            setSubmitting(false);
            showModal(serverMessage(response, passwordMessages.saveFailed));
          }
        },
        error: function(xhr) {
          setSubmitting(false);
          showModal(serverMessage(
            xhr && xhr.responseJSON,
            passwordMessages.serverError
          ));
        }
      });
    }

    $("#newPassword, #confirmPassword").on("input", updatePasswordStatus);
    $("#newPassword, #confirmPassword").on("keydown", function(event) {
      if (event.key === "Enter") {
        event.preventDefault();
        $("#passwordForm").trigger("submit");
      }
    });

    $("#passwordForm").on("submit", function(event) {
      event.preventDefault();
      if (isSubmitting) {
        return;
      }

      var newPassword = $("#newPassword").val();
      var confirmPassword = $("#confirmPassword").val();
      var policy = updatePasswordStatus();
      var validationMessage = passwordValidationMessage(newPassword, confirmPassword, policy);
      if (validationMessage) {
        showModal(validationMessage, function() {
          $("#newPassword").trigger("focus");
        });
        return;
      }

      showConfirm(passwordMessages.confirmSave, submitPasswordForm);
    });

    updatePasswordStatus();
  </script>
</body>
</html>
