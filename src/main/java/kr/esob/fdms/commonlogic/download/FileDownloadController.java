package kr.esob.fdms.commonlogic.download;

import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/download")
public class FileDownloadController {

    // 원래는 이렇게 쓰면 안되지만 리팩토링은 추후에 하도록 하겠습니다. 23/06/01 dev_koo
//    @Autowired
//    DocPdfLinkRequestDao dao;

    @GetMapping("/")
    public ResponseEntity<byte[]> downloadFile(
            @RequestParam String fileName
    ) throws IOException {
        // 파일을 찾는 로직을 구현해야 합니다.
        System.out.println("들어오는지 확인" + fileName);
        File file = new File(fileName);  // 실제 파일 경로로 변경해야 합니다.

        // 파일이 존재하는지 확인합니다.
        if (!file.exists()) {
            // 파일이 없을 경우 예외 처리를 원하는 대로 구현하세요.
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }

        // 파일을 응답에 첨부합니다.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getName());  // 다운로드되는 파일명으로 변경 가능

        // 파일을 byte 배열로 변환하여 응답합니다.
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

//    @GetMapping("/")
//    public ResponseEntity<byte[]> asd() throws IOException {
//        // 파일을 찾는 로직을 구현해야 합니다.
//        System.out.println("들어오는지 확인");
//        File file = new File("C:\\workspaceStartupHub\\OUT\\Document\\sample.docx");  // 실제 파일 경로로 변경해야 합니다.
//
//        // 파일이 존재하는지 확인합니다.
//        if (!file.exists()) {
//            // 파일이 없을 경우 예외 처리를 원하는 대로 구현하세요.
//            throw new RuntimeException("파일을 찾을 수 없습니다.");
//        }
//
//        // 파일을 응답에 첨부합니다.
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDispositionFormData("attachment", file.getName());  // 다운로드되는 파일명으로 변경 가능
//
//        // 파일을 byte 배열로 변환하여 응답합니다.
//        byte[] fileContent = FileUtils.readFileToByteArray(file);
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(fileContent);
//    }
}
