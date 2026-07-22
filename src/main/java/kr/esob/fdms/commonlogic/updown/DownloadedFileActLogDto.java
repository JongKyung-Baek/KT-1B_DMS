package kr.esob.fdms.commonlogic.updown;

public class DownloadedFileActLogDto {
    private int act_type;
    private String act_time;
    private String path;
    private String uuid;
    private String object_id;
    private String policy;

    public int getAct_type() {
        return act_type;
    }

    public void setAct_type(int act_type) {
        this.act_type = act_type;
    }

    public String getAct_time() {
        return act_time;
    }

    public void setAct_time(String act_time) {
        this.act_time = act_time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
