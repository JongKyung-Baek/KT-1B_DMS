package kr.esob.fdms.commonlogic.value;

public class Constant {
	public  static final String loginParamDelimeter = ":---:";
	/** 날짜 표시 default 구분자 */
	public static final String DATE_DELIMITER = "-";
	public static final String RESULT_SUCCESS = "success";
	public static final String RESULT_FAIL	  = "fail";



	public static final String SYSTEM_CONFIG = "SYSTEM_CONFIG";

	public static final String REQUEST = "REQUEST";
	public static final String ACCEPT = "ACCEPT";
	public static final String APPROVAL = "APPROVAL";
	public static final String REJECT = "REJECT";
	public static final String WAITING = "WAITING";

	public static final String DEVELOPMENT = "Development";
	public static final String PRODUCTION = "Production";

	public static final String DEFAULT_TEXT_NOTHING = "nothing";

	public static final String REQUEST_TYPE_DISTRIBUTION = "DISTRIBUTION";
	public static final String REQUEST_TYPE_PRODUCT = "PRODUCT";
	public static final String REQUEST_TYPE_PRINT = "PRINT";						// 출력
	public static final String REQUEST_TYPE_PRODUCT_PRINT = "PRODUCT_PRINT";	// 생산기술 출력
	public static final String REQUEST_TYPE_NOREG = "NO-REG";				// 미등록자료


	public static final String OBJECT_TYPE_DRAWING = "DRAWING";
	public static final String OBJECT_TYPE_DOC = "DOC";
	public static final String OBJECT_TYPE_SW = "SW";

	public static final String BUSINESS_AREA_CD_1 = "1210";		// 1사업장
	public static final String BUSINESS_AREA_CD_2 = "1310";		// 2사업장

	public static final String LOGIN_TYPE_LOGIN = "I";
	public static final String LOGIN_TYPE_LOGOUT = "O";

	public static final String SEED_KEY = "fdmsesob#soft#key";
	public static final String SEED_ENCODING = "UTF-8";


	public static final String DISTRIBUTION_APPROVAL_URL = "/inside/distribution/approval/";
	public static final String DISTRIBUTION_ACCEPTANCE_URL = "/inside/distribution/acceptance/";
	public static final String DISTRIBUTION_DISPOSAL_ACCEPTANCE_URL = "/inside/distribution/disposalacceptance/";
	public static final String DISTRIBUTION_DRAWING_STATUS_URL = "/outside/drawing/approvalStatus/";
	public static final String DISTRIBUTION_DRAWING_REQUEST_URL = "/outside/drawing/request/";
	public static final String DISTRIBUTION_DOC_STATUS_URL = "/outside/doc/approvalStatus/";
	public static final String DISTRIBUTION_SW_STATUS_URL = "/outside/sw/approvalStatus/";
	public static final String DISTRIBUTION_PRODUCT_STATUS_URL = "/outside/product/approvalStatus/";
	public static final String DISTRIBUTION_PRINT_APPROVAL_URL = "/inside/distribution/printApproval/";
	public static final String DISTRIBUTION_PRINT_HISTORY_URL = "/inside/distribution/printHistory/";
	public static final String DISTRIBUTION_DELETE_COMPANY_URL = "/main/";
	public static final String DISTRIBUTION_VALID_TERM_OVER_URL = "/main/";
	public static final String CR_APPROVAL_URL = "/inside/cr/approval/";
	public static final String CR_STATUS_URL = "/outside/cr/request/";
	public static final String UNREG_APPROVAL_URL = "/inside/unregisted/approval/";
	public static final String UNREG_STATUS_URL = "/outside/unregisted/status/";
	public static final String PRODUCT_APPROVAL_URL = "/inside/production/approval/";
	public static final String PRODUCT_STATUS_URL = "/inside/production/requeststatus/";
	public static final String PRODUCT_ACCEPT_URL = "/inside/production/acceptance/";
	public static final String PRODUCT_DISPOSAL_APPROVAL_URL = "/inside/production/disposalApproval/";
	public static final String PRODUCT_PRINT_APPROVAL_URL = "/inside/production/approval/";
	public static final String PRODUCT_PRINT_STATUS_URL = "/inside/production/requeststatus/";
	public static final String USER_APPROVAL_URL = "/inside/organizationmanage/approval/";
	public static final String DESTROY_APPROVAL_URL = "/inside/organizationmanage/approval/";
	public static final String DUANZONG_DOCS_URL = "/outside/duanzong/fdms/";

	public static final String GROUP_CD_OUTSIDE = "RG_006";
	public static final String GROUP_CD_ADMIN = "RG_001";
	public static final String ROOT_MENU_CD = "MENU_000";

}
