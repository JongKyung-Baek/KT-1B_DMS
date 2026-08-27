var emptyArray = [];
var files = [];
var swTechnicalFilePolicy = null;
var swTechnicalFileTypeLabels = {};

function configureSwTechnicalFileTypePolicy(config, labels) {
    if (!window.SwTechnicalFileTypePolicy) {
        throw new Error("SwTechnicalFileTypePolicy is not loaded.");
    }
    swTechnicalFilePolicy = window.SwTechnicalFileTypePolicy.create(config || {});
    swTechnicalFileTypeLabels = labels || {};
    return swTechnicalFilePolicy;
}

function getSwTechnicalFileTypeInfo(fileOrName) {
    if (!swTechnicalFilePolicy) {
        throw new Error("SwTechnicalFileTypePolicy is not configured.");
    }
    return swTechnicalFilePolicy.classify(fileOrName);
}

function swTechnicalFileTypeLabel(info) {
    var labels = swTechnicalFileTypeLabels || {};
    var extension = String(info && info.extension || "").toUpperCase();
    var label = labels[info && info.status] || labels.UNSUPPORTED_VIEWER || "";
    return String(label).replace(/\{0\}/g, extension || "-");
}

function swTechnicalFileTypeClass(info) {
    if (!info || info.status === "INVALID_FILE_NAME") return "invalid";
    if (info.status === "UNSUPPORTED_VIEWER") return "unsupported";
    return "supported";
}

function renderSwMainFileTypeStatus(target, file) {
    var $target = target && target.jquery ? target : $(target);
    if (!$target.length) return;
    if (!file) {
        $target.removeClass("is-visible file-type-status--supported file-type-status--unsupported file-type-status--invalid")
            .text("");
        return;
    }
    var info = getSwTechnicalFileTypeInfo(file);
    var typeClass = swTechnicalFileTypeClass(info);
    $target.removeClass("file-type-status--supported file-type-status--unsupported file-type-status--invalid")
        .addClass("is-visible file-type-status--" + typeClass)
        .text(swTechnicalFileTypeLabel(info));
}

function appendSwTechnicalFileTypeBadge($item, file) {
    var info = getSwTechnicalFileTypeInfo(file);
    var typeClass = swTechnicalFileTypeClass(info);
    $("<span>", {
        "class": "supporting-file-chip__status supporting-file-chip__status--" + typeClass,
        "text": swTechnicalFileTypeLabel(info)
    }).appendTo($item);
    return info;
}

function findInvalidSwTechnicalFile(filesToValidate) {
    var selected = window.SwSupportingFileSelection
        ? window.SwSupportingFileSelection.toArray(filesToValidate)
        : Array.prototype.slice.call(filesToValidate || []);
    for (var index = 0; index < selected.length; index += 1) {
        if (!getSwTechnicalFileTypeInfo(selected[index]).registrationAllowed) {
            return selected[index];
        }
    }
    return null;
}

function swSupportingFileSelectionApi() {
    if (!window.SwSupportingFileSelection) {
        throw new Error("SwSupportingFileSelection is not loaded.");
    }
    return window.SwSupportingFileSelection;
}

function getSwAccumulatedSubFiles(input) {
    if (!input || !Array.isArray(input._tdmsAccumulatedSubFiles)) {
        return [];
    }
    return swSupportingFileSelectionApi().toArray(input._tdmsAccumulatedSubFiles);
}

function appendSwAccumulatedSubFiles(input, incomingFiles) {
    if (!input) {
        return [];
    }
    return setSwAccumulatedSubFiles(input, swSupportingFileSelectionApi().merge(
        getSwAccumulatedSubFiles(input),
        incomingFiles
    ));
}

function setSwAccumulatedSubFiles(input, selectedFiles) {
    if (!input) {
        return [];
    }

    input._tdmsAccumulatedSubFiles = swSupportingFileSelectionApi().toArray(selectedFiles);

    // Keep the native FileList in sync where the browser permits it. Submission
    // still reads the accumulated list directly, so browsers that reject a
    // programmatic FileList assignment retain the same behavior.
    try {
        if (typeof DataTransfer === "function") {
            var dataTransfer = new DataTransfer();
            input._tdmsAccumulatedSubFiles.forEach(function (file) {
                dataTransfer.items.add(file);
            });
            input.files = dataTransfer.files;
        }
    } catch (ignored) {
        // The accumulated list remains authoritative.
    }

    return getSwAccumulatedSubFiles(input);
}

