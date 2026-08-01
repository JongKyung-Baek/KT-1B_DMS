package kr.esob.tdms.commonlogic.mail;

import java.util.List;

import kr.esob.tdms.commonlogic.distribution.model.RequestListVO;
import kr.esob.tdms.commonlogic.value.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocsMailEnum {

	DISTRIBUTION_APPROVAL(
			"[KT-1B 기술자료관리시스템] 기술자료 배포 요청",
			"기술자료 배포 요청 알림이 도착했습니다.<br/>요청 내용을 확인해 주세요.<br/>",
			Constant.DISTRIBUTION_APPROVAL_URL),
	DISTRIBUTION_PRODUCT_APPROVAL(
			"[KT-1B 기술자료관리시스템] 생산기술자료 배포 요청",
			"생산기술자료 배포 요청 알림이 도착했습니다.<br/>요청 내용을 확인해 주세요.<br/>",
			Constant.DISTRIBUTION_ACCEPTANCE_URL),
	DISTRIBUTION_PRINT_APPROVAL(
			"[KT-1B 기술자료관리시스템] 출력 요청",
			"출력 요청 알림이 도착했습니다.<br/>요청 내용을 확인해 주세요.<br/>",
			Constant.DISTRIBUTION_APPROVAL_URL),
	DISTRIBUTION_DRAWING_STATUS(
			"[KT-1B 기술자료관리시스템] 도면 처리 알림",
			"도면 배포 요청이 처리되었습니다.<br/>요청 상태를 확인해 주세요.<br/>",
			"/general/distribution/requeststatus/"),
	DISTRIBUTION_DOC_STATUS(
			"[KT-1B 기술자료관리시스템] 문서 처리 알림",
			"문서 배포 요청이 처리되었습니다.<br/>요청 상태를 확인해 주세요.<br/>",
			"/general/distribution/requeststatus/"),
	DISTRIBUTION_SW_STATUS(
			"[KT-1B 기술자료관리시스템] 소프트웨어 자료 처리 알림",
			"소프트웨어 자료 배포 요청이 처리되었습니다.<br/>요청 상태를 확인해 주세요.<br/>",
			"/general/distribution/requeststatus/"),
	DISTRIBUTION_PRODUCT_STATUS(
			"[KT-1B 기술자료관리시스템] 생산기술자료 처리 알림",
			"생산기술자료 배포 요청이 처리되었습니다.<br/>요청 상태를 확인해 주세요.<br/>",
			"/general/distribution/requeststatus/"),
	DISTRIBUTION_PRINT_HISTORY(
			"[KT-1B 기술자료관리시스템] 출력 요청 처리 알림",
			"출력 요청이 처리되었습니다.<br/>출력 이력을 확인해 주세요.<br/>",
			Constant.DISTRIBUTION_PRINT_HISTORY_URL),
	DISTRIBUTION_DELETE_COMPANY(
			"[KT-1B 기술자료관리시스템] 자료 이용 중단 알림",
			"이용이 중단된 자료의 폐기 확인이 필요합니다.<br/>배포 이력을 확인해 주세요.<br/>",
			"/general/distribution/history/"),
	CR_APPROVAL(
			"[KT-1B 기술자료관리시스템] CR 승인 요청",
			"CR 승인 요청 알림이 도착했습니다.<br/>승인 요청을 확인해 주세요.<br/>",
			Constant.CR_APPROVAL_URL),
	CR_STATUS(
			"[KT-1B 기술자료관리시스템] CR 요청 처리 알림",
			"CR 요청이 처리되었습니다.<br/>CR 이력을 확인해 주세요.<br/>",
			"/general/cr/history/"),
	PRODUCT_APPROVAL(
			"[KT-1B 기술자료관리시스템] 생산기술자료 배포 요청",
			"생산기술자료 배포 요청이 접수되었습니다.<br/>승인 요청을 확인해 주세요.<br/>",
			Constant.PRODUCT_APPROVAL_URL),
	PRODUCT_STATUS(
			"[KT-1B 기술자료관리시스템] 생산기술자료 요청 처리 알림",
			"생산기술자료 배포 요청이 처리되었습니다.<br/>요청 상태를 확인해 주세요.<br/>",
			Constant.PRODUCT_STATUS_URL),
	PRODUCT_ACCEPT(
			"[KT-1B 기술자료관리시스템] 생산기술자료 배포 처리 알림",
			"생산기술자료 배포 요청이 처리되었습니다.<br/>접수 내용을 확인해 주세요.<br/>",
			Constant.PRODUCT_ACCEPT_URL),
	PRODUCT_DISPOSAL_APPROVAL(
			"[KT-1B 기술자료관리시스템] 생산기술자료 폐기 요청",
			"생산기술자료 폐기 요청 알림이 도착했습니다.<br/>승인 요청을 확인해 주세요.<br/>",
			Constant.PRODUCT_DISPOSAL_APPROVAL_URL),
	PRODUCT_PRINT_APPROVAL(
			"[KT-1B 기술자료관리시스템] 생산기술자료 출력 승인 요청",
			"생산기술자료 출력 승인 요청 알림이 도착했습니다.<br/>승인 요청을 확인해 주세요.<br/>",
			Constant.PRODUCT_PRINT_APPROVAL_URL),
	PRODUCT_PRINT_STATUS(
			"[KT-1B 기술자료관리시스템] 생산기술자료 출력 처리 알림",
			"생산기술자료 출력 요청이 처리되었습니다.<br/>요청 상태를 확인해 주세요.<br/>",
			Constant.PRODUCT_PRINT_STATUS_URL),
	REVISION_UPDATED(
			"[KT-1B 기술자료관리시스템] 도면 리비전 업데이트",
			"기존 도면의 리비전이 업데이트되었습니다.<br/>변경된 도면을 확인해 주세요.<br/>",
			"/general/distribution/drawingRequest/");

	private final String title;
	private final String content;
	private final String url;

	private static final String HTML_TEMPLATE =
			"<!DOCTYPE html>"
					+ "<html lang=\"ko\">"
					+ "<head><meta charset=\"UTF-8\"><title>%s</title></head>"
					+ "<body style=\"font-family:'맑은 고딕',Arial,sans-serif;margin:0;padding:0;background:#f7f7f7;color:#333;\">"
					+ "<div style=\"max-width:600px;margin:auto;background:#fff;padding:20px;border:1px solid #ddd;border-radius:8px;\">"
					+ "<h1 style=\"font-size:24px;color:#0051a2;margin-bottom:20px;\">%s</h1>"
					+ "<p>%s</p>"
					+ "<a href=\"%s\" style=\"display:inline-block;padding:12px 25px;margin-top:20px;"
					+ "background-color:#0051a2;color:#ffffff;text-decoration:none;font-weight:bold;border-radius:5px;"
					+ "font-size:16px;\">확인하기</a>"
					+ "<p style=\"color:#888;font-size:12px;margin-top:10px;\">※ 링크는 PC에서 접속해 주세요.</p>"
					+ "<p>감사합니다.</p><div style=\"font-size:12px;color:#888;margin-top:20px;text-align:center;\">"
					+ "KT-1B DMS</div>"
					+ "</div></body></html>";

	public String getFormattedContent() {
		return String.format(HTML_TEMPLATE, title, title, content, url);
	}

	public String getFormattedContentWithDrawingDetails(List<RequestListVO> requestList) {
		StringBuilder drawingDetails = new StringBuilder();
		for (int i = 0; i < requestList.size(); i++) {
			RequestListVO request = requestList.get(i);
			drawingDetails.append("<p>")
					.append(i + 1).append(". 도번: ").append(request.getDrawingNo())
					.append(", 도면명: ").append(request.getDrawingNm())
					.append(", 변경 리비전: ").append(request.getRevNo())
					.append("</p>");
		}

		return String.format(
				HTML_TEMPLATE,
				title,
				title,
				content + drawingDetails,
				url);
	}
}
