package ru.iris.zwave.protocol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.Zone;
import ru.iris.commons.protocol.DeviceValueImpl;
import ru.iris.commons.protocol.ZoneImpl;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.zwave.protocol.ZWaveDevice;

import java.util.HashSet;
import java.util.Set;

@Service("zwaveservice")
public class ZWaveDeviceService {

	@Autowired
	private DeviceDAO deviceDAO;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ZWaveDevice getDeviceById(long id)
	{
		Device dbDevice = deviceDAO.findOne(id);

		if(dbDevice == null)
			return null;

		return merge(dbDevice);
	}

	public Set<ZWaveDevice> getZWaveDevices()
	{
		Set<ZWaveDevice> ret = new HashSet<>();

		Set<Device> devices = (Set<Device>) deviceDAO.findAll();

		for(Device device : devices)
		{
			ret.add(merge(device));
		}

		return ret;
	}

	public void saveIntoDatabase(ZWaveDevice device)
	{
		deviceDAO.save(mergeForDB(device));
	}

	private ZWaveDevice merge(Device device) {

		if (!device.getSource().equals(SourceProtocol.ZWAVE)) {
			logger.error("Specified device is not ZWave device!");
			return null;
		}

		ZWaveDevice ret = new ZWaveDevice();

		ret.setId(device.getId());
		ret.setDate(device.getDate());
		ret.setHumanReadable(device.getHumanReadable());
		ret.setInternalName(device.getInternalName());
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setSource(SourceProtocol.ZWAVE);
		ret.setState(State.UNKNOWN);

		ZoneImpl zone = new ZoneImpl();
		zone.setId(device.getZone().getId());
		zone.setDate(device.getZone().getDate());
		zone.setName(device.getZone().getName());

		ret.setZone(zone);

		Set<ru.iris.commons.protocol.DeviceValue> values = new HashSet<>();

		for(ru.iris.commons.database.model.DeviceValue deviceValue : device.getValues())
		{
			DeviceValueImpl dv = new DeviceValueImpl();

			dv.setId(deviceValue.getId());
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());
			dv.setValue(deviceValue.getValue());

			values.add(dv);
		}

		ret.setValues(values);

		return ret;
	}

	private Device mergeForDB(ZWaveDevice device) {

		Device ret = deviceDAO.findOne(device.getId());
		boolean creating = false;

		if(ret == null)
		{
			logger.debug("ZWave device with id {} not found in DB. Creating.", device.getId());
			creating = true;
			ret = new Device();
		}

		if(!creating) {
			ret.setId(device.getId());
			ret.setDate(device.getCreationDate());
		}

		ret.setHumanReadable(device.getHumanReadableName());
		ret.setInternalName(device.getInternalName());
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setSource(SourceProtocol.ZWAVE);

		if(creating && device.getZone() != null)
		{
			Zone zone = new Zone();

			zone.setId(device.getZone().getId());
			zone.setDate(device.getZone().getDate());
			zone.setName(device.getZone().getName());

			ret.setZone(zone);
		}

		Set<ru.iris.commons.database.model.DeviceValue> values = new HashSet<>();

		for(ru.iris.commons.protocol.DeviceValue deviceValue : device.getDeviceValues())
		{
			ru.iris.commons.database.model.DeviceValue dv = new ru.iris.commons.database.model.DeviceValue();

			dv.setId(deviceValue.getId());
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());
			dv.setValue(deviceValue.getValue().toString());

			values.add(dv);
		}

		ret.setValues(values);

		return ret;
	}

}
