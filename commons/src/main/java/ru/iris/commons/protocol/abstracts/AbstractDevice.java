package ru.iris.commons.protocol.abstracts;

import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.Zone;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractDevice<T> implements Device<T> {

	protected long id;
	protected Date date;
	protected Byte node;
	protected String humanReadable;
	protected String manufacturer;
	protected String productName;
	protected SourceProtocol source;
	protected DeviceType type;
	protected Zone zone;
	protected State state;
	protected Map<String, T> values = new HashMap<>();

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Date getCreationDate() {
		return date;
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
	public DeviceType getType() {
		return type;
	}

	@Override
	public Map<String, T> getDeviceValues() {
		return values;
	}

	@Override
	public void setDeviceValues(Map<String, T> values) {
		this.values = values;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public void setType(DeviceType type) {
		this.type = type;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	public Byte getNode() {
		return node;
	}

	public void setNode(Byte node) {
		this.node = node;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractDevice that = (AbstractDevice) o;
		return id == that.id &&
				Objects.equals(date, that.date) &&
				Objects.equals(node, that.node) &&
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
		return Objects.hash(id, date, node, humanReadable, manufacturer, productName, source, type, zone, state, values);
	}

	@Override
	public String toString() {
		return "AbstractDevice{" +
				"id=" + id +
				", date=" + date +
				", node='" + node + '\'' +
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
