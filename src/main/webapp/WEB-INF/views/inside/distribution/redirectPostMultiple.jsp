<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="java.util.Base64" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%
    Map<String, Object> params = (Map<String, Object>) request.getAttribute("params");

    String authority = (String) params.get("authority");
    String userName = (String) params.get("userName");
    String userID = (String) params.get("userID");
    
    String releasedToRIWatermarkYn = (String) params.get("releasedToRIWatermarkYn");

    List<Map<String, Object>> revisionList = (List<Map<String, Object>>) params.get("revisionList");
    ObjectMapper objectMapper = new ObjectMapper();
    String revisionListJson = objectMapper.writeValueAsString(revisionList);

    String base64Encoded = Base64.getEncoder()
            .encodeToString(revisionListJson.getBytes("UTF-8"));

    String objectID = (String) params.get("objectID");
    String finalURL = (String) params.get("finalURL");
    String userNameEnc = URLEncoder.encode(userName == null ? "" : userName, "UTF-8");
    String userNameBase64 = Base64.getEncoder()
            .encodeToString((userName == null ? "" : userName).getBytes("UTF-8"));
%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>Please Wait...</title>

<style>
    body {
        margin: 0;
        height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        background: #f5f7fa;
        font-family: Arial, sans-serif;
    }

    .loader-container {
        text-align: center;
    }

    .spinner {
        width: 60px;
        height: 60px;
        border: 6px solid #e0e0e0;
        border-top: 6px solid #0051a2;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin: 0 auto 20px;
    }

    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }

    .message {
        font-size: 18px;
        font-weight: 600;
        color: #333;
    }

    .sub-message {
        font-size: 14px;
        color: #666;
        margin-top: 8px;
    }
</style>
</head>

<body>

<div class="loader-container">
    <div class="spinner"></div>
    <div class="message">Please wait...</div>
    <div class="sub-message">Revision data is being processed.</div>
</div>

<!-- <form id="postForm" action="https://demo.esob.kr:7442/cv_post" method="POST">
    <input type="hidden" name="authority" value="<%= authority %>">
    <input type="hidden" name="userName" value="<%= userName %>">
    <input type="hidden" name="userID" value="<%= userID %>">
    <input type="hidden" name="objectID" value="<%= objectID %>">
    <input type="hidden" name="revisionListBase64" value="<%= base64Encoded %>">
    <input type="hidden" name="finalURL" value="<%= finalURL %>">
</form> -->

<!-- [KAI] -->
<!-- <form id="postForm" action="http://localhost:7442/cv_post" method="POST" accept-charset="UTF-8"> -->
<form id="postForm" action="http://192.168.10.194:7442/cv_post" method="POST" accept-charset="UTF-8"> 
    <input type="hidden" name="authority" value="<%= authority %>">
    <input type="hidden" name="userName" value="<%= userNameEnc %>">
    <input type="hidden" name="userNameEnc" value="<%= userNameEnc %>">
    <input type="hidden" name="userNameBase64" value="<%= userNameBase64 %>">
    <input type="hidden" name="userID" value="<%= userID %>">
    <input type="hidden" name="objectID" value="<%= objectID %>">
    <input type="hidden" name="revisionListBase64" value="<%= base64Encoded %>">
    <input type="hidden" name="finalURL" value="<%= finalURL %>">
    <input type="hidden" name="releasedToRIWatermarkYn" value="<%= releasedToRIWatermarkYn %>">
</form>

<script>
window.onload = function() {
    document.getElementById('postForm').submit();
};
</script>

</body>
</html>
