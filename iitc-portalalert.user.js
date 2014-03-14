// ==UserScript==
// @id             ingress-ph-portal-submission
// ==UserScript==
// @id             ingress-portalalert
// @name           PortalAlert
// @version        0.1
// @description    PortalAlert tool for Ingress
// @include        http://www.ingress.com/intel*
// @include        https://www.ingress.com/intel*
// @match          http://www.ingress.com/intel*
// @match          https://www.ingress.com/intel*
// ==/UserScript==

function wrapper() {
	if(typeof window.plugin !== 'function') window.plugin = function() {};
	window.plugin.portalalert = function() {};
	window.plugin.portalalert.portal = [];
	window.plugin.portalalert.setup_link = function(data){
		var d = data.portalDetails;
		var lat = d.locationE6.latE6 / 1e6
        var lng = d.locationE6.lngE6 / 1e6
		window.plugin.portalalert.portal = {title: d.descriptiveText.map.TITLE, address: d.descriptiveText.map.ADDRESS, lng: lng, lat: lat};
		$('#portaldetails').append('<div class="portalalert"> <a onclick="window.plugin.portalalert.open_dialog()" title="submit portal">Portalalert Submit</a></div>');
	}
	window.plugin.portalalert.submit_portal = function(){
        		$.ajax({url: 'http://portalalert.lorenzz.ch:3000/alert',type: 'POST', data:{'portal': JSON.stringify(window.plugin.portalalert.portal)},dataType: 'jsop',success: function(r){return;}});
	}
	
    
    window.plugin.portalalert.open_dialog = function() {
        var dialogtext = "<input type=text></input>";
        dialog({
    	text: dialogtext + JSON.stringify(window.plugin.portalalert.portal),
   		title: 'Portal Alert',
    	id: 'portalalert',
    	width: 350,
    	buttons: {
      		'Submit Portal': function() {
        		window.plugin.portalalert.submit_portal();
      		}
    }
  });
    }
	var setup = function(){
		window.addHook('portalDetailsUpdated', window.plugin.portalalert.setup_link);
	}

	if(window.iitcLoaded && typeof setup === 'function') {setup();} 
	else {
		if(window.bootPlugins) {window.bootPlugins.push(setup);} 
		else {window.bootPlugins = [setup];}
	}
}

var script = document.createElement('script');
script.appendChild(document.createTextNode('('+ wrapper +')();'));
(document.body || document.head || document.documentElement).appendChild(script);