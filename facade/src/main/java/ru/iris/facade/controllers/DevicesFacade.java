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
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.facade.model.DeviceInfoRequest;
import ru.iris.facade.model.DeviceSetLevelRequest;
import ru.iris.commons.model.status.ErrorStatus;
import ru.iris.commons.model.status.OkStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@Profile("facade")
@Slf4j
public class DevicesFacade {

    private final DeviceRegistry registry;
    private EventBus r;

    @Autowired(required = false)
    public DevicesFacade(
            DeviceRegistry registry
    ) {
        this.registry = registry;
    }

    @Autowired
    public void setR(EventBus r) {
        this.r = r;
    }

    /**
     * Return all devices (by source) or device on specified channel and source
     *
     * @param request request
     * @return list of devices
     */
    @RequestMapping(value = "/api/device/get", method = RequestMethod.POST)
    public List<Object> getAllDevices(@RequestBody DeviceInfoRequest request) {

        List<Object> ret = new ArrayList<>();

        if (request.getChannel() == null && request.getSource() != null) {

            switch (request.getSource()) {
                case "all":
                    ret.addAll(registry.getDevicesByProto(SourceProtocol.ZWAVE));
                    ret.addAll(registry.getDevicesByProto(SourceProtocol.NOOLITE));
                    break;
                case "zwave":
                    ret.addAll(registry.getDevicesByProto(SourceProtocol.ZWAVE));
                    break;
                case "noolite":
                    ret.addAll(registry.getDevicesByProto(SourceProtocol.NOOLITE));
                    break;
                default:
                    return Collections.singletonList(new ErrorStatus("Unknown source: " + request.getSource()));
            }
        } else if (request.getChannel() != null && request.getSource() != null) {
            Object device;

            switch (request.getSource()) {
                case "zwave":
                    device = registry.getDevice(SourceProtocol.ZWAVE, request.getChannel());
                    break;
                case "noolite":
                    device = registry.getDevice(SourceProtocol.NOOLITE, request.getChannel());
                    break;
                default:
                    return Collections.singletonList(new ErrorStatus("Unknown source: " + request.getSource()));
            }

            return Collections.singletonList(device);
        } else if (request.getSource() == null && request.getChannel() == null) {
            return Collections.singletonList(new ErrorStatus("Specify source (and channel)"));
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

        if (request.getSource() == null || request.getSource().isEmpty())
            return new ErrorStatus("source field is empty or null");

        SourceProtocol sourceProtocol;
        switch (request.getSource()) {
            case "zwave":
                sourceProtocol = SourceProtocol.ZWAVE;
                break;
            case "noolite":
                sourceProtocol = SourceProtocol.NOOLITE;
                break;
            default:
                return new ErrorStatus("protocol unknown");
        }

        Device device = registry.getDevice(sourceProtocol, request.getChannel());

        if (device == null)
            return new ErrorStatus("device not found");

        switch (request.getLevel()) {
            case "on":
            case "255":
                setDeviceLevel(device, new DeviceCommandEvent(device.getChannel(), sourceProtocol, "TurnOn"));
                break;
            case "off":
            case "0":
                setDeviceLevel(device, new DeviceCommandEvent(device.getChannel(), sourceProtocol, "TurnOff"));
                break;
            default:
                try {
                    Short bLevel = Short.valueOf(request.getLevel());

                    if (bLevel > 0 && bLevel < 255)
                        setDeviceLevel(device, new DeviceCommandEvent(device.getChannel(), sourceProtocol, "SetLevel", bLevel, ValueType.INT));
                    else
                        return new ErrorStatus("incorrect value level");
                } catch (NumberFormatException ex) {
                    return new ErrorStatus("parse error");
                }
                break;
        }

        return new OkStatus("message sent");
    }

    // sent message
    private void setDeviceLevel(Device device, Object message) {
        r.notify("command.device." + device.getSource().name().toLowerCase(), Event.wrap(message));
    }
}
