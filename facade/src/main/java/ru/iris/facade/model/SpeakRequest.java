package ru.iris.facade.model;

/**
 * Created by nix on 15.11.2016.
 */
public class SpeakRequest {

	private String text;
	private String zone;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
}
