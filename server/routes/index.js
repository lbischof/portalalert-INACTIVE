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
            scrape(function(userids){
                if (userids.contains(userid){
                    console.log("contains");
                } else {
                    obj.error = "NOT_FROG";
                }
            });
    	} else {
    		obj.error = err;
    	}
    	res.send(JSON.stringify(obj));
    });
}
}
function scrape(callback){
var webdriverjs = require('webdriverjs');
var secret = require('../../secret.json');
var options = { desiredCapabilities: { browserName: 'chrome' } };
var async = require("async");
var client = webdriverjs
.remote(options)
.init()
.url('https://accounts.google.com/ServiceLogin?continue=https://plus.google.com/communities/115821855317076020954')
.setValue('#Email','ingressportalalert')
.setValue('#Passwd',secret.gpassword)
.submitForm('#signIn')
.waitFor('.RZd',5000)
.click('.RZd')
.click('.RZd')
.title(function(err, res){
    if(res.value != 'Enlightened Bern - Members - Google+'){
        client.click('.RZd');
    }
})
.waitFor('.dFd',5000,function(){
    test();
    function test(){
        client.waitFor('.r0:not([style])',5000)
        .getAttribute('.r0','style',function(err,value){
            console.log(value);
            if(value == ''){
                client.click('.dFd')
                .pause(500);
                test();
            } else {
                var userids = [];
                client.elements('.X8c',function(err,res){
                    console.log(res.value.length);
                    return async.each(res.value,
                    // 2nd parameter is the function that each item is passed into
                    function(item, callback){
                    // Call an asynchronous function (often a save() to MongoDB)
                    client.elementIdAttribute(item.ELEMENT, 'oid', function(err,result) {
                        userids.push(result.value);
                        callback();
                    });
                },
                // 3rd parameter is the function call when everything is done
                function(err){
                // All tasks are done now
                callback(userids);
                });

                    
                }
            );
            }
        });
    }
}).end();
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

    var ttl = portal.ttl;
    var expire = ttl + (new Date).getTime();
    console.log(ttl);
    console.log(expire);
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
    		"type" : type,
    		"expire" : expire
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
exports.done = function(db) {
	return function(req, res) {
		var id = req.body.id;
		var lng = parseFloat(req.body.lng);
		var lat = parseFloat(req.body.lat);
		var userid = req.body.userid;
		var registrationIds = [];
		var alerts = db.get('alerts');
		var users = db.get('users');
		alerts.update({"_id":id},{ $set: { "done" : true }}, function(err, numAffected){
			if(numAffected == 1){
				users.distinct('regid',{location: {$near : { $geometry : { type: "Point", coordinates : [ lng, lat ]}, $maxDistance : 3000}}, userid: {$not : userid}},function(err, docs){
					res.send(id);
					registrationIds = docs;
					if (registrationIds) {
					var gcm = require('node-gcm');
					var gcmMessage = new gcm.Message({
					//collapseKey: 'demo',
					data: {"done":id}
				});
					var sender = new gcm.Sender('AIzaSyC7FUC_9nkgZoqsSVJg-FY0T9g-oxZPvro');

				sender.send(gcmMessage, registrationIds, 4, function (err, result) {
					console.log(result);
				});
			}
			});
			}
		});
	}
}
exports.sync = function(db) {
	return function(req, res) {
		res.setHeader('Access-Control-Allow-Origin', 'http://www.ingress.com');
		var userid = req.body.userid;
		var lng = parseFloat(req.body.lng);
		var lat = parseFloat(req.body.lat);
		var alerts = db.get('alerts');
		var users = db.get('users');
		var now = (new Date).getTime();
		alerts.ensureIndex( { "location" : "2dsphere" } );
		users.update({ "userid" : userid },{ $set: {
			"location" : { "type": "Point", "coordinates" : [ lng,lat ] } }

		}, function (err, numAffected) {
			var obj = new Object();
			alerts.find({location: {$near : { $geometry : { type: "Point", coordinates : [ lng ,lat ]}, $maxDistance : 3000}},expire: {"$gte": now}, done: {$ne: true}}, function(err, docs) {
				obj.error = err;
				obj.alerts = docs;
				res.send(JSON.stringify(obj));
			});
		});
		
	}
}


