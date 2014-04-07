/* GET home page. */
exports.index = function(req, res){
	res.render('index', { title: 'Express' });
};
exports.register = function(db) {
	return function(req, res) {

    // Get our form values. These rely on the "name" attributes
    var userid = req.body.userid;
    var username = req.body.username;
    var email = req.body.email;
    var regid = req.body.regid;
    var name = req.body.name;
    var lat = parseFloat(req.body.lat);
    var lng = parseFloat(req.body.lng);

    // Set our collection
    var users = db.get('users');
    var alerts = db.get('alerts');
    alerts.ensureIndex( { "location" : "2dsphere" } );
    users.ensureIndex( { "location" : "2dsphere" } );
    // Submit to the DB
    process.stdout.write(regid+"test");
    users.ensureIndex( { userid: 1 }, { unique: true } );
    users.update({ "userid" : userid },{
    	"userid" : userid,
    	"username" : username,
    	"email" : email,
    	"name" : name,
    	"regid" : regid
    }, function (err, numAffected) {
    	var obj = new Object();
    	if (numAffected == 0) {
    		obj.error = "NOT_FROG";
    	} else {
    		obj.error = err;
    	}
    	res.send(JSON.stringify(obj));
    });
}
}

exports.alert = function(db) {
	return function(req, res) {
		var portal = JSON.parse(req.body.portal);
		var registrationIds = [];
    // Get our form values. These rely on the "name" attributes
    var lat = parseFloat(portal.lat);
    var lng = parseFloat(portal.lng);
    var imagesrc = portal.imagesrc;
    var title = portal.title;
    //var urgency = req.body.urgency;
    var type = portal.type;
    var message = portal.message;
    // Set our collection
    var alerts = db.get('alerts');
    var users = db.get('users');

    
    alerts.ensureIndex( { "location" : "2dsphere" } );
    users.ensureIndex( { "location" : "2dsphere" } );
    users.distinct('regid',{location: {$near : { $geometry : { type: "Point", coordinates : [ lng, lat ]}, $maxDistance : 3000}}},function(err, docs){
    	registrationIds = docs;
    	alerts.insert({
    		"location" : { "type": "Point", "coordinates" : [ lng,lat ] },
    		"imagesrc" : imagesrc,
    		"title" : title,
    		//"urgency" : urgency,
    		"message" : message,
    		"type" : type
    	}, function (err, doc) {
    		if (err) {
            // If it failed, return error
            res.send("There was a problem adding the information to the database.");
        } else {
        	console.log(doc);
        	res.send(doc);
        	var gcm = require('node-gcm');
        	var gcmMessage = new gcm.Message({
					//collapseKey: 'demo',
					data: doc
				});
        	var sender = new gcm.Sender('AIzaSyC7FUC_9nkgZoqsSVJg-FY0T9g-oxZPvro');
				/**
				* Params: message-literal, registrationIds-array, No. of retries, callback-function
				**/
				sender.send(gcmMessage, registrationIds, 4, function (err, result) {
					console.log(result);
				});
			}
		});
    });  
}
}

exports.sync = function(db) {
	return function(req, res) {
		var userid = req.body.userid;
		var lng = parseFloat(req.body.lng);
		var lat = parseFloat(req.body.lat);
		var alerts = db.get('alerts');
		var users = db.get('users');
		alerts.ensureIndex( { "location" : "2dsphere" } );
		users.update({ "userid" : userid },{
			"location" : { "type": "Point", "coordinates" : [ lng, lat ] }
		}, function (err, numAffected) {
			console.log(err + numAffected);
			var obj = new Object();
			alerts.find({location: {$near : { $geometry : { type: "Point", coordinates : [ lng ,lat ]}, $maxDistance : 3000}}}, function(err, docs) {
				obj.error = err;
				obj.alerts = docs;
				res.send(JSON.stringify(obj));
			});
		});
		
	}
}

