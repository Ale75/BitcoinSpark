// pub.js
var zmq = require('zmq')
  , sock = zmq.socket('pub');

var fs = require('fs');
var sendNumber= 0;

sock.bindSync('tcp://127.0.0.1:28332');
console.log('Publisher bound to port 28332');

setInterval(function(){

	//var path = __dirname + "/home/antonio/.bitcoin/testnet3/blocks";
	var path = __dirname + "/bitcoin_txt.txt";

	/*fs.readdir( path ,function(err,files){

  		console.info("File trovati :" + files.length);
  		for(i = 0; i < files.length; i++){

  			var file = path + "/" + files[i];
  			console.info("Processando il file: " + file);

            fs.readFileSync(file).toString().split("\n").forEach(function(line, index, arr) {
                if (index === arr.length - 1 && line === "") { return; }

                console.log("Inviato il: " + index);
                console.log(line);
                sock.send(['rawblock', [[],line] ]);
            });


		}

	});*/

    fs.readFileSync(path).toString().split("\n").forEach(function(line, index, arr) {
        if (index === arr.length - 1 && line === "") { return; }

        var json = JSON.parse(line);
        console.log("Inviato il blocco: " + index++);
        //console.log("Blocco : " + json.message.data);
        //console.log("Blocco hex: " + json.hexMessage);

        sock.send([ 'rawblock',  new Buffer(json.message.data)    ] );
    });


}, 5000);





