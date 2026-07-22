var function1 = [];
var function2 = [];
var docTypes = [];
var selected1 = null;
var selected2 = null;
var selectedDoc = null;
var currentManageType = 'LEVEL';

$(function () {
    applyModeUi();
    loadFunction1();
});

function switchManageMode(mode) {
    if (mode !== 'DOC' && mode !== 'LEVEL') return;
    if (currentManageType === mode) return;
    currentManageType = mode;
    selected1 = null;
    selected2 = null;
    selectedDoc = null;
    function1 = [];
    function2 = [];
    docTypes = [];
    applyModeUi();
    loadFunction1();
}

function applyModeUi() {
    var $function1Col = $('#function1List').closest('.col');
    var $function2Col = $('#function2List').closest('.col');
    var $leftTreePanel = $('#leftTreePanel');

    $('#modeDocBtn').toggleClass('active', currentManageType === 'DOC');
    $('#modeLevelBtn').toggleClass('active', currentManageType === 'LEVEL');

    if (currentManageType === 'LEVEL') {
        $('.system-manage-page').addClass('level-mode');
        $('#treeMainTitle').text('Level');
/*         $('#treeCol1Title').text('상위 Level');
        $('#treeCol2Title').text('하위 Level'); */
        $('#levelActions').css('visibility', 'visible');
        $function1Col.show();
        $function2Col.show();
        $('#docTypePanel').hide();
        $leftTreePanel.removeClass('level-flat');
        $('#leftTreePanel .codes').css('grid-template-columns', '1fr 1fr');
        $('#treeManageLayout').css('grid-template-columns', '1fr');
    } else {
        $('.system-manage-page').removeClass('level-mode');
        $('#treeMainTitle').text('Function Code');
        $('#treeCol1Title').text('Function Code 1');
        $('#treeCol2Title').text('Function Code 2');
        $('#levelActions').css('visibility', 'visible');
        $function1Col.show();
        $function2Col.show();
        $('#docTypePanel').show();
        $leftTreePanel.removeClass('level-flat');
        $('#leftTreePanel .codes').css('grid-template-columns', '1fr 1fr');
        $('#treeManageLayout').css('grid-template-columns', '2fr 1fr');
    }
}

function renderFunction1List() {
    renderList('function1List', function1, selected1, function (id) {
        selected1 = id;
        selected2 = null;
        selectedDoc = null;
        renderFunction1List();
        loadFunction2();
    });
}

function renderFunction2List() {
    renderList('function2List', function2, selected2, function (id) {
        selected2 = id;
        selectedDoc = null;
        renderFunction2List();
        if (currentManageType === 'LEVEL') {
            docTypes = [];
            renderDocTypeList();
            return;
        }
        loadDocTypes();
    });
}

function renderDocTypeList() {
    renderList('docTypeList', docTypes, selectedDoc, function (id) {
        selectedDoc = id;
        renderDocTypeList();
    });
}

function renderList(targetId, items, selectedId, clickFn) {
    var html = [];
    $.each(items, function () {
        var active = this.id === selectedId ? ' active' : '';
        var label = this.text || this.id;
        html.push('<div class="list-item' + active + '" data-id="' + this.id + '">' + label + '</div>');
    });
    $('#' + targetId).html(html.join(''));
    $('#' + targetId + ' .list-item').on('click', function () {
        clickFn($(this).data('id'));
    });
}

function loadFunction1() {
    callAjax({ manageType: currentManageType }, '/inside/system/treemanage/function1/list', function (response) {
        function1 = response || [];
        renderFunction1List();
        loadFunction2();
    });
}

function loadFunction2() {
    if (!selected1) {
        function2 = [];
        renderFunction2List();
        loadDocTypes();
        return;
    }

    callAjax({ parentTreeCd: selected1, manageType: currentManageType }, '/inside/system/treemanage/function2/list', function (response) {
        function2 = response || [];
        if (selected2 && function2.filter(function (x) { return x.id === selected2; }).length === 0) selected2 = null;

        renderFunction2List();
        loadDocTypes();
    });
}

