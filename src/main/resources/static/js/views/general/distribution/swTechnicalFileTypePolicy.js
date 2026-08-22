(function (root, factory) {
    var api = factory();

    if (typeof module === "object" && module.exports) {
        module.exports = api;
    }
    if (root) {
        root.SwTechnicalFileTypePolicy = api;
    }
}(typeof globalThis !== "undefined" ? globalThis : this, function () {
    "use strict";

    var STATUS = Object.freeze({
        DIRECT_PDF: "DIRECT_PDF",
        DIRECT_STEP: "DIRECT_STEP",
        PDF_CONVERSION: "PDF_CONVERSION",
        UNSUPPORTED_VIEWER: "UNSUPPORTED_VIEWER",
        INVALID_FILE_NAME: "INVALID_FILE_NAME"
    });

    function normalizeExtensions(value) {
        var values = Array.isArray(value) ? value : String(value || "").split(",");
        return values.reduce(function (result, item) {
            var extension = String(item || "").trim().replace(/^\./, "").toLowerCase();
            if (/^[a-z0-9]{1,16}$/.test(extension)) {
                result[extension] = true;
            }
            return result;
        }, Object.create(null));
    }

    function extensionOf(fileName) {
        var normalized = String(fileName || "").trim();
        if (!normalized || normalized.length > 255 || /[\\/\u0000-\u001f\u007f]/.test(normalized)) {
            return "";
        }
        var dot = normalized.lastIndexOf(".");
        if (dot <= 0 || dot === normalized.length - 1) {
            return "";
        }
        var extension = normalized.slice(dot + 1).toLowerCase();
        return /^[a-z0-9]{1,16}$/.test(extension) ? extension : "";
    }

    function create(config) {
        var policy = config || {};
        var directPdf = normalizeExtensions(policy.directPdf);
        var directStep = normalizeExtensions(policy.directStep);
        var pdfConversion = normalizeExtensions(policy.pdfConversion);

        function classify(fileOrName) {
            var fileName = typeof fileOrName === "string"
                ? fileOrName
                : String(fileOrName && fileOrName.name || "");
            var extension = extensionOf(fileName);
            var status = STATUS.UNSUPPORTED_VIEWER;

            if (!extension) {
                status = STATUS.INVALID_FILE_NAME;
            } else if (directPdf[extension]) {
                status = STATUS.DIRECT_PDF;
            } else if (directStep[extension]) {
                status = STATUS.DIRECT_STEP;
            } else if (pdfConversion[extension]) {
                status = STATUS.PDF_CONVERSION;
            }

            return {
                fileName: fileName,
                extension: extension,
                status: status,
                registrationAllowed: status !== STATUS.INVALID_FILE_NAME,
                viewerSupported: status !== STATUS.INVALID_FILE_NAME
                    && status !== STATUS.UNSUPPORTED_VIEWER,
                conversionRequired: status === STATUS.PDF_CONVERSION
            };
        }

        return {
            classify: classify
        };
    }

    return {
        STATUS: STATUS,
        extensionOf: extensionOf,
        normalizeExtensions: normalizeExtensions,
        create: create
    };
}));
