(function ($, window, document) {
    'use strict';

    var config = window.securityAccessConfig || {};
    var contextPath = config.contextPath || '';
    var apiBase = contextPath + '/inside/system/securityaccess/api';
    var permissionCodes = ['LIST', 'DETAIL', 'VIEW', 'DOWNLOAD_ORIGINAL', 'PRINT', 'MANAGE_ACL'];
    var state = {
        grades: [],
        users: [],
        files: [],
        filePermissions: [],
        selectedUser: null,
        selectedFile: null,
        selectedFileNo: null,
        filePermissionRequestKey: null,
        selectedGradeCd: null
    };

    function escapeHtml(value) {
        return $('<div>').text(value == null ? '' : String(value)).html()
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function valueOf(source, keys, fallback) {
        var result = fallback;
        $.each(keys, function (_, key) {
            if (source && source[key] !== undefined && source[key] !== null) {
                result = source[key];
                return false;
            }
        });
        return result;
    }

    function isYes(value) {
        return value === true || value === 1 || value === '1' || String(value || '').toUpperCase() === 'Y' || String(value || '').toLowerCase() === 'true';
    }

    function normalizeList(response) {
        if ($.isArray(response)) return response;
        if (!response) return [];
        var candidates = [response.data, response.items, response.list, response.rows, response.content, response.result];
        for (var i = 0; i < candidates.length; i += 1) {
            if ($.isArray(candidates[i])) return candidates[i];
            if (candidates[i] && $.isArray(candidates[i].rows)) return candidates[i].rows;
            if (candidates[i] && $.isArray(candidates[i].items)) return candidates[i].items;
        }
        return [];
    }

    function csrfHeaders() {
        var token = $('meta[name="_csrf"], meta[name="csrf-token"], meta[name="csrfToken"]').first().attr('content');
        var headerName = $('meta[name="_csrf_header"], meta[name="csrf-header"], meta[name="csrfHeader"]').first().attr('content');
        var headers = {};
        if (token) headers[headerName || 'X-CSRF-TOKEN'] = token;
        return headers;
    }

    function apiRequest(method, path, payload) {
        var options = {
            url: apiBase + path,
            method: method,
            dataType: 'json',
            headers: csrfHeaders(),
            cache: false
        };
        if (method === 'GET') {
            options.data = payload || {};
        } else {
            options.contentType = 'application/json; charset=UTF-8';
            options.data = JSON.stringify(payload || {});
        }
        return $.ajax(options).then(function (response) {
            if (response && response.success === false) {
                return $.Deferred().reject({ responseJSON: response }).promise();
            }
            return response;
        });
    }

    function errorMessage(xhr) {
        var body = xhr && xhr.responseJSON ? xhr.responseJSON : {};
        return valueOf(body, ['message', 'failReason', 'error', 'detail'], '') ||
            (xhr && xhr.responseText ? xhr.responseText : '') ||
            (xhr && xhr.status ? '요청 처리에 실패했습니다. (HTTP ' + xhr.status + ')' : '요청 처리에 실패했습니다.');
    }

    var messageTimer;
    function showMessage(type, message) {
        window.clearTimeout(messageTimer);
        $('#securityAccessMessage')
            .removeClass('is-success is-error is-info')
            .addClass('is-' + type)
            .text(message)
            .prop('hidden', false);
        if (type === 'success') {
            messageTimer = window.setTimeout(function () {
                $('#securityAccessMessage').prop('hidden', true);
            }, 5000);
        }
    }

    function setBusy($button, busy, busyText) {
        if (!$button || !$button.length) return;
        if (busy) {
            if (!$button.data('normalText')) $button.data('normalText', $button.text());
            $button.prop('disabled', true).addClass('is-loading').text(busyText || '처리 중...');
        } else {
            $button.prop('disabled', false).removeClass('is-loading').text($button.data('normalText') || $button.text());
        }
    }

    function gradeCode(grade) {
        return valueOf(grade, ['gradeCd', 'securityGradeCd', 'code'], '');
    }

    function gradeName(grade) {
        return valueOf(grade, ['gradeNm', 'securityGradeNm', 'name'], gradeCode(grade));
    }

    function gradeNameByCode(code) {
        var name = code || '-';
        $.each(state.grades, function (_, grade) {
            if (gradeCode(grade) === code) {
                name = gradeName(grade);
                return false;
            }
        });
        return name || '-';
    }

    function renderGradeOptions() {
        var selectedUserGrade = $('#userGradeCd').val();
        var selectedFileGrade = $('#fileGradeCd').val();
        var options = ['<option value="">등급 선택</option>'];
        $.each(state.grades, function (_, grade) {
            if (!isYes(valueOf(grade, ['useYn', 'active'], 'Y'))) return;
            options.push('<option value="' + escapeHtml(gradeCode(grade)) + '">' +
                escapeHtml(gradeName(grade)) + ' (' + escapeHtml(valueOf(grade, ['gradeLevel', 'level', 'rank'], '0')) + ')' +
                '</option>');
        });
        $('#userGradeCd, #fileGradeCd').html(options.join(''));
        if (selectedUserGrade) $('#userGradeCd').val(selectedUserGrade);
        if (selectedFileGrade) $('#fileGradeCd').val(selectedFileGrade);
    }

    function renderGrades() {
        var rows = [];
        $.each(state.grades, function (index, grade) {
            var cd = gradeCode(grade);
            var selected = state.selectedGradeCd === cd ? ' is-selected' : '';
            rows.push('<tr class="sa-selectable-row' + selected + '" tabindex="0" data-grade-index="' + index + '">' +
                '<td><span class="sa-rank">' + escapeHtml(valueOf(grade, ['gradeLevel', 'level', 'rank'], '0')) + '</span></td>' +
                '<td><code>' + escapeHtml(cd) + '</code></td>' +
                '<td>' + escapeHtml(gradeName(grade)) + '</td>' +
                '<td>' + (isYes(valueOf(grade, ['defaultYn', 'isDefault'], 'N')) ? '<span class="sa-badge sa-badge--primary">기본</span>' : '-') + '</td>' +
                '<td>' + (isYes(valueOf(grade, ['useYn', 'active'], 'Y')) ? '<span class="sa-badge sa-badge--success">사용</span>' : '<span class="sa-badge">중지</span>') + '</td>' +
                '</tr>');
        });
        $('#gradeTableBody').html(rows.length ? rows.join('') : '<tr class="sa-empty-row"><td colspan="5">등록된 보안등급이 없습니다.</td></tr>');
        renderGradeOptions();
    }

    function resetGradeForm() {
        state.selectedGradeCd = null;
        $('#gradeOriginalCd, #gradeCd, #gradeNm, #gradeLevel, #gradeDescription').val('');
        $('#gradeCd').prop('disabled', false).focus();
        $('#gradeUseYn').prop('checked', true);
        $('#gradeDefaultYn').prop('checked', false);
        renderGrades();
    }

    function selectGrade(grade) {
        var cd = gradeCode(grade);
        state.selectedGradeCd = cd;
        $('#gradeOriginalCd, #gradeCd').val(cd);
        $('#gradeCd').prop('disabled', true);
        $('#gradeNm').val(gradeName(grade));
        $('#gradeLevel').val(valueOf(grade, ['gradeLevel', 'level', 'rank'], ''));
        $('#gradeDescription').val(valueOf(grade, ['description', 'gradeDescription'], ''));
        $('#gradeUseYn').prop('checked', isYes(valueOf(grade, ['useYn', 'active'], 'Y')));
        $('#gradeDefaultYn').prop('checked', isYes(valueOf(grade, ['defaultYn', 'isDefault'], 'N')));
        renderGrades();
    }

    function loadGrades() {
        setBusy($('#gradeReloadButton'), true, '조회 중...');
        return apiRequest('GET', '/grades').done(function (response) {
            state.grades = normalizeList(response).sort(function (a, b) {
                return Number(valueOf(a, ['gradeLevel', 'level', 'rank'], 0)) - Number(valueOf(b, ['gradeLevel', 'level', 'rank'], 0));
            });
            renderGrades();
        }).fail(function (xhr) {
            $('#gradeTableBody').html('<tr class="sa-empty-row sa-empty-row--error"><td colspan="5">등급을 불러오지 못했습니다.</td></tr>');
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#gradeReloadButton'), false);
        });
    }

    function saveGrade() {
        var gradeCd = $.trim($('#gradeCd').val());
        var gradeNm = $.trim($('#gradeNm').val());
        var levelText = $.trim($('#gradeLevel').val());
        if (!gradeCd || !gradeNm || levelText === '') {
            showMessage('error', '등급 코드, 등급명, 등급 순위를 입력해 주세요.');
            return;
        }
        var payload = {
            gradeCd: gradeCd,
            gradeNm: gradeNm,
            gradeLevel: Number(levelText),
            description: $.trim($('#gradeDescription').val()),
            useYn: $('#gradeUseYn').is(':checked') ? 'Y' : 'N',
            defaultYn: $('#gradeDefaultYn').is(':checked') ? 'Y' : 'N'
        };
        setBusy($('#gradeSaveButton'), true, '저장 중...');
        apiRequest('POST', '/grades', payload).done(function () {
            showMessage('success', '보안등급을 저장했습니다.');
            resetGradeForm();
            loadGrades();
        }).fail(function (xhr) {
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#gradeSaveButton'), false);
        });
    }

    function userCd(user) {
        return valueOf(user, ['userCd', 'id'], '');
    }

    function renderUsers() {
        var rows = [];
        $.each(state.users, function (index, user) {
            var selected = state.selectedUser && userCd(state.selectedUser) === userCd(user) ? ' is-selected' : '';
            var gradeCd = valueOf(user, ['gradeCd', 'securityGradeCd', 'clearanceGradeCd'], '');
            rows.push('<tr class="sa-selectable-row' + selected + '" tabindex="0" data-user-index="' + index + '">' +
                '<td><code>' + escapeHtml(valueOf(user, ['userId', 'username'], userCd(user))) + '</code></td>' +
                '<td>' + escapeHtml(valueOf(user, ['userNm', 'name'], '-')) + '</td>' +
                '<td>' + escapeHtml(valueOf(user, ['deptNm', 'departmentName', 'deptShortPath'], '-')) + '</td>' +
                '<td><span class="sa-badge sa-badge--grade">' + escapeHtml(valueOf(user, ['gradeNm', 'securityGradeNm', 'clearanceGradeNm'], gradeNameByCode(gradeCd))) + '</span></td>' +
                '</tr>');
        });
        $('#userTableBody').html(rows.length ? rows.join('') : '<tr class="sa-empty-row"><td colspan="4">조회된 사용자가 없습니다.</td></tr>');
    }

    function permissionMap(user) {
        var map = {};
        var raw = valueOf(user, ['permissions', 'permissionCodes', 'actions'], {});
        if ($.isArray(raw)) {
            $.each(raw, function (_, code) { map[String(code).toUpperCase()] = true; });
        } else if (typeof raw === 'string') {
            $.each(raw.split(','), function (_, code) { map[$.trim(code).toUpperCase()] = true; });
        } else if (raw) {
            $.each(raw, function (code, allowed) { map[String(code).toUpperCase()] = isYes(allowed); });
        }
        var aliases = {
            LIST: ['listYn'], DETAIL: ['detailYn'], VIEW: ['viewYn'],
            DOWNLOAD_ORIGINAL: ['downloadOriginalYn'], PRINT: ['printYn'], MANAGE_ACL: ['manageAclYn']
        };
        $.each(aliases, function (code, keys) {
            var value = valueOf(user, keys, undefined);
            if (value !== undefined) map[code] = isYes(value);
        });
        return map;
    }

    function dateValue(value) {
        if (!value) return '';
        return String(value).substring(0, 10);
    }

    function selectUser(user) {
        state.selectedUser = user;
        renderUsers();
        $('#selectedUserSummary').removeClass('is-empty').html(
            '<strong>' + escapeHtml(valueOf(user, ['userNm', 'name'], '-')) + '</strong>' +
            '<span>' + escapeHtml(valueOf(user, ['userId', 'username'], userCd(user))) + ' · ' +
            escapeHtml(valueOf(user, ['deptNm', 'departmentName', 'deptShortPath'], '부서 없음')) + '</span>'
        );
        $('#userGradeCd').prop('disabled', false).val(valueOf(user, ['gradeCd', 'securityGradeCd', 'clearanceGradeCd'], ''));
        $('#clearanceValidFrom').prop('disabled', false).val(dateValue(valueOf(user, ['validFrom'], '')));
        $('#clearanceValidTo').prop('disabled', false).val(dateValue(valueOf(user, ['validTo'], '')));
        $('#clearanceGrantReason').prop('disabled', false).val(valueOf(user, ['grantReason'], ''));
        $('.sa-permission-fieldset').prop('disabled', false);
        $('#clearanceSaveButton').prop('disabled', false);
        var allowed = permissionMap(user);
        $('input[name="accessPermission"]').each(function () {
            $(this).prop('checked', !!allowed[$(this).val()]);
        });
    }

    function loadUsers() {
        setBusy($('#userSearchButton'), true, '조회 중...');
        state.selectedUser = null;
        disableClearanceForm();
        apiRequest('GET', '/users', { keyword: $.trim($('#userKeyword').val()) }).done(function (response) {
            state.users = normalizeList(response);
            renderUsers();
        }).fail(function (xhr) {
            $('#userTableBody').html('<tr class="sa-empty-row sa-empty-row--error"><td colspan="4">사용자를 불러오지 못했습니다.</td></tr>');
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#userSearchButton'), false);
        });
    }

    function disableClearanceForm() {
        $('#selectedUserSummary').addClass('is-empty').text('왼쪽 목록에서 사용자를 선택하세요.');
        $('#userGradeCd, #clearanceValidFrom, #clearanceValidTo, #clearanceGrantReason, #clearanceSaveButton').prop('disabled', true);
        $('.sa-permission-fieldset').prop('disabled', true);
        $('#clearanceForm')[0].reset();
    }

    function saveClearance() {
        if (!state.selectedUser) {
            showMessage('error', '인가정보를 설정할 사용자를 선택해 주세요.');
            return;
        }
        var gradeCd = $('#userGradeCd').val();
        var validFrom = $('#clearanceValidFrom').val();
        var validTo = $('#clearanceValidTo').val();
        var reason = $.trim($('#clearanceGrantReason').val());
        if (!gradeCd) {
            showMessage('error', '최대 인가등급을 선택해 주세요.');
            return;
        }
        if (validFrom && validTo && validFrom > validTo) {
            showMessage('error', '유효 종료일은 시작일보다 빠를 수 없습니다.');
            return;
        }
        if (!reason) {
            showMessage('error', '인가등급 부여 또는 변경 사유를 입력해 주세요.');
            return;
        }
        var permissions = {};
        $.each(permissionCodes, function (_, code) { permissions[code] = 'N'; });
        $('input[name="accessPermission"]:checked').each(function () { permissions[$(this).val()] = 'Y'; });
        var anyAllowed = false;
        $.each(permissions, function (_, allowed) { if (allowed === 'Y') anyAllowed = true; });
        if (!anyAllowed) {
            showMessage('error', '하나 이상의 행위권한을 선택해 주세요.');
            return;
        }
        var payload = {
            userCd: userCd(state.selectedUser),
            gradeCd: gradeCd,
            permissions: permissions,
            validFrom: validFrom || null,
            validTo: validTo || null,
            grantReason: reason
        };
        setBusy($('#clearanceSaveButton'), true, '저장 중...');
        apiRequest('POST', '/users/clearance', payload).done(function () {
            showMessage('success', '사용자 인가정보를 저장했습니다.');
            loadUsers();
        }).fail(function (xhr) {
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#clearanceSaveButton'), false);
            if (!state.selectedUser) $('#clearanceSaveButton').prop('disabled', true);
        });
    }

    function fileId(file) {
        return valueOf(file, ['objectId', 'id'], '');
    }

    function fileKey(file) {
        return [
            valueOf(file, ['objectType', 'type'], ''),
            fileId(file),
            valueOf(file, ['fileNo'], '*') || '*'
        ].join('|');
    }

    function fileObjectType(file) {
        return valueOf(file, ['objectType', 'type'], $('#fileObjectType').val());
    }

    function fileObjectTypeLabel(objectType) {
        var labels = {
            SW: '기술자료',
            SW_SUB: '기술자료 보조파일',
            DOCUMENT: '일반문서',
            DRAWING: '도면',
            PRODUCT_DOCUMENT: '생산기술문서',
            PRODUCT_SW: '생산기술 소프트웨어',
            DXF: 'DXF',
            PEER_REVIEW: 'Peer Review',
            DOCUMENT_SUB: '일반문서 보조파일',
            DRAWING_SUB: '도면 보조파일',
            PRODUCT_DOCUMENT_SUB: '생산기술문서 보조파일',
            PRODUCT_SW_SUB: '생산기술 SW 보조파일',
            DXF_SUB: 'DXF 보조파일'
        };
        return labels[objectType] || objectType || '-';
    }

    function selectedFileResource() {
        if (!state.selectedFile) return null;
        return {
            objectType: fileObjectType(state.selectedFile),
            objectId: fileId(state.selectedFile),
            fileNo: state.selectedFileNo || valueOf(state.selectedFile, ['fileNo'], '*') || '*'
        };
    }

    function filePermissionField(action) {
        if (action === 'VIEW') return 'viewYn';
        if (action === 'DOWNLOAD_ORIGINAL') return 'downloadOriginalYn';
        return 'printYn';
    }

    function globalPermissionField(action) {
        if (action === 'VIEW') return 'globalViewYn';
        if (action === 'DOWNLOAD_ORIGINAL') return 'globalDownloadOriginalYn';
        return 'globalPrintYn';
    }

    function permissionActionName(action) {
        if (action === 'VIEW') return '열람';
        if (action === 'DOWNLOAD_ORIGINAL') return '원본 다운로드';
        return '출력';
    }

    function renderFilePermissionCheckbox(permission, index, action) {
        var accountActive = isYes(valueOf(permission, ['accountActiveYn'], 'N'));
        var eligible = isYes(valueOf(permission, ['gradeEligibleYn'], 'N'));
        var globallyAllowed = isYes(valueOf(permission, [globalPermissionField(action)], 'N'));
        var viewAvailable = isYes(valueOf(permission, ['globalViewYn'], 'N'));
        var disabled = !accountActive || !eligible || !globallyAllowed ||
            (action !== 'VIEW' && !viewAvailable);
        var checked = isYes(valueOf(permission, [filePermissionField(action)], 'N'));
        var title = '';
        if (!accountActive) {
            title = '사용 중지·잠금·삭제 계정에는 새 권한을 부여할 수 없습니다. 전체 회수 버튼으로 기존 권한을 회수하세요.';
        } else if (!eligible) {
            title = '사용자 인가등급이 문서등급보다 낮습니다.';
        } else if (!globallyAllowed) {
            title = '사용자 전역 ' + permissionActionName(action) + ' 권한이 없습니다.';
        } else if (action !== 'VIEW' && !viewAvailable) {
            title = '사용자 전역 열람 권한이 먼저 필요합니다.';
        }
        return '<label class="sa-matrix-check' + (disabled ? ' is-disabled' : '') + '"' +
            (title ? ' title="' + escapeHtml(title) + '"' : '') + '>' +
            '<input type="checkbox" class="sa-file-permission-checkbox"' +
            ' data-permission-index="' + index + '" data-permission-action="' + escapeHtml(action) + '"' +
            (checked ? ' checked' : '') + (disabled ? ' disabled' : '') +
            ' aria-label="' + escapeHtml(permissionActionName(action)) + '">' +
            '<span aria-hidden="true"></span></label>';
    }

    function updateFilePermissionSummary() {
        if (!state.selectedFile) {
            $('#filePermissionSummary').addClass('is-empty').text('위 목록에서 파일을 선택하세요.');
            return;
        }
        var resource = selectedFileResource();
        var permission = state.filePermissions.length ? state.filePermissions[0] : {};
        var objectNo = valueOf(state.selectedFile, ['objectNo', 'documentNo', 'drawingNo', 'swNo'], resource.objectId);
        var fileName = valueOf(state.selectedFile, ['orgFileNm', 'fileNm'], '파일명 없음');
        var fileGrade = valueOf(permission, ['fileGradeNm'], '') ||
            valueOf(state.selectedFile, ['gradeNm', 'securityGradeNm'], '미지정');
        $('#filePermissionSummary').removeClass('is-empty').html(
            '<strong>' + escapeHtml(objectNo) + ' · ' + escapeHtml(fileName) + '</strong>' +
            '<span>' + escapeHtml(resource.objectType) + ' · 파일번호 ' + escapeHtml(resource.fileNo) +
            ' · 문서등급 ' + escapeHtml(fileGrade) + '</span>'
        );
    }

    function renderFilePermissions() {
        var rows = [];
        $.each(state.filePermissions, function (index, permission) {
            if (isYes(valueOf(permission, ['downloadOriginalYn'], 'N')) ||
                isYes(valueOf(permission, ['printYn'], 'N'))) {
                permission.viewYn = 'Y';
            }
            var accountActive = isYes(valueOf(permission, ['accountActiveYn'], 'N'));
            var eligible = isYes(valueOf(permission, ['gradeEligibleYn'], 'N'));
            var userGradeCd = valueOf(permission, ['userGradeCd'], '');
            var userGradeNm = valueOf(permission, ['userGradeNm'], '') || gradeNameByCode(userGradeCd);
            var hasPermission = isYes(valueOf(permission, ['viewYn'], 'N')) ||
                isYes(valueOf(permission, ['downloadOriginalYn'], 'N')) ||
                isYes(valueOf(permission, ['printYn'], 'N'));
            var eligibilityLabel = accountActive ? (eligible ? '충족' : '미충족') : '계정중지';
            rows.push('<tr>' +
                '<td><code>' + escapeHtml(valueOf(permission, ['userId'], valueOf(permission, ['userCd'], '-'))) + '</code></td>' +
                '<td>' + escapeHtml(valueOf(permission, ['userNm'], '-')) + '</td>' +
                '<td>' + escapeHtml(valueOf(permission, ['deptNm'], '-')) + '</td>' +
                '<td><span class="sa-badge sa-badge--grade">' + escapeHtml(userGradeNm || '-') + '</span></td>' +
                '<td><span class="sa-badge ' + (accountActive && eligible ? 'sa-badge--success' : 'sa-badge--danger') + '">' +
                escapeHtml(eligibilityLabel) + '</span></td>' +
                '<td>' + renderFilePermissionCheckbox(permission, index, 'VIEW') + '</td>' +
                '<td>' + renderFilePermissionCheckbox(permission, index, 'DOWNLOAD_ORIGINAL') + '</td>' +
                '<td>' + renderFilePermissionCheckbox(permission, index, 'PRINT') + '</td>' +
                '<td>' + (hasPermission
                    ? '<button type="button" class="sa-button sa-button--danger sa-file-permission-revoke"' +
                      ' data-permission-index="' + index + '">회수</button>'
                    : '-') + '</td>' +
                '</tr>');
        });
        $('#filePermissionTableBody').html(rows.length ? rows.join('') :
            '<tr class="sa-empty-row"><td colspan="9">권한을 지정할 사용자가 없습니다.</td></tr>');
        updateFilePermissionSummary();
        $('#filePermissionCard').removeClass('is-disabled').attr('aria-disabled', 'false');
        $('#filePermissionChangeReason, #filePermissionSaveButton').prop('disabled', !rows.length);
    }

    function disableFilePermissionForm(message) {
        state.filePermissions = [];
        state.filePermissionRequestKey = null;
        $('#filePermissionCard').addClass('is-disabled').attr('aria-disabled', 'true');
        $('#filePermissionTableBody').html(
            '<tr class="sa-empty-row"><td colspan="9">' +
            escapeHtml(message || '파일을 선택하면 사용자 권한을 조회합니다.') + '</td></tr>'
        );
        $('#filePermissionChangeReason').val('').prop('disabled', true);
        $('#filePermissionSaveButton').prop('disabled', true);
        updateFilePermissionSummary();
    }

    function loadFilePermissions() {
        var resource = selectedFileResource();
        if (!resource) {
            disableFilePermissionForm();
            return $.Deferred().resolve().promise();
        }
        var requestKey = [resource.objectType, resource.objectId, resource.fileNo].join('|');
        state.filePermissionRequestKey = requestKey;
        state.filePermissions = [];
        $('#filePermissionCard').removeClass('is-disabled').attr('aria-disabled', 'false');
        $('#filePermissionTableBody').html(
            '<tr class="sa-empty-row"><td colspan="9">사용자 권한을 불러오는 중입니다.</td></tr>'
        );
        $('#filePermissionChangeReason').val('').prop('disabled', true);
        $('#filePermissionSaveButton').prop('disabled', true);
        updateFilePermissionSummary();
        return apiRequest('GET', '/files/permissions', resource).done(function (response) {
            if (state.filePermissionRequestKey !== requestKey) return;
            state.filePermissions = normalizeList(response);
            $.each(state.filePermissions, function (_, permission) {
                if (isYes(valueOf(permission, ['downloadOriginalYn'], 'N')) ||
                    isYes(valueOf(permission, ['printYn'], 'N'))) {
                    permission.viewYn = 'Y';
                }
                permission._originalViewYn = isYes(valueOf(permission, ['viewYn'], 'N')) ? 'Y' : 'N';
                permission._originalDownloadOriginalYn =
                    isYes(valueOf(permission, ['downloadOriginalYn'], 'N')) ? 'Y' : 'N';
                permission._originalPrintYn = isYes(valueOf(permission, ['printYn'], 'N')) ? 'Y' : 'N';
            });
            renderFilePermissions();
        }).fail(function (xhr) {
            if (state.filePermissionRequestKey !== requestKey) return;
            $('#filePermissionTableBody').html(
                '<tr class="sa-empty-row sa-empty-row--error"><td colspan="9">사용자 권한을 불러오지 못했습니다.</td></tr>'
            );
            $('#filePermissionChangeReason, #filePermissionSaveButton').prop('disabled', true);
            showMessage('error', errorMessage(xhr));
        });
    }

    function renderFiles() {
        var rows = [];
        $.each(state.files, function (index, file) {
            var selected = state.selectedFile && fileKey(state.selectedFile) === fileKey(file) ? ' is-selected' : '';
            var gradeCd = valueOf(file, ['gradeCd', 'securityGradeCd'], '');
            var objectType = valueOf(file, ['objectType', 'type'], '');
            rows.push('<tr class="sa-selectable-row' + selected + '" tabindex="0" data-file-index="' + index + '">' +
                '<td>' + escapeHtml(fileObjectTypeLabel(objectType)) + '</td>' +
                '<td><code>' + escapeHtml(valueOf(file, ['objectNo', 'documentNo', 'drawingNo', 'swNo'], fileId(file))) + '</code></td>' +
                '<td>' + escapeHtml(valueOf(file, ['orgFileNm', 'fileNm'], '-')) + '</td>' +
                '<td>' + escapeHtml(valueOf(file, ['fileNo'], '*')) + '</td>' +
                '<td><span class="sa-badge sa-badge--grade">' + escapeHtml(valueOf(file, ['gradeNm', 'securityGradeNm'], gradeNameByCode(gradeCd))) + '</span></td>' +
                '</tr>');
        });
        var selectedTypeLabel = $('#fileObjectType option:selected').text() || '선택한 자료유형';
        $('#fileTableBody').html(rows.length ? rows.join('') :
            '<tr class="sa-empty-row"><td colspan="5">' +
            escapeHtml(selectedTypeLabel) + '에서 조회된 파일이 없습니다.</td></tr>');
    }

    function selectFile(file, options) {
        options = options || {};
        state.selectedFile = file;
        state.selectedFileNo = options.fileNo || valueOf(file, ['fileNo'], '*') || '*';
        renderFiles();
        $('#selectedFileSummary').removeClass('is-empty').html(
            '<strong>' + escapeHtml(valueOf(file, ['objectNo', 'documentNo', 'drawingNo', 'swNo'], fileId(file))) + '</strong>' +
            '<span>' + escapeHtml(fileObjectTypeLabel(fileObjectType(file))) + ' · ' +
            escapeHtml(valueOf(file, ['objectNm', 'documentNm', 'drawingNm', 'swNm', 'fileNm', 'orgFileNm'], '자료명 없음')) + '</span>'
        );
        $('#fileGradeCd').prop('disabled', false).val(valueOf(file, ['gradeCd', 'securityGradeCd'], ''));
        $('#fileNo').prop('disabled', false).val(state.selectedFileNo);
        $('#labelReason').prop('disabled', false).val(valueOf(file, ['labelReason'], ''));
        $('#fileLabelSaveButton').prop('disabled', false);
        loadFilePermissions();
    }

    function disableFileLabelForm() {
        state.selectedFile = null;
        state.selectedFileNo = null;
        $('#selectedFileSummary').addClass('is-empty').text('왼쪽 목록에서 파일을 선택하세요.');
        $('#fileGradeCd, #fileNo, #labelReason, #fileLabelSaveButton').prop('disabled', true);
        $('#fileLabelForm')[0].reset();
        $('#fileNo').val('*');
        disableFilePermissionForm();
    }

    function loadFiles(options) {
        options = options || {};
        setBusy($('#fileSearchButton'), true, '조회 중...');
        if (!options.selectedKey) {
            disableFileLabelForm();
        }
        return apiRequest('GET', '/files', {
            objectType: $('#fileObjectType').val(),
            keyword: $.trim($('#fileKeyword').val())
        }).done(function (response) {
            state.files = normalizeList(response);
            var selectedFile = null;
            if (options.selectedKey) {
                $.each(state.files, function (_, file) {
                    if (fileKey(file) === options.selectedKey) {
                        selectedFile = file;
                        return false;
                    }
                });
            }
            if (selectedFile) {
                selectFile(selectedFile, { fileNo: options.fileNo });
            } else {
                disableFileLabelForm();
                renderFiles();
            }
        }).fail(function (xhr) {
            disableFileLabelForm();
            $('#fileTableBody').html('<tr class="sa-empty-row sa-empty-row--error"><td colspan="5">파일을 불러오지 못했습니다.</td></tr>');
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#fileSearchButton'), false);
        });
    }

    function saveFileLabel() {
        if (!state.selectedFile) {
            showMessage('error', '등급을 지정할 파일을 선택해 주세요.');
            return;
        }
        var gradeCd = $('#fileGradeCd').val();
        var reason = $.trim($('#labelReason').val());
        if (!gradeCd) {
            showMessage('error', '적용할 보안등급을 선택해 주세요.');
            return;
        }
        if (!reason) {
            showMessage('error', '파일등급 지정 또는 변경 사유를 입력해 주세요.');
            return;
        }
        var payload = {
            objectId: fileId(state.selectedFile),
            objectType: valueOf(state.selectedFile, ['objectType', 'type'], $('#fileObjectType').val()),
            fileNo: $.trim($('#fileNo').val()) || '*',
            gradeCd: gradeCd,
            labelReason: reason
        };
        var selectedKey = fileKey(state.selectedFile);
        setBusy($('#fileLabelSaveButton'), true, '저장 중...');
        apiRequest('POST', '/files/label', payload).done(function () {
            showMessage('success', '파일 보안등급을 저장했습니다.');
            state.selectedFileNo = payload.fileNo;
            loadFiles({ selectedKey: selectedKey, fileNo: payload.fileNo });
        }).fail(function (xhr) {
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#fileLabelSaveButton'), false);
            if (!state.selectedFile) $('#fileLabelSaveButton').prop('disabled', true);
        });
    }

    function checkboxPermissionValue(index, action) {
        return $('#filePermissionTableBody .sa-file-permission-checkbox' +
            '[data-permission-index="' + index + '"]' +
            '[data-permission-action="' + action + '"]').is(':checked') ? 'Y' : 'N';
    }

    function saveFilePermissions() {
        var resource = selectedFileResource();
        if (!resource) {
            showMessage('error', '권한을 지정할 파일을 선택해 주세요.');
            return;
        }
        var permissions = [];
        $.each(state.filePermissions, function (index, permission) {
            var current = {
                userCd: valueOf(permission, ['userCd'], ''),
                viewYn: checkboxPermissionValue(index, 'VIEW'),
                downloadOriginalYn: checkboxPermissionValue(index, 'DOWNLOAD_ORIGINAL'),
                printYn: checkboxPermissionValue(index, 'PRINT')
            };
            if (current.viewYn !== valueOf(permission, ['_originalViewYn'], 'N') ||
                current.downloadOriginalYn !== valueOf(permission, ['_originalDownloadOriginalYn'], 'N') ||
                current.printYn !== valueOf(permission, ['_originalPrintYn'], 'N')) {
                permissions.push(current);
            }
        });
        if (!permissions.length) {
            showMessage('error', '변경된 사용자 권한이 없습니다.');
            return;
        }
        var reason = $.trim($('#filePermissionChangeReason').val());
        if (!reason) {
            showMessage('error', '사용자 권한 변경 사유를 입력해 주세요.');
            $('#filePermissionChangeReason').focus();
            return;
        }
        var payload = {
            objectType: resource.objectType,
            objectId: resource.objectId,
            fileNo: resource.fileNo,
            changeReason: reason,
            permissions: permissions
        };
        setBusy($('#filePermissionSaveButton'), true, '저장 중...');
        $('#filePermissionTableBody .sa-file-permission-checkbox').prop('disabled', true);
        apiRequest('POST', '/files/permissions', payload).done(function () {
            showMessage('success', '문서별 사용자 권한을 저장했습니다.');
            $('#filePermissionChangeReason').val('');
            loadFilePermissions();
        }).fail(function (xhr) {
            renderFilePermissions();
            showMessage('error', errorMessage(xhr));
        }).always(function () {
            setBusy($('#filePermissionSaveButton'), false);
            $('#filePermissionSaveButton').prop('disabled',
                !state.selectedFile || !state.filePermissions.length);
        });
    }

    function activateTab(targetId) {
        $('.sa-tab').each(function () {
            var active = $(this).data('tab-target') === targetId;
            $(this).toggleClass('is-active', active).attr('aria-selected', active ? 'true' : 'false').attr('tabindex', active ? '0' : '-1');
        });
        $('.sa-tab-panel').each(function () {
            var active = this.id === targetId;
            $(this).toggleClass('is-active', active).prop('hidden', !active);
        });
    }

    function bindEvents() {
        $('#securityAccessTabs').on('click', '.sa-tab', function () { activateTab($(this).data('tab-target')); });
        $('#securityAccessTabs').on('keydown', '.sa-tab', function (event) {
            if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
            event.preventDefault();
            var $tabs = $('.sa-tab');
            var index = $tabs.index(this) + (event.key === 'ArrowRight' ? 1 : -1);
            if (index < 0) index = $tabs.length - 1;
            if (index >= $tabs.length) index = 0;
            $tabs.eq(index).focus().trigger('click');
        });
        $('#gradeForm').on('submit', function (event) { event.preventDefault(); saveGrade(); });
        $('#gradeNewButton').on('click', resetGradeForm);
        $('#gradeReloadButton').on('click', loadGrades);
        $('#gradeTableBody').on('click keydown', '.sa-selectable-row', function (event) {
            if (event.type === 'keydown' && event.key !== 'Enter' && event.key !== ' ') return;
            event.preventDefault();
            selectGrade(state.grades[Number($(this).data('grade-index'))]);
        });
        $('#userSearchForm').on('submit', function (event) { event.preventDefault(); loadUsers(); });
        $('#userTableBody').on('click keydown', '.sa-selectable-row', function (event) {
            if (event.type === 'keydown' && event.key !== 'Enter' && event.key !== ' ') return;
            event.preventDefault();
            selectUser(state.users[Number($(this).data('user-index'))]);
        });
        $('#clearanceForm').on('submit', function (event) { event.preventDefault(); saveClearance(); });
        $('#fileSearchForm').on('submit', function (event) { event.preventDefault(); loadFiles(); });
        $('#fileObjectType').on('change', function () { loadFiles(); });
        $('#fileTableBody').on('click keydown', '.sa-selectable-row', function (event) {
            if (event.type === 'keydown' && event.key !== 'Enter' && event.key !== ' ') return;
            event.preventDefault();
            selectFile(state.files[Number($(this).data('file-index'))]);
        });
        $('#fileNo').on('input', function () {
            if (!state.selectedFile) return;
            var editedFileNo = $.trim($(this).val()) || '*';
            if (editedFileNo === state.selectedFileNo) {
                loadFilePermissions();
            } else {
                disableFilePermissionForm('변경한 파일번호의 등급을 먼저 저장한 뒤 사용자 권한을 설정하세요.');
            }
        });
        $('#fileLabelForm').on('submit', function (event) { event.preventDefault(); saveFileLabel(); });
        $('#filePermissionForm').on('submit', function (event) { event.preventDefault(); saveFilePermissions(); });
        $('#filePermissionTableBody').on('change', '.sa-file-permission-checkbox', function () {
            var index = $(this).data('permission-index');
            var action = $(this).data('permission-action');
            var selector = '.sa-file-permission-checkbox[data-permission-index="' + index + '"]';
            if ((action === 'DOWNLOAD_ORIGINAL' || action === 'PRINT') && $(this).is(':checked')) {
                $(selector + '[data-permission-action="VIEW"]').prop('checked', true);
            }
            if (action === 'VIEW' && !$(this).is(':checked')) {
                $(selector + '[data-permission-action="DOWNLOAD_ORIGINAL"], ' +
                    selector + '[data-permission-action="PRINT"]').prop('checked', false);
            }
            state.filePermissions[index].viewYn =
                $(selector + '[data-permission-action="VIEW"]').is(':checked') ? 'Y' : 'N';
            state.filePermissions[index].downloadOriginalYn =
                $(selector + '[data-permission-action="DOWNLOAD_ORIGINAL"]').is(':checked') ? 'Y' : 'N';
            state.filePermissions[index].printYn =
                $(selector + '[data-permission-action="PRINT"]').is(':checked') ? 'Y' : 'N';
        });
        $('#filePermissionTableBody').on('click', '.sa-file-permission-revoke', function () {
            var index = Number($(this).data('permission-index'));
            var permission = state.filePermissions[index];
            if (!permission) return;
            permission.viewYn = 'N';
            permission.downloadOriginalYn = 'N';
            permission.printYn = 'N';
            renderFilePermissions();
        });
    }

    $(function () {
        $('.layout-wrapper.bodyWrap .content-wrapper > .container').addClass('distribution-invoice-container');
        bindEvents();
        disableClearanceForm();
        disableFileLabelForm();
        loadGrades().always(function () {
            loadUsers();
            loadFiles();
        });
    });
})(window.jQuery, window, document);
