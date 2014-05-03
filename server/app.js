var express = require('express');
var http = require('http');
var path = require('path');
var favicon = require('static-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var routes = require('./routes');
var users = require('./routes/user');
var mongo = require('mongodb');
var monk = require('monk');
var db = monk('localhost:27017/portalalert');

var gcm = require('node-gcm');


var app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

app.use(favicon());
app.use(logger('dev'));
app.use(express.urlencoded());
app.use(express.json());

app.use(cookieParser());
app.use(express.session({secret: '1234567890QWERTY'}));

app.use(express.static(path.join(__dirname, 'public')));
app.use(app.router);

app.get('/', routes.index);
app.get('/users', users.list);

app.post('/register', routes.register(db));
app.post('/alert', routes.alert(db));
app.post('/sync', routes.sync(db));
app.post('/done', routes.done(db));
app.post('/upload', routes.upload(db));
app.post('/search', routes.search(db));
app.post('/bounds', routes.bounds(db));
app.post('/everything', routes.everything(db));
app.post('getAlertById', routes.getAlertById(db));
/// catch 404 and forwarding to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

/// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function(err, req, res, next) {
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
    res.render('error', {
        message: err.message,
        error: err
    });
});


module.exports = app;
