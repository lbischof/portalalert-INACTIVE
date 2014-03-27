package com.lorenzbi.portalalert;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Alerts {
	private String error = null;
	private List<Alert> alerts;

	public String getError() {
		return error;
	}

	public List<Alert> getAlerts() {
		return alerts;
	}

	public static class Alert {
		@SerializedName("_id") 
		private String id;
		private String title, message, userid;
		private Integer type, urgency, time;
		private Float radius;
		private AlertLocation location;

		public Alert(String id, String title, String message, Integer type,
				Integer urgency, AlertLocation location, Float radius,
				String userid, Integer time) {
			this.id = id;
			this.title = title;
			this.message = message;
			this.type = type;
			this.urgency = urgency;
			this.location = location;
			this.radius = radius;
			this.userid = userid;
			this.time = time;
		}

		public String getId() {
			return this.id;
		}

		public String getTitle() {
			return this.title;
		}

		public String getMessage() {
			return this.message;
		}

		public Integer getType() {
			return this.type;
		}

		public Integer getUrgency() {
			return this.urgency;
		}

		public AlertLocation getLocation() {
			return this.location;
		}

		public Float getRadius() {
			if (this.radius == null)
				return (float) 200;
			return this.radius;
		}

		public String getUserid() {
			return this.userid;
		}

		public Integer getTime() {
			return this.time;
		}

	}

	public static class AlertLocation {
		private String type;
		private List<Double> coordinates = new ArrayList<Double>();

		public String getType() {
			return this.type;
		}

		public Double getLng() {
			return this.coordinates.get(0);
		}

		public void setLng(Double lng) {
			if (indexExists(this.coordinates, 0)) {
			    this.coordinates.set(0, lng);
			} else {
			    this.coordinates.add(0, lng);
			}
		}

		public Double getLat() {
			return this.coordinates.get(1);
		}

		public void setLat(Double lat) {
			if (indexExists(this.coordinates, 1)) {
			    this.coordinates.set(1, lat);
			} else {
			    this.coordinates.add(1, lat);
			}
		}
		public boolean indexExists(List<Double> list, int index) {
		    return index >= 0 && index < list.size();
		}
	}
	

}
