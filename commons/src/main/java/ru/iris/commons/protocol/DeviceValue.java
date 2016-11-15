package ru.iris.commons.protocol;

import ru.iris.commons.LIFO;
import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
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
	void setChanges(LIFO<? extends DeviceValueChange> changes);
	LIFO<? extends DeviceValueChange> getChanges();
}
