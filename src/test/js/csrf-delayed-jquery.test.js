const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

const csrfPath = "src/main/resources/static/js/csrf.js";
const csrfSource = fs.readFileSync(csrfPath, "utf8");

function createEventTarget() {
    const listeners = new Map();
    return {
        addEventListener(type, listener) {
            const registered = listeners.get(type) || [];
            registered.push(listener);
            listeners.set(type, registered);
        },
        dispatch(type, event = {}) {
            for (const listener of listeners.get(type) || []) {
                listener(event);
            }
        }
    };
}

function createVirtualClock() {
    let now = 0;
    let nextId = 1;
    const timers = [];

    return {
        setTimeout(callback, delay) {
            const id = nextId++;
            timers.push({ id, at: now + Number(delay || 0), callback });
            return id;
        },
        advanceTo(targetTime) {
            while (true) {
                timers.sort((left, right) => left.at - right.at || left.id - right.id);
                if (!timers.length || timers[0].at > targetTime) break;
                const timer = timers.shift();
                now = timer.at;
                timer.callback();
            }
            now = targetTime;
        },
        now() {
            return now;
        }
    };
}

function createJQueryHarness() {
    const prefilters = [];
    const jquery = function () {
        return {
            off() { return this; },
            on() { return this; }
        };
    };
    jquery.ajaxPrefilter = callback => prefilters.push(callback);
    return { jquery, prefilters };
}

const clock = createVirtualClock();
const documentEvents = createEventTarget();
const windowEvents = createEventTarget();
const meta = {
    _csrf: "delayed-token",
    _csrf_header: "X-CSRF-TOKEN",
    _csrf_parameter: "_csrf"
};
const document = {
    readyState: "loading",
    addEventListener: documentEvents.addEventListener,
    querySelector(selector) {
        const match = selector.match(/^meta\[name="([^"]+)"\]$/);
        if (!match || !(match[1] in meta)) return null;
        return {
            getAttribute(name) {
                return name === "content" ? meta[match[1]] : null;
            }
        };
    },
    createElement() {
        return {};
    }
};
const window = {
    document,
    location: {
        href: "https://demo.esob.kr:444/general/distribution/swRequest/regist",
        origin: "https://demo.esob.kr:444",
        protocol: "https:",
        host: "demo.esob.kr:444"
    },
    addEventListener: windowEvents.addEventListener,
    setTimeout: clock.setTimeout,
    URL
};
const context = { window, document, URL };
vm.createContext(context);
vm.runInContext(csrfSource, context, { filename: csrfPath });

// Exhaust the bounded 40 x 50 ms polling window without jQuery.
clock.advanceTo(2500);
assert.equal(clock.now(), 2500);

const jqueryHarness = createJQueryHarness();
window.jQuery = jqueryHarness.jquery;
assert.equal(
    jqueryHarness.prefilters.length,
    0,
    "bounded polling alone must already be exhausted after 2.5 seconds"
);

// Parser-blocking jQuery is present before DOMContentLoaded even when its download
// takes longer than the polling window. The lifecycle hook must install the filter.
documentEvents.dispatch("DOMContentLoaded");
assert.equal(
    jqueryHarness.prefilters.length,
    1,
    "DOMContentLoaded must install the CSRF prefilter when jQuery appears after 2 seconds"
);

const headers = new Map();
jqueryHarness.prefilters[0](
    { type: "POST", url: "/general/distribution/swRequest/uploadSwRegisFile" },
    {},
    { setRequestHeader(name, value) { headers.set(name, value); } }
);
assert.equal(headers.get("X-CSRF-TOKEN"), "delayed-token");

// A later load event and explicit installation must remain idempotent.
windowEvents.dispatch("load");
window.SdmsCsrf.install();
assert.equal(jqueryHarness.prefilters.length, 1);

const jspRoots = [
    "src/main/webapp/WEB-INF/decorator",
    "src/main/webapp/WEB-INF/views/login"
];
const jspFiles = jspRoots.flatMap(root =>
    fs.readdirSync(root)
        .filter(name => name.endsWith(".jsp"))
        .map(name => path.join(root, name))
);
let checkedLoadOrders = 0;
for (const jspPath of jspFiles) {
    const source = fs.readFileSync(jspPath, "utf8");
    const csrfIndex = source.indexOf('include file="/WEB-INF/jspf/csrf-meta.jspf"');
    const jqueryMatch = source.match(/<script\b([^>]*)src="\$\{pageContext\.request\.contextPath\}\/resources\/js\/jquery-3\.4\.1\.min\.js"([^>]*)><\/script>/i);
    if (csrfIndex === -1 || !jqueryMatch) continue;

    const jqueryIndex = source.indexOf(jqueryMatch[0]);
    assert.ok(csrfIndex < jqueryIndex, `${jspPath} must load csrf.js before jQuery`);
    assert.doesNotMatch(
        `${jqueryMatch[1]} ${jqueryMatch[2]}`,
        /\b(?:async|defer)\b/i,
        `${jspPath} must keep jQuery parser-blocking so lifecycle installation is deterministic`
    );
    checkedLoadOrders += 1;
}
assert.ok(checkedLoadOrders >= 10, "all shared decorators and login views must be covered");

console.log(`CSRF delayed-jQuery regression passed; audited ${checkedLoadOrders} JSP load orders`);
