
var isComeMsg = false;
var isComeAgent = false;

//브라우저 종류 검증 
function getBrowserType() {
	var userAgent = window.navigator.userAgent;
	var result = 'Unknown';

	if (  userAgent.indexOf('MSIE') != -1 || userAgent.indexOf('Trident') != -1) {
		result = 'IE';
	} else if (userAgent.indexOf('Edg') != -1) {
		if (userAgent.indexOf('Edge') != -1){
			result = 'Edge';
		}else{
			result = 'Chrome_Edge'
		}
	} else if (userAgent.indexOf('Firefox') != -1) {
		result = 'Firefox';
	} else if (userAgent.indexOf('Opera') != -1 || userAgent.indexOf('OPR') != -1) {
		result = 'Opera';
	} else if (userAgent.indexOf('Whale') != -1) {
		result = 'Whale';
	} else if (userAgent.indexOf('Vivaldi') != -1) {
		result = 'Vivaldi';
	} else if (userAgent.indexOf('Chrome') != -1) {
		result = 'Chrome';
	} else if (userAgent.indexOf('Safari') != -1) {
		result = 'Safari';
	}

	return result;
}
		

//설치유무판단, Extern용[외부그룹웨어서버 측에서 CALL해야하는 함수]
//설치유무특성상 내부용서버를 기본값으로 한다. isOutServer는 false 로 지정하여 호출
function GetIsKess(remote, isOutServer)
{		
	console.log('start GetIsKess!!');
	var brower = getBrowserType();
	var remote_server = remote;
	var isOutterRegion = false;	
	console.log(brower);
	
	//최초 LGDISPLAY에서 외부,내부용 서버주소를 다르게 하여 급히 분리를 위해 설정 
	//LGD에는 아직 해당 코드가 적용, 신규 패치를 위해서는 연동업체에서 CALL을 다르게 해야함!
	//if( remote_server.indexOf('https://nqacloude.lgdisplay.com') != -1  )
	//	isOutterRegion = true; 
	isOutterRegion = isOutServer;

	return new Promise(function(resolve,reject){ //Promise START 

		
		window.addEventListener('message', function(event)
		{  //Listner START
			
			if(event)
			{
				var origintext =  event.origin;
				
				if(event.origin =='http://localhost:57475')
				{					
					isComeMsg = true;
					console.log('msg come!!');
					console.log(event.data);
					console.log(event.origin);
				}
				else if(origintext.indexOf('chrome-extension://') != -1)  
				{//특이CASE: 크롬확장모듈시 들어온메시지 날려버리기 위한용도!
					console.log('find-chrome-extension, msg to Self!');
					postMessage('SelfInstallCheck', remote_server);
					return;
				}
				else if(origintext.indexOf(remote_server) != -1)
				{
					//부모에게 온 메시지의 경우 Nothing 처리 
					console.log('Ready To TimeOut~!!');
					return;
				}
			}
			
			if(isComeMsg)
			{
				
				if(event.origin =='http://localhost:57475' )
				{
					isComeAgent = true;
					
					if(event.data == 'installed')
					{
						resolve(true);
					}
				}					
			}
			else
			{
				console.log('not event!');
			}
				
		}); //Listener END 	
		
		
		if('IE' != brower)
		{   //IE 가 아닌경우 메시지를 받는방식으로 처리 
			var time = setTimeout(function(){
			//if(!isComeMsg) //크롬의경우 chrome-extension msg가 기본들어오므로 timeout체크는 agent한테 메시지가 온경우로 한정한다.
			{
				if(!isComeAgent)
				{
					console.log('time_out');
					reject(false);
				}
			}
			}, 3000); //3초뒤 까지도 kesm_agent로부터 메시지가 오지않을시 rject처리 

			var iframe = document.createElement("iframe");
			iframe.id = "iframe_kess";
			iframe.name = "Parent";
			iframe.hidden = true;
			iframe.style.width = "400px";
			iframe.style.height = "400px";	
			if(isOutterRegion)
			{
				iframe.src = "http://localhost:57475/kess/INSTALL/OUTTER?" + (new Date).getTime();
			}
			else
			{
				iframe.src = "http://localhost:57475/kess/INSTALL/INNER?" + (new Date).getTime();
			}

			document.body.appendChild(iframe);
						
		}
		else
		{  //IE 브라우저의 경우 이전방식 XHR 으로 요청
			var  addr = "";
			if(isOutterRegion)
			  addr = "http://localhost:57475"+"/kess/INSTALL/OUTTER?";	
			else 
			  addr = "http://localhost:57475"+"/kess/INSTALL/INNER?";			
			
			var xhr = new XMLHttpRequest();
			xhr.open('GET',addr + (new Date()).getTime(), true);
			xhr.onreadystatechange = function() //onreadystatechange START
			{
			 if (xhr.readyState == XMLHttpRequest.DONE)
			 {			  
			  if( xhr.status == 200)
			  {
				 console.log(xhr.responseText);

				if(xhr.responseText.indexOf("installed") != -1)
				{
					resolve(true);	
				}
			  }	  
			 }
			}; //onreadystatechange END 
			
			xhr.send(null);  
		} //IF IE END 	 									
	});//Promise END 	
}

