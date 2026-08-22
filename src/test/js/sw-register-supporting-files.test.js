const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const helperPath = "src/main/resources/static/js/views/general/distribution/swRegisterPopup.js";
const selectionPath = "src/main/resources/static/js/views/general/distribution/swSupportingFileSelection.js";
const registerViews = [
    "src/main/webapp/WEB-INF/views/general/distribution/swRegisterPage.jsp",
    "src/main/webapp/WEB-INF/views/general/distribution/swRegisterPopup.jsp"
];

class FakeDataTransfer {
    constructor() {
        this.files = [];
        this.items = {
            add: file => this.files.push(file)
        };
    }
}

const context = {
    DataTransfer: FakeDataTransfer
};
context.window = context;
vm.createContext(context);
vm.runInContext(fs.readFileSync(selectionPath, "utf8"), context);
vm.runInContext(fs.readFileSync(helperPath, "utf8"), context);

const first = { name: "manual.pdf", size: 101, lastModified: 11, type: "application/pdf" };
const second = { name: "drawing.dwg", size: 202, lastModified: 22, type: "application/acad" };
const third = { name: "review.xlsx", size: 303, lastModified: 33, type: "application/vnd.ms-excel" };
const sameAsFirst = { name: "MANUAL.PDF", size: 101, lastModified: 11, type: "APPLICATION/PDF" };
const input = { files: [], value: "selected" };

assert.deepEqual(
    Array.from(context.appendSwAccumulatedSubFiles(input, [first, second]), file => file.name),
    ["manual.pdf", "drawing.dwg"]
);
assert.deepEqual(
    Array.from(context.appendSwAccumulatedSubFiles(input, [third]), file => file.name),
    ["manual.pdf", "drawing.dwg", "review.xlsx"],
    "a second file-picker action must add to, rather than replace, the first selection"
);
assert.equal(
    context.appendSwAccumulatedSubFiles(input, [sameAsFirst]).length,
    3,
    "reselecting the same file must not create a duplicate multipart part"
);

const appendedParts = [];
const formData = {
    append(name, value) {
        appendedParts.push({ name, value });
    }
};
context.appendSwAccumulatedSubFilesToFormData(formData, input, "subFiles");
assert.deepEqual(appendedParts.map(part => part.name), ["subFiles", "subFiles", "subFiles"]);
assert.strictEqual(appendedParts[0].value, first);
assert.strictEqual(appendedParts[1].value, second);
assert.strictEqual(appendedParts[2].value, third);

let blockedNativeFiles = [];
const fallbackInput = { value: "selected" };
Object.defineProperty(fallbackInput, "files", {
    get() { return blockedNativeFiles; },
    set() { throw new Error("read-only FileList"); }
});
assert.equal(
    context.appendSwAccumulatedSubFiles(fallbackInput, [first, second, third]).length,
    3,
    "the authoritative list must survive browsers that reject FileList assignment"
);
const fallbackParts = [];
context.appendSwAccumulatedSubFilesToFormData({
    append(name, value) { fallbackParts.push({ name, value }); }
}, fallbackInput, "subFiles");
assert.equal(fallbackParts.length, 3);

assert.deepEqual(
    Array.from(context.removeSwAccumulatedSubFile(input, 1), file => file.name),
    ["manual.pdf", "review.xlsx"],
    "an individual supporting file must be removable without clearing the rest"
);

context.clearSwAccumulatedSubFiles(input);
assert.equal(context.getSwAccumulatedSubFiles(input).length, 0);
assert.equal(input.files.length, 0);
assert.equal(input.value, "");

registerViews.forEach(viewPath => {
    const view = fs.readFileSync(viewPath, "utf8");
    const selectionScript = view.indexOf("swSupportingFileSelection.js");
    const registrationScript = view.indexOf("swRegisterPopup.js");
    assert.notEqual(selectionScript, -1, `${viewPath} must load the selection helper`);
    assert.ok(selectionScript < registrationScript,
        `${viewPath} must load the selection helper before its DOM adapter`);
    assert.match(view, /appendSwAccumulatedSubFiles\(this, this\.files \|\| \[\]\)/);
    assert.match(view, /appendSwAccumulatedSubFiles\(this, this\.files \|\| \[\]\);\s*this\.value = ''/);
    assert.match(view, /appendSwAccumulatedSubFilesToFormData\(formData, subFileInput, "subFiles"\)/);
    assert.match(view, /id="swSubFileSelectionList"/);
    assert.match(view, /removeSwAccumulatedSubFile\(input, index\)/);
    assert.match(view, /renderSwSubSelectedFiles\(\)/);
    assert.match(view, /if \(response && response\.success\) \{\s*clearSwSubSelectedFiles\(\)/);
    assert.match(view,
        /if \(allowMultiple\) \{\s*appendSwAccumulatedSubFiles\(input, files\);\s*input\.value = '';\s*renderSwSubSelectedFiles\(\);\s*return;/,
        `${viewPath} must merge a dropped batch and refresh the visible selection`);
});

console.log("SW registration supporting-file accumulation tests passed");
