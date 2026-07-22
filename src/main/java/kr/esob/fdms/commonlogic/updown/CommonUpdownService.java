package kr.esob.fdms.commonlogic.updown;


import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
/* import kr.esob.fdms.commonlogic.distactlog.DistributionActLogDao;
import kr.esob.fdms.commonlogic.distactlog.DistributionActLogParam; */
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Slf4j
@Service
public class CommonUpdownService implements CommonService{

	@Inject
	CommonUpdownDao dao;

/* 	@Inject
	DistributionActLogDao distActLogDao; */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List selectList(Object obj) {
		List<CommonUpdownFileVO> rtnList = new ArrayList<CommonUpdownFileVO>();
		log.info(JSONObject.fromObject(obj).toString());

		CommonUpdownParam param = (CommonUpdownParam) obj;
		// 문서는 docSeq(DATA_OFFER_DOC_SEQ) 기준으로 정리한다.
		// 기존 dataNo 기준 필터는 주석으로 남긴다.
		// 다운로드 키(fileSeq/docSeq)가 비어있는 항목은 SQL = NULL 문제를 만들 수 있어 사전 제거
		if (param.getList() != null) {
			List<CommonUpdownParam> filtered = param.getList().stream()
					.filter(it -> it != null)
//					.filter(it -> !isBlank(it.getFileSeq()) || !isBlank(it.getDataNo()) || !isBlank(it.getDocSeq()))
					.filter(it -> !isBlank(it.getFileSeq()) || !isBlank(it.getDocSeq()))
					.collect(Collectors.toList());
			param.setList(filtered);
			if (filtered.isEmpty()) {
				log.warn("downloadData skipped: no valid fileSeq/docSeq in request list");
				return rtnList;
			}
		}

		List<CommonUpdownFileVO> tempList = dao.selectList(param);
//		File folder= new File(SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
		String srcUrl = SystemConfig.getSystemConfigValue("SERVER_URL_INSIDE");
		String dstUrl = SystemConfig.getSystemConfigValue("SERVER_URL_OUTSIDE");

		log.info("srcUrl : " + srcUrl);
		log.info("dstUrl : " + dstUrl);

		String userNm = param.getSessionUser().getUsername();
		log.info("userNm: >ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>ㅡ>" + userNm);
		String orgFilePath = null;
		String targetFilePath = null;
		String targetFileNm = null;
		String docSeq = null;
		String requestNo = null;

//		if(!folder.exists()) {
//			try {
//				folder.mkdir();
//			}
//			catch(Exception e) {
//				e.getStackTrace();
//			}
//		}
		for(CommonUpdownFileVO tempVO : tempList) {
			// REST API 기반 다운로드(DOC/DRAWING 공통): downloadData 단계에서는 파일 복사 없이 메타만 반환
			if ("DISTRIBUTION".equals(param.getReqType())
					&& ("DRAWING".equalsIgnoreCase(param.getObjectType())
					|| "DOC".equalsIgnoreCase(param.getObjectType())
					|| "SW".equalsIgnoreCase(param.getObjectType())
					|| "SECP".equalsIgnoreCase(param.getObjectType())
					|| "SECP_PARTDOC".equalsIgnoreCase(param.getObjectType()))) {
				rtnList.add(tempVO);
				continue;
			}

			//임시
//			tempVO.setFilePathNm(SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
//			rtnList.add(tempVO);

			//TODO : 추후에 COPY 방법 정한 뒤 알맞게 수정 부탁드립니다.
			if("DISTRIBUTION".equals(param.getReqType())) {
//				2023. 07. 12 천기범 ( 파일 다운로드를 위해 주석처리 )
				orgFilePath = /*SystemConfig.getSystemConfigValue("UPDOWN_ORG_FILE_PATH") + */  tempVO.getFilePathNm();
				targetFileNm = SystemConfig.getSystemConfigValue("UPDOWN_PATH") + tempVO.getOrgFileNm();
			}else if("UNREG".equals(param.getReqType())) {
				orgFilePath = tempVO.getFilePathNm();
				targetFileNm = tempVO.getFileNm();
				//tempVO.setFilePathNm(tempVO.getFilePathNm().replace(tempVO.getFileNm(), ""));
			}
			targetFilePath = SystemConfig.getSystemConfigValue("UPDOWN_PATH");

			docSeq = tempVO.getDocSeq();
			requestNo = tempVO.getRequestNo();


			log.info("requestNo: " + requestNo);
			log.info("docSeq: " + docSeq );
			log.info("orgFilePath: " + orgFilePath);
			log.info("targetFilePath: " + targetFilePath);
			log.info("targetFileNm: " + targetFileNm);

			try {
//				JSONObject result = FileUtil.sendFile(dstUrl, orgFilePath, targetFilePath);
				JSONObject result = FileUtil.callSender(
						Seed128Cipher.encrypt(srcUrl, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING),
						Seed128Cipher.encrypt(dstUrl, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING),
						Seed128Cipher.encrypt(orgFilePath, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING),
						Seed128Cipher.encrypt(targetFilePath, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING),
						Seed128Cipher.encrypt(targetFileNm, Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING)
						);

				log.info(result.toString());

				if(result.getBoolean("result")) {
					Integer downloadCount = dao.getDownloadCount(orgFilePath);

					// 다운로드 회수가 3미만이면 다운로드. ( 0>1 , 1>2 , 2>3. 3번 다운로드
					if (downloadCount <3 ){
						log.info("##############");
						log.info("다운로드 성공");
						tempVO.setFilePathNm(SystemConfig.getSystemConfigValue("UPDOWN_PATH"));
						tempVO.setFileNm(result.getString("fileNm"));
						rtnList.add(tempVO);
						dao.plusDownloadCount(orgFilePath); // 횟수 + 1하게 만드는 dao


						log.info(" JSONArray.fromObject(rtnList).toString() : " + JSONArray.fromObject(rtnList).toString());

						String jsonStringData = JSONArray.fromObject(rtnList).toString();
						ObjectMapper objectMapper = new ObjectMapper();
						List<Map<String, Object>> list = objectMapper.readValue(jsonStringData, new TypeReference<List<Map<String, Object>>>(){});
						String downloadedName = (String) list.get(0).get("fileNm");

						log.info("fileNm value: " + downloadedName);



						// requestNo, docSeq 이용해서 objectNo,objectNm 가져옴
						Map<String, Object> results = dao.getObjectNoObjectNm(requestNo,docSeq);
						String objectNo = (String)results.get("objectNo");
						String objectNm = (String)results.get("objectNm");

						// 다운로드 횟수 추가된거로 가져옴
						Integer downCount = dao.getDownloadCount(orgFilePath);


						java.util.Date downDate = new java.util.Date();
						dao.addToDownHistory(requestNo, docSeq, downCount, objectNo, objectNm, userNm, downDate, downloadedName);
						// KAI 문서행위로그-다운로드
						/* Map<String, Object> actBase = dao.selectActLogBase( requestNo, docSeq, orgFilePath, param.getSessionUser().getUserCd() );

						if (actBase != null) {
							DistributionActLogParam logParam = new DistributionActLogParam();
							logParam.setRequestNo((String) actBase.get("requestNo"));
							logParam.setRequestType((String) actBase.get("requestType"));
							logParam.setObjectId((String) actBase.get("objectId"));
							logParam.setFileNo((String) actBase.get("fileNo"));
							logParam.setObjectType((String) actBase.get("objectType"));
							logParam.setProtectYn((String) actBase.get("protectYn"));
							logParam.setActType("DOWNLOAD");
							logParam.setActResultCd("SUCCESS");
							logParam.setActResultMsg(null);
							logParam.setOrgFileNm((String) actBase.get("orgFileNm"));
							logParam.setActionUserCd((String) actBase.get("actionUserCd"));
							logParam.setActionUserNm((String) actBase.get("actionUserNm"));
							logParam.setCompanyCd((String) actBase.get("companyCd"));
							logParam.setCompanyNm((String) actBase.get("companyNm"));
							logParam.setProjectNm((String) actBase.get("projectNm"));
							logParam.setSourceType((String) actBase.get("sourceType"));
							logParam.setSourceKey((String) actBase.get("sourceKey"));
							logParam.setInsertUid(param.getSessionUser().getUserCd());

							distActLogDao.insertActLog(logParam);
						}	 */

						/* log.info(" 다운로드 로그 테이블에 저장 ~ "); */

					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		log.info("JSONArray.fromObject(rtnList).toString() :  " + JSONArray.fromObject(rtnList).toString());

		log.info(orgFilePath);
		if (orgFilePath != null && !"".equals(orgFilePath)) {
			Integer downloadCount = dao.getDownloadCount(orgFilePath);
			log.debug("downloadCount: {}", downloadCount);
		}

		log.info("rtnList >>>>>>> " + rtnList);
		return rtnList;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	@Override
	public int selectListCount(Object obj) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Map<String, Object> getUploadConfig() {
		return dao.getUploadConfig();
	}

//	public List<CommonUpdownFileVO> selectFileInfo(CommonUpdownParam param) {
//	}

	public boolean copyFile(CommonUpdownFileVO vo) {
		boolean success = false;
		File orgPath = new File(SystemConfig.getSystemConfigValue("UPDOWN_ORG_FILE_PATH") + vo.getOrgFileNm());

		return  success;
	}
}
