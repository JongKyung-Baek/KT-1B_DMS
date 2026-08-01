(function (window, document) {
    'use strict';

    var config = window.distributionWorkflowPage || {};
    var page = document.querySelector('.distribution-workflow-page');
    if (!page) return;

    var mode = config.mode || page.getAttribute('data-workflow-mode') || 'mine';
    var apiBase = (config.contextPath || '') + '/general/distribution/workflow/api';
    var state = {
        records: [],
        current: null,
        busy: false,
        lastFocused: null,
        partners: [],
        approvers: [],
        partnerUsers: {}
    };
    var parentCategoryMessageKeys = {
        TRB000002: 'drawing',
        TRB000003: 'spec',
        TRB000004: 'sow',
        TRB000005: 'sdrl',
        TRB000006: 'programData',
        TRB000007: 'sro',
        TRB000008: 'testProcedure',
        TRB000009: 'engineeringMemo',
        TRB000010: 'sourceData',
        TRB000011: 'etc',
        TRB000012: 'mfgData'
    };

    function element(id) {
        return document.getElementById(id);
    }

    function t(key, fallback) {
        var args = Array.prototype.slice.call(arguments, 2);
        if (window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
            return window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
        }
        if (args.length && typeof fallback === 'string') {
            return fallback.replace(/\{(\d+)\}/g, function (match, index) {
                return typeof args[Number(index)] === 'undefined' ? match : args[Number(index)];
            });
        }
        return fallback || key;
    }

    function text(value, fallback) {
        if (value === null || typeof value === 'undefined' || String(value).trim() === '') {
            return typeof fallback === 'undefined' ? '-' : fallback;
        }
        return String(value);
    }

    function append(parent, tagName, className, value) {
        var node = document.createElement(tagName);
        if (className) node.className = className;
        if (typeof value !== 'undefined') node.textContent = text(value);
        parent.appendChild(node);
        return node;
    }

    function formatCount(count) {
        return t('feature.common.count', '{0}건', count);
    }

    function formatDate(value) {
        if (!value) return '-';
        return String(value).replace('T', ' ').replace(/\.\d+$/, '');
    }

    function formatBytes(value) {
        var bytes = Number(value);
        if (!Number.isFinite(bytes) || bytes < 0) return '-';
        if (bytes < 1024) return bytes + ' B';
        var units = ['KB', 'MB', 'GB', 'TB'];
        var size = bytes;
        var unit = -1;
        do {
            size /= 1024;
            unit += 1;
        } while (size >= 1024 && unit < units.length - 1);
        return size.toFixed(size >= 10 ? 1 : 2).replace(/\.0+$/, '') + ' ' + units[unit];
    }

    function statusLabel(status) {
        var labels = {
            DRAFT: '작성중',
            PENDING_APPROVAL: '승인대기',
            APPROVED: '승인완료',
            REJECTED: '반려',
            CANCELLED: '취소',
            EXPIRED: '배포만료'
        };
        return t('feature.distributionWorkflow.status.' + status, labels[status] || text(status));
    }

    function statusChip(status) {
        var chip = document.createElement('span');
        chip.className = 'dw-status-chip dw-status--' + String(status || 'draft').toLowerCase().replace(/_/g, '-');
        chip.textContent = statusLabel(status);
        return chip;
    }

    function personCell(name, id) {
        var wrapper = document.createElement('div');
        wrapper.className = 'dw-cell';
        append(wrapper, 'span', 'dw-cell__primary', name || id || '-');
        if (id && id !== name) append(wrapper, 'span', 'dw-cell__secondary', id);
        return wrapper;
    }

    function titleCell(record) {
        var wrapper = document.createElement('div');
        wrapper.className = 'dw-cell';
        append(wrapper, 'span', 'dw-cell__primary', record.title);
        if (record.purpose) append(wrapper, 'span', 'dw-cell__secondary', record.purpose);
        return wrapper;
    }

    function targetCell(record) {
        var wrapper = document.createElement('div');
        wrapper.className = 'dw-cell';
        append(wrapper, 'span', 'dw-cell__primary', record.partnerCompanyName || '-');
        append(wrapper, 'span', 'dw-cell__secondary',
            t('feature.distributionWorkflow.label.recipientCount', '수신자 {0}명',
                Number(record.recipientCount) || 0));
        return wrapper;
    }

    function periodCell(record) {
        var wrapper = document.createElement('div');
        wrapper.className = 'dw-cell';
        append(wrapper, 'span', 'dw-cell__primary',
            text(record.distributionStartDate) + ' ~ ' + text(record.distributionEndDate));
        if (record.status === 'APPROVED' && record.remainingDays !== null
                && typeof record.remainingDays !== 'undefined') {
            append(wrapper, 'span', 'dw-cell__secondary',
                t('feature.distributionWorkflow.label.remainingDays', '잔여 {0}일', record.remainingDays));
        }
        return wrapper;
    }

    function setMessage(target, message, type) {
        if (!target) return;
        target.hidden = !message;
        target.className = 'dw-alert' + (type ? ' dw-alert--' + type : '');
        target.textContent = message || '';
    }

    function showPageMessage(message, type) {
        setMessage(element('workflowPageMessage'), message, type);
    }

    function showDialogMessage(message, type) {
        setMessage(element('workflowDialogMessage'), message, type);
    }

    function request(path, options) {
        var requestOptions = options || {};
        var headers = requestOptions.headers || {};
        headers.Accept = 'application/json';
        if (requestOptions.body) headers['Content-Type'] = 'application/json';
        requestOptions.headers = headers;
        requestOptions.credentials = 'same-origin';

        return window.fetch(apiBase + path, requestOptions).then(function (response) {
            return response.text().then(function (raw) {
                var body = null;
                if (raw) {
                    try {
                        body = JSON.parse(raw);
                    } catch (ignore) {
                        body = null;
                    }
                }
                if (!response.ok) {
                    var fallback = response.status === 403
                        ? t('feature.distributionWorkflow.message.accessDenied', '이 기능을 사용할 권한이 없습니다.')
                        : t('feature.distributionWorkflow.message.actionFailed', '요청을 처리하지 못했습니다.');
                    var error = new Error(body && body.message ? body.message : fallback);
                    error.status = response.status;
                    error.code = body && body.code ? body.code : '';
                    throw error;
                }
                if (body === null && raw) {
                    throw new Error(t('feature.distributionWorkflow.message.actionFailed', '요청을 처리하지 못했습니다.'));
                }
                return body;
            });
        });
    }

    function endpointForList() {
        if (mode === 'approval') return '/approval-queue?limit=100&offset=0';
        if (mode === 'approved') return '/approved?limit=100&offset=0';
        var status = element('workflowStatusFilter') ? element('workflowStatusFilter').value : '';
        return '/requests?limit=100&offset=0' + (status ? '&status=' + encodeURIComponent(status) : '');
    }

    function loadingRow() {
        var body = element('workflowTableBody');
        body.textContent = '';
        var row = append(body, 'tr', 'dw-loading-row');
        var cell = append(row, 'td');
        cell.colSpan = 8;
        append(cell, 'span', 'dw-spinner');
        cell.appendChild(document.createTextNode(
            t('feature.distributionWorkflow.message.loading', '자료를 불러오는 중입니다.')));
    }

    function emptyMessage() {
        if (mode === 'approval') {
            return t('feature.distributionWorkflow.message.emptyApproval', '승인대기 중인 배포요청이 없습니다.');
        }
        if (mode === 'approved') {
            return t('feature.distributionWorkflow.message.emptyApproved', '승인완료된 배포목록이 없습니다.');
        }
        return t('feature.distributionWorkflow.message.emptyMine', '등록된 배포요청이 없습니다.');
    }

    function searchable(record) {
        return [record.requestNo, record.title, record.purpose, record.status,
            record.requestedByUserId, record.requestedByUserNm, record.requestedDeptNm,
            record.decidedByUserId, record.decidedByUserNm, record.partnerCompanyCode,
            record.partnerCompanyName, record.approverUserId, record.approverUserNm,
            record.distributionStartDate, record.distributionEndDate].map(function (value) {
                return value == null ? '' : String(value).toLocaleLowerCase();
            }).join(' ');
    }

    function filteredRecords() {
        var keywordInput = element('workflowKeyword');
        var keyword = keywordInput ? keywordInput.value.trim().toLocaleLowerCase() : '';
        if (!keyword) return state.records.slice();
        return state.records.filter(function (record) {
            return searchable(record).indexOf(keyword) >= 0;
        });
    }

    function detailButton(requestId) {
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'dw-row-action';
        button.setAttribute('data-request-id', requestId);
        var icon = append(button, 'i', 'icon-base ti tabler-eye');
        icon.setAttribute('aria-hidden', 'true');
        button.appendChild(document.createTextNode(
            t('feature.distributionWorkflow.action.viewDetail', '상세보기')));
        return button;
    }

    function requestNumberButton(record) {
        var button = document.createElement('button');
        button.type = 'button';
        button.className = 'dw-request-link';
        button.setAttribute('data-request-id', record.requestId);
        button.textContent = text(record.requestNo);
        return button;
    }

    function cellWith(row, value) {
        var cell = append(row, 'td');
        if (value && value.nodeType) cell.appendChild(value);
        else cell.textContent = text(value);
        return cell;
    }

    function renderMineRow(record) {
        var row = document.createElement('tr');
        cellWith(row, requestNumberButton(record));
        cellWith(row, titleCell(record));
        cellWith(row, targetCell(record));
        cellWith(row, statusChip(record.status));
        cellWith(row, formatCount(Number(record.itemCount) || 0));
        cellWith(row, periodCell(record));
        cellWith(row, personCell(record.approverUserNm, record.approverUserId));
        cellWith(row, detailButton(record.requestId));
        return row;
    }

    function renderApprovalRow(record) {
        var row = document.createElement('tr');
        cellWith(row, requestNumberButton(record));
        cellWith(row, titleCell(record));
        cellWith(row, personCell(record.requestedByUserNm, record.requestedByUserId));
        cellWith(row, targetCell(record));
        cellWith(row, periodCell(record));
        cellWith(row, formatCount(Number(record.itemCount) || 0));
        cellWith(row, formatDate(record.submittedAt));
        cellWith(row, detailButton(record.requestId));
        return row;
    }

    function renderApprovedRow(record) {
        var row = document.createElement('tr');
        cellWith(row, requestNumberButton(record));
        cellWith(row, titleCell(record));
        cellWith(row, targetCell(record));
        cellWith(row, periodCell(record));
        cellWith(row, formatCount(Number(record.itemCount) || 0));
        cellWith(row, personCell(record.decidedByUserNm, record.decidedByUserId));
        cellWith(row, formatDate(record.decidedAt));
        cellWith(row, detailButton(record.requestId));
        return row;
    }

    function renderRecords() {
        var records = filteredRecords();
        var body = element('workflowTableBody');
        body.textContent = '';
        element('workflowCount').textContent = formatCount(records.length);
        if (!records.length) {
            var emptyRow = append(body, 'tr', 'dw-empty-row');
            var emptyCell = append(emptyRow, 'td', '', emptyMessage());
            emptyCell.colSpan = 8;
            return;
        }
        records.forEach(function (record) {
            body.appendChild(mode === 'approval' ? renderApprovalRow(record)
                : mode === 'approved' ? renderApprovedRow(record) : renderMineRow(record));
        });
    }

    function loadRecords() {
        loadingRow();
        showPageMessage('');
        return request(endpointForList()).then(function (records) {
            state.records = Array.isArray(records) ? records : [];
            renderRecords();
        }).catch(function (error) {
            state.records = [];
            renderRecords();
            showPageMessage(error.message ||
                t('feature.distributionWorkflow.message.loadFailed', '배포요청을 불러오지 못했습니다.'));
        });
    }

    function initialItems() {
        return Array.prototype.map.call(document.querySelectorAll('.dw-initial-item'), function (item) {
            return {
                objectType: item.getAttribute('data-object-type') || 'SW',
                objectId: item.getAttribute('data-object-id') || '',
                fileNo: item.getAttribute('data-file-no') || '',
                materialNo: item.getAttribute('data-material-no') || '',
                materialName: item.getAttribute('data-material-name') || '',
                originalFileName: item.getAttribute('data-file-name') || '',
                fileSize: item.getAttribute('data-file-size') || '',
                gradeCd: item.getAttribute('data-grade-cd') || '',
                gradeNm: item.getAttribute('data-grade-nm') || '',
                treeCd: item.getAttribute('data-tree-cd') || '',
                treeNm: item.getAttribute('data-tree-nm') || '',
                parentTreeCd: item.getAttribute('data-parent-tree-cd') || '',
                parentTreeNm: item.getAttribute('data-parent-tree-nm') || ''
            };
        });
    }

    function categoryOptions(selector) {
        return Array.prototype.map.call(document.querySelectorAll(selector), function (item) {
            return {
                value: item.getAttribute('data-value') || '',
                label: item.getAttribute('data-label') || '',
                parent: item.getAttribute('data-parent') || ''
            };
        });
    }

    function addSelectOption(select, value, label, selected) {
        var option = append(select, 'option', '', label);
        option.value = value;
        option.selected = !!selected;
        return option;
    }

    function resetSelect(select, placeholder) {
        select.textContent = '';
        addSelectOption(select, '', placeholder, true);
    }

    function kstDate(offsetDays) {
        var parts = new Intl.DateTimeFormat('en-CA', {
            timeZone: 'Asia/Seoul', year: 'numeric', month: '2-digit', day: '2-digit'
        }).formatToParts(new Date());
        var values = {};
        parts.forEach(function (part) { values[part.type] = part.value; });
        var date = new Date(Date.UTC(Number(values.year), Number(values.month) - 1,
            Number(values.day) + Number(offsetDays || 0)));
        return date.getUTCFullYear() + '-' + String(date.getUTCMonth() + 1).padStart(2, '0')
            + '-' + String(date.getUTCDate()).padStart(2, '0');
    }

    function addDays(dateValue, days) {
        if (!/^\d{4}-\d{2}-\d{2}$/.test(dateValue || '')) return '';
        var parts = dateValue.split('-').map(Number);
        var date = new Date(Date.UTC(parts[0], parts[1] - 1, parts[2] + Number(days || 0)));
        return date.getUTCFullYear() + '-' + String(date.getUTCMonth() + 1).padStart(2, '0')
            + '-' + String(date.getUTCDate()).padStart(2, '0');
    }

    function partnerLabel(partner) {
        var code = partner && (partner.code || partner.partnerCompanyCode);
        var name = partner && (partner.name || partner.partnerCompanyName);
        return code && name ? code + ' · ' + name : text(name || code);
    }

    function approverLabel(approver) {
        var name = approver && (approver.userName || approver.approverUserNm);
        var id = approver && (approver.userId || approver.approverUserId);
        var department = approver && (approver.deptNm || approver.departmentName);
        var label = name && id ? name + ' (' + id + ')' : text(name || id);
        return department ? label + ' · ' + department : label;
    }

    function populatePartnerOptions(selectedId, selectedLabel) {
        var select = element('workflowPartnerCompany');
        resetSelect(select, t('feature.distributionWorkflow.placeholder.partnerCompany',
            '협력업체를 선택하세요.'));
        var found = false;
        state.partners.forEach(function (partner) {
            var value = String(partner.partnerCompanyId || '');
            var selected = value === String(selectedId || '');
            found = found || selected;
            addSelectOption(select, value, partnerLabel(partner), selected);
        });
        if (selectedId && !found) addSelectOption(select, String(selectedId), selectedLabel || selectedId, true);
    }

    function populateApproverOptions(selectedCd, selectedLabel) {
        var select = element('workflowApprover');
        resetSelect(select, t('feature.distributionWorkflow.placeholder.approver',
            '승인자를 선택하세요.'));
        var found = false;
        state.approvers.forEach(function (approver) {
            var value = String(approver.approverUserCd || '');
            var selected = value === String(selectedCd || '');
            found = found || selected;
            addSelectOption(select, value, approverLabel(approver), selected);
        });
        if (selectedCd && !found) addSelectOption(select, String(selectedCd), selectedLabel || selectedCd, true);
    }

    function recipientLabel(user) {
        var label = text(user.userName);
        if (user.email) label += ' · ' + user.email;
        if (String(user.representativeYn || '').toUpperCase() === 'Y') {
            label += ' · ' + t('feature.distributionWorkflow.label.representative', '대표사용자');
        }
        return label;
    }

    function renderRecipients(users, selectedIds, editable) {
        var target = element('workflowRecipients');
        target.textContent = '';
        var selected = {};
        (selectedIds || []).forEach(function (id) { selected[String(id)] = true; });
        if (!users || !users.length) {
            append(target, 'p', 'dw-recipient-empty',
                t('feature.distributionWorkflow.message.emptyPartnerUsers',
                    '등록된 협력업체 사용자가 없습니다.'));
            return;
        }
        var hasExplicitSelection = !!(selectedIds && selectedIds.length);
        users.forEach(function (user) {
            var id = String(user.partnerUserId || '');
            var label = append(target, 'label', 'dw-recipient-option');
            var checkbox = append(label, 'input');
            checkbox.type = 'checkbox';
            checkbox.value = id;
            checkbox.checked = hasExplicitSelection ? !!selected[id]
                : String(user.representativeYn || '').toUpperCase() === 'Y';
            checkbox.disabled = !editable;
            append(label, 'span', '', recipientLabel(user));
        });
    }

    function loadPartnerUsers(partnerId, selectedIds, editable) {
        var normalized = String(partnerId || '');
        if (!normalized) {
            var target = element('workflowRecipients');
            target.textContent = '';
            append(target, 'p', 'dw-recipient-empty',
                t('feature.distributionWorkflow.message.selectPartnerFirst',
                    '협력업체를 먼저 선택하세요.'));
            return Promise.resolve([]);
        }
        if (state.partnerUsers[normalized]) {
            renderRecipients(state.partnerUsers[normalized], selectedIds, editable);
            return Promise.resolve(state.partnerUsers[normalized]);
        }
        var target = element('workflowRecipients');
        target.textContent = '';
        append(target, 'p', 'dw-recipient-empty',
            t('feature.distributionWorkflow.message.loading', '불러오는 중입니다.'));
        return request('/directory/partners/' + encodeURIComponent(normalized) + '/users')
            .then(function (users) {
                state.partnerUsers[normalized] = Array.isArray(users) ? users : [];
                renderRecipients(state.partnerUsers[normalized], selectedIds, editable);
                return state.partnerUsers[normalized];
            }).catch(function (error) {
                target.textContent = '';
                append(target, 'p', 'dw-recipient-empty', error.message ||
                    t('feature.distributionWorkflow.message.directoryFailed',
                        '협력업체 사용자 목록을 불러오지 못했습니다.'));
                return [];
            });
    }

    function loadDirectory() {
        return Promise.all([
            request('/directory/partners'), request('/directory/approvers')
        ]).then(function (responses) {
            state.partners = Array.isArray(responses[0]) ? responses[0] : [];
            state.approvers = Array.isArray(responses[1]) ? responses[1] : [];
            populatePartnerOptions('', '');
            populateApproverOptions('', '');
        }).catch(function (error) {
            state.partners = [];
            state.approvers = [];
            populatePartnerOptions('', '');
            populateApproverOptions('', '');
            showPageMessage(error.message ||
                t('feature.distributionWorkflow.message.directoryFailed',
                    '협력업체와 승인자 목록을 불러오지 못했습니다.'));
        });
    }

    function selectionPlaceholder() {
        return t('feature.distributionWorkflow.placeholder.select', '선택');
    }

    function populateParentCategories(select, selectedValue, selectedLabel) {
        resetSelect(select, selectionPlaceholder());
        var found = false;
        categoryOptions('.dw-category-parent-option').forEach(function (category) {
            var selected = category.value === selectedValue;
            var messageKey = parentCategoryMessageKeys[String(category.value || '').toUpperCase()];
            var displayLabel = messageKey
                ? t('feature.techList.tree.category.' + messageKey, category.label)
                : category.label;
            found = found || selected;
            addSelectOption(select, category.value, displayLabel, selected);
        });
        if (selectedValue && !found) {
            addSelectOption(select, selectedValue, selectedLabel || selectedValue, true);
        }
    }

    function populateChildCategories(select, parentValue, selectedValue, selectedLabel) {
        resetSelect(select, selectionPlaceholder());
        var found = false;
        categoryOptions('.dw-category-child-option').forEach(function (category) {
            if (category.parent !== parentValue) return;
            var selected = category.value === selectedValue;
            found = found || selected;
            addSelectOption(select, category.value, category.label, selected);
        });
        if (selectedValue && !found) {
            addSelectOption(select, selectedValue, selectedLabel || selectedValue, true);
        }
        select.disabled = !parentValue;
    }

    function clearItemIdentity(row) {
        row.querySelector('.dw-item-object-id').value = '';
        row.querySelector('.dw-item-grade').textContent = '-';
        row.querySelector('.dw-item-bundle').textContent = '-';
        row._selectedBundle = null;
    }

    function materialLabel(item) {
        var number = item.materialNo ? String(item.materialNo) : '';
        var name = item.materialName ? String(item.materialName) : '';
        if (!number) return name || '-';
        return name ? number + ' · ' + name : number;
    }

    function fileKindLabel(objectType) {
        return objectType === 'SW_SUB'
            ? t('feature.distributionWorkflow.label.auxiliaryFile', '보조파일')
            : t('feature.distributionWorkflow.label.primaryFile', '주파일');
    }

    function documentBundles(items) {
        var bundles = {};
        var order = [];
        (items || []).forEach(function (source) {
            if (!source || !source.objectId) return;
            var key = String(source.objectId);
            if (!bundles[key]) {
                bundles[key] = source.files ? source : {
                    objectId: source.objectId,
                    materialNo: source.materialNo,
                    materialName: source.materialName,
                    treeCd: source.treeCd,
                    treeNm: source.treeNm,
                    parentTreeCd: source.parentTreeCd,
                    parentTreeNm: source.parentTreeNm,
                    files: []
                };
                if (!bundles[key].files) bundles[key].files = [];
                order.push(key);
            }
            if (!source.files) bundles[key].files.push(source);
        });
        return order.map(function (key) {
            var bundle = bundles[key];
            var files = bundle.files || [];
            bundle.mainFileCount = Number(bundle.mainFileCount) || files.filter(function (file) {
                return file.objectType === 'SW';
            }).length;
            bundle.subFileCount = Number(bundle.subFileCount) || files.filter(function (file) {
                return file.objectType === 'SW_SUB';
            }).length;
            bundle.totalFileCount = Number(bundle.totalFileCount) || files.length;
            return bundle;
        });
    }

    function bundleGrade(bundle) {
        var labels = {};
        (bundle && bundle.files || []).forEach(function (file) {
            var label = file.gradeNm || file.gradeCd;
            if (label) labels[label] = true;
        });
        var names = Object.keys(labels);
        return names.length ? names.join(', ') : (bundle.gradeNm || bundle.gradeCd || '-');
    }

    function bundleSummary(bundle) {
        if (!bundle) return '-';
        var mainCount = Number(bundle.mainFileCount) || 0;
        var subCount = Number(bundle.subFileCount) || 0;
        var total = Number(bundle.totalFileCount) || mainCount + subCount;
        return t('feature.distributionWorkflow.label.fileBundleSummary',
            '주파일 {0} + 보조파일 {1} · 총 {2}개', mainCount, subCount, total);
    }

    function applySelectedDocument(row, objectId) {
        var selected = null;
        (row._catalog || []).some(function (bundle) {
            if (String(bundle.objectId) !== String(objectId || '')) return false;
            selected = bundle;
            return true;
        });
        clearItemIdentity(row);
        if (!selected) return;
        row.querySelector('.dw-item-object-id').value = selected.objectId;
        row._selectedBundle = selected;
        var grade = bundleGrade(selected);
        var gradeTarget = row.querySelector('.dw-item-grade');
        gradeTarget.textContent = grade;
        gradeTarget.title = grade;
        var bundleTarget = row.querySelector('.dw-item-bundle');
        bundleTarget.textContent = bundleSummary(selected);
        bundleTarget.title = (selected.files || []).map(function (file) {
            return fileKindLabel(file.objectType) + ': ' + text(file.originalFileName);
        }).join('\n');
    }

    function populateDocuments(row, selectedObjectId) {
        var materialSelect = row.querySelector('.dw-item-material-select');
        resetSelect(materialSelect, selectionPlaceholder());
        (row._catalog || []).forEach(function (bundle) {
            addSelectOption(materialSelect, bundle.objectId, materialLabel(bundle),
                String(bundle.objectId) === String(selectedObjectId || ''));
        });
        materialSelect.disabled = !(row._catalog && row._catalog.length);
        applySelectedDocument(row, selectedObjectId || '');
    }

    function loadCatalog(row, treeCd) {
        row._catalog = [];
        populateDocuments(row, '', '');
        if (!treeCd) return;
        var materialSelect = row.querySelector('.dw-item-material-select');
        resetSelect(materialSelect,
            t('feature.distributionWorkflow.message.loading', '불러오는 중'));
        materialSelect.disabled = true;
        request('/catalog?treeCd=' + encodeURIComponent(treeCd)).then(function (items) {
            if (row.querySelector('.dw-item-category-child').value !== treeCd) return;
            row._catalog = documentBundles(Array.isArray(items) ? items : []);
            populateDocuments(row, '', '');
            if (!row._catalog.length) {
                resetSelect(materialSelect,
                    t('feature.distributionWorkflow.message.emptyCategory', '선택 가능한 기술자료 없음'));
                materialSelect.disabled = true;
            }
        }).catch(function (error) {
            showDialogMessage(error.message ||
                t('feature.distributionWorkflow.message.catalogFailed',
                    '기술자료 목록을 불러오지 못했습니다.'));
            resetSelect(materialSelect, selectionPlaceholder());
            materialSelect.disabled = true;
        });
    }

    function addItemRow(item) {
        var body = element('workflowItemEditorBody');
        if (body.children.length >= 200) {
            showDialogMessage(t('feature.distributionWorkflow.message.maxItems',
                '배포 대상은 최대 200개까지 추가할 수 있습니다.'));
            return;
        }
        var source = item || {};
        var row = append(body, 'tr');
        append(row, 'td', 'dw-line-no', body.children.length);

        var parentCell = append(row, 'td');
        var parentSelect = append(parentCell, 'select', 'dw-item-category-parent');
        parentSelect.setAttribute('aria-label',
            t('feature.distributionWorkflow.column.categoryParent', '상위 자료분류'));
        populateParentCategories(parentSelect, source.parentTreeCd || '', source.parentTreeNm || '');

        var childCell = append(row, 'td');
        var childSelect = append(childCell, 'select', 'dw-item-category-child');
        childSelect.setAttribute('aria-label',
            t('feature.distributionWorkflow.column.categoryChild', '하위 자료분류'));
        populateChildCategories(childSelect, source.parentTreeCd || '',
            source.treeCd || '', source.treeNm || '');

        var materialCell = append(row, 'td');
        var materialSelect = append(materialCell, 'select', 'dw-item-material-select');
        materialSelect.setAttribute('aria-label',
            t('feature.distributionWorkflow.column.material', '기술자료'));
        var objectIdInput = append(materialCell, 'input', 'dw-item-object-id');
        objectIdInput.type = 'hidden';

        var gradeCell = append(row, 'td');
        append(gradeCell, 'span', 'dw-grade-chip dw-item-grade', '-');

        var fileCell = append(row, 'td');
        var fileBundle = append(fileCell, 'span', 'dw-item-bundle', '-');
        fileBundle.setAttribute('aria-label',
            t('feature.distributionWorkflow.column.fileBundle', '배포 파일 묶음'));

        var actionCell = append(row, 'td');
        var remove = append(actionCell, 'button', 'dw-icon-button dw-remove-item');
        remove.type = 'button';
        remove.setAttribute('aria-label', t('feature.distributionWorkflow.action.removeItem', '삭제'));
        remove.setAttribute('title', t('feature.distributionWorkflow.action.removeItem', '삭제'));
        var icon = append(remove, 'i', 'icon-base ti tabler-trash');
        icon.setAttribute('aria-hidden', 'true');

        row._catalog = source.objectId ? documentBundles([source]) : [];
        populateDocuments(row, source.objectId || '');

        parentSelect.addEventListener('change', function () {
            populateChildCategories(childSelect, parentSelect.value, '', '');
            row._catalog = [];
            populateDocuments(row, '', '');
        });
        childSelect.addEventListener('change', function () {
            loadCatalog(row, childSelect.value);
        });
        materialSelect.addEventListener('change', function () {
            applySelectedDocument(row, materialSelect.value);
        });
    }

    function renumberItemRows() {
        Array.prototype.forEach.call(element('workflowItemEditorBody').querySelectorAll('tr'),
            function (row, index) {
                var number = row.querySelector('.dw-line-no');
                if (number) number.textContent = index + 1;
            });
    }

    function renderItemEditor(items) {
        element('workflowItemEditorBody').textContent = '';
        var bundles = documentBundles(items || []);
        (bundles.length ? bundles : [{}]).forEach(addItemRow);
        renumberItemRows();
    }

    function renderItemSnapshots(items) {
        var body = element('workflowItemSnapshotBody');
        body.textContent = '';
        (items || []).forEach(function (item, index) {
            var row = append(body, 'tr');
            cellWith(row, item.lineNo || index + 1);
            cellWith(row, item.parentTreeNm);
            cellWith(row, item.treeNm);
            var material = document.createElement('div');
            material.className = 'dw-cell dw-cell--left';
            append(material, 'span', 'dw-cell__primary', item.materialNo);
            append(material, 'span', 'dw-cell__secondary', item.materialName);
            cellWith(row, material);
            var grade = document.createElement('span');
            grade.className = 'dw-grade-chip';
            grade.textContent = text(item.gradeNm || item.gradeCd);
            cellWith(row, grade);
            var file = document.createElement('div');
            file.className = 'dw-cell dw-cell--left';
            append(file, 'span', 'dw-file-kind', fileKindLabel(item.objectType));
            append(file, 'span', 'dw-cell__primary', item.originalFileName);
            cellWith(row, file);
            cellWith(row, item.fileNo);
            cellWith(row, formatBytes(item.fileSize));
        });
        if (!items || !items.length) {
            var emptyRow = append(body, 'tr', 'dw-empty-row');
            var emptyCell = append(emptyRow, 'td', '',
                t('feature.distributionWorkflow.message.itemsRequired', '배포 대상 기술자료가 없습니다.'));
            emptyCell.colSpan = 8;
        }
    }

    function eventLabel(eventType) {
        var labels = {
            CREATE: '생성', UPDATE_DRAFT: '임시저장', SUBMIT: '승인 요청',
            APPROVE: '승인', REJECT: '반려', CANCEL: '취소', EXPIRE: '배포만료'
        };
        return t('feature.distributionWorkflow.event.' + eventType,
            labels[eventType] || text(eventType));
    }

    function renderEvents(events) {
        var section = element('workflowEventsSection');
        var body = element('workflowEventsBody');
        body.textContent = '';
        section.hidden = !events || !events.length;
        (events || []).forEach(function (event) {
            var row = append(body, 'tr');
            var eventCell = append(row, 'td');
            append(eventCell, 'span', 'dw-event-chip', eventLabel(event.eventType));
            var transition = event.fromStatus
                ? statusLabel(event.fromStatus) + ' → ' + statusLabel(event.toStatus)
                : statusLabel(event.toStatus);
            cellWith(row, transition);
            cellWith(row, personCell(event.actorUserNm, event.actorUserId));
            cellWith(row, event.eventComment);
            cellWith(row, formatDate(event.occurredAt));
        });
    }

    function setHidden(id, hidden) {
        var target = element(id);
        if (target) target.hidden = hidden;
    }

    function setEditable(editable) {
        element('workflowTitle').disabled = !editable;
        element('workflowPurpose').disabled = !editable;
        element('workflowPartnerCompany').disabled = !editable;
        element('workflowApprover').disabled = !editable;
        element('workflowDistributionStartDate').disabled = !editable;
        element('workflowDistributionEndDate').disabled = !editable;
        Array.prototype.forEach.call(element('workflowRecipients').querySelectorAll('input'),
            function (input) { input.disabled = !editable; });
        setHidden('workflowAddItem', !editable);
        setHidden('workflowItemEditor', !editable);
        setHidden('workflowItemSnapshot', editable);
    }

    function updateLengthCounters() {
        element('workflowPurposeLength').textContent = element('workflowPurpose').value.length;
        element('workflowDecisionLength').textContent = element('workflowDecisionComment').value.length;
    }

    function resetActionButtons() {
        ['workflowCancelRequest', 'workflowSaveDraft', 'workflowSubmit',
            'workflowReject', 'workflowApprove'].forEach(function (id) {
                setHidden(id, true);
            });
    }

    function showDecisionSummary(comment) {
        var summary = element('workflowDecisionSummary');
        summary.hidden = false;
        element('workflowDecisionSummaryText').textContent = comment ||
            t('feature.distributionWorkflow.message.noDecisionComment', '등록된 승인·반려 의견이 없습니다.');
    }

    function openNew() {
        state.current = null;
        element('workflowDialogTitle').textContent =
            t('feature.distributionWorkflow.dialog.newTitle', '새 배포요청 작성');
        element('workflowTitle').value = '';
        element('workflowPurpose').value = '';
        populatePartnerOptions('', '');
        populateApproverOptions('', '');
        element('workflowDistributionStartDate').value = kstDate(0);
        element('workflowDistributionEndDate').value = kstDate(7);
        loadPartnerUsers('', [], true);
        element('workflowDecisionComment').value = '';
        setHidden('workflowRequestMeta', true);
        setHidden('workflowDecisionSection', true);
        setHidden('workflowDecisionSummary', true);
        setHidden('workflowEventsSection', true);
        resetActionButtons();
        setEditable(true);
        setHidden('workflowSaveDraft', false);
        setHidden('workflowSubmit', false);
        renderItemEditor(initialItems());
        showDialogMessage('');
        updateLengthCounters();
        showDialog(element('workflowCreateButton'));
    }

    function configureDetail(detail) {
        var record = detail && detail.request ? detail.request : {};
        var items = detail && Array.isArray(detail.items) ? detail.items : [];
        var events = detail && Array.isArray(detail.events) ? detail.events : [];
        state.current = detail;
        element('workflowTitle').value = record.title || '';
        element('workflowPurpose').value = record.purpose || '';
        populatePartnerOptions(record.partnerCompanyId,
            partnerLabel({code: record.partnerCompanyCode, name: record.partnerCompanyName}));
        populateApproverOptions(record.approverUserCd,
            approverLabel({userName: record.approverUserNm, userId: record.approverUserId}));
        element('workflowDistributionStartDate').value = record.distributionStartDate || '';
        element('workflowDistributionEndDate').value = record.distributionEndDate || '';
        element('workflowDecisionComment').value = '';
        element('workflowRequestNo').textContent = text(record.requestNo);
        var statusTarget = element('workflowRequestStatus');
        statusTarget.className = statusChip(record.status).className;
        statusTarget.textContent = statusLabel(record.status);
        element('workflowRequester').textContent = text(record.requestedByUserNm || record.requestedByUserId);
        element('workflowCreatedAt').textContent = formatDate(record.createdAt);
        setHidden('workflowRequestMeta', false);
        setHidden('workflowDecisionSection', true);
        setHidden('workflowDecisionSummary', true);
        resetActionButtons();
        renderEvents(events);
        var dialogNotice = '';

        var editable = mode === 'mine' && (record.status === 'DRAFT' || record.status === 'REJECTED');
        setEditable(editable);
        loadPartnerUsers(record.partnerCompanyId,
            (detail.recipients || []).map(function (recipient) { return recipient.partnerUserId; }),
            editable);
        if (editable) {
            renderItemEditor(detail.documents && detail.documents.length ? detail.documents : items);
        } else {
            renderItemSnapshots(items);
        }

        if (mode === 'approval') {
            element('workflowDialogTitle').textContent =
                t('feature.distributionWorkflow.dialog.approvalTitle', '배포요청 승인 검토');
            if (record.status === 'PENDING_APPROVAL') {
                setHidden('workflowDecisionSection', false);
                if (record.requestedByUserCd && window.USER_CD
                        && record.requestedByUserCd === window.USER_CD) {
                    dialogNotice = t('feature.distributionWorkflow.footer.selfApproval',
                        '요청자는 자신의 배포요청을 승인하거나 반려할 수 없습니다.');
                } else if (record.approverUserCd && window.USER_CD
                        && record.approverUserCd !== window.USER_CD) {
                    dialogNotice = t('feature.distributionWorkflow.message.assignedApproverOnly',
                        '지정된 승인자만 이 배포요청을 승인하거나 반려할 수 있습니다.');
                } else {
                    setHidden('workflowReject', false);
                    setHidden('workflowApprove', false);
                }
            } else {
                showDecisionSummary(record.decisionComment);
            }
        } else if (mode === 'approved') {
            element('workflowDialogTitle').textContent =
                t('feature.distributionWorkflow.dialog.approvedTitle', '승인완료 배포 상세');
            showDecisionSummary(record.decisionComment);
        } else {
            element('workflowDialogTitle').textContent =
                t('feature.distributionWorkflow.dialog.mineDetailTitle', '내 배포요청 상세');
            if (editable) {
                setHidden('workflowSaveDraft', false);
                setHidden('workflowSubmit', false);
            }
            if (record.status === 'DRAFT' || record.status === 'PENDING_APPROVAL'
                    || record.status === 'REJECTED') {
                setHidden('workflowCancelRequest', false);
            }
            if (record.status === 'REJECTED' || record.status === 'APPROVED') {
                showDecisionSummary(record.decisionComment);
            }
        }
        showDialogMessage(dialogNotice, dialogNotice ? 'info' : '');
        updateLengthCounters();
    }

    function showDialog(focusSource) {
        state.lastFocused = focusSource || document.activeElement;
        element('workflowDialog').hidden = false;
        document.body.classList.add('dw-dialog-open');
        window.setTimeout(function () {
            element('workflowDialog').querySelector('.dw-dialog__panel').focus();
        }, 0);
    }

    function closeDialog() {
        if (state.busy) return;
        element('workflowDialog').hidden = true;
        document.body.classList.remove('dw-dialog-open');
        showDialogMessage('');
        if (state.lastFocused && typeof state.lastFocused.focus === 'function') {
            state.lastFocused.focus();
        }
    }

    function setBusy(busy) {
        state.busy = busy;
        Array.prototype.forEach.call(element('workflowDialog').querySelectorAll('button'), function (button) {
            button.disabled = busy;
        });
    }

    function openDetail(requestId, focusSource) {
        element('workflowDialogTitle').textContent =
            t('feature.distributionWorkflow.message.loading', '자료를 불러오는 중입니다.');
        setHidden('workflowRequestMeta', true);
        setHidden('workflowItemEditor', true);
        setHidden('workflowItemSnapshot', true);
        setHidden('workflowDecisionSection', true);
        setHidden('workflowDecisionSummary', true);
        setHidden('workflowEventsSection', true);
        resetActionButtons();
        showDialog(focusSource);
        setBusy(true);
        request('/requests/' + encodeURIComponent(requestId)).then(function (detail) {
            configureDetail(detail);
        }).catch(function (error) {
            showDialogMessage(error.message ||
                t('feature.distributionWorkflow.message.detailFailed', '배포요청 상세를 불러오지 못했습니다.'));
        }).finally(function () {
            setBusy(false);
        });
    }

    function collectPayload() {
        var title = element('workflowTitle').value.trim();
        if (!title) {
            throw new Error(t('feature.distributionWorkflow.message.titleRequired', '요청 제목을 입력하세요.'));
        }
        var partnerCompanyId = element('workflowPartnerCompany').value;
        if (!partnerCompanyId) {
            throw new Error(t('feature.distributionWorkflow.message.partnerRequired',
                '협력업체를 선택하세요.'));
        }
        var recipientUserIds = Array.prototype.map.call(
            element('workflowRecipients').querySelectorAll('input:checked'),
            function (input) { return Number(input.value); });
        if (!recipientUserIds.length || recipientUserIds.length > 50) {
            throw new Error(t('feature.distributionWorkflow.message.recipientsRequired',
                '수신자를 1명 이상 50명 이하로 선택하세요.'));
        }
        var approverUserCd = element('workflowApprover').value;
        if (!approverUserCd) {
            throw new Error(t('feature.distributionWorkflow.message.approverRequired',
                '승인자를 선택하세요.'));
        }
        var startDate = element('workflowDistributionStartDate').value;
        var endDate = element('workflowDistributionEndDate').value;
        if (!startDate || !endDate || endDate < startDate) {
            throw new Error(t('feature.distributionWorkflow.message.invalidDistributionPeriod',
                '배포 종료일은 시작일과 같거나 이후여야 합니다.'));
        }
        var rows = element('workflowItemEditorBody').querySelectorAll('tr');
        if (!rows.length) {
            throw new Error(t('feature.distributionWorkflow.message.itemsRequired',
                '배포 대상 기술자료를 한 건 이상 추가하세요.'));
        }
        if (rows.length > 200) {
            throw new Error(t('feature.distributionWorkflow.message.maxItems',
                '배포 대상은 최대 200개까지 추가할 수 있습니다.'));
        }
        var documents = Array.prototype.map.call(rows, function (row) {
            var objectId = row.querySelector('.dw-item-object-id').value.trim();
            var parentCategory = row.querySelector('.dw-item-category-parent').value;
            var childCategory = row.querySelector('.dw-item-category-child').value;
            if (!parentCategory || !childCategory || !objectId || !row._selectedBundle) {
                throw new Error(t('feature.distributionWorkflow.message.itemIdentifierMissing',
                    '상위 자료분류, 하위 자료분류와 기술자료를 모두 선택하세요.'));
            }
            return {objectId: objectId};
        });
        return {
            title: title,
            purpose: element('workflowPurpose').value.trim(),
            partnerCompanyId: Number(partnerCompanyId),
            recipientUserIds: recipientUserIds,
            approverUserCd: approverUserCd,
            distributionStartDate: startDate,
            distributionEndDate: endDate,
            documents: documents
        };
    }

    function persistDraft(payload) {
        var record = state.current && state.current.request ? state.current.request : null;
        var existingId = record ? record.requestId : null;
        return request(existingId ? '/requests/' + encodeURIComponent(existingId) : '/requests', {
            method: existingId ? 'PUT' : 'POST',
            body: JSON.stringify(payload)
        });
    }

    function handleSaveDraft() {
        var payload;
        try {
            payload = collectPayload();
        } catch (error) {
            showDialogMessage(error.message);
            return;
        }
        setBusy(true);
        persistDraft(payload).then(function (detail) {
            configureDetail(detail);
            showDialogMessage(t('feature.distributionWorkflow.message.saved',
                '배포요청을 임시저장했습니다.'), 'success');
            return loadRecords();
        }).catch(function (error) {
            showDialogMessage(error.message ||
                t('feature.distributionWorkflow.message.actionFailed', '요청을 처리하지 못했습니다.'));
        }).finally(function () {
            setBusy(false);
        });
    }

    function handleSubmit() {
        var payload;
        try {
            payload = collectPayload();
        } catch (error) {
            showDialogMessage(error.message);
            return;
        }
        if (!window.confirm(t('feature.distributionWorkflow.message.confirmSubmit',
                '이 배포요청을 승인 요청하시겠습니까?'))) return;

        setBusy(true);
        persistDraft(payload).then(function (draft) {
            state.current = draft;
            return request('/requests/' + encodeURIComponent(draft.request.requestId) + '/submit', {
                method: 'POST'
            });
        }).then(function (detail) {
            configureDetail(detail);
            showDialogMessage(t('feature.distributionWorkflow.message.submitted',
                '배포요청을 승인 요청했습니다.'), 'success');
            return loadRecords();
        }).catch(function (error) {
            showDialogMessage(error.message ||
                t('feature.distributionWorkflow.message.actionFailed', '요청을 처리하지 못했습니다.'));
        }).finally(function () {
            setBusy(false);
        });
    }

    function handleCancelRequest() {
        var record = state.current && state.current.request;
        if (!record) return;
        if (!window.confirm(t('feature.distributionWorkflow.message.confirmCancel',
                '이 배포요청을 취소하시겠습니까?'))) return;
        setBusy(true);
        request('/requests/' + encodeURIComponent(record.requestId) + '/cancel', {
            method: 'POST'
        }).then(function (detail) {
            configureDetail(detail);
            showDialogMessage(t('feature.distributionWorkflow.message.cancelled',
                '배포요청을 취소했습니다.'), 'success');
            return loadRecords();
        }).catch(function (error) {
            showDialogMessage(error.message ||
                t('feature.distributionWorkflow.message.actionFailed', '요청을 처리하지 못했습니다.'));
        }).finally(function () {
            setBusy(false);
        });
    }

    function decide(action) {
        var record = state.current && state.current.request;
        if (!record) return;
        var comment = element('workflowDecisionComment').value.trim();
        if (action === 'reject' && !comment) {
            showDialogMessage(t('feature.distributionWorkflow.message.rejectionReasonRequired',
                '반려 사유를 입력하세요.'));
            element('workflowDecisionComment').focus();
            return;
        }
        var confirmKey = action === 'approve' ? 'confirmApprove' : 'confirmReject';
        var confirmFallback = action === 'approve'
            ? '이 배포요청을 승인하시겠습니까?' : '이 배포요청을 반려하시겠습니까?';
        if (!window.confirm(t('feature.distributionWorkflow.message.' + confirmKey, confirmFallback))) return;

        setBusy(true);
        request('/requests/' + encodeURIComponent(record.requestId) + '/' + action, {
            method: 'POST',
            body: JSON.stringify({comment: comment})
        }).then(function (detail) {
            configureDetail(detail);
            showDialogMessage(action === 'approve'
                ? t('feature.distributionWorkflow.message.approved', '배포요청을 승인했습니다.')
                : t('feature.distributionWorkflow.message.rejected', '배포요청을 반려했습니다.'), 'success');
            return loadRecords();
        }).catch(function (error) {
            showDialogMessage(error.message ||
                t('feature.distributionWorkflow.message.actionFailed', '요청을 처리하지 못했습니다.'));
        }).finally(function () {
            setBusy(false);
        });
    }

    function bindEvents() {
        element('workflowSearchForm').addEventListener('submit', function (event) {
            event.preventDefault();
            if (mode === 'mine') loadRecords();
            else renderRecords();
        });
        element('workflowResetButton').addEventListener('click', function () {
            element('workflowKeyword').value = '';
            if (element('workflowStatusFilter')) element('workflowStatusFilter').value = '';
            loadRecords();
        });
        if (element('workflowCreateButton')) {
            element('workflowCreateButton').addEventListener('click', openNew);
        }
        element('workflowPartnerCompany').addEventListener('change', function () {
            loadPartnerUsers(this.value, [], true);
        });
        element('workflowDistributionStartDate').addEventListener('change', function () {
            var end = element('workflowDistributionEndDate');
            if (!end.value || end.value < this.value) end.value = addDays(this.value, 7);
        });
        element('workflowTableBody').addEventListener('click', function (event) {
            var button = event.target.closest('[data-request-id]');
            if (!button) return;
            openDetail(button.getAttribute('data-request-id'), button);
        });
        Array.prototype.forEach.call(document.querySelectorAll('[data-workflow-close]'), function (button) {
            button.addEventListener('click', closeDialog);
        });
        element('workflowAddItem').addEventListener('click', function () {
            addItemRow({});
            renumberItemRows();
        });
        element('workflowItemEditorBody').addEventListener('click', function (event) {
            var remove = event.target.closest('.dw-remove-item');
            if (!remove) return;
            remove.closest('tr').remove();
            renumberItemRows();
        });
        element('workflowPurpose').addEventListener('input', updateLengthCounters);
        element('workflowDecisionComment').addEventListener('input', updateLengthCounters);
        element('workflowSaveDraft').addEventListener('click', handleSaveDraft);
        element('workflowSubmit').addEventListener('click', handleSubmit);
        element('workflowCancelRequest').addEventListener('click', handleCancelRequest);
        element('workflowApprove').addEventListener('click', function () { decide('approve'); });
        element('workflowReject').addEventListener('click', function () { decide('reject'); });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !element('workflowDialog').hidden) closeDialog();
        });
    }

    function initialize() {
        bindEvents();
        loadDirectory().then(function () {
            return loadRecords();
        }).then(function () {
            if (config.openCreateOnLoad && mode === 'mine') window.setTimeout(openNew, 0);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initialize);
    } else {
        initialize();
    }
})(window, document);
