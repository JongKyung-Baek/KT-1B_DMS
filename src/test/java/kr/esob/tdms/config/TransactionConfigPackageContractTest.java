package kr.esob.tdms.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

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
        assertFalse(source.contains("kr.esob.docs"));
    }
}
