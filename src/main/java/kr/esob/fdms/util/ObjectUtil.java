package kr.esob.fdms.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import kr.esob.fdms.controller.bbs.qna.BbsQnaAddParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ObjectUtil {

	public static <T> Predicate<T> distinctByKey( Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new HashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public static Object jsonToObj(String json, Class rtn) {
		return JSONObject.toBean(JSONObject.fromObject(json.replaceAll("&quot;", "\"")), rtn);
	}

	public static Object jsonArrToList(String json, Class rtn) {
		return JSONArray.toList(JSONArray.fromObject(json.replaceAll("&quot;", "\"")), rtn);
	}
}
