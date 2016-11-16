package ru.iris.facade.model;

import org.hibernate.validator.constraints.NotEmpty;

import ru.iris.commons.protocol.enums.SourceProtocol;

/**
 * Created by nix on 15.11.2016.
 */
public class HistoryRequest {

	@NotEmpty
	private String source;
	@NotEmpty
	private Short channel;
	private String startDate;
	private String endDate;
	@NotEmpty
	private String label;

	public String getSource() {
		return source;
	}

	public void setSource(@NotEmpty String source) {
		this.source = source;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(@NotEmpty Short channel) {
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

	public void setLabel(@NotEmpty String label) {
		this.label = label;
	}
}
