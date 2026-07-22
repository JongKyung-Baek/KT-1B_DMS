package kr.esob.fdms.commonlogic.filecache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class FileCacheDao extends AbstractDao{
    private static final String PREFIX = "sql.FileCache.";

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectLatestOnetimeOfferFile(String dataOfferDocSeq, String filePurpose){
        Map<String, Object> p = new HashMap<>();
        p.put("dataOfferDocSeq", dataOfferDocSeq);
        p.put("filePurpose", filePurpose);
        return (Map<String, Object>) obj(PREFIX + "selectLatestOnetimeOfferFile", p);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectLatestDeliveryConfirmFile(String delvyCnfirmDocSeq, String filePurpose){
        Map<String, Object> p = new HashMap<>();
        p.put("delvyCnfirmDocSeq", delvyCnfirmDocSeq);
        p.put("filePurpose", filePurpose);
        return (Map<String, Object>) obj(PREFIX + "selectLatestDeliveryConfirmFile", p);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectOnetimeOfferFileByFileSeq(String fileSeq){
        return (Map<String, Object>) obj(PREFIX + "selectOnetimeOfferFileByFileSeq", fileSeq);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> selectDeliveryConfirmFileByFileSeq(String fileSeq){
        return (Map<String, Object>) obj(PREFIX + "selectDeliveryConfirmFileByFileSeq", fileSeq);
    }

    public void updateOnetimeOfferViewerPaths(Map<String, Object> param){update(PREFIX + "updateOnetimeOfferViewerPaths", param);}
    public void updateDeliveryConfirmViewerPaths(Map<String, Object> param){update(PREFIX + "updateDeliveryConfirmViewerPaths", param);}
}
