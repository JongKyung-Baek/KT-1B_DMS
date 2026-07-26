(function () {
	"use strict";

	function homeUrl() {
		return document.body.getAttribute("data-home-url") || "/";
	}

	function goBackSafely() {
		try {
			var referrer = document.referrer ? new URL(document.referrer) : null;
			if (referrer
					&& referrer.origin === window.location.origin
					&& window.history.length > 1) {
				window.history.back();
				return;
			}
		} catch (ignored) {
			// A malformed referrer falls back to the authentication-aware root.
		}

		window.location.assign(homeUrl());
	}

	document.addEventListener("DOMContentLoaded", function () {
		var backButton = document.querySelector("[data-error-back]");
		if (backButton) {
			backButton.addEventListener("click", goBackSafely);
		}
	});
}());
