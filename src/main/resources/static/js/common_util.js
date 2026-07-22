var viewer = undefined;
var keyword = {};
var keywordIndex = 0;

var agent = navigator.userAgent.toLowerCase();
//
//var isInternetExplorer;

function getIsInternetExplorer(){
	if(agent.indexOf("msie") != -1 || navigator.appName == 'Netscape' && navigator.userAgent.search('Trident') != -1){
		return true;
	}else{
		return false;
	}
}

org.owasp.esapi.ESAPI.initialize();

/**
 * 이미지 뷰어 생성.
 * @param id
 * @param bButton
 */
function fnInitViewer(id, bButton) {
//	if(undefined !== viewer) {
//		viewer.destroy();
//		viewer = undefined;
//	}

	viewer = new Viewer(document.getElementById(id), {
		  inline: true,
		  title: 0,
		  button: bButton,
		  toolbar: {
			  reset: 1,
			  zoomIn: 1,
			  zoomOut: 1,
			  rotateLeft: 1,
			  rotateRight: 1,
			  oneToOne: 0,
			  prev: 0,
			  play: {
				  show: 0
			  },
			  next: 0,
			  flipHorizontal: 0,
			  flipVertical: 0
		  },
		  zoomRatio: 0.5,
		  maxZoomRatio: 2,
		  minZoomRatio: 0.1,
		  navbar: false
		});
}


function trace(obj){
	var strHtml = "";
	strHtml += "<div>";
	strHtml += obj;
	strHtml += "</div>";

	$("body").append(strHtml);
}

/**
 * Object 가 null 또는 undefined 인지 여부를 검사한다.
 * 인자로 넘어온 오브젝트 중 하나라도 null 이면 true 를 리턴한다.
 *
 * @param arguments 를 이용하여 가변인자로 검사할 오브젝트 리스트를 받는다.
 * @author
 */
function isNull() {
	for(var i=0; i<arguments.length; i++) {
		var obj = arguments[i];
		if(obj == null || typeof(obj) == "undefined") {
			return true;
		}
		if (new String(obj).valueOf() == "undefined") return true;
		var strCheck = new String(obj);
		if (strCheck == null || strCheck.toString().length == 0 ) return true;
	}
	return false;
}

function isNotNull() {
	for(var i=0; i<arguments.length; i++) {
		var obj = arguments[i];
		var chk = isNull(obj);
		if(chk) {
			return false;
		}
	}
	return true;
}

/**
 * Object 의 값(value)이 비었는지 검사한다.
 * 파라미터로 여러개의 Object 를 검사할 때는 하나라도 empty 이면 true 리턴한다.
 * isEmpty(GRID) : binddataset 이 empty인지 검사
 * isEmpty(DATASET) : rowcount 가 0 인지 검사
 * isEmpty(String) : trim().length 가 0인지 검사
 * isEmpty(ARRAY) : length 가 0 인지 검사
 * isEmpty(object) : value 가 empty 인지 검사
 * isEmpty(null) : true 리턴
 *
 * @author
 */
function isEmpty() {
	for(var i=0; i<arguments.length; i++) {
		var obj = arguments[i];
		if(isNull(obj)) {
			return true;
		}
		var objType = getObjectType(obj);
		switch(objType) {
			case "Array":
				if(obj.length <= 0) {
					return true;
				}
				break;
			case "String":
				if(obj.trim().length <= 0) {
					return true;
				}
				break;
			case "Dataset":
				if(obj.rowcount <= 0) {
					return true;
				}
				break;
			case "Grid":
				if(obj.binddataset.rowcount <= 0) {
					return true;
				}
				break;
			case "Function":
			case "Boolean":
			case "Number":
				//이 아이들은 Pass
				break;
			default:
				//debug("[isEmpty] unknown type encountered : " + objType);
				if(isEmpty(obj.value)) {
					return true;
				}
		}
	}
	return false;
}

/**
 * not empty 여부 확인.
 */
function isNotEmpty() {
	for(var i=0; i<arguments.length; i++) {
		var obj = arguments[i];
		var chk = isEmpty(obj);
		if(chk) {
			return false;
		}
	}
	return true;
}

/**
 * decode 함수.
 */
function decode() {
	var varRtnValue = null;
	var arrArgument = arguments;
	var varValue    = arrArgument[0];
	var bIsDefault  = false;
	var nCount      = 0;
	if ((arrArgument.length % 2) == 0) {
		nCount     = arrArgument.length - 1;
		bIsDefault = true;
	} else {
		nCount     = arrArgument.length;
		bIsDefault = false;
	}
	for (var i = 1; i < nCount; i+=2) {
		if (varValue == arrArgument[i]) {
			varRtnValue = arrArgument[i+1];
			i = nCount;
		}
	}
	if (varRtnValue == null && bIsDefault) {
		varRtnValue = arrArgument[arrArgument.length-1];
	}
	return varRtnValue;
}

/**
 * 참 거짓을 팜별 하여 값 해당하는 값 리턴.
 */
function iif() {
	return (arguments[0]) ? arguments[1] : arguments[2];
}

/**
 * null 일경우 대체값을 리턴
 */
function nvl(value, nullValue) {
    return iif(isNull(value), nullValue, value);
}

/**
 * 문자열이 특정 문자열로 시작하는지 여부.
 */
function startsWith(str, prefix, offset) {
	if(isNull(str)) {
		return false;
	}
    if(isNull(offset)) {
        offset = 0;
    }
    return (str.indexOf(prefix, offset) == offset) ? true : false;
}

/**
 * 문자열이 특정 문자열로 끝나는지 여부.
 */
function endsWith(str, suffix) {
	var offset = (str.length) - (suffix.length);
    return startsWith(str, suffix, offset);
}

/**
 * 오브젝트 배열을 전달받아 {key=value} 쌍으로 이루어진 파라미터 문자열을 리턴한다.
 *
 * KEY 값 규칙 : 오브젝트 ID 에서 타입 접두어(prefix)를 제거하고 첫 단어를 소문자로 바꾼다.
 *         예): edSearchType => 'searchType', cbUserType => 'userType'
 *
 * 파라미터는 Funciton 의 arguments 객체를 사용하여 가변 인자를 지원한다.
 *         예) getParamString(edSearchType, cbUserType);
 *
 * @param arguments 오브젝트 배열
 * @return {name=value} 쌍으로 이루어진 파라미터 문자열
 * @author
 */
function getParamString() {
	var args = "";
	if(arguments.length > 0 && getObjectType(arguments[0]) == "String") {
		for(var i=0; i<arguments.length; i++) {
			if(i%2 == 0) {
				var key = arguments[i];
				args = args + key + "=";
			} else {
				if(typeof(arguments[i]) == "object") {
					if(isNotNull(arguments[i])) {
						args = args + wrapQuote(getObjectValue(arguments[i]))+" ";
					} else {
						args = args + wrapQuote("") + " ";
					}
				} else {
					args = args + wrapQuote(getObjectValue(arguments[i]))+" ";
				}
			}
		}
	} else {
		for(var i=0; i<arguments.length; i++) {
			var key = stripObjTypePrefix(arguments[i]);
			key = key.charAt(0).toLowerCase() + key.slice(1);
			var paramObj = getObjectValue(arguments[i]);
			// 파라미터 trim 처리 추가
			var trimValue = (getObjectType(paramObj)=="String") ? paramObj.trim() : paramObj;
		    args = args + key + "=" + wrapQuote(trimValue)+" ";
			//From - to 파라미터 추가
			if(arguments[i].toString() == "uCalendarPeriod" ||
			   arguments[i].toString() == "uCalMonthPeriod" ||
			   arguments[i].toString() == "uCalPeriodTime") {
				var values = getObjectValue(arguments[i]).split("|");
				args = args + key + "From" + "=" + wrapQuote(values[0]) + " ";
				args = args + key + "To" + "=" + wrapQuote(values[1]) + " ";
			}
		}
	}
    return args;
}

/**
 * id=value 페어로 구성된 문자열 연결 스트링에서 id에 해당하는 value를 return한다.
 *
 * @param paramString - id=value 문자열 연결 스트링
 * @param id - 구하고자 하는 id
 */
function getParamValue(paramString, id) {
	var state = "key";
	var arrKey = new Array();
	var arrValue = new Array();
	var buf = "";

	paramString = paramString.replace("\\\"", "&quot;");
    paramString = paramString.replace("\\\\", "&bslash;");

	for(var i=0; i<paramString.length; i++) {
		var ch = paramString.charAt(i);
		if(ch=="=") {
			if(state == "key") {
				arrKey[arrKey.length] = buf.trim();
				buf = "";
				state = "ready";
			} else {
				buf = buf + ch;
			}
		} else if(ch=="\"") {
			if(state == "ready") {
			    buf = "";
				state = "value";
			} else if(state == "value") {
				arrValue[arrValue.length] = buf.trim();
				buf = "";
				state = "key";
			} else {
				debugMessage("getParamValue : state transition error", "error");
			}
		} else {
			if(state != "ready") {
				buf = buf + ch;
			}
		}
	}

	for(var i=0; i<arrKey.length; i++) {
		if(arrKey[i] == id) {
			var rtn = arrValue[i];
			rtn = rtn.replace("&quot;", "\"").replace("&bslash;", "\\");
			return rtn;
		}
	}
	debugMessage("[getParamValue] could not found id["+id+"] from paramString ["+paramString+"]", "error");
}

/**
* 해당 PC의 오늘 날짜를 가져온다.
* @param
* @return{String} strToday	Date
*/
function toDay()
{
	var strToday = "";
	var objDate = new Date();
	var strToday  = objDate.getFullYear() + "";
	strToday += right("0" + (objDate.getMonth() + 1), 2);
	strToday += right("0" + objDate.getDate(), 2);

	return strToday;
}

/**
* 해당 Local 시스템의 DateTime을 가져온다.
* @param
* @return{String} strToday DateTime
*/
function toDateTime()
{
	var strToday = "";
	var objDate = new Date();
	var strToday  = objDate.getFullYear().toString().padLeft(4, "0");
	strToday += (objDate.getMonth() + 1).toString().padLeft(2, "0");
	strToday += objDate.getDate().toString().padLeft(2, "0");
	strToday += objDate.getHours().toString().padLeft(2, "0");
	strToday += objDate.getMinutes().toString().padLeft(2, "0");
	strToday += objDate.getSeconds().toString().padLeft(2, "0");

	return strToday;
}

/**
* 기능 : 해당월의 일수 구하기 - 숫자.
* 인수 : a_strDate		yyyyMMdd형태의 날짜 ( 예 : "20121122" )
* 리턴 : numLastDate 마지막 날짜 숫자값 ( 예 : 30 )
*/
function getLastDateNum(a_strDate)
{
    var numMonth, numLastDate;

	numMonth = parseInt(a_strDate.substr(4,2), 10);
    if( numMonth == 1 || numMonth == 3 || numMonth == 5 || numMonth == 7  || numMonth == 8 || numMonth == 10 || numMonth == 12 )
		numLastDate = 31;
    else if( numMonth == 2 )
    {
        if( isLeapYear(a_strDate) == true )
			numLastDate = 29;
        else
			numLastDate = 28;
    }
    else
		numLastDate = 30;

	return numLastDate;
}

