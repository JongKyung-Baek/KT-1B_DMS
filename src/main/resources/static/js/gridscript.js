$(document).ready(function(){
/*
	$(window).on('resize.jqGrid', function () {
		$("#jqGrid1").jqGrid('setGridWidth', $("#jqGrid1").parents('.ui-jqgrid').parent('div').width() );
		$("#jqGrid2").jqGrid('setGridWidth', $("#jqGrid2").parents('.ui-jqgrid').parent('div').width() );
	});

	$(function(){
		$("#jqGrid1").jqGrid({
			datatype: "local",
			colNames:['자료번호','자료명','승인일'],
			colModel:[
				{name:'col1', index:'col1', width: "80"},
				{name:'col2', index:'col2'},
				{name:'col3', index:'col3', width: "80"},
			],
			autowidth:true,
			caption: false
		});

		var mydata1 = [
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-27 13:53:12"}, //1
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-26 09:12:48"}, //2
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-25 13:53:12"}, //3
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-24 09:12:48"}, //4
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-23 13:53:12"}, //5
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-22 09:12:48"}, //6
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-21 13:53:12"}, //7
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-20 09:12:48"}, //8
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-15 13:53:12"}, //9
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-13 09:12:48"}, //10
			];
		for(var i=0;i<=mydata1.length;i++){
			$("#jqGrid1").jqGrid('addRowData',i+1,mydata1[i]);
		}
	})
	$(function(){
		$("#jqGrid2").jqGrid({
			datatype: "local",
			colNames:['자료번호','자료명','승인일'],
			colModel:[
				{name:'col1', index:'col1', width: "80"},
				{name:'col2', index:'col2'},
				{name:'col3', index:'col3', width: "80"},
			],
			autowidth:true,
			caption: false
		});

		var mydata2 = [
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-27 13:53:12"}, //1
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-26 09:12:48"}, //2
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-25 13:53:12"}, //3
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-24 09:12:48"}, //4
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-23 13:53:12"}, //5
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-22 09:12:48"}, //6
			{col1:"A1234567",col2:"하우징, 전자구성품용하우징, 전자구성품용하우징",col3:"2019-08-21 13:53:12"}, //7
			{col1:"34567891",col2:"덥개, 입구용덥개, 입구용덥개",col3:"2019-08-20 09:12:48"}, //8
			];
		for(var i=0;i<=mydata2.length;i++){
			$("#jqGrid2").jqGrid('addRowData',i+1,mydata2[i]);
		}
	})
	$(function(){
		$(".jqGridNotice").jqGrid({
			datatype: "local",
			colNames:['제목','등록일'],
			colModel:[
				{name:'col1', index:'col1'},
				{name:'col2', index:'col2', width: "40"},
			],
			autowidth:true,
			caption: false
		});

		var mydatanotice = [
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"notice정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"notice인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			];
		for(var i=0;i<=mydatanotice.length;i++){
			$(".jqGridNotice").jqGrid('addRowData',i+1,mydatanotice[i]);
		}
	})
	$(function(){
		$(".jqGridQnA").jqGrid({
			datatype: "local",
			colNames:['제목','등록일'],
			colModel:[
				{name:'col1', index:'col1'},
				{name:'col2', index:'col2', width: "40"},
			],
			autowidth:true,
			caption: false
		});

		var mydataqnA = [
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			{col1:"qnA정보시스템 구축 발주자를 위한 표준설계서 수정안",col2:"2019-08-26"},
			{col1:"qnA인사개편에 따른 업무프로세스 변경 및 공지사항 전달",col2:"2019-08-27"},
			];
		for(var i=0;i<=mydataqnA.length;i++){
			$(".jqGridQnA").jqGrid('addRowData',i+1,mydataqnA[i]);
		}
	})
*/

/* ----- mydoc ----- */
	$(function(){

		var mydata3 = [
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-04-11",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},

			];

		$("#jqGrid3").jqGrid({
			data: mydata3,
			datatype: "local",
			colNames:['사업구분','배포유형','도번','도명','REV','페이지','총<br>페이지','기종코드','ECN','사업장<br>구분','방산<br>기술자료','등록자','등록팀','등록일','규격화<br>승인일','기술변경<br>승인일','CC번호','상태','업체명','구매<br>담당자',],
			colModel:[
				{name:'col1', index:'col1', width:'80'},
				{name:'col2', index:'col2', width:'80'},
				{name:'col3', index:'col3', width:'100'},
				{name:'col4', index:'col4', width:'200'},
				{name:'col5', index:'col5', width:'50'},
				{name:'col6', index:'col6', width:'60'},
				{name:'col7', index:'col7', width:'60'},
				{name:'col8', index:'col8', width:'70'},
				{name:'col9', index:'col9', width:'60'},
				{name:'col10', index:'col10', width:'70'},
				{name:'col11', index:'col11', width:'80'},
				{name:'col12', index:'col12', width:'80'},
				{name:'col13', index:'col13', width:'100'},
				{name:'col14', index:'col14', width:'100'},
				{name:'col15', index:'col15', width:'100'},
				{name:'col16', index:'col16', width:'100'},
				{name:'col17', index:'col17', width:'70'},
				{name:'col18', index:'col18', width:'80'},
				{name:'col19', index:'col19', width:'100'},
				{name:'col20', index:'col20', width:'80'},
			],
			height : 'auto',
			rowNum : 10,
			rownumbers : true,
			multiselect : true,
			caption : false,
			loadtext : /*'<img src=''/>'*/ 'loading~~',
			pager : '#pagerArea',
		    rowList: [15,30,50,100],        // disable page size dropdown
		    pgbuttons: true,
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true,
		    loadComplete: function(data) {
		    	initPage("jqGrid3", "pagerArea", true, "TOT");
		    }
		});

//		for(var i=0;i<=mydata3.length;i++){
//			$("#jqGrid3").jqGrid('addRowData',i+1,mydata3[i]);
//		}
	})

/* ----- external0101 ----- 
	$(function(){
		$("#jqGrid4").jqGrid({
			datatype: "local",
			colNames:['요청번호','도면번호','도면명','REV','용도','업체담당자','구매담당자','요청일','승인일','진행상태'],
			colModel:[
				{name:'col1', index:'col1', width:'80'},
				{name:'col2', index:'col2', width:'80'},
				{name:'col3', index:'col3', width:'80'},
				{name:'col4', index:'col4', width:'80'},
				{name:'col5', index:'col5', width:'80'},
				{name:'col6', index:'col6', width:'80'},
				{name:'col7', index:'col7', width:'80'},
				{name:'col8', index:'col8', width:'80'},
				{name:'col9', index:'col9', width:'80'},
				{name:'col10', index:'col10', width:'80'},
			],
			height : 'auto',
			rowNum : 10,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : 'loading~~',
			pager : '#pagerArea',
		    rowList: [15,30,50,100],        // disable page size dropdown
		    pgbuttons: true,
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true,
		    loadComplete: function(data) {
		    	initPage("jqGrid4", "pagerArea", true, "TOT");
		    }
		});

		var mydata = [
			{col1:"개발",col2:"원도",col3:"FOE21056",col4:"하우징,전자 구성품 용",col5:"A",col6:"3",col7:"3",col8:"LL54",col9:"",col10:"1사업장",col11:"Y",col12:"홍길동",col13:"발사체계3팀",col14:"2019-08-27",col15:"",col16:"",col17:"",col18:"",col19:"",col20:""},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGrid4").jqGrid('addRowData',i+1,mydata[i]);
		}
	})*/

	/* ----- external0102 ----- 
	$(function(){
		$("#jqGrid5").jqGrid({
			datatype: "local",
			colNames:['사업구분','배포유형','승인번호','도면번호','도면명','파일명','REV','페이지','총페이지','용도','구매담당자','유효기간','요청일','승인일','기종','방산기술','폐기구분','첨부파일'],
			colModel:[
				{name:'col1', index:'col1', width:'80'},
				{name:'col2', index:'col2', width:'80'},
				{name:'col3', index:'col3', width:'80'},
				{name:'col4', index:'col4', width:'80'},
				{name:'col5', index:'col5', width:'80'},
				{name:'col6', index:'col6', width:'80'},
				{name:'col7', index:'col7', width:'80'},
				{name:'col8', index:'col8', width:'80'},
				{name:'col9', index:'col9', width:'80'},
				{name:'col10', index:'col10', width:'80'},
				{name:'col11', index:'col11', width:'80'},
				{name:'col12', index:'col12', width:'80'},
				{name:'col13', index:'col13', width:'80'},
				{name:'col14', index:'col6', width:'80'},
				{name:'col15', index:'col15', width:'80'},
				{name:'col16', index:'col16', width:'80'},
				{name:'col17', index:'col17', width:'80'},
				{name:'col18', index:'col18', width:'80'},
			],
			height : 'auto',
			rowNum : 10,
			rownumbers : true,
			multiselect : false,
			caption : false,
			loadtext : 'loading~~',
			pager : '#pagerArea',
		    rowList: [15,30,50,100],        // disable page size dropdown
		    pgbuttons: true,
            viewsortcols : [ false, 'horizontal', true ],
		    viewrecords: true,
		    loadComplete: function(data) {
		    	initPage("jqGrid5", "pagerArea", true, "TOT");
		    }
		});var mydata = [
			{col1:"양산",col2:"VIEWING",col3:"42354",col4:"23452",col5:"조립도",col6:"ASFE.PLT",col7:"A",col8:"4",col9:"5",col10:"검토용",col11:"김구매",col12:"2019-01-01",col13:"2019-01-01",col14:"2019-01-01",col15:"6701",col16:"",col17:"폐기중",col18:"<a class='gridLinkBtn' onclick='openDialog2()'>1건</a>"},
			];
		for(var i=0;i<=mydata.length;i++){
			$("#jqGrid5").jqGrid('addRowData',i+1,mydata[i]);
		}
	})*/







});