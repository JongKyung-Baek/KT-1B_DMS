(function ($, window, document) {
    'use strict';

    var config = window.accessHistoryConfig || {};
    var contextPath = config.contextPath || '';
    var endpoint = contextPath + '/inside/distribution/viewPrintHistory/accessEvents';
    var state = {
        rows: []
    };

    var eventLabels = {
        FILE_ACCESS: '자료 접근',
        DOWNLOAD_RESULT: '다운로드 결과',
        PRINT_RESULT: '출력 결과',
        ACL_CHANGE: '권한 변경'
    };

    var actionLabels = {
        LIST: '목록 조회',
        DETAIL: '상세 조회',
        VIEW: '열람',
        DOWNLOAD_ORIGINAL: '원본 다운로드',
        PRINT: '출력',
        MANAGE_GRADE: '보안등급 변경',
        MANAGE_CLEARANCE: '사용자 인가 변경',
        MANAGE_USER_CLEARANCE: '사용자 인가 변경',
        MANAGE_FILE_LABEL: '문서등급 변경',
        MANAGE_FILE_PERMISSION: '문서권한 변경',
        MANAGE_DOCUMENT_PERMISSION: '문서권한 변경',
        MANAGE_ACL: '접근권한 관리'
    };

    var resultLabels = {
        ALLOW: '허용',
        SUCCESS: '성공',
        DENY: '차단',
        FAIL: '실패',
        FAILED: '실패',
        FAILURE: '실패',
        ERROR: '오류',
        CANCELLED: '취소',
        STARTED: '시작'
    };

    var reasonLabels = {
        ACCESS_ALLOWED: '접근 정책 허용',
        ACTION_NOT_ALLOWED: '행위 권한 없음',
        CLEARANCE_TOO_LOW: '사용자 인가등급 부족',
        FILE_GRADE_REQUIRED: '문서 보안등급 미지정',
        FILE_PERMISSION_DENIED: '문서별 사용자 권한 없음',
        RESOURCE_NOT_FOUND: '대상 자료를 찾을 수 없음',
        INVALID_REQUEST: '잘못된 요청',
        BUSINESS_FAILURE: '업무 처리 실패'
    };

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
        return Number(value || 0).toLocaleString('ko-KR');
    }

    function eventLabel(code) {
        return eventLabels[String(code || '').toUpperCase()] || text(code);
    }

    function actionLabel(code, fallback) {
        var normalized = String(code || '').toUpperCase();
        return actionLabels[normalized] || text(fallback || code);
    }

    function resultLabel(code) {
        return resultLabels[String(code || '').toUpperCase()] || text(code);
    }

    function resultClass(code) {
        var normalized = String(code || '').toUpperCase();
        if (normalized === 'ALLOW' || normalized === 'SUCCESS') return 'success';
        if (normalized === 'DENY' || normalized === 'FAIL' || normalized === 'FAILED' ||
                normalized === 'FAILURE' || normalized === 'ERROR') return 'danger';
        if (normalized === 'STARTED') return 'info';
        if (normalized === 'CANCELLED') return 'warning';
        return 'neutral';
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
        var primary = menu || (route ? '요청 경로' : '-');
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

    function resultHtml(row) {
        var result = text(row.resultCd, '');
        var cssClass = resultClass(result);
        return '<span class="ah-result-chip ah-result-chip--' + cssClass + '">' +
            escapeHtml(resultLabel(result)) + '</span>';
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
        if (fileNo && fileNo !== '*') metadata.push('파일 ' + fileNo);

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
        var primary = message || reasonLabels[reasonCode] || reasonCode || '-';
        var secondary = reasonCode && primary !== reasonCode ? reasonLabels[reasonCode] || reasonCode : '';

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
                '<td>' + resultHtml(row) + '</td>' +
                '<td>' + resourceHtml(row) + '</td>' +
                '<td>' + reasonHtml(row) + '</td>' +
                '<td><span class="ah-ip">' + escapeHtml(text(row.clientIp)) + '</span></td>' +
                '</tr>'
            );
        });

        $('#accessHistoryTableBody').html(html.length
            ? html.join('')
            : '<tr class="ah-empty-row"><td colspan="8">검색 조건에 해당하는 접근이력이 없습니다.</td></tr>');
        $('#accessHistoryCount').text(formatNumber(state.rows.length) + '건');
    }

    function renderSummary() {
        var success = 0;
        var denied = 0;
        var users = {};

        $.each(state.rows, function (_, row) {
            var result = String(row.resultCd || '').toUpperCase();
            var user = row.actorUserCd || row.actorUserId || row.actorUserNm;

            if (result === 'ALLOW' || result === 'SUCCESS') success += 1;
            if (result === 'DENY' || result === 'FAIL' || result === 'FAILED' ||
                    result === 'FAILURE' || result === 'ERROR') denied += 1;
            if (user) users[user] = true;
        });

        $('#accessMetricTotal').text(formatNumber(state.rows.length));
        $('#accessMetricSuccess').text(formatNumber(success));
        $('#accessMetricDenied').text(formatNumber(denied));
        $('#accessMetricUsers').text(formatNumber(Object.keys(users).length));
    }

    function showMessage(message) {
        $('#accessHistoryMessage').text(message).prop('hidden', !message);
    }

    function setLoading(loading) {
        var $button = $('#accessSearchButton');
        $button.prop('disabled', loading);
        if (loading) {
            $button.data('original-html', $button.html())
                .html('<span class="ah-spinner" aria-hidden="true"></span>조회 중');
        } else if ($button.data('original-html')) {
            $button.html($button.data('original-html'));
        }
    }

    function errorMessage(xhr) {
        if (xhr && xhr.status === 403) {
            return '접근이력 메뉴 권한이 없습니다. 시스템 관리의 메뉴권한 배정을 확인해 주세요.';
        }
        if (xhr && xhr.responseJSON) {
            return xhr.responseJSON.message || xhr.responseJSON.failReason || '접근이력을 불러오지 못했습니다.';
        }
        return '접근이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
    }

    function loadAccessHistory() {
        showMessage('');
        setLoading(true);
        $('#accessHistoryTableBody').html(
            '<tr class="ah-loading-row"><td colspan="8"><span class="ah-spinner" aria-hidden="true"></span>' +
            '접근이력을 불러오는 중입니다.</td></tr>'
        );

        $.ajax({
            url: endpoint,
            method: 'GET',
            dataType: 'json',
            cache: false,
            data: {
                keyword: $.trim($('#accessKeyword').val()),
                eventType: $('#accessEventType').val(),
                resultCd: $('#accessResultCd').val()
            }
        }).done(function (response) {
            state.rows = normalizeRows(response);
            renderRows();
            renderSummary();
            $('#accessHistoryUpdatedAt').text(
                '마지막 조회 ' + new Date().toLocaleString('ko-KR', {
                    month: '2-digit',
                    day: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                })
            );
        }).fail(function (xhr) {
            state.rows = [];
            renderRows();
            renderSummary();
            showMessage(errorMessage(xhr));
            $('#accessHistoryUpdatedAt').text('조회에 실패했습니다.');
        }).always(function () {
            setLoading(false);
        });
    }

    function resetSearch() {
        $('#accessEventType, #accessResultCd').val('');
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
