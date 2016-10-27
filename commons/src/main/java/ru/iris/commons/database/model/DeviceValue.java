package ru.iris.commons.database.model;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import ru.iris.commons.protocol.enums.ValueType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "device_values")
public class DeviceValue {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@ManyToOne
	private Device device;

	private String name;
	private String units;
	private Boolean readOnly;

	@Enumerated(EnumType.STRING)
	private ValueType type = ValueType.STRING;

	private String additionalData;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "deviceValue")
	@BatchSize(size=200)
	private List<DeviceValueChange> changes = new ArrayList<>();

	public DeviceValue() {
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

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ValueType getType() {
		return type;
	}

	public void setType(ValueType type) {
		this.type = type;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public String getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	public List<DeviceValueChange> getChanges() {
		return changes;
	}

	public void setChanges(List<DeviceValueChange> changes) {
		this.changes = changes;
	}
}