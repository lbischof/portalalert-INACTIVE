/* GET home page. */
exports.index = function(req, res){
	res.render('index', { title: 'Express' });
};
var scraping = false;
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
            if (!scraping){ 
                scrape('115821855317076020954', function(userids){
                    for (var i = userids.length - 1; i >= 0; i--) {
                        users.insert(userids[i], function(err, doc){
                            if (err == null){
                                console.log('scraping: inserted '+doc.userid);
                            }
                        });
                    };
                });
            }
            obj.error = "NOT_FROG";
            res.send(JSON.stringify(obj));
    	} else {
    		obj.error = err;
            res.send(JSON.stringify(obj));
    	}
    });
}
}
function scrape(community, callback){
scraping = true;
console.log('scraping: begin');
var webdriverjs = require('webdriverjs');
var secret = require('../../../secret.json');
var options = { desiredCapabilities: { browserName: 'chrome' } };
var async = require("async");
var client = webdriverjs
.remote(options)
.init()
.url('https://accounts.google.com/ServiceLogin?continue=https://plus.google.com/communities/'+community)
.setValue('#Email','ingressportalalert')
.setValue('#Passwd',secret.gpassword)
.submitForm('#signIn')
.waitFor('.RZd',5000)
.click('.RZd')
.click('.RZd')
.title(function(err, res){
    if(res.value.indexOf("Members") === -1){
        client.click('.RZd');
    }
})
.waitFor('.dFd',5000,function(){
    test();
    function test(){
        client.waitFor('.r0:not([style])',5000)
        .getAttribute('.r0','style',function(err,value){
            if(value == ''){
                client.click('.dFd')
                .pause(500);
                test();
            } else {
                var userids = [];
                client.elements('.X8c',function(err,res){
                    async.each(res.value,
                    function(item, callback){
                    client.elementIdAttribute(item.ELEMENT, 'oid', function(err,result) {
                        userids.push({'userid':result.value});
                        callback();
                    });
                },
                function(err){
                console.log('scraping: done');
                scraping = false;
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
    res.setHeader('Access-Control-Allow-Origin', 'http://www.ingress.com');
	var portal = JSON.parse(req.body.portal);
	var registrationIds = [];
    // Get our form values. These rely on the "name" attributes
    var guid = portal.guid;
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
    var portals = db.get('portals');
    var users = db.get('users');
    portals.ensureIndex( { "location" : "2dsphere" } );
    users.ensureIndex( { "location" : "2dsphere" } );
    users.distinct('regid',{location: {$near : { $geometry : { type: "Point", coordinates : [ lng, lat ]}, $maxDistance : 3000}}},function(err, docs){
    	registrationIds = docs;
        console.log(guid);
    	portals.update({ "_id" : guid},
        {
            $setOnInsert: {
                "location" : { "type": "Point", "coordinates" : [ lng,lat ] },
                "imagesrc" : imagesrc,
                "title" : title,
            },
            $set : {
                "alert" : {
    		      //"urgency" : urgency,
    		      "message" : message,
    		      "type" : type,
    		      "expire" : expire
                }
            }
        },
        { upsert : true }, function (err, doc) {
            console.log(err+doc+"test");
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
exports.upload = function(db) {
    return function(req, res) {
        res.setHeader('Access-Control-Allow-Origin', 'http://www.ingress.com');
        var portalsuploaded = req.body.portals;
        var portals = db.get('portals');
        console.log(portals);
        portals.ensureIndex( { "location" : "2dsphere" } );

        for (var i = portalsuploaded.length - 1; i >= 0; i--) {
            portalsuploaded[i].location.coordinates[0] = parseFloat(portalsuploaded[i].location.coordinates[0]);
            portalsuploaded[i].location.coordinates[1] = parseFloat(portalsuploaded[i].location.coordinates[1]);
            portals.insert(portalsuploaded[i],function(err,doc){
                console.log("err: "+err+" doc: "+doc);
            });
        };
        res.send("success");
    }
}
exports.search = function(db) {
    return function(req, res) {
        var lng = parseFloat(req.body.lng);
        var lat = parseFloat(req.body.lat);
        var title = req.body.title;
        var portals = db.get('portals');
        portals.find({location: {$near : { $geometry : { type: "Point", coordinates : [ lng ,lat ]}, $maxDistance : 3000}},title: { $regex: '^'+title, $options: 'i' } }, function(err, docs) {
            console.log(docs);
                var obj = new Object();
                obj.error = err;
                obj.portals = docs;
                res.send(JSON.stringify(obj));
        });
    }
}

