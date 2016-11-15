package ru.iris.facade.model;

import ru.iris.commons.protocol.enums.SourceProtocol;

/**
 * Created by nix on 15.11.2016.
 */
public class HistoryRequest {

	private SourceProtocol source;
	private Short channel;
	private String startDate;
	private String endDate;
	private String label;

	public SourceProtocol getSource() {
		return source;
	}

	public void setSource(SourceProtocol source) {
		this.source = source;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(Short channel) {
		this.channel = channel;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
