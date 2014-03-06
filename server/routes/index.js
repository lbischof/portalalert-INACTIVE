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
        var lat = req.body.lat;
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
                // If it worked, set the header so the address bar doesn't still say /adduser
                //res.location("userlist");
                // And forward to success page
                //res.redirect("userlist");
            }
        });
    }
}