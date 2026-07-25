(function (window, document) {
    'use strict';

    var GLOBAL_KEY = 'SdmsCsrf';
    var SAFE_METHODS = {
        GET: true,
        HEAD: true,
        OPTIONS: true,
        TRACE: true
    };

    if (window[GLOBAL_KEY] && window[GLOBAL_KEY].initialized) {
        window[GLOBAL_KEY].install();
        return;
    }

    function readMeta(name) {
        var element = document.querySelector('meta[name="' + name + '"]');
        return element ? element.getAttribute('content') || '' : '';
    }

    function getToken() {
        return readMeta('_csrf');
    }

    function getHeaderName() {
        return readMeta('_csrf_header') || 'X-CSRF-TOKEN';
    }

    function getParameterName() {
        return readMeta('_csrf_parameter') || '_csrf';
    }

    function isUnsafeMethod(method) {
        return !SAFE_METHODS[String(method || 'GET').toUpperCase()];
    }

    function isSameOrigin(url) {
        if (!url) {
            return true;
        }

        try {
            return new URL(url, window.location.href).origin === window.location.origin;
        } catch (ignore) {
            var anchor = document.createElement('a');
            anchor.href = url;
            return !anchor.host || anchor.protocol + '//' + anchor.host === window.location.protocol + '//' + window.location.host;
        }
    }

    function headers(baseHeaders) {
        var result = {};
        var token = getToken();

        if (baseHeaders) {
            Object.keys(baseHeaders).forEach(function (name) {
                result[name] = baseHeaders[name];
            });
        }
        if (token) {
            result[getHeaderName()] = token;
        }
        return result;
    }

    function addTokenToForm(form) {
        if (!form || !isUnsafeMethod(form.method) || !isSameOrigin(form.action)) {
            return form;
        }

        var token = getToken();
        var parameterName = getParameterName();
        if (!token || !parameterName) {
            return form;
        }

        var existing = null;
        for (var index = 0; index < form.elements.length; index += 1) {
            if (form.elements[index].name === parameterName) {
                existing = form.elements[index];
                break;
            }
        }

        if (!existing) {
            existing = document.createElement('input');
            existing.type = 'hidden';
            existing.name = parameterName;
            form.appendChild(existing);
        }
        existing.value = token;
        return form;
    }

    var installedJQuery = null;
    function installJQuery() {
        var jquery = window.jQuery;
        if (!jquery || !jquery.ajaxPrefilter || installedJQuery === jquery) {
            return !!jquery;
        }

        jquery.ajaxPrefilter(function (options, originalOptions, jqXHR) {
            var method = options.type || options.method || 'GET';
            var token = getToken();
            if (!token || !isUnsafeMethod(method) || options.crossDomain || !isSameOrigin(options.url)) {
                return;
            }
            jqXHR.setRequestHeader(getHeaderName(), token);
        });

        jquery(document)
            .off('submit.sdmsCsrf')
            .on('submit.sdmsCsrf', 'form', function () {
                addTokenToForm(this);
            });
        installedJQuery = jquery;
        return true;
    }

    var installedFetch = null;
    function installFetch() {
        var currentFetch = window.fetch;
        if (!currentFetch || currentFetch.__sdmsCsrfWrapped || installedFetch === currentFetch) {
            return !!currentFetch;
        }

        function csrfFetch(input, init) {
            var requestInit = init || {};
            var method = requestInit.method;
            var url = input;

            if (typeof window.Request !== 'undefined' && input instanceof window.Request) {
                method = method || input.method;
                url = input.url;
            }

            var token = getToken();
            if (!token || !isUnsafeMethod(method) || !isSameOrigin(String(url || ''))) {
                return currentFetch.apply(this, arguments);
            }

            var nextInit = {};
            Object.keys(requestInit).forEach(function (name) {
                nextInit[name] = requestInit[name];
            });

            if (typeof window.Headers !== 'undefined') {
                var sourceHeaders = requestInit.headers;
                if (!sourceHeaders && typeof window.Request !== 'undefined' && input instanceof window.Request) {
                    sourceHeaders = input.headers;
                }
                nextInit.headers = new window.Headers(sourceHeaders || {});
                nextInit.headers.set(getHeaderName(), token);
            } else {
                nextInit.headers = headers(requestInit.headers || {});
            }

            return currentFetch.call(this, input, nextInit);
        }

        csrfFetch.__sdmsCsrfWrapped = true;
        csrfFetch.__sdmsCsrfOriginal = currentFetch;
        window.fetch = csrfFetch;
        installedFetch = csrfFetch;
        return true;
    }

    function installNativeFormHooks() {
        if (document.__sdmsCsrfSubmitListenerInstalled) {
            return;
        }

        document.addEventListener('submit', function (event) {
            addTokenToForm(event.target);
        }, true);
        document.__sdmsCsrfSubmitListenerInstalled = true;

        if (window.HTMLFormElement &&
            window.HTMLFormElement.prototype &&
            !window.HTMLFormElement.prototype.__sdmsCsrfSubmitWrapped) {
            var originalSubmit = window.HTMLFormElement.prototype.submit;
            window.HTMLFormElement.prototype.submit = function () {
                addTokenToForm(this);
                return originalSubmit.apply(this, arguments);
            };
            window.HTMLFormElement.prototype.__sdmsCsrfSubmitWrapped = true;
        }
    }

    var installAttempts = 0;
    function install() {
        installNativeFormHooks();
        var jqueryReady = installJQuery();
        var fetchReady = installFetch();

        if ((!jqueryReady || !fetchReady) && installAttempts < 40) {
            installAttempts += 1;
            window.setTimeout(install, 50);
        }
    }

    window[GLOBAL_KEY] = {
        initialized: true,
        getToken: getToken,
        getHeaderName: getHeaderName,
        getParameterName: getParameterName,
        headers: headers,
        addTokenToForm: addTokenToForm,
        isSameOrigin: isSameOrigin,
        install: install
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', install);
    }
    window.addEventListener('load', install);
    install();
})(window, document);
