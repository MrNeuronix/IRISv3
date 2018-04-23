package ru.iris.protocol.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenetics.jpx.GPX;
import javastrava.api.v3.auth.TokenManager;
import javastrava.api.v3.auth.model.Token;
import javastrava.api.v3.auth.ref.AuthorisationScope;
import javastrava.api.v3.model.reference.StravaActivityType;
import javastrava.api.v3.service.Strava;
import javastrava.api.v3.service.exception.BadRequestException;
import javastrava.api.v3.service.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Instant;
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
import ru.iris.models.bus.transport.*;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.data.DataGPS;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.enums.*;
import ru.iris.protocol.transport.utils.StravaHttpUtils;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final int noActivityMinutes = 10;
    private final double staleSpeed = 2D;
    private final double minSpeed = 5D;
    private Map<Integer, Integer> speedStale = new HashMap<>();
    private Map<Integer, Long> lastPing = new HashMap<>();
    private Map<Integer, List<GPSDataEvent>> tracks = new HashMap<>();

    @Override
    public void onStartup() {
        logger.info("TransportController started");
        if (!config.loadPropertiesFormCfgDirectory("transport"))
            logger.error("Cant load transport-specific configs. Check transport.property if exists");
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000, initialDelay = 20_000) // 6 hours
    @PreDestroy
    public void cleanDatabase() {
        logger.info("Clean transport GPS and voltage history");
        List<Device> transports = registry.getDevicesByProto(SourceProtocol.TRANSPORT);
        int days = Integer.parseInt(config.get("data.gps.days"));
        int daysVoltage = Integer.parseInt(config.get("data.voltage.days"));

        transports.forEach(transport ->
                registry.deleteHistory(
                        SourceProtocol.TRANSPORT,
                        transport.getChannel(),
                        StandartDeviceValueLabel.GPS_DATA.getName(),
                        new DateTime().minusDays(days).toDate())
        );
        transports.forEach(transport ->
                registry.deleteHistory(
                        SourceProtocol.TRANSPORT,
                        transport.getChannel(),
                        StandartDeviceValueLabel.VOLTAGE.getName(),
                        new DateTime().minusDays(daysVoltage).toDate())
        );
    }

    @Scheduled(fixedRate = 15 * 60 * 1000, initialDelay = 30_000) // 15 minutes
    @PreDestroy
    public void saveTrack() {
        logger.info("Looking for GPS tracks save");
        for (Integer id : lastPing.keySet()) {
            long delta = (Instant.now().getMillis() - lastPing.get(id)) * 1000 * 60; // in minutes
            int stale = speedStale.get(id) == null ? 0 : speedStale.get(id);
            int trackSize = tracks.get(id) == null ? 0 : tracks.get(id).size();

            if (delta >= noActivityMinutes || stale >= 60 * noActivityMinutes) { // no info or speed stale - save tracks
                try {
                    if (trackSize > 10) {
                        logger.info("Saving GPS tracks for transport {}", id);
                        writeTrack(id);
                    }
                } finally {
                    tracks.remove(id);
                    lastPing.remove(id);
                    speedStale.remove(id);
                }
            }
        }
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
            } else if (event.getData() instanceof TransportPingEvent) {
                handlePing((TransportPingEvent) event.getData());
            }else {
                logger.error("Unknown request come to transport controller. Class: {}", event.getData().getClass());
            }
        };
    }

    private void handlePing(TransportPingEvent data) {
        Device device = getDevice(data);

        lastPing.put(data.getTransportId(), Instant.now().getMillis());

        broadcast(Queue.EVENT_DEVICE_PING, DeviceChangeEvent.builder()
                .channel(device.getChannel())
                .protocol(SourceProtocol.TRANSPORT)
                .eventLabel("Ping")
                .build()
        );
    }

    private void handleBatteryData(BatteryDataEvent data) {
        Device device = getDevice(data);

        registry.addChange(
                device,
                StandartDeviceValueLabel.VOLTAGE.getName(),
                data.getVoltage().toString(),
                ValueType.DOUBLE
        );

        broadcast(Queue.EVENT_VOLTAGE, DeviceChangeEvent.builder()
                .channel(device.getChannel())
                .protocol(SourceProtocol.TRANSPORT)
                .eventLabel("VoltageChange")
                .data(new DataLevel(data.getVoltage().toString(), ValueType.DOUBLE))
                .build()
        );
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
            logger.error("Can't serialize data: ", e);
        }

        tracks.computeIfAbsent(data.getTransportId(), k -> new ArrayList<>());

        int stale = speedStale.get(data.getTransportId()) == null ? 0 : speedStale.get(data.getTransportId());
        if (data.getSpeed() <= staleSpeed) {
            speedStale.put(data.getTransportId(), ++stale);
        }

        if (data.getSpeed() >= minSpeed) {
            speedStale.put(data.getTransportId(), 0);
        }

        if (data.getSpeed() >= minSpeed || stale <= 2 * 60) {
            tracks.get(data.getTransportId()).add(data);
        }

        broadcast(Queue.EVENT_GPS_DATA, DeviceChangeEvent.builder()
                .channel(device.getChannel())
                .protocol(SourceProtocol.TRANSPORT)
                .eventLabel("GPSChange")
                .data(new DataGPS(data.getLatitude(), data.getLongitude(), data.getSpeed(), data.getElevation()))
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

    private void writeTrack(Integer id) {
        if (tracks.get(id) == null || tracks.get(id).size() <= 10) {
            return;
        }

        final GPX gpx = GPX.builder()
                .addTrack(track -> track
                        .addSegment(segment ->
                                tracks.get(id).forEach(point -> {
                                    if (point != null) {
                                        segment.addPoint(p ->
                                                p
                                                        .lat(point.getLatitude())
                                                        .lon(point.getLongitude())
                                                        .ele(point.getElevation())
                                                        .speed(point.getSpeed())
                                                        .time(point.getTime() * 1000L)
                                        );
                                    }
                                })
                        ))
                .build();

        String filename;
        try {
            filename = "gpx/track-" + id + "-" + Instant.now().getMillis() + ".gpx";
            GPX.write(gpx, filename);
        } catch (IOException e) {
            logger.error("IOException while writing GPX track file", e);
            return;
        }

        if (config.get("strava.export").equals("true")) {
            logger.info("Exporting GPS track to Strava ({})", filename);
            Token token = getValidTokenWithFullAccess();
            Strava strava = new Strava(token);

            strava.upload(
                    StravaActivityType.EBIKE_RIDE,
                    "GPS track integration upload - " + Instant.now().toString(),
                    "GPS track exported from IRIS smart home system",
                    false,
                    false,
                    false,
                    "gpx",
                    null,
                    new File(filename)
            );

            logger.info("Exported ({})", filename);
        }
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

    private Token getValidTokenWithFullAccess() {
        return tokenWithExactScope(AuthorisationScope.WRITE, AuthorisationScope.VIEW_PRIVATE);
    }

    private Token tokenWithExactScope(final AuthorisationScope... scopes) {
        Token token = TokenManager.instance().retrieveTokenWithExactScope(config.get("strava.username"), scopes);
        if (token == null) {
            try {
                StravaHttpUtils utils = new StravaHttpUtils(config.get("strava.client.id"), config.get("strava.client.secret"));
                token = utils.getStravaAccessToken(config.get("strava.username"), config.get("strava.password"), scopes);
                TokenManager.instance().storeToken(token);
            } catch (BadRequestException | UnauthorizedException e) {
                logger.error("Strava error: ", e);
                return null;
            }
        }
        return token;
    }
}
