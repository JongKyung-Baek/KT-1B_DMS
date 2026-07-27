package kr.esob.fdms.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class OrganizationManagementPersistenceContractTest {

	private static final Path USER_MAPPER = Paths.get(
			"src/main/resources/sqlMaps/oracle/its/controller/inside/"
					+ "organizationmanage/insideuser/Insideuser.xml");
	private static final Path INTEGRITY_DDL = Paths.get(
			"src/main/resources/sql/organization_management_integrity_ddl.sql");

	@Test
	void newUsersStartWithZeroFailedLoginAttempts() throws Exception {
		String mapper = read(USER_MAPPER);
		String ddl = read(INTEGRITY_DDL);

		assertTrue(mapper.contains("LOGIN_COUNT,"));
		assertTrue(mapper.contains("#{lockYn},\n\t\t0,"));
		assertTrue(ddl.contains("SET login_count = 0"));
		assertTrue(ddl.contains("ALTER COLUMN login_count SET DEFAULT 0"));
		assertTrue(ddl.contains("ALTER COLUMN login_count SET NOT NULL"));
	}

	private String read(Path path) throws Exception {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
				.replace("\r\n", "\n");
	}
}
