/**
 * IP 유효성 검사
 * @param id
 * @returns
 */
function isValidationIpv4(id) {
	var $o = $("#" + id);
	var ip = $o.val();

	if(isIpv4(ip)) {
		return true;
	}

	alertMessage(g_msg("msg.invalidIp").replace("IP", g_msg("label." + id)), function() {
		$(this).dialog("close");
		$o.focus();
	});

	return false;
}

/**
 * 필수 입력값이 비어있는지 체크. msgId에 해당 column의 lang값을 전부 넣어준다.
 * @param id
 * @param msgId
 * @returns {Boolean}
 */
function isValidDataEmpty(id, msgId) {
	if(undefined === msgId) {
		msgId = id;
	}

	return isValidObjEmpty($("#" + id), msgId);
}

function isValidIpRange(startIp, endIp){
	if(startIp > endIp){
		alertMessage(g_msg("msg.ipRangeInputErr"), function() {
			$(this).dialog("close");
		});
		return false;
	}
	return true;
}

/**
 * 필수 입력값이 비어있는지 체크
 * @param $o
 * @param msgId
 * @returns
 */
function isValidObjEmpty($o, msgId) {
	if(null === $o.val() || "" === $o.val()) {
		alertMessage(g_msg("msg.requiredDataEmpty", g_msg(msgId)), function() {

			$(this).dialog("close");
			$o.focus();
		});

		return false;
	}

	return true;
}