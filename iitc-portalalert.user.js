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
        var dialogtext = "<select id=alert-type><option value=1>Upgrade</option><option value=2>Destroy</option></select><br><label>Message</label><textarea id=alert-message></textarea>";
        dialog({
    	text: dialogtext,
        title: 'Portal Alert: '+ window.plugin.portalalert.portal.title,
    	id: 'portalalert',
    	width: 350,
    	buttons: {
      		'Submit Alert': function() {
                window.plugin.portalalert.portal.type = parseInt($('#alert-type').val());
                window.plugin.portalalert.portal.message = $('#alert-message').val();
        		window.plugin.portalalert.submit_portal();
                $(".ui-dialog").hide("slow");
      		}
    }
  });
    }
	var setup = function(){
		window.addHook('portalDetailsUpdated', window.plugin.portalalert.setup_link);
        $('head').append('<style>' +
                         '#dialog-portalalert label { display: block; }' +
                         '#dialog-portalalert textarea { background: rgba(0, 0, 0, 0.3); color: #ffce00; height: 120px; width: 100%; padding: 4px 4px 0px 4px; font-size: 12px; border: 0; box-sizing: border-box; }' +
                         '#dialog-portalalert select { background: rgba(0, 0, 0, 0.3); color: #ffce00; border: 0; padding: 5px;}' +
                         '#dialog-portalalert option { background: rgba(8, 48, 78, 0.9); }' +
                         '.ui-dialog button {padding: 5px; }'+
    					 '</style>');
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