package kr.esob.tdms.commonlogic.viewerintegration;

import java.util.LinkedHashMap;
import java.util.Map;

public class ViewerDocumentMetadata {
    private String correlationId;
    private String objectType;
    private String objectId;
    private String aclObjectType;
    private String aclObjectId;
    private String fileNo;
    private String fileName;
    private String userCd;
    private String userId;
    private String userName;
    private String authority;
    private String revision;
    private String requestNo;
    private String distributionType;
    private String drawingNo;

    Map<String, Object> toSignedMap() {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("correlationId", correlationId);
        values.put("objectType", objectType);
        values.put("objectId", objectId);
        values.put("aclObjectType", aclObjectType);
        values.put("aclObjectId", aclObjectId);
        values.put("fileNo", fileNo);
        values.put("fileName", fileName);
        values.put("userId", userId);
        values.put("userName", userName);
        values.put("authority", authority);
        values.put("revision", revision);
        values.put("requestNo", requestNo);
        values.put("distributionType", distributionType);
        values.put("drawingNo", drawingNo);
        return values;
    }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getObjectId() { return objectId; }
    public void setObjectId(String objectId) { this.objectId = objectId; }
    public String getAclObjectType() { return aclObjectType; }
    public void setAclObjectType(String aclObjectType) { this.aclObjectType = aclObjectType; }
    public String getAclObjectId() { return aclObjectId; }
    public void setAclObjectId(String aclObjectId) { this.aclObjectId = aclObjectId; }
    public String getFileNo() { return fileNo; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getUserCd() { return userCd; }
    public void setUserCd(String userCd) { this.userCd = userCd; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAuthority() { return authority; }
    public void setAuthority(String authority) { this.authority = authority; }
    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getDistributionType() { return distributionType; }
    public void setDistributionType(String distributionType) { this.distributionType = distributionType; }
    public String getDrawingNo() { return drawingNo; }
    public void setDrawingNo(String drawingNo) { this.drawingNo = drawingNo; }
}
