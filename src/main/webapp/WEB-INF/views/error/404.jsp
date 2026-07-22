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
		.codeText > span{display:inline-block; display:inline-block; padding:30px 80px; border-top:1px solid #d0d0d0; color:#555; font-size:13px; line-height:1.5;}
		.codeText > span:first-child{display:block; padding:0 0 20px; border-top:0;}
	.homeButtonArea{padding:24px 10px 0; text-align:center;}
		.homeButtonArea .homeButton{display:inline-flex; align-items:center; justify-content:center; min-width:180px; height:44px; padding:0 24px; border-radius:22px; background:#034c8c; color:#fff; font-size:14px; font-weight:600; line-height:1; text-decoration:none;}
.code404 .codeHeader{background:#034c8c; border-top-color:#4c5e8e;}
</style>
</head>
<body>

<div id="errorWrap" class="code404">
	<div class="codeHeader">
		<div>
			<h1><span>ERROR</span><span class="errorCode">404</span></h1>
			<p>죄송합니다.<br>요청하신 페이지를 찾을 수 없습니다.</p>
		</div>
	</div>
	<p class="codeText">
		<span>방문하시려는 페이지의 주소가 잘못 입력되었거나,<br>페이지의 주소가 변경 혹은 삭제되어 요청하신 페이지를 찾을 수 없습니다.</span>
		<span>입력하신 주소가 정확한지 다시 한번 확인해 주시기 바랍니다.</span>
	</p>
	<div class="homeButtonArea">
		<!-- <a class="homeButton" href="${pageContext.request.contextPath}/main">메인화면으로 돌아가기</a> -->
		 <a class="homeButton" href="${pageContext.request.contextPath}/inside/distribution/swRequest/">메인화면으로 돌아가기</a>
	</div>
</div>
</body>
</html>
