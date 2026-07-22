package kr.esob.fdms.commonlogic.distributionDept;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
public class DistributionDeptVO {
    private String drawingNo;
    private String distributionPoint;
    private Timestamp inputDate;
    private int idx;
//    private String distributionType;
    private List<String> distributionType;
    private List<String> tableName;
}
