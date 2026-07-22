package kr.esob.fdms.commonlogic.distributionDept;

import kr.esob.fdms.commonlogic.FromAdapToItn.FromAdapToItnDao;
import kr.esob.fdms.commonlogic.FromAdapToItn.PdmDataReader;
import kr.esob.fdms.commonlogic.FromAdapToItn.TargetDbWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DeptInfoService {

    @Autowired
    FromAdapToItnDao itnDao;

    @Autowired
    PdmDataReader pdmDataReader;

    @Autowired
    TargetDbWriter targetDbWriter;

    public void moveData(){
        // 1. distribution_dept 테이블에서 가장 높은 인덱스를 가진 데이터의 인덱스 +1
        int departmentInfoIdx = getIndex();
        log.info("index: {}", departmentInfoIdx);


        // 2. DepartmentInfo 테이블 데이터 조회(추가된 데이터가 없다면 스케줄러 종료)
        List<DistributionDeptVO> deptList = pdmDataReader.getDistributionDept(departmentInfoIdx+1);
        if(deptList.isEmpty()){
            log.info("no updated dept info");
        }

        // 3. distribution_dept 테이블로 데이터 복사
        int copyResult = targetDbWriter.copyDistributionDept(deptList);
        if(!(copyResult == deptList.size())){
            log.error("failed to copy dept info");
        }


        // 4. distribution_dept 테이블에서 추가된 데이터 조회
        List<DistributionDeptVO> copiedDeptList = targetDbWriter.getCopiedDistributionDept();
        if (copiedDeptList == null || copiedDeptList.isEmpty()) {
            log.info("copiedDeptList is null or empty.");
        }

        // 5. CADDrawingInfo 테이블에서 drawing_no로 배포 타입 조회
        boolean tableNameResult = getTableName(copiedDeptList);
        if(!tableNameResult) log.error("getTableName() returned false");

        log.info("----------copiedDeptList----------");
        for(DistributionDeptVO deptVO : copiedDeptList){
            log.info(deptVO.toString());
        }

        // 6. 도면, CP, DXF 테이블에 변경사항 적용
        List<String> updateFailedDrawingList = targetDbWriter.updateDistributionDept(copiedDeptList);

        // 7. 복사 테이블의 status 값 업데이트
        if(updateFailedDrawingList.isEmpty() ) {
            int updateStatus = targetDbWriter.updateCopiedDataStatus(copiedDeptList);
            log.info("{} status updated", updateStatus);
        } else{
            for(String updateFailedDrawing : updateFailedDrawingList) {
                // log.info("도면 {} 권한 부서 정보 업데이트 시도", updateFailedDrawing);
                log.info("");
            }
        }

    }

    private int getIndex() {
        return targetDbWriter.getIndexFromDepartmentInfo();
    }

    private boolean getTableName(List<DistributionDeptVO> deptList){
        pdmDataReader.getDistributionType(deptList);

        for(DistributionDeptVO deptVO : deptList){
            if(deptVO.getDistributionType().isEmpty()) {
                log.error("deptVO.getDistributionType() returned null");
                return false;
            }
            else{
                List<String> distributionTypes = deptVO.getDistributionType();
                List<String> tableNames = new ArrayList<>();

                for (String distributionType : distributionTypes) {
                    if (distributionType.equals("도면/공정서")) {
                        tableNames.add("docs_drawing");
                    } else if (distributionType.equals("CP 도면") || distributionType.equals("CP")) {
                        tableNames.add("docs_product_document");
                        tableNames.add("docs_product_document_file");
                    } else if (distributionType.equals("DXF 도면") || distributionType.equals("DXF")) {
                        tableNames.add("docs_dxf_document");
                        tableNames.add("docs_dxf_document_file");
                    } else if (distributionType.equals("문서")) {
                        tableNames.add("docs_document");
                        tableNames.add("docs_document_file");
                    } else if (distributionType.equals("SW")) {
                        tableNames.add("docs_sw");
                        tableNames.add("docs_sw_file");
                    } else {
                        log.error("Unknown distributionType for drawingNo {}: {}", deptVO.getDrawingNo(), distributionType);
//                        return false;
                    }

                }
                deptVO.setTableName(tableNames);
            }
        }
        return true;
    }

}





























