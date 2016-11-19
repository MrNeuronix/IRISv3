package ru.iris.commons.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.enums.SourceProtocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class DeviceRegistry {

	private Map<String, Object> registry = new ConcurrentHashMap<>();
	private Map<String, String> humanReadable = new ConcurrentHashMap<>();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	public void addOrUpdateDevice(Device device) {

		if(device == null) {
			logger.error("Device, passed into registry is null!");
			return;
		}

		String ident = device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel();

		if(!humanReadable.containsValue(ident))
			if(device.getHumanReadableName() != null && !device.getHumanReadableName().isEmpty())
				humanReadable.put(device.getHumanReadableName(), ident);

		Object tmp = registry.replace(ident, device);
		if(tmp == null)
			registry.put(ident, device);
	}

	public void addOrUpdateDevices(List<? extends Device> devices) {
		devices.forEach(device ->  {

			if(device == null) {
				logger.error("Device, passed into registry is null!");
				return;
			}

			String ident = device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel();

			if(!humanReadable.containsValue(ident))
				if(device.getHumanReadableName() != null && !device.getHumanReadableName().isEmpty())
					humanReadable.put(device.getHumanReadableName(), ident);

			Object tmp = registry.replace(ident, device);
			if(tmp == null)
				registry.put(ident, device);
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

	public Object getDevice(String humanReadableIdent) {

		String ident = humanReadable.get(humanReadableIdent);

		if(ident != null)
			return registry.get(ident);
		else
			return null;
	}

	public Object getDeviceValue(SourceProtocol protocol, Short channel, String value) {
		Device device = (Device) registry.get(protocol.name().toLowerCase()+"/channel/"+channel);

		if(device == null)
			return null;

		if(device.getDeviceValues().containsKey(value))
			return device.getDeviceValues().get(value);
		else
			return null;
	}

	public Object getDeviceValue(String humanReadableIdent, String value) {

		String ident = humanReadable.get(humanReadableIdent);

		if(ident != null)
		{
			Device device = (Device) registry.get(ident);

			if(device.getDeviceValues().containsKey(value))
				return device.getDeviceValues().get(value);
			else
				return null;
		}
		else
			return null;
	}

	/////////////////////////////////////////////////////////////////
	// HISTORY
	/////////////////////////////////////////////////////////////////

	public List getHistory(String humanReadableIdent, String label, Date start)
	{
		String ident = humanReadable.get(humanReadableIdent);

		if(ident != null) {
			Device device = (Device) registry.get(ident);
			return getHistory(device.getSourceProtocol(), device.getChannel(), label, start, null);
		}
		else
			return null;
	}

	public List getHistory(String humanReadableIdent, String label, Date start, Date stop)
	{
		String ident = humanReadable.get(humanReadableIdent);

		if(ident != null) {
			Device device = (Device) registry.get(ident);
			return getHistory(device.getSourceProtocol(), device.getChannel(), label, start, stop);
		}
		else
			return null;
	}

	public List getHistory(SourceProtocol proto, Short channel, String label, Date start)
	{
		return getHistory(proto, channel, label, start, null);
	}

	public List getHistory(SourceProtocol proto, Short channel, String label, Date start, Date stop)
	{
		Device device = (Device) getDevice(proto, channel);

		for(String key : device.getDeviceValues().keySet()) {
			if(key.equals(label))
				return getHistory(device.getDeviceValues().get(label).getId(), start, stop);
		}

		return null;
	}

	private List getHistory(long id, Date startDate, Date stopDate)
	{
		String SQL = "FROM DeviceValueChange AS c WHERE c.deviceValue.id = :id AND c.date BETWEEN :stDate AND";

		if(stopDate == null) {
			SQL += " current_date order by c.date desc";
			return em.createQuery(SQL)
					.setParameter("id", id)
					.setParameter("stDate", startDate)
					.getResultList();
		}
		else{
			SQL += " :enDate order by c.date desc";
			return em.createQuery(SQL)
					.setParameter("id", id)
					.setParameter("stDate", startDate)
					.setParameter("enDate", stopDate)
					.getResultList();
		}
	}

}
