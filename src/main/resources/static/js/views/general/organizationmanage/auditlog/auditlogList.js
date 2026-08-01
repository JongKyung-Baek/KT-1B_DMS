var gridParam;

function auditText(key, fallback) {
    var args = Array.prototype.slice.call(arguments, 2);
    var translated = fallback;
    if (window.SdmsI18n && typeof window.SdmsI18n.t === "function") {
        translated = window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
    }
    if (!translated) translated = fallback || key;
    return String(translated).replace(/\{(\d+)\}/g, function (match, index) {
        return args[Number(index)] === undefined ? match : args[Number(index)];
    });
}

function getAuditActionLabel(actionType) {
    var labels = {
        LOGIN: ["feature.audit.action.login", "로그인"],
        LOGOUT: ["feature.audit.action.logout", "로그아웃"],
        PASSWORD_CHANGE: ["feature.audit.action.passwordChange", "비밀번호 변경"],
        PASSWORD_RESET: ["feature.audit.action.passwordReset", "비밀번호 초기화"],
        DOWNLOAD: ["feature.audit.action.download", "다운로드"],
        PRINT: ["feature.audit.action.print", "출력"],
        VIEW: ["feature.audit.action.view", "열람"],
        READ: ["feature.audit.action.read", "조회"],
        DELETE: ["feature.audit.action.delete", "삭제"],
        REJECT: ["feature.audit.action.reject", "반려"],
        APPROVE: ["feature.audit.action.approve", "승인"],
        SAVE: ["feature.audit.action.save", "저장"],
        CREATE: ["feature.audit.action.create", "등록"],
        UPDATE: ["feature.audit.action.update", "수정"],
        EXECUTE: ["feature.audit.action.execute", "실행"],
        MANAGE_GRADE: ["feature.audit.action.manageGrade", "보안등급 관리"],
        MANAGE_CLEARANCE: ["feature.audit.action.manageClearance", "사용자 인가 관리"],
        MANAGE_FILE_LABEL: ["feature.audit.action.manageFileLabel", "문서등급 관리"],
        MANAGE_FILE_PERMISSION: ["feature.audit.action.manageFilePermission", "문서 권한 관리"],
        MANAGE_ACL: ["feature.audit.action.manageAcl", "ACL 관리"]
    };
    var entry = labels[String(actionType || "").toUpperCase()];
    return entry ? auditText(entry[0], entry[1]) : "";
}

