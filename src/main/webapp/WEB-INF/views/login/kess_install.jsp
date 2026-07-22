<!DOCTYPE>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>KESS</title>

<style type="text/css">
* {margin:0; padding:0}
/* Webfont */
@font-face {font-family:'NanumGothic'; src:url('NanumGothic.eot');}
@font-face {font-family:'NanumGothic'; src:url('NanumGothic.woff');}

html, body {width: 100%; font-family:'NanumGothic', Malgun Gothic, "돋움",Dotum,"굴림",Gulim,Verdana,Helvetica,arial; }
body {background:url(images/bg.png); font-size:16px; color:#333333;}
#wrap {width:500px; margin:65px auto 50px; border:1px solid #2e3741; background:#ffffff; -webkit-border-radius: 6px; -moz-border-radius: 6px; border-radius: 6px; overflow:hidden;} 
#wrap .head {height:50px; background:#2e3741;}
#wrap .head .logo {margin:5px 0 0 22px;}
#wrap .mainIMG {width:100%; height:180px;}
#wrap .caution {margin:45px auto 50px; width:60%; min-height:65px; background:url(images/caution.png) no-repeat; padding-left:85px; line-height:30px; letter-spacing:-0.07em;}
#wrap .caution .txt1 {font-size:20px;}
#wrap .caution .txt2 {color:#ff4800;}
#wrap .download {margin:0 auto 35px; text-align:center;}
#wrap .download .button {border:0; background:#2162ab; -webkit-border-radius: 4px; -moz-border-radius: 4px; border-radius: 4px; text-align:center; font-size:20px; font-weight:600; color:#ffffff; padding:18px 60px; }

</style>
</head>

<body>
<div id="wrap">
	<!-- 
    <div class="head">
        <span class="logo"><img src="${pageContext.request.contextPath}/resources/images/kess/top_logo.png" alt=""></span>
    </div>-->
    <div class="mainIMG"><img src="${pageContext.request.contextPath}/resources/images/kess/main_img.png" alt=""></div>
    <div class="caution">
        <span class="txt1">KESS 클라이언트를 설치 해 주세요.</span><br>
        <span class="txt2">미설치 시 PC 사용이 제한</span> 될 수 있습니다.
    </div>
    <div class="download">
        <button class="button" onclick="location.href='http://192.168.1.1:8080/down/install/kess.exe';windows.close();">KESS 다운로드</button>
    </div>
</div>

</body>
</html>
