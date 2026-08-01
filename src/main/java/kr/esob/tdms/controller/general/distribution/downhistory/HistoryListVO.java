package kr.esob.tdms.controller.general.distribution.downhistory;
import kr.esob.tdms.commonlogic.combo.ComboLang;
import kr.esob.tdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryListVO {

    private String requestNo;
    private String userNm;
    private String objectNo;
    private String objectNm;
    private String downDate;
    private String downCount;
    private String objectId;

    private String downloadedName;
    private String actType;
    private String actTime;
    private String uuid;

    private String actLog;



}
