/**
 * form 생성 공통 js 파일
 */
function settingForm(formData, $container){
	if( (null==formData) || (""==formData) ){
		return;
	}
	var formInfo = $.parseJSON(formData);
	var formId = formInfo[0].formId;

	$form = $("<form id='" + formId + "'></form>");
	$ul = $("<ul class='ibx'></ul>");
	$btnDiv = $("<div class='btnBox'></div>")
	$form.append($ul);
	$form.append($btnDiv);

	if(undefined === $container) {
		$container = $(".sbr");
	}

	$container.append($form);
	$.each( formInfo, function( key, value ) {
		if(!("checkbox" == value.columnType) && !("radio"== value.columnType)) {
			$li = $("<li></li>");
			if("Y" === value.columnHidden){
			}else{
				$li.append("	<label for='input"+key+"'>"+value.columnName+"</lable>");
			}
		}
	    if("input" == value.columnType ){
	    	setInput(value);
	    }else if( ("calendar" == value.columnType) || ("datetime" == value.columnType)) {
	    	setCalendar(value);
	    }else if( ("select" == value.columnType) || ("selectInput" == value.columnType) || ("selectSearch" == value.columnType) ) {
	    	setSelect(value);
	    }else if( "checkbox" == value.columnType ) {
	    	setCheckbox(value);
	    }else if( "radio" == value.columnType ) {
	    	setRadio(value);
	    }else if( "btnSearch" == value.columnType ) {
	    	setSearchBtn($container, value);
	    }

	});

	$('.sbr .ibx li').find('input[type="hidden"]:only-child').parent('li').css({'display':'none'});


}


/**
 * 날짜 구하기
 * @param date
 * @returns
 */
function getDateValue(dataType){
	var now = new Date();
	var addValue = "";
	if(undefined === dataType || '' === dataType) { return ''; }
	if( dataType.indexOf("today") !=  -1){
		return getDateStr(now);
	}else if(dataType.indexOf("year") !=  -1){
		addValue = dataType.replace("year", "");
		now.setYear(now.getFullYear()+parseInt(addValue));
	}else if(dataType.indexOf("month") !=  -1){
		addValue = dataType.replace("month", "");
		now.setMonth((now.getMonth())+parseInt(addValue))
	}else if(dataType.indexOf("day") !=  -1){
		addValue = dataType.replace("day", "");
		now.setDate(now.getDate()+parseInt(addValue));
	}
	return getDateStr(now);
}

/**
 * 날짜 자릿수 맞추기 (2자리)
 * @param myDate
 * @returns
 */
function getDateStr(myDate){
	var month = "00" + (myDate.getMonth() + 1);
	var day = "00" + (myDate.getDate());
	return (myDate.getFullYear() + '-' + month.slice(-2) + '-' + day.slice(-2));
}

/**
 * input Setting
 * @param value
 * @returns
 */
function setInput(value){
	if("Y"===value.columnHidden){
		$li.append("<input type='hidden' id='"+value.columnId+"' name='"+value.columnId+"' />");
	}else{
		$li.append("<input type='text' id='"+value.columnId+"' name='"+value.columnId+"' />");
	}
//	$li.append("<input type='text' id='"+value.columnId+"' name='"+value.columnId+"' size='"+value.columnSize+"'/>");
	$ul.append($li);
}



/**
 * calendar Setting
 * @param value
 * @returns
 */
