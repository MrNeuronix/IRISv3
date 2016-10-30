package ru.iris.facade.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.devices.DeviceOff;
import ru.iris.commons.bus.devices.DeviceOn;
import ru.iris.commons.bus.devices.DeviceSetValue;
import ru.iris.commons.protocol.Device;
import ru.iris.commons.service.ProtocolService;
import ru.iris.facade.status.ErrorStatus;
import ru.iris.facade.status.OkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@Profile("facade")
public class DevicesFacade {

	private final ProtocolService<Device> zwave;
	private final ProtocolService<Device> nooliteRx;
	private EventBus r;

	private static final Logger logger = LoggerFactory.getLogger(DevicesFacade.class);

	@Autowired(required = false)
	public DevicesFacade(
			@Qualifier("zwave") ProtocolService<Device> zwave,
			@Qualifier("nooliterx") ProtocolService<Device> nooliteRx,
			@Qualifier("noolitetx") ProtocolService<Device> nooliteTx
	)
	{
		this.zwave = zwave;
		this.nooliteRx = nooliteRx;
	}

	@Autowired
	public void setR(EventBus r) {
		this.r = r;
	}

	/**
	 * Return all devices (by source if specified)
	 *
	 * @param source source protocol (optional)
	 * @return list of devices
	 */
	@RequestMapping(value = "/api/devices/{source}", method = RequestMethod.GET)
	public List<Device> getAllDevices(@PathVariable(value="source") String source) {

		List<Device> ret = new ArrayList<>();
		if (zwave != null && (source.equals("all") || source.equals("zwave")))
			ret.addAll(zwave.getDevices().values());
		if (nooliteRx != null && (source.equals("all") || source.equals("noolite")))
			ret.addAll(nooliteRx.getDevices().values());

		return ret;
	}

	/**
	 * Return device by source and channel
	 *
	 * @param source  source protocol
	 * @param channel channel of device
	 * @return device
	 */
	@RequestMapping("/api/device/{source}/channel/{channel}")
	public Object getDeviceByChannel(@PathVariable(value = "source") String source, @PathVariable(value = "channel") Short channel) {
		List<Device> ret = new ArrayList<>();
		if (zwave != null && (source.equals("all") || source.equals("zwave")))
			ret.addAll(zwave.getDevices().values());
		if (nooliteRx != null && (source.equals("all") || source.equals("noolite")))
			ret.addAll(nooliteRx.getDevices().values());

		for (Device device : ret) {
			if (Objects.equals(device.getChannel(), channel))
				return device;
		}
		return new ErrorStatus("device not found");
	}

	/**
	 * Set on/off/level on device by source and channel
	 *
	 * @param source  source protocol
	 * @param channel channel of device
	 * @param level   level for set (can be on/off/0-255)
	 * @return ok or error status
	 */
	@RequestMapping("/api/device/{source}/channel/{channel}/{level}")
	public Object onDeviceByChannel(@PathVariable(value = "source") String source,
	                                @PathVariable(value = "channel") Short channel,
	                                @PathVariable(value = "level") String level) {
		List<Device> ret = new ArrayList<>();
		if (zwave != null && (source.equals("all") || source.equals("zwave")))
			ret.addAll(zwave.getDevices().values());
		if (nooliteRx != null && (source.equals("all") || source.equals("noolite")))
			ret.addAll(nooliteRx.getDevices().values());

		for (Device device : ret) {
			if (Objects.equals(device.getChannel(), channel)) {
				switch (level) {
					case "on":
					case "255":
						setDeviceLevel(device, new DeviceOn(device.getChannel()));
						break;
					case "off":
					case "0":
						setDeviceLevel(device, new DeviceOff(device.getChannel()));
						break;
					default:
						try {
							Short bLevel = Short.valueOf(level);

							if (bLevel > 0 && bLevel < 255)
								setDeviceLevel(device, new DeviceSetValue(device.getChannel(), "level", bLevel));
							else
								return new ErrorStatus("incorrect value level");
						} catch (NumberFormatException ex) {
							return new ErrorStatus("parse error");
						}
						break;
				}
				return new OkStatus("message sent");
			}
		}
		return new ErrorStatus("device not found");
	}

	// sent message
	private void setDeviceLevel(Device device, Object message) {
		r.notify("command.device." + device.getSourceProtocol().name().toLowerCase(), Event.wrap(message));
	}
}
