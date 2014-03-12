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
		//window.plugin.portalalert.portal = d;
		window.plugin.portalalert.portal.push({title: d.descriptiveText.map.TITLE, address: d.descriptiveText.map.ADDRESS, lng: d.locationE6.lngE6, lat: d.locationE6.latE6});
		$('#portaldetails').append('<div class="portalalert"> <a onclick="window.plugin.portalalert.submit_portal()" title="submit portal">Portalalert Submit</a></div>');
	}
	window.plugin.portalalert.submit_portal = function(){
		var p = [];
		var d = window.plugin.portalalert.portal;
		$.ajax({url: 'http://portalalert.lorenzz.ch:3000/alert',type: 'POST', data:{'d': JSON.stringify(d)},dataType: 'jsop',success: function(r){return;}});
		alert(JSON.stringify(d.portalV2));
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
