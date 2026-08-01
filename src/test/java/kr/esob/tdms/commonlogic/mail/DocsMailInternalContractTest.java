package kr.esob.tdms.commonlogic.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class DocsMailInternalContractTest {

    private static final Set<String> ACTIVE_TYPES = Set.of(
            "DISTRIBUTION_APPROVAL",
            "DISTRIBUTION_PRODUCT_APPROVAL",
            "DISTRIBUTION_PRINT_APPROVAL",
            "DISTRIBUTION_DRAWING_STATUS",
            "DISTRIBUTION_DOC_STATUS",
            "DISTRIBUTION_SW_STATUS",
            "DISTRIBUTION_PRODUCT_STATUS",
            "DISTRIBUTION_PRINT_HISTORY",
            "DISTRIBUTION_DELETE_COMPANY",
            "CR_APPROVAL",
            "CR_STATUS",
            "PRODUCT_APPROVAL",
            "PRODUCT_STATUS",
            "PRODUCT_ACCEPT",
            "PRODUCT_DISPOSAL_APPROVAL",
            "PRODUCT_PRINT_APPROVAL",
            "PRODUCT_PRINT_STATUS",
            "REVISION_UPDATED");

    private static final Set<String> RETIRED_TYPES = Set.of(
            "DISTRIBUTION_APPROVAL_OUTSIDE",
            "DISTRIBUTION_PRODUCT_APPROVAL_OUTSIDE",
            "DISTRIBUTION_DISPOSAL_OUTSIDE",
            "DISTRIBUTION_VALID_TERM_OVER",
            "UNREG_APPROVAL",
            "UNREG_STATUS",
            "OUTREG_STATUS",
            "DUANZONG_DOCS");

    @Test
    void enumContainsOnlyActiveInternalMailTypes() {
        Set<String> actual = Arrays.stream(DocsMailEnum.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertEquals(ACTIVE_TYPES, actual);
        RETIRED_TYPES.forEach(type -> assertThrows(
                IllegalArgumentException.class,
                () -> DocsMailEnum.valueOf(type)));
        assertThrows(
                NoSuchFieldException.class,
                () -> DocsMailEnum.class.getDeclaredField("urlType"));
    }

    @Test
    void everyMailUsesAConcreteInternalRouteAndNeutralCopy() {
        for (DocsMailEnum mail : DocsMailEnum.values()) {
            assertTrue(mail.getUrl().startsWith("/general/"), mail.name());
            assertFalse(mail.getUrl().contains("/outside/"), mail.name());
            assertFalse(mail.getUrl().contains("url_type"), mail.name());
            assertFalse(mail.getTitle().contains("외부"), mail.name());
            assertFalse(mail.getTitle().contains("협력"), mail.name());
            assertFalse(mail.getContent().contains("외부"), mail.name());
            assertFalse(mail.getContent().contains("협력"), mail.name());

            String html = mail.getFormattedContent();
            assertTrue(html.contains(mail.getUrl()), mail.name());
            assertTrue(html.contains("KT-1B DMS"), mail.name());
        }
    }

    @Test
    void sourceAndMapperHaveNoExternalMailContract() throws IOException {
        String enumSource = read(Path.of(
                "src", "main", "java", "kr", "esob", "tdms",
                "commonlogic", "mail", "DocsMailEnum.java"));
        String serviceSource = read(Path.of(
                "src", "main", "java", "kr", "esob", "tdms",
                "commonlogic", "mail", "DocsMailService.java"));
        String requestService = read(Path.of(
                "src", "main", "java", "kr", "esob", "tdms",
				"controller", "general", "distribution", "commonrequest",
                "CommonDistributionRequestService.java"));
        String mapper = read(Path.of(
                "src", "main", "resources", "sqlMaps", "oracle", "its",
                "commonlogic", "mail", "DocsMail.xml"));

        String combined = enumSource + serviceSource + requestService;
        assertFalse(combined.contains("_OUTSIDE"));
        assertFalse(combined.contains("/outside/"));
        assertFalse(combined.contains("getUrlType"));
        assertFalse(combined.contains("urlType"));
        assertFalse(combined.contains("UNREG"));
        assertFalse(mapper.contains("selectUnregSecurityUserInfo"));
        assertFalse(mapper.contains("AUTH_SITE"));
        assertFalse(Files.exists(Path.of(
                "src", "main", "java", "kr", "esob", "tdms",
                "commonlogic", "mail", "DocsMailController.java")));
    }

    private String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
