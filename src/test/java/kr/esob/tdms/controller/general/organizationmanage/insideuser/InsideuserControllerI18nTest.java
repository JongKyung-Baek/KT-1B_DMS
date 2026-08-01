package kr.esob.tdms.controller.general.organizationmanage.insideuser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import kr.esob.tdms.commonlogic.combo.ComboInfoVO;
import kr.esob.tdms.commonlogic.message.Prop;

class InsideuserControllerI18nTest {

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void registerPopupSelectPromptsUseCurrentEnglishLocale() throws Exception {
		InsideuserController controller = controllerWithMessages();
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		ExtendedModelMap model = new ExtendedModelMap();

		String view = controller.registerUser(model, new UserPopupParam());

		assertEquals(
				"general/organizationmanage/insideuser/registerUserPopup",
				view);
		assertFirstComboLabel(model, "noSelect_dept", "Please select.");
		assertFirstComboLabel(model, "noSelect_position", "Please select.");
		assertFirstComboLabel(model, "noSelect_roleGroup", "Please select.");
	}

	@Test
	void registerPopupSelectPromptsUseCurrentKoreanLocale() throws Exception {
		InsideuserController controller = controllerWithMessages();
		LocaleContextHolder.setLocale(Locale.KOREAN);
		ExtendedModelMap model = new ExtendedModelMap();

		controller.registerUser(model, new UserPopupParam());

		assertFirstComboLabel(model, "noSelect_dept", "선택하세요");
		assertFirstComboLabel(model, "noSelect_position", "선택하세요");
		assertFirstComboLabel(model, "noSelect_roleGroup", "선택하세요");
	}

	private InsideuserController controllerWithMessages() {
		StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage(
				"feature.organization.common.selectPrompt",
				Locale.KOREAN,
				"선택하세요");
		messages.addMessage(
				"feature.organization.common.selectPrompt",
				Locale.ENGLISH,
				"Please select.");

		Prop prop = new Prop();
		prop.setMessageSource(messages);

		InsideuserController controller = new InsideuserController();
		ReflectionTestUtils.setField(controller, "prop", prop);
		return controller;
	}

	@SuppressWarnings("unchecked")
	private void assertFirstComboLabel(
			ExtendedModelMap model, String attribute, String expected) {
		List<ComboInfoVO> options =
				(List<ComboInfoVO>) model.get(attribute);
		assertEquals(expected, options.get(0).getComboLabel());
	}
}
