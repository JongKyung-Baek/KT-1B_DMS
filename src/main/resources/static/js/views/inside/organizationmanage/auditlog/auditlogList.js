var gridParam;

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
