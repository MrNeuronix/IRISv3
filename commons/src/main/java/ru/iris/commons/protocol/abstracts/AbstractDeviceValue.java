package ru.iris.commons.protocol.abstracts;

import ru.iris.commons.protocol.DeviceValue;

import java.util.Date;
import java.util.Objects;

public abstract class AbstractDeviceValue implements DeviceValue {

	protected long id;
	protected Date date;
	protected String name;
	protected Object value;

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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractDeviceValue that = (AbstractDeviceValue) o;
		return id == that.id &&
				Objects.equals(date, that.date) &&
				Objects.equals(name, that.name) &&
				Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, date, name, value);
	}

	@Override
	public String toString() {
		return "DeviceValueImpl{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", value=" + value +
				'}';
	}
}
