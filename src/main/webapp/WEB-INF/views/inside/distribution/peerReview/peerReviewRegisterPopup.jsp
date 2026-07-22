<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
  .peerReviewRegisterPopup .popupFormGrid {
    display: grid !important;
    grid-template-columns: 1fr 1fr;
    gap: 12px 24px;
  }

  .peerReviewRegisterPopup .popupFormGrid > li.half { width: 100% !important; }
  .peerReviewRegisterPopup .popupFormGrid > li.full { grid-column: 1 / -1; width: 100% !important; }
  .peerReviewRegisterPopup .popupFormGrid > li > label { white-space: nowrap; }

  .peerReviewRegisterPopup .popupFormGrid input[type="text"],
  .peerReviewRegisterPopup .popupFormGrid select { width: 100%; box-sizing: border-box; }

  .peerReviewRegisterPopup .popupFormGrid .select2-container { width: 100% !important; }
  .peerReviewRegisterPopup .popupFormGrid .select2-selection--multiple {
    min-height: 56px;
    padding: 6px 10px;
    border-radius: 12px;
    position: relative;
    padding-right: 30px !important;
  }
  .peerReviewRegisterPopup .popupFormGrid .select2-selection--multiple::after {
    content: "";
    position: absolute;
    right: 12px;
    top: 50%;
    width: 0;
    height: 0;
    margin-top: -2px;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 6px solid #8a8d93;
    pointer-events: none;
  }
  .peerReviewRegisterPopup .popupFormGrid #approver + .select2-container.people-empty .select2-selection--multiple::before,
  .peerReviewRegisterPopup .popupFormGrid #revieweruser + .select2-container.people-empty .select2-selection--multiple::before {
    content: "선택";
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #9aa0ad;
    font-size: 15px;
    pointer-events: none;
  }

  .peerReviewRegisterPopup .select2-container--open .select2-dropdown { margin-top: 2px; }
  .ui-dialog .select2-container--open { z-index: 3000; }

  .peerReviewRegisterPopup .uploadLine {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 8px;
    border: 1px dashed #cfd5e4;
    border-radius: 10px;
    min-height: 96px;
    padding: 18px;
    background: #f8fafc;
    cursor: pointer;
    transition: border-color 0.16s ease, background-color 0.16s ease;
  }
  .peerReviewRegisterPopup .uploadLine input[type="text"] { flex: 1 1 auto; }
  .peerReviewRegisterPopup .uploadLine .fileUploadBtn {
    flex: 0 0 auto;
    width: 34px;
    min-width: 34px;
    height: 34px;
    align-self: center;
  }
  .peerReviewRegisterPopup .uploadLine.is-dragover {
    border-color: #2f6fed;
    background-color: #f4f8ff;
  }
  .peerReviewRegisterPopup .fileNameWrap {
    position: relative;
    flex: 1 1 auto;
    min-width: 0;
  }
  .peerReviewRegisterPopup .fileClearBtn {
    position: absolute;
    top: 50%;
    right: 6px;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    min-width: 20px;
    padding: 0;
    font-size: 16px;
    line-height: 1;
    color: #6b7280;
    border: 0;
    background: transparent;
    cursor: pointer;
  }

  .peerReviewRegisterPopup .dialogBtnSet {
    display: flex !important;
    justify-content: center !important;
    align-items: center;
    gap: 12px;
  }
  .peerReviewRegisterPopup .dialogBtnSet .left { display: none !important; }
  .peerReviewRegisterPopup .dialogBtnSet .right {
    width: auto !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center;
    gap: 12px;
    margin: 0 auto;
  }
  .peerReviewRegisterPopup .dialogBtnSet .right > * {
    float: none !important;
    margin: 0 !important;
  }
</style>

