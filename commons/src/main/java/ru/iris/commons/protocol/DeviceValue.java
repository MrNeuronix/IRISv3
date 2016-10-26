package ru.iris.commons.protocol;

import ru.iris.commons.protocol.enums.ValueType;

import java.util.Date;
import java.util.List;

public interface DeviceValue<CHANGE> {

	long getId();
	Date getDate();
	String getName();
	<T> T getCurrentValue(Class<T> type);
	Object getCurrentValue();
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
	void setChanges(List<CHANGE> changes);
	List<CHANGE> getChanges();
}