function setCalendar(value){

	var arrColId = (""!=value.columnId) ? value.columnId.split(',') : "";								//Tag ID
	var arrColSize = (""!=value.columnSize) ? value.columnSize.split(',') : "";							//size
	var arrDefValue = (""!=value.defaultValue) ? value.defaultValue.split(',') : "";					//DefaultValue
	var arrMaxDate = (""!=value.maxDate) ? value.maxDate.split(',') : "";								//최대 선택일
	var arrMinDate = (""!=value.minDate) ? value.minDate.split(',') : "";
	var isAuditLogNativeDate = $form && $form.length && $form.attr('id') === 'formInsideAuditLog';

	var defValue = getDateValue(arrDefValue[0]);

	//if('' !== START_DT) { defValue = START_DT; }
	if('' !== TERM_LIMIT) {
		// 출력기한임박, 유효기간임박이 있으면 달력에 값을 넣지 않는다.
		defValue = '';
	}

	$divCalendar = "<div class='input-append date'><input class='ui-corner-all' id='"+arrColId[0]+"' name='"+arrColId[0]+"' type='text' size='"+arrColSize[0]+"' value='"+defValue+"'>";
	$divCalendar += "<span class='add-on'> <i class='icon-th'></i></span></div>";
	var startDefValue = defValue;

	defValue = getDateValue(arrDefValue[1]);

	if('' !== END_DT) { defValue = END_DT; }
	if('' !== TERM_LIMIT) {
		// 출력기한임박, 유효기간임박이 있으면 달력에 값을 넣지 않는다.
		defValue = '';
	}

	if(isAuditLogNativeDate) {
		$divCalendar = "<div class='input-append date'><input class='ui-corner-all' id='"+arrColId[0]+"' name='"+arrColId[0]+"' type='date' size='"+arrColSize[0]+"' value='"+startDefValue+"'></div>";
		if(2 === arrColId.length) {
			$divCalendar += "<span class='fromTo'>~</span><div class='input-append date'><input class='ui-corner-all' id='"+arrColId[1]+"' name='"+arrColId[1]+"' type='date' size='"+arrColSize[1]+"' value='"+defValue+"'></div>";
		}
		$li.append($divCalendar);
		$ul.append($li);

		if(2 === arrColId.length) {
			$("#"+arrColId[0]+", #"+arrColId[1]).on("change", function() {
				var startValue = $("#"+arrColId[0]).val();
				var endValue = $("#"+arrColId[1]).val();
				if(!startValue || !endValue) {
					return;
				}
				if(startValue > endValue) {
					if(this.id === arrColId[0]) {
						$("#"+arrColId[0]).val("");
						alertMessage(g_msg('msg.cannotGreaterStartDate'), function(){
							$(this).dialog("close");
						});
					} else {
						$("#"+arrColId[1]).val("");
						alertMessage(g_msg('msg.cannotLessStartDate'), function(){
							$(this).dialog("close");
						});
					}
				}
			});
		}
		return;
	}

	if(2 === arrColId.length) {
		$divCalendar += "<span class='fromTo'>~</span><div class='input-append date'><input class='ui-corner-all' id='"+arrColId[1]+"' name='"+arrColId[1]+"' type='text' size='"+arrColSize[1]+"' value='"+defValue+"'>";
		$divCalendar += "<span class='add-on'> <i class='icon-th'></i></span></div>";
	}


	$li.append($divCalendar);
	$ul.append($li);

	if(arrMaxDate.length > 1){						//캘린더에서 선택 가능한 마지막 날짜 및 시간
		$.each(arrMaxDate, function(index, item){
			if( !("today" == arrMaxDate[index]) ){
				arrMaxDate[index] = arrMaxDate[index].replace('month', 'M');
			}
		});
	}

	if(arrMinDate.length > 1){						//캘린더에서 선택 가능한 최소 날짜 및 시간
		$.each(arrMinDate, function(index, item){
			if( !("today" == arrMinDate[index]) ){
				arrMinDate[index] = arrMinDate[index].replace('month', 'M');
			}
		});
	}
	// select2를 달력에 적용하기 위한 코드인데, 이게 2개까지밖에 안되서 3개 이상이면 달력 자체에서 에러 발생
	/*

	$.datepicker._updateDatepicker_original = $.datepicker._updateDatepicker;
    $.datepicker._updateDatepicker = function(inst) {
    $.datepicker._updateDatepicker_original(inst);
	    var afterShow = this._get(inst, 'afterShow');
	    if (afterShow)
	        afterShow.apply((inst.input ? inst.input[0] : null));  // trigger custom callback
    },
    afterShow: function() {
    	$(".ui-datepicker select").select2();
    }
    */


	$("#"+arrColId[0]).datepicker({	//시작일
		dateFormat: 'yy-mm-dd'
		, prevText: g_msg('label.previousMonth')
	    , nextText: g_msg('label.nextMonth')
	    , maxDate: arrMaxDate[0]
	    , minDate: arrMinDate[0]
	    , monthNames: [g_msg('label.january'), g_msg('label.february'), g_msg('label.march'), g_msg('label.april'), g_msg('label.may'), g_msg('label.june'), g_msg('label.july'), g_msg('label.august'), g_msg('label.september'), g_msg('label.october'), g_msg('label.november'), g_msg('label.december')]
	    , monthNamesShort: [g_msg('label.january'), g_msg('label.february'), g_msg('label.march'), g_msg('label.april'), g_msg('label.may'), g_msg('label.june'), g_msg('label.july'), g_msg('label.august'), g_msg('label.september'), g_msg('label.october'), g_msg('label.november'), g_msg('label.december')]
	    , dayNames: [g_msg('label.sunday'), g_msg('label.monday'), g_msg('label.tuesday'), g_msg('label.wednesday'), g_msg('label.thursday'), g_msg('label.friday'), g_msg('label.saturday')]
	    , dayNamesShort: [g_msg('label.sundayShort'), g_msg('label.mondayShort'), g_msg('label.tuesdayShort'), g_msg('label.wednesdayShort'), g_msg('label.thursdayShort'), g_msg('label.fridayShort'), g_msg('label.saturdayShort')]
	    , dayNamesMin: [g_msg('label.sundayShort'), g_msg('label.mondayShort'), g_msg('label.tuesdayShort'), g_msg('label.wednesdayShort'), g_msg('label.thursdayShort'), g_msg('label.fridayShort'), g_msg('label.saturdayShort')]
	    , yearSuffix: g_msg('label.year')
	    , showOn: "button"
	    , changeYear: true
	    , changeMonth: true
	    , yearRange: '-100:+100'
	    , beforeShow: function() {
	        setTimeout(function(){
		        $(".ui-datepicker select").select2();
	            $('.ui-datepicker').css('z-index', 101);
	        }, 0);
	    }
		, onSelect: function(dateText, inst){
			var startDate = new Date(dateText);
			var endDate = new Date($("#"+arrColId[1]).val());
			if(startDate>endDate){
				$("#"+arrColId[0]).val(inst.lastVal);
				alertMessage(g_msg('msg.cannotGreaterStartDate'), function(){	//시작일은 종료일 보다 클 수 없습니다.
					$(this).dialog("close");
				});
				return false;
			}
		}
	});

	$("#"+arrColId[0]).click(function() {
		$("#"+arrColId[0]).datepicker('show');
	 });

	if(2 === arrColId.length) {
		$("#"+arrColId[1]).datepicker({	//종료일
			dateFormat: 'yy-mm-dd'
				, prevText: g_msg('label.previousMonth')
				, nextText: g_msg('label.nextMonth')
				, maxDate: arrMaxDate[0]
		, minDate: arrMinDate[0]
		, monthNames: [g_msg('label.january'), g_msg('label.february'), g_msg('label.march'), g_msg('label.april'), g_msg('label.may'), g_msg('label.june'), g_msg('label.july'), g_msg('label.august'), g_msg('label.september'), g_msg('label.october'), g_msg('label.november'), g_msg('label.december')]
		, monthNamesShort: [g_msg('label.january'), g_msg('label.february'), g_msg('label.march'), g_msg('label.april'), g_msg('label.may'), g_msg('label.june'), g_msg('label.july'), g_msg('label.august'), g_msg('label.september'), g_msg('label.october'), g_msg('label.november'), g_msg('label.december')]
		, dayNames: [g_msg('label.sunday'), g_msg('label.monday'), g_msg('label.tuesday'), g_msg('label.wednesday'), g_msg('label.thursday'), g_msg('label.friday'), g_msg('label.saturday')]
		, dayNamesShort: [g_msg('label.sundayShort'), g_msg('label.mondayShort'), g_msg('label.tuesdayShort'), g_msg('label.wednesdayShort'), g_msg('label.thursdayShort'), g_msg('label.fridayShort'), g_msg('label.saturdayShort')]
		, dayNamesMin: [g_msg('label.sundayShort'), g_msg('label.mondayShort'), g_msg('label.tuesdayShort'), g_msg('label.wednesdayShort'), g_msg('label.thursdayShort'), g_msg('label.fridayShort'), g_msg('label.saturdayShort')]
		, yearSuffix: g_msg('label.year')
		, showOn: "button"
			, changeYear: true
			, changeMonth: true
			, yearRange: '-100:+100'
				, beforeShow: function() {
					setTimeout(function(){
				        $(".ui-datepicker select").select2();
						$('.ui-datepicker').css('z-index', 101);
					}, 0);
				}
				, onSelect: function(dateText, inst){
					var startDate = new Date($("#"+arrColId[0]).val());
					var endDate = new Date(dateText);
					if(startDate>endDate){
						$("#"+arrColId[1]).val(inst.lastVal);
						alertMessage(g_msg('msg.cannotLessStartDate'), function(){	//종료일은 시작일보다 작을 수 없습니다.
							$(this).dialog("close");
						});
						return false;
					}
				}
		});

		$("#"+arrColId[1]).click(function() {
			$("#"+arrColId[1]).datepicker('show');
		});
	}




//	var arrColId = (""!=value.columnId) ? value.columnId.split(',') : "";								//Tag ID
//	var arrColSize = (""!=value.columnSize) ? value.columnSize.split(',') : "";							//size
//	var arrDefValue = (""!=value.defaultValue) ? value.defaultValue.split(',') : "";					//DefaultValue
//	var arrMaxDate = (""!=value.maxDate) ? value.maxDate.split(',') : "";								//최대 선택일
//	var arrMinDate = (""!=value.minDate) ? value.minDate.split(',') : "";								//최소 선택일
//	var startTime = "";									 												//시간 선택 가능시 시작되는 시간
//	var endTime = "";									 												//시간 선택 가능 시 종료되는 시간
//	var defValue = getDateValue(arrDefValue[0]);		 												//Default 날짜 구하기
//	var pickTime = false;								 												//캘린더에서 시간 선택 사용 여부
//	var endDate = "";
//
//	if("datetime" == value.columnType){
//		defValue += " 00:00:00";
//		startTime = " 00:00:00";
//		endTime = " 23:59:59";
//		pickTime = true;
//	}
//
//	if(arrMaxDate.length > 0){
//		endDate = arrMaxDate[0];
//	}
//
//	$divCalendar = "<div class='input-append date' id='"+arrColId[0]+"'><input class='ui-corner-all' id='"+arrColId[0]+"' name='"+arrColId[0]+"' type='text' size='"+arrColSize[0]+"' value='"+defValue+"'>";
//	$divCalendar += "<span class='add-on'> <i class='icon-th'></i></span></div>";
//
//	defValue = getDateValue(arrDefValue[1]);
//	$divCalendar += "<span class='fromTo'>~</span><div class='input-append date' id='"+arrColId[1]+"'><input class='ui-corner-all' id='"+arrColId[1]+"' name='"+arrColId[1]+"' type='text' size='"+arrColSize[1]+"' value='"+defValue+"'>";
//	$divCalendar += "<span class='add-on'> <i class='icon-th'></i></span></div>";
//
//	$li.append($divCalendar);
//	$ul.append($li);
//
//	if(arrMaxDate.length > 1){						//캘린더에서 선택 가능한 마지막 날짜 및 시간
//		$.each(arrMaxDate, function(index, item){
//			if("today" == arrMaxDate[index]){
//				arrMaxDate[index] = getDateValue('today') + endTime;
//			}else{
//				arrMaxDate[index] = arrMaxDate[index] + endTime;
//			}
//		});
//	}
//
//	if(arrMinDate.length > 1){						//캘린더에서 선택 가능한 최소 날짜 및 시간
//		$.each(arrMinDate, function(index, item){
//			if("today" == arrMinDate[index]){
//				arrMinDate[index] = getDateValue('today') + endTime;
//			}else{
//				arrMinDate[index] = arrMinDate[index] + endTime;
//			}
//		});
//	}
//
//
//  	$("#"+arrColId[0]).datetimepicker({	//시작일
//  		format: 'yyyy-MM-dd'
//  		,autoclose: true
//  		,language: 'ko'
//  		,pickTime: pickTime
//  	}).on('changeDate', function(e){
//  		if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) {
//  			$('.bootstrap-datetimepicker-widget.dropdown-menu').hide();
//  			}
//  	});
//
////  	if(arrMaxDate.length > 1){
////  		$("#"+arrColId[0]).datetimepicker('setEndDate',  arrMaxDate[0]);
////		}
////  	if(arrMinDate.length > 1){
////  		$("#"+arrColId[0]).datetimepicker('startDate',  arrMinDate[0]);
////  	}
//
//  	$("#"+arrColId[1]).datetimepicker({	//종료일
//  		format: 'yyyy-MM-dd'
//  		,autoclose: true
//  		,language: 'ko'
//  		,pickTime: pickTime
//  	}).on('changeDate', function(e){ if('' === e.lastAction || 'select' === e.lastAction.substring(0,6)) { $('.bootstrap-datetimepicker-widget.dropdown-menu').hide(); } });
//
////  	if(arrMaxDate.length > 1){
////  		$("#"+arrColId[1]).datetimepicker('endDate',  arrMaxDate[1]);
////		}
////  	if(arrMinDate.length > 1){
////  		$("#"+arrColId[1]).datetimepicker('startDate',  arrMinDate[1]);
////  	}
}