function removeSwAccumulatedSubFile(input, index) {
    return setSwAccumulatedSubFiles(input, swSupportingFileSelectionApi().removeAt(
        getSwAccumulatedSubFiles(input),
        index
    ));
}

function appendSwAccumulatedSubFilesToFormData(formData, input, fieldName) {
    return swSupportingFileSelectionApi().appendToFormData(
        formData,
        fieldName || "subFiles",
        getSwAccumulatedSubFiles(input)
    );
}

function clearSwAccumulatedSubFiles(input) {
    if (!input) {
        return;
    }
    setSwAccumulatedSubFiles(input, []);
    input.value = "";
}

function fileUpload(){
    $('#swRegisFile').click();
}

function fileChange(){
    var fileName = $('#swRegisFile').val();
    if(fileName.indexOf("\\") != -1){
        $('#fileName').val(fileName.substring(fileName.lastIndexOf('\\')+1, fileName.length));
    }
}



/**
 * 배포요청 시 validation체크
 * 용도, 업체명/담당자, Email, 사업장, 구매담당자가 선택되었는지 체크한다
 * @returns
 */
function isValidation(){


    if($.trim($("#dataName").val()) === ""){
        isValidDataEmpty("dataName", "form.dataName");
        return false;
    }

    if($.trim($("#swRegisFile").val()) === ""){
        alertMessage(g_msg('msg.noFile'));	 //첨부된 파일이 없습니다.
        return false;
    }

    return true;
}


function save(){
    if(!isValidation()){
        return;
    }
    var param = new FormData();
    param.append('file', $("#swRegisFile")[0].files[0]);
    param.append('formSwRegisterPopup', JSON.stringify($('#formSwRegisterPopup').serializeObject()));
    callAjaxUpload(param, "/general/distribution/saveSwRegisterFile", requestCrCallback);
}

/**
 * 배포요청 후 결과 메시지 출력
 * @param response
 * @returns
 */
function requestCrCallback(response){
    if(response.success){
        infoMessage(g_msg('msg.registerComplete'), function(){			//등록이 완료되었습니다.
            searchList(gridParam);
            closePopup('popupDialog');
            $(this).dialog("close");
        });
    }else{
        alertMessage(g_msg("msg.registerFailure"));						//등록이 실패했습니다.
    }
}

