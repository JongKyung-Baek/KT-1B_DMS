const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

const popupPath = "src/main/webapp/WEB-INF/views/general/distribution/swFilePopup.jsp";
const popup = fs.readFileSync(popupPath, "utf8");

function extractFunction(name) {
    const marker = `function ${name}(`;
    const start = popup.indexOf(marker);
    assert.notEqual(start, -1, `${name} must exist in ${popupPath}`);

    const bodyStart = popup.indexOf("{", start);
    let depth = 0;
    for (let index = bodyStart; index < popup.length; index += 1) {
        if (popup[index] === "{") depth += 1;
        if (popup[index] === "}") depth -= 1;
        if (depth === 0) return popup.slice(start, index + 1);
    }
    throw new Error(`Could not extract ${name}`);
}

const functionNames = [
    "isSwPdfFile",
    "isSwStepFile",
    "getSwFileProcessingStatus",
    "isSwFilePreviewBlocked",
    "isSwFileConversionDone",
    "isSwViewerPreviewFile"
];
const context = {};
vm.createContext(context);
vm.runInContext(functionNames.map(extractFunction).join("\n"), context);

assert.equal(context.isSwViewerPreviewFile({ orgFileNm: "drawing.pdf" }), true);
assert.equal(context.isSwViewerPreviewFile({ orgFileNm: "model.step", processingStatus: "DONE" }), true);
assert.equal(context.isSwViewerPreviewFile({ orgFileNm: "guide.docx", processingStatus: "DONE" }), true);
assert.equal(context.isSwViewerPreviewFile({ orgFileNm: "guide.docx", processingstatus: "succeeded" }), true);

for (const processingStatus of ["PENDING", "PROCESSING", "FAIL"]) {
    assert.equal(
        context.isSwViewerPreviewFile({ orgFileNm: "guide.docx", processingStatus }),
        false,
        `converted DOCX must not link while status is ${processingStatus}`
    );
    assert.equal(
        context.isSwViewerPreviewFile({ orgFileNm: "drawing.pdf", processingStatus }),
        false,
        `native PDF must not link while status is ${processingStatus}`
    );
}

assert.equal(context.isSwViewerPreviewFile({ orgFileNm: "guide.docx" }), false);
assert.match(popup, /initSwFileGrid\("gridSwMainFile", mainFileRows\)/);
assert.match(popup, /initSwFileGrid\("gridSwSubFile", subFileRows\)/);
assert.match(popup, /formatter: function \(cellValue, options, rowdata\) \{\s*return formatSwFileName/);

console.log("swFilePopup preview readiness tests passed");
