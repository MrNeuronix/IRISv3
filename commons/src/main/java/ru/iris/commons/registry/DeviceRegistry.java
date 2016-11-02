package ru.iris.commons.registry;

import org.springframework.stereotype.Component;
import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.enums.SourceProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceRegistry {

	private Map<String, Object> registry = new ConcurrentHashMap<>();

	public void addOrUpdateDevice(Device device) {
		Object tmp = registry.replace(device.getHumanReadableName(), device);
		if(tmp == null)
			registry.put(device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel(), device);
	}

	public void addOrUpdateDevices(List<? extends Device> devices) {
		devices.forEach(device ->  {
			Object tmp = registry.replace(device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel(), device);
			if(tmp == null)
				registry.put(device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel(), device);
		});
	}

	public List<Object> getDevicesByProto(SourceProtocol proto) {
		List<Object> ret = new ArrayList<>();
		registry.values().forEach(device ->  {
			Device tmp = (Device) device;
			if(tmp != null && tmp.getSourceProtocol().equals(proto))
				ret.add(device);
		});

		return ret;
	}

	public Object getDevice(SourceProtocol protocol, Short channel) {
		return registry.get(protocol.name().toLowerCase()+"/channel/"+channel);
	}

}
