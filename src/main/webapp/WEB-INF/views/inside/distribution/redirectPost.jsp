<%@ page import="java.util.Map" %>
<%@ page import="java.util.Base64" %>
<%
    Map<String, String> params = (Map<String, String>) request.getAttribute("params");
%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dynamic POST Request</title>

    <style>
        body {
            margin: 0;
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            background: #f8f9fa;
            font-family: Arial, sans-serif;
        }

        .loader-container {
            text-align: center;
        }

        .spinner {
            width: 50px;
            height: 50px;
            border: 6px solid #e0e0e0;
            border-top: 6px solid #007bff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 20px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .message {
            font-size: 16px;
            color: #555;
        }
    </style>
</head>

<body>

<div class="loader-container">
    <div class="spinner"></div>
    <div class="message">Please wait...</div>
</div>

<form id="postForm" action="<%= params != null ? params.get("url") : "#" %>" method="POST" accept-charset="UTF-8">
    <%
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();

                if ("url".equals(name)) continue;

                if ("userName".equals(name)) {
                    String userNameBase64 = Base64.getEncoder()
                            .encodeToString((value == null ? "" : value).getBytes("UTF-8"));
    %>
        <input type="hidden" name="userNameBase64" value="<%= userNameBase64 %>">
    <%
                }
    %>
        <input type="hidden" name="<%= name %>" value="<%= value %>">
    <%
            }
        }
    %>
</form>

<script>
    window.onload = function() {
        document.getElementById("postForm").submit();
    };
</script>

</body>
</html>
