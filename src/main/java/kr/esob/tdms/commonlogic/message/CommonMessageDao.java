package kr.esob.tdms.commonlogic.message;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class CommonMessageDao extends AbstractDao{
	@SuppressWarnings("unchecked")
	public List<CommonMessageVO> selectMessageList() {
		return list("sql.CommonMessage.selectMessageList");
	}
}
