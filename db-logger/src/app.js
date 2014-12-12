// Import configuration file.
var config = require('./config.js');

// Other imports.
var mqtt = require('mqtt');
var MongoClient = require('mongodb').MongoClient;

var client = mqtt.createClient(config.MQTT_PORT, config.MQTT_HOST);

// Connect to MongoDB instance.
MongoClient.connect(config.MONGODB_CONNECTION_URL, function (err, db) {

	// Initialize collection for data storage.
	var dataCollection = db.collection(config.DB_COLLECTION_NAME);

	// Handle subscribed MQTT broker messages.
	function handleMessage(topic, message) {
		//console.log('Received on ' + topic);

		try {
			// ASSUMPTION: Device name is always second element
			// in split array (from path).
			var splitTopic = topic.split('/');
			var device = splitTopic[1];
			
			if (topic.indexOf('/debug') >= 0) {
				console.log('Debug: ' + message);
				return;
			}
			if (topic.indexOf('/accelerometer') >= 0) {
				var data = JSON.parse(message);
				var payload = data.payload;
				
				// Generate unique id for
				// received data.
				data._id = {
					'timestamp': new Date(data.timestamp),
					'device': device,
					'type' : splitTopic[2]
				};
				
				// Cleanup data, remove dupes.
				delete data.timestamp;
				
				// Upserting, e.g. replacing data here.
				// The assumption is that sensor values differing only by
				// fractures of milliseconds are very identical
				// to each other.
				dataCollection.update({'_id': data._id}, data, 
					{'upsert':true}, function (err) {
					if (err) {
						console.log('Error: ' + err);
					}
				});
				return;
			}
		} catch (err) {
			console.log('Error:' + err);
		}
	}

	// Topics to subscribe on.
	var topicsToSubscribe = [
		'device/+/accelerometer',
		'device/+/debug'
	];

	// Subscribe on topics.
	topicsToSubscribe.forEach(function(topic){
		client.subscribe(topic);
	});
	
	client.on('message', handleMessage);

});

