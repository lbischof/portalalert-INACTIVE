package com.lorenzbi.portalalert;

public class Alert {
	private String id, title, message;
	private Integer type, urgency;
	private Float radius;
	private Double lat, lng;
	public Alert(String id, String title, String message, Integer type, Integer urgency, Double lat, Double lng, Float radius) {
		this.id = id;
		this.title = title;
		this.message = message;
		this.type = type;
		this.urgency = urgency;
		this.lat = lat;
		this.lng = lng;
		this.radius = radius;
	}
	public String getId() { return this.id; }
	public String getTitle() { return this.title; }
	public String getMessage() { return this.message; }
	public Integer getType() { return this.type; }
	public Integer getUrgency() { return this.urgency; }
	public Double getLat() { return this.lat; }
	public Double getLng() { return this.lng; }
	public Float getRadius() { return this.radius; }
}
