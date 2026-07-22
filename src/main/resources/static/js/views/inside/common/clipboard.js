

function pasteForChrome(){
	navigator.permissions.query({
		name: 'clipboard-read'
	}).then(permissionStatus => {
		// Will be 'granted', 'denied' or 'prompt':
		console.log(permissionStatus.state);
		// Listen for changes to the permission state
		permissionStatus.onchange = () => {
			console.log(permissionStatus.state);
		};
		navigator.clipboard.readText().then(text => {
			pasteText(text);
		})
	}).catch(err => {
		console.error('Failed to read clipboard contents: ', err);
	});
}