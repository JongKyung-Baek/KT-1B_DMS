package kr.esob.tdms.controller.general.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class DistributionWorkflowTechnicalListContractTest {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path LIST_SCRIPT = ROOT.resolve(
        "src/main/resources/static/js/views/general/distribution/swRequestList.js");
    private static final Path LIST_VIEW = ROOT.resolve(
        "src/main/webapp/WEB-INF/views/general/distribution/swRequestList.jsp");

    @Test
    void distributionActionOpensTheNewWorkflowWithSelectedFileIdentifiers() throws Exception {
        String script = read(LIST_SCRIPT);

        assertTrue(script.contains("function requestDistribute()"));
        assertTrue(script.contains("/general/distribution/workflow/requests/new?"));
        assertTrue(script.contains("objectType="));
        assertTrue(script.contains("objectId="));
        assertTrue(script.contains("fileNo="));
        assertTrue(script.contains("selectedRows.length > 200"));
        assertTrue(script.contains("ensureSwRequestWorkflowToolbar"));
        assertTrue(script.contains("swDistributionRequestButton"));
        assertFalse(script.contains("requestInsideUser('DISTRIBUTION'"));
    }

    @Test
    void technicalListExposesLocalizedValidationMessages() throws Exception {
        String view = read(LIST_VIEW);

        assertTrue(view.contains("feature.distributionWorkflow.validation.noSelection"));
        assertTrue(view.contains("feature.distributionWorkflow.validation.itemIdentifierMissing"));
        assertTrue(view.contains("feature.distributionWorkflow.validation.maxItems"));
        assertTrue(view.contains("feature.distributionWorkflow.action.createFromSelection"));
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
