var mosca = require('mosca')

var settings = {
    port: 1883,
    url: "mqtt://localhost"
}

var server = new mosca.Server(settings);
 
server.on('clientConnected', function(client) {
    console.log('client connected', client.id);
});
 
server.on('ready', setup);
 
// fired when the mqtt server is ready
function setup() {
  console.log('Mosca server is up and running');
}