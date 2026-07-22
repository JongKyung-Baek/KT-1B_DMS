/**
 * dialog popup을 띄운다.
 * @param url
 * @param width - s(mall)-500, m(edium)-750, l(arge)-1000
 * @param height
 * @returns
 */
/*function openDialogPopup(url, width, height) {*/

function initDialogPopup(dialogId, width, height) {
	var windowHeight = $(window).height();

	if(windowHeight < height) {
		height = windowHeight - 120;
	}
/*
	if('s' === width) {
		width = 500;
	}
	else if('m' === width) {
		width = 750;
	}
	else if('l' === width) {
		width = 1000;
	}
	else {
		alert("파라메터가 유효하지 않음. s, m, l");
		return;
	}
*/
	if(width){
		if('s' === width) {
			widthSize = 'sizeS';
		}
		else if('m' === width) {
			widthSize = 'sizeM';
		}
		else if('l' === width) {
			widthSize = 'sizeL';
		}
		else if('xl' === width) {
			widthSize = 'sizeXL';
		}
		else {
			alert("파라메터가 유효하지 않음. s, m, l, xl");
			return;
		}
	}
	try {
		$("#" + dialogId).dialog('destroy');
	}
	catch(e) { }

	$("#" + dialogId).dialog({
		modal: true,
		//width: width,
	    height: height,
	    resizable: false,
	    autoOpen: false,
	    dialogClass: "popupDialog" + "widthSize"
	});
}


// function generateUniqueId(baseId) {
// 	return baseId + '_' + Math.random().toString(36).substr(2, 9);
// }