function checkChrome(origintext,remote_server)
{
	if(origintext.indexOf('chrome-extension://') != -1)  
	{//특이CASE: 크롬확장모듈시 들어온메시지 날려버리기 위한용도!
		console.log('find-chrome-extension, msg to Self!');
		postMessage('SelfInstallCheck', remote_server);
		return false;
	}
	else if(origintext.indexOf(remote_server) != -1)
	{
		//부모에게 온 메시지의 경우 Nothing 처리 
		console.log('Ready To TimeOut~!!');
		return false;
	}
	return true;
}

//설치유무판단, Extern용[외부그룹웨어서버 측에서 CALL해야하는 함수]
//설치유무특성상 내부용서버를 기본값으로 한다. isOutServer는 false 로 지정하여 호출
function CallPBExtension(remote, param)
{		
	
	var remote_server = remote;
	var isOutterRegion = false;	
	var CallParam = param;

	console.log('start CallPBExtension!!');
	console.log(CallParam);

	return new Promise(function(resolve,reject)
	{ //Promise START 

		
		window.addEventListener('message', function(event)
		{  //Listner START
			
			if(event)
			{
				var origintext =  event.origin;
				
				if(event.origin =='http://localhost:57475')
				{					
					isComeMsg = true;
					console.log('CallPBExtension msg come!!');
					console.log(event.data);
					console.log(event.origin);
				}
				
				if(checkChrome(origintext,remote_server) == false)
				{
					return;
				}
			}
			
			if(isComeMsg)
			{
				if(event.origin =='http://localhost:57475' )
				{
					isComeAgent = true;
					
					if(event.data == 'succeed')
					{
						resolve(true);
					}
				}					
			}
			else
			{
				console.log('not event!');
			}
				
		}); //Listener END 	
		
		
		   //IE 가 아닌경우 메시지를 받는방식으로 처리 
			var time = setTimeout(function(){
			if(!isComeMsg) //크롬의경우 chrome-extension msg가 기본들어오므로 timeout체크는 agent한테 메시지가 온경우로 한정한다.
			{
				if(!isComeAgent)
				{
					console.log('time_out');
					reject(false);
				}
			}
			}, 3000); //3초뒤 까지도 kesm_agent로부터 메시지가 오지않을시 rject처리 

			var iframe = document.createElement("iframe");
			iframe.id = "iframe_kess_pbextension";
			iframe.name = "Parent_pbextension";
			iframe.hidden = true;
			iframe.style.width = "400px";
			iframe.style.height = "400px";	
		
			iframe.src = "http://localhost:57475/kess/PB_EXTENSION?" + CallParam;
			
			
			document.body.appendChild(iframe);
						
		
	});
}

//설치유무판단, Extern용[외부그룹웨어서버 측에서 CALL해야하는 함수]
//설치유무특성상 내부용서버를 기본값으로 한다. isOutServer는 false 로 지정하여 호출
function VersionCheck(remote, version)
{		
	
	var remote_server = remote;
	var isOutterRegion = false;	
	var checkVersion = version;

	console.log('start VersionCheck!!');
	console.log(checkVersion);

	return new Promise(function(resolve,reject)
	{ //Promise START 

		
		window.addEventListener('message', function(event)
		{  //Listner START
			
			if(event)
			{
				var origintext =  event.origin;
				
				if(event.origin =='http://localhost:57475')
				{					
					isComeMsg = true;
					console.log('VersionCheck msg come!!');
					console.log(event.data);
					console.log(event.origin);
				}
				
				if(checkChrome(origintext,remote_server) == false)
				{
					return;
				}
			}
			
			if(isComeMsg)
			{
				if(event.origin =='http://localhost:57475' )
				{
					isComeAgent = true;
					
					if(event.data == 'version_same')
					{
						resolve(true);
					}
					else if(event.data == 'version_difference')
					{
						reject(false);
					}
				}					
			}
			else
			{
				console.log('not event!');
			}
				
		}); //Listener END 	
		
		
		   //IE 가 아닌경우 메시지를 받는방식으로 처리 
			var time = setTimeout(function(){
			if(!isComeMsg) //크롬의경우 chrome-extension msg가 기본들어오므로 timeout체크는 agent한테 메시지가 온경우로 한정한다.
			{
				if(!isComeAgent)
				{
					console.log('time_out');
					reject(false);
				}
			}
			}, 3000); //3초뒤 까지도 kesm_agent로부터 메시지가 오지않을시 rject처리 

			var iframe = document.createElement("iframe");
			iframe.id = "iframe_kess_VersionCheck";
			iframe.name = "Parent_VersionCheck";
			iframe.hidden = true;
			iframe.style.width = "400px";
			iframe.style.height = "400px";	
		
			iframe.src = "http://localhost:57475/kess/CHECKVERSION?" + checkVersion;
			
			
			document.body.appendChild(iframe);
						
		
	});
}