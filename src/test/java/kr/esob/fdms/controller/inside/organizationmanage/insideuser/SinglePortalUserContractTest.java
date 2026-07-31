package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import kr.esob.fdms.controller.inside.system.role.RequestParam;
import kr.esob.fdms.controller.login.UserVO;

class SinglePortalUserContractTest {

    @Test
    void userModelsDoNotExposeTheFormerPortalSelector() {
        assertFalse(hasField(UserVO.class, "authSite"));
        assertFalse(hasField(UserPopupParam.class, "authSite"));
        assertFalse(hasField(RequestParam.class, "authSite"));
    }

    @Test
    void userAndAuthorizationQueriesDoNotFilterOrPersistByPortal() throws Exception {
        assertFalse(read("src/main/resources/sqlMaps/oracle/its/controller/inside/organizationmanage/insideuser/Insideuser.xml")
                .contains("AUTH_SITE"));
        assertFalse(read("src/main/resources/sqlMaps/oracle/its/controller/inside/authorization/Authorization.xml")
                .contains("AUTH_SITE"));
    }

    @Test
    void oldHistoryUsesOneFormAndGridWithoutACompanyPortalBranch() throws Exception {
        String controller = read("src/main/java/kr/esob/fdms/controller/inside/distribution/oldhistory/HistoryController.java");
        assertFalse(controller.contains("getAuthSite"));
        assertFalse(controller.contains("formOutDistributionOldHistory"));
        assertFalse(controller.contains("gridOutDistributionOldHistoryList"));
        assertFalse(controller.contains("setCompanyCode"));
    }

    private boolean hasField(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredFields()).anyMatch(field -> name.equals(field.getName()));
    }

    private String read(String path) throws Exception {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}
