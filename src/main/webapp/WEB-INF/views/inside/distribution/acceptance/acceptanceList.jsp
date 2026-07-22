<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/pages/distribution-invoice.css" media="screen" />
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/views/inside/distribution/acceptance/common-form-vuexy.js"></script>

<script>
	window.USE_ACCEPTANCE_VUEXY_FORM = true;

	function setGridParam(){
		gridParam = {
				gridId : 'gridList',
				formId : 'formAcceptance',
				url : '/inside/distribution/acceptance/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : false,
				numbering : false,
				layoutMode : 'invoice',
				fillColumns : true
		}

		return gridParam;
	}

	function formatRequestNo(cellValue, options, rowdata, action){
 		/* if("REQUEST" == rowdata["statusCd"]){
 			return '<a href="javascript: viewProductDetail(\'' + cellValue + ')">' + cellValue + '</button>';
			} */
			return '<a href="javascript: viewDetail(\'' + cellValue + '\', \'' + rowdata['objectType'] +'\', \''+rowdata["statusCd"]+'\', \''+rowdata["currentProcessSeqNo"]+'\', \'' + rowdata["businessTypeCd"] + '\')">' + cellValue + '</button>';
// 		}else{
// 			return cellValue;
// 		}
	}

	function viewDetail(requestNo, objectType, statusCd, currentProcessSeqNo, businessTypeCd) {
		var popupHeight = Math.min($(window).height() - 80, 760);
		openDialogPopup("/inside/distribution/acceptance/approvePopup",{requestNo: requestNo, objectType : objectType, statusCd : statusCd, currentProcessSeqNo : currentProcessSeqNo, businessTypeCd : businessTypeCd}, "popupDialog", 'xl', popupHeight, true, 'popup-common popup-distribution-approval');
	}

	//생산기술자료 접수 팝업
	function viewProductDetail(requestNo) {
		openDialogPopup("/inside/distribution/acceptance/productAcceptPopup",{requestNo: '20191104-P0002'}, "popupDialog", 'xl', 710);
	}

	$(function() {
		$('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
	});

</script>
</head>
<body>
	<div class="distribution-invoice-page">
		<custom:listTemplateInvoice gridId="gridList"/>
	</div>
</body>
</html>
