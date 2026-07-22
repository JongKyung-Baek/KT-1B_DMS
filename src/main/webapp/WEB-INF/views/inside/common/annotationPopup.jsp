<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script>
  var popupGridParam;
  $(function() {
    settingGrid('${gridInfo }', setPopupGridParam(), 'popupGridParam');
  });


  function setPopupGridParam(){
    popupGridParam = {
      formId : 'formAnnotationPopup',
      gridId : 'gridAnnotationPopupList',
      url : '/inside/distribution/annotationinfo/selectAnnotationPopupList',
      size : 4,
      page : 1,
      multiSelect : false,
      numbering : false
    }
    return popupGridParam;
  }




</script>


<form id="formAnnotationPopup" name="formAnnotationPopup" value="주석">
  <input type="hidden" id="objectId" name="objectId" value='${objectId}'/>
  <input type="hidden" id="objectType" name="objectType" value='${objectType}'/>
</form>

<div class="dialogContent annotationPopup commonRequestPopup popup-base popup-actions-center popup-type-form-grid">
  <div class="popupHero">
    <h2>주석 정보</h2>
  </div>

  <div class="section popupCard">
    <div class="gridContainer">
      <table id="gridAnnotationPopupList">
      </table>


    </div>
  </div>
</div>


<div class="dialogBtnSet">
  <div class="left"></div>
  <div class="right">
    <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
  </div>
</div>
<div id="detailPopupDialog" class="dialogContainer"></div>