// 원래 코드
function openDialogPopup(url, param, dialogId, width, height, modal, extraDialogClass) {
	if(undefined == modal){
		modal = true;
	}
	if(undefined == extraDialogClass){
		extraDialogClass = "";
	}
	var isNoticeAddPopup = url === "/configurationmanage/notice/addPopup";
	var isDialogMinHeightException =
		url === "/inside/distribution/commonRequest/commonPrintRequestPopup" ||
		url === "/inside/distribution/commonRequest/drawingRevisionUpdatePopup" ||
		url === "/inside/distribution/commonRequest/drawingVersionCheckPopup" ||
		url === "/inside/distribution/annotationinfo/annotationPopup";
	var isDeferredInitialLayoutPopup =
		url === "/inside/distribution/commonRequest/commonPrintRequestPopup" ||
		url === "/inside/distribution/commonRequest/drawingRevisionUpdatePopup" ||
		url === "/inside/distribution/commonRequest/drawingVersionCheckPopup";
	var isApprovalStatusPopup =
		url === "/inside/distribution/commonRequest/approvalStatusPopup";
	var useAutoContentHeight =
		isDialogMinHeightException ||
		url === "/outside/user/information/requestPopup" ||
		url === "/outside/user/information/userModPopup" ||
		url === "/outside/user/status/statusPopup" ||
		url === "/bbs/qna/addPopup" ||
		url === "/configurationmanage/qna/addPopup" ||
		url === "/configurationmanage/notice/addPopup" ||
		url === "/bbs/qna/qnaPopup" ||
		url === "/configurationmanage/qna/qnaPopup";
	var viewportHeight = $(window).height() || 0;
	var maxDialogHeight = Math.floor(viewportHeight * 0.8);
	var parsedHeight = parseInt(height, 10);

	if('s' === width) {
		widthSize = 'sizeS';
	}
	else if('m' === width) {
		widthSize = 'sizeM';
	}
	else if('l' === width) {
		widthSize = 'sizeL';
	}
	else if('xl' === width) {
		widthSize = 'sizeXL';
	}
	else {
		alert("파라메터가 유효하지 않음. s, m, l, xl");
		return;
	}
			var requestedDialogWidth = isApprovalStatusPopup ? 380 : 1000;
		var dialogClassName = widthSize;
		var cloneId = dialogId + 'a';
		if($.trim(extraDialogClass) !== ""){
			dialogClassName += " " + $.trim(extraDialogClass);
		}

			$('#' + cloneId).remove();
			$('#' + dialogId).removeAttr('style');
			var clone = $('#'+dialogId).clone();
			clone.attr('id', cloneId);
			clone.removeAttr('style');
			clone.appendTo('body');
		clone.data('requestedDialogHeight', parsedHeight);
		clone.data('requestedDialogWidth', requestedDialogWidth);
		callAjax({menuUrl : url}, CONTEXT_PATH + "/menu/getMenuNm", function(response) {
				clone.load(url, param, function(responseText, textStatus, response){
					if(response.status == 403){
						$(this).dialog('destroy').remove();
						alertMessage(g_msg("msg.accessDenied"));
						return;
					}
					setTimeout(function() {
						var syncDialogLayer = clone.data('syncDialogLayer');
						if(typeof syncDialogLayer === 'function') {
							syncDialogLayer();
							if(isDeferredInitialLayoutPopup){
								var $widget = clone.dialog('widget');
								if($widget.length){
									$widget[0].style.setProperty('visibility', 'visible', 'important');
								}
							}
						}
					}, 0);
				}).dialog({
				appendTo: 'body',
				dialogClass:dialogClassName,
				modal:modal,
				/* title: response.results, */
				height: 'auto',
				zIndex: 20000,
				autoOpen:true,
				resizable:false,
				draggable:false,
				position:{
					my: "center",
					at: "center",
					of: window
			},
			open:function(){
				var $dialog = $(this);
				var dialogEventNs = '.popupRecenter-' + $dialog.attr('id');

					function syncDialogLayer() {
						var $widget = $dialog.dialog('widget');
						var $content = $dialog.closest('.ui-dialog').find('.ui-dialog-content');
					var $titlebar = $widget.find('.ui-dialog-titlebar');
						var $closeButton = $widget.find('.ui-dialog-titlebar-close');
						var $overlay = $('.ui-widget-overlay:visible').last();
						var dialogZIndex = 2147483000;
						var currentViewportWidth = $(window).width() || 0;
						var currentViewportHeight = $(window).height() || 0;
						var currentMaxDialogWidth = Math.floor(currentViewportWidth * 0.95);
						var currentMaxDialogHeight = Math.floor(currentViewportHeight * 0.8);
						var requestedWidth = parseInt($dialog.data('requestedDialogWidth'), 10);
						var targetDialogWidth = currentMaxDialogWidth;
						var titlebarHeight = $titlebar.outerHeight(true) || 0;
						var contentPaddingOffset = $content.outerHeight(true) - $content.height();
							var minDialogHeight = 160;
							var minContentHeight = 120;
							var requestedDialogHeight = parseInt($dialog.data('requestedDialogHeight'), 10);
							var hasRequestedHeight = !isNaN(requestedDialogHeight) && requestedDialogHeight > 0;
							var maxContentHeight = Math.max(currentMaxDialogHeight - titlebarHeight - contentPaddingOffset, minContentHeight);
							var targetContentMaxHeight = maxContentHeight;
							var targetContentMinHeight = minContentHeight;

					$dialog.dialog('moveToTop');

					if(!isNaN(requestedWidth) && currentMaxDialogWidth > 0){
						targetDialogWidth = Math.min(requestedWidth, currentMaxDialogWidth);
					}

						if(targetDialogWidth > 0){
							$dialog.dialog('option', 'width', targetDialogWidth);
						}

							if(hasRequestedHeight && !useAutoContentHeight){
								targetContentMinHeight = Math.min(requestedDialogHeight, targetContentMaxHeight);
							}

								$widget.css({
									zIndex: dialogZIndex,
									margin: 0
								});
						if($widget.length){
							$widget[0].style.setProperty('width', targetDialogWidth + 'px', 'important');
							$widget[0].style.setProperty('max-width', currentMaxDialogWidth + 'px', 'important');
							$widget[0].style.setProperty('max-height', currentMaxDialogHeight + 'px', 'important');
							$widget[0].style.setProperty('height', 'auto', 'important');
							if(isNoticeAddPopup || isDialogMinHeightException){
								$widget[0].style.setProperty('min-height', '0', 'important');
							}
							else{
								$widget[0].style.setProperty('min-height', minDialogHeight + 'px', 'important');
							}
							$widget[0].style.setProperty('position', 'fixed', 'important');
							$widget[0].style.setProperty('display', 'flex', 'important');
							$widget[0].style.setProperty('flex-direction', 'column', 'important');
						}

								if($content.length){
									$content[0].style.setProperty('height', 'auto', 'important');
									if(useAutoContentHeight || isNoticeAddPopup || isDialogMinHeightException){
										$content[0].style.setProperty('min-height', '0', 'important');
									}
									else{
										$content[0].style.setProperty('min-height', targetContentMinHeight + 'px', 'important');
									}
									$content[0].style.setProperty('max-height', targetContentMaxHeight + 'px', 'important');
									$content[0].style.setProperty('overflow-y', 'auto', 'important');
									$content[0].style.setProperty('overflow-x', 'hidden', 'important');
									$content[0].style.setProperty('flex', '1 1 auto', 'important');
								}

					if($overlay.length){
						$overlay.css({
							position: 'fixed',
							zIndex: dialogZIndex - 1
						});
					}

						$dialog.dialog('widget').find('.ui-dialog-titlebar').css('cursor', 'default');

						if($closeButton.length){
							$closeButton.hide();
						}

						if($widget.length){
							var widgetHeight = $widget.outerHeight(true) || 0;
							var widgetTop = Math.max(Math.floor((currentViewportHeight - widgetHeight) / 2), 16);
							$widget[0].style.setProperty('top', widgetTop + 'px', 'important');
							$widget[0].style.setProperty('left', '50%', 'important');
							$widget[0].style.setProperty('transform', 'translateX(-50%)', 'important');
						}
					}

				$dialog.data('syncDialogLayer', syncDialogLayer);
				if(isDeferredInitialLayoutPopup){
					var $widget = $dialog.dialog('widget');
					if($widget.length){
						$widget[0].style.setProperty('visibility', 'hidden', 'important');
					}
				}
				else{
					syncDialogLayer();
					setTimeout(syncDialogLayer, 0);
					setTimeout(syncDialogLayer, 80);
				}
					$(window).off('resize' + dialogEventNs + ' orientationchange' + dialogEventNs).on('resize' + dialogEventNs + ' orientationchange' + dialogEventNs, function(){
						syncDialogLayer();
					});
			},
				close:function(ev,ui){
					$(window).off('.popupRecenter-' + $(this).attr('id'));
					$('body').css({"overflow":"auto"});
					$(this).dialog('destroy').remove();
				}
			});
		$('body').css({"overflow":"hidden"});
	});
}

