<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="custom" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="kr">
<head>
<meta charset="UTF-8">
<title>CollabHub</title>
<script>
var gridId = 'gridDocPdfLinkRequestList';
	function setGridParam(){
		gridParam = {
				gridId : gridId,
				formId : 'formDocPdfLinkRequest',
				url : '/inside/distribution/docPdfRequest/selectList',
				size : "" == $.trim(getCookie("rowNum")) ? 10 : $.trim(getCookie("rowNum")),
				page : 1,
				multiSelect : true,
				numbering : false,
				selectRowAction : 'check'
		}

		return gridParam;
	}

// 	function formatDocPdfFileList(cellValue, options, rowdata, action){
// 		return '<a onclick="openViewer(\''+rowdata["objectId"]+'\', \'DOCPDF\', \'OBJECT\', null)">'+cellValue+'</a>';
// 	}

function formatValidType(cellValue, options, rowdata, action){
	var rtn = "";
	if ( cellValue != undefined ) {
		rtn = cellValue;
	}
	return '<font color="red">'+ rtn +'</font>';
}

function formatConvert(cellValue, options, rowdata, action){
	alert('파일명:'+cellValue);

	//var ret = window.open('selectItem?file='+encodeURI(cellValue), '_blank', '', false);
	var ret = window.open('selectItem?file='+cellValue, '_blank', '', false);
	
	//return '<a onclick="alert('a');">'+cellValue+'</a>';
	//return '<a onclick="openFile(\'OBJECT\', \'DOCPDF\', null, \'' + rowdata["objectId"] +'\', \'' + rowdata["fileNo"] + '\', \'' + rowdata["protectYn"] + '\')">'+cellValue+'</a>';
	//return '<a onclick="openFile()">'+cellValue+'</a>';
	
}
function formatConvert2(cellValue, options, rowdata, action){
	alert('파일명2:'+cellValue);
	
	var ret = window.open('about:blank', 'esobWin', '', false);
	var frm = document.frmDocPdfList;
	frm.file.value = ""+cellValue;
	frm.action = "selectItem";
	frm.target = "esobWin";
	frm.submit();
	
}

function formatProtect(cellValue, options, rowdata, action){
	if(cellValue == "Y"){
		return '<a onclick="openDialog(\'' + rowdata["objectId"] + '\')">'+cellValue+'</a>';
	}else{
		return cellValue;
	}
}

function openDialog(objectId){
	openDialogPopup("/inside/distribution/commonRequest/protectPopup", {objectId: objectId, objectType : "DOCPDF"}, "popupDialog", 'm', 360);
}


</script>
</head>
<body>