/**
 * select Setting
 */
function setSelect(value){
	var searchType = -1;
//	$select = $("<select id='" + value.columnId + "_select' name='" + value.columnId + "' onchange='"+value.callFunc+"'></select>");
	$select = $("<select id='" + value.columnId + "_select' name='" + value.columnId + "' style='width:" + value.columnSize + "px;' onchange='"+value.callFunc+"'></select>");
//	$select = $("<select id='" + value.columnId + "_select' name='" + value.columnId + "' onchange='"+value.callFunc+"'></select>");
	if(!("nothing"==value.defaultText)){
		$option = $("<option value=''>선택하세요</option>");
		$select.append($option);
	}

	var comboCd = "";
	var queryId = "";
	if(!(""==value.queryId)){
		queryId = value.queryId;
	}else{	//combo 조회
		comboCd = value.systemClassGroup;
	}

	if('sessionUser' === value.defaultValue) {
		value.defaultValue = {
				value: USER_CD,
				text: USER_NM
		}
	}

	if('undefined' !== typeof DESTROY_REQUEST_USER_CD && '' !== DESTROY_REQUEST_USER_CD && 'destroyRequestUserCd' === value.columnId) {
		value.defaultValue = {
				value: DESTROY_REQUEST_USER_CD,
				text: DESTROY_REQUEST_USER_NM
		}
	}
	if('undefined' !== typeof APPROVAL_USER_CD && '' !== APPROVAL_USER_CD && 'approvalUserCd' === value.columnId) {
		value.defaultValue = {
				value: APPROVAL_USER_CD,
				text: APPROVAL_USER_NM
		}
	}


	$.ajax(
		    {
		        type: "POST",
		        url: CONTEXT_PATH + "/formInfo/getSelectList",
		        data: {
		        	"comboCd" : comboCd
		          , "queryId" :  queryId},
		        dataType: "json",
		        async: false,
		        success: function(result){
		        	var defaultValue = '';

		        	if(typeof value.defaultValue == 'string') {
		        		defaultValue = value.defaultValue;
		        	}
		        	else {
		        		defaultValue = value.defaultValue.value;
		        	}

		        	if('undefined' !== typeof STATUS_CD && '' !== STATUS_CD && 'statusCd' === value.columnId) {
		        		defaultValue = STATUS_CD;
		        	}

		        	if('undefined' !== typeof REQUEST_TYPE && '' !== REQUEST_TYPE && 'requestType' === value.columnId) {
		        		defaultValue = REQUEST_TYPE;
		        	}

		        	if('undefined' !== typeof PURCHASER_UID && '' !== PURCHASER_UID && 'purchaserUid' === value.columnId) {
		        		defaultValue = PURCHASER_UID;
		        	}

		        	$.each(result, function(index, item){
		        		if(defaultValue == item.comboVal){
		        			$option = $("<option value='"+item.comboVal+"' selected='selected'>"+item.comboLabel+"</option>");
		        		}else{
		        			$option = $("<option value='"+item.comboVal+"'>"+item.comboLabel+"</option>");
		        		}
		        		$select.append($option);
		    		});
		        	if(result.length >= 10){
		        		searchType = 1;
		        	}
		        },
		        error: function(x, e){
		             alert(x.readyState + " "+ x.status +" "+ e.msg);
		        }
		    });

	$selectInput = "";
	if("selectInput" == value.columnType || "selectSearch" == value.columnType){
		//input이 왜 필요한거지..
//		$selectInput = $("<input type='text' name='"+value.formId+"_input' id='"+value.formId+"_input' value='' style='width:"+value.columnSize+"px'/>")
//		$li.append($selectInput);
		searchType = 1;
	}
	$li.append($select);
	$li.append($selectInput);
	$ul.append($li);

	var option = {
		minimumResultsForSearch:searchType,
		language: 'ko'
		//	, allowClear: true
		//	, placeholder: '선택하세요'
	};


	if("selectSearch" == value.columnType){
		option["ajax"] = {
				url: value.searchUrl,
				delay: 250,
				defaultText: value.defaultText,
				dataType: 'json'
		};

		option["ajax"]["data"] = function(params){
			return {
				term: params.term || "",
		        page: params.page || 1
			}
		};

//		option["pagination"] = {
//		    more: true
//		  };

//		option["data"] =
	}

	$("#"+value.columnId+"_select").select2(option);

	if("selectSearch" == value.columnType) {
		var $ts = $("#"+value.columnId+"_select");
    	var selectedCode = [];
		var $newOption = $("<option></option>").val(value.defaultValue.value).text(value.defaultValue.text);
		$ts.append($newOption);
		selectedCode.push(value.defaultValue.value);
    	$ts.val(selectedCode).trigger("change");
    	$($("#"+value.columnId+"_select").data('select2').$container).addClass('searchSelect');
	}
	else if("selectInput" == value.columnType || searchType == 1){
		$($("#"+value.columnId+"_select").data('select2').$container).addClass('searchSelect');
	}

	//구매1,2팀  ILS1,2팀만 사업장 SETTING
    if('undefined' !== typeof DEPT_CD && ('D1DJ1F0' === DEPT_CD || 'D1DV1G0' === DEPT_CD || 'D1DV1G1' === DEPT_CD || 'D1DV1G2' === DEPT_CD || 'D1DC210' === DEPT_CD || 'D1DC220' === DEPT_CD)) {
		if('businessAreaCd' === value.columnId) {
			var $ts = $("#"+value.columnId+"_select");
			defaultValue = BUSINESS_AREA_CD;
			$ts.val(defaultValue).trigger("change");

			// 아래 옵션을 사용할 경우 submit 할 때 값을 가져오지 못함
			//$ts.select2("enable", false);

			$(".select2-selection").each(function() {
				var label = $(this).attr("aria-labelledby");

				if('select2-' + value.columnId + '_select-container' === label) {
					$(this).before('<div class="disabled-select"></div>')
				}

			});
		}
	}
}

