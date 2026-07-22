<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!doctype html>
<html lang="kr">
<head>
    <meta charset="UTF-8">
    <title>세션 관리</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f8f9fa;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            box-sizing: border-box;
        }
        .container1 {
            max-width: 500px;
            width: 100%;
            padding: 20px;
            border: 1px solid #ccc;
            background-color: #fff;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            text-align: left;
            box-sizing: border-box;
        }
        h1 {
            font-size: 20px;
            margin-bottom: 20px;
            color: #333;
        }
        .session-config {
            margin-top: 10px;
        }
        .form-group {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
        }
        label {
            width: 150px;
            font-size: 14px;
            color: #000;
            margin-right: 10px;
        }
        input[type="text"] {
            flex: 1;
            padding: 5px;
            font-size: 14px;
            border: 1px solid #ccc;
            background-color: #fff;
            text-align: center;
        }
        button {
            padding: 6px 12px;
            font-size: 14px;
            color: #000;
            background-color: #e7e7e7;
            border: 1px solid #ccc;
            cursor: pointer;
            margin-left: 10px;
        }
        button:hover {
            background-color: #d0d0d0;
        }
    </style>
</head>
<body>
<div class="container1">
    <h1>세션 관리</h1>
    <div class="session-config">
        <div class="form-group">
            <label for="sessionTimeout">세션 만료 시간 (분)</label>
            <input type="text" id="sessionTimeout" name="sessionTimeout" value="10" placeholder="숫자 입력"/>
            <button type="submit" id="saveButton">저장</button>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        $("#saveButton").click(function(e) {
            e.preventDefault();  // 기본 폼 제출 방지

            let sessionTimeout = $("#sessionTimeout").val();  // jQuery로 입력 값 가져오기

            // 숫자 검증 및 범위 체크
            if (sessionTimeout === "" || isNaN(sessionTimeout)) {
                alert("유효한 숫자를 입력하세요.");
                return;
            }

            // 입력 값을 숫자로 변환
            let sessionTime = parseInt(sessionTimeout, 10);

            // 입력 범위 검증 (10 ~ 120 사이의 값인지 확인)
            if (sessionTime < 10 || sessionTime > 240) {
                alert("세션 만료 시간은 10분에서 240분 사이여야 합니다.");
                return;
            }

            // AJAX 요청
            $.ajax({
                url: "${pageContext.request.contextPath}/extendSession/sessionTime",
                type: "POST",
                contentType: "application/json; charset=UTF-8",  // JSON 형식으로 전송
                data: JSON.stringify({
                    sessionTime: sessionTime
                }),
                success: function(response) {
                    if (response.success) {
                        alert("저장되었습니다.");
                        SessionTimer.init(sessionTimeout*60);
                        SessionTimer.updateTime(sessionTimeout*60);
                    } else {
                        alert("저장에 실패했습니다. 다시 시도해주세요.");
                    }
                },
                error: function(xhr, status, error) {
                    alert("저장에 실패했습니다. 오류 코드: " + xhr.status + " - " + xhr.responseText);
                }
            });
        });
    });
</script>
</body>
</html>
