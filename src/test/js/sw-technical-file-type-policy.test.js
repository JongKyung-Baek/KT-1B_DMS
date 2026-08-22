const assert = require("node:assert/strict");
const policyModule = require("../../main/resources/static/js/views/general/distribution/swTechnicalFileTypePolicy.js");

const policy = policyModule.create({
    directPdf: "pdf",
    directStep: "stp,step",
    pdfConversion: "docx,xlsx,hwp,png"
});

assert.deepEqual(policy.classify("manual.PDF"), {
    fileName: "manual.PDF",
    extension: "pdf",
    status: "DIRECT_PDF",
    registrationAllowed: true,
    viewerSupported: true,
    conversionRequired: false
});
assert.equal(policy.classify("assembly.step").status, "DIRECT_STEP");
assert.equal(policy.classify("drawing.docx").status, "PDF_CONVERSION");

const unsupported = policy.classify("native-model.dwg");
assert.equal(unsupported.status, "UNSUPPORTED_VIEWER");
assert.equal(unsupported.registrationAllowed, true);
assert.equal(unsupported.viewerSupported, false);
assert.equal(unsupported.conversionRequired, false);

for (const invalid of ["no-extension", ".hidden", "bad/name.pdf", "bad.exe\n.pdf"]) {
    assert.equal(policy.classify(invalid).status, "INVALID_FILE_NAME", invalid);
    assert.equal(policy.classify(invalid).registrationAllowed, false, invalid);
}

console.log("SW technical file type policy tests passed");
