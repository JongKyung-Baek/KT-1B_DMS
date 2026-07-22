package kr.esob.fdms.controller.login;

import java.util.Hashtable;

public class LoginManager {
	// 로그인 한 사용자들을 저장하는 변수
	public static Hashtable<String, String> loginUser = new Hashtable<>();

	/**
	 * 중복 로그인을 막기 위한 사용자 ID 검증
	 * @param userId
	 * @return
	 */
	public static boolean checkUsing(String userId, String ip) {
		if(null == loginUser.get(userId)) {
			return false;
		}

		if(ip.equals(loginUser.get(userId))) {
			return false;
		}
		else {
			//중복로그인 임시 해제 2023-03-15
			//return true;
			return false;
		}
	}
}
