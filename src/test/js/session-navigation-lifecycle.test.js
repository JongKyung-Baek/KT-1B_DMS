const assert = require("node:assert/strict");
const fs = require("node:fs");

const headerPath = "src/main/webapp/header.jsp";
const requestListPath = "src/main/webapp/WEB-INF/views/general/distribution/swRequestList.jsp";
const registerPagePath = "src/main/webapp/WEB-INF/views/general/distribution/swRegisterPage.jsp";
const commonPath = "src/main/resources/static/js/common.js";

const header = fs.readFileSync(headerPath, "utf8");
const requestList = fs.readFileSync(requestListPath, "utf8");
const registerPage = fs.readFileSync(registerPagePath, "utf8");
const common = fs.readFileSync(commonPath, "utf8");

function extractFunction(source, name) {
    const marker = `function ${name}(`;
    const start = source.indexOf(marker);
    assert.notEqual(start, -1, `${name} must exist`);
    const bodyStart = source.indexOf("{", start);
    let depth = 0;
    for (let index = bodyStart; index < source.length; index += 1) {
        if (source[index] === "{") depth += 1;
        if (source[index] === "}") depth -= 1;
        if (depth === 0) return source.slice(start, index + 1);
    }
    throw new Error(`Could not extract ${name}`);
}

// A page transition is not an authentication event. In particular, registration
// is reached by a toolbar button that assigns location.href rather than by an
// anchor or form submit, so a delegated navigation marker cannot make pagehide
// safe enough to invalidate a server session.
assert.match(
    requestList,
    /function upload\(\)[\s\S]*?location\.href\s*=\s*url\s*;/,
    "technical-data registration must remain covered as a programmatic navigation"
);
assert.match(requestList, /var url = "\/general\/distribution\/swRequest\/regist"/);

assert.doesNotMatch(
    header,
    /navigator\.sendBeacon|notifyLogoutOnLeave|clearPendingLogoutOnStay/,
    "normal page navigation must never send or cancel a delayed logout request"
);
assert.doesNotMatch(
    header,
    /addEventListener\s*\(\s*['"](?:pagehide|unload)['"]/,
    "page lifecycle events must not be used to end the authenticated session"
);
assert.doesNotMatch(
    registerPage,
    /notifyLogoutOnLeave|clearPendingLogoutOnStay/,
    "the new registration page must not depend on a late DOM-ready cancellation"
);

// The only first-party beforeunload hook is local download cleanup. It must not
// perform a request, beacon, redirect, or session mutation.
const downloadCleanupMatch = common.match(
    /function bindDownloadCleanupOnUnload\(\)\s*\{([\s\S]*?)\n\}/
);
assert.ok(downloadCleanupMatch, "download cleanup lifecycle hook must remain auditable");
assert.match(downloadCleanupMatch[1], /cleanupDownload\(/);
assert.doesNotMatch(
    downloadCleanupMatch[1],
    /\$\.ajax|fetch\s*\(|sendBeacon|XMLHttpRequest|location\.|logout|session/i
);

// Session termination is allowed only through the explicit POST logout form;
// CSRF protection is added before submission.
const explicitLogout = extractFunction(header, "logout");
assert.match(explicitLogout, /logoutForm\.method\s*=\s*['"]POST['"]/);
assert.match(explicitLogout, /\/login\/logout/);
assert.match(explicitLogout, /SdmsCsrf\.addTokenToForm\(logoutForm\)/);
assert.match(explicitLogout, /logoutForm\.submit\(\)/);

console.log("Session navigation lifecycle regression tests passed");
