package kr.esob.tdms.commonlogic.mail;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.seq.SeqDao;
import kr.esob.tdms.controller.general.cr.CrParam;
import kr.esob.tdms.controller.general.distribution.commonrequest.CommonApprovalParam;
import kr.esob.tdms.controller.login.LoginDao;
import kr.esob.tdms.controller.login.UserVO;
import kr.esob.tdms.commonlogic.distribution.model.RequestListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocsMailService {
	//false 메일 막기 메일 보내고싶으면 266줄 주석 풀기
	private static final boolean MAIL_SEND_ENABLED = false;
	/* private static final boolean MAIL_SEND_ENABLED = true; */
	private final String fromMail;
	private final String fromName;

	@Inject
	DocsMailDao dao;
	@Inject
	SeqDao seqDao;
	@Inject
	LoginDao loginDao;

	@Autowired
	public DocsMailService(MailConfig mailConfig) {
		this.fromMail = mailConfig.getFromMail().get("fromMail").toString();
		this.fromName = mailConfig.getFromMail().get("fromName").toString();
	}


	@Autowired
	private JavaMailSender mailSender;

	boolean isNewRevision = false;

	public void sendMail(MailInfoVO param) {
		if (!MAIL_SEND_ENABLED) {
			log.info("Mail sending is disabled.");
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			String result = "I";
			String detail = "";

			log.info("Method {}#sendMail is called", this.getClass().getSimpleName());

			helper.setFrom(fromMail, fromName);
			helper.setTo(param.getToMail());                			// 수신자
			String subject = (param.getTitle() != null && !param.getTitle().trim().isEmpty())
					? param.getTitle()
					: param.getMailEnum().getTitle();
			helper.setSubject(subject);          // 제목

//			if(param.getIsNewRevision().equals("true")){
//				helper.setText(param.getWholeContent(), true);
//			}
			String mailBody = (param.getContent() != null && !param.getContent().trim().isEmpty())
					? param.getContent()
					: param.getMailEnum().getFormattedContent();
			helper.setText(mailBody, true);    		// 본문


			mailSender.send(message);                        			// 실행
			//        MailServiceProxy mailServiceProxy = new MailServiceProxy();
			//        mailServiceProxy.setEndpoint(END_POINT_URL);
			result = "S";
			detail = null;

			//        log.debug("메일 발송 address : " + END_POINT_URL);

			//        List<MailInfoVO> list = dao.selectMail();

			//        //기본 메일 정보 처리
			//        WsMailInfo mailInfo = new WsMailInfo();
			//        mailInfo.setHtmlContent(false);      // 메일 본문 종류(true:HTML, false:Text)
			//        mailInfo.setMhtContent(false);       // MHT 본문 사용 여부(true:MHT)
			//        mailInfo.setImportant(false);        // 중요 메일 여부(true:중요, false:일반)

			//        for(MailInfoVO vo : list) {
			//        	mailInfo.setSubject(vo.getTitle());        // 메일 제목
			//            mailInfo.setSenderEmail(vo.getFromMail());   // 발신자 메일 주소
			//            String mailBody = vo.getContent();              // 메일 본문
			//
			//         // 메일 수신자 처리
			//            WsRecipient[] receivers = new WsRecipient[1];
			//        	receivers[0] = new WsRecipient();
			//        	receivers[0].setSeqID(1);                                       // 순번
			//        	receivers[0].setRecvType("TO");                                 // 수신자 형태(TO:수신, CC:참조, BCC:숨은참조)
			//        	receivers[0].setRecvEmail(vo.getToMail());     // 수신자 메일 주소
			//        	receivers[0].setDept(false);
			//
			//        	if(vo.getToCc() != null) {
			//    			WsRecipient[] ccReceivers = new WsRecipient[vo.getToCc().size()];
			//    			int j = 0;
			//    			for(String toCc : vo.getToCc()) {
			//    				ccReceivers[j] = new WsRecipient();
			//    				ccReceivers[j].setSeqID(j);                                       // 순번
			//    				ccReceivers[j].setRecvType("CC");                                 // 수신자 형태(TO:수신, CC:참조, BCC:숨은참조)
			//    				ccReceivers[j].setRecvEmail(toCc);     // 수신자 메일 주소
			//    				ccReceivers[j].setDept(false);
			//    				j++;
			//    			}
			//        	}
			//
			//        	// 첨부 파일 처리
			//        	mailInfo.setAttachCount(1);                                     // 첨부 파일 개수
			//            WsAttachFile[] attachFiles = new WsAttachFile[1];
			//
			//            log.debug("result fromMail ====================> " + vo.getFromMail());
			//            log.debug("result toMail ====================> " + vo.getToMail());
			//
			//            try {
			//                String resultMsg = mailServiceProxy.sendMISMail(mailBody, mailInfo, receivers, attachFiles);
			//                log.debug("resultMsg >>> " + resultMsg);
			//
			//            } catch (WsException e) {
			//                log.debug("FaultActor() >>> " + e.getExceptionActor());
			//                log.debug("ExceptionCode() >>> " + e.getExceptionCode());
			//                log.debug("ExceptionMessage() >>> " + e.getExceptionMessage());
			//                result = "E";
			//                detail = e.getExceptionMessage();
			//
			//            } catch (RemoteException re) {
			//                re.printStackTrace();
			//                result = "E";
			//                detail = re.getMessage();
			//            }
			//
			//            vo.setResult(result);
			//            vo.setDetail(detail);
			//
			//            dao.updateMail(vo);
			//        }
			param.setResult(result);
			param.setDetail(detail);

			dao.updateMail(param);
		} catch(Exception e){
			log.warn("Mail delivery failed. cause={}", e.getClass().getSimpleName());
		}
	}

	/**
	 * 기술자료 배포 요청을 접수할 때 메일 큐를 생성한다.
	 * @param param
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public ResultVO sendDocsMail(MailInfoVO param) {
		ResultVO resultVo = new ResultVO();
		if (!MAIL_SEND_ENABLED) {
			log.info("Mail sending is disabled.");
			resultVo.setSuccess(true);
			return resultVo;
		}
		UserVO userVo = null;

// SecurityContext에 Authentication 객체가 있는지 확인
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			// Authentication에 Principal이 있는지 확인
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof UserVO) {
				userVo = (UserVO) principal;
			}
		}

// userVo가 null이면 loginDao에서 사용자 정보 조회
		try {
			if (userVo == null) {
				userVo = loginDao.selectDeployUser(param.getToUserId());
				param.setMailEnum(DocsMailEnum.REVISION_UPDATED);
				isNewRevision = true;
			}
			StringBuffer contentbuffer = new StringBuffer();

			boolean hasCustomContent = param.getContent() != null && !param.getContent().trim().isEmpty();
			if (hasCustomContent) {
				contentbuffer.append(param.getContent());
			} else if(isNewRevision) {
				contentbuffer.append(param.getContent());
				param.setIsNewRevision("true");
			} else {
				contentbuffer.append(param.getMailEnum().getContent());
			}

			contentbuffer.append(param.getAppendContent());

			if (param.getTitle() == null || param.getTitle().trim().isEmpty()) {
				param.setTitle(param.getMailEnum().getTitle());
			}
			param.setContent(contentbuffer.toString());
			boolean insertResult = insertMail(param);
			/* if(insertResult) sendMail(param); */
			//잠깐 메일기능 잠금 주석
			resultVo.setSuccess(true);

		}catch(Exception e) {
			log.warn("Mail preparation failed. cause={}", e.getClass().getSimpleName());
			resultVo.setSuccess(false);
		}
		return resultVo;
	}

	public ResultVO sendScheduledMails() {
		log.info("deliverScheduledMails() is called");

		ResultVO resultVo = new ResultVO();
		if (!MAIL_SEND_ENABLED) {
			log.info("Mail sending is disabled. scheduled mails skipped.");
			resultVo.setSuccess(true);
			return resultVo;
		}
		List<MailInfoVO> scheduledMails = dao.selectMail();

		boolean hasFailure = false;

		for (MailInfoVO scheduledMail : scheduledMails) {
			try {
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

				helper.setFrom(fromMail, fromName);
				helper.setTo(scheduledMail.getToMail());
				helper.setSubject(scheduledMail.getTitle());

				if (scheduledMail.getMailEnum().equals(DocsMailEnum.REVISION_UPDATED)) {
					List<RequestListVO> revisionDatas = getRevisionData(scheduledMail);
					helper.setText(scheduledMail.getMailEnum().getFormattedContentWithDrawingDetails(revisionDatas), true);
				} else {
					helper.setText(scheduledMail.getMailEnum().getFormattedContent(), true);
				}

				mailSender.send(message);

				// 성공 업데이트
				scheduledMail.setResult("S");
				scheduledMail.setDetail(null);
				dao.updateMail(scheduledMail);

			} catch (Exception e) {
				hasFailure = true;

				log.warn("Scheduled mail delivery failed. cause={}", e.getClass().getSimpleName());

				// 실패 업데이트
				scheduledMail.setResult("F");
//				scheduledMail.setDetail(e.getMessage() + ": " + scheduledMail.getToMail());
				scheduledMail.setDetail(e.getMessage());
				dao.updateMail(scheduledMail);  // 실패 상태 저장
			}
		}

		resultVo.setSuccess(!hasFailure);  // 실패가 있으면 false
		return resultVo;
	}

	private List<RequestListVO> getRevisionData(MailInfoVO scheduledMail) {
		// scheduledMail.getReferenceKeys()의 값을 콤마로 구분된 문자열로 받아서 리스트로 변환
		String referenceKeys = scheduledMail.getReferenceKeys(); // "ex: 15, 16"
		List<String> mailIdsString = Arrays.asList(referenceKeys.split(",\\s*"));

		List<Integer> mailIds = mailIdsString.stream()
				.map(Integer::parseInt)
				.collect(Collectors.toList());

		// DAO 호출
		List<RequestListVO> revisionDataList = dao.selectRevisionData(mailIds);

		return revisionDataList;
	}

	public boolean insertDocsMail(MailInfoVO param) {
		if (!MAIL_SEND_ENABLED) {
			log.info("Mail sending is disabled. Mail queue insert skipped.");
			return true;
		}
		boolean insertResult = false;

		UserVO userVo = null;

// SecurityContext에 Authentication 객체가 있는지 확인
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			// Authentication에 Principal이 있는지 확인
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof UserVO) {
				userVo = (UserVO) principal;
			}
		}

