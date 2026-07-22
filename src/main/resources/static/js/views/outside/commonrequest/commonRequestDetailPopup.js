function save(){
	var selrow = $('#'+detailPopupGridId).jqGrid('getGridParam', 'selarrrow');
	if(selrow.length == 0){
		alertMessage(g_msg('msg.noSelectedItem'));
		return;
	}
	$('#'+popupGridId).jqGrid('delRowData', itemInfo.rowId);
	for(i=selrow.length - 1; i>=0; i--){
		var selectedItem = $('#'+detailPopupGridId).jqGrid('getRowData', selrow[i]);
		selectedItem.inspectionYn = 'Y';
		selectedItem.duplicateYn = 'N';
		$('#'+popupGridId).jqGrid('addRowData', itemInfo.rowId+selrow[i], selectedItem, 'first');
		setCheck(itemInfo.rowId+selrow[i]);
	}
	$('#detailPopupDialoga').dialog('close');
}

function setCheck(id){
	$('#'+popupGridId).jqGrid('setSelection', id);
	$("#jqg_" + popupGridId + "_" + id).each(function() {
		$(this).prettyCheckable("check");
	});
}

