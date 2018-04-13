package ru.iris.protocol.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.models.bus.Queue;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.models.bus.transport.AbstractTransportEvent;
import ru.iris.models.bus.transport.BatteryDataEvent;
import ru.iris.models.bus.transport.GPSDataEvent;
import ru.iris.models.bus.transport.TransportConnectEvent;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.data.DataGPS;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.enums.*;

import javax.annotation.PreDestroy;
import java.util.List;

@Component
@Profile("transport")
@Qualifier("transport")
@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
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

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000, initialDelay = 20_000) // 6 hours
    @PreDestroy
    public void cleanDatabase() {
        logger.info("Clean transport GPS history");
        List<Device> transports = registry.getDevicesByProto(SourceProtocol.TRANSPORT);
        int days = Integer.parseInt(config.get("data.gps.days"));
        transports.forEach(transport ->
                registry.deleteHistory(
                        SourceProtocol.TRANSPORT,
                        transport.getChannel(),
                        StandartDeviceValueLabel.GPS_DATA.getName(),
                        new DateTime().minusDays(days).toDate())
        );
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
            if (event.getData() instanceof TransportConnectEvent) {
                handleConnect((TransportConnectEvent) event.getData());
            }
            else if (event.getData() instanceof GPSDataEvent) {
                handleGPSData((GPSDataEvent) event.getData());
            } else if(event.getData() instanceof BatteryDataEvent) {
                handleBatteryData((BatteryDataEvent) event.getData());
            } else {
                logger.error("Unknown request come to transport controller. Class: {}", event.getData().getClass());
            }
        };
    }

    private void handleBatteryData(BatteryDataEvent data) {
        Device device = getDevice(data);

        try {
            registry.addChange(
                    device,
                    StandartDeviceValueLabel.VOLTAGE.getName(),
                    objectMapper.writeValueAsString(data),
                    ValueType.JSON
            );

            broadcast(Queue.EVENT_VOLTAGE, DeviceChangeEvent.builder()
                    .channel(device.getChannel())
                    .protocol(SourceProtocol.TRANSPORT)
                    .eventLabel("VoltageChange")
                    .data(new DataLevel(data.getVoltage().toString(), ValueType.DOUBLE))
                    .build()
            );
        } catch (JsonProcessingException e) {
            logger.error("Can't serailize data: ", e);
        }
    }

    private void handleConnect(TransportConnectEvent data) {
        Device device = getDevice(data);
        broadcast(Queue.EVENT_DEVICE_CONNECTED, new DeviceProtocolEvent(device.getChannel(), SourceProtocol.TRANSPORT, "DeviceOnline"));
    }

    private void handleGPSData(GPSDataEvent data) {
        Device device = getDevice(data);

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

        broadcast(Queue.EVENT_GPS_DATA, DeviceChangeEvent.builder()
                .channel(device.getChannel())
                .protocol(SourceProtocol.TRANSPORT)
                .eventLabel("GPSChange")
                .data(new DataGPS(data.getLatitude(), data.getLongitude(), data.getSpeed()))
                .build()
        );
    }

    @Override
    @Async
    public void run() {

    }

    @Override
    public String getServiceIdentifier() {
        return "transport";
    }

    private Device getDevice(AbstractTransportEvent data) {
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

        return device;
    }
}
