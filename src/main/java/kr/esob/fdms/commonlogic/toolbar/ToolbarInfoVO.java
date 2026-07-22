package kr.esob.fdms.commonlogic.toolbar;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolbarInfoVO {
    private String toolbarId;
    private String buttonId;
    private Integer buttonSort;
    private String buttonAlign;
    private String buttonLabel;
    private String buttonImg;
    private String callFunc;
    private String buttonType;
    private String systemClassGroup;

}
