package kr.esob.tdms.commonlogic.toolbar;



import java.util.List;

import kr.esob.tdms.controller.login.RoleVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolbarParamVO {
    private String toolbarId;
    private String sessionLang;
    List<String> roleCdList;

}
