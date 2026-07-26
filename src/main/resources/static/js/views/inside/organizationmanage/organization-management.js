(function (window, $) {
  "use strict";

  if (!window || !$) {
    return;
  }

  function text(key, fallback) {
    return window.SdmsI18n && typeof window.SdmsI18n.t === "function"
      ? window.SdmsI18n.t(key, fallback)
      : fallback;
  }

  function resetSearchForm($form) {
    if (!$form.length) {
      return;
    }

    if ($form[0] && typeof $form[0].reset === "function") {
      $form[0].reset();
    }

    $form.find("select").each(function () {
      $(this).trigger("change");
    });

    if (typeof window.searchList === "function" && window.gridParam) {
      window.searchList(window.gridParam);
    }
  }

  function enhanceSearchActions() {
    var formId = window.formId;
    var $form = formId ? $("#" + formId) : $();
    var $actions = $form.find(".formAcceptanceActions").first();

    if (!$actions.length || $actions.find(".organization-management-reset-btn").length) {
      return;
    }

    var $resetButton = $("<button>", {
      type: "button",
      "class": "btn resetBtn organization-management-reset-btn",
      "aria-label": text("feature.common.search.resetAria", "검색 조건 초기화")
    });

    $resetButton
      .append($("<i>", {
        "class": "icon-base ti tabler-refresh",
        "aria-hidden": "true"
      }))
      .append(document.createTextNode(text("feature.common.reset", "초기화")))
      .on("click.organizationManagement", function () {
        resetSearchForm($form);
      });

    $actions.prepend($resetButton);
  }

  $(enhanceSearchActions);
})(window, window.jQuery);
