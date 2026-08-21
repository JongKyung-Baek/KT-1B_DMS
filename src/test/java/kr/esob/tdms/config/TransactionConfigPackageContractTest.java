package kr.esob.tdms.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.esob.tdms.commonlogic.viewerintegration.ViewerCallbackAuthenticationFilter;
import kr.esob.tdms.commonlogic.viewerintegration.ViewerIntegrationService;
import kr.esob.tdms.commonlogic.branding.TdmsBrandFilter;
import kr.esob.tdms.commonlogic.pdfconversion.PdfConversionDao;
import kr.esob.tdms.commonlogic.pdfconversion.PdfConversionWorker;
import kr.esob.tdms.commonlogic.security.MobileClientAccessFilter;
import org.junit.jupiter.api.Test;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

class TransactionConfigPackageContractTest {

    @Test
    void transactionAdviceTargetsTheTdmsPackageRoot() throws Exception {
        String source = Files.readString(Path.of(
                "src", "main", "java", "kr", "esob", "tdms",
                "config", "TransactionConfig.java"), StandardCharsets.UTF_8);

        assertTrue(source.contains(
                "execution(* kr.esob.tdms..*.*(..))"));
        assertTrue(source.contains(
                "!within(kr.esob.tdms.commonlogic.audit.RequestAuditFilter)"));
        assertTrue(source.contains(
                "!within(kr.esob.tdms.commonlogic.branding..*)"));
        assertTrue(source.contains(
                "!within(kr.esob.tdms.commonlogic.security."
                        + "MobileClientAccessFilter)"));
        assertTrue(source.contains(
                "!within(kr.esob.tdms.commonlogic.viewerintegration."
                        + "ViewerCallbackAuthenticationFilter)"));
        assertTrue(source.contains(
                "!within(kr.esob.tdms.commonlogic.pdfconversion."
                        + "PdfConversionWorker)"));
        assertTrue(source.contains("UnexpectedRollbackException"));
        assertFalse(source.contains("kr.esob.docs"));
    }

    @Test
    void callbackFilterIsNotABusinessTransactionBoundary() throws Exception {
        TransactionConfig config = new TransactionConfig();
        ReflectionTestUtils.setField(
                config,
                "transactionManager",
                mock(PlatformTransactionManager.class));
        PointcutAdvisor advisor = (PointcutAdvisor) config.txAdviceAdvisor();

        assertFalse(advisor.getPointcut().getMethodMatcher().matches(
                TdmsBrandFilter.class.getDeclaredMethod(
                        "doFilterInternal",
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        FilterChain.class),
                TdmsBrandFilter.class));
        assertFalse(advisor.getPointcut().getMethodMatcher().matches(
                MobileClientAccessFilter.class.getDeclaredMethod(
                        "doFilterInternal",
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        FilterChain.class),
                MobileClientAccessFilter.class));
        assertFalse(advisor.getPointcut().getMethodMatcher().matches(
                ViewerCallbackAuthenticationFilter.class.getDeclaredMethod(
                        "doFilterInternal",
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        FilterChain.class),
                ViewerCallbackAuthenticationFilter.class));
        assertTrue(advisor.getPointcut().getMethodMatcher().matches(
                ViewerIntegrationService.class.getMethod(
                        "authenticateCallbackRequest",
                        byte[].class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class),
                ViewerIntegrationService.class));
    }

    @Test
    void pdfWorkerOrchestrationIsOutsideTransactionButClaimDaoStillCommits()
            throws Exception {
        TransactionConfig config = new TransactionConfig();
        ReflectionTestUtils.setField(
                config,
                "transactionManager",
                mock(PlatformTransactionManager.class));
        PointcutAdvisor advisor = (PointcutAdvisor) config.txAdviceAdvisor();

        assertFalse(advisor.getPointcut().getMethodMatcher().matches(
                PdfConversionWorker.class.getMethod("poll"),
                PdfConversionWorker.class));
        assertTrue(advisor.getPointcut().getMethodMatcher().matches(
                PdfConversionDao.class.getMethod("claim", java.util.Map.class),
                PdfConversionDao.class));
        assertTrue(advisor.getPointcut().getMethodMatcher().matches(
                PdfConversionDao.class.getMethod("failExpiredExhausted"),
                PdfConversionDao.class));
    }
}