function createSwRegistrationUploadLifecycle(nowProvider) {
    var now = typeof nowProvider === "function" ? nowProvider : function () { return Date.now(); };
    var state = inactiveState("idle");

    function inactiveState(outcome) {
        return {
            active: false,
            phase: "idle",
            outcome: outcome || "idle",
            startedAt: 0,
            elapsedMs: 0,
            loaded: 0,
            total: 0,
            lengthComputable: false,
            percent: null,
            cancelAllowed: false
        };
    }

    function finiteNumber(value, fallback) {
        var number = Number(value);
        return isFinite(number) ? number : fallback;
    }

    function snapshot() {
        return {
            active: state.active,
            phase: state.phase,
            outcome: state.outcome,
            startedAt: state.startedAt,
            elapsedMs: state.elapsedMs,
            loaded: state.loaded,
            total: state.total,
            lengthComputable: state.lengthComputable,
            percent: state.percent,
            cancelAllowed: state.cancelAllowed
        };
    }

    function updateElapsed(elapsedMs) {
        if (!state.active) return snapshot();
        state.elapsedMs = Math.max(0, finiteNumber(elapsedMs, now() - state.startedAt));
        return snapshot();
    }

    function finish(outcome) {
        if (!state.active) return snapshot();
        updateElapsed();
        state.active = false;
        state.cancelAllowed = false;
        state.outcome = outcome || state.outcome || "complete";
        return snapshot();
    }

    return {
        begin: function () {
            if (state.active) return false;
            state = {
                active: true,
                phase: "uploading",
                outcome: "pending",
                startedAt: now(),
                elapsedMs: 0,
                loaded: 0,
                total: 0,
                lengthComputable: false,
                percent: null,
                cancelAllowed: true
            };
            return true;
        },
        progress: function (progress) {
            if (!state.active || state.phase !== "uploading") return snapshot();
            progress = progress || {};
            updateElapsed(progress.elapsedMs);
            state.loaded = Math.max(0, finiteNumber(progress.loaded, state.loaded));
            state.lengthComputable = progress.lengthComputable === true
                && finiteNumber(progress.total, 0) > 0;

            if (state.lengthComputable) {
                state.total = Math.max(0, finiteNumber(progress.total, 0));
                var calculatedPercent = state.total > 0 ? (state.loaded / state.total) * 100 : 0;
                state.percent = Math.max(0, Math.min(100,
                    finiteNumber(progress.percent, calculatedPercent)));
                if (state.loaded >= state.total || state.percent >= 100) {
                    state.phase = "processing";
                    state.percent = 100;
                    state.cancelAllowed = false;
                } else {
                    state.cancelAllowed = true;
                }
            } else {
                state.total = 0;
                state.percent = null;
                state.cancelAllowed = true;
            }
            return snapshot();
        },
        uploadComplete: function (upload) {
            if (!state.active) return snapshot();
            upload = upload || {};
            updateElapsed(upload.elapsedMs);
            state.loaded = Math.max(state.loaded, finiteNumber(upload.loaded, state.loaded));
            if (finiteNumber(upload.total, 0) > 0) {
                state.total = finiteNumber(upload.total, state.total);
                state.lengthComputable = true;
                state.percent = 100;
            }
            state.phase = "processing";
            state.cancelAllowed = false;
            return snapshot();
        },
        requestCancel: function () {
            if (!state.active || !state.cancelAllowed || state.phase !== "uploading") return false;
            state.phase = "cancelling";
            state.cancelAllowed = false;
            return true;
        },
        tick: updateElapsed,
        finish: finish,
        snapshot: snapshot,
        isActive: function () { return state.active; },
        canCancel: function () { return state.active && state.cancelAllowed; }
    };
}

function formatSwRegistrationUploadMegabytes(bytes) {
    var value = Number(bytes);
    if (!isFinite(value) || value < 0) value = 0;
    return (value / (1024 * 1024)).toFixed(2);
}

function formatSwRegistrationUploadElapsed(elapsedMs) {
    var value = Number(elapsedMs);
    if (!isFinite(value) || value < 0) value = 0;
    var totalSeconds = Math.floor(value / 1000);
    var hours = Math.floor(totalSeconds / 3600);
    var minutes = Math.floor((totalSeconds % 3600) / 60);
    var seconds = totalSeconds % 60;
    var twoDigits = function (number) { return number < 10 ? "0" + number : String(number); };
    return hours > 0
        ? twoDigits(hours) + ":" + twoDigits(minutes) + ":" + twoDigits(seconds)
        : twoDigits(minutes) + ":" + twoDigits(seconds);
}

function swRegistrationUploadTemplate(template) {
    var values = Array.prototype.slice.call(arguments, 1);
    return values.reduce(function (text, value, index) {
        return String(text).replace(new RegExp("\\{" + index + "\\}", "g"), value);
    }, template || "");
}

var swRegistrationUploadMessages = {};
var swRegistrationUploadRuntime = createSwRegistrationUploadLifecycle();
var swRegistrationUploadHandle = null;
var swRegistrationUploadTimer = null;
var swRegistrationLockedButtons = [];

function configureSwRegistrationUploadProgressMessages(messages) {
    swRegistrationUploadMessages = messages || {};
}

function swRegistrationUploadMessage(key, fallback) {
    var message = swRegistrationUploadMessages[key];
    return message === undefined || message === null || message === "" ? fallback : message;
}

function getSwRegistrationUploadRoot() {
    if (typeof getSwPopupRoot === "function") {
        var $configuredRoot = getSwPopupRoot();
        if ($configuredRoot && $configuredRoot.length) return $configuredRoot;
    }
    return $(".swRegisterPopup").last();
}

function getSwRegistrationUploadPanel() {
    return getSwRegistrationUploadRoot().find("[data-sw-upload-progress]").first();
}

