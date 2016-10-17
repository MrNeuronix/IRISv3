package ru.iris.noolite.protocol.service;

import ru.iris.noolite.protocol.model.NooliteDevice;
import java.util.List;

public interface NooliteProtoService {

	NooliteDevice getDeviceById(long id);
	List<NooliteDevice> getNooliteDevices();
	NooliteDevice saveIntoDatabase(NooliteDevice device);

}
