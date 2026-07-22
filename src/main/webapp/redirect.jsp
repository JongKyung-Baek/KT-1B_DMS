<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<%
	String p = request.getParameter("p");
	String url_type = request.getParameter("url_type");
	if(p != null){
		p = p.replaceAll("<","<");
		p = p.replaceAll(">",">");
	}else{
		return;
	}
	if(url_type != null){
		url_type = url_type.replaceAll("<","<");
		url_type = url_type.replaceAll(">",">");
	}else{
		return;
	}
%>
<html>
<head>
<meta charset="EUC-KR">
<title>Insert title here</title>
</head>
<script>
	function fnCheckLogin(){
		document.frmUserAuth.submit();
	}
</script>
<body onload="fnCheckLogin()">
	<form name="frmUserAuth" name="frmUserAuth" method="POST" action="/login/loginProcess">
		<input type="hidden" name="url_type" id="url_type" value="<%=url_type %>">
		<input type="hidden" name="p" id="p" value="<%=p %>">
	</form>
</body>
</html>