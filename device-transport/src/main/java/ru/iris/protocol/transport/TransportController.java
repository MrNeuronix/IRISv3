package ru.iris.protocol.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.models.bus.Queue;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.models.bus.transport.GPSDataEvent;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.enums.*;

@Component
@Profile("transport")
@Qualifier("transport")
@Slf4j
public class TransportController extends AbstractProtocolService {
	@Autowired
    private EventBus r;

	@Autowired
    private ConfigLoader config;

    @Autowired
    private DeviceRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onStartup() {
        logger.info("TransportController started");
        if (!config.loadPropertiesFormCfgDirectory("transport"))
            logger.error("Cant load transport-specific configs. Check transport.property if exists");
    }

    @Override
    public void onShutdown() {
        logger.info("TransportController stopping");
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription(Queue.EVENT_TRANSPORT);
    }

    @Override
    public Consumer<Event<?>> handleMessage() {
        return event -> {
            if (event.getData() instanceof GPSDataEvent) {
                handleGPSData((GPSDataEvent) event.getData());
            }
        };
    }

    private void handleGPSData(GPSDataEvent data) {
        Device device = registry.getDevice(SourceProtocol.TRANSPORT, String.valueOf(data.getTransportId()));

        if(device == null) {
            String channel = String.valueOf(data.getTransportId());
            device = Device.builder()
                    .channel(channel)
                    .manufacturer("Not supported")
                    .productName("Not supported")
                    .humanReadable("transport/channel/"+channel)
                    .source(SourceProtocol.TRANSPORT)
                    .type(DeviceType.TRANSPORT)
                    .state(State.ACTIVE)
                    .build();

            device = registry.addOrUpdateDevice(device);
            broadcast(Queue.EVENT_DEVICE_ADDED, new DeviceProtocolEvent(channel, SourceProtocol.TRANSPORT, "DeviceAdded"));
        }

        try {
            registry.addChange(
                    device,
                    StandartDeviceValueLabel.GPS_DATA.getName(),
                    objectMapper.writeValueAsString(data),
                    ValueType.JSON
            );
        } catch (JsonProcessingException e) {
            logger.error("Can't serailize data: ", e);
        }
    }

    @Override
    @Async
    public void run() {

    }

    @Override
    public String getServiceIdentifier() {
        return "transport";
    }
}
