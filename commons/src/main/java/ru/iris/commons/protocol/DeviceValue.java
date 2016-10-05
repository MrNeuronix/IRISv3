package ru.iris.commons.protocol;

import java.util.Date;

public interface DeviceValue {

	long getId();
	Date getDate();
	String getName();
	<T> T getValue(Class<T> type);
	Object getValue();

}
