package ru.iris.commons.protocol;

import java.util.Date;

public class DeviceValueImpl implements DeviceValue {

	private long id;
	private Date date;
	private String name;
	private Object value;

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
}
