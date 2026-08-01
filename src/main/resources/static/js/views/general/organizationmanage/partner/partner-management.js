(function (window, document) {
    'use strict';

    var page = document.querySelector('.partner-management-page');
    if (!page) return;

    var config = window.partnerManagementPage || {};
    var apiBase = (config.contextPath || '') + '/general/organizationmanage/partner/api';
    var state = {companies: [], companyId: null, users: [], busy: false, lastFocused: null};
    var apiErrorMessageKeys = {
        PARTNER_COMPANY_NOT_FOUND: 'feature.partner.error.notFound',
        DUPLICATE_PARTNER_BUSINESS_NO: 'feature.partner.error.duplicateBusinessNo',
        DUPLICATE_PARTNER_USER: 'feature.partner.error.duplicateUser',
        DUPLICATE_PARTNER_USER_EMAIL: 'feature.partner.error.duplicateUserEmail',
        ONE_REPRESENTATIVE_REQUIRED: 'feature.partner.error.oneRepresentative',
        INACTIVE_REPRESENTATIVE: 'feature.partner.error.inactiveRepresentative',
        INVALID_PARTNER_EMAIL: 'feature.partner.error.invalidEmail',
        PARTNER_COMPANY_REQUIRED: 'feature.partner.error.companyRequired',
        PARTNER_USERS_REQUIRED: 'feature.partner.error.usersRequired',
        PARTNER_CONCURRENT_CHANGE: 'feature.partner.error.concurrentChange',
        PARTNER_SEARCH_TOO_LONG: 'feature.partner.error.searchTooLong',
        INVALID_PARTNER_FIELD: 'feature.partner.error.invalidField',
        INVALID_PARTNER_FLAG: 'feature.partner.error.invalidField',
        INVALID_PARTNER_USER: 'feature.partner.error.invalidUser',
        INVALID_PARTNER_USER_ID: 'feature.partner.error.invalidUser',
        INVALID_PARTNER_REQUEST: 'feature.partner.message.failed'
    };

    function el(id) { return document.getElementById(id); }
    function t(key, fallback) {
        if (window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
            return window.SdmsI18n.t(key, fallback);
        }
        return fallback || key;
    }
    function value(id) { return String(el(id).value || '').trim(); }
    function append(parent, tag, className, text) {
        var node = document.createElement(tag);
        if (className) node.className = className;
        if (typeof text !== 'undefined') node.textContent = text;
        parent.appendChild(node);
        return node;
    }
    function setMessage(target, message, type) {
        target.hidden = !message;
        target.className = 'pm-alert' + (type ? ' pm-alert--' + type : '');
        target.textContent = message || '';
    }
    function localizedApiError(body, fallback) {
        var key = body && body.code ? apiErrorMessageKeys[body.code] : '';
        return key ? t(key, fallback) : (body && body.message ? body.message : fallback);
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
                    try { body = JSON.parse(raw); } catch (ignore) { body = null; }
                }
                if (!response.ok) {
                    var fallback = t('feature.partner.message.failed', '요청을 처리하지 못했습니다.');
                    throw new Error(localizedApiError(body, fallback));
                }
                return body;
            });
        });
    }
    function setBusy(busy) {
        state.busy = busy;
        el('partnerSaveButton').disabled = busy;
        el('partnerCreateButton').disabled = busy;
    }

    function loadCompanies() {
        setMessage(el('partnerPageMessage'), '');
        var keyword = value('partnerKeyword');
        var path = '/companies' + (keyword ? '?keyword=' + encodeURIComponent(keyword) : '');
        return request(path).then(function (companies) {
            state.companies = companies || [];
            renderCompanies();
        }).catch(function (error) {
            setMessage(el('partnerPageMessage'), error.message, 'error');
        });
    }

    function renderCompanies() {
        var body = el('partnerTableBody');
        body.textContent = '';
        el('partnerCount').textContent = t('feature.common.count', '{0}건').replace('{0}', state.companies.length);
        if (!state.companies.length) {
            var emptyRow = append(body, 'tr', 'pm-empty-row');
            var emptyCell = append(emptyRow, 'td', '', t('feature.partner.message.empty', '등록된 협력업체가 없습니다.'));
            emptyCell.colSpan = 7;
            return;
        }
        state.companies.forEach(function (company) {
            var row = append(body, 'tr');
            append(row, 'td', 'pm-code', company.companyCode || '-');
            var companyCell = append(row, 'td');
            var openButton = append(companyCell, 'button', 'pm-link', company.companyName || '-');
            openButton.type = 'button';
            openButton.setAttribute('data-company-id', company.partnerCompanyId);
            if (company.contactEmail) append(companyCell, 'small', '', company.contactEmail);
            append(row, 'td', '', company.businessNo || '-');
            append(row, 'td', '', company.representativeUserName || '-');
            append(row, 'td', '', t('feature.common.count', '{0}건').replace('{0}', company.userCount || 0));
            var statusCell = append(row, 'td');
            append(statusCell, 'span', 'pm-status pm-status--' + (company.useYn === 'Y' ? 'active' : 'inactive'),
                company.useYn === 'Y' ? t('feature.partner.value.active', '사용') : t('feature.partner.value.inactive', '미사용'));
            var actions = append(row, 'td');
            var edit = append(actions, 'button', 'pm-icon-button', '');
            edit.type = 'button';
            edit.setAttribute('data-company-id', company.partnerCompanyId);
            edit.setAttribute('aria-label', t('feature.partner.action.edit', '수정'));
            edit.innerHTML = '<i class="icon-base ti tabler-edit" aria-hidden="true"></i>';
        });
    }

    function blankUser(representative) {
        return {partnerUserId: null, userName: '', email: '', phone: '', positionName: '',
            representativeYn: representative ? 'Y' : 'N', useYn: 'Y'};
    }
    function openCreate() {
        state.lastFocused = document.activeElement;
        state.companyId = null;
        state.users = [blankUser(true)];
        el('partnerDialogTitle').textContent = t('feature.partner.dialog.createTitle', '새 협력업체');
        el('partnerCompanyName').value = '';
        el('partnerBusinessNo').value = '';
        el('partnerContactEmail').value = '';
        el('partnerContactPhone').value = '';
        el('partnerAddress').value = '';
        el('partnerUseYn').value = 'Y';
        el('partnerDeleteButton').hidden = true;
        renderUsers();
        showDialog();
    }
    function openEdit(companyId) {
        if (state.busy) return;
        state.lastFocused = document.activeElement;
        setBusy(true);
        request('/companies/' + encodeURIComponent(companyId)).then(function (company) {
            state.companyId = company.partnerCompanyId;
            state.users = company.users || [];
            el('partnerDialogTitle').textContent = t('feature.partner.dialog.editTitle', '협력업체 정보 수정');
            el('partnerCompanyName').value = company.companyName || '';
            el('partnerBusinessNo').value = company.businessNo || '';
            el('partnerContactEmail').value = company.contactEmail || '';
            el('partnerContactPhone').value = company.contactPhone || '';
            el('partnerAddress').value = company.address || '';
            el('partnerUseYn').value = company.useYn || 'Y';
            el('partnerDeleteButton').hidden = false;
            renderUsers();
            showDialog();
        }).catch(function (error) {
            setMessage(el('partnerPageMessage'), error.message, 'error');
        }).then(function () { setBusy(false); });
    }
    function showDialog() {
        setMessage(el('partnerDialogMessage'), '');
        el('partnerDialog').hidden = false;
        document.body.classList.add('pm-dialog-open');
        window.setTimeout(function () { el('partnerCompanyName').focus(); }, 0);
    }
    function closeDialog() {
        el('partnerDialog').hidden = true;
        document.body.classList.remove('pm-dialog-open');
        if (state.lastFocused && typeof state.lastFocused.focus === 'function') state.lastFocused.focus();
    }

    function inputCell(row, user, index, field, type, maxLength) {
        var cell = append(row, 'td');
        var input = append(cell, 'input', 'pm-user-input');
        input.type = type || 'text';
        input.value = user[field] || '';
        input.setAttribute('data-user-index', index);
        input.setAttribute('data-user-field', field);
        if (maxLength) input.maxLength = maxLength;
        return input;
    }
    function renderUsers() {
        var body = el('partnerUserTableBody');
        body.textContent = '';
        state.users.forEach(function (user, index) {
            var row = append(body, 'tr');
            var representativeCell = append(row, 'td');
            var representative = append(representativeCell, 'input');
            representative.type = 'radio';
            representative.name = 'partnerRepresentative';
            representative.checked = user.representativeYn === 'Y';
            representative.setAttribute('data-representative-index', index);
            representative.setAttribute('aria-label', t('feature.partner.column.representativeShort', '대표'));
            inputCell(row, user, index, 'userName', 'text', 100).required = true;
            inputCell(row, user, index, 'email', 'email', 254).required = true;
            inputCell(row, user, index, 'phone', 'text', 40);
            inputCell(row, user, index, 'positionName', 'text', 100);
            var activeCell = append(row, 'td');
            var active = append(activeCell, 'input');
            active.type = 'checkbox';
            active.checked = user.useYn !== 'N';
            active.setAttribute('data-active-index', index);
            active.setAttribute('aria-label', t('feature.partner.label.use', '사용여부'));
            var actionCell = append(row, 'td');
            var remove = append(actionCell, 'button', 'pm-icon-button pm-icon-button--danger');
            remove.type = 'button';
            remove.setAttribute('data-remove-user-index', index);
            remove.setAttribute('aria-label', t('feature.partner.action.removeUser', '사용자 삭제'));
            remove.innerHTML = '<i class="icon-base ti tabler-trash" aria-hidden="true"></i>';
        });
    }
    function syncUserInput(target) {
        var fieldIndex = target.getAttribute('data-user-index');
        if (fieldIndex !== null) state.users[Number(fieldIndex)][target.getAttribute('data-user-field')] = target.value;
        var representativeIndex = target.getAttribute('data-representative-index');
        if (representativeIndex !== null && target.checked) {
            state.users.forEach(function (user, index) { user.representativeYn = index === Number(representativeIndex) ? 'Y' : 'N'; });
        }
        var activeIndex = target.getAttribute('data-active-index');
        if (activeIndex !== null) state.users[Number(activeIndex)].useYn = target.checked ? 'Y' : 'N';
    }
    function payload() {
        return {
            companyName: value('partnerCompanyName'), businessNo: value('partnerBusinessNo'),
            contactEmail: value('partnerContactEmail'), contactPhone: value('partnerContactPhone'),
            address: value('partnerAddress'), useYn: el('partnerUseYn').value, users: state.users
        };
    }
    function saveCompany() {
        if (state.busy) return;
        if (!el('partnerForm').reportValidity()) return;
        var invalidUser = Array.prototype.some.call(document.querySelectorAll('.pm-user-input'), function (input) {
            return !input.reportValidity();
        });
        if (invalidUser) return;
        setBusy(true);
        setMessage(el('partnerDialogMessage'), '');
        var path = '/companies' + (state.companyId ? '/' + state.companyId : '');
        request(path, {method: state.companyId ? 'PUT' : 'POST', body: JSON.stringify(payload())})
            .then(function () {
                closeDialog();
                return loadCompanies();
            }).catch(function (error) {
                setMessage(el('partnerDialogMessage'), error.message, 'error');
            }).then(function () { setBusy(false); });
    }
    function deleteCompany() {
        if (!state.companyId || state.busy) return;
        if (!window.confirm(t('feature.partner.message.deleteConfirm', '이 협력업체와 사용자를 삭제하시겠습니까?'))) return;
        setBusy(true);
        request('/companies/' + state.companyId, {method: 'DELETE'}).then(function () {
            closeDialog();
            return loadCompanies();
        }).catch(function (error) {
            setMessage(el('partnerDialogMessage'), error.message, 'error');
        }).then(function () { setBusy(false); });
    }

    el('partnerSearchForm').addEventListener('submit', function (event) { event.preventDefault(); loadCompanies(); });
    el('partnerResetButton').addEventListener('click', function () { el('partnerKeyword').value = ''; loadCompanies(); });
    el('partnerCreateButton').addEventListener('click', openCreate);
    el('partnerSaveButton').addEventListener('click', saveCompany);
    el('partnerDeleteButton').addEventListener('click', deleteCompany);
    el('partnerAddUserButton').addEventListener('click', function () { state.users.push(blankUser(false)); renderUsers(); });
    el('partnerTableBody').addEventListener('click', function (event) {
        var target = event.target.closest('[data-company-id]');
        if (target) openEdit(target.getAttribute('data-company-id'));
    });
    el('partnerUserTableBody').addEventListener('input', function (event) { syncUserInput(event.target); });
    el('partnerUserTableBody').addEventListener('change', function (event) { syncUserInput(event.target); });
    el('partnerUserTableBody').addEventListener('click', function (event) {
        var target = event.target.closest('[data-remove-user-index]');
        if (!target) return;
        state.users.splice(Number(target.getAttribute('data-remove-user-index')), 1);
        renderUsers();
    });
    Array.prototype.forEach.call(document.querySelectorAll('[data-partner-close]'), function (button) {
        button.addEventListener('click', closeDialog);
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !el('partnerDialog').hidden) closeDialog();
    });

    loadCompanies();
})(window, document);
