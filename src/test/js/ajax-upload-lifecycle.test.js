const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const commonUtilPath = "src/main/resources/static/js/common_util.js";
const commonUtilSource = fs.readFileSync(commonUtilPath, "utf8");
const functionStart = commonUtilSource.indexOf("function callAjaxUpload(");
const functionEnd = commonUtilSource.indexOf("\nfunction guid()", functionStart);

assert.notEqual(functionStart, -1, "callAjaxUpload must exist");
assert.notEqual(functionEnd, -1, "callAjaxUpload must remain independently testable");
const callAjaxUploadSource = commonUtilSource.slice(functionStart, functionEnd);

function createHarness() {
    let now = 1000;
    let ajaxOptions;
    const uploadListeners = new Map();
    const alerts = [];
    const refreshedSessions = [];
    const nativeXhr = {
        upload: {
            addEventListener(type, listener) {
                const listeners = uploadListeners.get(type) || [];
                listeners.push(listener);
                uploadListeners.set(type, listeners);
            }
        }
    };
    const request = {
        status: 0,
        aborted: 0,
        abort() {
            this.aborted += 1;
            ajaxOptions.error(this, "abort", "abort");
            ajaxOptions.complete(this, "abort");
        }
    };
    const jquery = {
        ajaxSettings: {
            xhr() {
                return nativeXhr;
            }
        },
        ajax(options) {
            ajaxOptions = options;
            options.xhr();
            return request;
        }
    };
    const parent = { parent: { location: { href: "/unchanged" } } };
    const context = {
        $: jquery,
        Date: { now: () => now },
        Math,
        Number,
        parent,
        alertMessage(message) {
            alerts.push(message);
        },
        g_msg(key) {
            return `translated:${key}`;
        },
        refreshSessionTimer(xhr) {
            refreshedSessions.push(xhr);
        }
    };
    vm.createContext(context);
    vm.runInContext(callAjaxUploadSource, context, { filename: commonUtilPath });

    return {
        alerts,
        context,
        nativeXhr,
        parent,
        refreshedSessions,
        request,
        options: () => ajaxOptions,
        advance(milliseconds) {
            now += milliseconds;
        },
        upload(type, event) {
            for (const listener of uploadListeners.get(type) || []) {
                listener(event);
            }
        },
        succeed(response = { success: true }) {
            request.status = 200;
            ajaxOptions.success(response, "success", request);
            ajaxOptions.complete(request, "success");
        },
        fail(status, textStatus = "error", errorThrown = "failure") {
            request.status = status;
            ajaxOptions.error(request, textStatus, errorThrown);
            ajaxOptions.complete(request, textStatus);
        }
    };
}

// The original three-argument contract and default error handling stay intact.
{
    const harness = createHarness();
    const formData = { multipart: true };
    let callbackResponse;
    const returned = harness.context.callAjaxUpload(
        formData,
        "/legacy-upload",
        response => { callbackResponse = response; }
    );

    assert.strictEqual(returned, harness.request, "the returned jqXHR must be an abort handle");
    assert.equal(harness.options().url, "/legacy-upload");
    assert.equal(harness.options().type, "POST");
    assert.strictEqual(harness.options().data, formData);
    assert.equal(harness.options().processData, false);
    assert.equal(harness.options().contentType, false);
    assert.equal(harness.options().global, true, "legacy requests must keep global AJAX events");
    assert.equal("timeout" in harness.options(), false, "long uploads must not get a fixed timeout");

    const response = { success: true, id: 7 };
    harness.succeed(response);
    assert.strictEqual(callbackResponse, response);
    assert.equal(harness.refreshedSessions.length, 0,
        "global requests leave session refresh to common.js ajaxComplete");
}

