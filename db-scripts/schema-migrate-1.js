var backupCollectionName = "rawData";
backupCollectionName += "_backup_";
backupCollectionName += new Date().valueOf().toString();

db.rawData.copyTo(backupCollectionName);

function updateCollectionItem(item) {


    var isOldSchema = !!item._id.timestamp;

    if (!isOldSchema)
        return;

    var oldId = item._id;
    delete item._id;
    item.timestamp = oldId.timestamp;
    item.device = oldId.device;
    item.type = oldId.type;
    db.rawData.insert(item);
    db.rawData.remove({
        "_id": oldId
    });

}

db.rawData.find().forEach(updateCollectionItem);