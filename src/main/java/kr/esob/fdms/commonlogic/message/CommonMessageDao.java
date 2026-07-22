package kr.esob.fdms.commonlogic.message;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class CommonMessageDao extends AbstractDao{
	@SuppressWarnings("unchecked")
	public List<CommonMessageVO> selectMessageList() {
		return list("sql.CommonMessage.selectMessageList");
	}
}
