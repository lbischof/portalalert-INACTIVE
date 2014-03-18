/* GET home page. */
exports.index = function(req, res){
	res.render('index', { title: 'Express' });
};
exports.register = function(db) {
	return function(req, res) {

    // Get our form values. These rely on the "name" attributes
    var username = req.body.username;
    var email = req.body.email;
    var regid = req.body.regid;
    var name = req.body.name;

    // Set our collection
    var users = db.get('users');

    // Submit to the DB
    process.stdout.write(regid+"test");
    users.insert({
    	"username" : username,
    	"email" : email,
    	"name" : name,
    	"regid" : regid
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
    users.find({}, 'regid -_id', function(err, docs){
for (var i = 0; i < docs.length; i++) {
    registrationIds.push(docs[i].regid);
}
console.log(registrationIds);
});
    alerts.insert({
    	"lat" : lat,
    	"lng" : lng,
    	"title" : title,
    	//"urgency" : urgency,
    	"message" : message,
    	"type" : type
    }, function (err, doc) {
    	if (err) {
            // If it failed, return error
            res.send("There was a problem adding the information to the database.");
        }
        else {
        	
        	var gcm = require('node-gcm');
        	// create a message with default values
        	var message = new gcm.Message();

			// or with object values
			var message = new gcm.Message({
				//collapseKey: 'demo',
				data: doc
			});

			var sender = new gcm.Sender('AIzaSyC7FUC_9nkgZoqsSVJg-FY0T9g-oxZPvro');

			// At least one required
			registrationIds.push('APA91bHU1j3-6WL_MRgtxkNMAUewQEJHFZyLbQWmqxfKgGkOFfwPGY3kkCdRsnjKoylhlI3iFk4e6CHU_qxfK2CHsaYmt8RtXG4jVdJVwAZEO5Qi9BFlrVivZkLecTdYpcK0ijxhY98JiC01RU9Xjx8Rcxh92sX86bN-5dPTZMXGnIc48kAdzxo');
			//registrationIds.push('regId2'); 

			/**
			* Params: message-literal, registrationIds-array, No. of retries, callback-function
			**/
			sender.send(message, registrationIds, 4, function (err, result) {
				console.log(result);
			});
			            // If it worked, set the header so the address bar doesn't still say /adduser
			            //res.location("userlist");
			            // And forward to success page
			            //res.redirect("userlist");
		}
	});
}
}