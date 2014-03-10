var app  = require(__dirname + '/../app.js');
var port = 3000;
var http = require('http');

function defaultGetOptions(path) {
  var options = {
    "host": "localhost",
    "port": port,
    "path": path,
    "method": "GET",
    /*"headers": {
      "Cookie": sessionCookie
    }*/
  };
  return options;
}
function defaultPostOptions(path) {
  var options = {
    "host": "localhost",
    "port": port,
    "path": path,
    "method": "POST",
    /*"headers": {
      "Cookie": sessionCookie
    }*/
  };
  return options;
}



describe('app', function () {
 
  before (function (done) {
    app.listen(port, function (err, result) {
      if (err) {
        done(err);
      } else {
        done();
      }
  });
 
 
  it('should exist', function (done) {
    should.exist(app);
    done();
  });
 
  it('should be listening at localhost:3000', function (done) {
    var headers = defaultGetOptions('/');
    http.get(headers, function (res) {
      res.statusCode.should.eql(404);
      done();
    });
  });
 
});
  it('should authenticate a user', function (done) {
  var qstring = JSON.stringify({
    "name": "aa",
    "regid": "aae",
    "username": "aaa",
    "email": "aea"
  });
  var options = defaultPostOptions('/register', qstring);
  var req = http.request(options, function (res) {
    res.on('data', function (d) {
      var body = JSON.parse(d.toString('utf8'));
      body.should.have.property('regid');//.and.match(/logged in/);
      //accountId = body.account.id;
      done();
    });
  });
  req.write(qstring);
  req.end();
});
});
