function settingTree(id, param) {
	try {
		$("#" + id).jstree('destroy');
	}
	catch(e) {}

	var p = {
			'core': {
				data: []
			},
			'types': {
			}
	};

	var initTreeData = function(param) {
		$.each(param.list, function() {
			var level = parseInt(this.level, 10);
			var ts = this;
			var tmp = {
					opened: false,
					hidden: false
			};

			if(0 == param.openLevel || level <= param.openLevel || 0 == level) {
				tmp.opened = true;
			}
			else {
				tmp.opened = false;
			}

			if(param.customCheckbox) {
				if('MENU_000' !== ts.id) {
					ts.text = '<div class="tree-checkbox" id="chk_' + ts.id + '"></div>' + ts.text;
				}
			}

			if(ts.type === 'P') {
				ts["icon"] = "jstree-detailview";
			}

//			if(ts.visible) {
//				tmp.hidden = false;
//			}
//			else {
//				tmp.hidden = true;
//			}

			ts.state = tmp;
		});
	}

	if(param.textMaxLength > -1) {
		p.core['textMaxLength'] = textMaxLength
	}

	if(true === param.multiSelect) {
		p.core['multiple'] = multiSelect
	}

	//console.log(param);

	initTreeData(param);

	p.core.data = param.list;

	if(param.dragDrop) {
		if("deptTree" == id) {
			p.core['check_callback'] = function(operation, node, node_parent, node_position, more) {
				if(operation === 'move_node') {
					if(node_parent.id === '#') {
						return false;
					}
				}
			}
		}
		else if("insideMenuTree" == id || "outsideMenuTree" == id) {
			p.core['check_callback'] = function(operation, node, node_parent, node_position, more) {
				if(operation === 'move_node') {
				//	console.log(node);

					// 팝업은 위치를 못바꾼다.
					if("P" === node.original.type) {
						return false;
					}

//					// 부모는 못바꾸고 순서만 바꿀 수 있게 하는 코드.
//					if(node_parent.id !== node.parent) {
//						return false;
//					}
				}
			}
		}
		else {
			p.core['check_callback'] = true;
		}
	}

	if(param.maxDepth > -1) {
		p.type['#'] = {'max_depth': maxDepth};
	}

	var plugins = ['search', 'wholerow'];

	if(param.dragDrop) {
		plugins.push('dnd');
		plugins.push('types');
	}

	if(param.useCheckbox) {
		plugins.push('checkbox');
	}

	if(false !== param.noClose) {
		plugins.push('noclose');
	}


	if(plugins.length > 0) {
		p['plugins'] = plugins;
	}

	//console.log(p);


	$("#" + id)
		.jstree(p)
		.on('ready.jstree', function (e, data, selected, event) {
			if(undefined !== param.selectedValue && "" !== param.selectedValue) {
				var ref = $('#' + id).jstree(true);

				if('string' === typeof param.selectedValue) {
					param.selectedValue = JSON.parse(param.selectedValue);
				}

				ref.select_node(param.selectedValue);
			}
		});

	if(param.useCheckbox) {
		$("#" + id).on('changed.jstree check_node.jstree uncheck_node.jstree', function (e, data) {
			if(data.node) {
				if('undefined' !== typeof changeCheckbox) {
					changeCheckbox();
				}
			}
		});
	}

}
