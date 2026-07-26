package kr.esob.fdms.presentation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class OrganizationPopupI18nContractTest {

	private static final Path USER_POPUP = Paths.get(
			"src/main/webapp/WEB-INF/views/inside/organizationmanage/"
					+ "insideuser/registerUserPopup.jsp");
	private static final Path DEPARTMENT_POPUP = Paths.get(
			"src/main/webapp/WEB-INF/views/inside/organizationmanage/"
					+ "insidedept/registerDeptPopup.jsp");

	@Test
	void userPopupUsesMessageKeysForVisibleHeadingAndDescription()
			throws Exception {
		String source = read(USER_POPUP);

		assertTrue(source.contains(
				"feature.organization.user.popup.create.title"));
		assertTrue(source.contains(
				"feature.organization.user.popup.edit.title"));
		assertTrue(source.contains(
				"feature.organization.user.popup.description"));
		assertFalse(source.contains(">내부 사용자 수정<"));
		assertFalse(source.contains(">내부 사용자 등록<"));
		assertFalse(source.contains(
				">내부 사용자 기본 정보와 권한 항목을 입력하거나 수정할 수 있습니다.<"));
	}

	@Test
	void departmentPopupUsesMessageKeysForVisibleHeadingAndDescription()
			throws Exception {
		String source = read(DEPARTMENT_POPUP);

		assertTrue(source.contains(
				"feature.organization.department.popup.title"));
		assertTrue(source.contains(
				"feature.organization.department.popup.description"));
		assertFalse(source.contains(">부서 정보<"));
		assertFalse(source.contains(
				">부서 코드와 부서명, 사용 여부를 입력하거나 수정할 수 있습니다.<"));
	}

	private String read(Path path) throws Exception {
		return new String(
				Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
