$(document).ready(function(){
	window.requestFileSystem  = window.requestFileSystem || window.webkitRequestFileSystem;

	var connections = [];
	var startTime = 0;
	var endTime = 0;
	var fileCounter = 0;
	var data = [];
	var bb = new window.WebKitBlobBuilder(); 

	
	function connect(server, port){
		var ws = new WebSocket('ws://' + server + ':' + port +'/bilkenttransfer');
		showMessage('Connecting to ' + server + ':' + port);	  
		ws.onopen = function() { 
			showMessage('Connected to ' + server + ':' + port ) 
			showMessage('Requesting file.');
			startTime = new Date().getTime();
			sendMessage(ws, "REQ;" + fileCounter);
		};
		ws.onclose = function() { showMessage('Lost connection to ' + server + ':' + port ); };
		ws.onmessage = function(msg) { 
			$("#bar").val(++fileCounter);
			bb.append(msg.data);
			if ( fileCounter == 121){
				writeData();	
			}
		};	
		connections.push(ws);
	}	
	
	function sendMessage(websocket, type){
		websocket.send(type);			
	}
	
	$("#connectBtn").click( function(){
		var ip = $("#IP").val();
		var port = $("#PORT").val();
		connect(ip, port);
	});
	
	var data = [];
	



	function onInitFs(fs){		
	  fs.root.getFile('aaaa.mp3', {create: true}, function(fileEntry) {		 
		fileEntry.createWriter(function(fileWriter) {
		  fileWriter.onwriteend = function(e) {
			showMessage('Write completed.');
			endTime = new Date().getTime();
			showMessage("Took: " + ( endTime - startTime));			
		  };
		  fileWriter.onerror = function(e) {
			showMessage('Write failed: ' + e.toString());
		  };
		  console.log(bb.getBlob());
		  fileWriter.write(bb.getBlob());			
		}, errorHandler);
		console.log( fileEntry.toURL());
	  }, errorHandler);
	}
	
	function writeData(){
		window.requestFileSystem(window.PERSISTENT, 50*1024*1024, onInitFs, errorHandler);	
	}

	function errorHandler(e) {
	  var msg = '';
	  switch (e.code) {
		case FileError.QUOTA_EXCEEDED_ERR:
		  msg = 'QUOTA_EXCEEDED_ERR';
		  break;
		case FileError.NOT_FOUND_ERR:
		  msg = 'NOT_FOUND_ERR';
		  break;
		case FileError.SECURITY_ERR:
		  msg = 'SECURITY_ERR';
		  break;
		case FileError.INVALID_MODIFICATION_ERR:
		  msg = 'INVALID_MODIFICATION_ERR';
		  break;
		case FileError.INVALID_STATE_ERR:
		  msg = 'INVALID_STATE_ERR';
		  break;
		default:
		  msg = 'Unknown Error';
		  break;
	  };

	  console.log('Error: ' + msg);
	}



	
	
});