# MongoDB IoT Example

## Context

The context of the solution is to provide an example of how to use IoT applications with MongoDB as a database.

Basically this is my extension of a talk given by John Page on MongoDB Days Munich 2014. It's lacking the paper, but it has more to do with the 'things' in IoT :)

In this example, data is transmitted using MQTT as broker solution.

Once published on the broker, sensor data from a mobile application gets received by a node.js application by subscription and stored in the database.

## Conventions

Requirements level are to be treated according to RFC2119 ("Key words for use in RFCs to Indicate Requirement Levels").

Timestamps must be specified as Unix Timestamp from epoch start (01/01/1970) in milliseconds.

## Component Descriptions

### Custom-developed solutions

#### Android application (android-app)

The Android application gathers data from the mobile device's accelerometer and publishes it onto a MQTT broker.

#### Persistent DB logger (db-logger)

Component responsible for persistent storage of received data from MQTT broker.

### COTS Solutions

#### MongoDB

MongoDB is to be used as datastore due to its scalability properties.

#### Mosquitto

Mosquitto is a standalone MQTT broker solution running in a hosted environment.


## Interface Descriptions

Data is exchanged by using MQTT (Message Queue Telemetry Transport) as a bus / PubSub provider.

Data is published to one or more topics.
Supported topics *must* follow the convention "device/{deviceName}/{subTopic}".

"{deviceName}" corresponds to a dynamic device name of a mobile. Here the (weak) assumption is made that the model name of the mobile device is unique enough for testing purposes.

"{subTopic}" may be one of the following:

"debug": Subtopic for debug output. Subscribed payload on persistent DB logger shall be print on screen.

"accelerometer": Subtopic for transmission of device accelerometer sensor data. Subscribed payload on persistent DB logger must be stored in database. The payload is defined as a JSON object "{ 'x': 0, 'y' : 0, 'z': 0, 'timestamp' : 0 } where "x"-"z" correspond to device acceleration in the corresponding orientation axis, "timestamp" is defined as time the sensor event was recorded.

## Data Architecture

Data is stored inside a single mongod instance or distributed among a cluster using sharding (the shard key is as of yet undefined, but will probably require an hashed index).

The received data from devices gets immediately stored in a collection 'rawData'. 

The document schema for accelerometer data contains the values for "x"-"z" as defined above, for every document a composite id consisting of the sensor data timestamp and the unique device name.
