package ru.iris.zwave.protocol.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.database.model.DeviceValueChange;
import ru.iris.commons.database.model.Zone;
import ru.iris.commons.protocol.ProtocolServiceLayer;
import ru.iris.commons.protocol.ZoneImpl;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;
import ru.iris.zwave.protocol.model.ZWaveDeviceValueChange;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

@Service("zwaveDeviceService")
public class ZWaveDeviceService implements ProtocolServiceLayer<ZWaveDevice, ZWaveDeviceValue> {

	private final DeviceDAO deviceDAO;
	private final DeviceRegistry registry;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Gson gson = new GsonBuilder().create();

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	private EntityManager em;

	@Autowired
	public ZWaveDeviceService(DeviceDAO deviceDAO, DeviceRegistry registry) {
		this.deviceDAO = deviceDAO;
		this.registry = registry;
	}

	@PostConstruct
	@Transactional
	public void init() {
		// load all zwave devices to registry
		getDevices();

		// schedule periodic save into database
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new SaveIntoDatabaseRunner(), 60, 60, TimeUnit.SECONDS);
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

		// add all devices to registry
		registry.addOrUpdateDevices(ret);

		return ret;
	}

	@Override
	@Transactional
	public ZWaveDevice saveIntoDatabase(ZWaveDevice device)
	{
		device = merge(deviceDAO.save(mergeForDB(device)), device);
		registry.addOrUpdateDevice(device);

		return device;
	}

	@Override
	@Transactional
	public void saveIntoDatabase() {
		List<Object> devices = registry.getDevicesByProto(SourceProtocol.ZWAVE);
		devices.forEach(device -> saveIntoDatabase((ZWaveDevice)device));
	}

	@Transactional
	private ZWaveDevice merge(Device device, ZWaveDevice zwdevice) {

		device = em.merge(device);

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
			deviceValue = em.merge(deviceValue);

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

			// fill changes
			Iterator<DeviceValueChange> changeIterator = deviceValue.getChanges().iterator();
			for(byte count = 1; count <= 5; count++)
			{
				if(!changeIterator.hasNext())
					break;

				DeviceValueChange change = changeIterator.next();
				ZWaveDeviceValueChange zchange = new ZWaveDeviceValueChange();

				zchange.setDate(change.getDate());
				zchange.setId(change.getId());
				zchange.setValue(change.getValue());
				zchange.setAdditionalData(change.getAdditionalData());

				dv.getChanges().addLast(zchange);
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

			deviceValue.getChanges().forEach(change -> {

				DeviceValueChange changeDB = new DeviceValueChange();

				if(change.getValue() == null)
					logger.debug("Skipping null ZWave value change");
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
	public ZWaveDeviceValue addChange(ZWaveDeviceValue value) {

		ZWaveDeviceValueChange add = new ZWaveDeviceValueChange();
		add.setValueId(value.getValueId());
		add.setValue(value.getCurrentValue());
		add.setAdditionalData(gson.toJson(value.getValueId()));
		add.setDate(new Date());
		value.setLastUpdated(new Date());

		value.getChanges().addFirst(add);

		return value;
	}

	@Override
	public void updateValue(ZWaveDevice device, String label, Object value, ValueType type) {
	}

	private class SaveIntoDatabaseRunner implements Runnable {
		@Override
		public void run() {
			logger.debug("Running save zwave devices into database thread");
			saveIntoDatabase();
			logger.debug("Done saving thread");
		}
	}
}
