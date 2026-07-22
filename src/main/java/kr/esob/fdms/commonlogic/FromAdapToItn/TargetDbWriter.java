
package kr.esob.fdms.commonlogic.FromAdapToItn;

import kr.esob.fdms.commonlogic.distributionDept.DistributionDeptVO;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TargetDbWriter {
    @Value("${spring.datasource.url}")
    private String URL;

    @Value("${spring.datasource.username}")
    private String USER;

    @Value("${spring.datasource.password}")
    private String PASSWORD;

    // 각 테이블에서 cn_serial의 최대값 조회
    private int getNextCnSerial(Connection conn, String table) throws SQLException {
        String query = String.format("SELECT COALESCE(MAX(cn_serial), 0) + 1 AS next_cn_serial FROM %s", table);
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("next_cn_serial");
            } else {
                throw new SQLException("cn_serial 값을 조회하는데 실패했습니다.");
            }
        }
    }
    public int insertDataToBDB(Map<String, Object> data, String distributionType) {
        String[] tableNames = checkTable(distributionType);  // tableNames 배열
        List<String> uniqueColumns = checkUniqueColumns(distributionType);  // 여러 컬럼 처리
        int result = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)){
//            int nextCnSerial = getNextCnSerial(conn);
//            System.out.println("nextCnSerial : " + nextCnSerial);
            Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
            System.out.println("currentTimestamp : " + currentTimestamp);

            for(String tableName: tableNames){
                System.out.println("tableName: " + tableName);
            }

            // 컬럼과 placeholder 생성
            String columns = "cn_serial, " + String.join(", ", data.keySet());
            if (!uniqueColumns.isEmpty()) {
                columns += ", " + String.join(", ", uniqueColumns);
            }

            String placeholders = String.join(", ", Collections.nCopies(data.size() + 1, "?"));  // cn_serial 포함
            if (!uniqueColumns.isEmpty()) {
                placeholders += ", " + String.join(", ", Collections.nCopies(uniqueColumns.size(), "?"));
            }

            // 각 테이블에 데이터 삽입
            for (String tableName : tableNames) {
                String query = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
                System.out.println("Generated Query: " + query);  // 디버깅용 출력
                int cn_serial = getNextCnSerial(conn, tableName);

                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    int index = 1;

                    // cn_serial 설정
                    pstmt.setInt(index++, cn_serial);

                    // Map의 값들을 PreparedStatement에 설정
                    for (Object value : data.values()) {
                        pstmt.setObject(index++, value);
                    }

                    // 추가 컬럼 값 설정
                    for (String column : uniqueColumns) {
                        if (column.equals("interface_dt")) {
                            pstmt.setTimestamp(index++, currentTimestamp);
                        } else if (column.equals("file_no")) {
                            pstmt.setString(index++, "0");
                        }
                    }

                    // 실행
                    result = pstmt.executeUpdate();
                    System.out.println(result + " row(s) inserted into " + tableName);
                } catch (SQLException e) {
                    System.err.println("Error inserting into table: " + tableName);
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int deleteDataFromTargetDB(String objectId, String distributionType) {
        String[] tableNames = checkTable(distributionType);  // 삭제할 테이블 목록
        int result = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);  // 트랜잭션 시작

            for (String tableName : tableNames) {
                String query = String.format("DELETE FROM %s WHERE object_id = ?", tableName);
                System.out.println("Generated delete Query: " + query);

                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, objectId);

                    int rowsAffected = pstmt.executeUpdate();
                    result += rowsAffected;  // 삭제된 행의 수를 누적
                    System.out.println(rowsAffected + " row(s) deleted from " + tableName);
                } catch (SQLException e) {
                    System.err.println("Error deleting from table: " + tableName);
                    e.printStackTrace();
                    conn.rollback();  // 실패 시 롤백
                    return 0;  // 오류가 발생하면 즉시 종료
                }
            }
            conn.commit();  // 성공적으로 삭제되면 커밋
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

