package ru.iris.commons.protocol;

import ru.iris.commons.protocol.enums.Type;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;

import java.util.Date;
import java.util.Map;

public interface Device {

	long getId();
	Date getCreationDate();
	String getInternalName();
	String getHumanReadableName();
	String getManufacturer();
	String getProductName();
	SourceProtocol getSourceProtocol();
	State getState();
	Zone getZone();
	Type getType();
	Map<String, DeviceValue> getDeviceValues();

}
