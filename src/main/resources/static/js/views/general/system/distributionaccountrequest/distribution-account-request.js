(function (window, document) {
    'use strict';

    var page = document.querySelector('.distribution-account-request-page');
    if (!page) return;

    var config = window.distributionAccountRequestPage || {};
    var apiBase = (config.contextPath || '') + '/general/distribution/account-requests/api';
    var state = {records: [], current: null, busy: false, lastFocused: null};
    var errorMessageKeys = {
        DISTRIBUTION_ACCOUNT_REQUEST_NOT_FOUND: 'feature.distributionAccountRequest.error.notFound',
        ACCOUNT_REQUEST_NOT_FOUND: 'feature.distributionAccountRequest.error.notFound',
        INVALID_DISTRIBUTION_ACCOUNT_STATUS_TRANSITION: 'feature.distributionAccountRequest.error.invalidStatus',
        DISTRIBUTION_ACCOUNT_REJECTION_COMMENT_REQUIRED: 'feature.distributionAccountRequest.error.rejectionReasonRequired',
        DISTRIBUTION_ACCOUNT_DECISION_COMMENT_TOO_LONG: 'feature.distributionAccountRequest.error.reasonTooLong',
        DISTRIBUTION_ACCOUNT_REQUEST_ACCESS_DENIED: 'feature.distributionAccountRequest.error.accessDenied',
        INVALID_DISTRIBUTION_ACCOUNT_STATUS: 'feature.distributionAccountRequest.error.invalidFilter',
        INVALID_DISTRIBUTION_ACCOUNT_REQUEST_TYPE: 'feature.distributionAccountRequest.error.invalidFilter',
        INVALID_DISTRIBUTION_ACCOUNT_FILTER: 'feature.distributionAccountRequest.error.invalidFilter',
        INVALID_DISTRIBUTION_ACCOUNT_PAGE_LIMIT: 'feature.distributionAccountRequest.error.invalidFilter',
        INVALID_DISTRIBUTION_ACCOUNT_PAGE_OFFSET: 'feature.distributionAccountRequest.error.invalidFilter',
        INVALID_DISTRIBUTION_ACCOUNT_API_REQUEST: 'feature.distributionAccountRequest.error.invalidFilter'
    };

    function el(id) { return document.getElementById(id); }
    function t(key, fallback) {
        if (window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
            return window.SdmsI18n.t(key, fallback);
        }
        return fallback || key;
    }
    function value(id) { return String(el(id).value || '').trim(); }
    function text(value) { return value === null || typeof value === 'undefined' || value === '' ? '-' : String(value); }
    function append(parent, tag, className, value) {
        var node = document.createElement(tag);
        if (className) node.className = className;
        if (typeof value !== 'undefined') node.textContent = value;
        parent.appendChild(node);
        return node;
    }
    function setMessage(target, message, type) {
        target.hidden = !message;
        target.className = 'dar-alert' + (type ? ' dar-alert--' + type : '');
        target.textContent = message || '';
    }
    function localizedApiError(body, fallback) {
        var key = body && body.code ? errorMessageKeys[body.code] : '';
        return key ? t(key, fallback) : (body && body.message ? body.message : fallback);
    }
    function requestHeaders(baseHeaders, method) {
        var headers = baseHeaders || {};
        var normalizedMethod = String(method || 'GET').toUpperCase();
        if (normalizedMethod === 'GET' || normalizedMethod === 'HEAD'
                || normalizedMethod === 'OPTIONS' || normalizedMethod === 'TRACE') {
            return headers;
        }
        if (window.SdmsCsrf && typeof window.SdmsCsrf.headers === 'function') {
            return window.SdmsCsrf.headers(headers);
        }
        var tokenMeta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');
        var token = tokenMeta ? tokenMeta.getAttribute('content') : '';
        if (token) {
            headers[headerMeta && headerMeta.getAttribute('content')
                ? headerMeta.getAttribute('content') : 'X-CSRF-TOKEN'] = token;
        }
        return headers;
    }
    function request(path, options) {
        var requestOptions = options || {};
        var headers = requestOptions.headers || {};
        headers.Accept = 'application/json';
        if (requestOptions.body) headers['Content-Type'] = 'application/json';
        requestOptions.headers = requestHeaders(headers, requestOptions.method);
        requestOptions.credentials = 'same-origin';
        return window.fetch(apiBase + path, requestOptions).then(function (response) {
            return response.text().then(function (raw) {
                var body = null;
                if (raw) {
                    try { body = JSON.parse(raw); } catch (ignore) { body = null; }
                }
                if (!response.ok) {
                    throw new Error(localizedApiError(body,
                        t('feature.distributionAccountRequest.message.failed', '요청을 처리하지 못했습니다.')));
                }
                return body;
            });
        });
    }
    function queryString(parameters) {
        var pairs = [];
        Object.keys(parameters).forEach(function (name) {
            var parameter = parameters[name];
            if (parameter !== null && typeof parameter !== 'undefined' && parameter !== '') {
                pairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(parameter));
            }
        });
        return pairs.length ? '?' + pairs.join('&') : '';
    }
    function formatDate(value) {
        if (!value) return '-';
        var date = new Date(value);
        if (isNaN(date.getTime())) return text(value);
        try {
            return new Intl.DateTimeFormat(document.documentElement.lang || 'ko', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit'
            }).format(date);
        } catch (ignore) {
            return date.toLocaleString();
        }
    }
    function typeLabel(type) {
        var fallback = {
            REGISTER_USER: '사용자등록',
            UNLOCK_ACCOUNT: '잠금해제',
            RESET_PASSWORD: '비밀번호 초기화'
        }[type] || text(type);
        return t('feature.distributionAccountRequest.type.' + type, fallback);
    }
    function statusLabel(status) {
        var fallback = {PENDING: '대기', APPROVED: '승인', REJECTED: '반려'}[status] || text(status);
        return t('feature.distributionAccountRequest.status.' + status, fallback);
    }
    function statusClass(status) {
        return 'dar-chip dar-chip--' + String(status || '').toLowerCase();
    }
    function identity(name, id) {
        if (name && id) return name + ' (' + id + ')';
        return text(name || id);
    }
    function contact(email, phone) {
        var values = [];
        if (email) values.push(email);
        if (phone) values.push(phone);
        return values.length ? values.join(' · ') : '-';
    }
    function systemLabel(record) {
        return text(record.sourceSystemId || record.clientId);
    }
    function setBusy(busy) {
        state.busy = busy;
        el('accountRequestRefreshButton').disabled = busy;
        el('accountRequestApproveButton').disabled = busy;
        el('accountRequestRejectButton').disabled = busy;
    }

    function loadRecords() {
        if (state.busy) return Promise.resolve();
        setBusy(true);
        setMessage(el('accountRequestPageMessage'), '');
        var query = queryString({
            keyword: value('accountRequestKeyword'),
            sourceSystemId: value('accountRequestSourceSystem'),
            requestType: value('accountRequestTypeFilter'),
            status: value('accountRequestStatusFilter'),
            limit: 100,
            offset: 0
        });
        return request('/requests' + query).then(function (records) {
            state.records = Array.isArray(records) ? records : [];
            renderRecords();
        }).catch(function (error) {
            state.records = [];
            renderRecords();
            setMessage(el('accountRequestPageMessage'), error.message, 'error');
        }).then(function () { setBusy(false); });
    }

    function renderRecords() {
        var body = el('accountRequestTableBody');
        body.textContent = '';
        el('accountRequestCount').textContent = t('feature.common.count', '{0}건')
            .replace('{0}', state.records.length);
        if (!state.records.length) {
            var emptyRow = append(body, 'tr', 'dar-empty-row');
            var emptyCell = append(emptyRow, 'td', '',
                t('feature.distributionAccountRequest.message.empty', '조회된 계정요청이 없습니다.'));
            emptyCell.colSpan = 7;
            return;
        }
        state.records.forEach(function (record) {
            var row = append(body, 'tr');
            var systemCell = append(row, 'td', '', systemLabel(record));
            if (record.clientId && record.clientId !== record.sourceSystemId) {
                append(systemCell, 'small', '', record.clientId);
            }
            var representativeCell = append(row, 'td', '',
                identity(record.representativeName, record.representativeId));
            if (record.organizationName) append(representativeCell, 'small', '', record.organizationName);
            var targetCell = append(row, 'td', '', identity(record.targetUserName, record.targetUserId));
            if (record.targetUserEmail) append(targetCell, 'small', '', record.targetUserEmail);
            var typeCell = append(row, 'td');
            append(typeCell, 'span', 'dar-chip dar-chip--type', typeLabel(record.requestType));
            append(row, 'td', '', formatDate(record.receivedAt));
            var statusCell = append(row, 'td');
            append(statusCell, 'span', statusClass(record.status), statusLabel(record.status));
            var manageCell = append(row, 'td');
            var button = append(manageCell, 'button', 'dar-link',
                record.status === 'PENDING'
                    ? t('feature.distributionAccountRequest.action.review', '검토')
                    : t('feature.distributionAccountRequest.action.detail', '상세'));
            button.type = 'button';
            button.setAttribute('data-request-id', record.requestId);
        });
    }

    function setDetailValue(id, value) { el(id).textContent = text(value); }
    function renderEvents(events) {
        var section = el('accountRequestEventsSection');
        var list = el('accountRequestEventList');
        list.textContent = '';
        section.hidden = !events || !events.length;
        (events || []).forEach(function (event) {
            var item = append(list, 'li');
            append(item, 'strong', '', statusLabel(event.toStatus || event.eventType));
            var description = identity(event.actorName, event.actorId);
            if (event.comment) description += ' · ' + event.comment;
            append(item, 'span', '', description);
            append(item, 'small', '', formatDate(event.occurredAt));
        });
    }
    function configureDetail(record) {
        state.current = record;
        setDetailValue('accountRequestDetailId', record.requestId);
        var statusTarget = el('accountRequestDetailStatus');
        statusTarget.textContent = '';
        append(statusTarget, 'span', statusClass(record.status), statusLabel(record.status));
        setDetailValue('accountRequestDetailSystem', systemLabel(record));
        setDetailValue('accountRequestDetailReceivedAt', formatDate(record.receivedAt));
        setDetailValue('accountRequestDetailRepresentative',
            identity(record.representativeName, record.representativeId));
        setDetailValue('accountRequestDetailRepresentativeContact',
            contact(record.representativeEmail, record.representativePhone));
        setDetailValue('accountRequestDetailTargetUser', identity(record.targetUserName, record.targetUserId));
        setDetailValue('accountRequestDetailTargetUserContact',
            contact(record.targetUserEmail, record.targetUserPhone));
        setDetailValue('accountRequestDetailType', typeLabel(record.requestType));
        setDetailValue('accountRequestDetailExternalId',
            record.eventId + (record.correlationId ? ' · ' + record.correlationId : ''));
        setDetailValue('accountRequestDetailReason', record.reason);

        var pending = record.status === 'PENDING';
        el('accountRequestDecisionSection').hidden = !pending;
        el('accountRequestApproveButton').hidden = !pending;
        el('accountRequestRejectButton').hidden = !pending;
        el('accountRequestDecisionReason').value = '';
        el('accountRequestDecisionLength').textContent = '0';
        var summary = el('accountRequestReviewSummary');
        summary.hidden = pending;
        if (!pending) {
            var reviewer = identity(record.decidedByUserName || record.decidedByUserId,
                record.decidedByUserCd);
            setDetailValue('accountRequestDetailReview', reviewer + ' · ' + formatDate(record.decidedAt) +
                (record.decisionComment ? '\n' + record.decisionComment : ''));
        }
        renderEvents(record.events);
        setMessage(el('accountRequestDialogMessage'), '');
    }
    function showDialog(source) {
        state.lastFocused = source || document.activeElement;
        el('accountRequestDialog').hidden = false;
        document.body.classList.add('dar-dialog-open');
        window.setTimeout(function () {
            el('accountRequestDialog').querySelector('.dar-dialog__panel').focus();
        }, 0);
    }
    function closeDialog() {
        if (state.busy) return;
        el('accountRequestDialog').hidden = true;
        document.body.classList.remove('dar-dialog-open');
        state.current = null;
        if (state.lastFocused && typeof state.lastFocused.focus === 'function') state.lastFocused.focus();
    }
    function openDetail(requestId, source) {
        if (state.busy) return;
        showDialog(source);
        setBusy(true);
        setMessage(el('accountRequestDialogMessage'),
            t('feature.distributionAccountRequest.message.loading', '계정요청을 불러오는 중입니다.'));
        request('/requests/' + encodeURIComponent(requestId)).then(function (record) {
            configureDetail(record || {});
        }).catch(function (error) {
            setMessage(el('accountRequestDialogMessage'), error.message, 'error');
        }).then(function () { setBusy(false); });
    }

    function decide(action) {
        if (!state.current || state.busy) return;
        var decisionComment = value('accountRequestDecisionReason');
        if (action === 'reject' && !decisionComment) {
            setMessage(el('accountRequestDialogMessage'),
                t('feature.distributionAccountRequest.error.rejectionReasonRequired', '반려 사유를 입력하세요.'), 'error');
            el('accountRequestDecisionReason').focus();
            return;
        }
        var confirmMessage = action === 'approve'
            ? t('feature.distributionAccountRequest.message.confirmApprove', '이 계정요청을 승인하시겠습니까?')
            : t('feature.distributionAccountRequest.message.confirmReject', '이 계정요청을 반려하시겠습니까?');
        if (!window.confirm(confirmMessage)) return;

        setBusy(true);
        request('/requests/' + encodeURIComponent(state.current.requestId) + '/' + action, {
            method: 'POST', body: JSON.stringify({decisionComment: decisionComment})
        }).then(function (record) {
            configureDetail(record || {});
            setMessage(el('accountRequestDialogMessage'), action === 'approve'
                ? t('feature.distributionAccountRequest.message.approved', '계정요청을 승인했습니다.')
                : t('feature.distributionAccountRequest.message.rejected', '계정요청을 반려했습니다.'), 'success');
            setBusy(false);
            return loadRecords();
        }).catch(function (error) {
            setBusy(false);
            setMessage(el('accountRequestDialogMessage'), error.message, 'error');
        });
    }

    function bindEvents() {
        el('accountRequestSearchForm').addEventListener('submit', function (event) {
            event.preventDefault();
            loadRecords();
        });
        el('accountRequestResetButton').addEventListener('click', function () {
            el('accountRequestKeyword').value = '';
            el('accountRequestSourceSystem').value = '';
            el('accountRequestTypeFilter').value = '';
            el('accountRequestStatusFilter').value = '';
            loadRecords();
        });
        el('accountRequestRefreshButton').addEventListener('click', loadRecords);
        el('accountRequestTableBody').addEventListener('click', function (event) {
            var button = event.target.closest('[data-request-id]');
            if (button) openDetail(button.getAttribute('data-request-id'), button);
        });
        Array.prototype.forEach.call(document.querySelectorAll('[data-account-request-close]'), function (button) {
            button.addEventListener('click', closeDialog);
        });
        el('accountRequestDecisionReason').addEventListener('input', function () {
            el('accountRequestDecisionLength').textContent = this.value.length;
        });
        el('accountRequestApproveButton').addEventListener('click', function () { decide('approve'); });
        el('accountRequestRejectButton').addEventListener('click', function () { decide('reject'); });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !el('accountRequestDialog').hidden) closeDialog();
        });
    }
    function initialize() {
        bindEvents();
        loadRecords();
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
    else initialize();
})(window, document);
