package ru.iris.commons.database.model;

import org.hibernate.annotations.CreationTimestamp;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "devices")
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	private Short channel;

	private String humanReadable;
	private String manufacturer;
	private String productName;

	@Enumerated(EnumType.STRING)
	private SourceProtocol source;

	@Enumerated(EnumType.STRING)
	private DeviceType type;

	@ManyToOne
	private Zone zone;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "device")
	@OrderBy("name ASC")
	@MapKey(name="name")
	private Map<String, DeviceValue> values = new HashMap<>();

	// State will be get in runtime runtime


	public Device() {
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

	public String getHumanReadable() {
		return humanReadable;
	}

	public void setHumanReadable(String humanReadable) {
		this.humanReadable = humanReadable;
	}

	public Short getChannel() {
		return channel;
	}

	public void setChannel(Short channel) {
		this.channel = channel;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public SourceProtocol getSource() {
		return source;
	}

	public void setSource(SourceProtocol source) {
		this.source = source;
	}

	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public Map<String, DeviceValue> getValues() {
		return values;
	}

	public void setValues(Map<String, DeviceValue> values) {
		this.values = values;
	}
}