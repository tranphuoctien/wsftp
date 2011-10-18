$(document).ready(function(){
	window.requestFileSystem  = window.requestFileSystem || window.webkitRequestFileSystem;
	var ws;
	var pieces = 0;
	var path = "";
	var fileName = "";
	var bb = []; 
	var offset = 0;
	var startTime = 0;
	var endTime = 0;
	initDragout();
	$("#dragout").hide();
	
	function st(){startTime = new Date().getTime();}
	function et(){endTime = new Date().getTime();}
	function diff(){return endTime - startTime;}
	
	function showMessage(msg){
		$("#console").append("<br/>" + msg);
		var objDiv = document.getElementById("console");
		objDiv.scrollTop = objDiv.scrollHeight;	
	}
	
	$("#connectBtn").click( function(){
		connect( $("#SRV_IP").val(), $("#SRV_USER").val(), $("#SRV_PWD").val());		
	});
	
	$(".file").live("click", function(){
		st();
		path = $(this).attr("data-path");
		var f = path.split("\\");
		fileName = f[f.length-1];
		removeFile(fileName);
		showMessage("Requesting: " + path);
		ws.send("FILEINFO;" + path);
	});
	
	function fragmentDisk(){
		showMessage("Now fragmenting disk.");
		offset = 0;
		recurse();						
		function recurse(){
			fileRead("_" + fileName + "_" + offset, function(data){
				console.log("reading: " + offset);
				if ( offset < pieces - 1){
					appendFile(fileName, data, recurse); 
				} else {
					deleteTemps();
				}
			});
		}
		
		function deleteTemps(){
			for ( var i = 0; i < pieces; i++){
				removeFile("_" + fileName + "_" + i);	
			}	
		}		
	}
	
	function connect(server, user, pass){
		ws = new WebSocket('ws://' + server +'/wsftp');
		showMessage('Connecting to ' + server);	  
		ws.onopen = function() { 
			showMessage('Connection established @' + server); 
			setTimeout( function(){ws.send("LOGIN;" + user +";" + pass);}, 20);
		};
		ws.onclose = function() { showMessage('Lost connection to ' + server ); };
		ws.onmessage = function(msg) { 
			if ( typeof (msg.data) == typeof("a")){
				var message = msg.data.split(";");
				var command = message[0];
				if ( command == "FILELIST"){
					showMessage("File List recieved: " + message.length + " files.");					
					populateFiles(message);
				} else if ( command == "FILEINFO"){
					path = message[1];
					pieces = message[2];
					$("#bar").attr("max", parseInt(pieces)-1);
					$("#bar").val("0"); 
					bb[0] = new window.WebKitBlobBuilder();
					ws.send("FILEGET;0;" + path);
				} else if ( command == "FILESENT"){
					createFile(fileName, offset);					
					offset = parseInt(message[1]) + 1;
					$("#bar").val(offset);					
					if ( offset < pieces){
						bb[offset] = new window.WebKitBlobBuilder();						
						ws.send("FILEGET;" + offset + ";" + path);						
					} else {
						et();
						fragmentDisk();
					}					
				}
			} else { // Binary
				bb[offset].append(msg.data);
			}				
		};	
	}	
	
	function populateFiles(files){
		var gui = "";
		for ( var i = 1; i < files.length; i++){
			var currFile = files[i].split("\\");
			gui += "<a href='#' class='file button white small' data-path='" + files[i] + "' title='Click to download file.'>" + currFile[currFile.length - 1] + "</a>";
		}
		$("#file_structure").html(gui);
	}
	
	function initDragout(){
		var file = document.getElementById("dragout");
		file.addEventListener("dragstart",function(evt){			
		var fileDetails;	 
		if(typeof file.dataset === "undefined") {
			fileDetails = file.getAttribute("data-downloadurl");
		} else {
			fileDetails = file.dataset.downloadurl;
		}
			evt.dataTransfer.setData("DownloadURL", fileDetails);
			console.log(evt.dataTransfer);
		},false);
	}
	
	// FILE OPERATIONS //
	function createFile(fileName, piece){
		window.requestFileSystem(window.PERSISTENT, 5 * 1024 *1024 *1024, function(fs) {
		  fs.root.getFile("_" + fileName + "_" + piece, {create: true}, function(fileEntry) {
			fileEntry.createWriter(function(fileWriter) {
			  fileWriter.onwriteend = function(e) {
				showMessage('Write completed: ' + piece + " / " + (pieces - 1));
				if ( piece == piece -1){
					fragmentDisk();
				}
			  };
			  fileWriter.onerror = function(e) {
				console.log(e);
			  };
			  fileWriter.write(bb[piece].getBlob());
			}, errorHandler);
		  }, errorHandler);
	  }, errorHandler)	  		
	}
	
	function removeFile(fileName){
		window.requestFileSystem(window.PERSISTENT, 5 * 1024 *1024 *1024, function(fs) {
		  fs.root.getFile(fileName, {create: false}, function(fileEntry) {		
			fileEntry.remove(function() {
			  console.log('File removed.');
			}, errorHandler);		
		  }, errorHandler);
		}, errorHandler);
	}
	
	function appendFile(fileName, data, callback){
		window.requestFileSystem(window.PERSISTENT, 5* 1024 * 1024 * 1024, function(fs){
			fs.root.getFile(fileName, {create: true, exclusive: false}, function(fileEntry) {
			fileEntry.createWriter(function(fileWriter) {
			  fileWriter.seek(fileWriter.length); // Go to EOF
			  var tmpBB = new window.WebKitBlobBuilder();
			  tmpBB.append(data);
			  fileWriter.write(tmpBB.getBlob());
			  fileWriter.onwriteend = function(e){
				  offset++;
				  callback();
			  };			  
			}, errorHandler);
		  }, errorHandler);			
		}, errorHandler);				
	}
	
	function fileRead(fileName, callback){
		window.requestFileSystem(window.PERSISTENT, 5* 1024 * 1024 * 1024, function(fs){
		  fs.root.getFile(fileName, {}, function(fileEntry) {
			fileEntry.file(function(file) {
			   var reader = new FileReader();
			   reader.onloadend = function(e) {
					callback(this.result);
			   };
			   reader.readAsArrayBuffer(file);
			}, errorHandler);
		  }, errorHandler);
		}, errorHandler);
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