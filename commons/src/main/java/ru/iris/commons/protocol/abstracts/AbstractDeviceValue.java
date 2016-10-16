package ru.iris.commons.protocol.abstracts;

import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;

public abstract class AbstractDeviceValue implements DeviceValue {

	protected long id;
	protected Date date;
	protected String name;
	protected Object value;
	protected String units;
	protected boolean readOnly = false;
	protected ValueType type;
	protected String additionalData;

	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public <T> T getValue(Class<T> type) {
		return type.cast(value);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String getUnits() {
		return units;
	}

	@Override
	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public ValueType getType() {
		return type;
	}

	public void setType(ValueType type) {
		this.type = type;
	}

	@Override
	public String getAdditionalData() {
		return additionalData;
	}

	@Override
	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractDeviceValue)) return false;

		AbstractDeviceValue that = (AbstractDeviceValue) o;

		if (id != that.id) return false;
		if (readOnly != that.readOnly) return false;
		if (date != null ? !date.equals(that.date) : that.date != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (value != null ? !value.equals(that.value) : that.value != null) return false;
		if (units != null ? !units.equals(that.units) : that.units != null) return false;
		if (type != that.type) return false;
		return additionalData != null ? additionalData.equals(that.additionalData) : that.additionalData == null;

	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (date != null ? date.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (units != null ? units.hashCode() : 0);
		result = 31 * result + (readOnly ? 1 : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (additionalData != null ? additionalData.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "AbstractDeviceValue{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", value=" + value +
				", units='" + units + '\'' +
				", readOnly=" + readOnly +
				", type=" + type +
				", additionalData='" + additionalData + '\'' +
				'}';
	}
}
