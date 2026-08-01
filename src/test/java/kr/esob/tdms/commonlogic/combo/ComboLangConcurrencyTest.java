package kr.esob.tdms.commonlogic.combo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

class ComboLangConcurrencyTest {

    @BeforeEach
    void clearBeforeTest() {
        ComboLang.clear();
        LocaleContextHolder.resetLocaleContext();
    }

    @AfterEach
    void clearAfterTest() {
        LocaleContextHolder.resetLocaleContext();
        ComboLang.clear();
    }

    @Test
    void currentRequestLocaleSelectsItsOwnLanguageMap() {
        ComboLang.replaceLanguage("ko-KR", languageMap("진행"));
        ComboLang.replaceLanguage("en-US", languageMap("In progress"));

        LocaleContextHolder.setLocale(Locale.KOREA);
        assertEquals(
                "진행",
                ComboLang.getComboLang("requestStatus", "IN_PROGRESS"));

        LocaleContextHolder.setLocale(Locale.US);
        assertEquals(
                "In progress",
                ComboLang.getComboLang("requestStatus", "IN_PROGRESS"));
        assertEquals(
                "UNKNOWN",
                ComboLang.getComboLang("requestStatus", "UNKNOWN"));
    }

    @Test
    void listReplacementBuildsAnImmutableLanguageSnapshot() {
        ComboCdVO combo = new ComboCdVO();
        combo.setComboCd("requestStatus");
        combo.setValue("IN_PROGRESS");
        combo.setLangDesc("Sedang berlangsung");

        ComboLang.replaceLanguage("id-ID", Arrays.asList(combo));
        combo.setLangDesc("changed after publication");

        assertTrue(ComboLang.containsLanguage("id"));
        assertEquals(
                "Sedang berlangsung",
                ComboLang.getComboLang(
                        "id", "requestStatus", "IN_PROGRESS"));
        ComboLang.clearLanguage("id-ID");
        assertFalse(ComboLang.containsLanguage("id"));
    }

    @Test
    void simultaneousKoreanAndEnglishRequestsNeverShareLabels()
            throws Exception {
        ComboLang.replaceLanguage("ko", languageMap("승인"));
        ComboLang.replaceLanguage("en", languageMap("Approved"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> korean = executor.submit(
                    () -> labelsRemain(Locale.KOREA, "승인", ready, start));
            Future<Boolean> english = executor.submit(
                    () -> labelsRemain(Locale.US, "Approved", ready, start));

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            assertTrue(korean.get(5, TimeUnit.SECONDS));
            assertTrue(english.get(5, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private boolean labelsRemain(
            Locale locale,
            String expected,
            CountDownLatch ready,
            CountDownLatch start) throws Exception {
        LocaleContextHolder.setLocale(locale);
        ready.countDown();
        start.await(5, TimeUnit.SECONDS);
        try {
            for (int index = 0; index < 5000; index += 1) {
                if (!expected.equals(
                        ComboLang.getComboLang("requestStatus", "IN_PROGRESS"))) {
                    return false;
                }
            }
            return true;
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    private Map<String, String> languageMap(String label) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("requestStatus|IN_PROGRESS", label);
        return map;
    }
}
