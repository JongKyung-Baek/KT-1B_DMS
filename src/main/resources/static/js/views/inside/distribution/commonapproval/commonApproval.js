	function approval(){
		batchCall("A");
	}
	function reject(){
		batchCall("R");
	}

	function batchCall(saveType){
		var list = [];
		if($("#" + gridId).getGridParam('selarrrow').length < 1){
			alertMessage(g_msg('msg.noSelectedItem'));
			return false;
		}
		$.each($("#" + gridId).getGridParam('selarrrow'), function(index, item){
			var data = $("#" + gridId).jqGrid('getRowData', item);
			list.push({requestNo: data.hiddenRequestNo});
		});
		var param = {list:list};
		param.saveType = saveType;
		callAjax( param, "/inside/distribution/batchSaveApproval", batchCallback, 'json');
	}

	function batchCallback(response){
		console.log(response.result.success);
		if(response.result.success){
			infoMessage(g_msg('msg.requestComplete'), function(){
				searchList(gridParam);
				$(this).dialog("close");
			});
		}else{
			alertMessage(g_msg("msg.requestFailure"));
		}
	}