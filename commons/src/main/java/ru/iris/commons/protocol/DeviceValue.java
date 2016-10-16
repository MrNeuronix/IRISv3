package ru.iris.commons.protocol;

import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;

public interface DeviceValue {

	long getId();
	Date getDate();
	String getName();
	<T> T getValue(Class<T> type);
	Object getValue();
	void setValue(Object value);
	void setName(String name);
	void setUnits(String units);
	String getUnits();
	boolean isReadOnly();
	void setReadOnly(boolean readOnly);
	ValueType getType();
	void setType(ValueType type);
	void setAdditionalData(String json);
	String getAdditionalData();
}