<script>
  function getPeerReviewPopupRoot() {
    var $root = $('.peerReviewRegisterPopup:visible').last();
    if ($root.length) return $root;
    $root = $('#popupDialog .peerReviewRegisterPopup').last();
    if ($root.length) return $root;
    return $('.peerReviewRegisterPopup').last();
  }

  function initPeerReviewSelect2() {
    var $root = getPeerReviewPopupRoot();
    var $dialogContent = $root.closest('.ui-dialog-content');
    var $dropdownParent = $dialogContent.length ? $dialogContent : $root;
    var EMPTY_VALUE = '__NONE__';

    $root.find('#approver, #revieweruser').each(function () {
      var $select = $(this);
      if ($select.data('select2')) {
        $select.select2('destroy');
      }
      $select.select2({
        dropdownParent: $dropdownParent,
        width: '100%',
        closeOnSelect: false,
        placeholder: '선택'
      });

      // Keep placeholder visible by removing dummy selected value.
      var current = $select.val() || [];
      if (!Array.isArray(current)) {
        current = [current];
      }
      var filtered = current.filter(function (v) { return v && v !== EMPTY_VALUE; });
      $select.val(filtered).trigger('change.select2');

      $select.off('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder');
      $select.on('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder', function () {
        var $current = $(this);
        var selected = $current.val() || [];
        if (!Array.isArray(selected)) selected = [selected];
        var nonEmpty = selected.filter(function (v) { return $.trim(v || '') !== '' && v !== EMPTY_VALUE; });
        var hasSelected = nonEmpty.length > 0;
        var $container = $current.next('.select2');
        $container.toggleClass('people-empty', !hasSelected);
        $container.find('.select2-search__field').attr('placeholder', hasSelected ? '' : ($current.data('placeholder') || '선택'));
      });
      $select.trigger('change.multiPlaceholder');
    });
  }

  function closePeerReviewRegisterPopup() {
    var $content = getPeerReviewPopupRoot().closest('.ui-dialog-content');
    if ($content.length && typeof $content.dialog === 'function') {
      $content.dialog('close');
      return;
    }
    if ($('#popupDialog').length && typeof $('#popupDialog').dialog === 'function') {
      $('#popupDialog').dialog('close');
    }
  }

  function openPeerReviewFileSelect() {
    $('#peerReviewRegisFile').trigger('click');
  }

  function clearPeerReviewSelectedFile() {
    $('#peerReviewRegisFile').val('');
    $('#peerReviewFileName').val('');
  }

  function bindPeerReviewFileDragDrop() {
    var $fileInput = $('#peerReviewRegisFile');
    var $zone = $fileInput.closest('form').next('.uploadLine');
    if (!$zone.length) return;

    var dragDepth = 0;
    $zone.off('.peerDnD');
    $zone.on('dragenter.peerDnD', function (e) {
      e.preventDefault();
      e.stopPropagation();
      dragDepth += 1;
      $zone.addClass('is-dragover');
    });
    $zone.on('dragover.peerDnD', function (e) {
      e.preventDefault();
      e.stopPropagation();
    });
    $zone.on('dragleave.peerDnD', function (e) {
      e.preventDefault();
      e.stopPropagation();
      dragDepth = Math.max(0, dragDepth - 1);
      if (dragDepth === 0) $zone.removeClass('is-dragover');
    });
    $zone.on('drop.peerDnD', function (e) {
      e.preventDefault();
      e.stopPropagation();
      dragDepth = 0;
      $zone.removeClass('is-dragover');
      var files = (e.originalEvent && e.originalEvent.dataTransfer) ? e.originalEvent.dataTransfer.files : null;
      if (!files || !files.length || !window.DataTransfer) return;
      var dt = new DataTransfer();
      dt.items.add(files[0]);
      $fileInput[0].files = dt.files;
      $fileInput.trigger('change');
    });
  }

  function savePeerReviewRegister() {
    var peerreviewNo = $.trim($('#peerreviewNo').val() || '');
    var peerreviewNm = $.trim($('#peerreviewNm').val() || '');
    var reviewdate = $.trim($('#reviewdate').val() || '');
    var revNo = $.trim($('#revNo').val() || '00');
    var fileInput = $('#peerReviewRegisFile')[0];
    var file = fileInput && fileInput.files && fileInput.files.length ? fileInput.files[0] : null;

    if (peerreviewNo === '') {
      alertMessage('PeerReview 번호를 입력하세요.');
      return;
    }
    if (peerreviewNm === '') {
      alertMessage('PeerReview 제목을 입력하세요.');
      return;
    }
    if (!file) {
      alertMessage('주파일을 선택하세요.');
      return;
    }

    var formData = new FormData();
    formData.append('file', file);
    formData.append('peerreviewNo', peerreviewNo);
    formData.append('peerreviewNm', peerreviewNm);
    formData.append('reviewdate', reviewdate);
    formData.append('revNo', revNo);
    formData.append('approver', ($('#approver').val() || []).join(','));
    formData.append('revieweruser', ($('#revieweruser').val() || []).join(','));
    formData.append('registerUser', $.trim($('#registerUser').val() || ''));
    formData.append('insertUserNm', $.trim($('#registerUser').val() || ''));
    formData.append('status', '승인진행중');

    alertMessage(g_msg('msg.registerComplete'));
    try {
      if (parent && typeof parent.setTimeout === "function") {
        parent.setTimeout(function () {
          try {
            if (typeof parent.searchList === "function") {
              parent.searchList(parent.gridParam);
            }
          } catch (e) {}
        }, 1200);
        parent.setTimeout(function () {
          try {
            if (typeof parent.searchList === "function") {
              parent.searchList(parent.gridParam);
            }
          } catch (e) {}
        }, 2800);
      }
    } catch (e) {}
    closePeerReviewRegisterPopup();

    $.ajax({
      url: '/inside/distribution/peerReview/uploadPeerReviewRegisFile',
      type: 'POST',
      global: false,
      data: formData,
      processData: false,
      contentType: false,
      success: function (response) {
        if (response && response.success) {
          return;
        }
        if (response && response.message) {
          console.log('[PEERREVIEW_REGISTER] failed: ' + response.message);
        }
      },
      error: function () {
        console.log('[PEERREVIEW_REGISTER] request error');
      }
    });
  }

  $(function () {
    initPeerReviewSelect2();
    callAjax({}, '/inside/distribution/peerReview/nextPeerReviewNo', function (response) {
      if (response && response.peerreviewNo) {
        $('#peerreviewNo').val(response.peerreviewNo);
      }
    }, 'json');
    $('#peerReviewRegisFile').on('change', function () {
      var name = this.files && this.files.length ? this.files[0].name : '';
      $('#peerReviewFileName').val(name);
    });
    bindPeerReviewFileDragDrop();
  });
