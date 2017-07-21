package ru.iris.commons.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.devices.DeviceCommandEvent;
import ru.iris.commons.protocol.Device;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.registry.DeviceRegistry;

@Component
public class DeviceHelper {

    private final EventBus r;
    private final DeviceRegistry deviceRegistry;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public DeviceHelper(EventBus r, DeviceRegistry deviceRegistry) {
        this.r = r;
        this.deviceRegistry = deviceRegistry;
    }

    public void on(String ident) {
        Device device = (Device) deviceRegistry.getDevice(ident);
        if (device != null) {
            if (device.getSourceProtocol().equals(SourceProtocol.NOOLITE))
                r.notify("command.device.noolite", Event.wrap(new DeviceCommandEvent(
                        device.getChannel(), device.getSourceProtocol(), "TurnOn")));
            else if (device.getSourceProtocol().equals(SourceProtocol.ZWAVE))
                r.notify("command.device.zwave", Event.wrap(new DeviceCommandEvent(
                        device.getChannel(), device.getSourceProtocol(), "TurnOn")));
            else
                logger.warn("Source Protocol unknown: " + device.getSourceProtocol());
        }
    }

    public void off(String ident) {
        Device device = (Device) deviceRegistry.getDevice(ident);
        if (device != null) {
            if (device.getSourceProtocol().equals(SourceProtocol.NOOLITE))
                r.notify("command.device.noolite", Event.wrap(new DeviceCommandEvent(
                        device.getChannel(), device.getSourceProtocol(), "TurnOff")));
            else if (device.getSourceProtocol().equals(SourceProtocol.ZWAVE))
                r.notify("command.device.zwave", Event.wrap(new DeviceCommandEvent(
                        device.getChannel(), device.getSourceProtocol(), "TurnOff")));
            else
                logger.warn("Source Protocol unknown: " + device.getSourceProtocol());
        }
    }

    public void level(String ident, Object level) {
        Device device = (Device) deviceRegistry.getDevice(ident);
        if (device != null) {
            if (device.getSourceProtocol().equals(SourceProtocol.NOOLITE))
                r.notify("command.device.noolite", Event.wrap(new DeviceCommandEvent(
                        device.getChannel(), device.getSourceProtocol(), "SetLevel", level)));
            else if (device.getSourceProtocol().equals(SourceProtocol.ZWAVE))
                r.notify("command.device.zwave", Event.wrap(new DeviceCommandEvent(
                        device.getChannel(), device.getSourceProtocol(), "SetLevel", level)));
            else
                logger.warn("Source Protocol unknown: " + device.getSourceProtocol());
        }
    }

}
