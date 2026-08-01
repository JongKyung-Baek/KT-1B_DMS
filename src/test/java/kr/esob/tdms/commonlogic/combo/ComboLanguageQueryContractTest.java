package kr.esob.tdms.commonlogic.combo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class ComboLanguageQueryContractTest {

    private static final Path COMBO_XML = Paths.get(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/combo/Combo.xml");
    private static final Path LOGIN_SUCCESS = Paths.get(
            "src/main/java/kr/esob/tdms/controller/login/LoginSuccess.java");
    private static final Path LOGOUT_SUCCESS = Paths.get(
            "src/main/java/kr/esob/tdms/controller/login/LogoutSuccess.java");

    @Test
    void everyActiveComboTranslationUsesTheSessionLanguageParameter()
            throws Exception {
        String mapper = read(COMBO_XML);

        assertFalse(mapper.matches(
                "(?s).*LANG_TYPE\\s*=\\s*['\"]ko['\"].*"));
        assertEquals(6, occurrences(mapper, "LANG_TYPE = #{sessionLang}"));
        assertTrue(mapper.contains("<select id=\"selectComboLang\""));
        assertTrue(mapper.contains("<select id=\"selectComboListByCd\""));
        assertTrue(mapper.contains("<select id=\"selectComboList\""));
        assertTrue(mapper.contains("<select id=\"selectObjectClassCdSw\""));
        assertTrue(mapper.contains("<select id=\"selectObjectClassCdDoc\""));
    }

    @Test
    void loginPublishesOnlyItsLanguageAndLogoutNeverClearsOtherUsers()
            throws Exception {
        String login = read(LOGIN_SUCCESS);
        String logout = read(LOGOUT_SUCCESS);

        assertTrue(login.contains("ComboLang.replaceLanguage("));
        assertTrue(login.contains("comboDao.selectComboLang(sessionLang)"));
        assertFalse(login.contains("ComboLang.comboLang"));
        assertFalse(logout.contains("ComboLang"));
    }

    private int occurrences(String value, String token) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(token, index)) >= 0) {
            count += 1;
            index += token.length();
        }
        return count;
    }

    private String read(Path path) throws Exception {
        return new String(
                Files.readAllBytes(path),
                StandardCharsets.UTF_8);
    }
}
