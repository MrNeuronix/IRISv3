package ru.iris.facade.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.enums.EventLabel;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.models.web.DeviceInfoRequest;
import ru.iris.models.web.DeviceNamingRequest;
import ru.iris.models.web.DeviceSetLevelRequest;
import ru.iris.models.status.ErrorStatus;
import ru.iris.models.status.OkStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@Profile("facade")
@Slf4j
public class DevicesFacade {

    @Autowired
    private DeviceRegistry registry;

    @Autowired
    private EventBus r;

    /**
     * Return all devices (by source) or device on specified channel and source
     *
     * @param request request
     * @return list of devices
     */
    @RequestMapping(value = "/api/device/get", method = RequestMethod.POST)
    public List<Object> getAllDevices(@RequestBody DeviceInfoRequest request) {
        List<Object> ret = new ArrayList<>();

        if (request.getChannel() == null && request.getSource() == null) {
            ret.addAll(registry.getDevices());
        } else if (request.getChannel() != null && request.getSource() != null) {
            Device device = registry.getDevice(request.getSource(), request.getChannel());
            return Collections.singletonList(device);
        } else if (request.getChannel() == null && request.getSource() != null) {
            ret.addAll(registry.getDevicesByProto(request.getSource()));
        } else if (request.getSource() == null && request.getChannel() != null) {
            return Collections.singletonList(new ErrorStatus("Specify source"));
        }

        return ret;
    }

    /**
     * Set on/off/level on device by source and channel
     *
     * @param request request
     * @return ok or error status
     */
    @RequestMapping("/api/device/set")
    public Object onDeviceByChannel(@RequestBody DeviceSetLevelRequest request) {

        if (request.getSource() == null || request.getSource() == null)
            return new ErrorStatus("source field is empty or null");

        Device device = registry.getDevice(request.getSource(), request.getChannel());

        if (device == null)
            return new ErrorStatus("device not found");

        switch (request.getLevel()) {
            case "on":
            case "255":
                if (request.getSubchannel() == null || request.getSubchannel() == 0) {
                    setDeviceLevel(new DeviceCommandEvent(device.getChannel(), request.getSource(), EventLabel.TURN_ON));
                } else {
                    setDeviceLevel(new DeviceCommandEvent(device.getChannel(), request.getSubchannel(), request.getSource(), EventLabel.TURN_ON));
                }
                break;
            case "off":
            case "0":
                if (request.getSubchannel() == null || request.getSubchannel() == 0) {
                    setDeviceLevel(new DeviceCommandEvent(device.getChannel(), request.getSource(), EventLabel.TURN_OFF));
                } else {
                    setDeviceLevel(new DeviceCommandEvent(device.getChannel(), request.getSubchannel(), request.getSource(), EventLabel.TURN_OFF));
                }
                break;
            default:
                try {
                    Short bLevel = Short.valueOf(request.getLevel());

                    if (bLevel > 0 && bLevel < 255) {
                        if (request.getSubchannel() == null || request.getSubchannel() == 0) {
                            setDeviceLevel(new DeviceCommandEvent(device.getChannel(), request.getSource(), EventLabel.SET_LEVEL, request.getLevel()));
                        } else {
                            setDeviceLevel(new DeviceCommandEvent(device.getChannel(), request.getSubchannel(), request.getSource(), EventLabel.SET_LEVEL, request.getLevel()));
                        }
                    } else {
                        return new ErrorStatus("incorrect value level");
                    }
                } catch (NumberFormatException ex) {
                    return new ErrorStatus("parse error");
                }
                break;
        }

        return new OkStatus("message sent");
    }

	/**
	 * Name device
	 *
	 * @param request request
	 * @return result text
	 */
	@RequestMapping(value = "/api/device/name", method = RequestMethod.POST)
	public Object setNameForDevice(@RequestBody DeviceNamingRequest request) {
		if (request.getSource() == null || request.getSource() == null || request.getName() == null)
			return new ErrorStatus("source fields is empty or null");

		Device device = registry.getDevice(request.getSource(), request.getChannel());

		if (device == null)
			return new ErrorStatus("device not found");

		device.setHumanReadable(request.getName());
		registry.addOrUpdateDevice(device);

		return new OkStatus("device saved");
	}

    // sent message
    private void setDeviceLevel(Object message) {
        r.notify("command.device", Event.wrap(message));
    }
}
