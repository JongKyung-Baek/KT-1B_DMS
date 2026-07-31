<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Viewer</title>
    <style>
        body { margin: 0; height: 100vh; display: flex; justify-content: center;
               align-items: center; background: #f8f9fa; font-family: Arial, sans-serif; }
        .loader { color: #555; }
    </style>
</head>
<body>
<div class="loader">Please wait...</div>
<form id="postForm" action="${fn:escapeXml(params.url)}" method="POST" accept-charset="UTF-8">
    <input type="hidden" name="launchToken"
           value="${fn:escapeXml(params.launchToken)}">
</form>
<script>
    window.addEventListener('load', function () {
        document.getElementById('postForm').submit();
    });
</script>
</body>
</html>
