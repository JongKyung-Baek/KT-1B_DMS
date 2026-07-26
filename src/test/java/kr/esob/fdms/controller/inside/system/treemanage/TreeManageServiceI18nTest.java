package kr.esob.fdms.controller.inside.system.treemanage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.result.ResultVO;

class TreeManageServiceI18nTest {

	@AfterEach
	void resetLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void insertValidationUsesCurrentRequestLocale() {
		TreeManageService service = serviceWithEnglishMessages();
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		ResultVO result = service.insertNode(new TreeManageSaveParam());

		assertEquals("Enter a name.", result.getFailReason());
	}

	@Test
	void generatedTreeCodeFailureUsesCurrentRequestLocale() {
		TreeManageService service = serviceWithEnglishMessages();
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		TreeManageSaveParam param = new TreeManageSaveParam();
		param.setManageType("LEVEL");
		param.setTreeNm("Level 1");

		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> service.insertNode(param));

		assertEquals("Failed to generate TREE_CD.", exception.getMessage());
	}

	private TreeManageService serviceWithEnglishMessages() {
		StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage(
				"feature.treeManage.validation.nameRequired",
				Locale.ENGLISH,
				"Enter a name.");
		messages.addMessage(
				"feature.treeManage.error.treeCodeGenerationFailed",
				Locale.ENGLISH,
				"Failed to generate TREE_CD.");

		Prop prop = new Prop();
		prop.setMessageSource(messages);

		TreeManageService service = new TreeManageService();
		ReflectionTestUtils.setField(service, "dao", mock(TreeManageDao.class));
		ReflectionTestUtils.setField(service, "prop", prop);
		return service;
	}
}
