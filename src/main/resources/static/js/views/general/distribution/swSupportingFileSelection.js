(function (root, factory) {
    var api = factory();

    if (typeof module === "object" && module.exports) {
        module.exports = api;
    }
    if (root) {
        root.SwSupportingFileSelection = api;
    }
}(typeof globalThis !== "undefined" ? globalThis : this, function () {
    "use strict";

    function toArray(fileList) {
        return fileList ? Array.prototype.slice.call(fileList) : [];
    }

    function fileIdentity(file) {
        if (!file) return "";
        return [
            String(file.name || "").toLowerCase(),
            String(typeof file.size === "number" ? file.size : ""),
            String(typeof file.lastModified === "number" ? file.lastModified : ""),
            String(file.type || "").toLowerCase()
        ].join("\u0000");
    }

    function merge(existingFiles, newlySelectedFiles) {
        var merged = [];
        var known = Object.create(null);

        toArray(existingFiles).concat(toArray(newlySelectedFiles)).forEach(function (file) {
            var identity = fileIdentity(file);
            if (!identity || known[identity]) return;
            known[identity] = true;
            merged.push(file);
        });
        return merged;
    }

    function removeAt(files, index) {
        return toArray(files).filter(function (file, currentIndex) {
            return currentIndex !== index;
        });
    }

    function appendToFormData(formData, fieldName, selectedFiles) {
        toArray(selectedFiles).forEach(function (file) {
            formData.append(fieldName, file);
        });
        return formData;
    }

    return {
        toArray: toArray,
        fileIdentity: fileIdentity,
        merge: merge,
        removeAt: removeAt,
        appendToFormData: appendToFormData
    };
}));
