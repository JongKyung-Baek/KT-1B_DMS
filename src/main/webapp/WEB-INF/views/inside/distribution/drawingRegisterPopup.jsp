<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
      <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

        <script type="text/javascript"
          src="/resources/js/views/inside/distribution/drawingRequest/drawingRegisterPopup.js"></script>

        <style>
          .drawingRegisterPopup .popupFormGrid>li.half {
            width: calc(50% - 12px);
          }

          .drawingRegisterPopup .popupFormGrid>li.full {
            width: 100%;
          }

          .drawingRegisterPopup .popupFormGrid>li>label {
            white-space: nowrap;
          }

          .drawingRegisterPopup .popupFormGrid input[type="text"],
          .drawingRegisterPopup .popupFormGrid select {
            width: 100%;
            box-sizing: border-box;
          }

          .drawingRegisterPopup .popupFormGrid .select2-container {
            width: 100% !important;
          }

          .drawingRegisterPopup .select2-container--open .select2-dropdown {
            margin-top: 2px;
          }

          .ui-dialog .select2-container--open {
            z-index: 3000;
          }

          .drawingRegisterPopup .popupFormGrid #coPublisher+.select2-container .select2-selection--multiple,
          .drawingRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection--multiple,
          .drawingRegisterPopup .popupFormGrid #approver+.select2-container .select2-selection--multiple {
            position: relative;
            padding-right: 28px !important;
          }

          .drawingRegisterPopup .popupFormGrid #coPublisher+.select2-container.people-empty .select2-selection--multiple::before,
          .drawingRegisterPopup .popupFormGrid #reviewerUser+.select2-container.people-empty .select2-selection--multiple::before,
          .drawingRegisterPopup .popupFormGrid #approver+.select2-container.people-empty .select2-selection--multiple::before {
            content: "선택";
            position: absolute;
            left: 12px;
            top: 50%;
            transform: translateY(-50%);
            color: #9aa0ad;
            font-size: 15px;
            pointer-events: none;
          }

          .drawingRegisterPopup .popupFormGrid #coPublisher+.select2-container .select2-selection--multiple::after,
          .drawingRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection--multiple::after,
          .drawingRegisterPopup .popupFormGrid #approver+.select2-container .select2-selection--multiple::after {
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

          .drawingRegisterPopup .popupFormGrid #coPublisher+.select2-container .select2-selection__choice[title="선택"],
          .drawingRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection__choice[title="선택"],
          .drawingRegisterPopup .popupFormGrid #approver+.select2-container .select2-selection__choice[title="선택"] {
            display: none !important;
          }

          .drawingRegisterPopup .inlineCheckGroup {
            display: flex;
            align-items: center;
            column-gap: 18px;
            row-gap: 8px;
            min-height: 32px;
            flex-wrap: wrap;
          }

          .drawingRegisterPopup .inlineCheckGroup label {
            margin: 0;
            display: inline-flex;
            align-items: center;
            column-gap: 8px;
            line-height: 1.2;
            white-space: nowrap;
          }

          .drawingRegisterPopup .inlineCheckGroup.is-locked {
            pointer-events: none;
            opacity: 0.7;
          }

          .drawingRegisterPopup .uploadLine {
            display: flex;
            align-items: center;
            gap: 5px;
            border: 1px dashed #cfd5e4;
            border-radius: 10px;
            min-height: 96px;
            padding: 18px;
            background: #f8fafc;
            cursor: pointer;
            transition: border-color 0.16s ease, background-color 0.16s ease;
          }

          .drawingRegisterPopup .uploadLine input[type="text"] {
            width: 100% !important;
            padding-right: 32px;
          }

          .drawingRegisterPopup .uploadLine.is-dragover {
            border-color: #2f6fed;
            background-color: #f4f8ff;
          }

          .drawingRegisterPopup .fileNameWrap {
            position: relative;
            flex: 1 1 auto;
            min-width: 0;
          }

          .drawingRegisterPopup .fileClearBtn {
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

          .drawingRegisterPopup .subFileNames {
            width: 100%;
            box-sizing: border-box;
          }
        </style>


        <!-- 등록 및 배포요청 팝업(미등록 자료등록 버튼) -->
        <script>
          var DRAWING_MULTI_EMPTY_VALUE = '__NONE__';

          function getDrawingPopupRoot() {
            var $root = $('.drawingRegisterPopup:visible').last();
            if ($root.length) return $root;
            $root = $('#popupDialog .drawingRegisterPopup').last();
            if ($root.length) return $root;
            return $('.drawingRegisterPopup').last();
          }

          function $popupField(selector) {
            var $root = getDrawingPopupRoot();
            if ($root.length) return $root.find(selector).first();
            return $(selector).first();
          }

          function getDrawingPopupDialog() {
            var $root = getDrawingPopupRoot();
            var $dialog = $root.closest('.ui-dialog');
            if ($dialog.length) return $dialog;
            if ($('#popupDialog').length) return $('#popupDialog');
            return $(document.body);
          }

          function isDrawingPeopleMultiSelect($select) {
            if (!$select || !$select.length) return false;
            var id = $select.attr('id');
            return id === 'coPublisher' || id === 'reviewerUser' || id === 'approver';
          }

          function bindSelect2ValueSync(selector) {
            var $el = $popupField(selector);
            if (!$el.length) return;

            $el.off('select2:select.valueSync');
            $el.on('select2:select.valueSync', function (e) {
              var selectedId = e && e.params && e.params.data ? e.params.data.id : $(this).val();
              if (selectedId === undefined || selectedId === null) return;

              // Keep all selected values for people multi-selects.
              if (isDrawingPeopleMultiSelect($(this))) {
                var current = $(this).val();
                if (!$.isArray(current)) current = current ? [String(current)] : [];
                var normalized = String(selectedId);
                if (normalized !== DRAWING_MULTI_EMPTY_VALUE && current.indexOf(normalized) === -1) {
                  current.push(normalized);
                }
                current = $.map(current, function (item) {
                  item = $.trim(item || '');
                  return item && item !== DRAWING_MULTI_EMPTY_VALUE ? item : null;
                });
                $(this).val(current).trigger('change');
              } else {
                $(this).val(String(selectedId)).trigger('change');
              }
              syncSelect2DisplayedText($(this));
            });
          }

          function syncSelect2DisplayedText($select) {
            if (!$select || !$select.length) return;
            var text = $.trim($select.find('option:selected').text() || '');
            if (!text) text = '선택';
            var $render = $select.next('.select2').find('.select2-selection__rendered').first();
            if ($render.length) {
              $render.text(text);
              $render.attr('title', text);
            }
          }

          function destroyDrawingPopupSelect2() {
            var $root = getDrawingPopupRoot();
            if (!$root.length) return;

            $root.find('form#formDrawingRegisterPopup select').each(function () {
              var $select = $(this);
              if ($select.data('select2')) {
                $select.select2('destroy');
              }
            });
          }

          function initDrawingPopupSelect2() {
            var $form = $popupField('form#formDrawingRegisterPopup');
            var $dialog = getDrawingPopupDialog();

            if (!$form.length) return;

            $form.find('select').each(function () {
              var $select = $(this);
              var isMultiple = $select.prop('multiple');

              if ($select.data('select2')) {
                $select.select2('destroy');
              }

              var option = {
                dropdownParent: $dialog,
                width: '100%'
              };

              if (isMultiple) {
                option.closeOnSelect = false;
                option.placeholder = $select.data('placeholder') || '선택';
              } else {
                option.minimumResultsForSearch = -1;
              }

              $select.select2(option);

              if (isMultiple && isDrawingPeopleMultiSelect($select)) {
                $select.off('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder');
                $select.on('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder', function () {
                  var $current = $(this);
                  var selected = $current.val() || [];
                  if (!Array.isArray(selected)) selected = [selected];
                  var nonEmpty = selected.filter(function (v) {
                    return $.trim(v || '') !== '' && v !== DRAWING_MULTI_EMPTY_VALUE;
                  });
                  var $container = $current.next('.select2');
                  var hasSelected = nonEmpty.length > 0;
                  $container.toggleClass('people-empty', !hasSelected);
                  $container.find('.select2-search__field').attr('placeholder', hasSelected ? '' : ($current.data('placeholder') || '선택'));
                });

                $select.off('change.multiEmptyOption');
                $select.on('change.multiEmptyOption', function () {
                  var $current = $(this);
                  if ($current.data('syncingEmptyOption')) return;

                  var selected = $current.val() || [];
                  if (!Array.isArray(selected)) selected = [selected];
                  var hasEmpty = selected.indexOf(DRAWING_MULTI_EMPTY_VALUE) !== -1;
                  var nonEmpty = selected.filter(function (v) {
                    return $.trim(v || '') !== '' && v !== DRAWING_MULTI_EMPTY_VALUE;
                  });

                  if (hasEmpty && nonEmpty.length > 0) {
                    $current.data('syncingEmptyOption', true);
                    $current.val(nonEmpty).trigger('change.select2');
                    $current.data('syncingEmptyOption', false);
                    return;
                  }

                  if (!hasEmpty && nonEmpty.length === 0) {
                    $current.data('syncingEmptyOption', true);
                    $current.val([DRAWING_MULTI_EMPTY_VALUE]).trigger('change.select2');
                    $current.data('syncingEmptyOption', false);
                  }
                });

                var currentValues = $select.val() || [];
                if (!Array.isArray(currentValues) || currentValues.length === 0) {
                  $select.val([DRAWING_MULTI_EMPTY_VALUE]).trigger('change.select2');
                }
                $select.trigger('change.multiPlaceholder');
              }
            });

            bindSelect2ValueSync('#functionCode1');
            bindSelect2ValueSync('#functionCode2');
            bindSelect2ValueSync('#documentTypeCode');
          }

          function getPopupSelectValue(selector) {
            var value = $popupField(selector).val();
            if ($.isArray(value)) {
              return $.map(value, function (item) {
                return $.trim(item || '');
              }).filter(function (item) {
                return item.length > 0 && item !== DRAWING_MULTI_EMPTY_VALUE;
              }).join(',');
            }
            if (value === DRAWING_MULTI_EMPTY_VALUE) return '';
            return $.trim(value || '');
          }

          function isValidationX() {
            if ($.trim($popupField("#approvalDate").val()) === "") {
              alertMessage("발행일을 확인하세요.");
              return false;
            }

            if (getPopupSelectValue("#coPublisher") === "") {
              alertMessage("공동 발행자를 선택하세요.");
              return false;
            }

            if ($.trim($popupField("#dataName").val()) === "") {
              //isValidDataEmpty("dataName", "form.dataName");
              alertMessage('문서제목을 입력하세요.');
              return false;
            }

            if ($.trim($popupField("#functionCode1").val()) === "") {
              alertMessage("Function Code1을 선택하세요.");
              return false;
            }

            if ($.trim($popupField("#functionCode2").val()) === "") {
              alertMessage("Function Code2를 선택하세요.");
              return false;
            }

            if ($.trim($popupField("#documentTypeCode").val()) === "") {
              alertMessage("Document Type Code를 선택하세요.");
              return false;
            }

            if ($.trim($popupField("#registerNo").val()) === "") {
              alertMessage("등록번호를 입력하세요.");
              return false;
            }

            if ($.trim($popupField("#drawingNo").val()) === "") {
              alertMessage("문서번호를 확인하세요.");
              return false;
            }

            if ($popupField('input[name="versionType"]:checked').length === 0) {
              alertMessage("버전을 선택하세요.");
              return false;
            }

            if ($.trim($popupField("#versionNo").val()) === "") {
              alertMessage("버전 No를 확인하세요.");
              return false;
            }

            if (getPopupSelectValue("#reviewerUser") === "") {
              alertMessage("검토자를 선택하세요.");
              return false;
            }

            if (getPopupSelectValue("#approver") === "") {
              alertMessage("승인자를 선택하세요.");
              return false;
            }
            /* 04-01 */
            /* if($.trim($("#modelCode").val()) === ""){
              alertMessage('기종을 입력하세요');
              return false;
            } */
            // if($.trim($("#customerRevision").val()) === ""){
            //   alertMessage('고객 리비전을 입력하세요.');
            //   return false;
            // }
            // var revision = $.trim($("#customerRevision").val());
            // if (!/^[A-Z]$/.test(revision)) {
            //   alertMessage('고객 리비전 형식은 대문자 알파벳 한 글자 입니다.');
            //   return false;
            // }

            var fileInput = $popupField("#drawingRegisFile")[0];
            var file = fileInput ? fileInput.files[0] : null;
            if (!file) {
              alertMessage("주파일을 선택하세요.");
              return false;
            }

            return true;
          }

          var isVersionTypeLocked = false;
          var nextVersionRequestSeq = 0;

          function updateDrawingVersionNo() {
            requestNextDrawingVersionNo();
          }

          function getVersionPrefix(versionType) {
            if (versionType === 'Draft') return 'D';
            if (versionType === 'Final') return 'F';
            return 'P';
          }

          function setVersionTypeLocked(locked) {
            isVersionTypeLocked = !!locked;
            var $radios = getDrawingPopupRoot().find('input[name="versionType"]');
            $radios.prop('disabled', isVersionTypeLocked);
            getDrawingPopupRoot().find('.versionTypeGroup').toggleClass('is-locked', isVersionTypeLocked);
          }

          function syncVersionTypeByVersionNo(versionNo) {
            if (!versionNo) return;
            var type = 'Preliminary';
            if (String(versionNo).indexOf('D') === 0) {
              type = 'Draft';
            } else if (String(versionNo).indexOf('F') === 0) {
              type = 'Final';
            }
            getDrawingPopupRoot().find('input[name="versionType"][value="' + type + '"]').prop('checked', true);
          }

          function requestNextDrawingVersionNo() {
            var drawingNo = $.trim($popupField('#drawingNo').val() || '');
            var versionType = getDrawingPopupRoot().find('input[name="versionType"]:checked').val() || 'Preliminary';
            var prefix = getVersionPrefix(versionType);
            var requestSeq = ++nextVersionRequestSeq;

            if (!drawingNo) {
              setVersionTypeLocked(false);
              $popupField('#versionNo').val(prefix + '00');
              return;
            }

            callAjax(
              { drawingNo: drawingNo, versionType: versionType },
              "/inside/distribution/drawingRequest/nextVersionNo",
              function (response) {
                if (requestSeq !== nextVersionRequestSeq) {
                  return;
                }
                var versionNo = response && response.versionNo ? response.versionNo : (prefix + '00');
                var hasPreviousRevision = !!(response && response.hasPreviousRevision);
                syncVersionTypeByVersionNo(versionNo);
                setVersionTypeLocked(hasPreviousRevision);
                $popupField('#versionNo').val(versionNo);
              }
            );
          }


          function saveX() {
            if (!isValidationX()) {
              return;
            }

            var dataText = $popupField('#dataName').val();
            var orgFileNm = $popupField('#fileName').val();
            var businessType = $popupField("#businessTypeCd").length ? $popupField("#businessTypeCd").val() : "";
            var distributeType = $popupField("#distributeTypeCd").length ? $popupField("#distributeTypeCd").val() : "";
            var fileType = $.trim($popupField("#documentTypeCode option:selected").text() || "");
            var approvalDate = $popupField("#approvalDate").val();
            var coPublisher = getPopupSelectValue("#coPublisher");
            var reviewerUser = getPopupSelectValue("#reviewerUser");
            var approver = getPopupSelectValue("#approver");
            var totalPageNo = $popupField("#totalPageNo").length ? $popupField("#totalPageNo").val() : "1";
            /* 04-01 */
            /* var modelCode = document.getElementById("modelCode").value; */
            // var customerRevision = document.getElementById("customerRevision").value;

            let protectYn = document.querySelector('input[name="protectYn"]:checked') ? document.querySelector('input[name="protectYn"]:checked').value : "N";
            let drawingType = document.querySelector('input[name="drawingType"]:checked') ? document.querySelector('input[name="drawingType"]:checked').value : "2D";
            var drawingNo = $popupField("#drawingNo").val();
            var treeCd = $popupField("#treeCd").val();
            var registerUser = $popupField("#registerUser").val();

            var fileInput = $popupField("#drawingRegisFile")[0];
            var subFileInput = $popupField("#drawingSubFiles")[0];


            var file = fileInput.files[0];
            var formData = new FormData();

            formData.append("totalPageNo", totalPageNo);
            formData.append("file", file);
            formData.append("fileName", dataText);
            formData.append("orgFileNm", orgFileNm);
            formData.append("objectType", "DRAWING");
            formData.append("businessTypeCd", businessType);
            formData.append("distributeTypeCd", distributeType);
            formData.append("fileType", fileType);
            formData.append("approvalDate", approvalDate);
            formData.append("coPublisher", coPublisher);
            formData.append("reviewerUser", reviewerUser);
            formData.append("approver", approver);
            formData.append("protectYn", protectYn);
            formData.append("drawingType", drawingType);
            formData.append("drawingNo", drawingNo);
            formData.append("treeCd", treeCd);
            formData.append("registerUser", registerUser);
            formData.append("insertUserNm", registerUser);
            formData.append("versionType", getDrawingPopupRoot().find('input[name="versionType"]:checked').val() || "");
            formData.append("versionNo", $popupField("#versionNo").val() || "");
            if (subFileInput && subFileInput.files && subFileInput.files.length > 0) {
              Array.prototype.forEach.call(subFileInput.files, function (subFile) {
                formData.append("subFiles", subFile);
              });
            }
            /* 04-01 */
            /* formData.append("modelCode", modelCode); */
            // formData.append("customerRevision", customerRevision);
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
            closeDrawingRegisterPopup();
            callAjaxUpload(formData, "/inside/distribution/drawingRequest/uploadDrawingRegisFile", requestCrXCallback);
          }

          function closeDrawingRegisterPopup() {
            destroyDrawingPopupSelect2();
            closePopup('popupDialog');
          }

          function requestCrXCallback(response) {
            if (response.success) {
            } else {
              alertMessage(g_msg("msg.registerFailure"));						//등록에 실패했습니다.
            }
          }

          function clearDrawingSelectedFile() {
            var $fileInput = $popupField('#drawingRegisFile');
            var $fileName = $popupField('#fileName');
            if ($fileInput.length) {
              $fileInput.val('');
              if ($fileInput[0]) {
                $fileInput[0].value = '';
              }
            }
            $fileName.val('');
          }

          function clearDrawingSubSelectedFiles() {
            var $subFileInput = $popupField('#drawingSubFiles');
            var $subFileNames = $popupField('#subFileNames');
            if ($subFileInput.length) {
              $subFileInput.val('');
              if ($subFileInput[0]) {
                $subFileInput[0].value = '';
              }
            }
            $subFileNames.val('');
          }

          function bindUploadLineDragDrop(inputSelector, isMultiple) {
            var $fileInput = $popupField(inputSelector);
            if (!$fileInput.length) return;

            var $zone = $fileInput.closest('form').next('.uploadLine');
            if (!$zone.length) return;

            var dragDepth = 0;
            $zone.off('.drawingDnD');

            $zone.on('dragenter.drawingDnD', function (e) {
              e.preventDefault();
              e.stopPropagation();
              dragDepth += 1;
              $zone.addClass('is-dragover');
            });

            $zone.on('dragover.drawingDnD', function (e) {
              e.preventDefault();
              e.stopPropagation();
            });

            $zone.on('dragleave.drawingDnD', function (e) {
              e.preventDefault();
              e.stopPropagation();
              dragDepth = Math.max(0, dragDepth - 1);
              if (dragDepth === 0) {
                $zone.removeClass('is-dragover');
              }
            });

            $zone.on('drop.drawingDnD', function (e) {
              e.preventDefault();
              e.stopPropagation();
              dragDepth = 0;
              $zone.removeClass('is-dragover');

              var event = e.originalEvent || e;
              var files = event.dataTransfer && event.dataTransfer.files ? event.dataTransfer.files : null;
              if (!files || !files.length) return;

              var input = $fileInput[0];
              if (!window.DataTransfer || !input) return;

              var dt = new DataTransfer();
              if (isMultiple) {
                for (var i = 0; i < files.length; i++) {
                  dt.items.add(files[i]);
                }
              } else {
                dt.items.add(files[0]);
              }
              input.files = dt.files;
              $fileInput.trigger('change');
            });
          }

          function bindDrawingFileDragDrop() {
            bindUploadLineDragDrop('#drawingRegisFile', false);
            bindUploadLineDragDrop('#drawingSubFiles', true);
          }

          $(document).ready(function () {
            $popupField('#drawingRegisFile').off('change').on('change', function () {
              var fullFilename = $(this).val().split('\\').pop();
              var lastIndex = fullFilename.lastIndexOf(".");

              // If the filename has an extension, remove it, if not, keep it as is
              var filenameWithoutExtension = lastIndex >= 0 ? fullFilename.substring(0, lastIndex) : fullFilename;

              $popupField('#fileName').val(fullFilename); // Preserve extension

              // Check if the #dataName field is empty
              if ($popupField('#dataName').val() === '') {
                $popupField('#dataName').val(filenameWithoutExtension); // Remove extension
              }
            });

            $popupField('#drawingSubFiles').off('change').on('change', function () {
              var files = this.files || [];
              var names = [];
              for (var i = 0; i < files.length; i++) {
                names.push(files[i].name);
              }
              $popupField('#subFileNames').val(names.join(', '));
            });

            destroyDrawingPopupSelect2();
            initDrawingPopupSelect2();
            bindDrawingFileDragDrop();

            var $dialog = getDrawingPopupDialog();
            $dialog.off('dialogbeforeclose.drawingPopup').on('dialogbeforeclose.drawingPopup', function () {
              destroyDrawingPopupSelect2();
            });
            $dialog.off('dialogclose.drawingPopup').on('dialogclose.drawingPopup', function () {
              destroyDrawingPopupSelect2();
            });

            initDrawingRegisterTreeSelectors();
            $popupField('#registerNo').off('input').on('input', updateDrawingNoPreview);
            getDrawingPopupRoot().find('input[name="versionType"]').off('change').on('change', function (e) {
              if (isVersionTypeLocked) {
                e.preventDefault();
                return false;
              }
              updateDrawingVersionNo();
            });
            getDrawingPopupRoot().find('.versionTypeGroup').off('mousedown.versionLock click.versionLock').on('mousedown.versionLock click.versionLock', function (e) {
              if (!isVersionTypeLocked) {
                return;
              }
              e.preventDefault();
              e.stopPropagation();
              return false;
            });
            updateDrawingVersionNo();
          });

          function initDrawingRegisterTreeSelectors() {
            callAjax({}, "/inside/distribution/drawingRequest/selectTree", function (response) {
              var list = response || [];
              var childrenMap = {};
              var nodeMap = {};
              var normalizedNodes = [];

              $.each(list, function () {
                var node = $.extend({}, this);
                node.id = normalizeTreeKey(node.id);
                node.parent = normalizeTreeKey(node.parent || "#");
                node.levelNo = parseInt(node.level, 10);
                if (isNaN(node.levelNo)) {
                  node.levelNo = -1;
                }
                nodeMap[node.id] = node;
                normalizedNodes.push(node);
              });

              $.each(normalizedNodes, function () {
                var node = this;
                if (!node.parent || node.parent === "#" || !nodeMap[node.parent]) {
                  node.parent = "#";
                  return;
                }

                var parentNode = nodeMap[node.parent];
                while (parentNode && node.levelNo >= 0 && parentNode.levelNo >= 0 && node.levelNo <= parentNode.levelNo) {
                  node.parent = normalizeTreeKey(parentNode.parent || "#");
                  if (node.parent === "#" || !nodeMap[node.parent]) {
                    break;
                  }
                  parentNode = nodeMap[node.parent];
                }
              });

              $.each(normalizedNodes, function () {
                var parentId = normalizeTreeKey(this.parent || "#");
                if (!childrenMap[parentId]) {
                  childrenMap[parentId] = [];
                }
                childrenMap[parentId].push(this);
              });

              $.each(childrenMap, function (_, childList) {
                childList.sort(function (a, b) {
                  var sa = parseInt(a.sort, 10);
                  var sb = parseInt(b.sort, 10);
                  if (isNaN(sa)) sa = 999999;
                  if (isNaN(sb)) sb = 999999;
                  if (sa !== sb) return sa - sb;
                  return (a.text || "").localeCompare(b.text || "");
                });
              });

              $popupField('#functionCode1').off('change').on('change', function () {
                bindFunctionCode2(childrenMap, $(this).val(), true);
                bindDocumentType(childrenMap, $popupField('#functionCode2').val(), true);
                syncSelect2DisplayedText($popupField('#functionCode1'));
                syncSelect2DisplayedText($popupField('#functionCode2'));
                syncSelect2DisplayedText($popupField('#documentTypeCode'));
                updateDrawingNoPreview();
              });

              $popupField('#functionCode2').off('change').on('change', function () {
                bindDocumentType(childrenMap, $(this).val(), true);
                syncSelect2DisplayedText($popupField('#functionCode2'));
                syncSelect2DisplayedText($popupField('#documentTypeCode'));
                updateDrawingNoPreview();
              });

              $popupField('#documentTypeCode').off('change').on('change', function () {
                $popupField('#treeCd').val($(this).val() || "");
                syncSelect2DisplayedText($popupField('#documentTypeCode'));
                updateDrawingNoPreview();
              });

              bindFunctionCode1(childrenMap, false);
              bindFunctionCode2(childrenMap, $popupField('#functionCode1').val(), false);
              bindDocumentType(childrenMap, $popupField('#functionCode2').val(), false);
            });
          }

          function normalizeTreeKey(value) {
            if (value === undefined || value === null) return "";
            return String(value).trim();
          }

          function getChildren(childrenMap, parentId) {
            var key = normalizeTreeKey(parentId);
            return childrenMap[key] || [];
          }

          function bindFunctionCode1(childrenMap, autoSelectFirst) {
            var $f1 = $popupField('#functionCode1');
            var prevValue = $f1.val();
            $f1.empty().append($('<option>', { value: '', text: '선택' }));
            $.each(getChildren(childrenMap, '0'), function () {
              if (normalizeTreeKey(this.level) === '1') {
                $f1.append($('<option>', { value: this.id, text: this.text }));
              }
            });
            // 루트가 0이 아닐 수도 있어서 level 1 전체를 fallback으로 수집
            if ($f1.find('option').length === 1) {
              $.each(childrenMap, function (_, arr) {
                $.each(arr || [], function () {
                  if (normalizeTreeKey(this.level) === '1') {
                    $f1.append($('<option>', { value: this.id, text: this.text }));
                  }
                });
              });
            }
            var hasCurrent = prevValue && $f1.find("option[value='" + prevValue + "']").length > 0;
            if (hasCurrent) {
              $f1.val(prevValue);
            } else if (autoSelectFirst && $f1.find('option').length > 1) {
              $f1.val($f1.find('option:eq(1)').val());
            } else {
              $f1.val('');
            }
            $f1.trigger('change.select2');
            syncSelect2DisplayedText($f1);
          }

          function bindFunctionCode2(childrenMap, parentId, autoSelectFirst) {
            var $f2 = $popupField('#functionCode2');
            var prevValue = $f2.val();
            $f2.empty().append($('<option>', { value: '', text: '선택' }));
            $.each(getChildren(childrenMap, parentId), function () {
              if (normalizeTreeKey(this.level) === '2') {
                $f2.append($('<option>', { value: this.id, text: this.text }));
              }
            });
            if (prevValue && $f2.find("option[value='" + prevValue + "']").length > 0) {
              $f2.val(prevValue);
            } else if (autoSelectFirst && $f2.find('option').length > 1) {
              $f2.val($f2.find('option:eq(1)').val());
            } else {
              $f2.val('');
            }
            $f2.trigger('change.select2');
            syncSelect2DisplayedText($f2);
          }

          function bindDocumentType(childrenMap, parentId, autoSelectFirst) {
            var $doc = $popupField('#documentTypeCode');
            var prevValue = $doc.val();
            $doc.empty().append($('<option>', { value: '', text: '선택' }));
            $.each(getChildren(childrenMap, parentId), function () {
              if (normalizeTreeKey(this.level) === '3' || String(this.type || '').toLowerCase() === 'item') {
                $doc.append($('<option>', { value: this.id, text: this.text }));
              }
            });
            if (prevValue && $doc.find("option[value='" + prevValue + "']").length > 0) {
              $doc.val(prevValue);
            } else if (autoSelectFirst && $doc.find('option').length > 1) {
              $doc.val($doc.find('option:eq(1)').val());
            } else {
              $doc.val('');
            }
            $popupField('#treeCd').val($doc.val() || '');
            $doc.trigger('change.select2');
            syncSelect2DisplayedText($doc);
          }

          function updateDrawingNoPreview() {
            var docType = $.trim($popupField('#documentTypeCode option:selected').text() || '');
            var function2Text = $.trim($popupField('#functionCode2 option:selected').text() || '');
            var registerNo = $.trim($popupField('#registerNo').val() || '');
            var function2NoMatch = function2Text.match(/(\d{2,3})/);
            var function2No = function2NoMatch ? function2NoMatch[1] : '';
            var registerNoPadded = registerNo ? registerNo.padStart(3, '0') : '';

            if (!docType || !function2No || !registerNoPadded || docType === '선택') {
              $popupField('#drawingNo').val('');
              requestNextDrawingVersionNo();
              return;
            }
            $popupField('#drawingNo').val('K8-' + docType + '-' + function2No + '-' + registerNoPadded);
            requestNextDrawingVersionNo();
          }


        </script>
        <div class="dialogContent drawingRegisterPopup popup-base popup-actions-center">
          <div class="popupHero">
            <h2>등록 정보</h2>
            <p>필수 항목을 입력하고 파일을 첨부한 뒤 등록해 주세요.</p>
          </div>

          <form id="formDrawingRegisterPopup">
            <ul class="section popupCard popupFormGrid">

              <li class="half">
                <label>등록자</label>
                <input type="text" id="registerUserView" value="${registerUser}" readonly>
              </li>

              <li class="half">
                <label>발행일</label>
                <input type="text" id="approvalDate" name="approvalDate" value="${date}" readonly>
              </li>

              <li class="half">
                <label>공동 발행자</label>
                <select id="coPublisher" name="coPublisher" style="width:100%;" multiple="multiple"
                  data-placeholder="선택">
                  <option value="__NONE__" selected>선택</option>
                  <c:forEach var="user" items="${coPublisherUsers}">
                    <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
                  </c:forEach>
                </select>
              </li>

              <li class="full">
                <label>문서제목</label>
                <input type="text" id="dataName" name="dataName" value="">
              </li>

              <li class="half">
                <label>Function Code1</label>
                <select id="functionCode1" name="functionCode1" style="width:100%;"></select>
              </li>
              
              <li class="half">
                <label>Function Code2</label>
                <select id="functionCode2" name="functionCode2" style="width:100%;"></select>
              </li>

              <li class="half">
                <label>Document Type Code</label>
                <select id="documentTypeCode" name="documentTypeCode" style="width:100%;"></select>
              </li>

              <li class="half">
                <label>등록번호</label>
                <input type="text" id="registerNo" name="registerNo" value="" maxlength="3">
              </li>

              <li class="full">
                <label>문서번호</label>
                <input type="text" id="drawingNo" name="drawingNo" value="" readonly>
              </li>

              <li class="full">
                <label>버전</label>
                <div class="inlineCheckGroup versionTypeGroup">
                  <label><input type="radio" name="versionType" value="Draft"> Draft</label>
                  <label><input type="radio" name="versionType" value="Preliminary" checked> Preliminary</label>
                  <label><input type="radio" name="versionType" value="Final"> Final</label>
                </div>
              </li>

              <li class="half">
                <label>버전 No</label>
                <input type="text" id="versionNo" name="versionNo" value="P00" readonly>
              </li>

              <li class="half">
                <label>검토자</label>
                <select id="reviewerUser" name="reviewerUser" style="width:100%;" multiple="multiple"
                  data-placeholder="선택">
                  <option value="__NONE__" selected>선택</option>
                  <c:forEach var="user" items="${coPublisherUsers}">
                    <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
                  </c:forEach>
                </select>
              </li>

              <li class="half">
                <label>승인자</label>
                <select id="approver" name="approver" style="width:100%;" multiple="multiple" data-placeholder="선택">
                  <option value="__NONE__" selected>선택</option>
                  <c:forEach var="user" items="${coPublisherUsers}">
                    <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
                  </c:forEach>
                </select>
              </li>

              <input type="hidden" id="treeCd" name="treeCd" value="">
              <input type="hidden" id="registerUser" name="registerUser" value="${registerUser}">
              <input type="hidden" id="businessTypeCd" name="businessTypeCd" value="DEVELOPMENT">
              <input type="hidden" id="distributeTypeCd" name="distributeTypeCd" value="NORMAL">
              <input type="hidden" id="totalPageNo" name="totalPageNo" value="1">
              <input type="hidden" id="distributionPoint" name="distributionPoint" value="">


            </ul>
          </form>

          <ul class="section popupCard popupFormGrid uploadOnly">
            <li class="singleFileUpload full">
              <label>주파일 등록</label>

              <form id="fileForm" name="fileForm" enctype="multipart/form-data">
                <input type="file" id="drawingRegisFile" name="drawingRegisFile" onchange="fileChange()"
                  style="display:none;">
              </form>
              <div class="uploadLine" title="파일을 드래그하여 첨부하거나 업로드 버튼을 클릭하세요.">
                <div class="fileNameWrap">
                  <input type="text" name="fileName" id="fileName" placeholder="첨부 버튼 클릭 또는 드래그앤드롭" readonly>
                  <button type="button" class="fileClearBtn" title="첨부파일 초기화"
                    onclick="clearDrawingSelectedFile()">×</button>
                </div>
                <button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()">
                </button>
              </div>
            </li>

            <li class="singleFileUpload full">
              <label>보조파일 등록</label>
              <form id="subFileForm" name="subFileForm" enctype="multipart/form-data">
                <input type="file" id="drawingSubFiles" name="drawingSubFiles" multiple="multiple" style="display:none;">
              </form>
              <div class="uploadLine">
                <div class="fileNameWrap">
                  <input type="text" id="subFileNames" class="subFileNames" placeholder="보조파일(다수) 선택" readonly>
                  <button type="button" class="fileClearBtn" title="보조파일 초기화"
                    onclick="clearDrawingSubSelectedFiles()">×</button>
                </div>
                <button type="button" class="ui-button ui-corner-all fileUploadBtn"
                  onclick="$popupField('#drawingSubFiles').trigger('click');">
                </button>
              </div>
            </li>
          </ul>

          <div class="dialogBtnSet">
            <div class="left"></div>

            <div class="right">

              <custom:popupButton function="saveX()" name="save" label="btn.register" id="save" />

              <custom:popupButton function="closeDrawingRegisterPopup()" name="close" label="btn.close" id="close" />

            </div>
          </div>

        </div>
