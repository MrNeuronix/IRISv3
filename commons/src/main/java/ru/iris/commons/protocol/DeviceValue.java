package ru.iris.commons.protocol;

import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface DeviceValue {

	long getId();
	Date getDate();
	String getName();
	<T> T getCurrentValue(Class<T> type);
	Object getCurrentValue();
	Date getLastUpdated();
	void setCurrentValue(Object value);
	void setName(String name);
	void setUnits(String units);
	String getUnits();
	boolean isReadOnly();
	void setReadOnly(boolean readOnly);
	ValueType getType();
	void setType(ValueType type);
	void setAdditionalData(String json);
	String getAdditionalData();
	void setChanges(ConcurrentLinkedQueue<DeviceValueChange> changes);
	ConcurrentLinkedQueue<DeviceValueChange> getChanges();
}
