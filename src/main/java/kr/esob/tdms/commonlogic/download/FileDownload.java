package kr.esob.tdms.commonlogic.download;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Compatibility endpoint retained only to give old clients an explicit
 * deprecation response. The former implementation accepted an encrypted
 * filesystem path from the request and therefore bypassed resource ACLs.
 */
@Controller
public class FileDownload {

    @RequestMapping("/fileDownload")
    public ResponseEntity<Void> legacyDirectPathDownload() {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}