// 수정 코드
// function openDialogPopup(url, param, dialogId, width, height, modal) {
// 	if (undefined == modal) {
// 		modal = true;
// 	}
// 	var widthSize;
//
// 	// 적절한 너비 설정
// 	if ('s' === width) {
// 		widthSize = 'sizeS';
// 	} else if ('m' === width) {
// 		widthSize = 'sizeM';
// 	} else if ('l' === width) {
// 		widthSize = 'sizeL';
// 	} else if ('xl' === width) {
// 		widthSize = 'sizeXL';
// 	} else {
// 		alert("파라메터가 유효하지 않음. s, m, l, xl");
// 		return;
// 	}
//
// 	// 고유한 ID 생성
// 	var uniqueId = dialogId + '_' + new Date().getTime();
// 	var clone = $('#' + dialogId).clone();
// 	clone.attr('id', uniqueId);
//
// 	// 전역 스택 초기화
// 	if (!window.dialogIdStack) {
// 		window.dialogIdStack = [];
// 	}
//
// 	callAjax({menuUrl: url}, CONTEXT_PATH + "/menu/getMenuNm", function(response) {
// 		clone.load(url, param, function(responseText, textStatus, response) {
// 			if (response.status == 403) {
// 				$(this).dialog('destroy');
// 				alertMessage(g_msg("msg.accessDenied"));
// 			} else {
// 				setTimeout(function (){
// 						console.log("초기화 잘 하나요")
// 						// 이 부분에서 select2를 다시 초기화합니다.
// 						$('#' + uniqueId).find('select').select2()
// 				},5)
//
// 			}
// 		}).dialog({
// 			dialogClass: widthSize,
// 			modal: modal,
// 			title: response.results,
// 			height: height,
// 			autoOpen: true,
// 			resizable: false,
// 			close: function(ev, ui) {
// 				$('body').css({"overflow":"auto"});
// 				$(this).dialog('destroy');
// 				var index = window.dialogIdStack.indexOf(uniqueId);
// 				if (index > -1) {
// 					window.dialogIdStack.splice(index, 1);
// 				}
// 			}
// 		});
//
// 		$('body').css({"overflow":"hidden"});
// 		window.dialogIdStack.push(uniqueId);
// 	});
// }

