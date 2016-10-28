package ru.iris.zwave.protocol.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zwave4j.ValueId;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.DeviceValueChange;
import ru.iris.commons.database.model.Zone;
import ru.iris.commons.protocol.ProtocolServiceLayer;
import ru.iris.commons.protocol.ZoneImpl;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;
import ru.iris.zwave.protocol.model.ZWaveDeviceValueChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("zwaveDeviceService")
public class ZWaveDeviceService implements ProtocolServiceLayer<ZWaveDevice, ZWaveDeviceValue> {

	private final DeviceDAO deviceDAO;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Gson gson = new GsonBuilder().create();

	@Autowired
	public ZWaveDeviceService(DeviceDAO deviceDAO) {
		this.deviceDAO = deviceDAO;
	}

	@Override
	@Transactional(readOnly = true)
	public ZWaveDevice getDeviceById(long id)
	{
		Device dbDevice = deviceDAO.findOne(id);

		if(dbDevice == null)
			return null;

		return merge(dbDevice, null);
	}

	@Override
	@Transactional
	public List<ZWaveDevice> getDevices()
	{
		List<ZWaveDevice> ret = new ArrayList<>();

		List<Device> devices = deviceDAO.findBySource(SourceProtocol.ZWAVE);

		for(Device device : devices)
		{
			ret.add(merge(device, null));
		}

		return ret;
	}

	@Override
	@Transactional
	public ZWaveDevice saveIntoDatabase(ZWaveDevice device)
	{
		return merge(deviceDAO.save(mergeForDB(device)), device);
	}

	@Transactional
	private ZWaveDevice merge(Device device, ZWaveDevice zwdevice) {

		if (!device.getSource().equals(SourceProtocol.ZWAVE)) {
			logger.error("Specified device is not ZWave device!");
			return null;
		}

		ZWaveDevice ret = new ZWaveDevice();

		ret.setId(device.getId());
		ret.setDate(device.getDate());
		ret.setHumanReadable(device.getHumanReadable());
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setSource(SourceProtocol.ZWAVE);
		ret.setState(State.UNKNOWN);
		ret.setChannel(device.getChannel());

		if(device.getZone() != null) {

			ZoneImpl zone = new ZoneImpl();

			zone.setId(device.getZone().getId());
			zone.setDate(device.getZone().getDate());
			zone.setName(device.getZone().getName());

			ret.setZone(zone);
		}

		Map<String, ZWaveDeviceValue> values = new HashMap<>();

		for(ru.iris.commons.database.model.DeviceValue deviceValue : device.getValues().values())
		{
			ZWaveDeviceValue dv = new ZWaveDeviceValue();

			dv.setId(deviceValue.getId());
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());
			dv.setUnits(deviceValue.getUnits());
			dv.setReadOnly(deviceValue.getReadOnly());
			dv.setType(deviceValue.getType());

			// fill values
			if(zwdevice != null) {

				ret.setHomeId(zwdevice.getHomeId());

				ZWaveDeviceValue zValue = zwdevice.getDeviceValues().get(dv.getName());

				if(zValue != null) {
					dv.setCurrentValue(zValue.getCurrentValue());
					dv.setAdditionalData(zValue.getAdditionalData());
					dv.setValueId(zValue.getValueId());
				}
				else {
					logger.error("Cannot found device value for " + dv.getName());
				}
			}

			values.put(dv.getName(), dv);
		}

		ret.setDeviceValues(values);

		return ret;
	}

	@Transactional
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
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setChannel(device.getChannel());
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

		for(ZWaveDeviceValue deviceValue : device.getDeviceValues().values())
		{
			ru.iris.commons.database.model.DeviceValue dv = new ru.iris.commons.database.model.DeviceValue();

			if(deviceValue.getId() != 0L)
				dv.setId(deviceValue.getId());

			dv.setDevice(ret);
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());
			dv.setUnits(deviceValue.getUnits());
			dv.setReadOnly(deviceValue.isReadOnly());
			dv.setType(deviceValue.getType());

			deviceValue.getChanges().stream().filter(change -> change.getId() == 0L).forEach(change -> {

				DeviceValueChange changeDB = new DeviceValueChange();

				changeDB.setDeviceValue(dv);

				if(deviceValue.getCurrentValue() != null)
					changeDB.setValue(deviceValue.getCurrentValue().toString());
				else
					logger.debug("Skipping null ZWave value change");

				changeDB.setAdditionalData(gson.toJson(deviceValue.getValueId(), ValueId.class));

				dv.getChanges().add(changeDB);
			});

			values.put(dv.getName(), dv);
		}

		ret.setValues(values);

		return ret;
	}

	@Override
	public ZWaveDeviceValue addChange(ZWaveDeviceValue value) {

		ZWaveDeviceValueChange add = new ZWaveDeviceValueChange();
		add.setValueId(value.getValueId());
		add.setValue(value.getCurrentValue());
		value.getChanges().add(add);

		return value;
	}
}
