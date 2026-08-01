package kr.esob.tdms.commonlogic.updown;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DownloadedFileActLogController {

    /**
     * Client-supplied file action logs are not trusted. Download results are
     * persisted by the V2 state machine using the authenticated server actor.
     */
    @PostMapping("/log/downloadedFileActLog")
    public ResponseEntity<Void> receiveData() {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}
