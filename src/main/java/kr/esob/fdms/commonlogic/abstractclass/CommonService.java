package kr.esob.fdms.commonlogic.abstractclass;

import java.util.List;

/**
 * 추가,수정,삭제,selectList(Grid 에서 사용) 하는 것들을 공통으로 사용하기 위해
 * 인터페이스를 정의하여 사용
 * @author younjh
 *
 */
public interface CommonService {
	@SuppressWarnings("rawtypes")
	public List selectList(Object obj);
	public int selectListCount(Object obj);

}