function lockSwRegistrationUploadButtons() {
    var $root = getSwRegistrationUploadRoot();
    var $buttons = $root.find("button").not("[data-sw-upload-cancel]");
    var $dialog = $root.closest(".ui-dialog");
    if ($dialog.length) {
        $buttons = $buttons.add($dialog.find(".ui-dialog-titlebar-close"));
    }
    swRegistrationLockedButtons = [];
    $buttons.each(function () {
        swRegistrationLockedButtons.push({
            element: this,
            disabled: !!this.disabled,
            ariaDisabled: $(this).attr("aria-disabled")
        });
        $(this).prop("disabled", true).attr("aria-disabled", "true").addClass("is-upload-locked");
    });
    $root.addClass("is-uploading");

    var $dialogContent = $root.closest(".ui-dialog-content");
    if ($dialogContent.length) {
        $dialogContent.off("dialogbeforeclose.swRegistrationUpload")
            .on("dialogbeforeclose.swRegistrationUpload", function (event) {
                if (swRegistrationUploadRuntime.isActive()) event.preventDefault();
            });
    }
    $(window).off("beforeunload.swRegistrationUpload")
        .on("beforeunload.swRegistrationUpload", function (event) {
            if (!swRegistrationUploadRuntime.isActive()) return undefined;
            var warning = swRegistrationUploadMessage(
                "leaveWarning",
                "파일 업로드가 진행 중입니다. 이 페이지를 나가면 업로드가 중단될 수 있습니다."
            );
            event.returnValue = warning;
            return warning;
        });
}

function unlockSwRegistrationUploadButtons() {
    swRegistrationLockedButtons.forEach(function (locked) {
        var $button = $(locked.element);
        $button.prop("disabled", locked.disabled).removeClass("is-upload-locked");
        if (locked.ariaDisabled === undefined) {
            $button.removeAttr("aria-disabled");
        } else {
            $button.attr("aria-disabled", locked.ariaDisabled);
        }
    });
    swRegistrationLockedButtons = [];
    var $root = getSwRegistrationUploadRoot();
    $root.removeClass("is-uploading");
    $root.closest(".ui-dialog-content").off("dialogbeforeclose.swRegistrationUpload");
    $(window).off("beforeunload.swRegistrationUpload");
}

function renderSwRegistrationUploadProgress(snapshot) {
    var $panel = getSwRegistrationUploadPanel();
    if (!$panel.length || !snapshot || !snapshot.active) return;
    var $status = $panel.find("[data-sw-upload-status]");
    var $bar = $panel.find("[data-sw-upload-bar]");
    var $barFill = $panel.find("[data-sw-upload-bar-fill]");
    var $percent = $panel.find("[data-sw-upload-percent]");
    var $metrics = $panel.find("[data-sw-upload-metrics]");
    var $elapsed = $panel.find("[data-sw-upload-elapsed]");
    var $notice = $panel.find("[data-sw-upload-notice]");
    var $cancel = $panel.find("[data-sw-upload-cancel]");
    var statusText = swRegistrationUploadMessage("uploading", "파일을 전송하고 있습니다.");
    var noticeText = swRegistrationUploadMessage(
        "slowNotice",
        "느린 회선에서는 업로드에 여러 분이 걸릴 수 있습니다. 이 창을 닫거나 새로고침하지 마세요."
    );

    $panel.prop("hidden", false).attr("aria-busy", "true");
    $elapsed.text(swRegistrationUploadTemplate(
        swRegistrationUploadMessage("elapsed", "경과 {0}"),
        formatSwRegistrationUploadElapsed(snapshot.elapsedMs)
    ));

    if (snapshot.phase === "processing") {
        statusText = swRegistrationUploadMessage(
            "processing",
            "파일 전송 완료 · 서버에서 등록 처리 중입니다."
        );
        noticeText = swRegistrationUploadMessage(
            "processingNotice",
            "파일 전송은 끝났습니다. 등록 결과가 표시될 때까지 기다려 주세요."
        );
    } else if (snapshot.phase === "cancelling") {
        statusText = swRegistrationUploadMessage("cancelling", "업로드를 취소하고 있습니다.");
    }
    $status.text(statusText);
    $notice.text(noticeText);

    if (snapshot.lengthComputable && snapshot.total > 0) {
        var visiblePercent = snapshot.phase === "processing"
            ? 100
            : Math.min(99, Math.max(0, Math.floor(snapshot.percent || 0)));
        $bar.removeClass("is-indeterminate").attr("aria-valuenow", visiblePercent);
        $barFill.css("width", visiblePercent + "%");
        $percent.text(swRegistrationUploadTemplate(
            swRegistrationUploadMessage("percent", "{0}%"),
            visiblePercent
        ));
        $metrics.text(swRegistrationUploadTemplate(
            swRegistrationUploadMessage("determinate", "{0} / {1} MB"),
            formatSwRegistrationUploadMegabytes(snapshot.loaded),
            formatSwRegistrationUploadMegabytes(snapshot.total)
        ));
    } else {
        $bar.addClass("is-indeterminate").removeAttr("aria-valuenow");
        $barFill.css("width", "38%");
        $percent.text(swRegistrationUploadMessage("inProgress", "진행 중"));
        $metrics.text(snapshot.loaded > 0
            ? swRegistrationUploadTemplate(
                swRegistrationUploadMessage("indeterminate", "{0} MB 전송됨 · 전체 크기 확인 중"),
                formatSwRegistrationUploadMegabytes(snapshot.loaded)
            )
            : swRegistrationUploadMessage("measuring", "전송량을 확인하고 있습니다."));
    }

    var hasAbortHandle = swRegistrationUploadHandle
        && typeof swRegistrationUploadHandle.abort === "function";
    var canCancel = snapshot.cancelAllowed && hasAbortHandle;
    $cancel.prop("hidden", !canCancel).prop("disabled", !canCancel)
        .attr("aria-disabled", canCancel ? "false" : "true");
}

