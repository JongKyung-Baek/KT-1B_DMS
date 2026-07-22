<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CollabHub</title>
    <link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>
    <script>
        window.USE_ACCEPTANCE_VUEXY_FORM = true;

        function setGridParam(){
            gridParam = {
                gridId : 'gridDownHistoryList',
                formId : 'formDownHistory',
                url : '/inside/distribution/downHistory/selectList',
                size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
                page : 1,
                multiSelect : true,
                numbering : false,
                selectRowAction : 'check',
                layoutMode : 'invoice',
                fillColumns : true
            }

            return gridParam;
        }


        // 자료명: 행위이력
        function formatViewActLog(cellValue, options, rowdata, action){
            return '<a onclick="openActLogPopup(\'' + rowdata["downloadedName"] + '\')">'+cellValue+'</a>';}

        // 저장 횟수: 파일에 대한 저장 정보
        function formatDownHistory(cellValue, options, rowdata, action){
            return '<a onclick="openDownHistoryPopup(\'' + rowdata["requestNo"] +'\', \'' + rowdata["objectId"] +'\')">'+cellValue+'</a>';
        }

        $(function () {
            $('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
        });

    </script>
</head>
<body>
<div class="distribution-invoice-page">
    <custom:listTemplateInvoice gridId="gridDownHistoryList"/>
</div>

</body>
</html>
