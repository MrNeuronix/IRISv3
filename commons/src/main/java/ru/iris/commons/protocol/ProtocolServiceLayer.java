package ru.iris.commons.protocol;

import java.util.List;

/**
 * Created by nix on 26.09.2016.
 */


public interface ProtocolServiceLayer<DEVICE, DEVICEVALUE> {

	DEVICE getDeviceById(long id);
	List<DEVICE> getDevices();
	DEVICE saveIntoDatabase(DEVICE device);
	DEVICEVALUE addChange(DEVICEVALUE value);
}
