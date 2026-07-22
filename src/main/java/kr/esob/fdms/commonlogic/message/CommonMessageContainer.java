package kr.esob.fdms.commonlogic.message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import kr.esob.fdms.commonlogic.value.RootAbsolutePath;

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
		rootAbsolutePath.setRootAbsolutePath(webApplicationContext.getServletContext().getRealPath("/"));
		//생성 및 초기화
		messageList = new ArrayList<Map<String, String>>();

		List<CommonMessageVO> messageMapList 	 = commonMessageDao.selectMessageList();

		//메시지 properties 생성
		Properties props   = new Properties();
		Properties propsKo = new Properties();
		Properties propsEn = new Properties();
		Properties propsJa = new Properties();
		Properties propsZh = new Properties();
		Iterator iterator = messageMapList.iterator();

		while (iterator.hasNext()) {
			CommonMessageVO commonMessageVO = (CommonMessageVO) iterator.next();
			if("en".equalsIgnoreCase(commonMessageVO.getLangType())){
				propsEn.setProperty(commonMessageVO.getLangCd(), commonMessageVO.getLangDesc());
			}else if("ja".equalsIgnoreCase(commonMessageVO.getLangType())){
				propsJa.setProperty(commonMessageVO.getLangCd(), commonMessageVO.getLangDesc());
			}else if("ko".equalsIgnoreCase(commonMessageVO.getLangType())){
				propsKo.setProperty(commonMessageVO.getLangCd(), commonMessageVO.getLangDesc());
			}else if("zh".equalsIgnoreCase(commonMessageVO.getLangType())){
				propsZh.setProperty(commonMessageVO.getLangCd(), commonMessageVO.getLangDesc());
			}else {
				props.setProperty(commonMessageVO.getLangCd(), commonMessageVO.getLangDesc());
			}
		}
//		props.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message.properties")), "UTF-8"), "message comment");
		propsKo.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message.properties")),"UTF-8"), "message comment");
		propsKo.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message_ko.properties")),"UTF-8"), "message comment");
		propsKo.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message_ko_KR.properties")),"UTF-8"), "message comment");
		propsEn.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message_en.properties")), "UTF-8"), "message comment");
		propsJa.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message_ja.properties")), "UTF-8"), "message comment");
		propsZh.store(new OutputStreamWriter(new FileOutputStream(new File(rootAbsolutePath.toString()+"/messages/message_zh.properties")), "UTF-8"), "message comment");


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
