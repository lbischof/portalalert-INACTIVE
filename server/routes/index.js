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
    var collection = db.get('users');

    // Submit to the DB
    process.stdout.write(regid+"test");
    collection.insert({
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

    // Get our form values. These rely on the "name" attributes
    var lat = req.body.portal;
    var long = req.body.long;
    var name = req.body.name;
    var urgency = req.body.urgency;
    var type = req.body.type;
    var message = req.body.message;

    // Set our collection
    var collection = db.get('alerts');

    // Submit to the DB
    process.stdout.write(lat+":"+long);
    collection.insert({
    	"lat" : lat,
    	"long" : long,
    	"name" : name,
    	"urgency" : urgency,
    	"message" : message
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
				collapseKey: 'demo',
				delayWhileIdle: true,
				timeToLive: 3,
				data: {
					key1: 'message1',
					key2: 'message2'
				}
			});

			var sender = new gcm.Sender('AIzaSyC7FUC_9nkgZoqsSVJg-FY0T9g-oxZPvro');
			var registrationIds = [];

			// At least one required
			registrationIds.push('APA91bGOxvQ4qXv8UuzzDwK50PEJAFuagaUbyArWoNmQi-d4nAOw1-QKccuXNCP97gGjDEK60Szez2huSlhFTa1vAqHswbf_t9D1JL5KDGvJc-HgcqsDRLqXvqzGM6ugBA8Io53Y1PfZ1uZbfzUKw7b-M1fihOraHdaxBOCe7w9DxD-vgshhiMQ');
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