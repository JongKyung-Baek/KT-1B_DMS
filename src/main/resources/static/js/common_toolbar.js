/**
 * form 생성 공통 js 파일
 */

function settingToolbar(toolbarData, $container){
	if( (null==toolbarData) || (""==toolbarData) ){
		if(undefined === $container) {
			$container = $(".btnArea");
		}
		$container.addClass('is-empty');
		return;
	}

	if(undefined === $container) {
		$container = $(".btnArea");
	}

	$container.empty().removeClass('is-empty');

	//var toolbarInfo = $.parseJSON(toolbarData);
	var toolbarInfo = toolbarData;
	if(toolbarInfo.length === 0){
		$container.addClass('is-empty');
		return;
	}
	var toolbarId = toolbarInfo[0].toolbarId;
	$leftBtn = $("<div class='left'></div>");
	$rightBtn = $("<div class='right'></div>");
	$container.append($leftBtn);
	$container.append($rightBtn);

	$.each( toolbarInfo, function( key, value ) {

		if("left" === value.buttonAlign){
			$leftBtn.append("<button class='ui-button ui-corner-all' onclick=\""+value.callFunc+";\">"+value.buttonLabel+"</button>");
		}else if("right" === value.buttonAlign){
			$rightBtn.append("<button class='ui-button ui-corner-all' onclick=\""+value.callFunc+";\">"+value.buttonLabel+"</button>");
		}

	});

	if($leftBtn.children().length === 0){
		$leftBtn.hide();
	}
	if($rightBtn.children().length === 0){
		$rightBtn.hide();
	}
	if($leftBtn.children().length === 0 && $rightBtn.children().length === 0){
		$container.addClass('is-empty');
		return;
	}

	var $toolbarAreas = $('.contentArea .sbr + .btnArea, .contentArea .distribution-invoice-layout > .btnArea');
	var btnL = $toolbarAreas.length;
	for(i = 0 ; i < btnL ; i++ ){
		var btnW = $toolbarAreas.eq(i).width();
		var btnWL = $toolbarAreas.eq(i).find('.left').width() + 10;
		$toolbarAreas.eq(i).find('.right').css({'width':'calc(100% - ' +  btnWL + 'px)'});
		$toolbarAreas.eq(i).find('.left').css({'width':btnWL + 'px'});
	}

}


