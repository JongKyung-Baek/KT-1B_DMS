package kr.esob.fdms.commonlogic.mail;

import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Autowired
    DocPdfLinkRequestDao dao;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Map<String, Object> mailConfig = getFromMail();

        // 2026-03-30: PostgreSQL key case/null-safe handling
//        String fromMail = mailConfig.get("fromMail").toString();
//        String host = mailConfig.get("host").toString();
//        int port = Integer.parseInt(mailConfig.get("port").toString());
//        String password = mailConfig.get("password").toString();
//        String fromName = mailConfig.get("fromName").toString();
        String fromMail = String.valueOf(mailConfig.getOrDefault("fromMail", ""));
        String host = String.valueOf(mailConfig.getOrDefault("host", ""));
        int port = Integer.parseInt(String.valueOf(mailConfig.getOrDefault("port", "25")));
        String password = String.valueOf(mailConfig.getOrDefault("password", ""));
        String fromName = String.valueOf(mailConfig.getOrDefault("fromName", ""));

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(fromMail);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    public Map<String, Object> getFromMail() {
        List<Map<String, Object>> dbConfig = dao.selectDbConfig();
        String fromMail = null;
        String password = null;
        String fromName = null;
        String host = null;
        int port = 0;

        if (dbConfig == null) {
            dbConfig = Collections.emptyList();
        }

        System.out.println("dbConfig.size(): " + dbConfig.size());
        for (Map<String, Object> config : dbConfig) {
            // 2026-03-30: support both uppercase/lowercase keys from result map
//            String configCd = config.get("SYSTEM_CONFIG_CD").toString();
//            String configValue = config.get("SYSTEM_CONFIG_VALUE").toString();
            String configCd = strVal(config, "SYSTEM_CONFIG_CD", "system_config_cd");
            String configValue = strVal(config, "SYSTEM_CONFIG_VALUE", "system_config_value");

            System.out.println("SYSTEM_CONFIG_CD: " + configCd + ", SYSTEM_CONFIG_VALUE: " + configValue);

            if ("FROM_MAIL_ADDRESS".equals(configCd)) {
                fromMail = configValue;
                System.out.println("fromMail: " + fromMail);
            }
            if ("FROM_MAIL_PASSWORD".equals(configCd)) {
                password = configValue;
            }
            if ("FROM_MAIL_NAME".equals(configCd)) {
                fromName = configValue;
            }
            if ("SMTP_HOST".equals(configCd)) {
                host = configValue;
            }
            if ("SMTP_PORT".equals(configCd)) {
                try {
                    port = Integer.parseInt(configValue);
                } catch (Exception ignore) {
                    port = 25;
                }
            }
        }

        Map<String, Object> mailConfig = new HashMap<>();
        mailConfig.put("fromMail", fromMail == null ? "" : fromMail);
        mailConfig.put("password", password == null ? "" : password);
        mailConfig.put("fromName", fromName == null ? "" : fromName);
        mailConfig.put("port", port == 0 ? 25 : port);
        mailConfig.put("host", host == null ? "" : host);

        return mailConfig;
    }

    private String strVal(Map<String, Object> map, String upperKey, String lowerKey) {
        Object val = map.get(upperKey);
        if (val == null) {
            val = map.get(lowerKey);
        }
        return val == null ? "" : String.valueOf(val);
    }
}