/**
* 입력받은 전체 길이를 계산.
* 문자, 숫자, 특수문자 	: 1 로 Count
* 그외 한글/한자 			: 2 로 count 되어 합산한다.
* @param {String} value	원본 문자열
* @return{Number} v_cnt	입력받은 전체 길이
*/
function getLengB(sValue)
{
	if(new String(sValue).valueOf() == "undefined") sValue = "";

    var v_ChkStr = sValue.toString();
    var v_cnt = 0;

    for(var i=0; i<v_ChkStr.length; i++) {
        if(v_ChkStr.charCodeAt(i) > 127) {
            v_cnt += 3;
        } else {
            v_cnt += 1;
        }
    }

	return v_cnt;
}

/**
* 문자열의 오른쪽부분을 지정한 길이만큼 Return 한다.
* @param {String} strVal	오른부분을 얻어올 원본 문자열
* @param {Number} nSize 	얻어올 크기. [Default Value = 0]
* @return{String} rtnVal 	오른쪽 부분이 얻어진 문자열
*/
function right(strVal, nSize)
{
	if(isNull(strVal)) return "";
	var nStart = String(strVal).length;
    var nEnd = Number(nStart) - Number(nSize);
    var rtnVal = strVal.substring(nStart, nEnd);

    return rtnVal;
}

/**
* Date Formatting 함수.
* @param {String} sDate		원래 날짜
* @param {String} sFormat	포멧 형식 (Default : ####-##-##)
* @return{String}			변환된 날짜
*/
function getDateFormat(sDate, sFormat)
{
	if(isNull(sDate)) return "";
	if(isNull(sFormat)) sFormat = "####-##-##";

	var rtnForm = "";
	var j = 0;

	for (var i = 0; i < sFormat.length; i++) {
		if (sFormat.charAt(i) == "#") {
			rtnForm += sDate.charAt(j);
			j++;
		} else {
			rtnForm += sFormat.charAt(i);
		}
	}
	return rtnForm;
}


/**
* 날짜 형식이 맞는지 확인.
* @param {String } sDate	yyyyMMdd형태의 날짜 ( 예 : "20121122" )
* @return{Boolean}
*/
function isDate(sDate)
{
	sDate = sDate.replace(" ","");

    if(sDate.length != 8) { return false; }

    if(!isNumber(sDate)) { return false; }

	var nMonth  = parseInt(sDate.substring(4,6), 10);
	var nDate  = parseInt(sDate.substring(6,8), 10);

    if( nMonth < 1 || nMonth > 12 ) { return false; }

    if( nDate < 1 || nDate > getLastDateNum(sDate) ) { return false; }

    return true;
}

/**
* 시간 형식이 맞는지 확인.
* @param {String } sDate	hhmmss 형태의 날짜 ( 예 : "235959" )
* @return{Boolean}
*/
function isTime(strVal)
{
	var blnRtn = false;
	if(isNull(strVal)) return blnRtn;
	if(strVal.indexOf(" ") >= 0) return blnRtn;
	var nHour, nMin, nSec;

	nHour = nMin = nSec = -1;
	if(isNumber(strVal) && (strVal.length == 4 || strVal.length == 6)) {
		nHour = parseInt(strVal.substr(0, 2));
		nMin = parseInt(strVal.substr(2, 2));
		nSec = 0;
		if(strVal.length == 6) {
		  nSec = parseInt(strVal.substr(4));
		}
	}
	if(nHour >= 0 && nHour <= 23 && nMin >= 0 && nMin <= 59 && nSec >= 0 && nSec <= 59) {
		blnRtn = true;
	}
	return blnRtn;
// 	var objRegExpTm = /^([01][0-9]|[2][0-3])[0-5][0-9][0-5][0-9]$/g;
// 	return objRegExpTm.test(sTime);
}

/**
* 숫자 여부 체크.
* @param {String } value	입력 문자열
* @return{Boolean}
*/
function isNumber(sValue)
{
	var v_ChkStr = new String(sValue);
	var v_Bit;
	var v_Flag = "No";
	var v_ReturnValue = false;
	var v_LengthValue = v_ChkStr.length;

	if (isNull(v_ChkStr) == true) {
		v_Flag = "Yes";
		v_ReturnValue = false;
	}

	if (((v_ChkStr.split(".").length) > 2) && v_Flag == "No") {
		v_Flag = "Yes";
		v_ReturnValue = false;
	}

	if (v_Flag == "No") {
		for (var i=0; i<v_LengthValue ; i++) {
			v_Bit = v_ChkStr.substr(i,1);
			if (i == 0) {
				if(!isNaN(v_Bit) || (v_Bit == "-" && v_ChkStr.substr(0,1) != ".") || (v_Bit == "+"  && v_ChkStr.substr(0,1) != ".")) {
					v_ReturnValue = true;
				} else {
					v_ReturnValue = false;
					break;
				}
			} else {
				if(!isNaN(v_Bit) || (v_Bit == "." && v_ChkStr.substr(i,1).length != 0)) {
					v_ReturnValue = true;
				} else {
					v_ReturnValue = false;
					break;
				}
			}
	    }
	}

	return v_ReturnValue;
}

/**
* 영문 여부 체크.
* @param {String } value	입력 문자열
* @return{Boolean}
*/
function isAlpha(sValue)
{
	var v_ReturnValue = true;
	var v_TmpValue = "";

	if (isNull(sValue) == true) {
		v_ReturnValue = false;
	} else {
	    var v_CharPos = sValue.search("[^A-Za-z]");

	    if (v_CharPos >= 0) { v_ReturnValue = false; }
	}

	return v_ReturnValue;
}

/**
* 한글 여부 체크.
* @param {String } value	입력 문자열
* @return{Boolean}
*/
function isKor(sValue)
{
	var v_ReturnValue = true;

	if (isNull(sValue) == true) {
		v_ReturnValue = false;
	} else {
	    for (var i=0; i<sValue.length; i++) {
		    if(!((sValue.charCodeAt(i) > 0x3130 && sValue.charCodeAt(i) < 0x318F) || (sValue.charCodeAt(i) >= 0xAC00 && sValue.charCodeAt(i) <= 0xD7A3))) {
		        v_ReturnValue = false;
		        break;
		    }
		}
	}

	return v_ReturnValue;
}

/**
* 영문/숫자  체크.
* @param {String } value	입력 문자열
* @return{Boolean}
*/
function isAlphaNumber(sValue)
{
	var v_ReturnValue = true;
	var v_TmpValue = "";

	if (isNull(sValue) == true) {
		v_ReturnValue = false;
	} else {
	    for (var i=0; i<sValue.length; i++) {
		    if(((sValue.charCodeAt(i) >= 48 && sValue.charCodeAt(i) <=57) || (sValue.charCodeAt(i) >=65 && sValue.charCodeAt(i) <= 90) || (sValue.charCodeAt(i) >= 97 && sValue.charCodeAt(i) <= 122))) {

		    } else {
		        v_ReturnValue = false;
		        break;
		    }
		}
	}

	return v_ReturnValue;
}

/**
* 공통 주민번호 확인.
* @param {String } sValue	주민번호
* @return{Boolean}			적합/부적합
*/
function isRsrno(sValue)
{
    if ((sValue == null) || (isNull(sValue))) {
        //application.mainframe.childframe.form.alert("주민등록번호가 잘못되었습니다! [NULL]");
 //       gfn_messageOpen("SI2001","주민등록번호");
		return false;
    }
    var v_JuminNo = sValue.replace("-", "");
	var v_JuminChkDgt = [2,3,4,5,6,7,8,9,2,3,4,5];
	var v_FNum = new Number();
	var v_LNum = new Number();
	var v_iSum = new Number();
	var v_RtnVal;
	var v_YY;

	v_FNum = v_JuminNo.substr(0, 6).toString();
	v_LNum = v_JuminNo.substr(6).toString();

    if (v_LNum.substr(0,1) == '1' ||  v_LNum.substr(0,1) == '2') {
        v_YY  = '19';
    } else if (v_LNum.substr(0,1) == '3' ||  v_LNum.substr(0,1) == '4') {
        v_YY  = '20';
    } else {
//		gfn_messageOpen("SI1015","주민등록번호,[성별]");
//        application.mainframe.childframe.form.alert("주민등록번호가 잘못되었습니다! [성별]");
		return false;
    }

    // if (gfn_IsDate(v_YY + v_FNum) == false) {
        // alert("주민등록번호가 잘못되었습니다! [날짜]");
		// return false;
    // }

    if(v_JuminNo.length != 13) {
//		gfn_messageOpen("SI1015","주민등록번호,[자릿수]");
//        application.mainframe.childframe.form.alert("주민등록번호가 잘못되었습니다! [자릿수]");
        return false;
    }

    if (isNumber(v_JuminNo) == false) {
//		gfn_messageOpen("SI1015","주민등록번호,[숫자]");
//        application.mainframe.childframe.form.alert("주민등록번호가 잘못되었습니다! [숫자]");
		return false;
    }

	for (var ix = 0; ix < 12 ; ix++) {
		v_iSum += (parseInt(v_JuminNo.substr(ix, 1)) * v_JuminChkDgt[ix]);
	}

	v_iSum = 11 - (v_iSum%11);
	v_iSum = v_iSum % 10;
	if (v_iSum != (parseInt(v_JuminNo.substr(12, 1)))) {
	    // alert("주민등록번호가 잘못되었습니다! [검번]");
//		gfn_messageOpen("SI1015","주민등록번호,[검번]");
		return false;
	}
	return true;
}

/**
* 사업자 등록번호를 확인.
* @param {String } sValue	사업자 등록 번호
* @return{Boolean}			적합/부적합
*/
function isCompRegNo(sValue)
{
    var vCompNo = sValue.replace("-", "");
	var checkID = new Array(1, 3, 7, 1, 3, 7, 1, 3, 5, 1);
	var i, Sum=0, c2, remander;

	if (checkID.length < 10) return false; // 길이가 10자리가 아니면 false

	for (var i = 0; i <= 7; i++) Sum += checkID[i] * vCompNo.charAt(i);

	c2 = "0" + (checkID[8] * vCompNo.charAt(8));
	c2 = c2.substring(c2.length - 2, c2.length);

	Sum += Math.floor(c2.charAt(0)) + Math.floor(c2.charAt(1));

	remander = (10 - (Sum % 10)) % 10 ;

	if (Math.floor(vCompNo.charAt(9)) != remander) {
		//alert ("정확한 사업자 등록번호를 입력하세요");
		return false;
	} else {
		return true;
	}
}

