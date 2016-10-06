package ru.iris.commons.protocol.abstracts;

import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.Zone;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.Type;
import ru.iris.commons.protocol.enums.State;

import java.util.*;

public abstract class AbstractDevice implements Device {

	protected long id;
	protected Date date;
	protected String internalName;
	protected String humanReadable;
	protected String manufacturer;
	protected String productName;
	protected SourceProtocol source;
	protected Type type;
	protected Zone zone;
	protected State state;
	protected Map<String, DeviceValue> values = new HashMap<>();

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

	public Map<String, DeviceValue> getDeviceValues() {
		return values;
	}

	public void setDeviceValues(Map<String, DeviceValue> values) {
		this.values = values;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractDevice that = (AbstractDevice) o;
		return id == that.id &&
				Objects.equals(date, that.date) &&
				Objects.equals(internalName, that.internalName) &&
				Objects.equals(humanReadable, that.humanReadable) &&
				Objects.equals(manufacturer, that.manufacturer) &&
				Objects.equals(productName, that.productName) &&
				source == that.source &&
				type == that.type &&
				Objects.equals(zone, that.zone) &&
				state == that.state &&
				Objects.equals(values, that.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, date, internalName, humanReadable, manufacturer, productName, source, type, zone, state, values);
	}

	@Override
	public String toString() {
		return "AbstractDevice{" +
				"id=" + id +
				", date=" + date +
				", internalName='" + internalName + '\'' +
				", humanReadable='" + humanReadable + '\'' +
				", manufacturer='" + manufacturer + '\'' +
				", productName='" + productName + '\'' +
				", source=" + source +
				", type=" + type +
				", zone=" + zone +
				", state=" + state +
				", values=" + values +
				'}';
	}
}