//    public String getPrevRevisionFile(String obejct_no){
//        String query = "SELECT request_no FROM docs_request_mapping WHERE object_no = ? ORDER BY rev_no DESC LIMIT 1";
//        String requestNo = "";
//
//        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
//             PreparedStatement pstmt = conn.prepareStatement(query)) {
//
//            pstmt.setString(1, obejct_no);
//            ResultSet rs = pstmt.executeQuery();
//
//            if (rs.next()) {
//                requestNo = rs.getString("request_no");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return requestNo;
//    }
    public List<String> getPrevRevisionFile(String object_no) {
        String query = "SELECT request_no FROM docs_request_mapping WHERE object_no = ? ORDER BY rev_no DESC";
        List<String> requestNos = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, object_no);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requestNos.add(rs.getString("request_no"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requestNos;
    }

    public int insertRevisionData(Map<String, Object> data){
        int result = 0;

        int mailId = Integer.parseInt(data.get("mail_id").toString());
        String toMail = data.get("to_mail").toString();
        String userNm = data.get("user_nm").toString();
        String drawingNo = data.get("drawing_no").toString();
        String drawingNm = data.get("drawing_nm").toString();
        String revNo = data.get("rev_no").toString();

        String query = "INSERT INTO docs_mail_revision (mail_id, to_mail, user_nm, drawing_no, drawing_nm, rev_no, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, mailId);
            pstmt.setString(2, toMail);
            pstmt.setString(3, userNm);
            pstmt.setString(4, drawingNo);
            pstmt.setString(5, drawingNm);
            pstmt.setString(6, revNo);
            pstmt.setString(7, "F");

            result += pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Map<String, List<Map<String, Object>>> getRevisionData(){
        String query = "SELECT * FROM docs_mail_revision WHERE status = 'F'";
        Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int mailId = rs.getInt("mail_id");
                String toMail = rs.getString("to_mail");
                String userNm = rs.getString("user_nm");
                String drawingNo = rs.getString("drawing_no");
                String drawingNm = rs.getString("drawing_nm");
                String revNo = rs.getString("rev_no");

                // 새로운 도면 데이터 생성
                Map<String, Object> drawingData = new HashMap<>();
                drawingData.put("mail_id", mailId);
                drawingData.put("drawing_no", drawingNo);
                drawingData.put("user_nm", userNm);
                drawingData.put("drawing_nm", drawingNm);
                drawingData.put("rev_no", revNo);
                groupedData.computeIfAbsent(toMail, k -> new ArrayList<>()).add(drawingData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groupedData;
    }

    private String[] checkTable(String distributionType){
        String[] tableName = {};

        System.out.println("distributionType: "+distributionType);
        // 추후 해당 방식을 enum으로 바꾸는 것이 나을듯.
        if (distributionType.equals("도면/공정서")) tableName = new String[]{"docs_drawing"};
        if (distributionType.equals("문서")) tableName = new String[]{"docs_document", "docs_document_file"};
        if (distributionType.equals("CP 도면")) tableName =  new String[]{"docs_product_document", "docs_product_document_file"};
        if (distributionType.equals("CP")) tableName =  new String[]{"docs_product_document", "docs_product_document_file"};
        if (distributionType.equals("SW")) tableName =  new String[]{"docs_sw", "docs_sw_file"};
        if (distributionType.equals("DXF 도면")) tableName =  new String[]{"docs_dxf_document", "docs_dxf_document_file"};
        if (distributionType.equals("DXF")) tableName =  new String[]{"docs_dxf_document", "docs_dxf_document_file"};

        return tableName;
    }

    private List<String> checkUniqueColumns(String distributionType) {
        List<String> uniqueColumns = new ArrayList<>();

        switch (distributionType) {
            case "도면/공정서":
                break;  // 빈 리스트 반환 (추가 컬럼 없음)
            case "문서":
            case "CP 도면":
            case "CP":
            case "DXF 도면":
            case "DXF":
                uniqueColumns.add("file_no");
                break;
            case "SW":
                uniqueColumns.add("interface_dt");
                uniqueColumns.add("file_no");
                break;
        }
        return uniqueColumns;
    }

    public int changeRevisionData(List<MailInfoVO> mails) {
        int result = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            for (MailInfoVO mail : mails) {

                int[] mailIds = extractMailId(mail.getMailId());

                String query = String.format("UPDATE docs_mail_revision SET status = 'S' WHERE mail_id = ?");

                for (int i = 0; i < mailIds.length; i++) {

                    System.out.println("Generated update Query: " + query);

                    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                        pstmt.setInt(1, mailIds[i]);

                        int rowsAffected = pstmt.executeUpdate();
                        result += rowsAffected;  // 변경된 행의 수를 누적
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return 0;  // 오류가 발생하면 즉시 종료
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public int[] extractMailId(String mailIds){

        System.out.println("mailIds = " + mailIds);

        String[] mailIdArray = mailIds.split(",");

        int[] formattedMailIds = new int[mailIdArray.length];
        for (int i = 0; i < mailIdArray.length; i++) {
            formattedMailIds[i] = Integer.parseInt(mailIdArray[i].trim());
        }

        return formattedMailIds;
    }

    public int copyDistributionDept(List<DistributionDeptVO> deptList) {
        int result = 0;

        for(DistributionDeptVO distributionDept : deptList) {
            int idx = distributionDept.getIdx();
            String drawingNo = distributionDept.getDrawingNo();
            String distributionPoint = distributionDept.getDistributionPoint();
            Timestamp inputDate = distributionDept.getInputDate();


            String query = "INSERT INTO distribution_dept (drawing_no, distribution_point, input_date, status, idx) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, drawingNo);
                pstmt.setString(2, distributionPoint);
                pstmt.setTimestamp(3, inputDate);
                pstmt.setString(4, "F");
                pstmt.setInt(5, idx);

                result += pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public int insertNewSeq(int newSeq) {
        return 0;
    }

    public List<DistributionDeptVO> getCopiedDistributionDept() {
        List<DistributionDeptVO> distributionDeptList = new ArrayList<>();

        String query = "SELECT * FROM distribution_dept WHERE status = 'F' ORDER BY idx";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DistributionDeptVO distributionDept = new DistributionDeptVO();
                distributionDept.setDrawingNo(rs.getString("drawing_no"));
                distributionDept.setDistributionPoint(rs.getString("distribution_point"));
                distributionDept.setIdx(rs.getInt("idx"));
                Timestamp timestamp = rs.getTimestamp("input_date");

//                if (timestamp != null) {
                distributionDept.setInputDate(timestamp);
//                } else {
//                    distributionDept.setInputDate(null);
//                }

                distributionDeptList.add(distributionDept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return distributionDeptList;
    }

    public int getIndexFromDepartmentInfo() {
        int maxIdx = 0;

        String query = "SELECT MAX(idx) AS max_idx FROM distribution_dept";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();

            // MAX(id) 결과를 가져오기
            if (rs.next()) {
                maxIdx = rs.getInt("max_idx");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxIdx;
    }

    public List<String> updateDistributionDept(List<DistributionDeptVO> deptList) {
        int result = 0;

        List<String> updateFailedDrawingList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            for (DistributionDeptVO deptVO : deptList) {

                List<String> tableNames = deptVO.getTableName();
                for(String tableName : tableNames) {
                    String query = "UPDATE " + tableName +" SET distribution_point = ? WHERE drawing_no = ?";

                    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                        pstmt.setString(1, deptVO.getDistributionPoint());
                        pstmt.setString(2, deptVO.getDrawingNo());

                        int prevResult = result;
                        int rowsAffected = pstmt.executeUpdate();
                        result += rowsAffected;
                        if(prevResult == result) updateFailedDrawingList.add(deptVO.getDrawingNo());
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return updateFailedDrawingList;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updateFailedDrawingList;
    }

    public int updateCopiedDataStatus(List<DistributionDeptVO> deptList) {
        int result = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            for (DistributionDeptVO deptVO : deptList) {
                    String query = "UPDATE distribution_dept SET status = 'S' WHERE idx = ?";

                    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                        pstmt.setInt(1, deptVO.getIdx());

                        int rowsAffected = pstmt.executeUpdate();
                        result += rowsAffected;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return 0;
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}