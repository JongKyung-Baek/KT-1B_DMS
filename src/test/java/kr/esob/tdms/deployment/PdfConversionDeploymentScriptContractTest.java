package kr.esob.tdms.deployment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class PdfConversionDeploymentScriptContractTest {

    private static final Path ROOT = Path.of("deployment", "windows-demo",
            "runtime", "pdf-conversion");

    private static String read(String file) throws IOException {
        return Files.readString(ROOT.resolve(file), StandardCharsets.UTF_8);
    }

    private static String readProject(String file) throws IOException {
        return Files.readString(Path.of(file), StandardCharsets.UTF_8);
    }

    @Test
    void onePinnedServerRunnerOwnsPreflightApplyAndRollback()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        assertTrue(runner.contains("ValidateSet('Preflight', 'Apply', 'Rollback')"));
        assertTrue(runner.contains("$PSCommandPath"));
        assertTrue(runner.contains("ExpectedRunnerSha256"));
        assertTrue(runner.contains("ExpectedCommonSha256"));
        assertTrue(runner.contains("ExpectedRequestSha256"));
        assertTrue(runner.contains("Deployment $Name artifact hash mismatch"));
        assertTrue(runner.contains("Invoke-PreflightMode"));
        assertTrue(runner.contains("Invoke-ApplyMode"));
        assertTrue(runner.contains("Invoke-RollbackMode"));
        assertFalse(Files.exists(ROOT.resolve("Start-PdfConversionJointRelease.ps1")));
        assertFalse(Files.exists(ROOT.resolve("Rollback-PdfConversionRelease.ps1")));
    }

    @Test
    void stageAndLiveRuntimeStayUnderTheTdmsRoot() throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String request = read("deployment-request.example.json");
        String readme = read("README.md");
        String overlay = readProject(
                "deployment/windows-demo/runtime/compose.pdf-converter.yaml");

        assertTrue(runner.contains("Join-Path $script:Root 'staging'"));
        assertTrue(runner.contains("Join-Path $script:Runtime 'secrets'"));
        assertTrue(runner.contains("'pdf-conversion.env'"));
        assertTrue(runner.contains("'compose.pdf-conversion.yaml'"));
        assertTrue(runner.contains("A .partial upload cannot be executed"));
        assertTrue(runner.contains("Release staging root cannot be a reparse point"));
        assertTrue(runner.contains("Lock-ImmutableReleaseFiles"));
        assertTrue(runner.contains("[IO.FileShare]::Read"));
        assertTrue(runner.contains("Release staging contains undeclared or missing files"));
        assertTrue(request.contains("\"protocolVersion\": 2"));
        assertTrue(request.contains("\"exampleOnly\": true"));
        assertTrue(readme.contains("<releaseId>.partial"));
        assertTrue(readme.contains("powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File"));
        assertFalse(readme.contains("EncodedCommand"));
        assertFalse((runner + request + overlay).toLowerCase()
                .contains("d:\\e_pdf_conv"));
        assertFalse((runner + request + overlay).toLowerCase()
                .contains("d:/e_pdf_conv"));
    }

    @Test
    void publicProbeCaIsOptionalAndPinnedWhenDeclared()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");
        String request = read("deployment-request.example.json");
        String readme = read("README.md");

        assertFalse(request.contains("\n    \"publicProbeCa\":"));
        assertTrue(request.contains("Omit artifacts.publicProbeCa"));
        assertTrue(readme.contains("exactly the existing nine immutable files"));
        assertTrue(runner.contains("-ccontains 'publicProbeCa'"));
        assertTrue(runner.contains("Get-Artifact -Name 'publicProbeCa'"));
        assertTrue(runner.contains("Assert-PublicProbeCaCertificate"));
        assertTrue(common.contains("X509Certificate2"));
        assertTrue(common.contains("2.5.29.19"));
        assertTrue(common.contains("CertificateAuthority"));
        assertTrue(common.contains("$arguments.Add('--cacert')"));
        assertTrue(runner.contains("$script:Artifacts['publicProbeCa']"));
        assertTrue(runner.contains("CURL_CA_BUNDLE"));
        assertTrue(runner.contains("SSL_CERT_FILE"));
        assertTrue(runner.contains("$script:CurlExecutable"));
    }

    @Test
    void publicProbeCaTraversalExtraFilesAndHashMismatchAreRejected()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");

        assertTrue(runner.contains(
                "^[A-Za-z0-9][A-Za-z0-9._-]*$"));
        assertTrue(runner.contains("Assert-DeploymentChildPath"));
        assertTrue(runner.contains(
                "Release artifacts violate the exact allowlist"));
        assertTrue(runner.contains("$declaredArtifactNames.Count -in 6, 7"));
        assertTrue(runner.contains(
                "Release artifact hash mismatch: $([IO.Path]::GetFileName($full))"));
        assertTrue(runner.contains(
                "Release staging contains undeclared or missing files"));
        assertTrue(runner.contains(
                "Release artifact declaration is invalid: $name"));
    }

    @Test
    void bootstrapFilesAreLockedBeforeImportAndRequestParsing()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        int bootstrapLock = runner.indexOf(
                "Lock-BootstrapPinnedReleaseFile -Name $artifact.Name");
        int commonImport = runner.indexOf("Import-Module ([string]$lockedCommon.Path)");
        int lockedRequestRead = runner.indexOf(
                "Read-BootstrapLockedUtf8Text -Name 'request'");
        int requestParse = runner.indexOf("$requestText | ConvertFrom-Json");

        assertTrue(bootstrapLock >= 0 && bootstrapLock < commonImport);
        assertTrue(commonImport < lockedRequestRead && lockedRequestRead < requestParse);
        assertTrue(runner.contains("[IO.FileShare]::Read"));
        assertTrue(runner.contains("Get-BootstrapLockedSha256 -Stream"));
        assertTrue(runner.contains("Assert-BootstrapReleaseAcl"));
        assertTrue(runner.contains("RequireProtected"));
        assertTrue(runner.contains("Locked deployment request changed before parsing"));
        assertTrue(runner.contains("Locked deployment request changed during parsing"));
        assertFalse(runner.contains(
                "Get-Content -LiteralPath $script:RequestPath -Raw"));
    }

    @Test
    void completeReleaseAncestryIsPinnedAndPowerShellMustBe64Bit()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String readme = read("README.md");
        String helper = read("Test-PdfConversionDeployment.Common.ps1");

        int bitnessGate = runner.indexOf("[Environment]::Is64BitProcess");
        int rootSelection = runner.indexOf("$script:Root = if ($Rehearsal)");
        int ancestryDefinition = runner.indexOf(
                "function Assert-BootstrapReleaseAncestry");
        int bootstrapLock = runner.indexOf(
                "Lock-BootstrapPinnedReleaseFile -Name $artifact.Name");
        int firstAncestry = runner.lastIndexOf(
                "Assert-BootstrapReleaseAncestry", bootstrapLock);
        int lockedAncestry = runner.indexOf(
                "Assert-BootstrapReleaseAncestry", bootstrapLock);
        int commonImport = runner.indexOf(
                "Import-Module ([string]$lockedCommon.Path)");
        int importedAncestry = runner.indexOf(
                "Assert-BootstrapReleaseAncestry", commonImport);

        assertTrue(bitnessGate >= 0 && bitnessGate < rootSelection);
        assertTrue(firstAncestry > ancestryDefinition && firstAncestry < bootstrapLock);
        assertTrue(lockedAncestry > bootstrapLock && lockedAncestry < commonImport);
        assertTrue(importedAncestry > commonImport);
        assertTrue(runner.contains("@{ Name = 'root'; Path = $script:Root }"));
        assertTrue(runner.contains(
                "@{ Name = 'staging'; Path = $script:StageParent }"));
        assertTrue(runner.contains(
                "@{ Name = 'release'; Path = $script:StageRoot }"));
        assertTrue(runner.contains("-RequireProtected -PassThru"));
        assertTrue(runner.contains("Immutable release ancestry changed after bootstrap"));
        assertTrue(runner.contains("DeleteSubdirectoriesAndFiles"));
        assertTrue(runner.contains("ChangePermissions"));
        assertTrue(runner.contains("TakeOwnership"));
        assertTrue(readme.contains(
                "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe"));
        assertTrue(readme.contains("Protect the complete ancestry"));
        assertTrue(readme.contains("one-time ACL hardening"));
        assertTrue(readme.contains("never changes these ACLs"));
        assertTrue(readme.contains("Modify grant is forbidden"));
        assertTrue(helper.contains(
                "locked common module blocks release-directory pathname swap"));
        assertTrue(helper.contains(
                "locked common module blocks staging-ancestor pathname swap"));
        assertTrue(helper.contains(
                "locked common module blocks root-ancestor pathname swap"));
        assertTrue(helper.contains(
                "Import-Module consumes the pinned common-module pathname"));
        assertTrue(helper.contains(
                "32-bit Windows PowerShell is rejected before release path access"));
    }

    @Test
    void recursiveStageInventoryRejectsEveryDirectoryAndUnknownFile()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String inventory = runner.substring(
                runner.indexOf("function Lock-ImmutableReleaseFiles"),
                runner.indexOf("function Read-ReleaseContract"));

        assertTrue(inventory.contains("-Recurse -Force"));
        assertTrue(inventory.contains("Release staging cannot contain subdirectories"));
        assertTrue(inventory.contains("$inventoryPaths"));
        assertTrue(inventory.contains("$lockedPaths"));
        assertTrue(inventory.contains("Get-BootstrapLockedSha256 -Stream"));
    }

    @Test
    void curlAndPemAdversarialInputsCannotChangeTheProbeTrustBoundary()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");
        String helper = read("Test-PdfConversionDeployment.Common.ps1");

        assertTrue(common.contains("@('--disable', '--silent'"));
        assertTrue(common.contains("'--noproxy', '*'"));
        assertTrue(runner.contains("'CURL_HOME'"));
        assertTrue(runner.contains("'HOME', 'HTTPS_PROXY', 'ALL_PROXY'"));
        assertTrue(runner.contains("'HTTP_PROXY', 'NO_PROXY'"));
        assertTrue(helper.contains("malicious curlrc and proxy environment"));
        assertTrue(helper.contains("--proxy \"http://127.0.0.1:1\""));
        assertTrue(common.contains("$certificate.Export("));
        assertTrue(common.contains("$raw.Length -eq $canonicalRaw.Length"));
        assertTrue(common.contains("Test-DeploymentByteSequenceEqual"));
        assertTrue(common.contains("-bxor"));
        assertTrue(helper.contains("concatenated DER objects"));
    }

    @Test
    void probeEnvironmentRemovalAndRestorationAreFailClosed()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");
        String helper = read("Test-PdfConversionDeployment.Common.ps1");

        assertTrue(runner.contains("Invoke-WithIsolatedDeploymentEnvironment"));
        assertFalse(runner.contains("$ambientEnvironment"));
        assertTrue(common.contains(
                "Remove-Item -LiteralPath \"Env:$key\" -ErrorAction Stop"));
        assertTrue(common.contains(
                "Deployment environment key remains present"));
        assertTrue(common.contains(
                "Deployment environment restoration failed for $key"));
        assertTrue(common.contains("foreach ($key in $Keys)"));
        assertTrue(common.contains("[AggregateException]::new("));
        assertTrue(common.contains("throw $primaryException"));
        assertTrue(helper.contains(
                "probe isolation restores present keys and preserves absent keys"));
        assertTrue(helper.contains(
                "probe isolation preserves the original operation failure"));
    }

    @Test
    void missingSidecarsAndNormalizedPostgresCatalogAreHandled()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");

        assertTrue(runner.contains("Stop-ContainerIfPresent"));
        assertTrue(runner.contains("docker ps --all"));
        assertFalse(runner.contains("Required container is missing: $script:FileApiContainer"));
        assertTrue(common.contains("Test-PgRestoreTableDataEntry"));
        assertTrue(common.contains("TABLE DATA\\s+"));
        assertFalse(common.contains("\\\\bTABLE DATA"));
        assertTrue(runner.contains("Test-PgRestoreTableDataEntry -CatalogText"));
    }

    @Test
    void dockerBuildContextUsesTheStrictCopyAllowList()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");

        assertTrue(common.contains("Assert-DockerfileCopyAllowlist"));
        assertTrue(common.contains("Dockerfile ADD is not permitted"));
        assertTrue(common.contains("$instructions.Count -le 2"));
        assertTrue(common.contains("$warCount -eq 1"));
        assertTrue(runner.contains("New-RestrictedBuildContext"));
        assertTrue(runner.contains("$ignore.Add('*')"));
        assertTrue(runner.indexOf("$context = New-RestrictedBuildContext")
                < runner.indexOf("$script:OutageStarted = $true"));
    }

    @Test
    void allPreparationCompletesBeforeTheBoundedOutage()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        int prepare = runner.indexOf("$script:State = New-PreOutageState");
        int outage = runner.indexOf("$script:OutageStarted = $true");
        int stopGateway = runner.indexOf("Stop existing gateway", outage);

        assertTrue(prepare >= 0 && prepare < outage);
        assertTrue(outage < stopGateway);
        assertTrue(runner.contains("AddSeconds(180)"));
        assertTrue(runner.contains("outageTimeoutSeconds -eq 180"));
        assertTrue(runner.contains("The 180-second outage budget expired"));
        assertTrue(runner.contains("Apply is not replayable"));
        assertTrue(runner.contains("[IO.FileShare]::None"));
    }

    @Test
    void outageDeadlineHasAnApplyReserveAndAnExternalHardStop()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String rollback = runner.substring(
                runner.indexOf("function Invoke-RuntimeRollback"),
                runner.indexOf("function Assert-AndRestoreDamagedData"));

        assertTrue(runner.contains("AddSeconds(90)"));
        assertTrue(runner.contains("Start-OutageSupervisor"));
        assertTrue(runner.contains("Stop-ProcessTreePreservingCurrent"));
        assertTrue(runner.contains("RecoveryDeadlineUtc"));
        assertTrue(rollback.contains("-Deadline $Deadline"));
        assertFalse(rollback.contains("AddSeconds(120)"));
        assertFalse(rollback.contains("AddSeconds(60)"));
    }

    @Test
    void allWritersStopBeforeTheAuthoritativeFingerprintGate()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String apply = runner.substring(runner.indexOf("function Invoke-ApplyMode"),
                runner.indexOf("function Invoke-RollbackMode"));
        int gatewayStop = apply.indexOf("Stop existing gateway");
        int appStop = apply.indexOf(
                "Stop-ContainerIfPresent -Name $script:AppContainer");
        int stableGate = apply.indexOf("$quiescedDatabase =");
        int firstMutation = apply.indexOf(
                "$script:RuntimeMutationStarted = $true");

        assertTrue(gatewayStop >= 0 && gatewayStop < appStop);
        assertTrue(appStop < stableGate && stableGate < firstMutation);
        assertFalse(apply.contains("$quiescedDatabaseFull"));
        assertTrue(apply.contains("$quiescedDatabaseSchema"));
        assertTrue(apply.contains("$quiescedStorage"));
        assertTrue(apply.contains("$quiescedStorageMetadata"));
        assertTrue(apply.contains("$script:QuiesceEvidencePath"));
        assertTrue(apply.contains("Write-DeploymentJsonAtomically"));
        assertTrue(apply.contains("Resume-OriginalBeforeMutation"));
    }

    @Test
    void catalogStructureFingerprintAvoidsRestoreNormalizationNoise()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String normalizedRunner = runner.replace("\r\n", "\n");
        String prepare = runner.substring(
                runner.indexOf("function New-DatabaseBackupAndTestMigrations"),
                runner.indexOf("function New-RestrictedBuildContext"));
        String preOutage = runner.substring(
                runner.indexOf("function New-PreOutageState"),
                runner.indexOf("function Restore-RuntimeFileSet"));

        assertTrue(runner.contains("function Get-ArchiveSchemaFingerprint"));
        assertTrue(runner.contains("WITH relation_objects AS"));
        assertTrue(runner.contains("pg_catalog.pg_attribute"));
        assertTrue(runner.contains("pg_catalog.pg_constraint"));
        assertTrue(runner.contains("pg_catalog.pg_index"));
        assertTrue(runner.contains("pg_catalog.pg_sequence"));
        assertTrue(runner.contains("pg_catalog.aclexplode"));
        assertTrue(runner.contains("ORDER BY kind COLLATE \"C\""));
        assertTrue(runner.contains("docker exec -i $script:DbContainer psql"));
        Pattern varcharArrayRelabel = Pattern.compile(
                "\\((ARRAY\\[[^,\\]]+::character varying"
                        + "(, [^,\\]]+::character varying)*\\])\\)::text\\[\\]");
        assertTrue(varcharArrayRelabel.matcher(
                "(ARRAY['A'::character varying, 'B'::character varying])::text[]")
                .matches());
        assertFalse(varcharArrayRelabel.matcher("(ARRAY[1, 2])::text[]")
                .matches());
        assertFalse(runner.contains("E'\\\\((ARRAY\\\\[[^]]+\\\\])"));
        assertTrue(normalizedRunner.contains(
                "pg_restore --schema-only `\n"
                        + "                --no-owner --no-privileges --file=-"));
        assertTrue(prepare.contains("$archiveSchemaFingerprint"));
        assertTrue(prepare.contains("$restoredSchemaFingerprint"));
        assertTrue(prepare.contains(
                "OriginalSchemaFingerprint = $restoredSchemaFingerprint"));
        assertTrue(prepare.contains(
                "ArchiveSchemaFingerprint = $archiveSchemaFingerprint"));
        assertTrue(prepare.contains(
                "pg_restore --exit-on-error `\n"
                        + "                    --no-owner -U $admin"));
        assertFalse(prepare.contains(
                "--no-owner --no-privileges -U $admin -d $tempDb"));
        String explicitDataRestore = runner.substring(
                runner.indexOf("function Assert-AndRestoreDamagedData"),
                runner.indexOf("function Read-ApprovedState"));
        assertFalse(explicitDataRestore.contains(
                "--no-owner --no-privileges"));
        assertTrue(runner.contains(
                "restoredDatabaseFullFingerprint = "
                        + "[string]$databaseBackup.OriginalFullDataFingerprint"));
        assertTrue(runner.contains(
                "archiveDatabaseSchemaFingerprint = "
                        + "[string]$databaseBackup.ArchiveSchemaFingerprint"));
        assertTrue(runner.contains(
                "restoredDatabaseSchemaFingerprint = "
                        + "[string]$databaseBackup.RestoredSchemaFingerprint"));
        assertTrue(preOutage.contains(
                "$databaseFingerprint -ceq $databaseBackup.OriginalDataFingerprint"));
        assertFalse(preOutage.contains("$databaseFullFingerprint -ceq"));
        String runtimeRollback = runner.substring(
                runner.indexOf("function Invoke-RuntimeRollback"),
                runner.indexOf("function Assert-AndRestoreDamagedData"));
        assertFalse(runtimeRollback.contains("Get-FullDatabaseFingerprint"));
        String explicitRestore = normalizedRunner.substring(
                normalizedRunner.indexOf("function Assert-AndRestoreDamagedData"),
                normalizedRunner.indexOf("function Read-ApprovedState"));
        assertTrue(explicitRestore.contains(
                "[string]$State.restoredDatabaseSchemaFingerprint"));
        assertFalse(explicitRestore.contains(
                "[string]$State.originalDatabaseSchemaFingerprint) `\n"
                        + "                -Message 'Explicit database restore schema"));
    }

    @Test
    void everyRuntimeRollbackPhasePreservesTheDatabaseContainer()
            throws IOException {
        String common = read("PdfConversionDeployment.Common.psm1");
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String runtimeOnly = common.substring(
                common.indexOf("function Invoke-PdfRuntimeOnlyRollback"),
                common.indexOf("Export-ModuleMember"));

        assertTrue(runtimeOnly.contains("AssertDatabaseInvariant"));
        assertTrue(runtimeOnly.contains("$name 'before'"));
        assertTrue(runtimeOnly.contains("$name 'after'"));
        assertTrue(runner.contains("Assert-DatabaseContainerInvariant"));
        assertTrue(runner.contains("Database container identity changed"));
        assertTrue(runner.contains("Database container is not healthy"));
    }

    @Test
    void jointRuntimeHasExactImagesNetworkNamespaceAndNoPrivatePorts()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");
        String overlay = readProject(
                "deployment/windows-demo/runtime/compose.pdf-converter.yaml");

        assertTrue(overlay.contains("image: ${KT1B_APP_IMAGE:?"));
        assertTrue(runner.contains("'app', 'file-api', 'pdf-converter'"));
        assertTrue(runner.contains("Container runs an unexpected image"));
        assertTrue(common.contains("Assert-SharedNetworkRuntimeContract"));
        assertTrue(common.contains("'container:' + [string]$app.Id"));
        assertTrue(common.contains("@(9001, 18080)"));
        assertTrue(runner.contains("SandboxKey"));
        assertTrue(runner.contains("18080/api/v1/health"));
        assertTrue(runner.contains("Status = '200'"));
        assertTrue(runner.contains("Status = '405'"));
    }

    @Test
    void overlayWindowsPathGuardAllowsUrlsAndContainerVolumesOnly()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String overlay = readProject(
                "deployment/windows-demo/runtime/compose.pdf-converter.yaml");
        Pattern windowsHostPath = Pattern.compile(
                "(?im)(?:^|[\\s\"'])[A-Z]:[\\\\/]");

        assertTrue(runner.contains(
                "(?im)(?:^|[\\s\"''])[A-Z]:[\\\\/]"));
        assertFalse(windowsHostPath.matcher(overlay).find());
        assertTrue(windowsHostPath.matcher(
                "    - \"D:\\\\data:/data/kt1b/files\"").find());
    }

    @Test
    void databaseFingerprintStreamsSqlSoMixedCaseIdentifiersSurviveWindows()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String fingerprint = runner.substring(
                runner.indexOf("function Get-DatabaseFingerprint"),
                runner.indexOf("function Get-FullDatabaseFingerprint"));

        assertTrue(fingerprint.contains(
                "$sql | & docker exec -i $script:DbContainer psql"));
        assertTrue(fingerprint.contains("public.`\"$table`\""));
        assertFalse(fingerprint.contains("-d $Database -c $sql"));
        assertTrue(fingerprint.contains("CV_VIEW_MARKUP"));
    }

    @Test
    void gatewayIsOnlyRestartedAsTheExistingContainer()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");

        assertTrue(runner.contains("& docker start $script:GatewayContainer"));
        assertTrue(runner.contains("gatewayContainerId"));
        assertTrue(runner.contains("Gateway container was replaced"));
        assertFalse(runner.matches(
                "(?is).*Invoke-(?:Base)?Compose\\s+-Arguments\\s+@\\([^)]*['\"]gateway['\"].*"));
        assertFalse(runner.contains("docker compose down"));
        assertTrue(runner.split("--volumes", -1).length - 1 == 2);
    }

    @Test
    void automaticRollbackIsRuntimeOnlyAndDataRecoveryIsExplicit()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");
        int automaticCatch = runner.indexOf("AUTOMATIC_ROLLBACK=RUNTIME_ONLY_SUCCESS");
        int explicitRestore = runner.indexOf("function Assert-AndRestoreDamagedData");

        assertTrue(common.contains("Invoke-PdfRuntimeOnlyRollback"));
        assertTrue(common.contains("Database and storage restoration are intentionally absent"));
        assertTrue(common.contains("RestoreOriginalSidecars"));
        String runtimeOnly = common.substring(
                common.indexOf("function Invoke-PdfRuntimeOnlyRollback"),
                common.indexOf("Export-ModuleMember"));
        assertFalse(runtimeOnly.toLowerCase().contains("dropdb"));
        assertFalse(runtimeOnly.toLowerCase().contains("pg_restore"));
        assertTrue(runner.substring(Math.max(0, automaticCatch - 500),
                Math.min(runner.length(), automaticCatch + 200))
                .contains("Invoke-RuntimeRollback"));
        assertTrue(explicitRestore >= 0);
        assertTrue(runner.substring(explicitRestore).contains("$RestoreData.IsPresent"));
        assertTrue(runner.substring(explicitRestore).contains("ExpectedEvidenceSha256"));
        assertTrue(runner.substring(explicitRestore).contains("No actual fingerprint damage exists"));
        assertTrue(runner.substring(explicitRestore).contains("expectedDatabaseFingerprint"));
        assertTrue(runner.substring(explicitRestore).contains(
                "expectedDatabaseFullFingerprint"));
        assertTrue(runner.substring(explicitRestore).contains(
                "expectedDatabaseSchemaFingerprint"));
        assertTrue(runner.substring(explicitRestore).contains("expectedStorageFingerprint"));
    }

    @Test
    void powershell51SafeFilesAndAtomicSecretFreeStateAreUsed()
            throws IOException {
        String runner = read("Invoke-PdfConversionRelease.ps1");
        String common = read("PdfConversionDeployment.Common.psm1");

        assertFalse((runner + common).contains("[IO.File]::Replace"));
        assertFalse(common.matches("(?m)^\\s*[^#].*Resolve-Path.*$"));
        assertFalse(runner.matches("(?m)^\\s*[^#].*Resolve-Path.*$"));
        assertTrue(common.contains("Move-Item -LiteralPath $targetFull"));
        assertTrue(common.contains("Write-DeploymentJsonAtomically"));
        assertTrue(runner.contains("Protect-BackupDirectory"));
        assertTrue(runner.contains("Assert-ProtectedDeploymentSecretFile"));
        assertTrue(runner.contains("'KT1B_FILE_API_KEY'"));
        assertFalse(runner.contains("docker compose config"));
    }

    @Test
    void ps51RehearsalModelsForcedPostAppFailureOnDDrive()
            throws IOException {
        String rehearsal = read(
                "Test-PdfConversionRuntimeRollback.Rehearsal.ps1");

        assertTrue(rehearsal.contains("D:\\KT1B-DMS"));
        assertTrue(rehearsal.contains("FORCED_POST_APP_FAILURE"));
        assertTrue(rehearsal.contains("Invoke-PdfRuntimeOnlyRollback"));
        assertTrue(rehearsal.contains("old app identity is restored"));
        assertTrue(rehearsal.contains("same gateway identity is restarted"));
        assertTrue(rehearsal.contains("database is unchanged by automatic rollback"));
        assertTrue(rehearsal.contains("storage is unchanged by automatic rollback"));
    }

    @Test
    void runLogsRemainIgnoredAndLegacyAdHocRunnersAreNotContracts()
            throws IOException {
        String ignore = readProject(".gitignore");

        assertTrue(ignore.lines().anyMatch(line -> line.equals("run-logs/")));
        assertFalse(ignore.contains("!run-logs/tdms-pdf-conversion"));
    }
}
