package kr.esob.tdms.commonlogic.message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import kr.esob.tdms.commonlogic.value.RootAbsolutePath;

@Component
public class CommonMessageContainer {
	@Inject
	CommonMessageDao commonMessageDao;

	@Inject
	RootAbsolutePath rootAbsolutePath;

	@Autowired
	WebApplicationContext webApplicationContext;

	private List<Map<String, String>> messageList;

	@SuppressWarnings({ "rawtypes" })
	@PostConstruct
	public void init() throws Exception {
		String webRoot = webApplicationContext.getServletContext().getRealPath("/");
		if (webRoot == null || webRoot.trim().isEmpty()) {
			webRoot = new File(System.getProperty("java.io.tmpdir"), "kt1b-webroot").getAbsolutePath();
		}
		File messageDir = new File(webRoot, "messages");
		if (!messageDir.exists() && !messageDir.mkdirs()) {
			throw new IllegalStateException("메시지 디렉터리를 생성할 수 없습니다: " + messageDir.getAbsolutePath());
		}
		rootAbsolutePath.setRootAbsolutePath(new File(webRoot).getAbsolutePath() + File.separator);
		//생성 및 초기화
		messageList = new ArrayList<Map<String, String>>();

		List<CommonMessageVO> messageMapList 	 = commonMessageDao.selectMessageList();

		//메시지 properties 생성
		Map<String, Properties> propertiesByLanguage =
				new LinkedHashMap<String, Properties>();
		for (CommonMessageVO message : messageMapList) {
			String language = normalizeLanguageCode(message.getLangType());
			if (language == null || message.getLangCd() == null
					|| message.getLangDesc() == null) {
				continue;
			}
			Properties languageProperties = propertiesByLanguage.get(language);
			if (languageProperties == null) {
				languageProperties = new Properties();
				propertiesByLanguage.put(language, languageProperties);
			}
			languageProperties.setProperty(message.getLangCd(), message.getLangDesc());
		}

		Properties propsKo = propertiesByLanguage.get("ko");
		if (propsKo == null) {
			propsKo = new Properties();
		}
		store(propsKo, new File(messageDir, "message.properties"));
		store(propsKo, new File(messageDir, "message_ko.properties"));
		store(propsKo, new File(messageDir, "message_ko_KR.properties"));
		for (Map.Entry<String, Properties> entry : propertiesByLanguage.entrySet()) {
			if ("ko".equals(entry.getKey())) {
				continue;
			}
			store(entry.getValue(),
					new File(messageDir, "message_" + entry.getKey() + ".properties"));
		}

	}

	private String normalizeLanguageCode(String language) {
		if (language == null) {
			return null;
		}
		String normalized = language.trim().toLowerCase(Locale.ROOT);
		return normalized.matches("^[a-z]{2,3}$") ? normalized : null;
	}

	private void store(Properties properties, File target) throws Exception {
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target), "UTF-8")) {
			properties.store(writer, "message comment");
		}
	}

	/**
	 * 메시지 리스트
	 * @return
	 */
	public List<Map<String, String>> getMessageList() {
		return messageList;
	}

	/**
	 * 메시지를 가져온다
	 * @param messageCode
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getMessage(String messageCode){
		String messageName = "";
		Map selectMap = new HashMap();

		try{
			selectMap = (HashMap)messageList.get(0);
			messageName = selectMap.get(messageCode).toString();
		}catch(Exception e){
		}

		return messageName;
	}

	/**
	 * 메세지를 return
	 * @param messageCode 메세지 코드
	 * @param langCode 언어 코드
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getMessage(String messageCode, String langCode){

		//언어코드가 없을 경우[세션이 존재 하지 않는 상태...] default 설정
		if (langCode == null) {
			langCode = "ko";
		}

		String messageName = "";
		Map selectMap = new HashMap();

		try{
			selectMap = (HashMap)messageList.get(0);
			messageName = selectMap.get(messageCode).toString();
		}catch(Exception e){
		}

		return messageName;
	}

	/**
	 * 메세지의 '@'를 인자로 replace하여 return
	 * @param messageCode 메세지 코드
	 * @param langCode 언어 코드
	 * @param strReplace replace할 문자열
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public String getMessage(String messageCode, String langCode, String strReplace){

		//언어코드가 없을 경우[세션이 존재 하지 않는 상태...] default 설정
		if (langCode == null) {
			langCode = "ko";
		}

		String messageName = "";
		Map selectMap = new HashMap();

		try{
			selectMap = (HashMap)messageList.get(0);
			messageName = selectMap.get(messageCode).toString();

			String[] parsingArray = strReplace.split("▥");

			for(int i=0; i < parsingArray.length; i++) {
				if(messageName.indexOf("@") != -1){
					String s1 = messageName.substring(0, messageName.indexOf("@"));
					String s2 = messageName.substring(messageName.indexOf("@") + 1);
					messageName = s1 +  parsingArray[i] + s2;
				}
			}

		}catch(Exception e){
		}

		return messageName;
	}

}