function loadDocTypes() {
    if (currentManageType === 'LEVEL') {
        docTypes = [];
        renderDocTypeList();
        return;
    }
    if (!selected2) {
        docTypes = [];
        renderDocTypeList();
        return;
    }

    callAjax({ parentTreeCd: selected2, manageType: currentManageType }, '/inside/system/treemanage/doctype/list', function (response) {
        docTypes = response || [];
        if (selectedDoc && docTypes.filter(function (x) { return x.id === selectedDoc; }).length === 0) selectedDoc = null;

        renderDocTypeList();
    });
}

function openNodeDialog(options) {
    function escAttr(v) {
        return String(v || '')
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }
    function escHtml(v) {
        return String(v || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    var includeCode = !!options.includeCode;
    var codeLabel = options.codeLabel || '코드';
    var nameLabel = options.nameLabel || '명칭';
    var nameFirst = !!options.nameFirst;
    var readOnlyCode = !!options.readOnlyCode;
    var namePlaceholder = options.namePlaceholder || '';
    var codePlaceholder = options.codePlaceholder || '';
    var headerText = options.title || '입력';
    var html = [];
    html.push('<div class="tree-node-dialog" style="padding:14px 10px 8px 10px;">');
    html.push('<div style="font-size:18px;font-weight:700;line-height:1.2;margin:0 0 14px 2px;color:#2f3542;">' + escHtml(headerText) + '</div>');
    function addRow(label, inputId, value, extraAttr, marginBottom, placeholder) {
        html.push('<div style="display:flex;align-items:center;gap:12px;' + (marginBottom ? 'margin-bottom:16px;' : '') + '">');
        html.push('<label style="display:inline-block;width:56px;flex:0 0 56px;">' + escHtml(label) + '</label>');
        html.push('<input type="text" id="' + inputId + '" class="ui-widget-content ui-corner-all" style="width:100%;height:34px;padding:6px 10px;box-sizing:border-box;" placeholder="' + escAttr(placeholder || '') + '" value="' + escAttr(value) + '" ' + (extraAttr || '') + '>');
        html.push('</div>');
    }

    if (includeCode && nameFirst) {
        addRow(nameLabel, 'treeNodeNameInput', options.name, '', true, namePlaceholder);
        addRow(codeLabel, 'treeNodeCodeInput', options.code, readOnlyCode ? 'readonly' : '', false, codePlaceholder);
    } else {
        if (includeCode) addRow(codeLabel, 'treeNodeCodeInput', options.code, readOnlyCode ? 'readonly' : '', true, codePlaceholder);
        addRow(nameLabel, 'treeNodeNameInput', options.name, '', false, namePlaceholder);
    }
    html.push('</div>');

    var $oldDlg = $('#treeNodeEditDialog');
    if ($oldDlg.length && $oldDlg.hasClass('ui-dialog-content')) {
        $oldDlg.dialog('destroy').remove();
    } else if ($oldDlg.length) {
        $oldDlg.remove();
    }

    var $dlg = $('<div id="treeNodeEditDialog"></div>').html(html.join('')).dialog({
        title: options.title || '입력',
        modal: true,
        appendTo: 'body',
        width: 520,
        minHeight: 0,
        height: 'auto',
        resizable: false,
        draggable: true,
        position: { my: 'center', at: 'center', of: window },
        open: function () {
            var $widget = $dlg.dialog('widget');
            $widget.css('max-width', '92vw');
            $dlg.css('overflow', 'visible');
            $widget.find('.ui-dialog-content').css('padding', '0');
            $widget.find('.ui-dialog-buttonpane').css({
                padding: '14px 18px 14px 18px',
                marginTop: '16px'
            });
            $widget.find('.ui-dialog-buttonset button').css({
                minWidth: '56px',
                height: '36px'
            });
            setTimeout(function () {
                if (includeCode) $('#treeNodeCodeInput').focus();
                else $('#treeNodeNameInput').focus();
            }, 0);
        },
        close: function () {
            var $this = $(this);
            setTimeout(function () {
                if ($this.hasClass('ui-dialog-content')) {
                    $this.dialog('destroy');
                }
                $this.remove();
            }, 0);
        },
        buttons: [
            {
                text: '저장',
                click: function () {
                    var code = includeCode ? $.trim($('#treeNodeCodeInput').val()) : (options.code || '');
                    var name = $.trim($('#treeNodeNameInput').val());
                    if (includeCode && !code) {
                        alertMessage('코드를 입력해 주세요.');
                        return;
                    }
                    if (!name) {
                        alertMessage('명칭을 입력해 주세요.');
                        return;
                    }
                    options.onSubmit(code, name);
                    $(this).dialog('close');
                }
            },
            {
                text: '닫기',
                click: function () {
                    $(this).dialog('close');
                }
            }
        ]
    });
}

function toFunctionTreeCd(numberText) {
    var number = $.trim(numberText || '').toUpperCase();
    if (number.indexOf('FC') === 0) return number;
    return 'FC' + number;
}

function toFunctionNumber(treeCd) {
    var cd = $.trim(treeCd || '').toUpperCase();
    return cd.indexOf('FC') === 0 ? cd.substring(2) : cd;
}

function toFunctionTitle(treeCd, treeNm) {
    var number = toFunctionNumber(treeCd);
    var name = $.trim(treeNm || '');
    var prefix = number + ' ';
    if (name.indexOf(prefix) === 0) return $.trim(name.substring(prefix.length));
    return name;
}

function composeFunctionTreeNm(numberText, titleText) {
    return $.trim(numberText || '') + ' ' + $.trim(titleText || '');
}

function normalizeDocTypeName(name) {
    return $.trim(name || '').toUpperCase();
}

function composeDocTypeTreeCd(parentTreeCd, docTypeName) {
    return $.trim(parentTreeCd || '') + '_' + normalizeDocTypeName(docTypeName);
}

function isValidFunctionNumber(numberText) {
    return /^[0-9]+$/.test($.trim(numberText || ''));
}

function isValidFunctionTitle(titleText) {
    return $.trim(titleText || '').length > 0;
}

function isValidDocTypeName(name) {
    return /^[A-Z0-9]+$/.test(normalizeDocTypeName(name));
}

function addFunctionCode(parentTreeCd, doneFn, placeholders) {
    var titleText = 'Function Code 생성';
    placeholders = placeholders || {};
    openNodeDialog({
        title: titleText,
        includeCode: true,
        codeLabel: '번호',
        nameLabel: 'Title',
        nameFirst: true,
        namePlaceholder: placeholders.name || 'ex) Program Management',
        codePlaceholder: placeholders.code || 'ex) 100',
        onSubmit: function (number, title) {
            var n = $.trim(number);
            var t = $.trim(title);
            if (!isValidFunctionNumber(n)) {
                alertMessage('번호는 숫자만 입력해 주세요.');
                return;
            }
            if (!isValidFunctionTitle(t)) {
                alertMessage('Title을 입력해 주세요.');
                return;
            }
            callAjax({
                treeCd: '',
                functionCd: toFunctionTreeCd(n),
                treeNm: composeFunctionTreeNm(n, t),
                parentTreeCd: parentTreeCd,
                manageType: currentManageType
            }, '/inside/system/treemanage/node/add', function (response) {
                if (response.success) {
                    alertMessage('등록되었습니다.');
                    doneFn();
                } else {
                    alertMessage(response.failReason || '등록 실패');
                }
            });
        }
    });
}

function editFunctionCode(treeCd, currentTreeNm, currentFunctionCd, doneFn, placeholders) {
    if (!treeCd) { alertMessage('대상을 선택해 주세요.'); return; }
    var number = toFunctionNumber(currentFunctionCd || treeCd);
    var title = toFunctionTitle(currentFunctionCd || treeCd, currentTreeNm);
    var titleText = 'Function Code 수정';
    placeholders = placeholders || {};
    openNodeDialog({
        title: titleText,
        includeCode: true,
        readOnlyCode: true,
        codeLabel: '번호',
        nameLabel: 'Title',
        nameFirst: true,
        namePlaceholder: placeholders.name || 'ex) Program Management',
        codePlaceholder: placeholders.code || 'ex) 100',
        code: number,
        name: title,
        onSubmit: function (fixedNumber, fixedTitle) {
            if (!isValidFunctionTitle(fixedTitle)) {
                alertMessage('Title을 입력해 주세요.');
                return;
            }
            callAjax({
                treeCd: treeCd,
                treeNm: composeFunctionTreeNm(fixedNumber, fixedTitle),
                manageType: currentManageType
            }, '/inside/system/treemanage/node/update', function (response) {
                if (response.success) {
                    alertMessage('수정되었습니다.');
                    doneFn();
                } else {
                    alertMessage(response.failReason || '수정 실패');
                }
            });
        }
    });
}

function normalizeBoardLevelName(name) {
    return $.trim(name || '');
}

function addBoardLevel(parentTreeCd, doneFn) {
    var isChild = !!parentTreeCd;
    openNodeDialog({
        title: isChild ? '하위 Level 생성' : 'Level 생성',
        includeCode: false,
        nameLabel: 'Level',
        namePlaceholder: isChild ? 'ex) Detail Level' : 'ex) LV1',
        onSubmit: function (code, levelName) {
            var normalized = normalizeBoardLevelName(levelName);
            if (!normalized) {
                alertMessage('Level을 입력해 주세요.');
                return;
            }
            callAjax({
                functionCd: normalized,
                treeCd: '',
                treeNm: normalized,
                parentTreeCd: parentTreeCd || null,
                manageType: currentManageType
            }, '/inside/system/treemanage/node/add', function (response) {
                if (response.success) {
                    alertMessage('등록되었습니다.');
                    doneFn();
                } else {
                    alertMessage(response.failReason || '등록 실패');
                }
            });
        }
    });
}

function editBoardLevel(treeCd, currentTreeNm, doneFn) {
    if (!treeCd) { alertMessage('대상을 선택해 주세요.'); return; }
    openNodeDialog({
        title: 'Level 수정',
        includeCode: false,
        nameLabel: 'Level',
        namePlaceholder: 'ex) LV1',
        name: currentTreeNm || '',
        onSubmit: function (code, levelName) {
            var normalized = normalizeBoardLevelName(levelName);
            if (!normalized) {
                alertMessage('Level을 입력해 주세요.');
                return;
            }
            callAjax({
                treeCd: treeCd,
                treeNm: normalized,
                manageType: currentManageType
            }, '/inside/system/treemanage/node/update', function (response) {
                if (response.success) {
                    alertMessage('수정되었습니다.');
                    doneFn();
                } else {
                    alertMessage(response.failReason || '수정 실패');
                }
            });
        }
    });
}

function addDocTypeCode(parentTreeCd, parentFunctionCd, doneFn) {
    openNodeDialog({
        title: 'Document Type code 추가',
        includeCode: false,
        nameLabel: '명칭',
        namePlaceholder: 'ex) SP',
        onSubmit: function (code, name) {
            var docTypeName = normalizeDocTypeName(name);
            if (!isValidDocTypeName(docTypeName)) {
                alertMessage('Document Type은 영문/숫자만 입력해 주세요.');
                return;
            }
            callAjax({
                treeCd: '',
                functionCd: composeDocTypeTreeCd(parentFunctionCd, docTypeName),
                treeNm: docTypeName,
                parentTreeCd: parentTreeCd,
                manageType: currentManageType
            }, '/inside/system/treemanage/node/add', function (response) {
                if (response.success) {
                    alertMessage('등록되었습니다.');
                    doneFn();
                } else {
                    alertMessage(response.failReason || '등록 실패');
                }
            });
        }
    });
}

function editDocTypeCode(treeCd, currentName, doneFn) {
    if (!treeCd) { alertMessage('대상을 선택해 주세요.'); return; }
    openNodeDialog({
        title: 'Document Type code 수정',
        includeCode: false,
        nameLabel: '명칭',
        namePlaceholder: 'ex) SP',
        name: currentName || '',
        onSubmit: function (code, name) {
            var docTypeName = normalizeDocTypeName(name);
            if (!isValidDocTypeName(docTypeName)) {
                alertMessage('Document Type은 영문/숫자만 입력해 주세요.');
                return;
            }
            callAjax({
                treeCd: treeCd,
                treeNm: docTypeName,
                manageType: currentManageType
            }, '/inside/system/treemanage/node/update', function (response) {
                if (response.success) {
                    alertMessage('수정되었습니다.');
                    doneFn();
                } else {
                    alertMessage(response.failReason || '수정 실패');
                }
            });
        }
    });
}

function doDelete(treeCd, doneFn) {
    if (!treeCd) { alertMessage('대상을 선택해 주세요.'); return; }
    confirmMessage('선택한 항목을 삭제하시겠습니까?', function () {
        $('#confirmMessage').dialog('close');
        callAjax({ treeCd: treeCd, manageType: currentManageType }, '/inside/system/treemanage/node/delete', function (response) {
            if (response.success) {
                alertMessage('삭제되었습니다.');
                doneFn();
            } else {
                alertMessage(response.failReason || '삭제 실패');
            }
        });
    });
}

function getItemById(list, id) {
    var found = null;
    $.each(list, function () { if (this.id === id) { found = this; return false; } });
    return found;
}

function addFunction1() {
    if (currentManageType === 'LEVEL') {
        addBoardLevel(null, loadFunction1);
        return;
    }
    addFunctionCode('ROOT', loadFunction1, { name: 'ex) Program Management', code: 'ex) 100' });
}
function editFunction1() {
    var item = getItemById(function1, selected1);
    if (currentManageType === 'LEVEL') {
        editBoardLevel(selected1, item ? item.text : '', loadFunction1);
        return;
    }
    editFunctionCode(selected1, item ? item.text : '', item ? item.functionCd : '', loadFunction1, { name: 'ex) Program Management', code: 'ex) 100' });
}
function deleteFunction1() { doDelete(selected1, function () { selected1 = null; selected2 = null; selectedDoc = null; loadFunction1(); }); }

function addFunction2() {
    if (!selected1) {
        alertMessage(currentManageType === 'LEVEL' ? '상위 Level을 먼저 선택해 주세요.' : 'Function Code 1을 먼저 선택해 주세요.');
        return;
    }
    if (currentManageType === 'LEVEL') {
        addBoardLevel(selected1, loadFunction2);
        return;
    }
    addFunctionCode(selected1, loadFunction2, { name: 'ex) Program Office', code: 'ex) 110' });
}
function editFunction2() {
    if (currentManageType === 'LEVEL') {
        var levelItem = getItemById(function2, selected2);
        editBoardLevel(selected2, levelItem ? levelItem.text : '', loadFunction2);
        return;
    }
    var item = getItemById(function2, selected2);
    editFunctionCode(selected2, item ? item.text : '', item ? item.functionCd : '', loadFunction2, { name: 'ex) Program Office', code: 'ex) 110' });
}
function deleteFunction2() {
    if (currentManageType === 'LEVEL') {
        doDelete(selected2, function () { selected2 = null; loadFunction2(); });
        return;
    }
    doDelete(selected2, function () { selected2 = null; selectedDoc = null; loadFunction2(); });
}

function addDocType() {
    if (!selected2) { alertMessage('Function Code 2를 먼저 선택해 주세요.'); return; }
    var parentItem = getItemById(function2, selected2);
    addDocTypeCode(selected2, parentItem ? parentItem.functionCd : selected2, loadDocTypes);
}
function editDocType() {
    var item = getItemById(docTypes, selectedDoc);
    editDocTypeCode(selectedDoc, item ? item.text : '', loadDocTypes);
}
function deleteDocType() { doDelete(selectedDoc, function () { selectedDoc = null; loadDocTypes(); }); }
