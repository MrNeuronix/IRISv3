package ru.iris.commons.protocol;

import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;

import java.util.Date;
import java.util.Map;

public interface Device {

	long getId();
	Date getCreationDate();
	Short getNode();
	String getHumanReadableName();
	String getManufacturer();
	String getProductName();
	SourceProtocol getSourceProtocol();
	State getState();
	Zone getZone();
	DeviceType getType();
	Map<String, ? extends DeviceValue> getDeviceValues();
	void setDeviceValues(Map<String, ? extends DeviceValue> values);

}
