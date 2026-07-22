package kr.esob.fdms.config;

import kr.esob.fdms.controller.main.MainService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
public class SessionInfoController {

    @Autowired
    private MainService service;

    @GetMapping("/getRemainingSessionTime")
    @ResponseBody
    public Map<String, Integer> getRemainingSessionTime(HttpSession session, HttpServletRequest request) {
        // 남은 시간을 계산하기 위한 변수
        long currentTime = System.currentTimeMillis();

        // 30분(1800초)를 기본 세션 시간으로 설정
        int defaultSessionTime = session.getMaxInactiveInterval();
        if (defaultSessionTime <= 0) {
            defaultSessionTime = SessionExtendController.sessionTime;
        }
        if (defaultSessionTime <= 0) {
            defaultSessionTime = 600;
        }
        Integer sessionTimeLeft = (Integer) session.getAttribute("sessionTimeLeft");

        // 세션이 처음 설정된 경우 (null인 경우)
        if (sessionTimeLeft == null) {
            session.setAttribute("sessionStartTime", currentTime);
            session.setAttribute("sessionTimeLeft", defaultSessionTime); // 30분 설정
            sessionTimeLeft = defaultSessionTime;
        } else {
            // 세션이 이미 존재하는 경우: 남은 시간을 계산
            Object sessionStartTimeAttribute = session.getAttribute("sessionStartTime");
            if (!(sessionStartTimeAttribute instanceof Long)) {
                session.setAttribute("sessionStartTime", currentTime);
                session.setAttribute("sessionTimeLeft", defaultSessionTime);
                sessionTimeLeft = defaultSessionTime;
            } else {
                long sessionStartTime = (long) sessionStartTimeAttribute;
                long elapsedTimeInSeconds = (currentTime - sessionStartTime) / 1000;
                sessionTimeLeft = defaultSessionTime - (int) elapsedTimeInSeconds;

                if (sessionTimeLeft <= 0) {
                    session.invalidate();
                    sessionTimeLeft = 0;
                }
            }
        }

        log.info("Remaining session time (seconds): {}", sessionTimeLeft);

        // 남은 시간 반환
        Map<String, Integer> response = new HashMap<>();
        response.put("remainingTime", sessionTimeLeft);
        log.info("sessionTimeLeft: {}", sessionTimeLeft);

        return response;
    }
}
