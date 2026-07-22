package kr.esob.fdms.commonlogic.FromAdapToItn;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.distributionDept.DistributionDeptVO;

import java.sql.Connection;
  import java.sql.PreparedStatement;
  import java.sql.ResultSet;
  import java.sql.SQLException;
  import java.sql.Timestamp;
  import java.time.LocalDateTime;
  import java.util.ArrayList;
  import java.util.List;
  import java.util.Map;

  @Service
  public class PdmDataReader {
    //[KAI]
      private final DataSource dataSource;

      public PdmDataReader(DataSource dataSource) {
          this.dataSource = dataSource;
      }

      public void getDataFromADB(Map<String, Object> row, String fileName) {
          String query =
                  "SELECT TOP 1 * FROM caddrawinginfo " +
                  "WHERE LOWER(filename) = LOWER(?) " +
                  "ORDER BY " +
                  "  CASE WHEN CustomerRevision LIKE '[A-Z]' THEN 1 ELSE 0 END DESC, " +
                  "  CustomerRevision DESC, " +
                  "  Inputdate DESC";

          try (Connection conn = dataSource.getConnection();
               PreparedStatement pstmt = conn.prepareStatement(query)) {

              pstmt.setString(1, fileName);

              try (ResultSet rs = pstmt.executeQuery()) {
                  Timestamp timestampNow = Timestamp.valueOf(LocalDateTime.now());

                  while (rs.next()) {
                      row.put("insert_dt", timestampNow);
                      row.put("drawing_no", rs.getString("drawingno"));
                      row.put("drawing_nm", rs.getString("drawingtitle"));
                      row.put("change_action_no", rs.getString("changeactionno"));
                      row.put("factory_region", rs.getString("factoryregion"));
                      row.put("business_phase", rs.getString("businessphase"));
                      row.put("distribution_method", rs.getString("distributionmethod"));
                      row.put("file_type", rs.getString("filetype"));
                      row.put("request_ed_no", rs.getString("requestedno"));
                      row.put("customer_revision", rs.getString("customerrevision"));
                      row.put("org_file_nm", rs.getString("filename"));
                      row.put("file_order", rs.getInt("fileorder"));
                      row.put("current_page_no", rs.getInt("documentpage"));
                      row.put("total_page_no", rs.getInt("documenttotalpage"));
                      row.put("drawing_usage", rs.getString("drawingusage"));
                      row.put("customer_assignee", rs.getString("customerassignee"));
                      row.put("purchase_assignee", rs.getString("purchaseassignee"));
                      row.put("distributor", rs.getString("distributor"));
                      row.put("document_validation_expiredate", rs.getTimestamp("documentvalidationexpiredate"));
                      row.put("request_ed_date", rs.getTimestamp("requesteddate"));
                      row.put("document_approval_date", rs.getTimestamp("documentapprovaldate"));
                      row.put("standardization_approval_date", rs.getTimestamp("standardizationapprovaldate"));
                      row.put("technology_change_approval_date", rs.getTimestamp("technologychangeapprovaldate"));
                      row.put("model_code", rs.getString("modelcode"));
                      row.put("technology_protection_type", rs.getString("technologyprotectiontype"));
                      row.put("obsolete_status", rs.getString("obsoletestatus"));
                      row.put("obsolete_classification", rs.getString("obsoleteclassification"));
                      row.put("attachment_count", rs.getInt("attachmentcount"));
                      row.put("check_3dfile", rs.getString("check3dfile"));
                      row.put("create_date", rs.getTimestamp("inputdate"));
                      row.put("input_date", rs.getTimestamp("inputdate"));
                      row.put("edit_date", rs.getTimestamp("editdate"));
                      row.put("interface_yn", rs.getString("interfaceyn"));
                      row.put("rev_no", rs.getString("revision"));
                      row.put("revision", rs.getString("revision"));
                      row.put("remark", rs.getString("remark"));
                      row.put("customer_document_no", rs.getString("customerdocumentno"));
                      row.put("customer_document_name", rs.getString("customerdocumentname"));
                      row.put("distribution_type", rs.getString("distributiontype"));
                      row.put("distribution_point", rs.getString("distributionpoint"));
                      row.put("connection_id", rs.getString("connectionid"));
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

      public List<DistributionDeptVO> getDistributionDept(int departmenInfoSeq) {
          List<DistributionDeptVO> distributionDeptList = new ArrayList<>();
          String query = "SELECT * FROM DepartmentInfo WHERE [index] >= ?";

          try (Connection conn = dataSource.getConnection();
               PreparedStatement pstmt = conn.prepareStatement(query)) {

              pstmt.setInt(1, departmenInfoSeq);

              try (ResultSet rs = pstmt.executeQuery()) {
                  while (rs.next()) {
                      DistributionDeptVO distributionDept = new DistributionDeptVO();
                      distributionDept.setDrawingNo(rs.getString("DrawingNo"));
                      distributionDept.setDistributionPoint(rs.getString("DistributionPoint"));
                      distributionDept.setIdx(rs.getInt("Index"));
                      distributionDept.setInputDate(rs.getTimestamp("inputDate"));
                      distributionDeptList.add(distributionDept);
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }

          return distributionDeptList;
      }

      public void getDistributionType(List<DistributionDeptVO> deptList) {
          String query = "SELECT distributiontype FROM CaddrawingInfo WHERE drawingno = ?";

          try (Connection conn = dataSource.getConnection();
               PreparedStatement pstmt = conn.prepareStatement(query)) {

              for (DistributionDeptVO distributionDept : deptList) {
                  List<String> distributionTypes = new ArrayList<>();

                  pstmt.setString(1, distributionDept.getDrawingNo());

                  try (ResultSet rs = pstmt.executeQuery()) {
                      while (rs.next()) {
                          distributionTypes.add(rs.getString("distributiontype"));
                      }
                  }

                  distributionDept.setDistributionType(distributionTypes);
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

    //demo용 
    /*
    @Value("${pdm.source.db.url}")
    private String URL;

    @Value("${pdm.source.db.username}")
    private String USER;

    @Value("${pdm.source.db.password}")
    private String PASSWORD;

    public void getDataFromADB( Map<String, Object> row, String fileName) {
//      String query = "SELECT * FROM caddrawinginfo WHERE LOWER(filename) = LOWER(?) ORDER BY revision DESC LIMIT 1";
        String query =
                "SELECT TOP 1 * FROM caddrawinginfo " +
                        "WHERE LOWER(filename) = LOWER(?) " +
                        "ORDER BY " +
                        "  CASE WHEN CustomerRevision LIKE '[A-Z]' THEN 1 ELSE 0 END DESC, " +
                        "  CustomerRevision DESC, " +
                        "  Inputdate DESC";


        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fileName);
            ResultSet rs = pstmt.executeQuery();


            // 현재 시간 LocalDateTime -> Timestamp 변환
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestampNow = Timestamp.valueOf(now);

            while (rs.next()) {
                String distributionType = rs.getString("distributiontype");
                row.put("insert_dt", timestampNow);
                row.put("drawing_no", rs.getString("drawingno"));
                row.put("drawing_nm", rs.getString("drawingtitle"));
                row.put("change_action_no", rs.getString("changeactionno"));
                row.put("factory_region", rs.getString("factoryregion"));
                row.put("business_phase", rs.getString("businessphase"));
                row.put("distribution_method", rs.getString("distributionmethod"));
                row.put("file_type", rs.getString("filetype"));
                row.put("request_ed_no", rs.getString("requestedno"));
                row.put("customer_revision", rs.getString("customerrevision"));
                row.put("org_file_nm", rs.getString("filename"));
                row.put("file_order", rs.getInt("fileorder"));
                row.put("current_page_no", rs.getInt("documentpage"));
                row.put("total_page_no", rs.getInt("documenttotalpage"));
                row.put("drawing_usage", rs.getString("drawingusage"));
                row.put("customer_assignee", rs.getString("customerassignee"));
                row.put("purchase_assignee", rs.getString("purchaseassignee"));
                row.put("distributor", rs.getString("distributor"));
                row.put("document_validation_expiredate", rs.getTimestamp("documentvalidationexpiredate"));
                row.put("request_ed_date", rs.getTimestamp("requesteddate"));
                row.put("document_approval_date", rs.getTimestamp("documentapprovaldate"));
                row.put("standardization_approval_date", rs.getTimestamp("standardizationapprovaldate"));
                row.put("technology_change_approval_date", rs.getTimestamp("technologychangeapprovaldate"));
                row.put("model_code", rs.getString("modelcode"));
                row.put("technology_protection_type", rs.getString("technologyprotectiontype"));
                row.put("obsolete_status", rs.getString("obsoletestatus"));
                row.put("obsolete_classification", rs.getString("obsoleteclassification"));
                row.put("attachment_count", rs.getInt("attachmentcount"));
                row.put("check_3dfile", rs.getString("check3dfile"));
                row.put("create_date", rs.getTimestamp("inputdate"));
                row.put("input_date", rs.getTimestamp("inputdate"));
                row.put("edit_date", rs.getTimestamp("editdate"));
                row.put("interface_yn", rs.getString("interfaceyn"));
                row.put("rev_no", rs.getString("revision"));
                row.put("revision", rs.getString("revision"));
                row.put("remark", rs.getString("remark"));
                row.put("customer_document_no", rs.getString("customerdocumentno"));
                row.put("customer_document_name", rs.getString("customerdocumentname"));
                row.put("distribution_type", rs.getString("distributiontype"));
                row.put("distribution_point", rs.getString("distributionpoint"));
                row.put("connection_id", rs.getString("connectionid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DistributionDeptVO> getDistributionDept(int departmenInfoSeq) {
        List<DistributionDeptVO> distributionDeptList = new ArrayList<>();

        String query = "SELECT * FROM DepartmentInfo WHERE [index] >= ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, departmenInfoSeq);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DistributionDeptVO distributionDept = new DistributionDeptVO();
                distributionDept.setDrawingNo(rs.getString("DrawingNo"));
                distributionDept.setDistributionPoint(rs.getString("DistributionPoint"));
                distributionDept.setIdx(rs.getInt("Index"));
                Timestamp timestamp = rs.getTimestamp("inputDate");

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

    public void getDistributionType(List<DistributionDeptVO> deptList) {
        String query = "SELECT distributiontype FROM CaddrawingInfo WHERE drawingno = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for(DistributionDeptVO distributionDept : deptList) {
                List<String> distributionTypes = new ArrayList<>();

                pstmt.setString(1, distributionDept.getDrawingNo());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    distributionTypes.add(rs.getString("distributiontype"));
//                    distributionDept.setDistributionType(rs.getString("DistributionType"));
                }
                distributionDept.setDistributionType(distributionTypes);
                for(String distributionType: distributionTypes){
                    System.out.println("distributionType: " + distributionType);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }      
      
      
*/
  }