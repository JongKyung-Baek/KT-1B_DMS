var gridParam;

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
        primary = "미수집";
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
    var primary = actionName || actionType || eventType || cellValue;
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

function getAuditResultPresentation(resultCode) {
    var code = String(resultCode || "").trim().toUpperCase();
    var presentation = {
        code: code,
        label: code || "미수집",
        tone: "neutral"
    };

    if (code === "ALLOW") {
        presentation.label = "허용";
        presentation.tone = "success";
    } else if (code === "SUCCESS") {
        presentation.label = "성공";
        presentation.tone = "success";
    } else if (code === "DENY") {
        presentation.label = "거부";
        presentation.tone = "denied";
    } else if (code === "FAILURE" || code === "FAIL" || code === "ERROR") {
        presentation.label = "실패";
        presentation.tone = "failed";
    }

    return presentation;
}

function formatAuditResult(cellValue, options, rowObject) {
    var resultCode = getAuditRowValue(rowObject, ["resultCd"], cellValue);
    var reasonCode = getAuditRowValue(rowObject, ["reasonCd"], "");
    var resultMessage = getAuditRowValue(rowObject, ["resultMessage"], "");
    var httpStatus = getAuditRowValue(rowObject, ["httpStatus"], "");
    var durationMs = getAuditRowValue(rowObject, ["durationMs"], "");
    var presentation = getAuditResultPresentation(resultCode);
    var secondary = getAuditUniqueValues([
        httpStatus === "" ? "" : "HTTP " + httpStatus,
        durationMs === "" ? "" : durationMs + "ms",
        reasonCode,
        resultMessage
    ]);
    var html = '<span class="audit-grid-result">';

    html += '<span class="audit-result-badge audit-result-badge--'
        + presentation.tone + '" title="' + escapeAuditHtml(presentation.code || presentation.label) + '">'
        + escapeAuditHtml(presentation.label) + "</span>";

    if (secondary.length > 0) {
        var secondaryText = secondary.join(" · ");
        html += '<small class="audit-grid-cell__meta" title="' + escapeAuditHtml(secondaryText) + '">'
            + escapeAuditHtml(secondaryText) + "</small>";
    }

    html += "</span>";
    return html;
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
        secondary.push("대상 " + objectId);
    }
    if (fileNo && String(fileNo) !== String(primary)) {
        secondary.push("파일 " + fileNo);
    }
    if (requestNo && String(requestNo) !== String(primary)) {
        secondary.push("요청 " + requestNo);
    }
    if (gradeCode && String(gradeCode) !== String(primary)) {
        secondary.push("등급 " + gradeCode);
    }

    return buildAuditCell(primary, secondary, "audit-grid-cell--target");
}

function setGridParam() {
    gridParam = {
        gridId: gridId,
        formId: formId,
        url: '/inside/organizationmanage/auditlog/selectList',
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

function downloadAuditLogExcel() {
    alertMessage("Excel download is not implemented yet.");
}

$(function () {
    $('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
});
