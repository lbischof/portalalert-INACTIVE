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
					async.each(res.value,
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
    			console.log(userids);
				});

					
				}
			);
			}
		});
	}
}).end();