function escapeAuditHtml(value) {
    if (value === null || typeof value === "undefined") {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function getAuditRowValue(rowObject, keys, fallbackValue) {
    var row = rowObject && typeof rowObject === "object" ? rowObject : {};

    for (var i = 0; i < keys.length; i += 1) {
        var value = row[keys[i]];
        if (value !== null && typeof value !== "undefined" && String(value).trim() !== "") {
            return value;
        }
    }

    return fallbackValue === null || typeof fallbackValue === "undefined" ? "" : fallbackValue;
}

function getAuditUniqueValues(values) {
    var uniqueValues = [];
    var normalizedValues = {};

    for (var i = 0; i < values.length; i += 1) {
        var value = values[i];
        if (value === null || typeof value === "undefined") {
            continue;
        }

        var displayValue = String(value).trim();
        var normalizedValue = displayValue.toLowerCase();
        if (!displayValue || normalizedValues[normalizedValue]) {
            continue;
        }

        normalizedValues[normalizedValue] = true;
        uniqueValues.push(displayValue);
    }

    return uniqueValues;
}

function buildAuditCell(primaryValue, secondaryValues, modifierClass) {
    var primary = String(primaryValue === null || typeof primaryValue === "undefined"
        ? "" : primaryValue).trim();
    var secondary = getAuditUniqueValues(secondaryValues || []);
    var safeModifier = modifierClass ? " " + modifierClass : "";

    if (!primary) {
        primary = auditText("feature.audit.value.notCollected", "미수집");
    }

    var html = '<span class="audit-grid-cell' + safeModifier + '">';
    html += '<strong class="audit-grid-cell__primary" title="' + escapeAuditHtml(primary) + '">'
        + escapeAuditHtml(primary) + '</strong>';

    if (secondary.length > 0) {
        var secondaryText = secondary.join(" · ");
        html += '<small class="audit-grid-cell__meta" title="' + escapeAuditHtml(secondaryText) + '">'
            + escapeAuditHtml(secondaryText) + '</small>';
    }

    html += "</span>";
    return html;
}

function formatAuditUser(cellValue, options, rowObject) {
    var userName = getAuditRowValue(rowObject, ["userNm", "userName"], cellValue);
    var userId = getAuditRowValue(rowObject, ["userId"], "");
    var userCd = getAuditRowValue(rowObject, ["userCd"], "");
    var primary = userName || userId || userCd || cellValue;
    var secondary = [];

    if (userId && String(userId) !== String(primary)) {
        secondary.push(userId);
    }
    if (userCd && String(userCd) !== String(primary) && String(userCd) !== String(userId)) {
        secondary.push(userCd);
    }

    return buildAuditCell(primary, secondary, "audit-grid-cell--user");
}

function formatAuditMenu(cellValue, options, rowObject) {
    var menuName = getAuditRowValue(rowObject, ["menuNm"], cellValue);
    var menuCode = getAuditRowValue(rowObject, ["menuCd"], "");
    var menuPath = getAuditRowValue(rowObject, ["menuUrl", "requestUri"], "");
    var httpMethod = getAuditRowValue(rowObject, ["httpMethod"], "");
    var primary = menuName || menuCode || menuPath || cellValue;
    var secondary = [];

    if (String(menuCode).toUpperCase() === "AUTH") {
        primary = auditText("feature.audit.menu.authentication", "인증 / 계정");
    }

    if (menuCode && String(menuCode) !== String(primary)) {
        secondary.push(menuCode);
    }
    if (menuPath && String(menuPath) !== String(primary)) {
        secondary.push((httpMethod ? String(httpMethod).toUpperCase() + " " : "") + menuPath);
    }

    return buildAuditCell(primary, secondary, "audit-grid-cell--menu");
}

function formatAuditAction(cellValue, options, rowObject) {
    var actionName = getAuditRowValue(rowObject, ["actionNm"], cellValue);
    var actionType = getAuditRowValue(rowObject, ["actionType"], "");
    var eventType = getAuditRowValue(rowObject, ["eventType"], "");
    var sourceType = getAuditRowValue(rowObject, ["sourceType"], "");
    var primary = getAuditActionLabel(actionType) || actionName || actionType || eventType || cellValue;
    var secondary = [];

    if (actionType && String(actionType) !== String(primary)) {
        secondary.push(actionType);
    }
    if (eventType && String(eventType) !== String(primary)) {
        secondary.push(eventType);
    }
    if (sourceType && String(sourceType) !== String(primary)) {
        secondary.push(sourceType);
    }

    return buildAuditCell(primary, secondary, "audit-grid-cell--action");
}

function formatAuditTarget(cellValue, options, rowObject) {
    var targetSummary = getAuditRowValue(rowObject, ["targetSummary"], cellValue);
    var objectId = getAuditRowValue(rowObject, ["objectId"], "");
    var fileNo = getAuditRowValue(rowObject, ["fileNo"], "");
    var requestNo = getAuditRowValue(rowObject, ["requestNo"], "");
    var objectType = getAuditRowValue(rowObject, ["objectType"], "");
    var gradeCode = getAuditRowValue(rowObject, ["gradeCd"], "");
    var primary = targetSummary || objectId || fileNo || requestNo || cellValue;
    var secondary = [];

    if (objectType && String(objectType) !== String(primary)) {
        secondary.push(objectType);
    }
    if (objectId && String(objectId) !== String(primary)) {
        secondary.push(auditText("feature.audit.target.object", "대상 {0}", objectId));
    }
    if (fileNo && String(fileNo) !== String(primary)) {
        secondary.push(auditText("feature.audit.target.file", "파일 {0}", fileNo));
    }
    if (requestNo && String(requestNo) !== String(primary)) {
        secondary.push(auditText("feature.audit.target.request", "요청 {0}", requestNo));
    }
    if (gradeCode && String(gradeCode) !== String(primary)) {
        secondary.push(auditText("feature.audit.target.grade", "등급 {0}", gradeCode));
    }

    return buildAuditCell(primary, secondary, "audit-grid-cell--target");
}

function setGridParam() {
    gridParam = {
        gridId: gridId,
        formId: formId,
        url: '/general/organizationmanage/auditlog/selectList',
        size: "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
        page: 1,
        multiSelect: true,
        numbering: false,
        selectRowAction: 'check',
        layoutMode: 'invoice',
        fillColumns: true
    };

    return gridParam;
}

function searchAuditLogList() {
    searchList(gridParam);
}

function resetAuditLogSearch() {
    if ($("#" + formId).length > 0) {
        $("#" + formId)[0].reset();
        $("#" + formId).find("select").trigger("change");
    }

    searchAuditLogList();
}

function ensureAuditLogResetButton() {
    var $actions = $("#" + formId + " .formAcceptanceActions").first();
    var $resetButtons = $("#" + formId + " #auditLogResetButton");

    if (!$actions.length) {
        return;
    }

    if ($resetButtons.length > 1) {
        $resetButtons.slice(1).remove();
    }

    if (!$resetButtons.length) {
        $("<button>", {
            id: "auditLogResetButton",
            type: "button",
            class: "audit-log-reset-btn",
            "aria-label": auditText("feature.audit.search.resetAria", "접근·감사이력 검색조건 초기화")
        })
            .append($("<i>", {
                class: "icon-base ti tabler-refresh",
                "aria-hidden": "true"
            }))
            .append(document.createTextNode(auditText("feature.common.reset", "초기화")))
            .on("click", resetAuditLogSearch)
            .prependTo($actions);
    }
}

function downloadAuditLogExcel() {
    alertMessage(auditText("feature.audit.excel.unavailable", "엑셀 다운로드는 아직 지원하지 않습니다."));
}

$(function () {
    $('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
    ensureAuditLogResetButton();
});
