var zmq = require('zmq')
  , sock = zmq.socket('sub');

var fs = require('fs'); 

sock.connect('tcp://127.0.0.1:28332');
sock.subscribe('rawblock');
console.log('Subscriber connected to port 28332');

sock.on('message', function(topic, message) {

    console.log(new Date());
    console.log('received a message related to:', topic, 'containing message:', message);

  var m = {
      topic: topic,
      message: message,
      hexMessage: toHexString(message),
      date : new Date()
  };
 /*fs.appendFile(__dirname + "/bitcoin.txt", JSON.stringify(m) + "\n", function(err) {
    if(err) {
        return console.log(err);
    } else {
        console.log("The file was saved!");
    }
	});

    var raw = "\n" + message;

    fs.appendFile(__dirname + "/rawBitcoin.txt", raw, function(err) {
        if(err) {
            return console.log(err);
        } else {
            console.log("The file was saved!");
        }
    });*/

});

function toHexString(byteArray) {
    return Array.from(byteArray, function(byte) {
        return ('0' + (byte & 0xFF).toString(16)).slice(-2);
    }).join('')
}