package ru.iris.protocol.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.commons.bus.devices.DeviceChangeEvent;
import ru.iris.commons.database.model.Device;
import ru.iris.commons.model.status.BackendAnswer;
import ru.iris.commons.model.status.ErrorStatus;
import ru.iris.commons.model.status.OkStatus;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.ValueType;
import ru.iris.commons.registry.DeviceRegistry;

@Component
@Profile("httpapi")
@Qualifier("httpapi")
@RestController
@Slf4j
public class HttpApiController {

    @Autowired
    private EventBus r;

    @Autowired
    private DeviceRegistry deviceRegistry;

    @RequestMapping(value = "/api/event/device/{channel}/state/{state}", method = RequestMethod.GET)
    public BackendAnswer switchDeviceState(@PathVariable Short channel, @PathVariable Boolean state) {

        Device device = deviceRegistry.getDevice(SourceProtocol.HTTP, channel);

        if (device == null) {
            logger.info("Device channel {} on protocol HTTP not found! Creating", channel);

            device = new Device();
            device.setSource(SourceProtocol.HTTP);
            device.setHumanReadable("http/channel/" + channel);
            device.setProductName("");
            device.setType(DeviceType.UNKNOWN);
            device.setState(State.NOT_SUPPORTED);
            device.setManufacturer("HTTP device point");
            device.setChannel(channel);

            device = deviceRegistry.addOrUpdateDevice(device);
        }

        if (state != null) {
            logger.info("HTTP channel {}: State {}", channel, state);
            deviceRegistry.addChange(device, "state", state.toString(), ValueType.BOOL);
            broadcast("event.device.state", new DeviceChangeEvent(channel, SourceProtocol.HTTP, "state", state, ValueType.BOOL));
        } else {
            return new ErrorStatus("No state passed. Only true/false accepted");
        }

        return new OkStatus("Received");
    }

    @RequestMapping(value = "/api/event/device/{channel}/trigger", method = RequestMethod.GET)
    public BackendAnswer armDeviceState(@PathVariable Short channel) {

        Device device = deviceRegistry.getDevice(SourceProtocol.HTTP, channel);

        if (device == null) {
            logger.info("Device channel {} on protocol HTTP not found! Creating", channel);

            device = new Device();
            device.setSource(SourceProtocol.HTTP);
            device.setProductName("");
            device.setHumanReadable("http/channel/" + channel);
            device.setType(DeviceType.UNKNOWN);
            device.setState(State.NOT_SUPPORTED);
            device.setManufacturer("HTTP device point");
            device.setChannel(channel);

            device = deviceRegistry.addOrUpdateDevice(device);
        }

        logger.info("HTTP channel {}: Triggered", channel);
        deviceRegistry.addChange(device, "alarm", "triggered", ValueType.BOOL);
        broadcast("event.device.trigger", new DeviceChangeEvent(channel, SourceProtocol.HTTP, "alarm", "triggered", ValueType.STRING));

        return new OkStatus("Received");
    }

    private void broadcast(String queue, Object object) {
        r.notify(queue, Event.wrap(object));
    }
}