{
    const unauthorized = createHarness();
    unauthorized.context.callAjaxUpload({}, "/upload", () => {});
    unauthorized.fail(401);
    assert.equal(unauthorized.parent.parent.location.href, "/login/loginPage");

    const forbidden = createHarness();
    forbidden.context.callAjaxUpload({}, "/upload", () => {});
    forbidden.fail(403);
    assert.deepEqual(forbidden.alerts, ["translated:msg.accessDenied"]);

    const generic = createHarness();
    generic.context.callAjaxUpload({}, "/upload", () => {});
    generic.fail(500);
    assert.equal(generic.alerts.length, 1);
    assert.match(generic.alerts[0], /^translated:msg\.error\[/);
}

// Opt-in progress includes computable and indeterminate transfers plus elapsed time.
{
    const harness = createHarness();
    let startedWith;
    const progress = [];
    const uploadComplete = [];
    harness.context.callAjaxUpload({}, "/upload", () => {}, {
        global: false,
        handleDefaultError: false,
        onStart(xhr) { startedWith = xhr; },
        onProgress(state) { progress.push(state); },
        onUploadComplete(state) { uploadComplete.push(state); }
    });

    assert.strictEqual(startedWith, harness.nativeXhr);
    assert.equal(harness.options().global, false,
        "an upload screen can suppress the global LOADING overlay");
    harness.advance(1500);
    harness.upload("progress", { loaded: 5 * 1024 * 1024, total: 20 * 1024 * 1024, lengthComputable: true });
    harness.upload("progress", { loaded: 6 * 1024 * 1024, total: 0, lengthComputable: false });

    assert.equal(progress.length, 2);
    assert.equal(progress[0].loaded, 5 * 1024 * 1024);
    assert.equal(progress[0].total, 20 * 1024 * 1024);
    assert.equal(progress[0].lengthComputable, true);
    assert.equal(progress[0].percent, 25);
    assert.equal(progress[0].elapsedMs, 1500);
    assert.equal(progress[1].total, null);
    assert.equal(progress[1].lengthComputable, false);
    assert.equal(progress[1].percent, null);

    harness.advance(500);
    const loadEvent = { loaded: 20 * 1024 * 1024, total: 20 * 1024 * 1024, lengthComputable: true };
    harness.upload("load", loadEvent);
    harness.upload("load", loadEvent);
    assert.equal(uploadComplete.length, 1,
        "the UI must enter the non-cancellable server-processing phase only once");
    assert.equal(uploadComplete[0].loaded, 20 * 1024 * 1024);
    assert.equal(uploadComplete[0].total, 20 * 1024 * 1024);
    assert.equal(uploadComplete[0].elapsedMs, 2000);

    harness.succeed();
    assert.deepEqual(harness.refreshedSessions, [harness.request],
        "global:false requests must refresh the visible session timer themselves exactly once");
}

// Success, HTTP error, and abort each complete exactly once with no cross-callbacks.
{
    const events = [];
    const harness = createHarness();
    harness.context.callAjaxUpload({}, "/upload", response => events.push(["legacy", response]), {
        handleDefaultError: false,
        onSuccess(response) { events.push(["success", response]); },
        onError(error) { events.push(["error", error.type]); },
        onAbort(error) { events.push(["abort", error.type]); },
        onComplete(result) { events.push(["complete", result.state]); }
    });
    const response = { id: 3 };
    harness.succeed(response);
    assert.deepEqual(events, [
        ["legacy", response],
        ["success", response],
        ["complete", "success"]
    ]);
}

{
    const events = [];
    const harness = createHarness();
    const returned = harness.context.callAjaxUpload({}, "/upload", () => {}, {
        handleDefaultError: false,
        onError(error) { events.push(["error", error.type]); },
        onAbort(error) { events.push(["abort", error.type]); },
        onComplete(result) { events.push(["complete", result.state]); }
    });
    returned.abort();
    assert.equal(returned.aborted, 1);
    assert.deepEqual(events, [
        ["abort", "abort"],
        ["complete", "abort"]
    ]);
}

const errorCases = [
    { status: 401, textStatus: "error", expected: "unauthorized" },
    { status: 403, textStatus: "error", expected: "forbidden" },
    { status: 413, textStatus: "error", expected: "payload-too-large" },
    { status: 0, textStatus: "error", expected: "network" },
    { status: 503, textStatus: "error", expected: "server" },
    { status: 418, textStatus: "error", expected: "http" },
    { status: 0, textStatus: "timeout", expected: "timeout" }
];
for (const testCase of errorCases) {
    const harness = createHarness();
    let receivedError;
    const completeStates = [];
    harness.context.callAjaxUpload({}, "/upload", () => {}, {
        handleDefaultError: false,
        onError(error) { receivedError = error; },
        onComplete(result) { completeStates.push(result.state); }
    });
    harness.fail(testCase.status, testCase.textStatus);
    assert.equal(receivedError.type, testCase.expected);
    assert.equal(receivedError.status, testCase.status);
    assert.deepEqual(completeStates, ["error"]);
}

// Cleanup still runs if a legacy success callback or an opt-in error callback throws.
{
    const harness = createHarness();
    const completed = [];
    harness.context.callAjaxUpload({}, "/upload", () => { throw new Error("callback failed"); }, {
        onComplete(result) { completed.push(result.state); }
    });
    assert.throws(() => harness.succeed(), /callback failed/);
    assert.deepEqual(completed, ["success"]);
}

{
    const harness = createHarness();
    const completed = [];
    harness.context.callAjaxUpload({}, "/upload", () => {}, {
        handleDefaultError: false,
        onError() { throw new Error("handler failed"); },
        onComplete(result) { completed.push(result.state); }
    });
    assert.throws(() => harness.fail(500), /handler failed/);
    assert.deepEqual(completed, ["error"]);
}

console.log("AJAX upload progress and lifecycle regression tests passed");