function beginSwRegistrationUpload() {
    if (!swRegistrationUploadRuntime.begin()) return false;
    swRegistrationUploadHandle = null;
    lockSwRegistrationUploadButtons();
    renderSwRegistrationUploadProgress(swRegistrationUploadRuntime.snapshot());
    if (swRegistrationUploadTimer) clearInterval(swRegistrationUploadTimer);
    swRegistrationUploadTimer = setInterval(function () {
        renderSwRegistrationUploadProgress(swRegistrationUploadRuntime.tick());
    }, 1000);
    return true;
}

function isSwRegistrationUploadActive() {
    return swRegistrationUploadRuntime.isActive();
}

function attachSwRegistrationUploadHandle(handle) {
    if (!swRegistrationUploadRuntime.isActive() || !handle) return;
    if (typeof handle.abort === "function") swRegistrationUploadHandle = handle;
    renderSwRegistrationUploadProgress(swRegistrationUploadRuntime.snapshot());
}

function updateSwRegistrationUploadProgress(progress) {
    renderSwRegistrationUploadProgress(swRegistrationUploadRuntime.progress(progress));
}

function markSwRegistrationUploadComplete(upload) {
    renderSwRegistrationUploadProgress(swRegistrationUploadRuntime.uploadComplete(upload));
}

function finishSwRegistrationUploadUi(outcome) {
    swRegistrationUploadRuntime.finish(outcome);
    if (swRegistrationUploadTimer) {
        clearInterval(swRegistrationUploadTimer);
        swRegistrationUploadTimer = null;
    }
    swRegistrationUploadHandle = null;
    unlockSwRegistrationUploadButtons();
    var $panel = getSwRegistrationUploadPanel();
    $panel.prop("hidden", true).attr("aria-busy", "false");
    $panel.find("[data-sw-upload-cancel]").prop("hidden", true).prop("disabled", true)
        .attr("aria-disabled", "true");
}

