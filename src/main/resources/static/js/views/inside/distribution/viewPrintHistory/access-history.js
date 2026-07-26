(function ($, window, document) {
    'use strict';

    var config = window.accessHistoryConfig || {};
    var contextPath = config.contextPath || '';
    var endpoint = contextPath + '/inside/distribution/viewPrintHistory/accessEvents';
    var state = {
        rows: []
    };

    var eventLabels = {
        FILE_ACCESS: ['feature.history.event.fileAccess', '자료 접근'],
        DOWNLOAD_RESULT: ['feature.history.event.download', '다운로드'],
        ACL_CHANGE: ['feature.history.event.aclChange', '권한 변경']
    };

    var actionLabels = {
        LIST: ['feature.history.action.list', '목록 조회'],
        DETAIL: ['feature.history.action.detail', '상세 조회'],
        DOWNLOAD_ORIGINAL: ['feature.history.action.downloadOriginal', '원본 다운로드'],
        MANAGE_GRADE: ['feature.history.action.manageGrade', '보안등급 변경'],
        MANAGE_CLEARANCE: ['feature.history.action.manageClearance', '사용자 인가 변경'],
        MANAGE_USER_CLEARANCE: ['feature.history.action.manageClearance', '사용자 인가 변경'],
        MANAGE_FILE_LABEL: ['feature.history.action.manageFileLabel', '문서등급 변경'],
        MANAGE_FILE_PERMISSION: ['feature.history.action.manageFilePermission', '문서권한 변경'],
        MANAGE_DOCUMENT_PERMISSION: ['feature.history.action.manageFilePermission', '문서권한 변경'],
        MANAGE_ACL: ['feature.history.action.manageAcl', '접근권한 관리']
    };

    var reasonLabels = {
        ACCESS_ALLOWED: ['feature.history.reason.accessAllowed', '접근 정책 허용'],
        ACTION_NOT_ALLOWED: ['feature.history.reason.actionNotAllowed', '행위 권한 없음'],
        CLEARANCE_TOO_LOW: ['feature.history.reason.clearanceTooLow', '사용자 인가등급 부족'],
        FILE_GRADE_REQUIRED: ['feature.history.reason.fileGradeRequired', '문서 보안등급 미지정'],
        FILE_PERMISSION_DENIED: ['feature.history.reason.filePermissionDenied', '문서별 사용자 권한 없음'],
        RESOURCE_NOT_FOUND: ['feature.history.reason.resourceNotFound', '대상 자료를 찾을 수 없음'],
        INVALID_REQUEST: ['feature.history.reason.invalidRequest', '잘못된 요청'],
        BUSINESS_FAILURE: ['feature.history.reason.businessFailure', '업무 처리 실패']
    };

    function t(key, fallback) {
        var args = Array.prototype.slice.call(arguments, 2);
        var translated = fallback;
        if (window.SdmsI18n && typeof window.SdmsI18n.t === 'function') {
            translated = window.SdmsI18n.t.apply(window.SdmsI18n, [key, fallback].concat(args));
        }
        if (!translated) translated = fallback || key;
        return String(translated).replace(/\{(\d+)\}/g, function (_, index) {
            return args[Number(index)] === undefined ? _ : args[Number(index)];
        });
    }

    function codeLabel(labels, code) {
        var entry = labels[String(code || '').toUpperCase()];
        return entry ? t(entry[0], entry[1]) : '';
    }

    function escapeHtml(value) {
        return $('<div>').text(value == null ? '' : String(value)).html()
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function text(value, fallback) {
        if (value === undefined || value === null || String(value).trim() === '') {
            return fallback === undefined ? '-' : fallback;
        }
        return String(value);
    }

    function normalizeRows(response) {
        if ($.isArray(response)) return response;
        if (!response) return [];
        if ($.isArray(response.data)) return response.data;
        if ($.isArray(response.rows)) return response.rows;
        if ($.isArray(response.contents)) return response.contents;
        return [];
    }

    function formatNumber(value) {
        return Number(value || 0).toLocaleString(document.documentElement.lang || undefined);
    }

    function eventLabel(code) {
        return codeLabel(eventLabels, code) || text(code);
    }

    function actionLabel(code, fallback) {
        return codeLabel(actionLabels, code) || text(fallback || code);
    }

    function formatOccurredAt(value) {
        var formatted = text(value);
        if (formatted === '-') return formatted;
        return formatted.replace('T', ' ').replace(/\.\d+(?=(?:Z|[+-]\d{2}:?\d{2})?$)/, '')
            .replace(/(?:Z|[+-]\d{2}:?\d{2})$/, '');
    }

    function actorHtml(row) {
        var name = text(row.actorUserNm, '');
        var account = text(row.actorUserId || row.actorUserCd, '');
        var primary = name || account || '-';
        var secondary = name && account && name !== account ? account : text(row.actorUserCd, '');
        return '<div class="ah-cell">' +
            '<span class="ah-cell__primary" title="' + escapeHtml(primary) + '">' + escapeHtml(primary) + '</span>' +
            (secondary && secondary !== primary
                ? '<span class="ah-cell__secondary" title="' + escapeHtml(secondary) + '">' + escapeHtml(secondary) + '</span>'
                : '') +
            '</div>';
    }

    function menuHtml(row) {
        var menu = text(row.menuNm, '');
        var route = text(row.menuUrl || row.requestUri, '');
        var primary = menu || (route ? t('feature.history.value.requestPath', '요청 경로') : '-');
        return '<div class="ah-cell">' +
            '<span class="ah-cell__primary" title="' + escapeHtml(primary) + '">' + escapeHtml(primary) + '</span>' +
            (route
                ? '<span class="ah-cell__secondary" title="' + escapeHtml(route) + '">' + escapeHtml(route) + '</span>'
                : '') +
            '</div>';
    }

    function actionHtml(row) {
        var eventType = text(row.eventType, '');
        var action = actionLabel(row.actionType, row.actionNm);
        return '<div class="ah-cell">' +
            '<span class="ah-event-chip">' + escapeHtml(eventLabel(eventType)) + '</span>' +
            '<span class="ah-cell__secondary" title="' + escapeHtml(action) + '">' + escapeHtml(action) + '</span>' +
            '</div>';
    }

    function resourceHtml(row) {
        var type = text(row.objectType, '');
        var objectId = text(row.objectId, '');
        var requestNo = text(row.requestNo, '');
        var fileNo = text(row.fileNo, '');
        var grade = text(row.gradeCd, '');
        var primary = requestNo || objectId || '-';
        var metadata = [];

        if (type) metadata.push(type);
        if (objectId && objectId !== primary) metadata.push(objectId);
        if (fileNo && fileNo !== '*') {
            metadata.push(t('feature.history.value.fileNumber', '파일 {0}', fileNo));
        }

        return '<div class="ah-cell">' +
            '<span class="ah-cell__primary" title="' + escapeHtml(primary) + '">' + escapeHtml(primary) + '</span>' +
            (metadata.length
                ? '<span class="ah-cell__secondary" title="' + escapeHtml(metadata.join(' · ')) + '">' +
                    escapeHtml(metadata.join(' · ')) + '</span>'
                : '') +
            (grade ? '<span class="ah-grade-chip">' + escapeHtml(grade) + '</span>' : '') +
            '</div>';
    }

    function reasonHtml(row) {
        var reasonCode = text(row.reasonCd, '');
        var message = text(row.resultMessage, '');
        var localizedReason = codeLabel(reasonLabels, reasonCode);
        var primary = localizedReason || message || reasonCode || '-';
        var secondary = !localizedReason && reasonCode && primary !== reasonCode ? reasonCode : '';

        return '<div class="ah-cell">' +
            '<span class="ah-cell__primary" title="' + escapeHtml(primary) + '">' + escapeHtml(primary) + '</span>' +
            (secondary
                ? '<span class="ah-cell__secondary" title="' + escapeHtml(secondary) + '">' + escapeHtml(secondary) + '</span>'
                : '') +
            '</div>';
    }

    function renderRows() {
        var html = [];

        $.each(state.rows, function (_, row) {
            html.push(
                '<tr>' +
                '<td><time class="ah-time">' + escapeHtml(formatOccurredAt(row.occurredAt)) + '</time></td>' +
                '<td>' + actorHtml(row) + '</td>' +
                '<td>' + menuHtml(row) + '</td>' +
                '<td>' + actionHtml(row) + '</td>' +
                '<td>' + resourceHtml(row) + '</td>' +
                '<td>' + reasonHtml(row) + '</td>' +
                '<td><span class="ah-ip">' + escapeHtml(text(row.clientIp)) + '</span></td>' +
                '</tr>'
            );
        });

        $('#accessHistoryTableBody').html(html.length
            ? html.join('')
            : '<tr class="ah-empty-row"><td colspan="7">' +
                escapeHtml(t('feature.history.access.empty', '검색 조건에 해당하는 접근이력이 없습니다.')) +
                '</td></tr>');
        $('#accessHistoryCount').text(t('feature.common.count', '{0}건', formatNumber(state.rows.length)));
    }

    function showMessage(message) {
        $('#accessHistoryMessage').text(message).prop('hidden', !message);
    }

    function setLoading(loading) {
        var $button = $('#accessSearchButton');
        $button.prop('disabled', loading);
        if (loading) {
            $button.data('original-html', $button.html())
                .html('<span class="ah-spinner" aria-hidden="true"></span>' +
                    escapeHtml(t('feature.common.searching', '조회 중')));
        } else if ($button.data('original-html')) {
            $button.html($button.data('original-html'));
        }
    }

    function errorMessage(xhr) {
        if (xhr && xhr.status === 403) {
            return t('feature.history.access.forbidden',
                '접근이력 메뉴 권한이 없습니다. 시스템 관리의 메뉴권한 배정을 확인해 주세요.');
        }
        if (xhr && xhr.responseJSON) {
            var fallback = t('feature.history.access.loadFailed', '접근이력을 불러오지 못했습니다.');
            var serverMessage = xhr.responseJSON.message || xhr.responseJSON.failReason;
            if (serverMessage && /^feature\./.test(serverMessage)) {
                return t(serverMessage, fallback);
            }
            if (serverMessage && /^ko(?:-|$)/i.test(document.documentElement.lang || 'ko')) {
                return serverMessage;
            }
            return fallback;
        }
        return t('feature.history.access.loadRetry',
            '접근이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    }

    function loadAccessHistory() {
        showMessage('');
        setLoading(true);
        $('#accessHistoryTableBody').html(
            '<tr class="ah-loading-row"><td colspan="7"><span class="ah-spinner" aria-hidden="true"></span>' +
            escapeHtml(t('feature.history.access.loading', '접근이력을 불러오는 중입니다.')) + '</td></tr>'
        );

        $.ajax({
            url: endpoint,
            method: 'GET',
            dataType: 'json',
            cache: false,
            data: {
                keyword: $.trim($('#accessKeyword').val()),
                eventType: $('#accessEventType').val()
            }
        }).done(function (response) {
            state.rows = normalizeRows(response);
            renderRows();
        }).fail(function (xhr) {
            state.rows = [];
            renderRows();
            showMessage(errorMessage(xhr));
        }).always(function () {
            setLoading(false);
        });
    }

    function resetSearch() {
        $('#accessEventType').val('');
        $('#accessKeyword').val('');
        loadAccessHistory();
    }

    $(function () {
        $('#accessHistorySearchForm').on('submit', function (event) {
            event.preventDefault();
            loadAccessHistory();
        });
        $('#accessResetButton').on('click', resetSearch);
        loadAccessHistory();
    });
})(window.jQuery, window, document);
