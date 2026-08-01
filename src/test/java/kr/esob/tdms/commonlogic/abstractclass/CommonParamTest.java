package kr.esob.tdms.commonlogic.abstractclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.esob.tdms.controller.login.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CommonParamTest {

    @Test
    void acceptsConfiguredIdentifierAndQualifiedColumnNames() {
        CommonParam param = new CommonParam();

        param.setSortColumn(" documentNm ");
        assertEquals("documentNm", param.getSortColumn());

        param.setSortColumn("info.INSERT_DT");
        assertEquals("info.INSERT_DT", param.getSortColumn());
    }

    @Test
    void rejectsSqlExpressionsAndCommentSyntax() {
        CommonParam param = new CommonParam();

        assertThrows(IllegalArgumentException.class,
            () -> param.setSortColumn("documentNm desc"));
        assertThrows(IllegalArgumentException.class,
            () -> param.setSortColumn("documentNm,requestNo"));
        assertThrows(IllegalArgumentException.class,
            () -> param.setSortColumn("CASE WHEN 1=1 THEN documentNm END"));
        assertThrows(IllegalArgumentException.class,
            () -> param.setSortColumn("info.table.column"));
        assertThrows(IllegalArgumentException.class,
            () -> param.setSortColumn("sleep(1)"));
    }

    @Test
    void normalizesOrderAndRejectsAnythingElse() {
        CommonParam param = new CommonParam();

        param.setOrder(" desc ");
        assertEquals("DESC", param.getOrder());

        param.setOrder(null);
        assertNull(param.getOrder());
        assertThrows(IllegalArgumentException.class,
            () -> param.setOrder("DESC NULLS LAST"));
    }

    @Test
    void ignoresClientSuppliedSessionUserAndDoesNotSerializeIt() throws Exception {
        UserVO authenticatedUser = new UserVO();
        authenticatedUser.setUserCd("AUTHENTICATED");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(authenticatedUser, null));

        try {
            ObjectMapper mapper = new ObjectMapper();
            CommonParam param = mapper.readValue(
                "{\"sessionUser\":{\"userCd\":\"ATTACKER\"}}", CommonParam.class);

            assertSame(authenticatedUser, param.getSessionUser());
            assertFalse(mapper.writeValueAsString(param).contains("sessionUser"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
