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
			if (topic.indexOf('/accelerometer') >= 0 || topic.indexOf('/temperature') >= 0 || topic.indexOf('/location') >= 0) {
			
				var sensorFromTopic = splitTopic[2];
			
				var data = JSON.parse(message);
				var payload = data.payload;
				
				var rawTimestamp = data.timestamp;
				
				// Set fields on data.
				data.timestamp = new Date(rawTimestamp);
				data.device = device;
			    data.type = sensorFromTopic;
				
				if (sensorFromTopic == "location"){
					payload.loc = {
						'type' : 'Point',
						'coordinates' : [
							payload.longitude,
							payload.latitude
						]
					};
					delete payload.latitude;
					delete payload.longitude;
				}
								
				// Upserting, e.g. replacing data here.
				// The assumption is that sensor values differing only by
				// fractures of milliseconds are very identical
				// to each other.
				dataCollection.insert(data, function (err, inserted) {
					if (err) {
						console.log('Error: ' + err);
						return ;
					}
					var persistedMsg = {
						"_id" : inserted[0]._id,
						"timestamp" : rawTimestamp
					};
					client.publish('persisted/' + topic, JSON.stringify(persistedMsg));
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
		'device/+/location',
		'device/+/temperature',
		'device/+/debug'
	];

	// Subscribe on topics.
	topicsToSubscribe.forEach(function(topic){
		client.subscribe(topic);
	});
	
	client.on('message', handleMessage);

});

