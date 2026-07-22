/**
 * 다국어 지원 함수. 이 함수에서 XML5619 오류 발생함. ( IE 에서만 발생 ) 기능은 정상동작
 * @param lang
 * @param context
 */
function loadBundles(lang, context) {
	try {
		jQuery.i18n.properties({
			name:'message',
			path:context+'/messages/',
			mode:'map',
			language:lang,
			callback: function() {
			}
		});
	}
	catch(e) {
		console.log(e);
	}
}

var g_msg = jQuery.i18n.prop;