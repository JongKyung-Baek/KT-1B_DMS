package kr.esob.fdms.config;

import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.controller.main.MainService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/extendSession")
public class SessionExtendController {

    public static Integer sessionTime = 0;

    @Autowired
    private MainService service;

    @Autowired
    private Provider<SessionValue> sessionValueProvider;

    @PostMapping("")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> extendSession(HttpSession session) {
        int timeoutSeconds = resolveSessionTime(session);
        log.info("extended session time: {}", timeoutSeconds);
        session.setMaxInactiveInterval(timeoutSeconds);
        session.setAttribute("sessionStartTime", System.currentTimeMillis());
        session.setAttribute("sessionTimeLeft", timeoutSeconds);

        HashMap<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/sessionTime")
    public ResponseEntity<Map<String, Boolean>> setSessionTime(@RequestBody Map<String, Integer> request, HttpSession session) {
        HashMap<String, Boolean> response = new HashMap<>();

        Integer requestSessionTime = request.get("sessionTime");
        if(requestSessionTime == null || requestSessionTime < 10 || requestSessionTime > 240) {
            response.put("success", false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        sessionTime = requestSessionTime * 60;
        log.info("sessionTime on setSessionTime: {}", sessionTime);

        int result = service.updateSessionTime(sessionTime);
        if(result != 1){
            response.put("success", false);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        session.setMaxInactiveInterval(sessionTime);
        session.setAttribute("sessionStartTime", System.currentTimeMillis());
        session.setAttribute("sessionTimeLeft", sessionTime);
        try {
            sessionValueProvider.get().setTimeoutSecond(String.valueOf(sessionTime));
        } catch(Exception e) {
            log.warn("Unable to update session scoped timeout value", e);
        }

        response.put("success", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private int resolveSessionTime(HttpSession session) {
        if (sessionTime != null && sessionTime > 0) {
            return sessionTime;
        }
        int timeoutSeconds = session.getMaxInactiveInterval();
        return timeoutSeconds > 0 ? timeoutSeconds : 600;
    }
}