// userVo가 null이면 loginDao에서 사용자 정보 조회
		try {
			if (userVo == null) {
				userVo = loginDao.selectDeployUser(param.getToUserId());
				param.setMailEnum(DocsMailEnum.REVISION_UPDATED);
				isNewRevision = true;
			}
			StringBuffer contentbuffer = new StringBuffer();

			boolean hasCustomContent = param.getContent() != null && !param.getContent().trim().isEmpty();
			if (hasCustomContent) {
				contentbuffer.append(param.getContent());
			} else if(isNewRevision) {
				contentbuffer.append(param.getContent());
				param.setIsNewRevision("true");
			} else {
				contentbuffer.append(param.getMailEnum().getContent());
			}

			contentbuffer.append(param.getAppendContent());

			if (param.getTitle() == null || param.getTitle().trim().isEmpty()) {
				param.setTitle(param.getMailEnum().getTitle());
			}
			param.setContent(contentbuffer.toString());
			insertResult = insertMail(param);


		}catch(Exception e) {
			log.warn("Mail preparation failed. cause={}", e.getClass().getSimpleName());
		}
		return insertResult;
	}

	public MailInfoVO selectReceiveUser(String param) {
		return dao.selectReceiveUser(param);
	}

	public MailInfoVO selectRequestUserInfo(Object param) {
		return dao.selectRequestUserInfo(param);
	}

	public MailInfoVO selectDefUserInfo(CommonApprovalParam param) {
		return dao.selectDefUserInfo(param);
	}

	public MailInfoVO selectDeployUserInfo(Object param) {
		return dao.selectDeployUserInfo(param);
	}

	public MailInfoVO selectApprovalUserInfo(CrParam param) {
		return dao.selectApprovalUserInfo(param);
	}

	public MailInfoVO selectCrRequestUserInfo(CrParam param) {
		return dao.selectCrRequestUserInfo(param);
	}

	public List<MailInfoVO> selectCompanyUserList(Object param){
		return dao.selectCompanyUserList(param);
	}

	public String selectPurchaserEmail(Object param) {
		return dao.selectPurchaserEmail(param);
	}

	public boolean insertMail(MailInfoVO param){
		log.info("DocsMailService.insertMail is called");
		int result = 0;
		Long seq = null;
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("tableNm", "DOCS_MAIL");

		if(isNewRevision){
			map.put("sessionYn", "N");
		} else {
			map.put("sessionYn", "Y");
		}

		seq = seqDao.getSeq(map);
		param.setProcessSeq(seq);
		param.setFromMail(fromMail);
		result = dao.insertMail(param);
		if(result<1){
			return false;
		}
		return true;
	}

}