function openDialogPopup_onlyForActLog(url, param, dialogId, width, height, modal, extraDialogClass) {
	if(undefined == modal){
		modal = true;
	}
	if(undefined == extraDialogClass){
		extraDialogClass = "";
	}
	var viewportHeight = $(window).height() || 0;
	var parsedHeight = parseInt(height, 10);

	if('s' === width) {
		widthSize = 'sizeS';
	}
	else if('m' === width) {
		widthSize = 'sizeM';
	}
	else if('l' === width) {
		widthSize = 'sizeL';
	}
	else if('xl' === width) {
		widthSize = 'sizeXL';
	}
	else {
		alert("파라메터가 유효하지 않음. s, m, l, xl");
		return;
	}
	var dialogClassName = widthSize;
	if($.trim(extraDialogClass) !== ""){
		dialogClassName += " " + $.trim(extraDialogClass);
	}
	var clone = $('#'+dialogId).clone();
	clone.attr('id', dialogId+'b');
	callAjax({menuUrl : url}, CONTEXT_PATH + "/menu/getMenuNm", function(response) {
		clone.load(url, param, function(responseText, textStatus, response){
			if(response.status == 403){
				$(this).dialog('destroy');
				alertMessage(g_msg("msg.accessDenied"));
			}
		}).dialog({
			appendTo: 'body',
			dialogClass:dialogClassName,
			modal:modal,
			/* title: response.results, */
			height: 'auto',
			autoOpen:true,
			resizable:false,
			draggable:false,
			position:{
				my: "center",
				at: "center",
				of: window
			},
			open:function(){
				var $dialog = $(this);
				var dialogEventNs = '.popupActLogRecenter-' + $dialog.attr('id');

				function syncDialogLayer() {
					var $widget = $dialog.dialog('widget');
					var $content = $dialog.closest('.ui-dialog').find('.ui-dialog-content');
					var $titlebar = $widget.find('.ui-dialog-titlebar');
					var $closeButton = $widget.find('.ui-dialog-titlebar-close');
					var $overlay = $('.ui-widget-overlay:visible').last();
					var dialogZIndex = 2147483000;
					var currentViewportWidth = $(window).width() || 0;
					var currentViewportHeight = $(window).height() || 0;
					var currentMaxDialogWidth = Math.floor(currentViewportWidth * 0.95);
					var currentMaxDialogHeight = Math.floor(currentViewportHeight * 0.8);
					var requestedWidth = 1000;
					var targetDialogWidth = currentMaxDialogWidth;
					var titlebarHeight = $titlebar.outerHeight(true) || 0;
					var contentPaddingOffset = $content.outerHeight(true) - $content.height();
					var minDialogHeight = 160;
					var minContentHeight = 120;
					var requestedDialogHeight = !isNaN(parsedHeight) && parsedHeight > 0 ? parsedHeight : 0;
					var maxContentHeight = Math.max(currentMaxDialogHeight - titlebarHeight - contentPaddingOffset, minContentHeight);
					var targetContentMaxHeight = maxContentHeight;
					var targetContentMinHeight = requestedDialogHeight > 0 ? Math.min(requestedDialogHeight, targetContentMaxHeight) : minContentHeight;

					$dialog.dialog('moveToTop');

					if(!isNaN(requestedWidth) && currentMaxDialogWidth > 0){
						targetDialogWidth = Math.min(requestedWidth, currentMaxDialogWidth);
					}

					if(targetDialogWidth > 0){
						$dialog.dialog('option', 'width', targetDialogWidth);
					}

					$widget.css({
						zIndex: dialogZIndex,
						margin: 0
					});

					if($widget.length){
						$widget[0].style.setProperty('width', targetDialogWidth + 'px', 'important');
						$widget[0].style.setProperty('max-width', currentMaxDialogWidth + 'px', 'important');
						$widget[0].style.setProperty('max-height', currentMaxDialogHeight + 'px', 'important');
						$widget[0].style.setProperty('height', 'auto', 'important');
						$widget[0].style.setProperty('min-height', minDialogHeight + 'px', 'important');
						$widget[0].style.setProperty('position', 'fixed', 'important');
						$widget[0].style.setProperty('display', 'flex', 'important');
						$widget[0].style.setProperty('flex-direction', 'column', 'important');
					}

					if($content.length){
						$content[0].style.setProperty('height', 'auto', 'important');
						$content[0].style.setProperty('min-height', targetContentMinHeight + 'px', 'important');
						$content[0].style.setProperty('max-height', targetContentMaxHeight + 'px', 'important');
						$content[0].style.setProperty('overflow-y', 'auto', 'important');
						$content[0].style.setProperty('overflow-x', 'hidden', 'important');
						$content[0].style.setProperty('flex', '1 1 auto', 'important');
					}

					if($overlay.length){
						$overlay.css({
							position: 'fixed',
							zIndex: dialogZIndex - 1
						});
					}

					$dialog.dialog('widget').find('.ui-dialog-titlebar').css('cursor', 'default');

					if($closeButton.length){
						$closeButton.hide();
					}

					if($widget.length){
						var widgetHeight = $widget.outerHeight(true) || 0;
						var widgetTop = Math.max(Math.floor((currentViewportHeight - widgetHeight) / 2), 16);
						$widget[0].style.setProperty('top', widgetTop + 'px', 'important');
						$widget[0].style.setProperty('left', '50%', 'important');
						$widget[0].style.setProperty('transform', 'translateX(-50%)', 'important');
					}
				}

				$dialog.data('syncDialogLayer', syncDialogLayer);
				syncDialogLayer();
				setTimeout(syncDialogLayer, 0);
				setTimeout(syncDialogLayer, 80);
				$(window).off('resize' + dialogEventNs + ' orientationchange' + dialogEventNs).on('resize' + dialogEventNs + ' orientationchange' + dialogEventNs, function(){
					syncDialogLayer();
				});
			},
			close:function(ev,ui){
				$(window).off('.popupActLogRecenter-' + $(this).attr('id'));
				$('body').css({"overflow":"auto"});
				$(this).dialog('destroy').remove();
			}
		});
		$('body').css({"overflow":"hidden"});
	});
}



