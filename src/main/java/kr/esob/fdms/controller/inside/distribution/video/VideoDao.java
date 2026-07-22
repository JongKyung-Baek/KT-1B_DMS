package kr.esob.fdms.controller.inside.distribution.video;


import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.distribution.drawingrequest.DrawingRequestVO;
import kr.esob.fdms.controller.inside.distribution.production.ProductionRegisterPopupParam;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VideoDao extends AbstractDao {

    private String prefix = "sql.VideoRequest.";



    public List<VideoRequestVO> selectList(Object param){
        return list(prefix + "selectList", param);
    }

    public Integer selectListCount(Object param){
        return (Integer) obj(prefix + "selectListCount", param);
    }


    public void insertVideoRegisterInfo(VideoRegisterPopupParam param) {
        insert(prefix + "insertVideoRegisterInfo", param);
    }

    public String getVideoPath(Object objectId){
        return sqlSession.selectOne(prefix+"getVideoPath", objectId);
    }

    //	2023.07.21 기범(중복파일은 못올라감)
    @Autowired
    private SqlSession sqlSession;
    public String getVideoRegisterByOrgFileNm(String objectId) {
        return sqlSession.selectOne(prefix + "getVideoRegisterByOrgFileNm", objectId);
    }




}
