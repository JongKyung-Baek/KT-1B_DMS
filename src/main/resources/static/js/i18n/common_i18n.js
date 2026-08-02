/**
 * 다국어 지원 함수. 이 함수에서 XML5619 오류 발생함. ( IE 에서만 발생 ) 기능은 정상동작
 * @param lang
 * @param context
 */
function normalizeBundleLanguage(lang) {
	var language = String(lang || 'ko')
		.trim()
		.replace('_', '-')
		.split('-')[0]
		.toLowerCase();
	return /^(ko|en|id|ja|zh)$/.test(language) ? language : 'ko';
}

function loadBundles(lang, context) {
	try {
		jQuery.i18n.properties({
			name:['message', 'feature'],
			path:context+'/messages/',
			mode:'map',
			language:normalizeBundleLanguage(lang),
			callback: function() {
			}
		});
	}
	catch(e) {
		console.log(e);
	}
}

var g_msg = jQuery.i18n.prop;

window.SdmsI18n = window.SdmsI18n || {
	t: function(key, fallback) {
		var args = Array.prototype.slice.call(arguments, 2);
		var value;
		try {
			value = g_msg.apply(null, [key].concat(args));
		} catch (e) {
			value = null;
		}
		return (typeof value === 'string' && value && value !== key)
			? value
			: (fallback || key);
	}
};
