<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
    <meta charset="UTF-8">
    <title>CollabHub</title>
    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/video/videoRequestList.js"></script>
    <script>
        var gridId = 'gridVideoRequestList';
        function setGridParam(){
            gridParam = {
                gridId : gridId,
                formId : 'formVideoRequest',
                url : '/inside/distribution/videoRequest/selectList',
                size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
                page : 1,
                multiSelect : true,
                numbering : false,
                selectRowAction : 'check'
            }

            return gridParam;
        }

        // 2023.07.10 기범추가 ( 등록 버튼 생성, db toolbar에도 생성 )
        function upload(){
            openDialogPopup("/inside/distribution/videoRequest/videoRegisterPopup", {}, "popupDialog", 'm', 500 );
        }

        function formatProtect(cellValue, options, rowdata, action){
            if(cellValue == "Y"){
                return '<a onclick="openDialog(\'' + rowdata["objectId"] + '\')">'+cellValue+'</a>';
            }else{
                return cellValue;
            }
        }



        // function formatViewFile(cellValue, options, rowdata, action){
        //     return '<a onclick="openVideo(\'OBJECT\', \'VIDEO\', \'' + rowdata["objectId"] + '\' )">'+cellValue+'</a>';
        // }
        //
        // function openVideo(objectType, videoType, objectId) {
        //     var url = "/inside/distribution/videoRequest/videoPlayController"
        //         + "?objectType=" + objectType
        //         + "&videoType=" + videoType
        //         + "&objectId=" + objectId
        //
        //     console.log("321321321")
        //     window.open(url, '_blank');
        // }



        function openDialog(objectId){
            openDialogPopup("/inside/distribution/commonRequest/protectPopup", {objectId: objectId, objectType : "VIDEO"}, "popupDialog", 'm', 360);
        }

    </script>
</head>
<body>
<custom:listTemplate gridId="gridVideoRequestList"/>
<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>
