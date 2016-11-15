package ru.iris.noolite.protocol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.DeviceValue;
import ru.iris.commons.database.model.DeviceValueChange;
import ru.iris.commons.database.model.Zone;
import ru.iris.commons.protocol.ProtocolServiceLayer;
import ru.iris.commons.protocol.ZoneImpl;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.noolite.protocol.model.NooliteDevice;
import ru.iris.noolite.protocol.model.NooliteDeviceValue;
import ru.iris.noolite.protocol.model.NooliteDeviceValueChange;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service("nooliteDeviceService")
public class NooliteDeviceService implements ProtocolServiceLayer<NooliteDevice, NooliteDeviceValue> {

	private final DeviceDAO deviceDAO;
	private final DeviceRegistry registry;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public NooliteDeviceService(DeviceDAO deviceDAO, DeviceRegistry registry) {
		this.deviceDAO = deviceDAO;
		this.registry = registry;

		// load all noolite devices to registry
		getDevices();

		// schedule periodic save into database
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new SaveIntoDatabaseRunner(), 60, 60, TimeUnit.SECONDS);
	}

	@Override
	@Transactional(readOnly = true)
	public NooliteDevice getDeviceById(long id)
	{
		Device dbDevice = deviceDAO.findOne(id);

		if(dbDevice == null)
			return null;

		return merge(dbDevice, null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<NooliteDevice> getDevices()
	{
		List<NooliteDevice> ret = new ArrayList<>();

		List<Device> devices = deviceDAO.findBySource(SourceProtocol.NOOLITE);

		for(Device device : devices)
		{
			ret.add(merge(device, null));
		}

		// add all devices to registry
		registry.addOrUpdateDevices(ret);

		return ret;
	}

	@Override
	@Transactional
	public NooliteDevice saveIntoDatabase(NooliteDevice device)
	{
		device = merge(deviceDAO.save(mergeForDB(device)), device);
		registry.addOrUpdateDevice(device);

		return device;
	}

	@Override
	@Transactional
	public void saveIntoDatabase()
	{
		List<Object> devices = registry.getDevicesByProto(SourceProtocol.NOOLITE);
		devices.forEach(device -> saveIntoDatabase((NooliteDevice)device));
	}

	@Transactional
	private NooliteDevice merge(Device device, NooliteDevice nooDevice) {

		if (!device.getSource().equals(SourceProtocol.NOOLITE)) {
			logger.error("Specified device is not Noolite device!");
			return null;
		}

		NooliteDevice ret = new NooliteDevice();

		ret.setId(device.getId());
		ret.setDate(device.getDate());
		ret.setHumanReadable(device.getHumanReadable());
		ret.setChannel(device.getChannel());
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setSource(SourceProtocol.NOOLITE);
		ret.setState(State.UNKNOWN);

		if(device.getZone() != null) {

			ZoneImpl zone = new ZoneImpl();

			zone.setId(device.getZone().getId());
			zone.setDate(device.getZone().getDate());
			zone.setName(device.getZone().getName());

			ret.setZone(zone);
		}

		Map<String, NooliteDeviceValue> values = new HashMap<>();

		for(DeviceValue deviceValue : device.getValues().values())
		{
			NooliteDeviceValue dv = new NooliteDeviceValue();

			dv.setId(deviceValue.getId());
			dv.setDate(deviceValue.getDate());
			dv.setName(deviceValue.getName());
			dv.setUnits(deviceValue.getUnits());
			dv.setReadOnly(deviceValue.getReadOnly());
			dv.setType(deviceValue.getType());

			// fill values
			if(nooDevice != null) {
				NooliteDeviceValue nooValue = nooDevice.getDeviceValues().get(dv.getName());

				if(nooValue != null) {
					dv.setCurrentValue(nooValue.getCurrentValue());
					dv.setAdditionalData(nooValue.getAdditionalData());
				}
				else {
					logger.error("Cannot found device value for " + dv.getName());
				}
			}

			// fill changes
			Iterator<DeviceValueChange> changeIterator = deviceValue.getChanges().iterator();
			for(byte count = 1; count <= 5; count++)
			{
				if(!changeIterator.hasNext())
					break;

				DeviceValueChange change = changeIterator.next();
				NooliteDeviceValueChange noochange = new NooliteDeviceValueChange();

				noochange.setDate(change.getDate());
				noochange.setId(change.getId());
				noochange.setValue(change.getValue());
				noochange.setAdditionalData(change.getAdditionalData());

				dv.getChanges().addLast(noochange);
			}

			values.put(dv.getName(), dv);
		}

		ret.setDeviceValues(values);

		return ret;
	}

	@Transactional
	private Device mergeForDB(NooliteDevice device) {

		Device ret = deviceDAO.findOne(device.getId());
		boolean creating = false;

		if(ret == null)
		{
			logger.debug("Noolite device with id {} not found in DB. Creating.", device.getId());
			creating = true;
			ret = new Device();
		}

		if(!creating) {
			ret.setId(device.getId());
			ret.setDate(device.getCreationDate());
		}

		ret.setHumanReadable(device.getHumanReadableName());
		ret.setChannel(device.getChannel());
		ret.setManufacturer(device.getManufacturer());
		ret.setProductName(device.getProductName());
		ret.setType(device.getType());
		ret.setSource(SourceProtocol.NOOLITE);

		if(creating && device.getZone() != null)
		{
			Zone zone = new Zone();

			zone.setId(device.getZone().getId());
			zone.setDate(device.getZone().getDate());
			zone.setName(device.getZone().getName());

			ret.setZone(zone);
		}

		Map<String, ru.iris.commons.database.model.DeviceValue> values = new HashMap<>();

		for(NooliteDeviceValue deviceValue : device.getDeviceValues().values())
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

			deviceValue.getChanges().forEach(change -> {

				DeviceValueChange changeDB = new DeviceValueChange();

				if (change.getValue() == null)
					logger.debug("Skipping null Noolite value change");
				else {
					changeDB.setId(change.getId());
					changeDB.setDeviceValue(dv);
					changeDB.setDate(change.getDate());
					changeDB.setValue(change.getValue().toString());
					changeDB.setAdditionalData(change.getAdditionalData());

					dv.getChanges().add(changeDB);
				}
			});

			values.put(dv.getName(), dv);
		}

		ret.setValues(values);

		return ret;
	}

	@Override
	public NooliteDeviceValue addChange(NooliteDeviceValue value) {

		NooliteDeviceValueChange add = new NooliteDeviceValueChange();
		add.setAdditionalData(value.getAdditionalData());
		add.setValue(value.getCurrentValue());
		add.setDate(new Date());
		value.setLastUpdated(new Date());

		value.getChanges().addFirst(add);

		return value;
	}

	@Override
	public void updateValue(NooliteDevice device, String label, Object value, ValueType type) {
		NooliteDeviceValue deviceValue = device.getDeviceValues().get(label);

		if (deviceValue == null) {
			deviceValue = new NooliteDeviceValue();
			deviceValue.setName(label);
			deviceValue.setCurrentValue(value);
			deviceValue.setType(type);
			deviceValue.setReadOnly(false);

			device.getDeviceValues().put(label, deviceValue);
		}
		else {
			deviceValue.setCurrentValue(value);
			device.getDeviceValues().replace(label, device.getDeviceValues().get(label), deviceValue);
		}

		addChange(deviceValue);
	}

	private class SaveIntoDatabaseRunner implements Runnable {
		@Override
		public void run() {
			logger.debug("Running save noolite devices into database thread");
			saveIntoDatabase();
			logger.debug("Done saving thread");
		}
	}
}
