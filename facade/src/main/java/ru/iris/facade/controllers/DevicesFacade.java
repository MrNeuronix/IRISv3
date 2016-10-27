package ru.iris.facade.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import ru.iris.commons.protocol.Device;
import ru.iris.commons.service.ProtocolService;

import java.util.ArrayList;
import java.util.List;

@RestController
@Profile("facade")
public class DevicesFacade {

	private final ProtocolService zwave;
	private final ProtocolService nooliteRx;
	private final ProtocolService nooliteTx;

	private static final Logger logger = LoggerFactory.getLogger(DevicesFacade.class);

	@Autowired(required = false)
	public DevicesFacade(
			@Qualifier("zwave") ProtocolService zwave,
			@Qualifier("nooliterx") ProtocolService nooliteRx,
			@Qualifier("noolitetx") ProtocolService nooliteTx
	)
	{
		this.zwave = zwave;
		this.nooliteRx = nooliteRx;
		this.nooliteTx = nooliteTx;
	}

	@RequestMapping(value = "/devices/{source}", method = RequestMethod.GET)
	public List<Device> getAllDevices(@PathVariable(value="source") String source) {

		List<Device> ret = new ArrayList<>();
		if(source.equals("all") || source.equals("zwave"))
			ret.addAll(zwave.getDevices().values());
		if(source.equals("all") || source.equals("noolite"))
			ret.addAll(nooliteRx.getDevices().values());

		return ret;
	}

	@RequestMapping("/devices/channel")
	public Device getDeviceByChannel(@RequestParam(value="channel", defaultValue="1") Byte channel) {
		return null;
	}
}