function readonly_select(objs, action) {
    if (action===true)
        objs.prepend('<div class="disabled-select"></div>');
    else
        $(".disabled-select", objs).remove();
}

/**
 * 체크박스
 * @param value
 * @returns
 */
function setCheckbox(value){
	if(""==value.systemClassGroup){
		$li = $("<li></li>");
		$ul.append($li);
		$chkInput = $("<input type='checkbox' name='"+value.columnId+"' id='"+value.columnId+"'/>");
		$chkLabel = $("<label for='"+value.columnId+"'>"+value.columnName+"</label>");
		$li.append($chkInput);
		$li.append($chkLabel);
		$("#"+value.columnId).prettyCheckable({labelPosition:'left'});
	}else{
		var comboCd = value.systemClassGroup;
		$li = $("<li></li>");
		$ul.append($li);
		$.ajax(
			    {
			        type: "POST",
			        url: CONTEXT_PATH + "/formInfo/getCombo",
			        data: {"comboCd" : comboCd},
			        dataType: "json",
			        async: false,
			        success: function(result){
			        	$.each(result, function(index, item){
			        		var checkId = value.columnId+"_"+index;
			        		$chkInput = $("<input type='checkbox' name='"+value.columnId+"' id='"+checkId+"' value='" + item.comboVal + "' />");
			        		$chkLabel = $("<label for='"+checkId+"'>"+item.comboLabel+"</label>");
			        		$li.append($chkInput);
			        		$li.append($chkLabel);
			        		$("#"+checkId).prettyCheckable({labelPosition:'left'});

			        		// 출력기한임박, 유효기간 임박
			        		if(("termLimit" === value.columnId) && (TERM_LIMIT === item.comboVal)) {
			        			$('#'+checkId).prettyCheckable('check');
			        		}
			    		});

			        },
			        error: function(x, e){
			             alert(x.readyState + " "+ x.status +" "+ e.msg);
			        }
			    });
	}
}

