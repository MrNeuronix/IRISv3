package ru.iris.commons.database.model;

import org.hibernate.annotations.CreationTimestamp;
import ru.iris.commons.protocol.enums.Type;
import ru.iris.commons.protocol.enums.SourceProtocol;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "v2_devices")
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	private String internalName;
	private String humanReadable;
	private String manufacturer;
	private String productName;

	@Enumerated(EnumType.ORDINAL)
	private SourceProtocol source;

	@Enumerated(EnumType.ORDINAL)
	private Type type;

	@ManyToOne
	private Zone zone;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "device")
	@OrderBy("name ASC")
	private Set<DeviceValue> values = new HashSet<>();

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

	public String getInternalName() {
		return internalName;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public String getHumanReadable() {
		return humanReadable;
	}

	public void setHumanReadable(String humanReadable) {
		this.humanReadable = humanReadable;
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public Set<DeviceValue> getValues() {
		return values;
	}

	public void setValues(Set<DeviceValue> values) {
		this.values = values;
	}
}