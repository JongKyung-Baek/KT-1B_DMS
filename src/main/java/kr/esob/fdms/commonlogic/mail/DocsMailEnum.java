package kr.esob.fdms.commonlogic.mail;

import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.outside.drawing.request.RequestListVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum DocsMailEnum {
//	DISTRIBUTION_APPROVAL("[DOCS 시스템] 외부업체 배포 요청"
//			,"외부배포 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_APPROVAL_URL
//			, "I"),
//	DISTRIBUTION_APPROVAL_OUTSIDE("[DOCS 시스템] 외부업체 배포 요청"
//			,"외부배포 요청 알림메일이 도착했습니다. 다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_ACCEPTANCE_URL
//			, "I"),
//	DISTRIBUTION_PRODUCT_APPROVAL_OUTSIDE("[DOCS 시스템] 생산기술자료 외부업체 배포 요청"
//			,"생산기술자료 외부배포 요청 알림메일이 도착했습니다. 다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_ACCEPTANCE_URL
//			, "I"),
//	DISTRIBUTION_DISPOSAL_OUTSIDE("[DOCS 시스템] 외부업체 폐기 요청"
//			,"외부업체 폐기요청 알림메일이 도착했습니다. 다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_DISPOSAL_ACCEPTANCE_URL
//			, "I"),
//	DISTRIBUTION_PRINT_APPROVAL("[DOCS 시스템] 출력 요청"
//			,"출력 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_APPROVAL_URL
//			, "I"),
//	DISTRIBUTION_DRAWING_STATUS("[DOCS 시스템] 도면 배포 통보"
//			,"도면 배포요청이 승인되었습니다.<br/>DOCS SYSTEM에 접속해서 확인바랍니다."
//			,""
//			,"E"),
//	DISTRIBUTION_DOC_STATUS("[DOCS 시스템] 문서 배포 통보"
//			,"문서 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,""
//			,"E"),
//	DISTRIBUTION_SW_STATUS("[DOCS 시스템] S/W 배포 통보"
//			,"S/W 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,""
//			,"E"),
//	DISTRIBUTION_PRODUCT_STATUS("[DOCS 시스템] CP 배포 통보"
//			,"CP 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,""
//			,"E"),
//	DISTRIBUTION_PRINT_HISTORY("[DOCS 시스템] 출력 승인"
//			,"출력 요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_PRINT_HISTORY_URL
//			, "I"),
//	DISTRIBUTION_DELETE_COMPANY("[DOCS 시스템] 거래중단 알림"
//			,"거래중단으로 모든 배포 자료 폐기가 필요합니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/><br/>"
//			,Constant.DISTRIBUTION_DELETE_COMPANY_URL
//			, "I"),
//	DISTRIBUTION_VALID_TERM_OVER("[DOCS 시스템] 유효기간 만료에 의한 폐기 알림"
//			,"배포된 자료의 유효기간이 만료되어 자료 폐기가 필요합니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DISTRIBUTION_VALID_TERM_OVER_URL
//			, "I"),
//	CR_APPROVAL("[DOCS 시스템] CR 승인 요청"
//			,"CR 승인 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.CR_APPROVAL_URL
//			, "I"),
//	CR_STATUS("[DOCS 시스템] CR 요청 반려"
//			,"CR 요청이 반려되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,""
//			,"E"),
//	UNREG_APPROVAL("[DOCS 시스템] 미등록자료 배포 요청"
//			,"미등록자료 승인 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.UNREG_APPROVAL_URL
//			,"I"),
//	UNREG_STATUS("[DOCS 시스템] 미등록자료 배포 통보"
//			,"미등록자료 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,""
//			,"E"),
//	OUTREG_STATUS("[DOCS 시스템] 협력사로 부터 자료전송 통보"
//			,"협력사로부터 자료가 전송 되었습니다."
//			,""
//			,"I"),
//	PRODUCT_APPROVAL("[DOCS 시스템] 생산기술자료 배포 요청"
//			,"생산기술자료 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.PRODUCT_APPROVAL_URL
//			,"I"),
//	PRODUCT_STATUS("[DOCS 시스템] 생산기술자료 요청 반려"
//			,"생산기술자료 배포요청이 반려되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.PRODUCT_STATUS_URL
//			,"I"),
//	PRODUCT_ACCEPT("[DOCS 시스템] 생산기술자료 배포 통보"
//			,"생산기술자료 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.PRODUCT_ACCEPT_URL
//			,"I"),
//	PRODUCT_DISPOSAL_APPROVAL("[DOCS 시스템] 생산기술자료 폐기 요청"
//			,"생산기술자료 폐기 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.PRODUCT_DISPOSAL_APPROVAL_URL
//			,"I"),
//	PRODUCT_PRINT_APPROVAL("[DOCS 시스템] 생산기술자료 출력 승인 요청"
//			,"생산기술자료 출력 승인 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.PRODUCT_PRINT_APPROVAL_URL
//			,"I"),
//	PRODUCT_PRINT_STATUS("[DOCS 시스템] 생산기술자료 출력 승인"
//			,"생산기술자료 출력 요청이 승인되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.PRODUCT_PRINT_STATUS_URL
//			,"I"),
//	DUANZONG_DOCS("[DOCS 시스템] 협력업체 단종정보관리 등록"
//			,"단종품이 등록되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
//			,Constant.DUANZONG_DOCS_URL
//			,"E"),
////	REVISION_UPDATED("[DOCS 시스템] 리비전 업데이트"
////			,"기존 도면의 리비전이 업데이트 되었습니다.<br/>다음 링크를 클릭하여 DOCS SYSTEM에 접속해 주세요.<br/>"
////			,Constant.DUANZONG_DOCS_URL
////			,"E"),
//	REVISION_UPDATED("[DOCS 시스템] 리비전 업데이트"
//			, "<!DOCTYPE html>"
//		+ "<html lang=\"ko\">"
//		+ "<head><meta charset=\"UTF-8\"><title>결재 승인 알림</title>"
//		+ "<style>body{font-family:'맑은 고딕',Arial,sans-serif;margin:0;padding:0;background:#f7f7f7;color:#333}"
//		+ ".container{max-width:600px;margin:auto;background:#fff;padding:20px;border:1px solid #ddd;border-radius:8px}"
//		+ "h1{font-size:24px;color:#007bff;margin-bottom:20px}"
//		+ "p{font-size:16px;line-height:1.6;margin:10px 0}"
//		+ ".button{padding:12px 20px;background:#007bff;color:#fff;text-decoration:none;border-radius:5px;display:inline-block}"
//		+ ".button:hover{background:#0056b3}.footer{font-size:12px;color:#888;margin-top:20px;text-align:center}"
//		+ "</style></head><body>"
//		+ "<div class=\"container\"><h1>결재 승인 요청 알림</h1>"
//		+ "<p>안녕하세요,</p><p>귀하의 결재 승인이 필요한 도면 배포 요청이 있습니다. "
//		+ "아래의 링크를 통해 결재를 진행해 주시기 바랍니다.</p>"
//		+ "<a href=\"http://localhost:3508/?url_type=E\" class=\"button\">결재 확인하기</a>"
//		+ "<p style=\"color:#888;font-size:12px;margin-top:10px;\">※ 링크는 PC에서 접속해 주세요.</p>"
//		+ "<p>감사합니다.</p><div class=\"footer\">© 2025 이솝소프트 주식회사. All rights reserved.</div>"
//		+ "</div></body></html>"
//			,Constant.DISTRIBUTION_DRAWING_REQUEST_URL
//			,"E"),
//	;
//	private String title;
//	private String content;
//	private String url;
//	private String urlType;

	// ENUM 값 정의
	DISTRIBUTION_APPROVAL("[도면배포관리시스템] 외부업체 배포 요청","외부배포 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>", Constant.DISTRIBUTION_APPROVAL_URL, "I"),
	DISTRIBUTION_APPROVAL_OUTSIDE("[도면배포관리시스템] 외부업체 배포 요청","외부배포 요청 알림메일이 도착했습니다. 다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DISTRIBUTION_ACCEPTANCE_URL, "I"),
	DISTRIBUTION_PRODUCT_APPROVAL_OUTSIDE("[도면배포관리시스템] 생산기술자료 외부업체 배포 요청","생산기술자료 외부배포 요청 알림메일이 도착했습니다. 다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DISTRIBUTION_ACCEPTANCE_URL, "I"),
	DISTRIBUTION_DISPOSAL_OUTSIDE("[도면배포관리시스템] 외부업체 폐기 요청","외부업체 폐기요청 알림메일이 도착했습니다. 다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DISTRIBUTION_DISPOSAL_ACCEPTANCE_URL, "I"),
	DISTRIBUTION_PRINT_APPROVAL("[도면배포관리시스템] 출력 요청","출력 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DISTRIBUTION_APPROVAL_URL, "I"),
	DISTRIBUTION_DRAWING_STATUS("[KARI 기술자료관리시스템] 문서 등록 알림","도면 배포요청이 승인되었습니다.<br/>도면 배포 관리 시스템에 접속해서 확인바랍니다.","","E"),
	DISTRIBUTION_DOC_STATUS("[KARI 기술자료관리시스템] 문서 등록 알림","문서 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>","","E"),
	DISTRIBUTION_SW_STATUS("[KARI 기술자료관리시스템] 문서 등록 알림","S/W 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>","","E"),
	DISTRIBUTION_PRODUCT_STATUS("[KARI 기술자료관리시스템] 문서 등록 알림","CP 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>","","E"),
	DISTRIBUTION_PRINT_HISTORY("[도면배포관리시스템] 출력 승인","출력 요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DISTRIBUTION_PRINT_HISTORY_URL, "I"),
	DISTRIBUTION_DELETE_COMPANY("[도면배포관리시스템] 거래중단 알림","거래중단으로 모든 배포 자료 폐기가 필요합니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/><br/>",Constant.DISTRIBUTION_DELETE_COMPANY_URL, "I"),
	DISTRIBUTION_VALID_TERM_OVER("[도면배포관리시스템] 유효기간 만료에 의한 폐기 알림","배포된 자료의 유효기간이 만료되어 자료 폐기가 필요합니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DISTRIBUTION_VALID_TERM_OVER_URL, "I"),
	CR_APPROVAL("[도면배포관리시스템] CR 승인 요청","CR 승인 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.CR_APPROVAL_URL, "I"),
	CR_STATUS("[도면배포관리시스템] CR 요청 반려","CR 요청이 반려되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>","","E"),
	UNREG_APPROVAL("[도면배포관리시스템] 미등록자료 배포 요청","미등록자료 승인 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.UNREG_APPROVAL_URL,"I"),
	UNREG_STATUS("[도면배포관리시스템] 미등록자료 배포 통보","미등록자료 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>","","E"),
	OUTREG_STATUS("[도면배포관리시스템] 협력사로 부터 자료전송 통보","협력사로부터 자료가 전송 되었습니다.","","I"),
	PRODUCT_APPROVAL("[도면배포관리시스템] 생산기술자료 배포 요청","생산기술자료 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.PRODUCT_APPROVAL_URL,"I"),
	PRODUCT_STATUS("[도면배포관리시스템] 생산기술자료 요청 반려","생산기술자료 배포요청이 반려되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.PRODUCT_STATUS_URL,"I"),
	PRODUCT_ACCEPT("[도면배포관리시스템] 생산기술자료 배포 통보","생산기술자료 배포요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.PRODUCT_ACCEPT_URL,"I"),
	PRODUCT_DISPOSAL_APPROVAL("[도면배포관리시스템] 생산기술자료 폐기 요청","생산기술자료 폐기 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.PRODUCT_DISPOSAL_APPROVAL_URL,"I"),
	PRODUCT_PRINT_APPROVAL("[도면배포관리시스템] 생산기술자료 출력 승인 요청","생산기술자료 출력 승인 요청 알림메일이 도착했습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.PRODUCT_PRINT_APPROVAL_URL,"I"),
	PRODUCT_PRINT_STATUS("[도면배포관리시스템] 생산기술자료 출력 승인","생산기술자료 출력 요청이 승인되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.PRODUCT_PRINT_STATUS_URL,"I"),
	DUANZONG_DOCS("[도면배포관리시스템] 협력업체 단종정보관리 등록","단종품이 등록되었습니다.<br/>다음 링크를 클릭하여 도면 배포 관리 시스템에 접속해 주세요.<br/>",Constant.DUANZONG_DOCS_URL,"E"),REVISION_UPDATED("[도면배포관리시스템] 리비전 업데이트", "기존 도면의 리비전이 업데이트 되었습니다. 아래의 링크를 통해 확인해 주세요.", Constant.DISTRIBUTION_DRAWING_REQUEST_URL, "E");

	private final String title;
	private final String content;
	private final String url;
	private final String urlType;

	private static final String HTML_TEMPLATE =
			"<!DOCTYPE html>"
					+ "<html lang=\"ko\">"
					+ "<head><meta charset=\"UTF-8\"><title>%s</title></head>"
					+ "<body style=\"font-family:'맑은 고딕',Arial,sans-serif;margin:0;padding:0;background:#f7f7f7;color:#333;\">"
					+ "<div style=\"max-width:600px;margin:auto;background:#fff;padding:20px;border:1px solid #ddd;border-radius:8px;\">"
					+ "<h1 style=\"font-size:24px;color:#0051a2;margin-bottom:20px;\">%s</h1>"
					+ "<p>%s</p>"
					+ "<a href=\"https://demo.esob.kr/?url_type=E\" style=\"display:inline-block;padding:12px 25px;margin-top:20px;"
					+ "background-color:#0051a2;color:#ffffff;text-decoration:none;font-weight:bold;border-radius:5px;"
					+ "font-size:16px;transition:background-color 0.3s ease;\">확인하기</a>"
					+ "<p style=\"color:#888;font-size:12px;margin-top:10px;\">※ 링크는 PC에서 접속해 주세요.</p>"
					+ "<p>감사합니다.</p><div style=\"font-size:12px;color:#888;margin-top:20px;text-align:center;\">"
					+ "© 2025 이솝소프트 주식회사. All rights reserved.</div>"
					+ "</div></body></html>";


	// 메일 내용을 HTML 템플릿으로 반환하는 메서드
	public String getFormattedContent() {
		return String.format(HTML_TEMPLATE, title, title, content, url);
	}

	// 동적으로 도면 정보를 추가하여 메일 내용을 반환하는 메서드
	public String getFormattedContentWithDrawingDetails(List<RequestListVO> requestList) {
		StringBuilder drawingDetails = new StringBuilder();
		for (int i = 0; i < requestList.size(); i++) {
			RequestListVO requestVO = requestList.get(i);
			drawingDetails.append("<p>")
					.append(i + 1).append(". 도번: ").append(requestVO.getDrawingNo())
					.append(", 도면명: ").append(requestVO.getDrawingNm())
					.append(", 변경된 리비전: ").append(requestVO.getRevNo())
					.append("</p><br/>");
		}

		// HTML 내용 조합
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<!DOCTYPE html>")
				.append("<html lang=\"ko\">")
				.append("<head><meta charset=\"UTF-8\"><title>").append(title).append("</title></head>")
				.append("<body style=\"font-family:'맑은 고딕',Arial,sans-serif;margin:0;padding:0;background:#f7f7f7;color:#333;\">")
				.append("<div style=\"max-width:600px;margin:auto;background:#fff;padding:20px;border:1px solid #ddd;border-radius:8px;\">")
				.append("<h1 style=\"font-size:24px;color:#0051a2;margin-bottom:20px;\">").append(title).append("</h1>")
				.append("<p>").append(content).append("</p><br/><br/>")
				.append(drawingDetails)
				.append("<a href=\"").append("https://demo.esob.kr/?url_type=E").append("\" style=\"display:inline-block;padding:12px 25px;margin-top:20px;")
				.append("background-color:#0051a2;color:#ffffff;text-decoration:none;font-weight:bold;border-radius:5px;")
				.append("font-size:16px;transition:background-color 0.3s ease;\">확인하기</a>")
				.append("<p style=\"color:#888;font-size:12px;margin-top:10px;\">※ 링크는 PC에서 접속해 주세요.</p>")
				.append("<p>감사합니다.</p><div style=\"font-size:12px;color:#888;margin-top:20px;text-align:center;\">")
				.append("© 2025 이솝소프트 주식회사. All rights reserved.</div>")
				.append("</div></body></html>");

		return htmlContent.toString();
	}

}