</script>

<div class="dialogContent peerReviewRegisterPopup popup-base popup-actions-center">
  <div class="popupHero">
    <h2>Peer Review 등록 정보</h2>
    <p>필수 항목을 입력한 뒤 등록해 주세요.</p>
  </div>

  <form id="formPeerReviewRegisterPopup">
    <ul class="section popupCard popupFormGrid">
      <li class="full">
        <label>등록자</label>
        <input type="text" id="registerUserView" value="${registerUser}" readonly>
      </li>
      <li class="half">
        <label>등록일</label>
        <input type="text" value="${date}" readonly>
      </li>
      <li class="half">
        <label>검토일</label>
        <input type="date" value="${date}" id="reviewdate" name="reviewdate">
      </li>
      <li class="full">
        <label>PeerReview 제목</label>
        <input type="text" id="peerreviewNm" name="peerreviewNm" value="">
      </li>
      <li class="half">
        <label>PeerReview 번호</label>
        <input type="text" id="peerreviewNo" name="peerreviewNo" value="" readonly>
      </li>
      <!-- <li class="half">
        <label>REV</label>
        <input type="text" id="revNo" name="revNo" value="00">
      </li> -->
      <li class="half">
        <label>검토자</label>
        <select id="approver" name="approver" multiple="multiple" data-placeholder="선택">
          <option value="__NONE__" selected>선택</option>
          <c:forEach var="user" items="${coPublisherUsers}">
            <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
          </c:forEach>
        </select>
      </li>
      <!-- <li class="half">
        <label>참여자</label>
        <select id="revieweruser" name="revieweruser" multiple="multiple" data-placeholder="선택">
          <c:forEach var="user" items="${coPublisherUsers}">
            <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
          </c:forEach>
        </select>
      </li> -->
      <input type="hidden" id="registerUser" name="registerUser" value="${registerUser}">
    </ul>
  </form>

  <ul class="section popupCard popupFormGrid uploadOnly">
    <li class="full">
      <label>주파일 등록</label>
      <form id="fileForm" name="fileForm" enctype="multipart/form-data">
        <input type="file" id="peerReviewRegisFile" name="peerReviewRegisFile" style="display:none;">
      </form>
      <div class="uploadLine" title="파일을 드래그하거나 첨부 버튼을 클릭하세요.">
        <div class="fileNameWrap">
          <input type="text" id="peerReviewFileName" placeholder="첨부 버튼 클릭 또는 드래그앤드롭" readonly>
          <button type="button" class="fileClearBtn" title="첨부 파일 초기화" onclick="clearPeerReviewSelectedFile()">×</button>
        </div>
        <button type="button" class="ui-button ui-corner-all fileUploadBtn" onclick="openPeerReviewFileSelect()"></button>
      </div>
    </li>
  </ul>

  <div class="dialogBtnSet">
    <div class="left"></div>
    <div class="right">
      <custom:popupButton function="savePeerReviewRegister()" name="save" label="btn.register" id="save" />
      <custom:popupButton function="closePeerReviewRegisterPopup()" name="close" label="btn.close" id="close" />
    </div>
  </div>
</div>