/**
* 법인 등록번호를 확인.
* @param {String } sValue	법인 등록 번호
* @return{Boolean}
*/
function isCorpRegNo(sValue)
{
	var vCorpNo = sValue.replace("-", "");
	var checkID = new Array(1,2,1,2,1,2,1,2,1,2,1,2);
	var i, Sum=0, c2, remander;

	if (sValue.length < 13) return false; // 길이가 10자리가 아니면 false

	for (var i = 0; i <= 12; i++) Sum += checkID[i] * vCorpNo.charAt(i);

	remander = Sum / 10;
	var nIndex = remander.toString().indexOf(".");
	remander = remander.toString().substring(nIndex+1,nIndex+2);
	remander = 10 - remander;

	if (remander > 9 ) {
		remander = 0;
	}
	if (remander == sValue.substring(12, 13)) {
		return true;
	} else {
		return false;
	}
}

/**
* 윤년여부 확인.
* @param {String } strData	일자
* @return{Boolean}
*/
function isLeapYear(strDate)
{
    var v_RVal;
    strDate = parseInt(strDate.substring(0,4),  10);

    if ((strDate % 4) == 0) {
        if ((strDate % 100) != 0 || (strDate % 400) == 0) {
            v_RVal = true;
        } else {
            v_RVal = false;
        }
    } else {
        v_RVal = false;
    }

    return v_RVal;
}

/**
* 주민번호로 성별 구분.
* @param {String} strRsrno	주민등록 번호
* @return{String}			남자 'M' 여자 'W'
*/
function getSex(strRsrno)
{
    if (!(isRsrno(strRsrno)))
        return false;

    var vSexGb = strRsrno.substr(6,1);

    if (vSexGb == '1' || vSexGb == '3' || vSexGb == '5' || vSexGb == '7') {
        return "M";
    } else if (vSexGb == '2' || vSexGb == '4' || vSexGb == '6' || vSexGb == '8') {
        return "W";
    } else {
        return "X";
    }
}

/**
* 입력된 날자로부터 주의 시작일짜와 종료 일짜를 구함.
* @param {String} strDay	'yyyyMMdd' 형태로 표현된 날짜
* @return{String}			시작일짜,종료일짜 (배열로 값을 다시 던져줌)
*/
function getToWeek(strDay)
{
	var sYo = getDay(strDay)
	var startWeekday = addDate(strDay, -(parseInt(sYo)));
	var endWeekday = addDate(strDay, +(7-(parseInt(sYo)+1)));
	var strReturn = startWeekday + "," + endWeekday;
	return strReturn;
}

/**
* 입력된 날자로부터 요일을 구함.
* @param {String} strDate	'yyyyMMdd' 형태로 표현된 날짜
* @return{String}			요일에 따른 숫자
*							0 = 일요일 ~ 6 = 토요일. 오류가 발생할 경우 -1 Return
*/
function getDay(strDate)
{
    var date = new Date();
    date.setYear(strDate.substr(0, 4));
    date.setMonth(strDate.substr(4, 2) - 1);
    date.setDate(strDate.substr(6, 2));

    return date.getDay();
}

/**
* Array 의 같은 값을 가진 Index 를 Return.
* @param {Array}  arrVal	값을 검색할 Array
* @param {String} strVal	검색할 값
* @return{String}			Index (같은 값이 존재하지 않으면 -1 을 Return)
*/
function arrIndexOf(arrVal, strVal)
{
	var numRtn = -1;
	for(var i=0; i<arrVal.length; i++) {
		if(arrVal[i] == strVal) {
			numRtn = i;
			break;
		}
	}
	return numRtn;
}

/**
* 문자열의 부분 문자열이 뒤에서 나오는 문자 위치를 반환.
* @param {Array}  strOrg	부분문자열을 찾을 원본 문자열
* @param {String} strFind	검색할 값
* @param {number} numStart	검색할 뒤에서 시작위치
* @return{number}	Index (같은 값이 존재하지 않으면 -1 을 Return)
*/
function reversePos(strOrg, strFind, numStart)
{
	var numPos;
	var blnFind = false;
	if(isNull(strOrg) || isNull(strFind)) return -1;
	var strValue = String(strOrg);
	if(isNull(numStart)) numStart = strValue.length-1;
	for(numPos=numStart; numPos >= 0; numPos--) {
		if(strValue.indexOf(strFind, numPos)>=0) {
			blnFind = true;
			break;
		}
	}

	if(!blnFind) { numPos = -1; }
	return numPos;
}

/**
* 기   능:	Font Object 생성 반환.
* 인   수:	iFontSize
            sFontName
* Return : 	Font Object
*/
function utlf_getObjFont(iFontSize, sFontName)
{
	var objFont = new Font;
	objFont.size = iFontSize;
	objFont.name = sFontName;
	objFont.bold = true;
	return objFont;
}

/**
* 기   능:	1depth 메뉴 Text Size 반환.
* 인   수:	sText (사이즈를 계산할 텍스트 )
            objFont(Font정보를 가지고 있는 object입니다.)
            iLimitWidth (Option : word wrap이 일어나는 문자열 길이 제한 정수 값입)
            sConstWordWrapOption (Option : word wrap 옵션입니다)
* Return : 	계산된 사이즈가 저장된  Size object
*/
function utlf_getTextSize(sText, objFont, iLimitWidth, sConstWordWrapOption)
{
	var objPainter = this.canvas.getPainter();
	if(isNull(objPainter)==false)
	{
		var objTextSize = objPainter.getTextSize(sText, objFont);
		return objTextSize;
	}else
	{
		return false;
	}
}

/**
 * Desc				: 입력된 문자열의 좌우측 공백을 제거한 문자열을 반환.
 * Parameter		: arg(String)
 * Return 			: String
 */
function isTrim(arg)
{
	if (isNull(arg))
	    return "";

	//var sArg = arg.toString();
	var sArg = new String(arg);

	return sArg.replace(/(^\s*)|(\s*$)/g, "");
}

/**
 * Desc				: 입력된 숫자를 Number 포맷으로 변환.
 * Parameter		: numNumber(Number)
 * Return 			: String
 */
 function getNumFormat(numNumber)
 {
	if(isNull(numNumber)) return "";

	var strNum = String(numNumber);
	var strMinus = (strNum.substr(0, 1)=="-"?"-":"");
	if(!isNull(strMinus)) {
		strNum = strNum.replace("-","");
	}
	strNum += '';
	arrNum = strNum.split('.');
	if(arrNum.length>=1 && !isNumeric(arrNum[0])) return "";
	if(arrNum.length>=2 && !isNumeric(arrNum[1])) return "";

	var strInt = arrNum[0];
	var strDec = arrNum.length>1?'.'+arrNum[1]:'';
	var strRtn = /(\d+)(\d{3})/;
	while (strRtn.test(strInt)) {
		strInt = strInt.replace(strRtn, '$1' + ',' + '$2');
	}

 	return strMinus + strInt + strDec;
 }

/**
 * 두 날짜 사이의 개월수 개산.
 * Oracle의 months_between 과 동일한 결과를 추구
 * 예) getMonthBetween('20090101', '20090201'); // return 1
 *     getMonthBetween('20090102', '20090201'); // return 0
 */
function getMonthBetween(fromObj, toObj) {
    var v1 = getObjectValue(fromObj);
    var v2 = getObjectValue(toObj);
    //"20120101|20121230"
    if(v1.split("|").length == 2 && isNull(toObj)) {
		var periods = v1.split("|");
		v1 = periods[0];
		v2 = periods[1];
    }
    if(!isDate(v1) || !isDate(v2)) {
        debugMessage("[getMonthBetween] : not a valid date : " + v1 + ", " + v2);
        return 0;
    }

    var sign = 1;
    if(v1 > v2) { //swap
        var temp = v1;
        v1 = v2;
        v2 = temp;
        sign = -1;
    }

    var yy = parseInt(v2.substr(0,4)) - parseInt(v1.substr(0,4));
    var mm = parseInt(v2.substr(4,2)) - parseInt(v1.substr(4,2));
    var dd = parseInt(v2.substr(6,2)) - parseInt(v1.substr(6,2));

    var rtn = (yy * 12) + mm + iif(dd >= 0, 0, -1);
    return sign * rtn;
}

/**
 * 두 날짜 사이의 일수 개산.
 * 날짜 유효성이 올바르지 않다면, null 을 리턴한다.
 */
function getDayBetween(fromObj, toObj) {
    var v1 = getObjectValue(fromObj);
    var v2 = getObjectValue(toObj);
    //"20120101|20121230"
    if(v1.split("|").length == 2 && isNull(toObj)) {
		var periods = v1.split("|");
		v1 = periods[0];
		v2 = periods[1];
    }

    if(!isDate(v1) || !isDate(v2)) {
        debugMessage("[getDayBetween] : not a valid date : " + v1 + ", " + v2);
        return null;
    }

    var day1 = new Date(parseInt(v1.substr(0, 4)), parseInt(v1.substr(4, 2))-1, v1.substr(6, 2));
    var day2 = new Date(parseInt(v2.substr(0, 4)), parseInt(v2.substr(4, 2))-1, v2.substr(6, 2));

    return parseInt((day2 - day1)/(1000*60*60*24));
}

/*
 * 한글 문자열을 받아서 마지막에 받침이 있는 문장인지를 검사한다.
 * alert문을 만들때 조사 을(를)/은(는)을 선택하기 위해 만들었다.
 * 영문인경우 return false
 */
function isEndWithJaum(src) {
	var ch = src.charCodeAt(src.length-1);
	ch = ch-44032;
	var first = ch / 588;
	var temp = ch % 588;
	var third = temp % 28;
	if(third > 0) {
		return true;
	}else {
		return false;
	}
}

/**
 * edit component의 정규식을 이용한 유효성 검사.
 * @param obj - 검사할 component object 또는 value
 * @param regExp - 검사할 정규식 패턴
 */
function isValidRegExp(obj, pattern) {
    obj = getObjectValue(obj);
    if(isEmpty(obj)){
		return false;
    }
    return iif(obj.match(pattern), true, false);
}

/**
 * 6자리 숫자코드로 된 우편번호를 XXX-XXX 현태로 변환한다.
 */
function translateZipNo(zipCd) {
    zipCd = zipCd.replace("-", "").trim();
    if(isNumber(zipCd) && zipCd.length > 3) {
        return zipCd.substr(0, 3) + "-" + zipCd.substr(3);
    }
    return zipCd;
}

/**
 * 우편번호 숫자 6자리 또는 XXX-XXX 형태인지 검사한다.
 */
function isValidZipCd(obj) {
    obj = getObjectValue(obj);
    obj = translateZipNo(obj);
    return isValidRegExp(obj, RX_ZIP_CODE);
}

/**
 * IPv4 형태인지 검사한다.
 */
