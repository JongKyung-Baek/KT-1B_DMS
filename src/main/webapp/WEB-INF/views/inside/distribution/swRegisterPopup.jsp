<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

                <script type="text/javascript"
                    src="/resources/js/views/inside/distribution/swRegisterPopup.js"></script>

                <style>
                    .swRegisterPopup .popupFormGrid>li.half {
                        width: calc(50% - 12px);
                    }

                    .swRegisterPopup .popupFormGrid>li.full {
                        width: 100%;
                    }

                    .swRegisterPopup .popupFormGrid>li>label {
                        white-space: nowrap;
                    }

                    .swRegisterPopup .popupFormGrid input[type="text"],
                    .swRegisterPopup .popupFormGrid select {
                        width: 100%;
                        box-sizing: border-box;
                    }

                    .swRegisterPopup .popupFormGrid .select2-container {
                        width: 100% !important;
                    }

                    .swRegisterPopup .popupFormGrid #swTypeCd+.select2-container .select2-selection--multiple,
                    .swRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection--multiple {
                        position: relative;
                        padding-right: 28px !important;
                    }

                    .swRegisterPopup .popupFormGrid #swTypeCd+.select2-container.swtype-empty .select2-selection--multiple::before,
                    .swRegisterPopup .popupFormGrid #reviewerUser+.select2-container.swtype-empty .select2-selection--multiple::before {
                        content: "선택";
                        position: absolute;
                        left: 12px;
                        top: 50%;
                        transform: translateY(-50%);
                        color: #9aa0ad;
                        font-size: 15px;
                        pointer-events: none;
                    }

                    .swRegisterPopup .popupFormGrid #swTypeCd+.select2-container .select2-selection--multiple::after,
                    .swRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection--multiple::after {
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

                    .swRegisterPopup .popupFormGrid #swTypeCd+.select2-container .select2-search--inline,
                    .swRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-search--inline {
                        width: 100%;
                    }

                    .swRegisterPopup .popupFormGrid #swTypeCd+.select2-container .select2-search--inline .select2-search__field,
                    .swRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-search--inline .select2-search__field {
                        width: 100% !important;
                        min-width: 120px;
                    }

                    .swRegisterPopup .popupFormGrid #swTypeCd+.select2-container .select2-selection__choice[title="선택"],
                    .swRegisterPopup .popupFormGrid #reviewerUser+.select2-container .select2-selection__choice[title="선택"] {
                        display: none !important;
                    }

                    .swRegisterPopup .uploadLine {
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

                    .swRegisterPopup .uploadLine input[type="text"] {
                        width: 100% !important;
                        padding-right: 32px;
                    }

                    .swRegisterPopup .uploadLine.is-dragover {
                        border-color: #2f6fed;
                        background-color: #f4f8ff;
                    }

                    .swRegisterPopup .fileNameWrap {
                        position: relative;
                        flex: 1 1 auto;
                        min-width: 0;
                    }

                    .swRegisterPopup .fileClearBtn {
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

                    .swRegisterPopup .subFileNames {
                        width: 100%;
                        box-sizing: border-box;
                    }
                </style>

                <!-- 등록 및 배포요청 팝업(미등록 자료등록 버튼) -->
                <script>
                    var SWTYPE_EMPTY_VALUE = '__NONE__';
                    var $swPopupRoot = null;
                    var IS_SW_REVISION_UPDATE = "${param.isNewRevision}" === "true";
                    var SW_PREV_OBJECT_ID = "${param.objectId}";
                    var IS_SW_REGISTER_PAGE = "${swRegisterPageMode}" === "true";

                    function nextRevisionValue(current) {
                        var value = $.trim(current || "");
                        if (value === "") return "01";
                        var m = value.match(/^(.*?)(\d+)$/);
                        if (!m) return value + "1";
                        var prefix = m[1];
                        var digits = m[2];
                        var num = parseInt(digits, 10);
                        if (isNaN(num)) return value + "1";
                        var next = String(num + 1);
                        while (next.length < digits.length) next = "0" + next;
                        return prefix + next;
                    }

                    function getSwPopupRoot() {
                        if ($swPopupRoot && $swPopupRoot.length && $.contains(document, $swPopupRoot[0])) {
                            return $swPopupRoot;
                        }
                        $swPopupRoot = $('#popupDialog .swRegisterPopup').last();
                        if (!$swPopupRoot.length) {
                            $swPopupRoot = $('.swRegisterPopup').last();
                        }
                        return $swPopupRoot;
                    }

                    function $swPopupField(selector) {
                        var $root = getSwPopupRoot();
                        var $found = $root.find(selector);
                        if ($found.length) return $found.first();
                        return $(selector).first();
                    }

                    function getSwPopupDialog() {
                        var $root = getSwPopupRoot();
                        if (IS_SW_REGISTER_PAGE) {
                            var $page = $root.closest('.sw-register-page');
                            return $page.length ? $page : $(document.body);
                        }
                        if ($root.length) {
                            var $dialogContent = $root.closest('.ui-dialog-content');
                            if ($dialogContent.length) return $dialogContent;
                        }
                        if ($('#popupDialog').length) return $('#popupDialog');
                        return $(document.body);
                    }

                    function initSwPopupSelect2() {
                        var $root = getSwPopupRoot();
                        var $dialog = getSwPopupDialog();
                        $root.find('select:visible').each(function () {
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
                                if ($select.attr('id') === 'swTypeCd') {
                                    option.maximumSelectionLength = 3;
                                    option.language = {
                                        maximumSelected: function () {
                                            return '보드 멤버는 최대 3명까지 선택할 수 있습니다.';
                                        }
                                    };
                                }
                            }
                            $select.select2(option);
                            if (isMultiple) {
                                $select.off('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder');
                                $select.on('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder', function () {
                                    var $current = $(this);
                                    var placeholder = $current.data('placeholder') || '선택';
                                    var selected = $current.val() || [];
                                    var hasSelected = Array.isArray(selected)
                                        ? selected.filter(function (v) { return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; }).length > 0
                                        : ($.trim(selected || '') !== '' && selected !== SWTYPE_EMPTY_VALUE);
                                    var text = hasSelected ? '' : placeholder;
                                    var $container = $current.next('.select2');
                                    $container.toggleClass('swtype-empty', !hasSelected);
                                    $container.find('.select2-search__field').attr('placeholder', text);
                                });
                                $select.off('change.multiEmptyOption');
                                $select.on('change.multiEmptyOption', function () {
                                    var $current = $(this);
                                    if ($current.data('syncingEmptyOption')) return;
                                    var selected = $current.val() || [];
                                    if (!Array.isArray(selected)) selected = [selected];
                                    var hasEmpty = selected.indexOf(SWTYPE_EMPTY_VALUE) !== -1;
                                    var nonEmpty = selected.filter(function (v) { return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });

                                    if (hasEmpty && nonEmpty.length > 0) {
                                        $current.data('syncingEmptyOption', true);
                                        $current.val(nonEmpty).trigger('change.select2');
                                        $current.data('syncingEmptyOption', false);
                                        return;
                                    }
                                    if (!hasEmpty && nonEmpty.length === 0) {
                                        $current.data('syncingEmptyOption', true);
                                        $current.val([SWTYPE_EMPTY_VALUE]).trigger('change.select2');
                                        $current.data('syncingEmptyOption', false);
                                    }
                                });
                                //                $select.off('change.multiLimit');
                                //                $select.on('change.multiLimit', function() {
                                //                    var $current = $(this);
                                //                    if ($current.attr('id') !== 'swTypeCd') return;
                                //                    if ($current.data('syncingLimit')) return;
                                //                    var selected = $current.val() || [];
                                //                    if (!Array.isArray(selected)) selected = [selected];
                                //                    var nonEmpty = selected.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
                                //                    var prevNonEmpty = $current.data('prevNonEmpty') || [];
                                //
                                //                    if (nonEmpty.length > 3) {
                                //                        var rollback = prevNonEmpty.length ? prevNonEmpty.slice(0, 3) : nonEmpty.slice(0, 3);
                                //                        $current.data('syncingLimit', true);
                                //                        $current.val(rollback.length ? rollback : [SWTYPE_EMPTY_VALUE]).trigger('change.select2');
                                //                        $current.data('syncingLimit', false);
                                //                        alertMessage('보드 멤버는 최대 3명까지 선택할 수 있습니다.');
                                //                        return;
                                //                    }
                                //
                                //                    $current.data('prevNonEmpty', nonEmpty);
                                //                });
                                var currentValues = $select.val() || [];
                                if (!Array.isArray(currentValues) || currentValues.length === 0) {
                                    $select.val([SWTYPE_EMPTY_VALUE]).trigger('change.select2');
                                }
                                $select.trigger('change.multiLimit');
                                $select.trigger('change.multiPlaceholder');
                            }
                        });
                    }

                    function updateSwNoPreview() {
                        var levelValue = $.trim($swPopupField('#businessTypeCd').val() || '');
                        var levelText = $.trim($swPopupField('#businessTypeCd option:selected').text() || '');
                        var levelNo = '';
                        var levelTextMatch = levelText.match(/(?:LEVEL|LV|L)\s*([0-9]{1,3})/i) || levelText.match(/([0-9]{1,3})/);
                        if (levelTextMatch) {
                            levelNo = levelTextMatch[1];
                        } else if (!/^TRB/i.test(levelValue)) {
                            var levelValueMatch = levelValue.match(/(?:LEVEL|LV|L)\s*([0-9]{1,3})/i) || levelValue.match(/([0-9]{1,3})/);
                            levelNo = levelValueMatch ? levelValueMatch[1] : '';
                        }

                        if (!levelNo) {
                            $swPopupField('#registerNo').val('');
                            return;
                        }

                        callAjax({
                            levelNo: levelNo
                        }, "/inside/distribution/swRequest/nextSwNo", function (response) {
                            var nextNo = response && response.nextRegisterNo ? String(response.nextRegisterNo) : '';
                            var documentNo = response && response.documentNo ? String(response.documentNo) : '';
                            $swPopupField('#registerNo').val(nextNo);
                            // if ($.trim($swPopupField('#swNo').val()) === '') {
                            //     $swPopupField('#swNo').val(documentNo);
                            // }
                        });
                    }

                    var emptyArray2 = [];
                    var bUploadCheck = false;
                    var curSendIndex = 0

                    function isValidationX() {
                        emptyArray2 = [];

                        if ($.trim($swPopupField("#ccbIssueDt").val()) === "") {
                            alertMessage("CCB발행일을 입력하세요.");
                            return false;
                        }

                        if ($.trim($swPopupField("#dataName").val()) === "") {
                            //isValidDataEmpty("dataName", "form.dataName");
                            /* alertMessage('의뢰명을 입력하세요.'); */
                            alertMessage(g_msg('msg.requestNm'));
                            return false;
                        }
                        if ($.trim($swPopupField("#businessTypeCd").val()) === "") {
                                    /* alertMessage("유형을 선택하세요."); */
                                    alertMessage(g_msg('msg.selectType'));
                                    return false;
                                }
                        if ($.trim($swPopupField("#revNo").val()) === "") {
                            alertMessage("Revision을 선택하세요.");
                            return false;
                        }
                       if ($.trim($swPopupField("#swNo").val()) === "") {
                                    /* alertMessage("송부번호를 확인하세요."); */
                                    alertMessage(g_msg('msg.sendNum'));
                                    return false;
                                }
                        /* var swTypeCdValue = $swPopupField("#swTypeCd").val();
                        var selectedSwTypes = Array.isArray(swTypeCdValue)
                            ? swTypeCdValue.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
                            : [swTypeCdValue].filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
                        if(selectedSwTypes.length === 0){
                            alertMessage("보드 멤버를 선택하세요.");
                            return false;
                        }
                        var reviewerValues = $swPopupField("#reviewerUser").val();
                        var selectedReviewers = Array.isArray(reviewerValues)
                            ? reviewerValues.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
                            : [reviewerValues].filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
                        if(selectedReviewers.length === 0){
                            alertMessage("참여자를 선택하세요.");
                            return false;
                        } */
                        // var fileInput = document.getElementById("fileInput");
                        var fileInput = document.getElementById("swRegisFile");
                        var file = fileInput.files[0];
                        if (file) {
                            emptyArray2.push(file);
                        }

                        if (emptyArray2.length <= 0) {
                                    /* alertMessage("첨부파일을 선택하세요."); */
                                    alertMessage(g_msg('msg.attachedFile'));
                                    return false;
                                }

                        return true;
                    }


                    function saveX() {
                        if (!isValidationX()) {
                            return;
                        }

                        for (var i = 0; i < emptyArray2.length; i++) {
                            if (emptyArray2[i]) {
                                emptyArray2[i].dataName = $('#dataName').val();
                            }
                        }

                        curSendIndex = 0

                        var dataText = document.getElementById('dataName').value;
                        var orgFileNm = document.getElementById('fileName').value;
                        var businessType = document.getElementById("businessTypeCd").value;
                        var distributeType = document.getElementById("distributeTypeCd").value;
                        var revNo = document.getElementById("revNo").value;
                        var swTypeCdValues = $swPopupField("#swTypeCd").val() || [];
                        var swTypeCdSelected = Array.isArray(swTypeCdValues)
                            ? swTypeCdValues.filter(function (v) { return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
                            : [swTypeCdValues].filter(function (v) { return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
                        var swTypeCd = swTypeCdSelected.join(",");
                        var reviewerValues = $swPopupField("#reviewerUser").val() || [];
                        var reviewerSelected = Array.isArray(reviewerValues)
                            ? reviewerValues.filter(function (v) { return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
                            : [reviewerValues].filter(function (v) { return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
                        var reviewerUser = reviewerSelected.join(",");
                        var ccbIssueDt = document.getElementById("ccbIssueDt").value;
                        var swNo = $swPopupField("#swNo").val();
                        var treeCd = $swPopupField("#treeCd").val() || businessType;
                        if (!$.trim(treeCd)) {
                            alertMessage("Level을 선택하세요.");
                            return;
                        }

                        var fileInput = document.getElementById("swRegisFile");
                        var subFileInput = document.getElementById("swSubFiles");

                        var file = fileInput.files[0];
                        var registerUser = $swPopupField("#registerUser").val() || "";
                        var formData = new FormData();

                        // formData.append("totalPageNo", totalPageNo);
                        formData.append("file", file);
                        formData.append("fileName", dataText);
                        formData.append("orgFileNm", orgFileNm);
                        formData.append("businessTypeCd", businessType);
                        formData.append("distributeTypeCd", distributeType);
                        formData.append("revNo", revNo);
                        formData.append("swTypeCd", swTypeCd);
                        formData.append("reviewerUser", reviewerUser);
                        formData.append("ccbIssueDt", ccbIssueDt);
                        formData.append("swNo", swNo);
                        formData.append("treeCd", treeCd);
                        formData.append("registerUser", registerUser);
                        formData.append("insertUserNm", registerUser);
                        if (IS_SW_REVISION_UPDATE && $.trim(SW_PREV_OBJECT_ID) !== "") {
                            formData.append("objectId", $.trim(SW_PREV_OBJECT_ID));
                            formData.append("isNewRevision", "true");
                        }
                        if (subFileInput && subFileInput.files && subFileInput.files.length > 0) {
                            Array.prototype.forEach.call(subFileInput.files, function (subFile) {
                                formData.append("subFiles", subFile);
                            });
                        }

                        callAjaxUpload(formData, "/inside/distribution/swRequest/uploadSwRegisFile", requestCrXCallback);
                    }

                    function requestCrXCallback(response) {
                        if (response.success) {
                            infoMessage(g_msg('msg.registerComplete'), function () {
                                $(this).dialog("close");
                                if (IS_SW_REGISTER_PAGE) {
                                    location.href = "/inside/distribution/swRequest/";
                                    return;
                                }
                                try {
                                    if (parent && typeof parent.searchList === "function") {
                                        parent.searchList(parent.gridParam);
                                    }
                                } catch (e) { }
                                closePopup('popupDialog');
                            });
                        } else {
                            alertMessage(g_msg("msg.registerFailure"));						//등록에 실패했습니다.
                        }
                    }

                    function fnUploadBeforeCheck() {
                        bUploadCheck = true;
                        //    console.log("fnUploadBeforeCheck")

                        return true;
                    }

                    function fnUploadFail() {
//	console.log("fnUploadFail")
        // alertMessage("파일전송 실패");
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
                        emptyArray2.push(param);
                    }

                    function clearSwSelectedFile() {
                        var $fileInput = $swPopupField('#swRegisFile');
                        var $fileName = $swPopupField('#fileName');
                        if ($fileInput.length) {
                            $fileInput.val('');
                            if ($fileInput[0]) {
                                $fileInput[0].value = '';
                            }
                        }
                        $fileName.val('');
                    }

                    function clearSwSubSelectedFiles() {
                        var $subFileInput = $swPopupField('#swSubFiles');
                        var $subFileNames = $swPopupField('#subFileNames');
                        if ($subFileInput.length) {
                            $subFileInput.val('');
                            if ($subFileInput[0]) {
                                $subFileInput[0].value = '';
                            }
                        }
                        $subFileNames.val('');
                    }

                    function bindSwFileDragDrop(inputSelector, allowMultiple) {
                        var $fileInput = $swPopupField(inputSelector);
                        var $form = $fileInput.closest('form');
                        var $zone = $form.next('.uploadLine');
                        if (!$zone.length) {
                            $zone = $form.closest('.uploadLine');
                        }
                        if (!$zone.length || !$fileInput.length) return;

                        var dragDepth = 0;

                        $zone.off('.swDnD');
                        $zone.on('dragenter.swDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            dragDepth += 1;
                            $zone.addClass('is-dragover');
                        });
                        $zone.on('dragover.swDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                        });
                        $zone.on('dragleave.swDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            dragDepth = Math.max(0, dragDepth - 1);
                            if (dragDepth === 0) {
                                $zone.removeClass('is-dragover');
                            }
                        });
                        $zone.on('drop.swDnD', function (e) {
                            e.preventDefault();
                            e.stopPropagation();
                            dragDepth = 0;
                            $zone.removeClass('is-dragover');

                            var event = e.originalEvent || e;
                            var files = event.dataTransfer && event.dataTransfer.files ? event.dataTransfer.files : null;
                            if (!files || !files.length) return;

                            var input = $fileInput[0];
                            if (window.DataTransfer) {
                                var dt = new DataTransfer();
                                if (allowMultiple) {
                                    for (var i = 0; i < files.length; i++) {
                                        dt.items.add(files[i]);
                                    }
                                } else {
                                    dt.items.add(files[0]);
                                }
                                input.files = dt.files;
                            }
                            $fileInput.trigger('change');
                        });
                    }

                    $(document).ready(function () {
                        if (IS_SW_REVISION_UPDATE) {
                            var prevRevNo = "${param.revNo}";
                            var nextRevNo = nextRevisionValue(prevRevNo);
                            $swPopupField('#revNo').val(nextRevNo);
                            $swPopupField('#swNo').val("${param.swNo}");
                            $swPopupField('#dataName').val("${param.dataName}");
                            if ($.trim("${param.businessTypeCd}") !== "") {
                                $swPopupField('#businessTypeCd').val("${param.businessTypeCd}");
                                $swPopupField('#treeCd').val("${param.businessTypeCd}");
                            }
                            $swPopupField('#businessTypeCd').prop('disabled', true);
                            if ($.trim("${param.ccbIssueDt}") !== "") {
                                $swPopupField('#ccbIssueDt').val("${param.ccbIssueDt}");
                            }
                            if ($.trim("${param.distributeTypeCd}") !== "") {
                                $swPopupField('#distributeTypeCd').val("${param.distributeTypeCd}");
                            }
                        }

                        $('#swRegisFile').change(function () {
                            var fullFilename = $(this).val().split('\\').pop();
                            var lastIndex = fullFilename.lastIndexOf(".");

                            // If the filename has an extension, remove it, if not, keep it as is
                            var filenameWithoutExtension = lastIndex >= 0 ? fullFilename.substring(0, lastIndex) : fullFilename;

                            $('#fileName').val(fullFilename); // Preserve extension

                            // Check if the #dataName field is empty
                            if ($('#dataName').val() === '') {
                                $('#dataName').val(filenameWithoutExtension); // Remove extension
                            }
                        });

                        $swPopupField('#swSubFiles').change(function () {
                            var files = this.files || [];
                            var names = [];
                            for (var i = 0; i < files.length; i++) {
                                names.push(files[i].name);
                            }
                            $swPopupField('#subFileNames').val(names.join(', '));
                        });

                        $swPopupField("#businessTypeCd").on("change", function () {
                            $swPopupField("#treeCd").val($(this).val() || "");
                            updateSwNoPreview();
                        });
                        if (!$.trim($swPopupField("#treeCd").val() || "")) {
                            $swPopupField("#treeCd").val($swPopupField("#businessTypeCd").val() || "");
                        }

                        // Popup 렌더 직후/지연 렌더 둘 다 커버
                        initSwPopupSelect2();
                        setTimeout(initSwPopupSelect2, 0);
                        if (IS_SW_REVISION_UPDATE) {
                            var swTypeRaw = "${param.swTypeCd}";
                            if ($.trim(swTypeRaw) !== "") {
                                $swPopupField('#swTypeCd').val(swTypeRaw.split(',')).trigger('change.select2');
                            } else {
                                $swPopupField('#swTypeCd').trigger('change.multiPlaceholder');
                            }
                            var reviewerRaw = "${param.reviewerUser}";
                            if ($.trim(reviewerRaw) !== "") {
                                $swPopupField('#reviewerUser').val(reviewerRaw.split(',')).trigger('change.select2');
                            } else {
                                $swPopupField('#reviewerUser').trigger('change.multiPlaceholder');
                            }
                        } else {
                            updateSwNoPreview();
                        }
                        bindSwFileDragDrop('#swRegisFile', false);
                        bindSwFileDragDrop('#swSubFiles', true);
                    });

                </script>
                <div class="dialogContent swRegisterPopup popup-base popup-actions-center">
                    <div class="popupHero">
                        <h2>
                            <spring:message code='label.dataUpdate'></spring:message>
                        </h2>
                        <p>
                            <spring:message code='label.dataUpdateInfo'></spring:message>
                        </p>
                    </div>
                    <form id="formSwRegisterPopup">
                        <input type="hidden" name="treeCd" id="treeCd" value="${treeCd}" />
                        <input type="hidden" name="registerUser" id="registerUser" value="${registerUser}" />
                        <ul class="section popupCard popupFormGrid">

                            <li>
                                <!-- <label>발행자</label> --><label>
                                    <spring:message code='label.client'></spring:message>
                                </label><input type="text" id="registerUserView" value="${registerUser}" readonly>
                            </li>
                            <li style="display:none;">
                                <label>CCB 개최일</label><input type="date" id="ccbIssueDt" name="ccbIssueDt"
                                    value="${date}">
                            </li>

                            <li class="full">
                                <!-- <label>CCB 제목</label> -->
                                <label>
                                    <spring:message code='label.projectNm'></spring:message>
                                </label>
                                <input type="text" name="dataName" id="dataName" value="" />
                            </li>

                            <li class="half">
                                <!-- <label>Level</label> -->
                                <label>
                                    <spring:message code='label.typeSub'></spring:message>
                                </label>
                                <select id="businessTypeCd" name="businessTypeCd">
                                    <option value="">
                                        <spring:message code="combo.select" />
                                    </option>
                                    <c:forEach var="item" items="${businessTypeCd}">
                                        <option value="${item.comboVal}">${item.comboLabel}</option>
                                    </c:forEach>
                                </select>
                            </li>

                            <li class="half">
                                <!-- <label>Revision</label> -->
                                <label>
                                    <spring:message code='label.version'></spring:message>
                                </label>
                                <input type="text" id="revNo" name="revNo" value="00" readonly>
                            </li>

                            <li class="full">
                                <!-- <label>문서번호</label> -->
                                <label>
                                    <spring:message code='label.sendNum'></spring:message>
                                </label>
                                <input type="text" id="swNo" name="swNo" value="" maxlength="100" />
                            </li>

                            <li class="half" style="display:none;">
                                <label>등록번호</label>
                                <input type="text" id="registerNo" name="registerNo" value="" maxlength="3" />
                            </li>

                            <li class="half" style="display:none;">
                                <label>보드 멤버</label>
                                <select id="swTypeCd" name="swTypeCd" multiple="multiple" data-placeholder="선택">
                                    <option value="__NONE__" selected>선택</option>
                                    <c:forEach var="item" items="${swTypeCd}">
                                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ?
                                            '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty
                                            item.comboTooltip ? '' : ')'}</option>
                                    </c:forEach>
                                </select>
                            </li>

                            <li class="half" style="display:none;">
                                <label>참여자</label>
                                <select id="reviewerUser" name="reviewerUser" multiple="multiple" data-placeholder="선택">
                                    <option value="__NONE__" selected>선택</option>
                                    <c:forEach var="item" items="${swTypeCd}">
                                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ?
                                            '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty
                                            item.comboTooltip ? '' : ')'}</option>
                                    </c:forEach>
                                </select>
                            </li>

                            <li class="half" style="display:none;">
                                <label>문서유형</label>
                                <select id="distributeTypeCd" name="distributeTypeCd">
                                    <option value="">선택</option>
                                    <c:forEach var="item" items="${distributeTypeCd}">
                                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ?
                                            '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty
                                            item.comboTooltip ? '' : ')'}</option>
                                    </c:forEach>
                                </select>
                            </li>
                        </ul>
                    </form>
                    <ul class="section popupCard popupFormGrid uploadOnly">
                        <li class="singleFileUpload full">
                            <label>
                                <spring:message code='label.attachedFileMain'></spring:message>
                            </label>
                            <%-- 파일추가 --%>
                                <div class="uploadLine" title="Drag and drop a file to upload it, or click the button.">
                                    <form id="fileForm" name="fileForm" enctype="multipart/form-data">
                                        <input type="file" id="swRegisFile" name="swRegisFile" onchange="fileChange()"
                                            style="display: none;" />
                                    </form>
                                    <div class="fileNameWrap">
                                        <input type="text" name="fileName" id="fileName"
                                            placeholder="<spring:message code='msg.fileUpload'></spring:message>"
                                            readonly />
                                        <button type="button" class="fileClearBtn" title=""
                                            onclick="clearSwSelectedFile()">×</button>
                                    </div>
                                    <button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"><%--
                                            <spring:message code="btn.fileUpload" /> --%></button>
                                </div>
                        </li>
                        <li class="singleFileUpload full">
                            <label>
                                <spring:message code='label.attachedFileSub'></spring:message>
                            </label>
                            <div class="uploadLine" title="Drag and drop a file to upload it, or click the button.">
                                <form id="subFileForm" name="subFileForm" enctype="multipart/form-data">
                                    <input type="file" id="swSubFiles" name="swSubFiles" multiple="multiple"
                                        style="display: none;" />
                                </form>
                                <div class="fileNameWrap">
                                    <input type="text" name="subFileNames" id="subFileNames" class="subFileNames"
                                        placeholder="<spring:message code='msg.fileUpload'></spring:message>"
                                        readonly />
                                    <button type="button" class="fileClearBtn" title=""
                                        onclick="clearSwSubSelectedFiles()">×</button>
                                </div>
                                <button class="ui-button ui-corner-all fileUploadBtn" type="button"
                                    onclick="$('#swSubFiles').click();"></button>
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
