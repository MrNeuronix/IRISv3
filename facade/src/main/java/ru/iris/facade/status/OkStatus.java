package ru.iris.facade.status;

public class OkStatus {

	private String text;

	public OkStatus() {
	}

	public OkStatus(String text) {
		this.text = text;
	}

	public String getStatus() {
		return "OK";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
