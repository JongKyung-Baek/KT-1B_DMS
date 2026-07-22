package kr.esob.fdms.controller.login;

import kr.esob.fdms.config.Property;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of= {"userCd"})
public class UserVO implements UserDetails{
	private static final long serialVersionUID = 3163522928774314714L;
	private String url_type;
	private String userCd;
	private String userId;
	private String userNm;
	private String userPwd;
	private String companyCd;
	private String deptCd;
	private String deptNm;
	private String positionCd;
	private String positionNm;
	private String email;
	private String authLevel;
	private String authApprovalYn;
	private int authApprovalLevel;
	private Date firstLoginDt;
	private Date lastLoginDt;
	private String lastLoginIp;
	private String userLang;
	private String sessionLang;
	private String useYn;
	private String delYn;
	private String insertUid;
	private Date insertDt;
	private String updateUid;
	private Date updateDt;
	private Date pwdUpdate;
	private Date pwdUpdateDt;
	private String authSite;
	private int loginCount;
	private String roleGroup;
	private String companyNm;
	private String bizNo;
	private String businessAreaCd;
	private String workPlaceCd;
	private String passUseYn;		/* 방산기술 결재권한 있는 사람 중에 '개발'은 다른 사람에게 이관이 가능해야 함. */
	private String approvalYn;		/* 외부 사용자 중에 부서 결재 권한(CR)이 있는지 여부 (부서의 팀장) */
	private String lockYn;
	private String companyDelYn;	/* 업체 사용여부 */
	private String deptShortPath;
	//팀장
	private String teamLeaderUid;
	private String approverYn;			// 결재가능여부

	private String purchaseYn;			// 구매가능여부
	private String purchaseTeamYn;		// 구매팀여부
	private String purchasePlanTeamYn;	// 구매기획팀여부
	private String ilsTeamYn;			// ILS팀여부
	private String productTeamYn;		// 생산기술팀여부
	private String deptType;			// 부서유형

	private String protectYn;			// 방산기술자료 권한여부 ( 외부사용자 전용 )

	private String companyApprovalUser; // 업체 CR결재자
	private String oneOffMainUrl;
	private String pwdExpiredYn;		//패스워드 유효기간 여부

	private List<RoleVO> authorities;

	public String getCrPurchaser(String businessAreaCd) {
		if("1210".equals(businessAreaCd)) {
			return "";
		}else if("1310".equals(businessAreaCd)) {
			return "";
		}else {
			return "";
		}
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}
	@Override
	public String getPassword() {
		return getUserPwd();
	}
	@Override
	public String getUsername() {
		return this.userNm;
	}
	@Override
	public boolean isAccountNonExpired() {
		if(this.getLastLoginDt() == null)setLastLoginDt(new Date());
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.getLastLoginDt());
		cal.add(Calendar.DATE, 30);
		if(-1 == cal.getTime().compareTo(new Date())) {
			return false;
		}else {
			return true;
		}
	}

	public boolean isAccountNonExpired(UserVO userVo) {
		if(this.getLastLoginDt() == null)setLastLoginDt(new Date());
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.getLastLoginDt());

		Property property = new Property();

		if("I".equals(userVo.getAuthSite())) {
			cal.add(Calendar.DATE, Integer.parseInt(property.getProperty("last.loginDt.term.inside")));
		}
		else {
			cal.add(Calendar.DATE, Integer.parseInt(property.getProperty("last.loginDt.term.outside")));
		}

		if(-1 == cal.getTime().compareTo(new Date())) {
			return false;
		}else {
			return true;
		}
	}

	@Override
	public boolean isAccountNonLocked() {
		if("Y".equals(this.lockYn)) {
			return false;
		}else {
			return true;
		}
	}
	@Override
	public boolean isCredentialsNonExpired() {
		if("I".equals(this.authSite)) {
			this.pwdExpiredYn = "N";
			return true;
		}else {
			if(this.getPwdUpdateDt() == null)setPwdUpdateDt(new Date());
			Calendar cal = Calendar.getInstance();
			cal.setTime(this.getPwdUpdateDt());
			cal.add(Calendar.DATE, 90);
			if(-1 == cal.getTime().compareTo(new Date())) {
				this.pwdExpiredYn = "Y";
				return false;
			}else {
				this.pwdExpiredYn = "N";
				return true;
			}
		}
	}
	@Override
	public boolean isEnabled() {
		return "Y".equals(this.useYn);
	}

	@Override
	public String toString() {
		return "UserVO{" +
				"url_type='" + url_type + '\'' +
				", userCd='" + userCd + '\'' +
				", userId='" + userId + '\'' +
				", userNm='" + userNm + '\'' +
				", userPwd='" + userPwd + '\'' +
				", companyCd='" + companyCd + '\'' +
				", deptCd='" + deptCd + '\'' +
				", deptNm='" + deptNm + '\'' +
				", positionCd='" + positionCd + '\'' +
				", email='" + email + '\'' +
				", authLevel='" + authLevel + '\'' +
				", authApprovalYn='" + authApprovalYn + '\'' +
				", firstLoginDt=" + firstLoginDt +
				", lastLoginDt=" + lastLoginDt +
				", lastLoginIp='" + lastLoginIp + '\'' +
				", userLang='" + userLang + '\'' +
				", useYn='" + useYn + '\'' +
				", delYn='" + delYn + '\'' +
				", insertUid='" + insertUid + '\'' +
				", insertDt=" + insertDt +
				", updateUid='" + updateUid + '\'' +
				", updateDt=" + updateDt +
				", pwdUpdate=" + pwdUpdate +
				", pwdUpdateDt=" + pwdUpdateDt +
				", authSite='" + authSite + '\'' +
				", loginCount=" + loginCount +
				", roleGroup='" + roleGroup + '\'' +
				", companyNm='" + companyNm + '\'' +
				", bizNo='" + bizNo + '\'' +
				", businessAreaCd='" + businessAreaCd + '\'' +
				", workPlaceCd='" + workPlaceCd + '\'' +
				", passUseYn='" + passUseYn + '\'' +
				", approvalYn='" + approvalYn + '\'' +
				", lockYn='" + lockYn + '\'' +
				", companyDelYn='" + companyDelYn + '\'' +
				", deptShortPath='" + deptShortPath + '\'' +
				", teamLeaderUid='" + teamLeaderUid + '\'' +
				", approverYn='" + approverYn + '\'' +
				", purchaseYn='" + purchaseYn + '\'' +
				", purchaseTeamYn='" + purchaseTeamYn + '\'' +
				", purchasePlanTeamYn='" + purchasePlanTeamYn + '\'' +
				", ilsTeamYn='" + ilsTeamYn + '\'' +
				", productTeamYn='" + productTeamYn + '\'' +
				", deptType='" + deptType + '\'' +
				", protectYn='" + protectYn + '\'' +
				", companyApprovalUser='" + companyApprovalUser + '\'' +
				", oneOffMainUrl='" + oneOffMainUrl + '\'' +
				", pwdExpiredYn='" + pwdExpiredYn + '\'' +
				", authorities=" + authorities +
				'}';
	}
}
