var zmq = require('zmq')
  , sock = zmq.socket('sub');

var fs = require('fs'); 

sock.connect('tcp://127.0.0.1:28332');
sock.subscribe('rawblock');
console.log('Subscriber connected to port 28332');

sock.on('message', function(topic, message) {
  console.log('received a message related to:', topic, 'containing message:', message);

 fs.writeFile(__dirname + "/bitcoin.txt", message, function(err) {
    if(err) {
        return console.log(err);
    } else {
        console.log("The file was saved!");
    }
	}); 

});
