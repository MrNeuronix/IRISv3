package ru.iris.zwave.protocol.service;

import ru.iris.zwave.protocol.ZWaveDevice;

import java.util.Set;

public interface ZWaveProtoService {

	ZWaveDevice getDeviceById(long id);
	Set<ZWaveDevice> getZWaveDevices();
	ZWaveDevice saveIntoDatabase(ZWaveDevice device);

}
