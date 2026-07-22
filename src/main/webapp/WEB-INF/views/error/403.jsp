<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% response.setStatus(200); %>
<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Error page</title>
<style>
body,div,h1,h2,h3,h4,h5,h6,ul,li,ol, dl, dt, dd, header,nav,article,section,footer,button,p,span,form,fieldset,legend,input,label,select,option,iframe{margin:0; padding:0; font-family:"돋움", Dotum, Helvetica, Sans-serif;}
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
.code403 .codeHeader{background:#59b2b5; border-top-color:#3d989b;}
</style>
</head>
<body>

<div id="errorWrap" class="code403">
	<div class="codeHeader">
		<div>
			<!-- <h1><span>ERROR</span><span class="errorCode">403</span></h1> -->
			<h1><span>접근 권한이 없는 페이지입니다.</span></h1>
			<!-- <p>접근 권한이 없는 페이지입니다.</p> -->
		</div>
	</div>
	<p class="codeText">
		<span>해당 페이지에 접근 권한이 없거나 정상적이지 않은 경로로 접근되었습니다.</span>
		<span>입력하신 주소가 정확한지 다시 한번 확인해 주시기 바랍니다.</span>
	</p>
</div>
</body>
</html>