function swRegistrationUploadErrorMessage(errorInfo) {
    errorInfo = errorInfo || {};
    var type = errorInfo.type || "http";
    var status = Number(errorInfo.status || (errorInfo.xhr && errorInfo.xhr.status) || 0);
    if (status === 401) type = "unauthorized";
    if (status === 403) type = "forbidden";
    if (status === 413) type = "payload-too-large";
    if (status >= 500) type = "server";
    var messages = {
        unauthorized: swRegistrationUploadMessage(
            "unauthorized",
            "로그인 세션이 만료되었습니다. 다시 로그인한 뒤 등록해 주세요."
        ),
        forbidden: swRegistrationUploadMessage(
            "forbidden",
            "기술자료를 등록할 권한이 없습니다. 권한을 확인해 주세요."
        ),
        "payload-too-large": swRegistrationUploadMessage(
            "payloadTooLarge",
            "첨부파일이 서버의 업로드 허용 크기를 초과했습니다. 파일 크기를 줄이거나 관리자에게 문의해 주세요."
        ),
        network: swRegistrationUploadMessage(
            "network",
            "네트워크 연결이 끊겨 업로드하지 못했습니다. 연결을 확인한 뒤 다시 시도해 주세요."
        ),
        server: swRegistrationUploadMessage(
            "server",
            "서버에서 등록을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요."
        ),
        timeout: swRegistrationUploadMessage(
            "timeout",
            "네트워크 또는 중간 장비에서 응답 시간이 초과되었습니다. 연결을 확인한 뒤 다시 시도해 주세요."
        ),
        http: swRegistrationUploadMessage(
            "http",
            "업로드 요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요."
        )
    };
    return messages[type] || messages.http;
}

function handleSwRegistrationUploadError(errorInfo) {
    errorInfo = errorInfo || {};
    var message = swRegistrationUploadErrorMessage(errorInfo);
    var snapshot = swRegistrationUploadRuntime.snapshot();
    var status = Number(errorInfo.status || (errorInfo.xhr && errorInfo.xhr.status) || 0);
    var uncertainAfterTransfer = snapshot.phase === "processing"
        && (errorInfo.type === "network" || errorInfo.type === "server" || status === 0 || status >= 500);
    if (uncertainAfterTransfer) {
        message += " " + swRegistrationUploadMessage(
            "verifyRegistration",
            "목록에서 등록 여부를 확인한 뒤 다시 시도해 주세요."
        );
    }
    var unauthorized = errorInfo.type === "unauthorized" || status === 401;
    finishSwRegistrationUploadUi("error");
    if (unauthorized) {
        alertMessage(message, function () {
            try {
                window.top.location.href = "/login/loginPage";
            } catch (ignored) {
                window.location.href = "/login/loginPage";
            }
            try { $(this).dialog("close"); } catch (ignoredDialog) { }
        });
        return;
    }
    alertMessage(message);
}

function handleSwRegistrationUploadAbort() {
    finishSwRegistrationUploadUi("abort");
    alertMessage(swRegistrationUploadMessage("cancelled", "업로드가 취소되었습니다."));
}

function cancelSwRegistrationUpload() {
    if (!swRegistrationUploadRuntime.canCancel()) return false;
    var handle = swRegistrationUploadHandle;
    if (!handle || typeof handle.abort !== "function") return false;
    if (typeof window.confirm === "function" && !window.confirm(swRegistrationUploadMessage(
        "cancelConfirm",
        "파일 전송을 취소할까요?"
    ))) return false;
    if (!swRegistrationUploadRuntime.requestCancel()) return false;
    renderSwRegistrationUploadProgress(swRegistrationUploadRuntime.snapshot());
    try {
        handle.abort();
    } catch (error) {
        handleSwRegistrationUploadError({ type: "network", errorThrown: error });
    }
    return true;
}

function createSwRegistrationUploadAjaxOptions() {
    return {
        global: false,
        handleDefaultError: false,
        onStart: function (xhr) {
            attachSwRegistrationUploadHandle(xhr);
        },
        onProgress: function (progress) {
            updateSwRegistrationUploadProgress(progress);
        },
        onUploadComplete: function (upload) {
            markSwRegistrationUploadComplete(upload);
        },
        onSuccess: function () {
            markSwRegistrationUploadComplete();
        },
        onError: function (errorInfo) {
            handleSwRegistrationUploadError(errorInfo);
        },
        onAbort: function () {
            handleSwRegistrationUploadAbort();
        },
        onComplete: function (completeInfo) {
            finishSwRegistrationUploadUi(completeInfo && completeInfo.state || "complete");
        }
    };
}
