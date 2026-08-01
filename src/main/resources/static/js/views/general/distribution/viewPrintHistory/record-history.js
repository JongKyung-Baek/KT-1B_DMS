(function ($, window) {
    'use strict';

    var config = window.recordHistoryConfig || {};
    var mode = config.mode === 'print' ? 'print' : 'view';
    var endpoint = (config.contextPath || '') + (config.endpoint || '');
    var columnCount = mode === 'print' ? 7 : 6;
    var state = { rows: [] };

    var typeLabels = {
        SW: ['feature.history.type.technicalData', '기술자료'],
        CCB: ['feature.history.type.technicalData', '기술자료'],
        TECHNICAL_DATA: ['feature.history.type.technicalData', '기술자료'],
        '기술자료관리': ['feature.history.type.technicalData', '기술자료'],
        MERGE: ['feature.history.type.mergePrint', '병합 출력'],
        FUNCTIONCODE: ['feature.history.type.document', '문서'],
        DOCUMENTS: ['feature.history.type.document', '문서']
    };

    var viewMenuLabels = {
        TECHNICAL_DATA_SEARCH: ['feature.history.menu.technicalDataSearch', '기술자료관리 > 조회'],
        DOCUMENT_SEARCH: ['feature.history.menu.documentSearch', '문서 > 조회']
    };

    var viewActionLabels = {
        VIEW: ['feature.history.action.view', '열람']
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

    function localizedType(value, fallback) {
        var entry = typeLabels[String(value || '').toUpperCase()];
        return entry ? t(entry[0], entry[1]) : text(value, fallback);
    }

    function localizedCode(labels, value, fallback) {
        var entry = labels[String(value || '').toUpperCase()];
        return entry ? t(entry[0], entry[1]) : text(value, fallback);
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

    function formatTime(value) {
        var formatted = text(value);
        return formatted === '-' ? formatted : formatted.replace('T', ' ');
    }

    function actorHtml(row) {
        var name = text(row.actorUserNm, '');
        var account = text(row.actorUserId || row.actorUserCd, '');
        var primary = name || account || '-';
        var secondary = name && account && name !== account ? account : text(row.actorUserCd, '');
        return cellHtml(primary, secondary);
    }

    function cellHtml(primary, secondary, extra) {
        var first = text(primary);
        var second = text(secondary, '');
        return '<div class="ah-cell">' +
            '<span class="ah-cell__primary" title="' + escapeHtml(first) + '">' + escapeHtml(first) + '</span>' +
            (second && second !== first
                ? '<span class="ah-cell__secondary" title="' + escapeHtml(second) + '">' + escapeHtml(second) + '</span>'
                : '') +
            (extra || '') +
            '</div>';
    }

    function timeHtml(row) {
        var requested = formatTime(row.occurredAt);
        var completed = formatTime(row.completedAt);
        return '<div class="ah-cell">' +
            '<time class="ah-time">' + escapeHtml(requested) + '</time>' +
            (mode === 'print' && completed !== '-'
                ? '<span class="ah-cell__secondary">' +
                    escapeHtml(t('feature.history.value.completedAt', '완료 {0}', completed)) + '</span>'
                : '') +
            '</div>';
    }

    function typeHtml(row) {
        var raw = String(row.objectType || '').toUpperCase();
        var label = localizedType(raw || row.requestType,
            t('feature.history.type.technicalData', '기술자료'));
        return '<span class="ah-event-chip">' + escapeHtml(label) + '</span>';
    }

    function resourceHtml(row) {
        return cellHtml(
            row.requestNo || row.objectId || row.printJobId,
            row.printJobId
                ? t('feature.history.value.job', '작업 {0}', row.printJobId)
                : row.objectId);
    }

    function fileHtml(row) {
        var secondary = [];
        if (row.itemSeq != null) {
            secondary.push(t('feature.history.value.item', '항목 {0}', row.itemSeq));
        }
        if (Number(row.itemCount || 0) > 1) {
            secondary.push(t('feature.history.value.totalItems', '총 {0}개', row.itemCount));
        }
        return cellHtml(row.fileNo, secondary.join(' · '));
    }

    function viewMenuHtml(row) {
        return cellHtml(localizedCode(
            viewMenuLabels,
            row.menuCd,
            localizedType(row.distributionType,
                t('feature.history.type.technicalData', '기술자료'))));
    }

    function viewActionHtml(row) {
        return '<span class="ah-event-chip">' + escapeHtml(localizedCode(
            viewActionLabels, row.actionType, t('feature.history.action.view', '열람'))) + '</span>';
    }

    function viewDocumentHtml(row) {
        return cellHtml(row.drawingNo || row.objectId, row.objectId);
    }

    function viewFileNumberHtml(row) {
        var secondary = [];
        if (row.orgFileNm) secondary.push(row.orgFileNm);
        if (row.revision) secondary.push('Revision ' + row.revision);
        return cellHtml(row.fileNo, secondary.join(' · '));
    }

    function detailHtml(row) {
        var chips = [];
        if (row.pageCount != null) {
            chips.push('<span>' + escapeHtml(t('feature.history.value.pages', '{0}쪽', row.pageCount)) + '</span>');
        }
        if (row.copyCount != null) {
            chips.push('<span>' + escapeHtml(t('feature.history.value.copies', '{0}부', row.copyCount)) + '</span>');
        }
        if (row.printerNm) chips.push('<span>' + escapeHtml(row.printerNm) + '</span>');
        if (row.deviceId) chips.push('<span>' + escapeHtml(row.deviceId) + '</span>');
        var error = text(row.errorMessage, '');

        return '<div class="ah-cell">' +
            (chips.length ? '<div class="rh-detail-chips">' + chips.join('') + '</div>' :
                '<span class="ah-cell__secondary">' +
                    escapeHtml(t('feature.history.detail.none', '상세 정보 없음')) + '</span>') +
            (error ? '<span class="rh-error" title="' + escapeHtml(error) + '">' + escapeHtml(error) + '</span>' : '') +
            '</div>';
    }

    function renderRows() {
        var html = [];

        $.each(state.rows, function (_, row) {
            if (mode === 'view') {
                html.push(
                    '<tr>' +
                    '<td>' + timeHtml(row) + '</td>' +
                    '<td>' + actorHtml(row) + '</td>' +
                    '<td>' + viewMenuHtml(row) + '</td>' +
                    '<td>' + viewActionHtml(row) + '</td>' +
                    '<td>' + viewDocumentHtml(row) + '</td>' +
                    '<td>' + viewFileNumberHtml(row) + '</td>' +
                    '</tr>'
                );
                return;
            }

            html.push(
                '<tr>' +
                '<td>' + timeHtml(row) + '</td>' +
                '<td>' + actorHtml(row) + '</td>' +
                '<td>' + typeHtml(row) + '</td>' +
                '<td>' + resourceHtml(row) + '</td>' +
                '<td>' + fileHtml(row) + '</td>' +
                '<td>' + detailHtml(row) + '</td>' +
                '<td><span class="ah-ip">' + escapeHtml(text(row.clientIp)) + '</span></td>' +
                '</tr>'
            );
        });

        $('#recordHistoryTableBody').html(html.length
            ? html.join('')
            : '<tr class="ah-empty-row"><td colspan="' + columnCount + '">' +
                escapeHtml(t(mode === 'print' ? 'feature.history.print.empty' : 'feature.history.view.empty',
                    mode === 'print'
                        ? '검색 조건에 해당하는 출력이력이 없습니다.'
                        : '검색 조건에 해당하는 열람이력이 없습니다.')) +
                '</td></tr>');
        $('#recordHistoryCount').text(t('feature.common.count', '{0}건', formatNumber(state.rows.length)));
    }

    function showMessage(message) {
        $('#recordHistoryMessage').text(message).prop('hidden', !message);
    }

    function setLoading(loading) {
        var $button = $('#recordSearchButton');
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
            return t(mode === 'print' ? 'feature.history.print.forbidden' : 'feature.history.view.forbidden',
                mode === 'print'
                    ? '출력이력 메뉴 권한이 없습니다. 시스템 관리의 메뉴권한 배정을 확인해 주세요.'
                    : '열람이력 메뉴 권한이 없습니다. 시스템 관리의 메뉴권한 배정을 확인해 주세요.');
        }
        return t(mode === 'print' ? 'feature.history.print.loadFailed' : 'feature.history.view.loadFailed',
            mode === 'print'
                ? '출력이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
                : '열람이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    }

    function loadHistory() {
        showMessage('');
        setLoading(true);
        $('#recordHistoryTableBody').html(
            '<tr class="ah-loading-row"><td colspan="' + columnCount + '"><span class="ah-spinner" aria-hidden="true"></span>' +
            escapeHtml(t(mode === 'print' ? 'feature.history.print.loading' : 'feature.history.view.loading',
                mode === 'print'
                    ? '출력이력을 불러오는 중입니다.'
                    : '열람이력을 불러오는 중입니다.')) + '</td></tr>'
        );

        var requestData = {
            keyword: $.trim($('#recordKeyword').val())
        };
        if (mode === 'view') {
            requestData.scope = $('#recordScope').val();
        }

        $.ajax({
            url: endpoint,
            method: 'GET',
            dataType: 'json',
            cache: false,
            data: requestData
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

    $(function () {
        $('#recordHistorySearchForm').on('submit', function (event) {
            event.preventDefault();
            loadHistory();
        });
        $('#recordResetButton').on('click', function () {
            if (mode === 'view') {
                $('#recordScope').val('');
            }
            $('#recordKeyword').val('');
            loadHistory();
        });
        loadHistory();
    });
})(window.jQuery, window);