function isValidIPV4(obj) {
    obj = getObjectValue(obj);
    if(isEmpty(obj)){
		return false;
    }
    var rx = new RegExp(RX_IPV4, "ig");
    if(!rx.test(obj)) {
        return false;
    }

    //0-255 check - 이걸 정규식으로 하려면 저질식이 나옴.
    var chk = Split(obj, ".");
    for(var i=0; i<chk.length; i++) {
        var num = parseInt(toNumber(chk[i]));
        if(num > 255) {
            return false;
        }
    }

    return true;
}

/**
 * 주민번호 13자리(또는 대시(-)를 포함한 14자리) 검사.
 */
function isValidJuminNo(obj) {
    obj = getObjectValue(obj);

    if(isEmpty(obj)){
		return false;
    }

    var rx = new RegExp(RX_JUMIN_NO, "ig");
    if(!rx.test(obj)) {
        return false;
    }

    obj = obj.replace("-", "");

    var sum = 0;
    var num = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];

    var last = parseInt(obj.charAt(12));
    for(var i = 0; i < 12; i++) {
        sum += parseInt(obj.charAt(i)) * num[i];
    }

    return iif(((11 - sum % 11) % 10 == last), true , false);
}

/**
 * 외국인 등록번호 13자리 검사.
 */
function isValidAlienNo(obj) {
    obj = getObjectValue(obj);

    if(isEmpty(obj)){
		return false;
    }

    var rx = new RegExp(RX_ALIEN_NO, "ig");
    if(!rx.test(obj)) {
        return false;
    }

    obj = obj.replace("-", "");

    if((parseInt(obj.charAt(7)) * 10 + parseInt(obj.charAt(8))) % 2 != 0) {
        return false;
    }
    var sum = 0;
    var num = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];

    var last = parseInt(obj.charAt(12));
    for(var i = 0; i < 12; i++) {
        sum += parseInt(obj.charAt(i)) * num[i];
    }
    sum = ((11 - sum % 11) % 10);
    if( sum >= 10 ) sum -= 10;
    sum += 2;
    if( sum >= 10 ) sum -= 10;

    return iif(sum == last, true, false);

}

/**
 * 사업자번호 10자리 검사.
 */
function isValidBizNo(obj) {
    obj = getObjectValue(obj);

    if(isEmpty(obj)){
		return false;
    }

    var rx = new RegExp(RX_BIZ_NO, "ig");
    var rx2 = new RegExp(RX_BIZ_NO_HYPHEN, "ig");
    if(!rx.test(obj) && !rx2.test(obj)) {
        return false;
    }

    obj = obj.replace("-", "");

    var sum = parseInt(obj.charAt(0));
    var num = [0, 3, 7, 1, 3, 7, 1, 3];
    for(var i = 1; i < 8; i++) {
        sum += (parseInt(obj.charAt(i)) * num[i]) % 10;
    }
    sum += Math.floor(parseInt(obj.charAt(8)) * 5 / 10);
    sum += (parseInt(obj.charAt(8)) * 5) % 10 + parseInt(obj.charAt(9));
    return iif((sum % 10 == 0), true, false);
}

/**
 * 법인번호 13자리 검사(또는 대시('-')를 포함한 14자리 검사.
 */
function isValidCorpNo(obj) {
    obj = getObjectValue(obj);

    if(isEmpty(obj)){
		return false;
    }

    var rx = new RegExp(RX_CORP_NO, "ig");
    if(!rx.test(obj)) {
        return false;
    }

    obj = obj.replace("-", "");

    var sum = 0;
    var num = [1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2];
    var last = parseInt(obj.charAt(12));
    for(var i = 0; i < 12; i++) {
        sum += parseInt(obj.charAt(i)) * num[i];
    }
    return iif(((10 - sum % 10) % 10 == last), true, false);
}
/**
 * 전화번호 유효성 검사.
 * 09(9)-999(9)-9999 형태인지 검사한다. (지역번호 국번과 '-'(하이픈)을 포함한 전화번호)
 * 또는 하이픈 없이 지역번호 국번을 포함한 전화번호
 * 09(9)999(9)9999
 *
 */
function isValidPhoneNumber(obj) {
    if(!isValidRegExp(obj, RX_TEL_NO) &&  !isValidRegExp(obj, RX_TEL_NO_HYPHEN)
        && !isValidRegExp(obj, RX_TEL_NO_ARS) &&  !isValidRegExp(obj, RX_TEL_NO_ARS_HYPHEN) ) {
            return false;
    }
    return true;
}


/**
 * 팩스번호 유효성 검사.
 * 09(9)-999(9)-9999 형태인지 검사한다. (지역번호 국번과 '-'(하이픈)을 포함한 전화번호)
 * 또는 하이픈 없이 지역번호 국번을 포함한 전화번호
 * 09(9)999(9)9999
 *
 */
function isValidFaxNumber(obj) {
    if(!isValidRegExp(obj, RX_TEL_NO) &&  !isValidRegExp(obj, RX_TEL_NO_HYPHEN)) {
            return false;
    }
    return true;
}


/**
 * 휴대폰 전화번호 유효성 검사.
 * 099-(9)999-9999 형태인지 검사한다. (국번과 '-'(하이픈)을 포함한 전화번호)
 * 또는 하이픈 없이 국번을 포함한 전화번호
 * 099(9)9999999
 */
function isValidHPNumber(obj) {
    if(!isValidRegExp(obj, RX_HP_NO) &&  !isValidRegExp(obj, RX_HP_NO_HYPHEN)) {
        return false;
    }
    return true;
}

/**
 * YYYYMMDD 형태인지 검사한다.
 */
function isValidDate(obj) {
	obj = getObjectValue(obj);
	obj = sDate.replace(" ","").replace("-","");
    return isDate(obj);
}

/**
 * Email 주소 유효성 검사.
 */
function isValidEmail(obj) {
	var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
	return re.test(obj);
}

/**
 * 여권번호 검사.
 * 영문대문자1~2자리 + 숫자 7~8자리
 */
function isValidPassportNo(obj) {
    return isValidRegExp(obj, RX_PASSPORT_NO);
}


/**
 * 문자열에서 HTML 태그를 제거한다.
 */
