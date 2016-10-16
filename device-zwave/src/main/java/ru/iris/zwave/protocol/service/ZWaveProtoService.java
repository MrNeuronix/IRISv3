package ru.iris.zwave.protocol.service;

import ru.iris.zwave.protocol.model.ZWaveDevice;

import java.util.List;

public interface ZWaveProtoService {

	ZWaveDevice getDeviceById(long id);
	List<ZWaveDevice> getZWaveDevices();
	ZWaveDevice saveIntoDatabase(ZWaveDevice device);

}
