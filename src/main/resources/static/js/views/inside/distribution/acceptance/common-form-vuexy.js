(function (window, $) {
  "use strict";

  if (!window || !$ || typeof window.settingForm !== "function") {
    return;
  }

  var originalSettingForm = window.settingForm;
  var supportedFormIds = {
    formAcceptance: true,
    formDisposalAcceptance: true,
    formRequestStatus: true,
    formPeerReviewRequest: true,
    formDocRequest: true,
    formDrawingRequest: true,
    formSwRequest: true,
    formProductionRequest: true,
    formDxfRequest: true,
    formItsSend: true,
    formDownHistory: true,
    formDistributionHistory: true,
    formViewPrintHistoryList: true,
    formDistributionApproval: true,
    formPrintApproval: true,
    formPrintDestroyApproval: true,
    formBbsNotice: true,
    formBbsQna: true,
    formInsideUser: true,
    formInsideDept: true,
    formInsideAuditLog: true,
    formOrganizationmanageApproval: true,
    formOutsideCompany: true,
    formOutsideUser: true,
    formOutsideUserInformation: true,
    formOutsideUserStatus: true,
    formOutsideDrawingRequest: true,
    formOutsideDrawingApprovalStatus: true,
    formOutsideDocRequest: true,
    formOutsideDocApprovalStatus: true,
    formOutsideSwRequest: true,
    formOutsideSwApprovalStatus: true,
    formOutsideCrRequest: true,
    formOutsideProductionRequest: true,
    formOutsideProductionApprovalStatus: true
  };

  window.settingForm = function (formData, $container) {
    if (!window.USE_ACCEPTANCE_VUEXY_FORM) {
      return originalSettingForm(formData, $container);
    }

    if (formData == null || formData === "") {
      return;
    }

    var formInfo = $.parseJSON(formData);
    var formId = formInfo && formInfo.length ? formInfo[0].formId : "";
    var result = originalSettingForm(formData, $container);

    if (supportedFormIds[formId]) {
      enhanceAcceptanceForm($container, formId);
    }

    return result;
  };

  function enhanceAcceptanceForm($container, formId) {
    var $host = $container && $container.length ? $container : $(".sbr");
    var $form = $host.find("#" + formId);

    if (!$form.length) {
      return;
    }

    $form.addClass("formAcceptance");
    $form.find(".ibx").addClass("formAcceptanceFields");
    $form.find(".ibx > li").addClass("formAcceptanceItem");
    $form.find(".ibx > li > label").addClass("form-label");
    $form.find("input[type='text'], input[type='password']").addClass("form-control");
    $form.find(".btnBox").each(function () {
      var $btnBox = $(this);
      if (!$btnBox.parent().hasClass("formAcceptanceFields")) {
        $form.find(".formAcceptanceFields").append($btnBox);
      }
      $btnBox.addClass("formAcceptanceActions formAcceptanceItem");
    });
    $form.find(".searchBtn").removeClass("ui-button ui-corner-all").addClass("btn btn-primary");

    $form.find(".input-append.date").each(function () {
      var $dateWrap = $(this);
      var $input = $dateWrap.find("input");
      $dateWrap.addClass("formAcceptanceDate");
      $input.addClass("form-control");
      $dateWrap.closest("li").addClass("is-date-range");
    });

    $form.find(".fromTo").each(function () {
      $(this).addClass("formAcceptanceDateSeparator");
    });

    $form.find(".select2-container").each(function () {
      $(this).addClass("formAcceptanceSelect2");
    });

    $form.find("select").off(".formAcceptanceVuexy");
    $form.find("select").on("select2:open.formAcceptanceVuexy", function () {
      $(".select2-container--open .select2-dropdown").addClass("formAcceptanceDropdown");
      $(".select2-container--open .select2-results__option--highlighted").addClass("formAcceptanceDropdownOption");
    });
  }
})(window, window.jQuery);
