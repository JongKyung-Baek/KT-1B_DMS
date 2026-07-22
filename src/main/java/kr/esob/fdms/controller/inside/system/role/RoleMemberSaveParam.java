package kr.esob.fdms.controller.inside.system.role;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 그룹에 할당된 부서 및 사용자 저장을 위한 Class
 * @author younjh
 *
 */
@Getter
@Setter
public class RoleMemberSaveParam {
	private String groupCd;
	private List<String> assignedUser;
	private List<String> assignedDept;
}
