package ru.iris.zwave.protocol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.Zone;
import ru.iris.commons.protocol.ZoneImpl;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ZWaveDeviceService implements ZWaveProtoService {

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

	public List<ZWaveDevice> getZWaveDevices()
	{
		List<ZWaveDevice> ret = new ArrayList<>();

		List<Device> devices = deviceDAO.findBySource(SourceProtocol.ZWAVE);

		for(Device device : devices)
		{
			ret.add(merge(device));
		}

		return ret;
	}

	public ZWaveDevice saveIntoDatabase(ZWaveDevice device)
	{
		return merge(deviceDAO.save(mergeForDB(device)));
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
		ret.setNode(device.getNode());
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setSource(SourceProtocol.ZWAVE);
		ret.setState(State.UNKNOWN);

		if(device.getZone() != null) {

			ZoneImpl zone = new ZoneImpl();

			zone.setId(device.getZone().getId());
			zone.setDate(device.getZone().getDate());
			zone.setName(device.getZone().getName());

			ret.setZone(zone);
		}

		Map<String, ru.iris.commons.protocol.DeviceValue> values = new HashMap<>();

		for(ru.iris.commons.database.model.DeviceValue deviceValue : device.getValues().values())
		{
			ZWaveDeviceValue dv = new ZWaveDeviceValue();

			dv.setId(deviceValue.getId());
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());
			dv.setValue(deviceValue.getValue());
			dv.setUnits(deviceValue.getUnits());
			dv.setReadOnly(deviceValue.getReadOnly());
			dv.setType(deviceValue.getType());
			dv.setAdditionalData(deviceValue.getAdditionalData());

			values.put(dv.getName(), dv);
		}

		ret.setDeviceValues(values);

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
		ret.setNode(device.getNode());
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

		Map<String, ru.iris.commons.database.model.DeviceValue> values = new HashMap<>();

		for(ru.iris.commons.protocol.DeviceValue deviceValue : device.getDeviceValues().values())
		{
			ru.iris.commons.database.model.DeviceValue dv = new ru.iris.commons.database.model.DeviceValue();

			if(deviceValue.getId() != 0L)
				dv.setId(deviceValue.getId());

			dv.setDevice(ret);
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());

			if(deviceValue.getValue() != null)
				dv.setValue(deviceValue.getValue().toString());

			dv.setUnits(deviceValue.getUnits());
			dv.setReadOnly(deviceValue.isReadOnly());
			dv.setType(deviceValue.getType());
			dv.setAdditionalData(deviceValue.getAdditionalData());

			values.put(dv.getName(), dv);
		}

		ret.setValues(values);

		return ret;
	}

}
