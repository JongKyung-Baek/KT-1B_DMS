const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const helperPath = "src/main/resources/static/js/views/general/distribution/swRegisterPopup.js";
const cssPath = "src/main/resources/static/css/pages/sw-registration-upload-progress.css";
const registerViews = [
    "src/main/webapp/WEB-INF/views/general/distribution/swRegisterPage.jsp",
    "src/main/webapp/WEB-INF/views/general/distribution/swRegisterPopup.jsp"
];

const context = {
    console,
    clearInterval,
    setInterval
};
context.window = context;
vm.createContext(context);
vm.runInContext(fs.readFileSync(helperPath, "utf8"), context);

let now = 10_000;
const lifecycle = context.createSwRegistrationUploadLifecycle(() => now);
assert.equal(lifecycle.begin(), true);
assert.equal(lifecycle.begin(), false, "a second submit must be rejected while a request is active");
assert.equal(lifecycle.isActive(), true);
assert.equal(lifecycle.canCancel(), true, "an indeterminate transfer may be cancelled before upload.load");

let state = lifecycle.progress({
    loaded: 5 * 1024 * 1024,
    total: 10 * 1024 * 1024,
    lengthComputable: true,
    percent: 50,
    elapsedMs: 125_000
});
assert.equal(state.phase, "uploading");
assert.equal(state.percent, 50);
assert.equal(state.cancelAllowed, true);
assert.equal(context.formatSwRegistrationUploadMegabytes(state.loaded), "5.00");
assert.equal(context.formatSwRegistrationUploadElapsed(state.elapsedMs), "02:05");
assert.equal(context.formatSwRegistrationUploadElapsed(3_661_000), "01:01:01");
assert.equal(context.swRegistrationUploadTemplate("{0} / {1} MB", "5.00", "10.00"), "5.00 / 10.00 MB");

state = lifecycle.progress({
    loaded: 10 * 1024 * 1024,
    total: 10 * 1024 * 1024,
    lengthComputable: true,
    percent: 100
});
assert.equal(state.phase, "processing", "100% must become a server-processing phase");
assert.equal(state.cancelAllowed, false, "abort must be disabled after all bytes are transferred");
assert.equal(lifecycle.requestCancel(), false);
state = lifecycle.finish("success");
assert.equal(state.active, false);
assert.equal(state.outcome, "success");

assert.equal(lifecycle.begin(), true, "cleanup must allow a later retry");
state = lifecycle.progress({ loaded: 1234, lengthComputable: false, elapsedMs: 1000 });
assert.equal(state.lengthComputable, false);
assert.equal(state.percent, null);
assert.equal(state.cancelAllowed, true);
state = lifecycle.uploadComplete({ loaded: 1234, elapsedMs: 2000 });
assert.equal(state.phase, "processing", "upload.load must close the indeterminate cancellation window");
assert.equal(state.cancelAllowed, false);
assert.equal(lifecycle.finish("error").active, false, "error cleanup must release the in-flight guard");

["success", "error", "abort"].forEach(outcome => {
    const runtime = context.createSwRegistrationUploadLifecycle(() => 100);
    assert.equal(runtime.begin(), true);
    const finished = runtime.finish(outcome);
    assert.equal(finished.active, false, `${outcome} must clean up the active state`);
    assert.equal(finished.cancelAllowed, false, `${outcome} must disable cancellation`);
    assert.equal(runtime.begin(), true, `${outcome} cleanup must re-enable submission`);
});

const helper = fs.readFileSync(helperPath, "utf8");
assert.match(helper, /global:\s*false/);
assert.match(helper, /handleDefaultError:\s*false/);
assert.match(helper, /onStart:\s*function/);
assert.match(helper, /onProgress:\s*function/);
assert.match(helper, /onUploadComplete:\s*function/);
assert.match(helper, /onSuccess:\s*function/);
assert.match(helper, /onError:\s*function/);
assert.match(helper, /onAbort:\s*function/);
assert.match(helper, /onComplete:\s*function/);
assert.match(helper, /window\.top\.location\.href\s*=\s*"\/login\/loginPage"/,
    "401 handling must retain the legacy login redirect after acknowledgement");
assert.match(helper, /snapshot\.phase === "processing"[\s\S]*verifyRegistration/,
    "ambiguous failures after transfer completion must tell users to verify the list");

registerViews.forEach(viewPath => {
    const view = fs.readFileSync(viewPath, "utf8");
    assert.match(view, /sw-registration-upload-progress\.css\?v=20260827\.1/);
    assert.match(view, /data-sw-upload-progress/);
    assert.match(view, /data-sw-upload-bar/);
    assert.match(view, /data-sw-upload-metrics/);
    assert.match(view, /data-sw-upload-elapsed/);
    assert.match(view, /data-sw-upload-cancel/);
    assert.match(view, /role="progressbar"/);
    assert.match(view, /callAjaxUpload\([\s\S]*?requestCrXCallback,[\s\S]*?createSwRegistrationUploadAjaxOptions\(\)[\s\S]*?\)/);
    assert.match(view, /attachSwRegistrationUploadHandle\(uploadHandle\)/);
    assert.match(view, /finishSwRegistrationUploadUi\(response && response\.success \? "success" : "response-error"\)/);

    const saveFunction = view.slice(view.indexOf("function saveX()"), view.indexOf("function requestCrXCallback"));
    const guardIndex = saveFunction.indexOf("isSwRegistrationUploadActive()");
    const validationIndex = saveFunction.indexOf("isValidationX()");
    const beginIndex = saveFunction.indexOf("beginSwRegistrationUpload()");
    const requestIndex = saveFunction.indexOf("callAjaxUpload(");
    assert.ok(guardIndex >= 0 && guardIndex < validationIndex,
        `${viewPath} must block duplicates before validation mutates file state`);
    assert.ok(validationIndex < beginIndex && beginIndex < requestIndex,
        `${viewPath} must lock the UI after validation and before the POST`);
});

const css = fs.readFileSync(cssPath, "utf8");
assert.match(css, /\.sw-registration-upload\[hidden\]/);
assert.match(css, /\.sw-registration-upload__bar\.is-indeterminate/);
assert.match(css, /button\.is-upload-locked/);

[
    "src/main/webapp/messages/feature.properties",
    "src/main/webapp/messages/feature_en.properties",
    "src/main/webapp/messages/feature_id.properties"
].forEach(bundlePath => {
    const bundle = fs.readFileSync(bundlePath, "utf8");
    assert.match(bundle, /feature\.techRegister\.uploadProgress\.processing=/);
    assert.match(bundle, /feature\.techRegister\.uploadProgress\.verifyRegistration=/);
    assert.match(bundle, /feature\.techRegister\.uploadProgress\.error\.payloadTooLarge=/);
});

console.log("SW registration upload-progress tests passed");
