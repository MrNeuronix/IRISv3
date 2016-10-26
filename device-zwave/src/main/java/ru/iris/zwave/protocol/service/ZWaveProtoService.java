package ru.iris.zwave.protocol.service;

import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;

import java.util.List;

public interface ZWaveProtoService {

	ZWaveDevice getDeviceById(long id);
	List<ZWaveDevice> getZWaveDevices();
	ZWaveDevice saveIntoDatabase(ZWaveDevice device);
	ZWaveDeviceValue addChange(ZWaveDeviceValue value);

}