function stripHTML(string){
	var strip = new RegExp();
	strip = /[\r][\n]/gi;
	var retString =  string.replace(strip, "");
	strip = /[<][b][r][>]/gi;
	retString =  retString.replace(strip, "\r\n");
	//strip = /[<][p][>]/gi;
	strip = new RegExp("[<][p][>]", "gi");
	retString =  retString.replace(strip, "");
	//strip = /[<][/][p][>]/gi;
	strip = new RegExp("[<][/][p][>]", "gi");
	retString =  retString.replace(strip, "\r\n");
	strip = /[<][^>]*[>]/gi;
	var retString =  retString.replace(strip, "");
	strip = /[&][a][m][p][;]/gi;
	retString =  retString.replace(strip, "&");
	strip = /[&][l][t][;]/gi;
	retString =  retString.replace(strip, "<");
	strip = /[&][g][t][;]/gi;
	retString =  retString.replace(strip, ">");
	strip = /[&][q][u][o][t][;]/gi;
	retString =  retString.replace(strip, '"');
	strip = /[&][#][0][3][9][;]/gi;
	retString =  retString.replace(strip, "'");
	strip = /[&][n][b][s][p][;]/gi;
	retString =  retString.replace(strip, " ");
	return retString;
}

/**
 * text를 html형식으로 변경한다.
 */
function textToHtml(textVal){
	var strip = new RegExp();
	var retString = "";
	strip = /[&]/gi;
	retString =  retString.replace(strip, "&amp;");
	strip = /[ ]/gi;
	retString =  retString.replace(strip, "&nbsp;");
	strip = /[\r][\n][<][b][r][>]/gi;
	retString =  textVal.replace(strip, "\r\n");
	strip = /[<][b][r][>][\r][\n]/gi;
	retString =  retString.replace(strip, "\r\n");
	strip = /[<][b][r][>]/gi;
	retString =  retString.replace(strip, "\r\n");
	strip = /[<]/gi;
	retString =  retString.replace(strip, "&lt;");
	strip = /[>]/gi;
	retString =  retString.replace(strip, "&gt;");
	strip = /[\n]/gi;
	retString =  retString.replace(strip, "\r\n");
	strip = /[\r][\n]/gi;
	retString =  retString.replace(strip, "<br />\r\n");
	strip = /["]/gi;
	retString =  retString.replace(strip, '&quot;');
	strip = /[']/gi;
	retString =  retString.replace(strip, "'");

	return retString;
}

/**
 * 오브젝트를 위/아래로 이동시킨다.
 *
 * @param obj 이동시킬 오브젝트(또는 배열)
 * @param nOffset 이동시킬 px 수
 * @param bResetScroll 이동 후 Form 의 scroll reset
 * @author
 */
function shiftDown(obj, nOffset, bResetScroll) {
	var arrObj = new Array();
	if(obj instanceof Array) {
		arrObj = obj;
	} else {
		arrObj[arrObj.length] = obj;
	}

	for(var i=0; i<arrObj.length; i++) {
		arrObj[i].move(arrObj[i].position.left, arrObj[i].position.top + nOffset);
	}

	if(isNull(bResetScroll) || bResetScroll) {
		var objForm = getRoot();
		debugObject(objForm);
		if(isNotNull(objForm)) {
			objForm.resetScroll();
		}
	}
}


/**
 * 배열을 순서을 왼쪽으로 이동.
 */
function toTheLeft(obj, nOffset) {
	var arrObj = new Array();
	if(obj instanceof Array) {
		arrObj = obj;
	} else {
		arrObj[arrObj.length] = obj;
	}
	for(var i=0; i<arrObj.length; i++) {
		arrObj[i].move(arrObj[i].position.left - nOffset, arrObj[i].position.top);
	}
}


/**
 * 배열을 순서을 오른쪽으로 이동.
 */
function toTheRight(obj, nOffset) {
	toTheLeft(obj, nOffset * -1);
}

/**
 * object 디버그.
 */
function objTrace(obj){
//	var s;
//	trace("[objTrace]-------start-------------------");
//	for(var x in obj){
//		trace("obj." + x + "=[" + obj[x] +"]");
//		s += "obj." + x + "=[" + obj[x] +"] / ";
//	}
//	trace("[objTrace]-------end-------------------");
//	return s;
}

/**
 * 확장자로 Image 파일인지 판단한다.
 */
function isImageFileExtension(strExtension) {
	if(isEmpty(strExtension)) {
		return false;
	}
	var imageExtensions = ["bmp", "dib", "jpg", "jpeg", "jpe", "jfif", "tif", "tiff", "png", "gif"];
	for(var i=0; i<imageExtensions.length; i++) {
		if(strExtension.toLowerCase() == imageExtensions[i]) {
			return true;
		}
	}
	return false;
}

function sleep(msecs) {
    var start = new Date().getTime();
    var cur = start;
    while (cur - start < msecs)
    {
        cur = new Date().getTime();
    }
}

function getDiffDate(sdate, sdiff, stype) {
	var tyy = sdate.substring(0,4);
	var tmm = sdate.substring(4,6) - 1;
	var tdd = "";

	if (stype == "M" || sdate.length==6) tdd = "01";
	else tdd = sdate.substring(6,8);

	currdate = new Date(tyy,tmm,tdd);

	switch (stype) {
		case "Y" :
			diffdate = new Date(currdate.getYear() + sdiff,currdate.getMonth(),currdate.getDate());
			break;
		case "M":
		case "MD" :
			diffdate = new Date(currdate.getYear(),currdate.getMonth() + sdiff,currdate.getDate());
			break;
		default  :
			diffdate = new Date(currdate.getYear(),currdate.getMonth(),currdate.getDate() + sdiff);
			break;
	}

	var tmpyy = diffdate.getYear();
	var ls_yy = (tmpyy > 99) ? tmpyy : 1900 + tmpyy;

	var tmpmm = diffdate.getMonth();
	var ls_mm = (tmpmm < 9)  ? "0" + (tmpmm + 1) : tmpmm + 1;

	var tmpdd = diffdate.getDate();
	var ls_dd = (tmpdd < 10) ? "0" + tmpdd : tmpdd;

	switch (stype) {
		case "M" :
			return ls_yy.toString() + ls_mm.toString();
		default  :
			return ls_yy.toString() + ls_mm.toString() + ls_dd.toString() ;
	}
}

function getDateDiff( num ) {
	var date = new Date();

	var year = date.getFullYear();
	var nmonth = date.getMonth();
	var day = date.getDate();
	var nextDay = date.getDate() + Number(num);

	date = new Date( year , nmonth , nextDay );

	var tmpYy  = date.getFullYear();
	var yy  = (tmpYy > 99)  ? tmpYy : 1900 + tmpYy;

	var tmpMon = date.getMonth();
	var mon = (tmpMon < 9)  ? "0" + (tmpMon + 1) : tmpMon + 1 ;

	var tmpDay = date.getDate();
	var day = (tmpDay < 10) ? "0" + tmpDay : tmpDay ;

	return yy.toString() + mon.toString() + day.toString() ;
}

/**
 * 24시간 기준 쿠키 설정하기
 * expiredays 후의 클릭한 시간까지 쿠키 설정
 * @param name
 * @param value
 * @param expiredays
 */
function setCookie( name, value, expiredays ) {
	var todayDate = new Date();
	todayDate.setDate( todayDate.getDate() + expiredays );
	document.cookie = name + "=" + escape( value ) + "; path=/; expires=" + todayDate.toGMTString() + ";"
}

/**
 * 00:00 시 기준 쿠키 설정하기
 * expiredays 의 새벽  00:00:00 까지 쿠키 설정
 * @param name
 * @param value
 * @param expiredays
 */
function setCookieAt00( name, value, expiredays ) {
	var todayDate = new Date();
	todayDate = new Date(parseInt(todayDate.getTime() / 86400000) * 86400000 + 54000000);
	if ( todayDate > new Date() )
	{
		expiredays = expiredays - 1;
	}
	todayDate.setDate( todayDate.getDate() + expiredays );
	document.cookie = name + "=" + escape( value ) + "; path=/; expires=" + todayDate.toGMTString() + ";"
}



/**
 * 팝업을 띄운다.
 * @param mypage
 * @param myname
 * @param w
 * @param h
 */
function openPopUp(mypage, myname, w, h) {
	var winl = (screen.width - w) / 2;
	var wint = (screen.height - h) / 2;
	var winprops = 'height='+h+',width='+w+',top='+wint+',left='+winl+',scrollbars=no,resizable=no';
	var popwin = window.open(mypage, myname, winprops);
	popwin.window.focus();
}

$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {

    	var name = this.name;
    	var index = this.name.indexOf("_select");

    	if(index > -1) {
    		name = this.name.substring(0, index);
    	}

        if (o[name] !== undefined) {
            if (!o[name].push) {
                o[name] = [o[name]];
            }
            o[name].push(this.value || '');
        } else {
            o[name] = this.value || '';
        }
    });
    return o;
};

jQuery.extend({
    stringify  : function stringify(obj) {

//        if ("JSON" in window) {
//            return JSON.stringify(obj);
//        }

        var t = typeof (obj);
        if (t != "object" || obj === null) {
            // simple data type
            if (t == "string") obj = '"' + obj + '"';

            return String(obj);
        } else {
            // recurse array or object
            var n, v, json = [], arr = (obj && obj.constructor == Array);

            for (n in obj) {

                v = obj[n];
                t = typeof(v);

                if (obj.hasOwnProperty(n)) {

                 if(null === v || undefined === v) {
                  v = "";
                 }

                    if (t == "string") {
                        v = '"' + v + '"';
                    } else if (t == "object" && v !== null){
                        v = jQuery.stringify(v);
                    }

                    json.push((arr ? "" : '"' + n + '":') + String(v));
                }
            }

            return (arr ? "[" : "{") + String(json) + (arr ? "]" : "}");
        }
    }
});

function getRequestJsonParam(obj) {
	return encodeURIComponent(JSON.stringify(obj));
}

function getRequestJsonParamForm(formId) {

	var o = $("#" + formId).serializeObject();

	if(undefined !== o["USER_PWD"] && null !== o["USER_PWD"]) {
		o["USER_PWD"] = Sha256.hash(o["USER_PWD"]);
	}

	if(undefined !== o["USER_PWD_CONFIRM"] && null !== o["USER_PWD_CONFIRM"]) {
		o["USER_PWD_CONFIRM"] = "";
	}

	return encodeURIComponent(JSON.stringify(o));
}

/**
 * select2를 생성하고 초기화 한다.
 * @param selectedId select2를 적용할 ID - #id 형태
 * @param {object} obj - option list
 * @param {string} selectedValue - default value
 * @param {number} width - select2's width
 * @param {boolean} isDisabled - disabled is true or not
 */
function initSelectData(selectedId, obj, selectedValue, width, isDisabled, maxSelection) {
    var $ts = $(selectedId);

    // width 설정
    if(undefined == width || null == width) {
        width = $(selectedId).css("width") != undefined ? $(selectedId).css("width") : "90%";
    }

    placeholder = g_msg('form.select');

    if(undefined != obj) {
    	obj.splice(0, 0, {id:'', text: placeholder});
    }

    //$ts.select2("destroy");

    var option = {
    		width: width,
//            placeholder: placeholder,		// 무조건 선택하세요가 들어가게 하고 싶을 때 사용
//            allowClear: true				// x 아이콘을 누르면 선택하세요가 나오게 하는 옵션
    }

    if(undefined !== $ts.attr("multiple")) {
    	option["placeholder"] = placeholder;
    }

    if(undefined != obj) {
    	option["data"] = obj;
    }

    if(undefined != maxSelection) {
    	option["maximumSelectionLength"] = maxSelection;
    }

    if( isDisabled != undefined && true === isDisabled) {
    	$ts.prop("disabled", true);
    }

    $ts.select2(option);

//    $ts.select2("container").css("display", "inline-block");

    if( selectedValue != undefined && "" !== selectedValue) {

    	var arrSelectedValue = selectedValue.split(",");

    	$ts.val(arrSelectedValue).trigger("change");
    }
}

//function initSelectRemoteData(selectedId, url, selectedValue, nullText) {
//	var $ts = $(selectedId);
//
//	// width 설정
//    width = $(selectedId).css("width") != undefined ? $(selectedId).css("width") : "90%";
//
//    var placeholder = $ts.attr("placeholder");
//
//    if(undefined === placeholder || "" === placeholder) {
//    	placeholder = g_msg('form.select');
//    }
//
//    $ts.select2({
//        width: width,
//        nullText: nullText,
//        minimumInputLength: 1,
////        placeholder: placeholder,
////        multiple : isMultiple,
//
//    	ajax: {
//    	    url: url,
//    	    delay: 250,
//    	    dataType: 'json'
//    	  }
//    });
////    if( selectedValue != undefined ) $ts.select2("val", selectedValue);
//
//    if( selectedValue != undefined && "" !== selectedValue) {
//    	$.ajax({
//    		url: "/manage/user/getUserName.json"
//			, type : "POST"
//			, cache: false
//			, data : {
//				"USER_CODE": selectedValue
//			}
//	    	, success : function(data){
//	    		var $newOption = $("<option></option>").val(selectedValue).text(data.USER_NAME);
//	    		$ts.append($newOption);
//	    	}
//	    	,error : function(e){
//				if(e.status==401){
//					parent.parent.location.href = CONTEXT_PATH+"/login/logout.login?statusCode="+e.status;
//				}else{
//					alertMessage(g_msg("msg.error") + '[' + e + ']');
//				}
//	    		alertMessage(g_msg('label.fail'));
//	    	}
//    	});
//    }
//    else {
//    	var $newOption = $("<option ></option>").val("").text(g_msg('form.select'));
//    	$ts.append($newOption);
//
//    	$ts.val([""]).trigger("change");
//    }
//
//
//}

/**
 * 숫자의 경우 첫번째 자리가 0인지 찾기 위한 함수. 001, 0001 등을 찾기 위함. 0이 아닌 수일 때 0으로 시작한다면 true를 리턴한다.
 * @param {*} n
 * @returns {Boolean}
 * @author younjh
 */
function isFirstIndexZero(n) {
	var str = n.toString();

	if(1 < str.length && "0" == str[0] && "." != str[1]) return true;

	return false;
};

/**
 * 숫자 형태의 문자열인지 확인한다.
 * @param n
 * @returns
 */
function isNumberString(n) {
	var regex = /^-?[0-9]*$/;

	s = (typeof n === 'number') ? n.toString() : n;

	return regex.test(s)
}

/**
 * 정수인지 확인한다.
 * @param n
 * @returns
 */
function isInteger(n) {
	var s = undefined;
	var regex = /^-?[0-9]*$/;

	s = (typeof n === 'number') ? n.toString() : n;

	return regex.test(s) && !isFirstIndexZero(s) && !isNaN(parseInt(s, 10)) && isFinite(s);
};

/**
 * POS ON/OFF 상세 팝업
 */
function fnRealtimeOnOffView() {
	fnOpenDialogPopup('/search/attack/onOffRealtimeView.do', 650, 710, {
		BACKUP_SEQID: ''
	});
}

/**
 * 쿠키 설정
 * @param cName
 * @param cValue
 * @param cDay
 */
function setCookie(cName, cValue, cDay){
    var expire = new Date();
    expire.setDate(expire.getDate() + cDay);
    cookies = cName + '=' + escape(cValue) + '; path=/ '; // 한글 깨짐을 막기위해 escape(cValue)를 합니다.
    if(typeof cDay != 'undefined') cookies += ';expires=' + expire.toGMTString() + ';';
    document.cookie = cookies;
}

/**
 * 쿠키 가져오기
 * @param cName
 * @returns
 */
function getCookie(cName) {
    cName = cName + '=';
    var cookieData = document.cookie;
    var start = cookieData.indexOf(cName);
    var cValue = '';
    if(start != -1){
        start += cName.length;
        var end = cookieData.indexOf(';', start);
        if(end == -1)end = cookieData.length;
        cValue = cookieData.substring(start, end);
    }
    return unescape(cValue);
}

/**
 * 필수 입력값이 정수값인지 체크
 * @param id
 * @param msgId
 * @returns
 */
function isValidDataInteger(id, msgId) {
	if(undefined === msgId) {
		msgId = id;
	}

	return isValidObjInteger($("#" + id), msgId);
}

function isValidObjInteger($o, msgId) {
	if(!isInteger($o.val())) {
		alertMessage(g_msg("msg.invalidInteger", g_msg("label.countControl")), function() {
			$(this).dialog('close');
			$o.focus();
		});

		return false;
	}

	return true;
}

function fnGetRadioTag(id, list, selectedValue) {

	var jsonParam = {
			id: id
			, list: list
			, selectedValue: selectedValue
	};

	var result = '';

	$.ajax({
		url: '/common/tag/getRadioTag.json?jsonParam=' + encodeURIComponent(JSON.stringify(jsonParam)),
		type: 'POST',
		//method: 'POST',
		//contentType : contentType,
		async: false,
	}).done(function(v){
		result = $.trim(v);
	}).fail(function(jqXHR, strMessage){
	  	if(jqXHR.status==401){
			location.href = CONTEXT_PATH+"/login/logout.login?statusCode="+e.status;
		}else{
			alertMessage(g_msg("msg.error") + '[' + e + ']');
		}
		return false;
	});

	return result;
}

/**
 * 공통 저장 함수 - 파일 업로드 아닌 경우 - 기존에는 grid에서 delete하는 방식이었으나 popup에서 delete하도록 코드 변경 [2019.02.08]
 * @param gridId
 * @param formId
 * @param code - ex) POSI_CODE
 * @param saveUrl
 * @param listUrl
 */
function commonDelete(gridId, formId, code, saveUrl, listUrl) {
	var param = {
			saveFlag: "D"
	};

	param[code] = $("input[name=" + code + "]").val();

	confirmMessage(g_msg("msg.delete"), function() {

		$("#confirmMessage").dialog("close");

		$.ajax({
			url: saveUrl + "?jsonParam=" + encodeURIComponent(JSON.stringify(param))
			, type : "POST"
			, cache: false
			, success : function(data){
				if(data.result == 'fail'){
					alertMessage(data.reason);
				}else{
					infoMessage(g_msg("msg.deleteComplete"), function() {
						parent.searchList(gridId, formId, listUrl);
						parent.closePopupDialog();
					});
				}
			}
			,error : function(e){
				if(e.status==401){
					parent.parent.location.href = CONTEXT_PATH+"/login/logout.login?statusCode="+e.status;
				}else{
					alertMessage(g_msg("msg.error") + '[' + e + ']');
				}
			}
		});
	});

//	var selrow = $("#" + gridId).jqGrid('getGridParam', "selarrrow" );
//	var i=0;
//
//	if(selrow.length == 0) {
//		alertMessage(g_msg("msg.noSelectedData"));
//		return;
//	}
//
//	var arrCode = [];
//
//	for(i=0; i<selrow.length; i++) {
//		arrCode.push($("#" + gridId).jqGrid("getRowData", selrow[i])[code]);
//	}
//
//	var param = {saveFlag: "D"};
//	param[code] = arrCode.join(",");
//	param["selrow"] = selrow.join(",");
//
//	if('function' === typeof checkUsed) {
//		if(false === checkUsed(param)) {
//			return;
//		}
//	}
//
//	confirmMessage(g_msg("msg.delete"), function() {
//
//		$("#confirmMessage").dialog("close");
//
//		$.ajax({
//			url: CONTEXT_PATH + saveUrl + "?jsonParam=" + encodeURIComponent(JSON.stringify(param))
//			, type : "POST"
//			, cache: false
//			, success : function(data){
//				if(data.result == 'fail'){
//					alertMessage(data.reason);
//				}else{
//					infoMessage(g_msg("msg.deleteComplete"), function() {
//						searchList(gridId, formId, listUrl);
//						closeAlertMessage();
//					});
//				}
//			}
//			,error : function(){
//				alertMessage(g_msg("msg.cantDelete"));
//			}
//		});
//	});
}

/**
 * 선택 팝업에서 메인 창에 값을 setting한다.
 * @param callbackCode
 * @param callbackText
 * @param code
 * @param text
 */
function setCallbackCode(callbackCode, code, callbackText, text) {
	$("#" + callbackCode).val(code);
	$("#" + callbackText).val(text);
}

/**
 * radio / check 를 체크한다.
 * @param inputName
 * @param value
 * @param checked
 */
function checkInput(inputName, value, checked) {
	$("input[name=\"" + inputName + "\"][value=\"" + value + "\"]").each(function() {
		if(checked) {
			$(this).prettyCheckable("check");
		}
		else {
			$(this).prettyCheckable("uncheck");
		}
	});
}

/**
 * 천단위 , 넣기
 * @param n
 * @returns
 */
function commify(n) {
	var reg = /(^[+-]?\d+)(\d{3})/;   // 정규식
	n += '';                          // 숫자를 문자열로 변환

	while (reg.test(n))
		n = n.replace(reg, '$1' + ',' + '$2');

	return n;
}

/**
 * 팝업창 tooltip 추가
 * @param title
 */
function fnSetDialogPopupTooltip(title) {
	if(null == title || '' == title){
		$('#popupTitle').hide();
	}
	$('#popupTitle > span').text(title);
}

/**
 * 비밀번호 생성 규칙
 * 8자 이상이어야 하고, 숫자/영어/특수문자를 모두 포함해야 함
 * @param pwd
 * @returns
 */
function checkPasswordRule(pwd) {
	var reg = /^(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/;

	return reg.test(pwd);
}


/**
* 특수 문자 입력 방지
*
*/
function checkSaveNameRule(str){
	var regExp = /[\/?.:|\*`\<>\\'\"]/gi

	if(regExp.test(str)){
		alertMessage(g_msg('msg.neverInputSpecialChar'));
		return true;
	}else{
		return false;
	}
}

/**
 *
 * @param formId
 * @returns
 */
function getFormData(formId) {
	return $("#" + formId).serializeObject();
}

/**
 *
 * @param formId
 * @returns
 */
function getJsonParamForm(formId) {

	var o = $("#" + formId).serializeObject();

	if(undefined !== o["USER_PWD"] && null !== o["USER_PWD"]) {
		o["USER_PWD"] = Sha256.hash(o["USER_PWD"]);
	}

	if(undefined !== o["USER_PWD_CONFIRM"] && null !== o["USER_PWD_CONFIRM"]) {
		o["USER_PWD_CONFIRM"] = "";
	}

	return encodeURIComponent(JSON.stringify(o));
}


function getJsonParamFormNotEnc(formId) {

	var o = $("#" + formId).serializeObject();

	if(undefined !== o["USER_PWD"] && null !== o["USER_PWD"]) {
		o["USER_PWD"] = Sha256.hash(o["USER_PWD"]);
	}

	if(undefined !== o["USER_PWD_CONFIRM"] && null !== o["USER_PWD_CONFIRM"]) {
		o["USER_PWD_CONFIRM"] = "";
	}

	return JSON.stringify(o);
}

function initSelectRemoteData(selectedId, url, selectedValue, nullText, maxSelection, dropdownParent) {
	var $ts = $(selectedId);
	// width 설정
    width = $(selectedId).css("width") != undefined ? $(selectedId).css("width") : "90%";

    var placeholder = $ts.attr("placeholder");

    if(undefined === placeholder || "" === placeholder) {
    	placeholder = "선택하세요";
    }

    if(undefined != maxSelection) {
    	option["maximumSelectionLength"] = maxSelection;
    }

    if(undefined === dropdownParent) {
    	dropdownParent = $('#popupDialoga');
    }
    else {
    	dropdownParent = $(dropdownParent);
    }

    $ts.select2({
    	language: 'ko',
        width: width,
        //nullText: nullText,
        //minimumInputLength: 1,
        placeholder: placeholder,
        //maximumSelectionLength: maxSelection,
        dropdownParent: dropdownParent,
        //dropdownParent: $ts.parent(),
//        multiple : isMultiple,

    	ajax: {
    	    url: url,
    	    delay: 250,
    	    dataType: 'json'
    	  }
    });
//    if( selectedValue != undefined ) $ts.select2("val", selectedValue);

    if( selectedValue != undefined && "" !== selectedValue) {
    	var arr = $.parseJSON(selectedValue);
    	var selectedCode = [];
    	for(var i=0; i<arr.length; i++){
    		var $newOption = $("<option></option>").val(arr[i].value).text(arr[i].text);
			$ts.append($newOption);
			selectedCode.push(arr[i].value);
    	}
//    	var value = "PCDEPT_000000001,PCDEPT_000000002";
//    	var arrVal = value.split(",");
    	$ts.val(selectedCode).trigger("change");
//    	var arrSelectedValue = selectedValue.split(",");

    } else {
//    	var $newOption = $("<option ></option>").val("").text("선택하세요");
//			$ts.append($newOption);

			$ts.val([""]).trigger("change");
    }
}

/**
 * searchType을 변경했을 때 우측 검색 조건을 변경한다.
 * @param ts
 * @returns
 */
function changeSearchType(ts) {
	var $ts = $(ts);

	$("ul.searchOptionBar li.item").each(function() {
		var cls = $(this).attr("class");

		if(cls.indexOf("hide") == -1 && cls.indexOf("startDate") == -1  && cls.indexOf("btnAdd") == -1
				&& cls.indexOf("startTime") == -1  && cls.indexOf("searchType") == -1 && cls.indexOf("andOr") == -1) {
			$(this).addClass("hide");
		}
	});

	var key = $ts.val();

	switch(key) {
	case "editTime":
	case "regTime":
		$("li." + key.substring(0, key.indexOf('Time')) + "StartTime").removeClass("hide");
		break;
	case "distributeDate" :
	case "validStartDate":
	case "validEndDate":
		$("li." + key.substring(0, key.indexOf('Date')) + "StartDate").removeClass("hide");
		break;
	case "pcdTotalCount":
		$('li.ruleId').removeClass("hide");
		var $li = $("li." + $("#searchType_select").val());
		var $input = $li.find("input");
		setIntegerInput($input.attr('id'), 4);
	case "pcdTotalCountOcr":
		$('li.ruleId').removeClass("hide");
		var $li = $("li." + $("#searchType_select").val());
		var $input = $li.find("input");
		setIntegerInput($input.attr('id'), 4);
//		break;
		default:
			$("li." + $ts.val()).removeClass("hide");
	}
}

/**
 * 키워드 추가
 * @returns
 */
function addKeyword() {

	if($("#keywordBar > li").length >= 10) {
		alertMessage(g_msg("msg.limitSearchCondition"));
		return;
	}

	var v = [];
	var ruleId = "";
	v.push("[" + $("#andOr_select option:selected").text() + "]");
	v.push(" " + $("#searchType_select option:selected").text());

	var andOrValue = $("#andOr_select").val();

	var $li = undefined;
	var searchType = $("#searchType_select").val();
	if('editTime' === searchType || 'regTime' === searchType) {
		$li = $("li." + searchType.substring(0, searchType.indexOf('Time')) + "StartTime");
	}
	else if('validStartDate' === searchType || 'validEndDate' === searchType || 'distributeDate' === searchType) {
		$li = $("li." + searchType.substring(0, searchType.indexOf('Date')) + "StartDate");
	}
	else if('pcdTotalCount' === searchType || 'pcdTotalCountOcr' === searchType) {	//개인정보 갯수
		if($(".ruleId ")){
			ruleId = $("#ruleId_select").val();
			v.push(" " + $(".ruleId option:selected").text());
			$li = $("li." + $("#searchType_select").val());
		}
	}
	else {
		$li = $("li." + $("#searchType_select").val());
	}

	var $select = $li.find("select");
	var selectedText = $li.find("select option:selected").text();
	var $input = $li.find("input");

	if('pcdTotalCount' === searchType || 'pcdTotalCountOcr' === searchType) {	//개인정보 갯수
		if( (null === $input.val()) || ("" === $input.val())){
			alertMessage('값을 입력해주시 바랍니다.');
			return;
		}
	}

	var keywordValue = {
			selectType: $("#searchType_select").val(),
			andOr: andOrValue,
			select: "",
			input: "",
			ruleId : ruleId
	};

	if($select.length > 0) {
		v.push(" " + selectedText);
		keywordValue.select = $select.val();
	}

	if(1 === $input.length) {
		v.push(" " + $input.val());
		keywordValue.input = $input.val();
	}
	else if(2 === $input.length) {
		var fromTo = $($input[0]).val()+"~"+$($input[1]).val();
		v.push(" " + fromTo);
		keywordValue.input = fromTo;
	}


	if('remainedCountMono' === searchType ){
		if('Y' == $select.val()){
			keywordValue.select = 'lt';
			keywordValue.input = '0';
		}else if('N' == $select.val()){
			keywordValue.select = 'gt';
			keywordValue.input = '0';
		}

	}

	var sameObject = false;

	$.each(keyword, function() {
		if(JSON.stringify(this) === JSON.stringify(keywordValue)) {
			sameObject = true;
		}
	});

	if(!sameObject) {
		keyword["keyword_" + keywordIndex] = keywordValue;

		$("#keywordBar").append(
				"<li class=\"andOr-" + andOrValue + "\" id=\"keyword_" + keywordIndex + "\" title=\"" + v.join('') + "\">"
				+ "<a class=\"keyword-remove\" href=\"javascript: removeKeyword('keyword_" + keywordIndex + "')\">x</a>"
				+ "<a class=\"keyword-text\" href=\"javascript: changeAndOr('keyword_" + keywordIndex + "')\">" + v.join('') + "</a>"
				+ "</li>");

		keywordIndex++;

		resizeContentWrap();
	}
}

/**
 * content 영역의 높이를 조절한다.
 * @returns
function resizeContentWrap() {
	var mhp = $("#pageBody .contentWrap").innerHeight() - $("#pageBody .contentWrap").height();
	$("#pageBody .contentWrap").css('height', ($(window).height() - $("#header").outerHeight(true) - $("#navigation").outerHeight(true) - $(".searchFormContainer").outerHeight(true) - 3) + "px");
	$(".ui-jqgrid-bdiv").css('height', ($(".gridContainer").height() - $(".ui-jqgrid-hdiv").outerHeight(true) - $(".ui-jqgrid-sdiv").outerHeight(true) - $(".ui-jqgrid-pager").outerHeight(true)) + "px");
	$(".frozen-bdiv.ui-jqgrid-bdiv").css('height', ($(".ui-jqgrid-bdiv").height() - 17) + "px");
}
*/


/**
 * 키워드를 삭제한다.
 * @param key
 * @returns
 */
function removeKeyword(key) {
	$("#" + key).remove();
	delete keyword[key];

	resizeContentWrap();
}

/**
 * keyword 클릭 시 and/or 토글
 * @param key
 * @returns
 */
function changeAndOr(key) {
	var $ts = $("#" + key);
	var $text = $ts.find(".keyword-text");
	var text = $text.text();

	if($ts.attr("class").indexOf("andOr-and") > -1) {
		$ts.removeClass("andOr-and").addClass("andOr-or");
		$text.text(text.replace("[AND]", "[OR]"));
		keyword[key].andOr = "or";
	}
	else if($ts.attr("class").indexOf("andOr-or") > -1) {
		$ts.removeClass("andOr-or").addClass("andOr-and");
		$text.text(text.replace("[OR]", "[AND]"));
		keyword[key].andOr = "and";
	}

	resizeContentWrap();
}

/**
 * 숫자 자릿수 제한
 * @param id
 * @param size
 * @returns
 */
function setIntegerInput(id, size){
	var maskStr = "";
	for(var i=1; i<=size; i++){
		maskStr += "Z";
	}
	$("#"+id).mask(maskStr, {	//컴서버 포트
	    translation: {
	        'Z': {
	            pattern: /[0-9]/,
	            optional: true
	        },
	    }
	});
}

/**
 * 숫자 값 제한
 * @param id
 * @param value
 * @returns
 */
function setIntegerInputLimitValue(id, value) {
	var len = ('number' === typeof value ? value + '' : value);

	var maskStr = "";
	for(var i=1; i<=len; i++){
		maskStr += "Z";
	}
	$("#"+id).mask(maskStr, {
	    translation: {
	        'Z': {
	            pattern: /[0-9]/,
	            optional: true
	        },
	    },
	    onKeyPress: function(val, e, el, options) {
	        var match = val.match(/[0-9]+$/);
	        if (match) {
	            var v = parseInt(match[0]) || 0;
	            $(el).val(val.substr(0, match.index) + ((v > value) ? value : v));
	        }
	    }
	});
}

/**
 * input의 입력 값을 min ~ max 사이의 실수값으로 제한
 * @param id
 * @param min
 * @param max
 * @param dot - 소숫점 자리수
 * @returns
 */
function setDoubleInputLimitValue(id, min, max, dot) {
	var len = ('number' === typeof max ? max + '' : max);

	var maskStr = "";
	for(var i=1; i<=len; i++){
		maskStr += "Z";
	}

	if(dot > 0) {
		maskStr += '.';
		for(var i=1; i<=dot; i++){
			maskStr += "0";
		}
	}

	$("#"+id).mask('B' + maskStr, {
	    translation: {
	        'Z': {
	            pattern: /[0-9]/,
	            optional: true
	        },
	        'B': {
	            pattern: /[0-9\-]/,
	            optional: true
	        }
	    },
	    onKeyPress: function(val, e, el, options) {
	        var match = val.match(/[0-9.\-]+$/);

	        if('-' === val) {
	        	return;
	        }

	        if (match) {
	            var v = parseFloat(match[0]) || 0;

	            $(el).val((v >= max) ? max : v <= min ? min : val);
	        }
	    }
	});
}

/**
 * 객체에 IP mask를 적용한다.
 * @param $obj - ex) $("#objID")    $(".objClass")
 * @returns
 */
function setIpMask($obj) {
	$obj.mask('0ZZ.0ZZ.0ZZ.0ZZ', {
		translation: {
			'Z': {
				pattern: /[0-9]/,
				optional: true
			},
		},
		onKeyPress: function(val, e, el, options) {
			var match = val.match(/[0-9]+$/);
			if (match) {
				var v = parseInt(match[0]) || 0;
				$(el).val(val.substr(0, match.index) + ((v > 255) ? 255 : v));
			}
		}
	});
}

/**
 * 업로드한 파일결과값을 WordArray로 변환
 * @param ab
 * @returns
 */
function arrayBufferToWordArray(ab) {
	  var i8a = new Uint8Array(ab);
	  var a = [];
	  for (var i = 0; i < i8a.length; i += 4) {
	    a.push(i8a[i] << 24 | i8a[i + 1] << 16 | i8a[i + 2] << 8 | i8a[i + 3]);
	  }
	  return CryptoJS.lib.WordArray.create(a, i8a.length);
}

/**
 * XSS 방지
 * @param strTemp [필수] 크로스사이트 스크립팅을 검사할 문자열
 * @param level [옵션] 검사레벨
 * 			0 (기본) -> XSS취약한 문자 제거
 * 			1 (선택) -> 단순한 <, > 치환
 * @returns
 */
function changeXSS(strTemp, level) {
	if ( level == undefined || level == 0 ) {
		strTemp = strTemp.replace(/\<|\>|\"|\'|\%|\;|\(|\)|\&|\+|\-/g,"");
	}
	else if (level != undefined && level == 1 ) {
		strTemp = strTemp.replace(/\</g, "&lt;");
		strTemp = strTemp.replace(/\>/g, "&gt;");
	}
	return strTemp;
}

/**
 * XSS 방지
 * @param v
 * @returns
 */
function encodeForHTMLAttribute(v) {
	return $ESAPI.encoder().encodeForHTMLAttribute(v);
}

/**
 * XSS 방지
 * @param v
 * @returns
 */
function encodeForHTML(v) {
	return $ESAPI.encoder().encodeForHTML(v);
}

/**
 * XSS 방지
 * @param v
 * @returns
 */
function encodeForJavaScript(v) {
	return $ESAPI.encoder().encodeForJavaScript(v);
}

/**
 * XSS 방지
 * @param v
 * @returns
 */
function encodeForURL(v) {
	return $ESAPI.encoder().encodeForURL(v);
}

function initTree(id, list, selectedValue, openLevel, dragDrop, maxDepth, useCheckbox, textMaxLength) {
	var getTypes = function(id, dragDrop, maxDepth) {
		var obj = undefined;

		if("menuList" === id && dragDrop) {
			obj = {
				'#' : {
					'valid_children' : ['root']
				},
				'root' : {
					'valid_children' : ['branch', 'leaf']
				},
				'branch' : {
					'valid_children' : ['leaf']
				},
				'leaf' : {
					'valid_children' : []
				}
			}
    	}
		else {
			obj = {};
		}

		if(maxDepth > -1) {
    		obj['#'] = {'max_depth': maxDepth };
    	}

		return obj;
	},
	getPlugins = function(dragDrop, useCheckbox){
//		var plugins = ["search", "wholerow"]
//
//		if(dragDrop) {
//			plugins.push("dnd");
//			plugins.push("types");
//		}
//
//		if(useCheckbox) {
//			plugins.push("checkbox");
//		}
		var plugins = ["checkbox"]

		return plugins;
	},
	initList = function(list, openLevel) {
		$.each(list, function() {
			var level = parseInt(this.level, 10);

			this.state = {
					opened: false,
					hidden: false
			};

			if(0 == openLevel || level <= openLevel || 0 == level) {
				this.state.opened = true;
			}
			else {
				this.state.opened = false;
			}

			if(!this.visible) {
				this.state.hidden = true;
			}else {
				this.state.hidden = false;
			}
		});
	},
	getCore = function(id, list, selectedValue, openLevel, dragDrop, maxDepth, useCheckbox, textMaxLength) {
		initList(list, openLevel);

		var core = {
				textMaxLength: textMaxLength,
				'multiple': false,
				animation: 0,
				data: list
		};

		if(dragDrop) {
			if("menuList" === id || "deptTree" === id) {
				core['check_callback'] = function(operation, node, node_parent, node_position, more){
					if(operation === 'move_node'){
						if(node_parent.id === '#'){
							return false;
						}
					}
				}
			}else {
				//core['check_callback'] = true;
			}
		}

		return core;
	}
    if(useCheckbox){
//    	$("#" + id).on('changed.jstree check_node.jstree uncheck_node.jstree', function(e, data){
//            if(data.node){
//                changeCheckbox();
//            }
//        });
    }

	$('#' + id).jstree({
		core: getCore(id, list, selectedValue, openLevel, dragDrop, maxDepth, useCheckbox, textMaxLength),
		types: getTypes(id, dragDrop, maxDepth),
		plugins: getPlugins(dragDrop, useCheckbox)
	}).on('ready.jstree', function (e, data, selected, event) {
//		if(undefined !== selectedValue && "" !== selectedValue) {
//			var ref = $('#' + id).jstree(true);
//
//			if('string' === typeof selectedValue) {
//				selectedValue = JSON.parse(selectedValue);
//			}
//
//			ref.select_node(selectedValue);
//		}
	});
}

function initSlider(id, min, max, step, range, value) {
	if(undefined === range || "" === range) { range = "min"; }

	$("#" + id).spinner({
		min:min, max:max, step:step, range:range, value: value,
		spin:function(event, ui){
			$("#" + id + "Slider").slider("value", ui.value);
		},
		stop:function(event, ui){
			$("#" + id + "Slider").slider("value", event.target.value);
		}
	});

	$("#" + id + "Slider").slider({
		min:min, max:max, step:step, range:range, value:$("#" + id).val(),
		slider:function(event, ui){
			$("#" + id).spinner("value", ui.value);
		},
		stop:function(event, ui){
			$("#" + id).spinner("value", ui.value);
		}
	});
}

/**
 * 스캔한 이미지를 본다. - PAPERX전용
 * @param outputFileId
 * @param printTypeCode
 * @returns
 */
function showScanImage(outputFileId, printTypeCode, targetName) {
	var reqImageType = getReqImageType(printTypeCode, targetName);

	console.log("outputFileId : " + outputFileId);
	console.log("printTypeCode : " + printTypeCode);
	console.log("targetName : " + targetName);
	console.log("reqImageType : " + reqImageType);

	$("#frmImage > input[name=printTypeCode]").val(printTypeCode);
	$("#frmImage > input[name=reqImageType]").val(reqImageType);
	$("#frmImage > input[name=outputManageSerialNo]").val(outputFileId);

	var cw = screen.availWidth;
	var ch = screen.availHeight;
	var sw = 800;
	var sh = 700;
	var ml = (cw-sw)/2;
	var mt = (ch-sh)/2;

	if(targetName == 'CRUSH_IMG_WIN'){
		ml += sw/2;
	}else if(printTypeCode == 'BOTH'){
		sw = 920;
		sh = 670;
		ml = (cw-sw)/2;
	}else{
		ml -= sw/2;
	}

	var imgWin = window.open("", targetName, "width=" + sw + ",height=" + sh + ",top=" + mt + ',left=' + ml + ",scrollbars=yes,resizable=yes");

	setTimeout(function() {
		$("#frmImage")
		.attr("action", contextPath + "/paperx/image/imageView.do")
		.attr("target", targetName)
		.submit();
	}, 300);
}

/**
 * PAPERX전용
 * @param printTypeCode
 * @param targetName
 * @returns
 */
function getReqImageType(printTypeCode, targetName) {
	if(targetName == 'CRUSH_IMG_WIN')
		return 'CRUSH';
	else if(targetName == 'NONTARGET_IMG_WIN')
		return 'NONTARGET';
	else if(targetName == 'CRUSH_DTAIL_IMG_WIN')
		return 'CRUSHDTAIL';
	else if(targetName == 'ALL_IMG_WIN')
		return 'ALL';
	else if(printTypeCode == 'PRINT')
		return 'PRINT';
	else if(printTypeCode == 'BOTH')
		return 'BOTH';
	else if(printTypeCode == 'CARRYOUT')
		return 'CARRYOUT';
	else
		return 'COPY';
}

/**
 * 신청서보기 - paperx 전용
 * @param requestNo
 * @returns
 */
function showApplicationForm(requestNo, openType, requestType) {
	var url = contextPath + "/paperx/exceptionrequest/exceptionRequestView.do";
	var sw = 750;
	var sh = 600;

	if(undefined !== requestType && 'EXPORT' === requestType) {
		url = contextPath + "/paperx/printexport/printExportView.do";
	}

	if('WINDOW' === openType) {
		$("input[name=requestNo]").val(requestNo);
		$("input[name=openType]").val(openType);

		var cw = screen.availWidth;
		var ch = screen.availHeight;
		var ml = (cw-sw)/2;
		var mt = (ch-sh)/2;
		var targetName = "ApplicationForm";

		var imgWin = window.open("", targetName, "width=" + sw + ",height=" + sh + ",top=" + mt + ',left=' + ml + ",scrollbars=yes,resizable=yes");

		setTimeout(function() {
			$("#frmImage")
			.attr("action", url)
			.attr("target", targetName)
			.submit();
		}, 300);
	}
	else {
		fnOpenDialogPopup(url, sw, sh, {
			requestNo : requestNo
		});
	}
}

function showChangeHistory(outputManageSerialNo) {
	var url = contextPath + '/paperx/printrecovery/changeHistoryView.do';

	fnOpenDialogPopup(url, 700, 700, {
		outputManageSerialNo : outputManageSerialNo
	});

}

function initSelectRemoteDataExcept(selectedId, url, selectedValue, nullText, exceptCode) {
	var $ts = $(selectedId);
	var param = {
			exceptCode : exceptCode
	}
	url = url + "?jsonParam=" + encodeURIComponent(JSON.stringify(param))
	// width 설정
    width = $(selectedId).css("width") != undefined ? $(selectedId).css("width") : "90%";

    var placeholder = $ts.attr("placeholder");

    if(undefined === placeholder || "" === placeholder) {
    	placeholder = "선택하세요";
    }

    $ts.select2({
        width: width,
        nullText: nullText,
        minimumInputLength: 1,
        placeholder: placeholder,
        maximumSelectionLength: 5,

    	ajax: {
    	    url: url,
    	    delay: 250,
    	    dataType: 'json'
    	  }
    });

    if( selectedValue != undefined && "" !== selectedValue) {
    	var arr = $.parseJSON(selectedValue);
    	var selectedCode = [];
    	for(var i=0; i<arr.length; i++){
    		var $newOption = $("<option></option>").val(arr[i].value).text(arr[i].text);
			$ts.append($newOption);
			selectedCode.push(arr[i].value);
    	}
    	$ts.val(selectedCode).trigger("change");

    } else {

			$ts.val([""]).trigger("change");
    }
}

/**
 * selectbox가 변경되면 tooltip을 변경한다.
 */
function changeSelectTooltip(id) {
	$("#" + id).change(function() {
		var tooltip = $('option:selected', this).attr("tooltip");

		if(undefined === tooltip || null === tooltip || 'null' === tooltip || 'undefined' === tooltip) {
			tooltip = '';
		}

		$("#" + id + "Tooltip").text(tooltip);
	});
}


/**
 *
 * @param param
 * @param url
 * @param callback
 * @param dataType - text, html, xml, json, jsonp, script. default: json
 * @returns
 */
function callAjax(param, url, callback, dataType, async){
	if(undefined === dataType) {
		dataType = "json";
	}
	if("json" == dataType){
		param = JSON.stringify(param);
	}
	if(undefined === async){
		async = true;
	}
	$.ajax({
		url: url
		, type : "POST"
		, cache: false
		, dataType : dataType
		, async: async
		, contentType: "application/json"
		, data : param
		, success : function(response){
			if('undefined' !== typeof callback) {
				console.log(response)
				callback(response);
			}
		}
		,error : function(e){
			console.log("error");
			console.log(e);
			if(e.status==401){
				parent.parent.location.href = "/login/logout";
			}else if(e.status == 403){
				alertMessage(g_msg("msg.accessDenied"));
			}else{
				alertMessage(g_msg("msg.error"));
				console.log(e);
			}
		}
	});
}


function checkBoolean(param, url){
	let bRet = false;

	$.ajax({
		url: url
		, type : "POST"
		, cache: false
		, dataType : "json"
		, async: false
		, contentType: "application/json"
		, data : JSON.stringify(param)
		, success : function(response){
			console.log(response);
			bRet = response;
		}
		,error : function(e){
			console.log("error");
			console.log(e);
			if(e.status==401){
				parent.parent.location.href = "/login/logout";
			}else if(e.status == 403){
				alertMessage(g_msg("msg.accessDenied"));
			}else{
				alertMessage(g_msg("msg.error"));
				console.log(e);
			}
		}
	});

	return bRet;
}

function callAjaxUpload(param, url, callback){
	console.log("enterted callAjaxUpload()");
	$.ajax({
		url: url
		, type: "POST"
		, data: param
		, cache: false
		, processData: false
		, contentType: false
		, encType: 'multipart/form-data'
		, success: function(response){
			console.log("enterted callAjaxUpload() - success");
			if('undefined' !== typeof callback){
				console.log("enterted callAjaxUpload() - typeof callback");
				console.log(response)
				callback(response);
			}
		}
		, error: function(e){

			if(e.status == 401){
				parent.parent.location.href = '/login/logout';
			}else if(e.status == 403){
				alertMessage(g_msg("msg.accessDenied"));
			}else{
				alertMessage(g_msg('msg.error') + '[' + e + ']');
			}
		}
	});
}

function guid() {
    function s4() {
      return ((1 + Math.random()) * 0x10000 | 0).toString(16).substring(1);
    }
    return s4() + s4();
  }

function pasteForIE(){
	var text = window.clipboardData.getData("Text");
	pasteText(text);
}