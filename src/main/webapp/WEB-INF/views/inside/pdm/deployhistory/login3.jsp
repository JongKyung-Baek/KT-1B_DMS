<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Login - CollabHub</title>
<script type="text/javascript" src="/resources/js/jquery-3.4.1.min.js"></script>
<script type="text/javascript" src="/resources/js/i18n/grid.locale-kr.js"></script>
<script type="text/javascript" src="/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="/resources/css/jquery-ui-1.12.1.custom/jquery-ui.js"></script>
<script type="text/javascript" src="/resources/js/gridscript.js"></script>
<script type="text/javascript" src="/resources/js/common.js"></script>
<link type="text/css" rel="stylesheet" href="/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
<link type="text/css" rel="stylesheet" href="/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
<link type="text/css" rel="stylesheet" href="/resources/css/style.css" media="screen" />
<style>

body{overflow:hidden;}
	.loginWrap{width:100%; height:100%; margin:0; padding:0; overflow:auto;}
	.loginWrap .header{min-width:470px; box-shadow:none;}
	
	.loginArea{background:url("/resources/images/KARI/bg_login.jpg") no-repeat top left; background-size:contain; width:100%; height:calc(100% - 60px); min-width:470px; min-height:510px; margin:0; padding:0; position:relative; top:60px;}

	.loginBox{background:rgba(255,255,255,0.8); width:460px; height:500px; margin-top:-250px; margin-left:-200px; padding:20px; border:1px solid #c0c0c0; border-top-color: #0051a2; font-size:0; line-height:0; box-shadow:0 3px 5px rgba(50,50,50,0.3); position:absolute; top:50%; left:50%;}

		.loginBox > div{width:100%; height:100%; padding:0; border:1px solid #e0e0e0;}

			.loginBox h1, .loginBox h2, .loginBox div > p, .loginBox form{display:block;}

			.loginBox h1{width:280px; margin:0 auto; padding:50px 0 0; color: #0051a2; font-size:30px; font-weight:900; line-height:40px; text-align:center;}
			.loginBox div > p{width:280px; margin:15px auto 20px; padding:0 0 20px; border-bottom:1px solid #c0c0c0; color:#505050; font-size:12px;}
			.loginBox div > p span{font-weight:600;}
			.loginBox form{width:280px; margin:0 auto; padding-bottom:20px; text-align:left;}
			.loginBox form label, .loginBox form input{display:inline-block; height:40px; margin:5px 0; line-height:40px; vertical-align:middle;}
			.loginBox form label{width:100px; color:#555;font-weight:600;}
			.loginBox form label:before{content:''; display:inline-block; width:4px; height:4px; margin-right:5px; border:2px solid #0051a2; border-radius:50%; vertical-align:middle;}
			.loginBox form input{width:calc(100% - 100px);}
			.loginSubmitBtn{background: #ff6600; display:block; width:100%; margin:10px 0; border-color:#e85d00; color:#fff; font-size:14px; font-weight:600; line-height:35px;}
			.loginBox p.linkBtnArea{width:280px; margin:0 auto; padding:0; border:0; color:#505050; font-size:0; line-height:0; text-align:right;}
			.loginBox p.linkBtnArea a{display:inline-block; margin:0 5px; padding:3px 5px; border-bottom:1px solid #707070; color:#505050; line-height:1; vertical-align:bottom;}
			.loginBox p.linkBtnArea a:first-child{margin-left:0;}
			.loginBox p.linkBtnArea a:last-child{margin-right:0;}


	@media screen and (max-width:500px){
		.loginBox{margin-left:auto; left:5px;}	
	}
	@media screen and (max-height:550px){
		.loginBox{margin-top:auto; top:5px;}	
	}
	@media screen and (max-width:500px) and (max-height:550px){
		.loginBox{margin-top:auto; margin-left:auto; top:5px; left:5px;}	
	}
</style>
<script>
	$(document).ready(function(){

	});
</script>
</head>
<body>
<div class="loginWrap inside">
	<div class="header">
		<div class="left">
			<h1 class="logo">
				<a href="#">logo</a>
				<span>CollabHub</span>
			</h1>
		</div>
		<div class="right">
		</div>
	</div>
	<div class="loginArea">
		<div class="loginBox">
			<div>
				<h1>CollabHub</h1>
				<p><span>외부 관리자용</span> 로그인 페이지</p>
				<form>
					<label for="businessNumber">사업자번호</label><input type="text" id="businessNumber" placeholder="사업자번호를 입력하세요">
					<label for="userName">성명</label><input type="text" id="userName" placeholder="성명을 입력하세요">
					<label for="userPassword">비밀번호</label><input type="password" id="userPassword" placeholder="비밀번호를 입력하세요">
					<button type="button" class="loginSubmitBtn">로그인</button>				
				</form>
				<!--<p class="linkBtnArea">
					  <a href="#" class="selectLanguage en">English</a>
					<a href="#" class="pwInquiry">비밀번호 찾기</a>
				</p> -->
			</div>
		</div>
	</div>
</div>
</body>
</html>