<br>
<br>

		<table> 
			<tr>
				<td width="100" align="left">
				
        <c:forEach var="dish" items="${menu1}">

               <li> ${dish }</li>

        </c:forEach>	
				
				</td>
			</tr>	
		</table>		
				
				
	<form name="frmDocPdfList">
		<input type="hidden" name="file" value="">
		<table> 
			<tr>
				<td width="100" align=""center"">&nbsp;1</td>
				<td width="150" align="left">1A-5.dwg</td>
				<td width="150" align="left"><input type="button" value="1A-5.dwg Pdf Viewer" 
					onclick="formatConvert2('1A-5.dwg','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;2</td>
				<td width="150" align="left">AJ_Digital_Camera.svg</td>
				<td width="150" align="left"><input type="button" value="AJ_Digital_Camera.svg Pdf Viewer" 
					onclick="formatConvert2('AJ_Digital_Camera.svg','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;3</td>
				<td width="150" align="left">fwedges.plt</td>
				<td width="150" align="left"><input type="button" value="fwedges.plt Pdf Viewer" 
					onclick="formatConvert('fwedges.plt','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;4</td>
				<td width="150" align="left">HPGL1.plt</td>
				<td width="150" align="left"><input type="button" value="HPGL1.plt Pdf Viewer" 
					onclick="formatConvert('HPGL1.plt','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;5</td>
				<td width="150" align="left">HPGL2.plt</td>
				<td width="150" align="left"><input type="button" value="HPGL2.plt Pdf Viewer" 
					onclick="formatConvert('HPGL2.plt','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;6</td>
				<td width="150" align="left">HPGL2.plt.pdf</td>
				<td width="150" align="left"><input type="button" value="HPGL2.plt.pdf Pdf Viewer" 
					onclick="formatConvert('HPGL2.plt.pdf','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;7</td>
				<td width="150" align="left">sample.docx</td>
				<td width="150" align="left"><input type="button" value="sample.docx Pdf Viewer" 
					onclick="formatConvert('sample.docx','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;8</td>
				<td width="150" align="left">sample.dwg</td>
				<td width="150" align="left"><input type="button" value="sample.dwg Pdf Viewer" 
					onclick="formatConvert('sample.dwg','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;9</td>
				<td width="150" align="left">sample.dxf</td>
				<td width="150" align="left"><input type="button" value="sample.dxf Pdf Viewer" 
					onclick="formatConvert('sample.dxf','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;10</td>
				<td width="150" align="left">sample.hwp</td>
				<td width="150" align="left"><input type="button" value="sample.hwp Pdf Viewer" 
					onclick="formatConvert('sample.hwp','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;11</td>
				<td width="150" align="left">sample.pptx</td>
				<td width="150" align="left"><input type="button" value="sample.pptx Pdf Viewer" 
					onclick="formatConvert('sample.pptx','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;12</td>
				<td width="150" align="left">Sample.SVG</td>
				<td width="150" align="left"><input type="button" value="Sample.SVG Pdf Viewer" 
					onclick="formatConvert('Sample.SVG','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;13</td>
				<td width="150" align="left">sample.tif</td>
				<td width="150" align="left"><input type="button" value="sample.tif Pdf Viewer" 
					onclick="formatConvert('sample.tif','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;14</td>
				<td width="150" align="left">sample.xlsx</td>
				<td width="150" align="left"><input type="button" value="sample.xlsx Pdf Viewer" 
					onclick="formatConvert('sample.xlsx','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;15</td>
				<td width="150" align="left">sample_640×426.bmp</td>
				<td width="150" align="left"><input type="button" value="sample_640×426.bmp Pdf Viewer" 
					onclick="formatConvert2('sample_640x426.bmp','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;16</td>
				<td width="150" align="left">sample_640×426.gif</td>
				<td width="150" align="left"><input type="button" value="sample_640×426.gif Pdf Viewer" 
					onclick="formatConvert('sample_640x426.gif','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;17</td>
				<td width="150" align="left">sample_640×426.jpg</td>
				<td width="150" align="left"><input type="button" value="sample_640×426.jpg Pdf Viewer" 
					onclick="formatConvert2('sample_640x426.jpg','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;18</td>
				<td width="150" align="left">sample_640×426.png</td>
				<td width="150" align="left"><input type="button" value="sample_640×426.png Pdf Viewer" 
					onclick="formatConvert('sample_640x426.png','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;19</td>
				<td width="150" align="left">sample1.dxf</td>
				<td width="150" align="left"><input type="button" value="sample1.dxf Pdf Viewer" 
					onclick="formatConvert('sample1.dxf','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>

			<tr>
				<td width="100" align="center">&nbsp;20</td>
				<td width="150" align="left">spectrum.plt</td>
				<td width="150" align="left"><input type="button" value="spectrum.plt Pdf Viewer" 
					onclick="formatConvert('spectrum.plt','','','')"></td>
				<td width="600">&nbsp;</td>
			</tr>		
			<tr>
				<td><br>&nbsp;</td>
			</tr>
		</table>
	</form>
	

<br>
<br>
<!-- 
<h1>원본파일 폴더 경로 : C:\workspaceStartupHub\OUT\orgFile</h1>	
<h1>변환파일 폴더 경로 : C:\workspaceStartupHub\OUT\destFile</h1>



1A-5.dwg
AJ_Digital_Camera.svg
fwedges.plt
HPGL1.plt
HPGL2.plt
HPGL2.plt.pdf
sample.docx
sample.dwg
sample.dxf
sample.hwp
sample.pptx
Sample.SVG
sample.tif
sample.xlsx
sample_640×426.bmp
sample_640×426.gif
sample_640×426.jpg
sample_640×426.png
sample1.dxf
spectrum.plt
	-->
	<div id="searchAllPopup" class="dialogContainer"></div>
</body>
</html>
