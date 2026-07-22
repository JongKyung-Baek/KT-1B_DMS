package kr.esob.fdms.controller.inside.organizationmanage.insidedept;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class InsidedeptService implements CommonService {

	@Inject
	InsidedeptDao dao;

	@Inject
	DateUtil dateUtil;

	@Autowired
	DocPdfLinkRequestDao dao_for_pwd;


	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public ResultVO saveRegsiterDept(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();

		String delYn = "N";
		String sortSeq = "20";
		String nextDeptCd = "";

		// 부서 코드, 부서명 조회
		InsidedeptListVO deptInfo = dao.selectDeptInfo();
		nextDeptCd = generateNextDeptCd(deptInfo);

		String deptNm = request.getParameter("deptNm");
		String deptNms = deptInfo.getAllDeptNms();

		log.info("deptNms:{}", deptNms);
		log.info("deptNm:{}", deptNm);
		log.info("deptCd:{}", request.getParameter("deptCd"));

		if ( request.getParameter("saveFlag").equals("I")){ // 생성일때

			// userPopupParam 객체 만들어서 저장
			DeptPopupParam deptPopupParam = DeptPopupParam.builder()
					.deptCd(nextDeptCd)
					.deptNm(deptNm)
					.useYn(request.getParameter("useYn"))
					.delYn(delYn)
					.sortSeq(sortSeq)
					.build();

			log.info("saveFlag is I");

			if (request.getParameter("deptNm") == null){
				resultVo.setMessage("msg.selectDeptNm");
				resultVo.setSuccess(false);
				return resultVo;
			} else if (!checkDeptNm(deptInfo, deptNm)) {
				resultVo.setMessage("msg.alrExistDept"); // "이미 존재하는 부서입니다."
				resultVo.setSuccess(false);
				return resultVo;
			} else {
				dao.insertRegisterDeptInfo(deptPopupParam); // 저장
				resultVo.setSuccess(true);
				return resultVo;
			}

		}else if( request.getParameter("saveFlag").equals("U")){ // 수정일때

			// userPopupParam 객체 만들어서 저장
			DeptPopupParam deptPopupParam = DeptPopupParam.builder()
					.deptCd(request.getParameter("deptCd"))
					.deptNm(deptNm)
					.useYn(request.getParameter("useYn"))
					.delYn(delYn)
					.sortSeq(sortSeq)
					.build();

			// deptCd , positionCd, roleGroupCd 가 null 값이면 실패
			if ( request.getParameter("deptCd") == null || request.getParameter("deptNm") == null){
				resultVo.setMessage("msg.selectDeptNm");
				resultVo.setSuccess(false);
				return resultVo;
			}

			// 현재 부서에 소속된 사용자가 있는데 useYn 값을 N으로 변경 시 실패
			if(request.getParameter("useYn").equals("N")){
				int deptUsers = dao.countUsersByDeptCd(deptPopupParam);
				System.out.println("deptUsers: " + deptUsers);
				if(deptUsers > 0){
					resultVo.setMessage("msg.userExist");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			dao.editDeptInfo(deptPopupParam); // 정보 업데이트
			resultVo.setSuccess(true);
			return resultVo;
			}

		System.out.println(" 실패 ");
		resultVo.setSuccess(false);
		return resultVo;

		}

		private String generateNextDeptCd(InsidedeptListVO deptInfo){
			String maxDeptCd = deptInfo.getDeptCd(); // 가장 큰 deptCd 가져오기

			if (maxDeptCd == null) {
				// 초기값 설정 (최초 생성 시)
				return "";
			}

			String letterPart = maxDeptCd.replaceAll("\\d+$", ""); // 문자 부분 추출
			String numberPart = maxDeptCd.substring(letterPart.length());

			System.out.println("maxDeptCd: " + maxDeptCd);
			System.out.println("letterPart: " + letterPart);
			System.out.println("numberPart: " + numberPart);
			int newNumber = Integer.parseInt(numberPart) + 1; // 숫자 증가

			// 새로운 deptCd 생성 (숫자는 3자리로 포맷)
			return letterPart + String.format("%03d", newNumber);
		}

		public boolean checkDeptNm(InsidedeptListVO deptInfo, String deptNm){
			String deptNms = deptInfo.getAllDeptNms();

			if (deptNms != null && !deptNms.isEmpty()) {
				// 콤마로 문자열 분리 후 List에 담기
				List<String> deptNamesList = Arrays.asList(deptNms.split(","));

				for(String deptName : deptNamesList){
					System.out.println("deptName: " + deptName);
				}

				// 특정 deptNm이 존재하는지 확인
				if (deptNamesList.contains(deptNm)) {
					log.info("deptNm is already exist.");
				} else {
					return true;
				}
			} else {
				System.out.println("deptNms 값이 비어있거나 null입니다.");
			}
			return false;
		}


	public DeptListVO selectDept(String deptCd) {
		return dao.selectDept(deptCd);
	}
}






