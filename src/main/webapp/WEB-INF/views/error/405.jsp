<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>허용되지 않은 요청 방식</title>
<style>
html, body { width: 100%; height: 100%; margin: 0; }
body {
	display: flex;
	align-items: center;
	justify-content: center;
	background: #f4f6f8;
	color: #25313c;
	font-family: "Pretendard", "Noto Sans KR", "Malgun Gothic", sans-serif;
}
.error-card {
	width: min(520px, calc(100% - 48px));
	padding: 48px;
	border-radius: 12px;
	background: #fff;
	box-shadow: 0 12px 32px rgba(25, 45, 65, 0.12);
	text-align: center;
}
.error-code { margin: 0 0 12px; color: #034c8c; font-size: 48px; }
h1 { margin: 0 0 18px; font-size: 22px; }
p { margin: 0; color: #5c6873; line-height: 1.7; }
</style>
</head>
<body>
	<main class="error-card">
		<p class="error-code">405</p>
		<h1>허용되지 않은 요청 방식입니다.</h1>
		<p>이 기능은 지정된 방식으로만 요청할 수 있습니다.<br>이전 화면으로 돌아가 다시 시도해 주세요.</p>
	</main>
</body>
</html>
