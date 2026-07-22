<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript" src="/resources/js/views/inside/distribution/dxf/dxfRegisterPopup.js"></script>

<style>
    .dxfRegisterPopup .popupFormGrid > li.half {
        width: calc(50% - 12px);
    }

    .dxfRegisterPopup .popupFormGrid > li.full {
        width: 100%;
    }

    .dxfRegisterPopup .popupFormGrid > li > label {
        white-space: nowrap;
    }

    .dxfRegisterPopup .popupFormGrid input[type="text"],
    .dxfRegisterPopup .popupFormGrid select {
        width: 100%;
        box-sizing: border-box;
    }

    .dxfRegisterPopup .popupFormGrid .select2-container {
        width: 100% !important;
    }

    .dxfRegisterPopup .popupFormGrid #swTypeCd + .select2-container .select2-selection--multiple,
    .dxfRegisterPopup .popupFormGrid #reviewerUser + .select2-container .select2-selection--multiple {
        position: relative;
        padding-right: 28px !important;
    }

    .dxfRegisterPopup .popupFormGrid #swTypeCd + .select2-container.swtype-empty .select2-selection--multiple::before,
    .dxfRegisterPopup .popupFormGrid #reviewerUser + .select2-container.swtype-empty .select2-selection--multiple::before {
        content: "선택";
        position: absolute;
        left: 12px;
        top: 50%;
        transform: translateY(-50%);
        color: #9aa0ad;
        font-size: 15px;
        pointer-events: none;
    }

    .dxfRegisterPopup .popupFormGrid #swTypeCd + .select2-container .select2-selection--multiple::after,
    .dxfRegisterPopup .popupFormGrid #reviewerUser + .select2-container .select2-selection--multiple::after {
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

    .dxfRegisterPopup .popupFormGrid #swTypeCd + .select2-container .select2-search--inline,
    .dxfRegisterPopup .popupFormGrid #reviewerUser + .select2-container .select2-search--inline {
        width: 100%;
    }

    .dxfRegisterPopup .popupFormGrid #swTypeCd + .select2-container .select2-search--inline .select2-search__field,
    .dxfRegisterPopup .popupFormGrid #reviewerUser + .select2-container .select2-search--inline .select2-search__field {
        width: 100% !important;
        min-width: 120px;
    }

    .dxfRegisterPopup .popupFormGrid #swTypeCd + .select2-container .select2-selection__choice[title="선택"],
    .dxfRegisterPopup .popupFormGrid #reviewerUser + .select2-container .select2-selection__choice[title="선택"] {
        display: none !important;
    }

    .dxfRegisterPopup .uploadLine {
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

    .dxfRegisterPopup .uploadLine input[type="text"] {
        width: 100% !important;
        padding-right: 32px;
    }

    .dxfRegisterPopup .uploadLine.is-dragover {
        border-color: #2f6fed;
        background-color: #f4f8ff;
    }

    .dxfRegisterPopup .fileNameWrap {
        position: relative;
        flex: 1 1 auto;
        min-width: 0;
    }

    .dxfRegisterPopup .fileClearBtn {
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

    .dxfRegisterPopup .subFileNames {
        width: 100%;
        box-sizing: border-box;
    }
</style>

<script>
    var SWTYPE_EMPTY_VALUE = '__NONE__';
    var $dxfPopupRoot = null;
    var IS_DXF_REVISION_UPDATE = "${param.isNewRevision}" === "true";
    var DXF_PREV_OBJECT_ID = "${param.objectId}";

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

    function getDxfPopupRoot() {
        if ($dxfPopupRoot && $dxfPopupRoot.length && $.contains(document, $dxfPopupRoot[0])) {
            return $dxfPopupRoot;
        }
        $dxfPopupRoot = $('#popupDialog .dxfRegisterPopup').last();
        if (!$dxfPopupRoot.length) {
            $dxfPopupRoot = $('.dxfRegisterPopup').last();
        }
        return $dxfPopupRoot;
    }

    function $dxfPopupField(selector) {
        var $root = getDxfPopupRoot();
        var $found = $root.find(selector);
        if ($found.length) return $found.first();
        return $(selector).first();
    }

    function getDxfPopupDialog() {
        var $root = getDxfPopupRoot();
        if ($root.length) {
            var $dialogContent = $root.closest('.ui-dialog-content');
            if ($dialogContent.length) return $dialogContent;
        }
        if ($('#popupDialog').length) return $('#popupDialog');
        return $(document.body);
    }

    function initDxfPopupSelect2() {
        var $root = getDxfPopupRoot();
        var $dialog = getDxfPopupDialog();
        $root.find('select:visible').each(function() {
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
                        maximumSelected: function() {
                            return '보드 멤버는 최대 3명까지 선택할 수 있습니다.';
                        }
                    };
                }
            }
            $select.select2(option);
            if (isMultiple) {
                $select.off('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder');
                $select.on('change.multiPlaceholder select2:open.multiPlaceholder select2:close.multiPlaceholder', function() {
                    var $current = $(this);
                    var placeholder = $current.data('placeholder') || '선택';
                    var selected = $current.val() || [];
                    var hasSelected = Array.isArray(selected)
                        ? selected.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; }).length > 0
                        : ($.trim(selected || '') !== '' && selected !== SWTYPE_EMPTY_VALUE);
                    var text = hasSelected ? '' : placeholder;
                    var $container = $current.next('.select2');
                    $container.toggleClass('swtype-empty', !hasSelected);
                    $container.find('.select2-search__field').attr('placeholder', text);
                });
                $select.off('change.multiEmptyOption');
                $select.on('change.multiEmptyOption', function() {
                    var $current = $(this);
                    if ($current.data('syncingEmptyOption')) return;
                    var selected = $current.val() || [];
                    if (!Array.isArray(selected)) selected = [selected];
                    var hasEmpty = selected.indexOf(SWTYPE_EMPTY_VALUE) !== -1;
                    var nonEmpty = selected.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });

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

    function updateDxfNoPreview() {
        var levelValue = $.trim($dxfPopupField('#businessTypeCd').val() || '');
        var levelText = $.trim($dxfPopupField('#businessTypeCd option:selected').text() || '');
        var levelNo = '';

        var levelTextMatch = levelText.match(/(?:LEVEL|LV|L)\s*([0-9]{1,3})/i) || levelText.match(/([0-9]{1,3})/);
        if (levelTextMatch) {
            levelNo = levelTextMatch[1];
        } else if (!/^TRB/i.test(levelValue)) {
            var levelValueMatch = levelValue.match(/(?:LEVEL|LV|L)\s*([0-9]{1,3})/i) || levelValue.match(/([0-9]{1,3})/);
            levelNo = levelValueMatch ? levelValueMatch[1] : '';
        }

        if (!levelNo) {
            $dxfPopupField('#registerNo').val('');
            $dxfPopupField('#dxfNo').val('');
            return;
        }

        callAjax({
            levelNo: levelNo
        }, "/inside/distribution/dxfRequest/nextDxfNo", function (response) {
            var nextNo = response && response.nextRegisterNo ? String(response.nextRegisterNo) : '';
            var documentNo = response && response.documentNo ? String(response.documentNo) : '';
            $dxfPopupField('#registerNo').val(nextNo);
            $dxfPopupField('#dxfNo').val(documentNo);
        });
    }

    var emptyArray2 = [];

    function isValidationX() {
        emptyArray2 = [];

        if ($.trim($dxfPopupField('#pmpcbIssueDt').val()) === '') {
            alertMessage('PMPCB개최일을 입력하세요.');
            return false;
        }
        if ($.trim($dxfPopupField('#dataName').val()) === '') {
            alertMessage('PMPCB제목을 입력하세요.');
            return false;
        }
        if ($.trim($dxfPopupField('#businessTypeCd').val()) === '') {
            alertMessage('Level을 선택하세요.');
            return false;
        }
        if ($.trim($dxfPopupField('#revNo').val()) === '') {
            alertMessage('Revision을 선택하세요.');
            return false;
        }
        var swTypeCdValue = $dxfPopupField('#swTypeCd').val();
        var selectedSwTypes = Array.isArray(swTypeCdValue)
            ? swTypeCdValue.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
            : [swTypeCdValue].filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
        if (selectedSwTypes.length === 0) {
            alertMessage('보드 멤버를 선택하세요.');
            return false;
        }
        var reviewerValues = $dxfPopupField('#reviewerUser').val();
        var selectedReviewers = Array.isArray(reviewerValues)
            ? reviewerValues.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
            : [reviewerValues].filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
        if (selectedReviewers.length === 0) {
            alertMessage('참여자를 선택하세요.');
            return false;
        }
        if ($.trim($dxfPopupField('#dxfNo').val()) === '') {
            alertMessage('문서번호를 확인하세요.');
            return false;
        }

        var fileInput = document.getElementById('dxfRegisFile');
        var subFileInput = document.getElementById('dxfSubFiles');
        var file = fileInput.files[0];
        if (file) {
            emptyArray2.push(file);
        }

        if (emptyArray2.length <= 0) {
            alertMessage('첨부파일을 선택하세요.');
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

        var dataText = document.getElementById('dataName').value;
        var orgFileNm = document.getElementById('fileName').value;
        var businessType = document.getElementById('businessTypeCd').value;
        var distributeType = document.getElementById('distributeTypeCd').value;
        var swTypeCdValues = $dxfPopupField('#swTypeCd').val() || [];
        var swTypeCdSelected = Array.isArray(swTypeCdValues)
            ? swTypeCdValues.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
            : [swTypeCdValues].filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
        var swTypeCd = swTypeCdSelected.join(',');
        var reviewerValues = $dxfPopupField('#reviewerUser').val() || [];
        var reviewerSelected = Array.isArray(reviewerValues)
            ? reviewerValues.filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; })
            : [reviewerValues].filter(function(v){ return $.trim(v || '') !== '' && v !== SWTYPE_EMPTY_VALUE; });
        var reviewerUser = reviewerSelected.join(',');
        var revNo = document.getElementById('revNo').value;
        var pmpcbIssueDt = document.getElementById('pmpcbIssueDt').value;
        var dxfNo = $dxfPopupField('#dxfNo').val();
        var treeCd = $dxfPopupField('#treeCd').val() || businessType;
        if (!$.trim(treeCd)) {
            alertMessage('Level을 선택하세요.');
            return;
        }

        var fileInput = document.getElementById('dxfRegisFile');
        var subFileInput = document.getElementById('dxfSubFiles');
        var file = fileInput.files[0];

        var registerUser = $dxfPopupField('#registerUser').val() || '';
        var formData = new FormData();
        formData.append('file', file);
        formData.append('fileName', dataText);
        formData.append('orgFileNm', orgFileNm);
        formData.append('businessTypeCd', businessType);
        formData.append('distributeTypeCd', distributeType);
        formData.append('swTypeCd', swTypeCd);
        formData.append('reviewerUser', reviewerUser);
        formData.append('revNo', revNo);
        formData.append('pmpcbIssueDt', pmpcbIssueDt);
        formData.append('dxfNo', dxfNo);
        formData.append('treeCd', treeCd);
        formData.append('registerUser', registerUser);
        formData.append('insertUserNm', registerUser);
        if (IS_DXF_REVISION_UPDATE && $.trim(DXF_PREV_OBJECT_ID) !== '') {
            formData.append('objectId', $.trim(DXF_PREV_OBJECT_ID));
            formData.append('isNewRevision', 'true');
        }
        if (subFileInput && subFileInput.files && subFileInput.files.length > 0) {
            Array.prototype.forEach.call(subFileInput.files, function (subFile) {
                formData.append('subFiles', subFile);
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
        callAjaxUpload(formData, '/inside/distribution/dxfRequest/uploadDxfRegisFile', requestCrXCallback);
    }

    function requestCrXCallback(response) {
        if (response.success) {
        } else {
            alertMessage(g_msg('msg.registerFailure'));
        }
    }

    function fileUpload() {
        $('#dxfRegisFile').click();
    }

    function fileChange() {
        var fileName = $('#dxfRegisFile').val();
        if (fileName.indexOf('\\') !== -1) {
            $('#fileName').val(fileName.substring(fileName.lastIndexOf('\\') + 1, fileName.length));
        }
    }

    function clearDxfSelectedFile() {
        var $fileInput = $dxfPopupField('#dxfRegisFile');
        var $fileName = $dxfPopupField('#fileName');
        if ($fileInput.length) {
            $fileInput.val('');
            if ($fileInput[0]) {
                $fileInput[0].value = '';
            }
        }
        $fileName.val('');
    }

    function clearDxfSubSelectedFiles() {
        var $subFileInput = $dxfPopupField('#dxfSubFiles');
        var $subFileNames = $dxfPopupField('#subFileNames');
        if ($subFileInput.length) {
            $subFileInput.val('');
            if ($subFileInput[0]) {
                $subFileInput[0].value = '';
            }
        }
        $subFileNames.val('');
    }

    function bindDxfFileDragDrop(inputSelector, allowMultiple) {
        var $fileInput = $dxfPopupField(inputSelector);
        var $form = $fileInput.closest('form');
        var $zone = $form.next('.uploadLine');
        if (!$zone.length) {
            $zone = $form.closest('.uploadLine');
        }
        if (!$zone.length || !$fileInput.length) return;

        var dragDepth = 0;

        $zone.off('.dxfDnD');
        $zone.on('dragenter.dxfDnD', function(e) {
            e.preventDefault();
            e.stopPropagation();
            dragDepth += 1;
            $zone.addClass('is-dragover');
        });
        $zone.on('dragover.dxfDnD', function(e) {
            e.preventDefault();
            e.stopPropagation();
        });
        $zone.on('dragleave.dxfDnD', function(e) {
            e.preventDefault();
            e.stopPropagation();
            dragDepth = Math.max(0, dragDepth - 1);
            if (dragDepth === 0) {
                $zone.removeClass('is-dragover');
            }
        });
        $zone.on('drop.dxfDnD', function(e) {
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

    $(document).ready(function() {
        if (IS_DXF_REVISION_UPDATE) {
            var prevRevNo = "${param.revNo}";
            var nextRevNo = nextRevisionValue(prevRevNo);
            $dxfPopupField('#revNo').val(nextRevNo);
            $dxfPopupField('#dxfNo').val("${param.dxfNo}");
            $dxfPopupField('#dataName').val("${param.dataName}");
            if ($.trim("${param.treeCd}") !== '') {
                $dxfPopupField('#treeCd').val("${param.treeCd}");
            }
            if ($.trim("${param.businessTypeCd}") !== '') {
                $dxfPopupField('#businessTypeCd').val("${param.businessTypeCd}");
                if (!$.trim($dxfPopupField('#treeCd').val() || '')) {
                    $dxfPopupField('#treeCd').val("${param.businessTypeCd}");
                }
            }
            $dxfPopupField('#businessTypeCd').prop('disabled', true);
            if ($.trim("${param.pmpcbIssueDt}") !== '') {
                $dxfPopupField('#pmpcbIssueDt').val("${param.pmpcbIssueDt}");
            }
            if ($.trim("${param.distributeTypeCd}") !== '') {
                $dxfPopupField('#distributeTypeCd').val("${param.distributeTypeCd}");
            }
        }

        $('#dxfRegisFile').change(function() {
            var fullFilename = $(this).val().split('\\').pop();
            var lastIndex = fullFilename.lastIndexOf('.');
            var filenameWithoutExtension = lastIndex >= 0 ? fullFilename.substring(0, lastIndex) : fullFilename;

            $('#fileName').val(fullFilename);
            if ($('#dataName').val() === '') {
                $('#dataName').val(filenameWithoutExtension);
            }
        });

        $dxfPopupField('#dxfSubFiles').change(function() {
            var files = this.files || [];
            var names = [];
            for (var i = 0; i < files.length; i++) {
                names.push(files[i].name);
            }
            $dxfPopupField('#subFileNames').val(names.join(', '));
        });

        $dxfPopupField('#businessTypeCd').on('change', function() {
            $dxfPopupField('#treeCd').val($(this).val() || '');
            updateDxfNoPreview();
        });
        if (!$.trim($dxfPopupField('#treeCd').val() || '')) {
            $dxfPopupField('#treeCd').val($dxfPopupField('#businessTypeCd').val() || '');
        }

        initDxfPopupSelect2();
        setTimeout(initDxfPopupSelect2, 0);
        if (IS_DXF_REVISION_UPDATE) {
            var swTypeRaw = "${param.swTypeCd}";
            if ($.trim(swTypeRaw) !== '') {
                $dxfPopupField('#swTypeCd').val(swTypeRaw.split(',')).trigger('change.select2');
            } else {
                $dxfPopupField('#swTypeCd').trigger('change.multiPlaceholder');
            }
            var reviewerRaw = "${param.reviewerUser}";
            if ($.trim(reviewerRaw) !== '') {
                $dxfPopupField('#reviewerUser').val(reviewerRaw.split(',')).trigger('change.select2');
            } else {
                $dxfPopupField('#reviewerUser').trigger('change.multiPlaceholder');
            }
        } else {
            updateDxfNoPreview();
        }
        bindDxfFileDragDrop('#dxfRegisFile', false);
        bindDxfFileDragDrop('#dxfSubFiles', true);
    });
</script>

<div class="dialogContent dxfRegisterPopup popup-base popup-actions-center">
    <div class="popupHero">
        <h2>등록 정보</h2>
        <p>필수 항목을 입력하고 파일을 첨부한 뒤 등록해 주세요.</p>
    </div>

    <form id="formDxfRegisterPopup">
        <input type="hidden" name="treeCd" id="treeCd" value="${treeCd}" />
        <input type="hidden" name="registerUser" id="registerUser" value="${registerUser}" />
        <ul class="section popupCard popupFormGrid">
            <li>
                <label>발행자</label><input type="text" id="registerUserView" value="${registerUser}" readonly>
            </li>
            <li>
                <label>PMPCB 개최일</label><input type="date" id="pmpcbIssueDt" name="pmpcbIssueDt" value="${date}">
            </li>
            <li class="full">
                <label>PMPCB 제목</label>
                <input type="text" name="dataName" id="dataName" value=""/>
            </li>
            <li class="half">
                <label>Level</label>
                <select id="businessTypeCd" name="businessTypeCd">
                    <option value="">선택</option>
                    <c:forEach var="item" items="${businessTypeCd}">
                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ? '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty item.comboTooltip ? '' : ')'}</option>
                    </c:forEach>
                </select>
            </li>

            <li class="half">
                <label>Revision</label>
                <input type="text" id="revNo" name="revNo" value="00" readonly>
            </li>
            
            <li class="full">
                <label>문서번호</label>
                <input type="text" id="dxfNo" name="dxfNo" value="" readonly />
            </li>
            <li class="half" style="display:none;">
                <label>등록번호</label>
                <input type="text" id="registerNo" name="registerNo" value="" maxlength="3" />
            </li>
            <li class="half">
                <label>보드 멤버</label>
                <select id="swTypeCd" name="swTypeCd" multiple="multiple" data-placeholder="선택">
                    <option value="__NONE__" selected>선택</option>
                    <c:forEach var="item" items="${swTypeCd}">
                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ? '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty item.comboTooltip ? '' : ')'}</option>
                    </c:forEach>
                </select>
            </li>
            <li class="half">
                <label>참여자</label>
                <select id="reviewerUser" name="reviewerUser" multiple="multiple" data-placeholder="선택">
                    <option value="__NONE__" selected>선택</option>
                    <c:forEach var="item" items="${swTypeCd}">
                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ? '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty item.comboTooltip ? '' : ')'}</option>
                    </c:forEach>
                </select>
            </li>
            <li class="half" style="display:none;">
                <label>문서유형</label>
                <select id="distributeTypeCd" name="distributeTypeCd">
                    <option value="">선택</option>
                    <c:forEach var="item" items="${distributeTypeCd}">
                        <option value="${item.comboVal}">${item.comboLabel}${empty item.comboTooltip ? '' : ' ('}${empty item.comboTooltip ? '' : item.comboTooltip}${empty item.comboTooltip ? '' : ')'}</option>
                    </c:forEach>
                </select>
            </li>
        </ul>
    </form>

    <ul class="section popupCard popupFormGrid uploadOnly">
        <li class="singleFileUpload full">
            <label>주파일 등록</label>
            <div class="uploadLine" title="파일을 드래그하여 첨부하거나 업로드 버튼을 클릭하세요.">
                <form id="fileForm" name="fileForm" enctype="multipart/form-data">
                    <input type="file" id="dxfRegisFile" name="dxfRegisFile" onchange="fileChange()" style="display: none;" />
                </form>
                <div class="fileNameWrap">
                    <input type="text" name="fileName" id="fileName" placeholder="첨부 버튼 클릭 또는 드래그앤드롭" readonly/>
                    <button type="button" class="fileClearBtn" title="첨부파일 초기화" onclick="clearDxfSelectedFile()">×</button>
                </div>
                <button class="ui-button ui-corner-all fileUploadBtn" onclick="fileUpload()"></button>
            </div>
        </li>
        <li class="singleFileUpload full">
            <label>보조파일 등록</label>
            <div class="uploadLine" title="파일을 드래그하여 첨부하거나 업로드 버튼을 클릭하세요.">
                <form id="subFileForm" name="subFileForm" enctype="multipart/form-data">
                    <input type="file" id="dxfSubFiles" name="dxfSubFiles" multiple="multiple" style="display: none;" />
                </form>
                <div class="fileNameWrap">
                    <input type="text" name="subFileNames" id="subFileNames" class="subFileNames" placeholder="보조파일 첨부 버튼 클릭 또는 드래그앤드롭" readonly/>
                    <button type="button" class="fileClearBtn" title="보조파일 초기화" onclick="clearDxfSubSelectedFiles()">×</button>
                </div>
                <button class="ui-button ui-corner-all fileUploadBtn" type="button" onclick="$('#dxfSubFiles').click();"></button>
            </div>
        </li>
    </ul>

    <div class="dialogBtnSet">
        <div class="left"></div>
        <div class="right">
            <custom:popupButton function="saveX()" name="save" label="btn.register" id="save"/>
            <custom:popupButton function="closePopup('popupDialog')" name="close" label="btn.close" id="close"/>
        </div>
    </div>
</div>
