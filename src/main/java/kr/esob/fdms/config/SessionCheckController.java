package kr.esob.fdms.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
public class SessionCheckController {

    private static final Logger log = LoggerFactory.getLogger(SessionCheckController.class);

    @GetMapping("/checkSession")
    public ResponseEntity<Map<String, Boolean>> checkSession(HttpServletRequest request) {
        Map<String, Boolean> response = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session == null || session.getId() == null) {
//            log.info("세션이 만료 ~ ~ ~ ~ ~ ");
            response.put("sessionExpired", true);
        } else {
//            log.info("세션 아직 유효 ~ ~ ~ ~ ~ ~");
            response.put("sessionExpired", false);
        }

        return ResponseEntity.ok(response);
    }
}
