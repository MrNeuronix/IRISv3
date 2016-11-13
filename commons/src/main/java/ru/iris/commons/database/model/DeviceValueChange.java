package ru.iris.commons.database.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "device_values_change")
public class DeviceValueChange {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@ManyToOne
	private DeviceValue deviceValue;

	private String value;
	private String additionalData;

	public DeviceValueChange() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	public DeviceValue getDeviceValue() {
		return deviceValue;
	}

	public void setDeviceValue(DeviceValue deviceValue) {
		this.deviceValue = deviceValue;
	}
}