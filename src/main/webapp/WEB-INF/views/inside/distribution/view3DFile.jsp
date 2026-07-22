<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication property="principal" var="sessionUser" />
<spring:message code='form.autoIncrement' var='autoIncrement'/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/VIZCore/Resource/css/VIZCore.css">
    <script type="module">
        import VIZWide3D from "${pageContext.request.contextPath}/resources/js/VIZCore/VIZCore.js";
        import { VIZCore } from "${pageContext.request.contextPath}/resources/js/VIZCore/VIZCore.js";

		// div 요소
 	    let view = document.getElementById("view");
 	    view.className = "VIZCore";

 	    // VIZWeb3D init
 	    let vizwide3d = new VIZWide3D(view);
 	    let initresult = vizwide3d.Init();

		var value = '${fname}';
        alert(value); 

 	    // 테스트 모델 열기
 	    vizwide3d.Model.OpenHeader(value);

 	    // 툴바 생성
 	    let toolbar = new vizwide3d.Toolbar(view, vizwide3d, VIZCore);
    </script>
    <script>
    	function setGridParam(){
    	}
    	$(document).ready(function() {
    		$(".gnb").hide();
    	})
    </script>
		<div id="view"></div>
	    <div id="multiView"></div>
	    <div id="view_info" class="view_info"></div>