/**
 * 라디오 버튼
 * @param value
 * @returns
 */
function setRadio(value){

	if(""==value.systemClassGroup){

	}else{
		var comboCd = value.systemClassGroup;
		$li = $("<li></li>");
		$ul.append($li);
		$.ajax(
			    {
			        type: "POST",
			        url: CONTEXT_PATH + "/formInfo/getCombo",
			        data: {"comboCd" : comboCd},
			        dataType: "json",
			        async: false,
			        success: function(result){
			        	$.each(result, function(index, item){
			        		var checkVal = "";
			        		var radioId = value.columnId+"_radio_"+index;
			        		if(0===index) checkVal = "checked='checked'";
			        		$radioInput = $("<input type='radio' id='"+radioId+"' name='"+value.columnId+"'" +
			        				"value='"+item.comboVal+"' "+checkVal+" onchange='"+value.callFunc+" />");
//	        						"value='"+item.comboVal+"' "+checkVal+" onchange='"+value.callFunc+"' style='width:"+value.columnSize+"px;'/>");
			        		$radioLabel = $("<label for='"+radioId+"'><span>"+item.comboLabel+"</span></label>");
			        		$li.append($radioInput);
			        		$li.append($radioLabel);

			        		$("#"+radioId).prettyCheckable();
			    		});
			        },
			        error: function(x, e){
			             alert(x.readyState + " "+ x.status +" "+ e.msg);
			        }
			    });
	}


}

function setSearchBtn($container, value){
	var param = {};

	if("" === value.callFunc) {
		value.callFunc = "searchList(gridParam)";
	}

	console.log(value);
	$button = $("<button class='ui-button ui-corner-all searchBtn' onclick='" + value.callFunc + "' type='button'>"+value.columnName+"</button>");
	$container.find(".btnBox").append($button);
}
