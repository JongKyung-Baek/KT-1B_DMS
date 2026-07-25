package kr.esob.fdms.commonlogic.download;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/download")
public class FileDownloadController {

    /**
     * Direct server-path downloads are intentionally disabled. Files must be
     * served through an authenticated resource-ID based endpoint with ACL checks.
     */
    @GetMapping({"", "/"})
    public ResponseEntity<Void> downloadFile() {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}
