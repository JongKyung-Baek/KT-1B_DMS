package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.controller.login.UserVO;

class SwRequestControllerI18nTest {

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void emptyWithdrawSelectionUsesCurrentRequestLocale() {
		SwRequestController controller = controllerWithEnglishMessages(mock(SwRequestService.class));
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Map<String, Object> result = controller.deleteSW(new HashMap<>(), authentication());

		assertEquals("Select an item to withdraw.", result.get("error"));
	}

	@Test
	void emptyApprovalSelectionUsesCurrentRequestLocale() {
		SwRequestController controller = controllerWithEnglishMessages(mock(SwRequestService.class));
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Map<String, Object> result = controller.approveSW(new HashMap<>(), authentication());

		assertEquals("Select an item to approve.", result.get("message"));
	}

	@Test
	void approvalFailureWithoutServiceDetailUsesLocalizedFallback() {
		SwRequestService service = mock(SwRequestService.class);
		ResultVO valid = new ResultVO();
		valid.setSuccess(true);
		ResultVO failed = new ResultVO();
		failed.setSuccess(false);
		when(service.validateApproveSW(any(), any())).thenReturn(valid);
		when(service.approveSW(any(), any())).thenReturn(failed);

		SwRequestController controller = controllerWithEnglishMessages(service);
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		Map<String, String> item = new HashMap<>();
		item.put("objectId", "SW-001");
		List<Map<String, String>> list = new ArrayList<>();
		list.add(item);
		Map<String, List<Map<String, String>>> request = new HashMap<>();
		request.put("list", list);

		Map<String, Object> result = controller.approveSW(request, authentication());

		assertEquals("Approval failed.", result.get("message"));
	}

	private SwRequestController controllerWithEnglishMessages(SwRequestService service) {
		StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage(
				"feature.techList.withdraw.selectionRequired",
				Locale.ENGLISH,
				"Select an item to withdraw.");
		messages.addMessage(
				"feature.techList.approval.selectionRequired",
				Locale.ENGLISH,
				"Select an item to approve.");
		messages.addMessage(
				"feature.techList.approval.failed",
				Locale.ENGLISH,
				"Approval failed.");

		Prop prop = new Prop();
		prop.setMessageSource(messages);

		SwRequestController controller = new SwRequestController();
		ReflectionTestUtils.setField(controller, "service", service);
		ReflectionTestUtils.setField(controller, "prop", prop);
		return controller;
	}

	private Authentication authentication() {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(new UserVO());
		return authentication;
	}
}
