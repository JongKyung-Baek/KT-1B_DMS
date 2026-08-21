package kr.esob.tdms.commonlogic.pdfconversion;

import java.time.OffsetDateTime;

/** Durable source-to-PDF conversion state owned by TDMS. */
public class PdfConversionJob {
    private String conversionId;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String sourceFileName;
    private String sourceFilePath;
    private Long sourceSizeBytes;
    private String sourceSha256;
    private String outputFileName;
    private String outputFilePath;
    private Long outputSizeBytes;
    private String outputSha256;
    private String status;
    private int attemptCount;
    private int maxAttempts;
    private OffsetDateTime nextAttemptAt;
    private String claimToken;
    private OffsetDateTime claimedAt;
    private OffsetDateTime claimExpiresAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private boolean current;
    private String lastError;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public String getConversionId() { return conversionId; }
    public void setConversionId(String conversionId) { this.conversionId = conversionId; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getFileNo() { return fileNo; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public String getSourceFileName() { return sourceFileName; }
    public void setSourceFileName(String sourceFileName) { this.sourceFileName = sourceFileName; }
    public String getSourceFilePath() { return sourceFilePath; }
    public void setSourceFilePath(String sourceFilePath) { this.sourceFilePath = sourceFilePath; }
    public Long getSourceSizeBytes() { return sourceSizeBytes; }
    public void setSourceSizeBytes(Long sourceSizeBytes) { this.sourceSizeBytes = sourceSizeBytes; }
    public String getSourceSha256() { return sourceSha256; }
    public void setSourceSha256(String sourceSha256) { this.sourceSha256 = sourceSha256; }
    public String getOutputFileName() { return outputFileName; }
    public void setOutputFileName(String outputFileName) { this.outputFileName = outputFileName; }
    public String getOutputFilePath() { return outputFilePath; }
    public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }
    public Long getOutputSizeBytes() { return outputSizeBytes; }
    public void setOutputSizeBytes(Long outputSizeBytes) { this.outputSizeBytes = outputSizeBytes; }
    public String getOutputSha256() { return outputSha256; }
    public void setOutputSha256(String outputSha256) { this.outputSha256 = outputSha256; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public OffsetDateTime getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(OffsetDateTime nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public String getClaimToken() { return claimToken; }
    public void setClaimToken(String claimToken) { this.claimToken = claimToken; }
    public OffsetDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(OffsetDateTime claimedAt) { this.claimedAt = claimedAt; }
    public OffsetDateTime getClaimExpiresAt() { return claimExpiresAt; }
    public void setClaimExpiresAt(OffsetDateTime claimExpiresAt) { this.claimExpiresAt = claimExpiresAt; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
