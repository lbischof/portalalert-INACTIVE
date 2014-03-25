package com.lorenzbi.portalalert;

import java.util.List;

public class Alert {
	private String id, title, message, userid;
	private Integer type, urgency, time;
	private Float radius;
	private Double lat, lng;
	public Alert(String id, String title, String message, Integer type, Integer urgency, Double lat, Double lng, Float radius, String userid, Integer time) {
		this.id = id;
		this.title = title;
		this.message = message;
		this.type = type;
		this.urgency = urgency;
		this.lat = lat;
		this.lng = lng;
		this.radius = radius;
		this.userid = userid;
		this.time = time;
	}
	public String getId() { return this.id; }
	public String getTitle() { return this.title; }
	public String getMessage() { return this.message; }
	public Integer getType() { return this.type; }
	public Integer getUrgency() { return this.urgency; }
	public Double getLat() { return this.lat; }
	public Double getLng() { return this.lng; }
	public Float getRadius() { return this.radius; }
	public String getUserid() { return this.userid; }
	public Integer getTime() { return this.time; }
	
	
}