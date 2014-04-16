var webdriverjs = require('webdriverjs');
var secret = require('../../secret.json');
var options = { desiredCapabilities: { browserName: 'chrome' } };

var client = webdriverjs
.remote(options)
 .init()
 .url('https://accounts.google.com/ServiceLogin?continue=https://plus.google.com/communities/115821855317076020954')
.setValue('#Email','ingressportalalert')
.setValue('#Passwd',secret.gpassword)
.submitForm('#signIn')
.waitFor('.RZd',1000)
.click('.RZd')
.click('.RZd')
.title(function(err, res){
	if(res.value != 'Enlightened Bern - Members - Google+'){
		client.click('.RZd');
	}
})
.waitFor('.dFd',1000,function(){
	test();
	function test(){
		client.waitFor('.r0:not([style])',2000)
		.getAttribute('.r0','style',function(err,value){
			if(value == ''){
				client.click('.dFd');
				test();
			} else {
				client.elements('.X8c',function(err,res){
					console.log(res.value.length);
					for (var i = 0; i<res.value.length; i++){
						client.elementIdAttribute(res.value[i].ELEMENT, 'oid', function(err,result) {
                			console.log(result.value);
            			});
					}
				});
			}
		});
	}
}).end();