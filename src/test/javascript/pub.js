// pub.js
var zmq = require('zmq')
  , sock = zmq.socket('pub');

var fs = require('fs');
var sendNumber= 0;

sock.bindSync('tcp://127.0.0.1:28332');
console.log('Publisher bound to port 28332');

setInterval(function(){

  	console.log('Nuovo giro di invii');
 	sendNumber= 0;

	fs.readFileSync(__dirname + "/bitcoin.txt").toString().split("\n").forEach(function(line, index, arr) {
	  if (index === arr.length - 1 && line === "") { return; }
	  console.log("Inviato il: " + index);
	  console.log(line);
	  sock.send(['rawblock', new ArrayBuffer(line) ]);
	});
	
  
}, 50000);





