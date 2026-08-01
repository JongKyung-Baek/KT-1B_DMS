<%@page import="org.springframework.web.servlet.i18n.SessionLocaleResolver" %>

	<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
		<%@page import="kr.esob.tdms.commonlogic.message.LocaleUtil" %>
		<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
			<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

				<!doctype html>
				<html lang="<%=LocaleUtil.getCurrentLanguage(request) %>">


				<head>
					<meta charset="UTF-8">
					<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
					<meta name="viewport" content="width=device-width, initial-scale=1">
					<meta http-equiv="X-UA-Compatible" content="IE=edge" />
					<%@ include file="/WEB-INF/jspf/csrf-meta.jspf" %>
					<%@ include file="/WEB-INF/jspf/favicon.jspf" %>

					<script type="text/javascript" src="/resources/js/common_util.js"></script>
					<script type="text/javascript" src="/resources/js/common_dialog.js"></script>
						<link type="text/css" rel="stylesheet" href="/resources/css/popup-common.css" media="screen" />
					<script type="text/javascript" src="/resources/js/common_validation.js"></script>
					<script type="text/javascript" src="/resources/js/views/general/common/clipboard.js"></script>
					<script>
						console.log("empty");
					</script>
					<sitemesh:write property='head' />
				</head>

				<body>
					<div id="alertMessage"></div>
					<sitemesh:write property='body' />
				</body>

				</html>

