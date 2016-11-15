package ru.iris.commons.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.enums.SourceProtocol;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class DeviceRegistry {

	private Map<String, Object> registry = new ConcurrentHashMap<>();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	public void addOrUpdateDevice(Device device) {

		if(device == null) {
			logger.error("Device, passed into registry is null!");
			return;
		}

		Object tmp = registry.replace(device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel(), device);
		if(tmp == null)
			registry.put(device.getSourceProtocol().name().toLowerCase()+"/channel/"+device.getChannel(), device);
	}

	public void addOrUpdateDevices(List<? extends Device> devices) {
		devices.forEach(device ->  {

			if(device == null) {
				logger.error("Device, passed into registry is null!");
				return;
			}

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

	/////////////////////////////////////////////////////////////////
	// HISTORY
	/////////////////////////////////////////////////////////////////

	public List getHistory(SourceProtocol proto, Short channel, String label, String start)
	{
		return getHistory(proto, channel, label, start, null);
	}

	public List getHistory(SourceProtocol proto, Short channel, String label, String start, String stop)
	{
		Device device = (Device) getDevice(proto, channel);

		for(String key : device.getDeviceValues().keySet()) {
			if(key.equals(label))
				return getHistory(device.getDeviceValues().get(label).getId(), start, stop);
		}

		return null;
	}

	private List getHistory(long id, String start, String stop)
	{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		Date startDate = null;
		Date stopDate = null;
		try {
			if(start != null && !start.isEmpty())
				startDate = format.parse(start);
			if(stop != null && !stop.isEmpty())
				stopDate = format.parse(stop);
		} catch (ParseException e) {
			logger.error("Date parse error");
			return null;
		}

		String SQL = "FROM DeviceValueChange AS c WHERE c.deviceValue.id = :id AND c.date BETWEEN :stDate AND";

		if(stop == null) {
			SQL += " current_date";
			return em.createQuery(SQL)
					.setParameter("id", id)
					.setParameter("stDate", startDate)
					.getResultList();
		}
		else{
			SQL += " :enDate";
			return em.createQuery(SQL)
					.setParameter("id", id)
					.setParameter("stDate", startDate)
					.setParameter("enDate", stopDate)
					.getResultList();
		}
	}

}
