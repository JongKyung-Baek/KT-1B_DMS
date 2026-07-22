<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

                <script type="text/javascript"
                    src="/resources/js/views/inside/distribution/docRegisterPopup.js"></script>

                <style>
                    .docRegisterPopup .popupFormGrid > li.half {
                        width: calc(50% - 12px);
                    }

                    .docRegisterPopup .popupFormGrid > li.full {
                        width: 100%;
                    }

                    .docRegisterPopup .popupFormGrid > li > label {
                        white-space: nowrap;
                    }

                    .docRegisterPopup .popupFormGrid input[type="text"],
                    .docRegisterPopup .popupFormGrid select {
                        width: 100%;
                        box-sizing: border-box;
                    }

                    .docRegisterPopup .popupFormGrid .select2-container {
                        width: 100% !important;
                    }

                    .docRegisterPopup .popupFormGrid #coPublisher+.select2-container .select2-selection--multiple,
                    .docRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection--multiple,
                    .docRegisterPopup .popupFormGrid #approver+.select2-container .select2-selection--multiple {
                        position: relative;
                        padding-right: 28px !important;
                    }

                    .docRegisterPopup .popupFormGrid #coPublisher+.select2-container.people-empty .select2-selection--multiple::before,
                    .docRegisterPopup .popupFormGrid #reviewerUser+.select2-container.people-empty .select2-selection--multiple::before,
                    .docRegisterPopup .popupFormGrid #approver+.select2-container.people-empty .select2-selection--multiple::before {
                        content: "선택";
                        position: absolute;
                        left: 12px;
                        top: 50%;
                        transform: translateY(-50%);
                        color: #9aa0ad;
                        font-size: 15px;
                        pointer-events: none;
                    }

                    .docRegisterPopup .popupFormGrid #coPublisher+.select2-container .select2-selection--multiple::after,
                    .docRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection--multiple::after,
                    .docRegisterPopup .popupFormGrid #approver+.select2-container .select2-selection--multiple::after {
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

                    .docRegisterPopup .popupFormGrid #coPublisher+.select2-container .select2-selection__choice[title="선택"],
                    .docRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection__choice[title="선택"],
                    .docRegisterPopup .popupFormGrid #approver+.select2-container .select2-selection__choice[title="선택"] {
                        display: none !important;
                    }

                    .docRegisterPopup .inlineCheckGroup {
                        display: flex;
                        align-items: center;
                        column-gap: 18px;
                        row-gap: 8px;
                        min-height: 32px;
                        flex-wrap: wrap;
                    }

                    .docRegisterPopup .inlineCheckGroup label {
                        margin: 0;
                        display: inline-flex;
                        align-items: center;
                        column-gap: 8px;
                        line-height: 1.2;
                        white-space: nowrap;
                    }

                    .docRegisterPopup .uploadLine {
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

                    .docRegisterPopup .uploadLine input[type="text"] {
                        width: 100% !important;
                        padding-right: 32px;
                    }

                    .docRegisterPopup .uploadLine.is-dragover {
                        border-color: #2f6fed;
                        background-color: #f4f8ff;
                    }

                    .docRegisterPopup .fileNameWrap {
                        position: relative;
                        flex: 1 1 auto;
                        min-width: 0;
                    }

                    .docRegisterPopup .fileClearBtn {
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

                    .docRegisterPopup .subFileNames {
                        width: 100%;
                        box-sizing: border-box;
                    }
                </style>

                <!-- 등록 및 배포요청 팝업(미등록 자료등록 버튼) -->
                <script>
                    var bUploadCheck = false;
                    var DOC_MULTI_EMPTY_VALUE = '__NONE__';
                    var DOC_MULTI_ALL_VALUE = '__ALL__';

                    function getDocPopupRoot() {
                        var $root = $('.docRegisterPopup:visible').last();
                        if ($root.length) return $root;
                        $root = $('#popupDialog .docRegisterPopup').last();
                        if ($root.length) return $root;
                        return $('.docRegisterPopup').last();
                    }

                    function $docPopupField(selector) {
                        var $root = getDocPopupRoot();
                        if ($root.length) return $root.find(selector).first();
                        return $(selector).first();
                    }

                    function getDocPopupDialog() {
                        var $root = getDocPopupRoot();
                        var $dialog = $root.closest('.ui-dialog');
                        if ($dialog.length) return $dialog;
                        if ($('#popupDialog').length) return $('#popupDialog');
                        return $(document.body);
                    }

                    function isDocPeopleMultiSelect($select) {
                        if (!$select || !$select.length) return false;
                        var id = $select.attr('id');
                        return id === 'coPublisher' || id === 'reviewerUser' || id === 'approver';
                    }

                    function bindDocSelect2ValueSync(selector) {
                        var $el = $docPopupField(selector);
                        if (!$el.length) return;

                        $el.off('select2:select.valueSync');
                        $el.on('select2:select.valueSync', function (e) {
                            var selectedId = e && e.params && e.params.data ? e.params.data.id : $(this).val();
                            if (selectedId === undefined || selectedId === null) return;
                            $(this).val(String(selectedId)).trigger('change');
                            syncDocSelect2DisplayedText($(this));
                        });
                    }

                    function syncDocSelect2DisplayedText($select) {
                        if (!$select || !$select.length) return;
                        if ($select.prop('multiple')) return;
                        var text = $.trim($select.find('option:selected').text() || '');
                        if (!text) text = '선택';
                        var $render = $select.next('.select2').find('.select2-selection__rendered').first();
                        if ($render.length) {
                            $render.text(text);
                            $render.attr('title', text);
                        }
                    }

                    function getDocPopupSelectValue(selector) {
                        var value = $docPopupField(selector).val();
                        if ($.isArray(value)) {
                            return $.map(value, function (item) {
                                return $.trim(item || '');
                            }).filter(function (item) {
                                return item.length > 0 && item !== DOC_MULTI_EMPTY_VALUE && item !== DOC_MULTI_ALL_VALUE;
                            }).join(',');
                        }
                        if (value === DOC_MULTI_EMPTY_VALUE) return '';
                        return $.trim(value || '');
                    }

                    function isValidationX() {
                        if ($.trim($docPopupField("#approvalDate").val()) === "") {
                            alertMessage("발행일을 확인하세요.");
                            return false;
                        }

                        if ($.trim($docPopupField("#dataName").val()) === "") {
                            //isValidDataEmpty("dataName", "form.dataName");
                            alertMessage('문서제목을 입력하세요.');
                            return false;
                        }
                        if ($.trim($docPopupField("#functionCode1").val()) === "") {
                            alertMessage("Function Code1을 선택하세요.");
                            return false;
                        }
                        if ($.trim($docPopupField("#functionCode2").val()) === "") {
                            alertMessage("Function Code2를 선택하세요.");
                            return false;
                        }
                        if ($.trim($docPopupField("#docNo").val()) === "") {
                            alertMessage("문서번호를 확인하세요.");
                            return false;
                        }
                        // [미사용] 버전/버전 No 검증
                        // if ($('input[name="versionType"]:checked').length === 0) {
                        //     alertMessage("버전을 선택하세요.");
                        //     return false;
                        // }
                        // if ($.trim($docPopupField("#versionNo").val()) === "") {
                        //     alertMessage("버전 No를 확인하세요.");
                        //     return false;
                        // }
                        if (getDocPopupSelectValue("#reviewerUser") === "") {
                            alertMessage("배포를 선택하세요.");
                            return false;
                        }
                        if (getDocPopupSelectValue("#approver") === "") {
                            alertMessage("승인자를 선택하세요.");
                            return false;
                        }
                        var fileInput = $docPopupField("#docRegisFile")[0];
                        var file = fileInput.files[0];
                        if (!file) {
                            alertMessage("주파일을 선택하세요.");
                            return false;
                        }

                        return true;
                    }


                    function saveX() {
                        if (!isValidationX()) {
                            return;
                        }

                        var dataText = $docPopupField('#dataName').val();
                        var orgFileNm = $docPopupField('#fileName').val();
                        var businessType = $docPopupField("#businessTypeCd").length ? $docPopupField("#businessTypeCd").val() : "DEVELOPMENT";
                        var distributeType = $docPopupField("#distributeTypeCd").length ? $docPopupField("#distributeTypeCd").val() : "NORMAL";
                        var fileType = "";
                        var docClassCd1 = $docPopupField("#docClassCd1").length ? $docPopupField("#docClassCd1").val() : "";
                        var treeCd = $docPopupField("#functionCode2").val() || "";
                        var docNo = $docPopupField("#docNo").val();
                        var registerNo = $docPopupField("#registerNo").val() || "";
                        let protectChecked = document.querySelector('input[name="protectYn"]:checked');
                        let protectYn = protectChecked ? protectChecked.value : ($docPopupField("#protectYn").val() || "N");

                        var fileInput = $docPopupField("#docRegisFile")[0];
                        var subFileInput = $docPopupField("#docSubFiles")[0];

                        var file = fileInput.files[0];
                        var coPublisher = getDocPopupSelectValue("#coPublisher");
                        var reviewerUser = getDocPopupSelectValue("#reviewerUser");
                        var approver = getDocPopupSelectValue("#approver");
                        var registerUser = $docPopupField("#registerUser").val() || "";
                        var formData = new FormData();

                        formData.append("file", file);
                        formData.append("fileName", dataText);
                        formData.append("orgFileNm", orgFileNm);
                        formData.append("businessTypeCd", businessType);
                        formData.append("distributeTypeCd", distributeType);
                        formData.append("fileType", fileType);
                        formData.append("docClassCd1", docClassCd1);
                        formData.append("protectYn", protectYn);
                        formData.append("treeCd", treeCd);
                        formData.append("docNo", docNo);
                        formData.append("registerNo", registerNo);
                        // [미사용] 버전/버전 No 미전송
                        formData.append("reviewerUser", reviewerUser);
                        formData.append("approver", approver);
                        formData.append("coPublisher", coPublisher);
                        formData.append("approvalDate", $docPopupField("#approvalDate").val() || "");
                        formData.append("registerUser", registerUser);
                        formData.append("insertUserNm", registerUser);
                        if (subFileInput && subFileInput.files && subFileInput.files.length > 0) {
                            Array.prototype.forEach.call(subFileInput.files, function (subFile) {
                                formData.append("subFiles", subFile);
                            });
                        }

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
                        closePopup('popupDialog');
                        callAjaxUpload(formData, "/inside/distribution/docRequest/uploadDocRegisFile", requestCrXCallback);
                    }

                    function requestCrXCallback(response) {
                        if (response.success) {
                        } else {
                            alertMessage(g_msg("msg.registerFailure"));						//등록에 실패했습니다.
                        }
                    }

                    function clearDocSelectedFile() {
                        var $fileInput = $docPopupField('#docRegisFile');
                        var $fileName = $docPopupField('#fileName');
                        if ($fileInput.length) {
                            $fileInput.val('');
                            if ($fileInput[0]) {
                                $fileInput[0].value = '';
                            }
                        }
                        $fileName.val('');
                    }

                    function clearDocSubSelectedFiles() {
                        var $subFileInput = $docPopupField('#docSubFiles');
                        var $subFileNames = $docPopupField('#subFileNames');
                        if ($subFileInput.length) {
                            $subFileInput.val('');
                            if ($subFileInput[0]) {
                                $subFileInput[0].value = '';
                            }
                        }
                        $subFileNames.val('');
                    }

                    function bindDocFileDragDrop() {
                        bindUploadLineDragDrop('#docRegisFile', false);
                        bindUploadLineDragDrop('#docSubFiles', true);
                    }

                    function bindUploadLineDragDrop(inputSelector, isMultiple) {
                        var $fileInput = $docPopupField(inputSelector);
                        if (!$fileInput.length) return;

                        var $zone = $fileInput.closest('form').next('.uploadLine');
                        if (!$zone.length) {
                            $zone = $fileInput.closest('.uploadLine');
                        }
                        if (!$zone.length) return;

                        var dragDepth = 0;
                        $zone.off('.docDnD');

                        $zone.on('dragenter.docDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            dragDepth += 1;
                            $zone.addClass('is-dragover');
                        });

                        $zone.on('dragover.docDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                        });

                        $zone.on('dragleave.docDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            dragDepth = Math.max(0, dragDepth - 1);
                            if (dragDepth === 0) {
                                $zone.removeClass('is-dragover');
                            }
                        });
                        $zone.on('drop.docDnD', function (e) {
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

                    function updateDocVersionNo() {
                        var selected = $('input[name="versionType"]:checked').val() || 'Preliminary';
                        var nextNo = '01';
                        if (selected === 'Draft') {
                            $docPopupField('#versionNo').val('Dr' + nextNo);
                        } else if (selected === 'Final') {
                            $docPopupField('#versionNo').val('Fi' + nextNo);
                        } else {
                            $docPopupField('#versionNo').val('Pre' + nextNo);
                        }
                    }

                    function normalizeDocTreeKey(value) {
                        if (value === undefined || value === null) return "";
                        return String(value).trim();
                    }

                    function getDocChildren(childrenMap, parentId) {
                        var key = normalizeDocTreeKey(parentId);
                        return childrenMap[key] || [];
                    }

                    function bindDocFunctionCode1(childrenMap, autoSelectFirst) {
                        var $f1 = $docPopupField('#functionCode1');
                        var prevValue = $f1.val();
                        $f1.empty().append($('<option>', { value: '', text: '선택' }));

                        $.each(getDocChildren(childrenMap, 'ROOT'), function () {
                            if (normalizeDocTreeKey(this.level) === '1') {
                                $f1.append($('<option>', { value: this.id, text: this.text }));
                            }
                        });

                        if ($f1.find('option').length === 1) {
                            $.each(childrenMap, function (_, arr) {
                                $.each(arr || [], function () {
                                    if (normalizeDocTreeKey(this.level) === '1') {
                                        $f1.append($('<option>', { value: this.id, text: this.text }));
                                    }
                                });
                            });
                        }

                        if (prevValue && $f1.find("option[value='" + prevValue + "']").length > 0) {
                            $f1.val(prevValue);
                        } else if (autoSelectFirst && $f1.find('option').length > 1) {
                            $f1.val($f1.find('option:eq(1)').val());
                        } else {
                            $f1.val('');
                        }
                        $f1.trigger('change.select2');
                        syncDocSelect2DisplayedText($f1);
                    }

                    function bindDocFunctionCode2(childrenMap, parentId, autoSelectFirst) {
                        var $f2 = $docPopupField('#functionCode2');
                        var prevValue = $f2.val();
                        $f2.empty().append($('<option>', { value: '', text: '선택' }));

                        $.each(getDocChildren(childrenMap, parentId), function () {
                            if (normalizeDocTreeKey(this.level) === '2') {
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
                        syncDocSelect2DisplayedText($f2);
                    }

                    function bindDocDocumentType(childrenMap, parentId, autoSelectFirst) {
                        var $doc = $docPopupField('#documentTypeCode');
                        var prevValue = $doc.val();
                        $doc.empty().append($('<option>', { value: '', text: '선택' }));

                        $.each(getDocChildren(childrenMap, parentId), function () {
                            if (normalizeDocTreeKey(this.level) === '3' || String(this.type || '').toLowerCase() === 'item') {
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
                        $docPopupField('#treeCd').val($doc.val() || '');
                        $doc.trigger('change.select2');
                        syncDocSelect2DisplayedText($doc);
                    }

                    function initDocRegisterTreeSelectors() {
                        callAjax({}, "/inside/distribution/docRequest/selectTree", function (response) {
                            var list = response || [];
                            var childrenMap = {};

                            $.each(list, function () {
                                var node = $.extend({}, this);
                                node.id = normalizeDocTreeKey(node.id);
                                node.parent = normalizeDocTreeKey(node.parent || "#");

                                if (!childrenMap[node.parent]) {
                                    childrenMap[node.parent] = [];
                                }
                                childrenMap[node.parent].push(node);
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

                            $docPopupField('#functionCode1').off('change').on('change', function () {
                                bindDocFunctionCode2(childrenMap, $(this).val(), true);
                                syncDocSelect2DisplayedText($docPopupField('#functionCode1'));
                                syncDocSelect2DisplayedText($docPopupField('#functionCode2'));
                                $docPopupField('#treeCd').val($docPopupField('#functionCode2').val() || '');
                                updateDocNoPreview();
                            });

                            $docPopupField('#functionCode2').off('change').on('change', function () {
                                syncDocSelect2DisplayedText($docPopupField('#functionCode2'));
                                $docPopupField('#treeCd').val($(this).val() || '');
                                updateDocNoPreview();
                            });

                            bindDocFunctionCode1(childrenMap, false);
                            bindDocFunctionCode2(childrenMap, $docPopupField('#functionCode1').val(), false);
                            $docPopupField('#treeCd').val($docPopupField('#functionCode2').val() || '');
                            updateDocNoPreview();
                        });
                    }

                    function updateDocNoPreview() {
                        var function2Text = $.trim($docPopupField('#functionCode2 option:selected').text() || '');
                        var treeCd = $.trim($docPopupField('#functionCode2').val() || '');
                        var function2NoMatch = function2Text.match(/(\d{2,4})/);
                        var function2No = function2NoMatch ? function2NoMatch[1] : '';

                        if (!treeCd || !function2No) {
                            $docPopupField('#docNo').val('');
                            $docPopupField('#registerNo').val('');
                            return;
                        }

                        callAjax({
                            treeCd: treeCd,
                            functionCode2No: function2No
                        }, "/inside/distribution/docRequest/nextDocNo", function (response) {
                            var nextNo = response && response.nextRegisterNo ? String(response.nextRegisterNo) : '';
                            var documentNo = response && response.documentNo ? String(response.documentNo) : '';
                            $docPopupField('#registerNo').val(nextNo);
                            $docPopupField('#docNo').val(documentNo);
                        });
                    }

                    function fnUploadBeforeCheck() {
                        bUploadCheck = true;
                        //    console.log("fnUploadBeforeCheck")

                        return true;
                    }

                    function fnUploadFail() {
//	console.log("fnUploadFail")
        <%-- alertMessage("<spring:message code='145'/>"); 파일전송 실패 --%>
    }

                    function fnUploadFinish() {
                        //	console.log("fnUploadFinish")
                        bUploadCheck = false;
                    }


                    function fnUploadSuccess() {
                        var nFileCount = arguments[0];
                        var nFileIndex = arguments[1];
                        var strFileId = arguments[2];
                        var strFileName = arguments[3];
                        var strFilePath = arguments[4];
                        var nFileSize = arguments[5];  // byte
                        var strErrorCode = arguments[6];
                        var strErrorMsg = arguments[7];
                        var strMFileCode = arguments[8];
                        var strMFileCode1 = arguments[9];

                        var fileLen = strFileName.length;
                        var lastDot = strFileName.lastIndexOf('.');
                        var ext = strFileName.substring(lastDot, fileLen);

                        //console.log("strMFileCode1 : " + strMFileCode1);

                        var strParam = "";
                        strParam += "파일갯수   : " + nFileCount + "\n";
                        strParam += "파일인덱스 : " + nFileIndex + "\n";
                        strParam += "파일아이디 : " + strFileId + "\n";
                        strParam += "파일명     : " + strFileName + "\n";
                        strParam += "파일경로   : " + strFilePath + "\n";
                        strParam += "파일크기   : " + nFileSize + "\n";
                        strParam += "에러코드   : " + strErrorCode + "\n";
                        strParam += "에러메시지 : " + strErrorMsg + "\n";
                        strParam += "마스터코드 : " + strMFileCode + "\n";
                        strParam += "마스터코드 : " + strMFileCode1 + "\n";
                        strParam += "확장자 : " + ext + "\n";

                        try { console.log("fnUploadSuccess", strParam); } catch (e) { }

                        var param = new Object();
                        param.fileNm = strFileName;
                        param.filePath = strFilePath + '/' + strFileId + ext;
                        param.fileSize = nFileSize;
                        //	console.log("param.filePath : ", param.filePath)

                        //callAjax(param, "/inside/unregisted/request/saveUnregisterFileX", saveUnregisterFileXCallback);
                    }

                    $(document).ready(function () {
                        $docPopupField('#docRegisFile').off('change').on('change', function () {
                            var fullFilename = $(this).val().split('\\').pop();
                            var lastIndex = fullFilename.lastIndexOf(".");

                            // If the filename has an extension, remove it, if not, keep it as is
                            var filenameWithoutExtension = lastIndex >= 0 ? fullFilename.substring(0, lastIndex) : fullFilename;

                            $docPopupField('#fileName').val(fullFilename); // Preserve extension

                            // Check if the #dataName field is empty
                            if ($docPopupField('#dataName').val() === '') {
                                $docPopupField('#dataName').val(filenameWithoutExtension); // Remove extension
                            }
                        });

                        $docPopupField('#docSubFiles').off('change').on('change', function () {
                            var files = this.files || [];
                            var names = [];
                            for (var i = 0; i < files.length; i++) {
                                names.push(files[i].name);
                            }
                            $docPopupField('#subFileNames').val(names.join(', '));
                        });

                        // Ensure select2 dropdown renders inside popup so popup-local colors apply.
                        $docPopupField('form#formDocRegisterPopup').find('select').each(function () {
                            var $select = $(this);
                            var isMultiple = $select.prop('multiple');

                            if ($select.data('select2')) {
                                $select.select2('destroy');
                            }

                            var option = {
                                dropdownParent: getDocPopupDialog(),
                                width: '100%'
                            };

                            if (isMultiple) {
                                option.closeOnSelect = false;
                                option.placeholder = $select.data('placeholder') || '선택';
                            } else {
                                option.minimumResultsForSearch = -1;
                            }

                            $select.select2(option);
                            syncDocSelect2DisplayedText($select);

                            if (isMultiple && isDocPeopleMultiSelect($select)) {
                                $select.off('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder');
                                $select.on('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder', function () {
                                    var $current = $(this);
                                    var selected = $current.val() || [];
                                    if (!Array.isArray(selected)) selected = [selected];
                                    var nonEmpty = selected.filter(function (v) {
                                        return $.trim(v || '') !== '' && v !== DOC_MULTI_EMPTY_VALUE && v !== DOC_MULTI_ALL_VALUE;
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
                                    var hasEmpty = selected.indexOf(DOC_MULTI_EMPTY_VALUE) !== -1;
                                    var nonEmpty = selected.filter(function (v) {
                                        return $.trim(v || '') !== '' && v !== DOC_MULTI_EMPTY_VALUE && v !== DOC_MULTI_ALL_VALUE;
                                    });

                                    if (hasEmpty && nonEmpty.length > 0) {
                                        $current.data('syncingEmptyOption', true);
                                        $current.val(nonEmpty).trigger('change.select2');
                                        $current.data('syncingEmptyOption', false);
                                        return;
                                    }

                                    if (!hasEmpty && nonEmpty.length === 0) {
                                        $current.data('syncingEmptyOption', true);
                                        $current.val([DOC_MULTI_EMPTY_VALUE]).trigger('change.select2');
                                        $current.data('syncingEmptyOption', false);
                                    }
                                });

                                $select.off('change.multiAllOption');
                                $select.on('change.multiAllOption', function () {
                                    var $current = $(this);
                                    if ($current.attr('id') !== 'reviewerUser') return;
                                    if ($current.data('syncingAllOption')) return;

                                    var selected = $current.val() || [];
                                    if (!Array.isArray(selected)) selected = [selected];
                                    var hasAll = selected.indexOf(DOC_MULTI_ALL_VALUE) !== -1;
                                    if (!hasAll) return;

                                    var allValues = [];
                                    $current.find('option').each(function () {
                                        var value = $.trim($(this).val() || '');
                                        if (value === '' || value === DOC_MULTI_EMPTY_VALUE || value === DOC_MULTI_ALL_VALUE) return;
                                        allValues.push(value);
                                    });

                                    $current.data('syncingAllOption', true);
                                    $current.val(allValues).trigger('change.select2');
                                    $current.data('syncingAllOption', false);
                                });

                                var currentValues = $select.val() || [];
                                if (!Array.isArray(currentValues) || currentValues.length === 0) {
                                    $select.val([DOC_MULTI_EMPTY_VALUE]).trigger('change.select2');
                                }
                                $select.trigger('change.multiPlaceholder');
                            }
                        });

                        bindDocSelect2ValueSync('#functionCode1');
                        bindDocSelect2ValueSync('#functionCode2');
                        bindDocFileDragDrop();

                        initDocRegisterTreeSelectors();
                        // [미사용] 버전/버전 No
                        // $('input[name="versionType"]').off('change').on('change', updateDocVersionNo);
                        // updateDocVersionNo();
                    });


                </script>
                <div class="dialogContent docRegisterPopup popup-base popup-actions-center">
                    <div class="popupHero">
                        <h2>등록 정보</h2>
                        <p>필수 항목을 입력하고 파일을 첨부한 뒤 등록해 주세요.</p>
                    </div>
                    <form id="formDocRegisterPopup">
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
                                    <option value="__NONE__">선택</option>
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

                            <%-- [IOC] Document Type Code / 등록번호 직접 입력 미사용 --%>

                            <li class="full">
                                <label>문서번호</label>
                                <input type="text" id="docNo" name="docNo" value="" readonly>
                            </li>

                            <%-- [미사용] 버전
                            <li class="full">
                                <label>버전</label>
                                <div class="inlineCheckGroup">
                                    <label><input type="radio" name="versionType" value="Draft"> Draft</label>
                                    <label><input type="radio" name="versionType" value="Preliminary" checked> Preliminary</label>
                                    <label><input type="radio" name="versionType" value="Final"> Final</label>
                                </div>
                            </li>
                            --%>

                            <%-- [미사용] 버전 No
                            <li class="half">
                                <label>버전 No</label>
                                <input type="text" id="versionNo" name="versionNo" value="Pre01" readonly>
                            </li>
                            --%>

                            <li class="half">
                                <label>배포</label>
                                <select id="reviewerUser" name="reviewerUser" style="width:100%;" multiple="multiple"
                                    data-placeholder="선택">
                                    <option value="__NONE__">선택</option>
                                    <option value="__ALL__">전체선택</option>
                                    <c:forEach var="user" items="${coPublisherUsers}">
                                        <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
                                    </c:forEach>
                                </select>
                            </li>

                            <li class="half">
                                <label>승인자</label>
                                <select id="approver" name="approver" style="width:100%;" multiple="multiple"
                                    data-placeholder="선택">
                                    <option value="__NONE__">선택</option>
                                    <c:forEach var="user" items="${coPublisherUsers}">
                                        <option value="${user.comboVal}">${user.comboLabel}${empty user.comboTooltip ? '' : ' ('}${empty user.comboTooltip ? '' : user.comboTooltip}${empty user.comboTooltip ? '' : ')'}</option>
                                    </c:forEach>
                                </select>
                            </li>

                            <input type="hidden" id="treeCd" name="treeCd" value="">
                            <input type="hidden" id="registerUser" name="registerUser" value="${registerUser}">
                            <input type="hidden" id="registerNo" name="registerNo" value="">
                            <input type="hidden" id="businessTypeCd" name="businessTypeCd" value="DEVELOPMENT">
                            <input type="hidden" id="distributeTypeCd" name="distributeTypeCd" value="NORMAL">
                            <input type="hidden" id="docClassCd1" name="docClassCd1" value="">
                            <input type="hidden" id="protectYn" name="protectYn" value="N">

                        </ul>
                    </form>
                    <ul class="section popupCard popupFormGrid uploadOnly">
                        <li class="singleFileUpload full">
                            <label>주파일 등록</label>
                            <form id="fileForm" name="fileForm" enctype="multipart/form-data">
                                <input type="file" id="docRegisFile" name="docRegisFile" onchange="fileChange()"
                                    style="display: none;" />
                            </form>
                            <div class="uploadLine" title="파일을 드래그하여 첨부하거나 업로드 버튼을 클릭하세요.">
                                <div class="fileNameWrap">
                                    <input type="text" name="fileName" id="fileName" placeholder="첨부 버튼 클릭 또는 드래그앤드롭" readonly />
                                    <button type="button" class="fileClearBtn"
                                        title="첨부파일 초기화" onclick="clearDocSelectedFile()">×</button>
                                </div>
                                <button class="ui-button ui-corner-all fileUploadBtn"
                                    onclick="fileUpload()"></button>
                            </div>
                        </li>
                        <li class="singleFileUpload full">
                            <label>보조파일 등록</label>
                            <form id="subFileForm" name="subFileForm" enctype="multipart/form-data">
                                <input type="file" id="docSubFiles" name="docSubFiles" multiple="multiple" style="display:none;">
                            </form>
                            <div class="uploadLine">
                                <div class="fileNameWrap">
                                    <input type="text" id="subFileNames" class="subFileNames" placeholder="보조파일(다수) 선택" readonly>
                                    <button type="button" class="fileClearBtn" title="보조파일 초기화"
                                        onclick="clearDocSubSelectedFiles()">×</button>
                                </div>
                                <button type="button" class="ui-button ui-corner-all fileUploadBtn"
                                    onclick="$docPopupField('#docSubFiles').trigger('click');"></button>
                            </div>
                        </li>
                    </ul>
                    <div class="dialogBtnSet">
                        <div class="left"></div>
                        <div class="right">
                            <!-- 등록 -->
                            <custom:popupButton function="saveX()" name="save" label="btn.register" id="save" />
                            <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close"
                                id="close" />
                        </div>
                    </div>
                </div>
