<!doctype html>
<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver"%>
<%@page import="java.util.Locale"%>
<html>
<head>

<%@ page session="false" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="custom" 	tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<title>메뉴추가</title>
<!-- <link rel="shortcut icon"  type="image/x-icon" href="${pageContext.request.contextPath}/resources/images/main/favicon.ico" /> -->
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-combined.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap-datetimepicker.min.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.css" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/jqGrid-master/css/ui.jqgrid.css" media="screen"/>
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/js/select2-master/dist/css/select2.css" media="screen" />
	<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css" media="screen" />

	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jquery-3.4.1.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/grid.locale-kr.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jqGrid-master/js/jquery.jqGrid.min.js"></script>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap.min.js"></script> --%>
<%-- 	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap-datetimepicker.js"></script> --%>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/esapi.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/ESAPI_Standard_en_US.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/esapi/Base.esapi.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_util.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_validation.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_dialog.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_form.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_toolbar.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common_grid_paging.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/common.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/css/jquery-ui-1.12.1.custom/jquery-ui.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/prettyCheck/prettyCheckable.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/select2-master/dist/js/select2.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-i18n-properties-master/jquery.i18n.properties.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/common_i18n.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/i18n/jquery-ui-i18n.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rsa.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/jsbn.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/prng4.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/rng.js"></script>

</head>
<body>
	<form action="/menu/insertMenu" method="POST" id="form">
		상위메뉴 :
		<select id='parentMenuCd' style='width: 300px' name="parentMenuCd">
		<option></option>
		</select>
		<br>
		<br>
		메뉴명   :
		<input type="text" id="menuNm" name="menuNm" value="">
		<br>
		<br>
		메뉴타입 :
		<select id="menuType" name="menuType"style="width: 300px">
			<option value="M">메뉴</option>
			<option value="P">팝업</option>
			<option value="D">폐기</option>
			<option value="E">거래종료</option>
			<option value="R">배포승인요청</option>
		</select>
		<br>
		<br>
		메뉴URL :
		<input type="text" id="menuUrl" name="menuUrl" value="">
		<br>
		<br>
		<input type="button" value="추가" onClick="menuAdd()">

		<c:forEach items="${sessionScope.menuTop }" var="menuTop">
		<a href="" title="${menuTop.menuNm}"><span>${menuTop.menuNm}</span></a>
		</c:forEach>
	</form>
	<script>
		initSelectRemoteDataExcept('#parentMenuCd', '/menu/getMenuCombo', '', '', '');
		$("#menuType").select2();

		function menuAdd(){
			var formData = $("#form").serialize();
			$.ajax({
				url: "/menu/insertMenu"
				, type : "POST"
				, cache: false
				, dataType : "json"
				, data : formData
				, success : function(data){
				}
				,error : function(e){
				}
			})
		}



		function initSelectRemoteDataExcept(selectedId, url, selectedValue, nullText, exceptCode) {
			var $ts = $(selectedId);
			// width 설정
		    width = $(selectedId).css("width") != undefined ? $(selectedId).css("width") : "90%";

		    var placeholder = $ts.attr("placeholder");

		    if(undefined === placeholder || "" === placeholder) {
		    	placeholder = "선택하세요";
		    }

		    $ts.select2({
		        width: width,
		        nullText: nullText,
		        minimumInputLength: 1,
		        placeholder: placeholder,
		        maximumSelectionLength: 5,

		    	ajax: {
		    	    url: url,
		    	    delay: 250,
		    	    dataType: 'json'
		    	  }
		    });

		    if( selectedValue != undefined && "" !== selectedValue) {
		    	var arr = $.parseJSON(selectedValue);
		    	console.log(arr);
		    	var selectedCode = [];
		    	for(var i=0; i<arr.length; i++){
		    		var $newOption = $("<option></option>").val(arr[i].value).text(arr[i].text);
					$ts.append($newOption);
					selectedCode.push(arr[i].value);
		    	}
		    	$ts.val(selectedCode).trigger("change");

		    } else {

					$ts.val([""]).trigger("change");
		    }
		}
	</script>
</body>
</html>
