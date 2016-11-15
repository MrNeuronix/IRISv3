package ru.iris.facade.model;

/**
 * Created by nix on 15.11.2016.
 */
public class DeviceInfoRequest {

	private String source;
	private Short channel;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(Short channel) {
		this.channel = channel;
	}
}