function getDialogMsg(msg) {
	return changeXSS(msg, 1).split('&lt;br/&gt;').join('<br/>').split('&lt;br&gt;').join('<br/>');
}

function bringMessageDialogToFront(dialogSelector) {
	var $dialog = $(dialogSelector);
	if(!$dialog.length || !$dialog.data('ui-dialog')) {
		return;
	}

	var $widget = $dialog.dialog('widget');
	if(!$widget.length) {
		return;
	}

	var maxZ = 0;
	$('.ui-dialog:visible, .ui-widget-overlay:visible').each(function() {
		var z = parseInt($(this).css('z-index'), 10);
		if(!isNaN(z) && z > maxZ) {
			maxZ = z;
		}
	});

	var targetZ = Math.min(Math.max(maxZ + 2, 2147483400), 2147483646);
	if($widget.length && $widget[0] && $widget[0].style) {
		$widget[0].style.setProperty('z-index', String(targetZ), 'important');
	}
	else {
		$widget.css('z-index', targetZ);
	}

	var $overlay = $('.ui-widget-overlay:visible').last();
	if($overlay.length) {
		if($overlay[0] && $overlay[0].style) {
			$overlay[0].style.setProperty('z-index', String(targetZ - 1), 'important');
		}
		else {
			$overlay.css('z-index', targetZ - 1);
		}
	}

	$dialog.dialog('moveToTop');
}

