<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% response.setStatus(200); %>
<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Error page</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/custom-font.css" media="screen" />
<style>
body,div,h1,h2,h3,h4,h5,h6,ul,li,ol, dl, dt, dd, header,nav,article,section,footer,button,p,span,form,fieldset,legend,input,label,select,option,iframe{margin:0; padding:0; font-family:"Pretendard", "Noto Sans KR", "Malgun Gothic", sans-serif;}
div,header,nav,article,section,footer{display:block;}
a{text-decoration:none; margin:0; padding:0; border:0;}
img{margin:0; padding:0; border:0;}
ul li{list-style:none;}
button{border:0;}

html{width:100%; height:100%;}
body{width:100%; height:100%; min-width:600px; color:#555; font-size:13px;}

*{box-sizing:border-box; -moz-box-sizing: border-box; -webkit-box-sizing: border-box;}

#errorWrap{width:100%; height:100%; font-weight:600;}
	.codeHeader{background:transparent; width:100%; padding:10px; border-top:3px solid transparent;}
	.codeHeader > div{width:100%; padding:50px 10px 30px; border:2px solid #fff; color:#fff; text-align:center; font-size:0; line-height:0;}
		.codeHeader h1{margin:0 auto 20px;}
			.codeHeader h1 > span{display:inline-block; *display:inline-block; color:#fff; font-size:48px; line-height:1; vertical-align:middle;}
			.codeHeader h1 > span.errorCode{margin-left:15px;}
		.codeHeader p{font-size:14px; line-height:1.5;}
	.codeText{padding:50px 10px 10px; text-align:center;}
		.codeText > span{display:inline-block; *display:inline-block; padding:30px 80px; border-top:1px solid #d0d0d0; color:#555; font-size:13px; line-height:1.5;}
		.codeText > span:first-child{display:block; padding:0 0 20px; border-top:0;}
.code500 .codeHeader{background:#555; border-top-color:#333;}
button{display:inline-block; height:auto; padding:2px 5px; border:1px solid #a0a0a0; border-radius:4px; font-size:13px; line-height:19px; vertical-align:top; cursor: pointer;}
</style>
	<script>
		function movePage(){
			location.href = "/login/loginPage";
		}

		window.onload = function() {
			if (performance.navigation.type == 1) {
				movePage();
			}
		}

	</script>
</head>
<body>


<div id="errorWrap" class="code500">
	<div class="codeHeader">
		<div>
			<h1><span>ERROR</span><span class="errorCode">500</span></h1>
			<p>시스템 오류가 발생했습니다.</p>
		</div>
	</div>
	<p class="codeText">
		<span>브라우저의 재시작 또는 페이지 새로고침을 시도해 보시기 바랍니다.
			<br><br>
		<button type="button" class="btn btn-primary" onclick="movePage()">로그인 페이지</button></span>
		<span>오류가 지속 될 경우 관리자에게 문의하여 주시기 바랍니다.<br>감사합니다.</span>
	</p>
</div>


</body>
</html>
