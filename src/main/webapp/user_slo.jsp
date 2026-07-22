<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<%
	String slo_p_ota = request.getParameter("slo_p_ota");
	String ds = request.getParameter("ds");
	if(slo_p_ota != null){
		slo_p_ota = slo_p_ota.replaceAll("<","<");
		slo_p_ota = slo_p_ota.replaceAll(">",">");
	}else{
		return;
	}
	if(ds != null){
		ds = ds.replaceAll("<","<");
		ds = ds.replaceAll(">",">");
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
		<input type="hidden" name="url_type" id="url_type" value="I">
		<input type="hidden" name="slo_p_ota" id="slo_p_ota" value="<%=slo_p_ota %>">
		<input type="hidden" name="ds" id="ds" value="<%=ds %>">
	</form>
</body>
</html>
