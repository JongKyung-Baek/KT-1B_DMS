package kr.esob.fdms.controller.login;


import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;



@Controller
@RequestMapping("/login/password")
public class PasswordController {

    @Autowired
    DocPdfLinkRequestDao dao_for_pwd;


    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Inject
    Prop prop;

    @RequestMapping("")
    public String getPasswordConfigPage(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserVO userVo = (UserVO) authentication.getPrincipal();

        List<Map<String,Object>> dbConfig = dao_for_pwd.selectDbConfig();
        String basicPassword="";

        for (Map<String, Object> config : dbConfig) {
            Object configCd = config.get("SYSTEM_CONFIG_CD");
            if ("BASIC_PASSWORD".equals(configCd)) {
                Object configValue = config.get("SYSTEM_CONFIG_VALUE");
                basicPassword = configValue == null ? "" : configValue.toString();
            }
        }


        model.addAttribute("basicPassword", basicPassword);
        log.info("userVo: {}", userVo.toString());
        model.addAttribute("userVo", userVo);
        return "login/passwordConfig";
    }
}
