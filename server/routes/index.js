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
    var lat = req.body.lat;
    var lng = req.body.lng;

    // Set our collection
    var users = db.get('users');

    // Submit to the DB
    process.stdout.write(regid+"test");
    users.insert({
    	"userid" : userid,
    	"username" : username,
    	"email" : email,
    	"name" : name,
    	"regid" : regid,
    	"loc" : [ lng, lat ]
    }, function (err, doc) {
    	if (err) {
            // If it failed, return error
            res.send("There was a problem adding the information to the database.");
        }
        else {
            // If it worked, set the header so the address bar doesn't still say /adduser
            //res.location("userlist");
            // And forward to success page
            //res.redirect("userlist");
        }
    });
}
}
exports.alert = function(db) {
	return function(req, res) {
	var portal = JSON.parse(req.body.portal);
	var registrationIds = [];
    // Get our form values. These rely on the "name" attributes
    var lat = portal.lat;
    var lng = portal.lng;
    var title = portal.title;
    //var urgency = req.body.urgency;
    var type = portal.type;
    var message = portal.message;
    // Set our collection
    var alerts = db.get('alerts');
    var users = db.get('users');

    // Submit to the DB
    users.ensureIndex( { regid: 1 }, { unique: true } );
    users.find({}, 'regid -_id', function(err, docs){
		for (var i = 0; i < docs.length; i++) {
    	registrationIds.push(docs[i].regid);
		}
 		alerts.insert({
    		"location" : [ lng,lat ],
    		"title" : title,
    		//"urgency" : urgency,
    		"message" : message,
    		"type" : type
    	}, function (err, doc) {
    		if (err) {
            // If it failed, return error
            	res.send("There was a problem adding the information to the database.");
        	} else {
        		console.log(message);
        		var gcm = require('node-gcm');
				var gcmMessage = new gcm.Message({
					//collapseKey: 'demo',
					data: {
						"_id" : doc._id,
    					"lat" : lat,
    					"lng" : lng,
    					"title" : title,
    					//"urgency" : urgency,
    					"message" : message,
    					"type" : type
    				}
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
exports.userlocation = function(db) {
	return function(req, res) {

    // Get our form values. These rely on the "name" attributes
    var userid = req.body.userid;
    var lat = req.body.lat;
    var lng = req.body.lng;

    // Set our collection
    var users = db.get('users');

    // Submit to the DB
    process.stdout.write(regid+"test");
    users.insert({
    	"userid" : userid,
    	"location" : [lng,lat]
    }, function (err, doc) {
    	if (err) {
            // If it failed, return error
            res.send("There was a problem adding the information to the database.");
        }
        else {
            // If it worked, set the header so the address bar doesn't still say /adduser
            //res.location("userlist");
            // And forward to success page
            //res.redirect("userlist");
        }
    });
}
}