<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" import="java.util.*" %>
<%@page import="kr.esob.fdms.util.seed.seed.Seed128Cipher"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="kr.esob.fdms.commonlogic.value.Constant"%>

<%
  String downloadUrl = "/fileDownload?fileName=" + URLEncoder.encode(Seed128Cipher.encrypt("install/COLLABVIEW_COMMON.exe", Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING));
%>

<!DOCTYPE html>

<html>
<head>
  <title>CollabHub</title>
  <style>
    body {
      background-color: slategrey;
      color: white;
      position: relative;
    }

    .site-wrapper {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      height: 85vh;
    }

    .cover-heading {
      text-align: center;
    }

    .loader {
      border: 30px solid #f3f3f3;
      border-radius: 50%;
      border-top: 30px solid darkgray;
      width: 250px;
      height: 250px;
      animation: spin 3s linear infinite;
      margin: auto;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .self {
      color: white;
      text-align: center;
      display: inline-block;
    }

    .install-link {
      color: white;
      text-align: center;
      display: inline-block;
      margin-left: 10px;
    }

    .instruction-container {
      text-align: center;
      margin-top: 50px;
    }
  </style>
</head>



<script type="text/javascript">


  // 열렸을때 무조건 실행할것
  window.onload = function() {

  };


  <%--var webSocket = new WebSocket("ws://localhost:39229/websocketendpoint");--%>


  <%--webSocket.onopen = function(event) {--%>
  <%--  console.log("Connected");--%>


  <%--  window.location.href = "http://collabhub.iptime.org:3508/login/loginPage#";--%>

  <%--  // 로그인 페이지 호출--%>
  <%--};--%>

  <%--webSocket.onerror = function (event){--%>
  <%--  console.log("UnConnected");--%>

  <%--  // 설치가 안됐을때. => 다운로드 시켜야함--%>
  <%--  var link = document.createElement('a');--%>
  <%--  link.href = "<%=downloadUrl%>";--%>
  <%--  link.click();--%>
  <%--};--%>





</script>




<body>

<div class="site-wrapper">

  <div class="site-wrapper-inner">

    <div class="cover-container">
      <div class="inner cover">
        <h2 class="cover-heading">어서오세요.<br><br> CollabHub에 진입하기 위해 CollabView 설치중입니다.</h2>
        <div class="loader"></div>
        <div class="instruction-container">
          <span class="self" >설치완료 후 새로고침(F5) 해주세요.</span>
          <a class="install-link" href="/fileDownload?fileName=<%=URLEncoder.encode(Seed128Cipher.encrypt("install/COLLABVIEW_COMMON.exe", Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)) %>">수동설치</a>
        </div>
      </div>
    </div>

  </div>

</div>




</body>
</html>