/**
 * alert
 * @param msg
 * @param callback
 */
function alertMessage(msg, callback) {
	var t = undefined;
	console.log("callback: " + callback);
	if(undefined === callback) {
		t = function() {
			$(this).dialog("close");
		};
	}
	else {
		t = function() {
			$(this).dialog("close");
			callback();
		};
	}

	$("#alertMessage").html(getDialogMsg(msg)).dialog({
		modal: true
		, title: g_msg("title.alert")
		, resizable: false
		, appendTo: 'body'
		, buttons: [{
			text: "확인",
			"class": "point",
			click: t
		}]
		, open: function() {
			bringMessageDialogToFront('#alertMessage');
		}
	});
	bringMessageDialogToFront('#alertMessage');
}


function infoMessage(msg, callback) {
	console.log('infoMessage');
	console.log(msg);
	var t = undefined;

	if(undefined === callback) {
		t = function() {
			$(this).dialog("close");
		};
	}
	else {
		t = callback;
	}

	$("#alertMessage").html(getDialogMsg(msg)).dialog({
		modal: true
		, title: g_msg("title.confirm")
		, resizable: false
		, appendTo: 'body'
		, buttons: [{
			text: "확인",
			"class": "point",
			click: t
		}]
		, open: function() {
			bringMessageDialogToFront('#alertMessage');
		}
	});
	bringMessageDialogToFront('#alertMessage');
}

/**
 * confirm
 * @param msg
 * @param callback
 * @param width
 * @param height
 * @param title
 */
function confirmMessage(msg, callback, width, height, title) {

	if(!title) {
		title = g_msg("title.confirm")
	}

	var option = {
		modal: true
		, title: g_msg("title.confirm")
		, resizable: false
		, buttons: []
	};

	option.buttons.push({
		text: g_msg("btn.cancel"),
		click: function() {
			$(this).dialog("close");
		}
	});

	option.buttons.push({
		text: "확인",
		"class": "point",
		click: callback

	});

	if(width) {
		option["width"] = width;
	}

	if(height) {
		option["height"] = height;
	}

	option["appendTo"] = "body";
	option["open"] = function() {
		bringMessageDialogToFront('#confirmMessage');
	};

	$("#confirmMessage").html(getDialogMsg(msg)).dialog(option);
	bringMessageDialogToFront('#confirmMessage');
}

// 원래 코드
function closePopup(dialog){
	$('body').css({"overflow":"auto"});
	try {
		searchList(gridParam);
		popupGridParam = '';
	} catch(e) {}
	$('#'+dialog+'a').dialog('close');
}
function closePopup_onlyForActLog(dialog){
	$('body').css({"overflow":"auto"});
	try {
		searchList(gridParam);
		popupGridParam = '';
	} catch(e) {}
	$('#'+dialog+'b').dialog('close');
}
// 수정 코드
// function closePopup() {
// 	if (window.dialogIdStack && window.dialogIdStack.length > 0) {
// 		var lastDialogId = window.dialogIdStack.pop();
// 		$('#' + lastDialogId).dialog('close');
// 	}
// }





/**
 * 일괄검색 팝업창을 닫을때의 event
 * gridParam.searchAllParam에 담겨있는 일괄검색 grid정보를 삭제한다.
 * @param event
 * @returns
 */
$('#searchAllPopupa').on('dialogclose', function(event) {
    gridParam.searchAllParam = '';
    $('#useLike').val('');
});


$('#emailCheck').on('change', function(){
	if($('input:checkbox[id=emailCheck]').is(":checked")){
		$('#sendEmailYn').val('Y');
	}else{
		$('#sendEmailYn').val('N');
	}
})
