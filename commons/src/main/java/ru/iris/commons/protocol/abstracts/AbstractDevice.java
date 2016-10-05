package ru.iris.commons.protocol.abstracts;

import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.Zone;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.Type;
import ru.iris.commons.protocol.enums.State;

import java.util.*;

public abstract class AbstractDevice implements Device {

	private long id;
	private Date date;
	private String internalName;
	private String humanReadable;
	private String manufacturer;
	private String productName;
	private SourceProtocol source;
	private Type type;
	private Zone zone;
	private State state;
	private Set<DeviceValue> values = new HashSet<>();

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Date getCreationDate() {
		return date;
	}

	@Override
	public String getInternalName() {
		return internalName;
	}

	@Override
	public String getHumanReadableName() {
		return humanReadable;
	}

	@Override
	public String getManufacturer() {
		return manufacturer;
	}

	@Override
	public String getProductName() {
		return productName;
	}

	@Override
	public SourceProtocol getSourceProtocol() {
		return source;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Zone getZone() {
		return zone;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Set<DeviceValue> getDeviceValues() {
		return values;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setInternalName(String internalName) {
		this.internalName = internalName;
	}

	public void setHumanReadable(String humanReadable) {
		this.humanReadable = humanReadable;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setSource(SourceProtocol source) {
		this.source = source;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void setValues(Set<DeviceValue> values) {
		this.values = values;
	}
}
