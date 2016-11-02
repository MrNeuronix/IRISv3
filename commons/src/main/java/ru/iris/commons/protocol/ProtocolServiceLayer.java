package ru.iris.commons.protocol;

import ru.iris.commons.protocol.enums.ValueType;

import java.util.List;

/**
 * Created by nix on 26.09.2016.
 */


public interface ProtocolServiceLayer<DEVICE, DEVICEVALUE> {

	DEVICE getDeviceById(long id);
	List<DEVICE> getDevices();
	DEVICE saveIntoDatabase(DEVICE device);
	void saveIntoDatabase();
	DEVICEVALUE addChange(DEVICEVALUE value);
	void updateValue(DEVICE device, String label, Object value, ValueType type);
}
