package ru.iris.protocol.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.annotations.RunOnStartup;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.commons.service.AbstractProtocolService;
import ru.iris.models.bus.Queue;
import ru.iris.models.bus.devices.DeviceChangeEvent;
import ru.iris.models.bus.devices.DeviceCommandEvent;
import ru.iris.models.bus.devices.DeviceProtocolEvent;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.protocol.data.DataLevel;
import ru.iris.models.protocol.enums.*;
import ru.iris.models.service.ServiceState;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author nix, 19.06.18
 */

@Service("httpdevice")
@Profile("httpdevice")
@RunOnStartup
@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HttpDeviceController extends AbstractProtocolService {
    private final Type itemListType = new TypeToken<ArrayList<HTTPDevice>>() {
    }.getType();
    @Autowired
    private EventBus r;
    @Autowired
    private DeviceRegistry registry;
    @Autowired
    private ConfigLoader config;
    @Autowired
    private Gson gson;
    private List<HTTPDevice> httpDevices;

    @Override
    public void onStartup() throws InterruptedException {
        setServiceState(ServiceState.STARTING);
        logger.info("HttpDeviceController started");
        if (!config.loadPropertiesFormCfgDirectory("httpdevices")) {
            logger.error("Cant load httpdevice-specific configs. Check httpdevices.properties if exists");
        }

        String devicesConfig = config.get("httpdevices");

        if (StringUtils.isEmpty(devicesConfig)) {
            logger.error("No devices specified!");
        } else {
            httpDevices = gson.fromJson(devicesConfig, itemListType);

            if (httpDevices != null && httpDevices.size() > 0) {
                logger.info("Loaded {} http devices", httpDevices.size());
                setServiceState(ServiceState.STARTED);
            } else {
                logger.error("No http devices loaded from config file - stopping service!");
                setServiceState(ServiceState.STOPPED);
            }
        }
    }

    @Override
    public void onShutdown() {
        setServiceState(ServiceState.STOPPING);
        logger.info("HttpDeviceController stopping");
        setServiceState(ServiceState.STOPPED);
    }

    @Override
    public Consumer<Event<?>> handleMessage() throws Exception {
        return event -> {
            if (event.getData() instanceof DeviceCommandEvent) {
                if (!getServiceState().equals(ServiceState.RUNNING)) {
                    setServiceState(ServiceState.RUNNING);
                }
                DeviceCommandEvent x = (DeviceCommandEvent) event.getData();

                if (!x.getProtocol().equals(SourceProtocol.HTTP)) {
                    return;
                }

                if (CollectionUtils.isEmpty(httpDevices)) {
                    logger.error("No http devices configured!");
                    return;
                }

                switch (EventLabel.parse(x.getEventLabel())) {
                    case TURN_ON:
                        if (x.getData() instanceof DataLevel) {
                            logger.info("Turn ON device on channel {}", x.getChannel());

                            Optional<HTTPDevice> deviceOptional = httpDevices.stream()
                                    .filter(d -> d.getId().equals(x.getChannel()))
                                    .findFirst();

                            if (deviceOptional.isPresent()) {
                                HTTPDevice httpDevice = deviceOptional.get();
                                HttpRequest.get(httpDevice.getUrl() + "/" + httpDevice.getName().toLowerCase() + "/on").send();
                            }
                        } else {
                            logger.error("Unknown data class!");
                        }
                        break;
                    case TURN_OFF:
                        if (x.getData() instanceof DataLevel) {
                            logger.info("Turn OFF device on channel {}", x.getChannel());

                            Optional<HTTPDevice> deviceOptional = httpDevices.stream()
                                    .filter(d -> d.getId().equals(x.getChannel()))
                                    .findFirst();

                            if (deviceOptional.isPresent()) {
                                HTTPDevice httpDevice = deviceOptional.get();
                                HttpRequest.get(httpDevice.getUrl() + "/" + httpDevice.getName().toLowerCase() + "/off").send();
                            }
                        } else {
                            logger.error("Unknown data class!");
                        }
                        break;
                    default:
                        logger.info("Received unknown request for HttpDevice service! Class: {}", event.getData().getClass());
                        break;
                }
            }
        };
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription(Queue.COMMAND_DEVICE);
    }

    @Override
    public String getServiceIdentifier() {
        return "http device";
    }

    @Scheduled(fixedDelay = 15_000, initialDelay = 10_000)
    public void doWork() {
        if (getServiceState().equals(ServiceState.STOPPED)) {
            return;
        }

        logger.debug("Check HTTP devices");

        httpDevices.forEach(deviceFromCfg -> {
            Device device = registry.getDevice(SourceProtocol.HTTP, deviceFromCfg.getId());

            if (device == null) {
                device = Device.builder()
                        .source(SourceProtocol.HTTP)
                        .channel(deviceFromCfg.getId())
                        .manufacturer("Unknown")
                        .productName("Unknown")
                        .state(State.UNKNOWN)
                        .humanReadable("http/channel/" + deviceFromCfg.getId())
                        .type(DeviceType.MULTILEVEL_SWITCH)
                        .values(new HashMap<>())
                        .build();

                device = registry.addOrUpdateDevice(device);

                registry.addChange(device, StandartDeviceValueLabel.LEVEL.getName(), "0", ValueType.DOUBLE);
                registry.addChange(device, "min", String.valueOf(deviceFromCfg.getMin()), ValueType.DOUBLE);
                registry.addChange(device, "max", String.valueOf(deviceFromCfg.getMax()), ValueType.DOUBLE);

                broadcast("event.device.added", new DeviceProtocolEvent(deviceFromCfg.getId(), SourceProtocol.HTTP, "DeviceAdded"));
            }

            DeviceValue level = device.getValues().get(StandartDeviceValueLabel.LEVEL.getName());
            DeviceValue min = device.getValues().get("min");
            DeviceValue max = device.getValues().get("max");

            HttpResponse response = HttpRequest.get(deviceFromCfg.getUrl() + "/status").send();
            List<HTTPDevice> itemsResponse = gson.fromJson(response.bodyText(), itemListType);

            Optional<HTTPDevice> itemOptional = itemsResponse.stream()
                    .filter(i -> i.getName().equals(deviceFromCfg.getName()))
                    .findFirst();

            if (itemOptional.isPresent()) {
                HTTPDevice httpItem = itemOptional.get();

                if (level == null || level.getCurrentValue() == null || !level.getCurrentValue().equals(httpItem.getLevel().toString())) {
                    logger.info("Channel: {}: Device \"{}\" level is \"{}\"", device.getHumanReadable(), httpItem.getName(), httpItem.getLevel());
                    registry.addChange(device, StandartDeviceValueLabel.LEVEL.getName(), httpItem.getLevel().toString(), ValueType.DOUBLE);

                    broadcast("event.device.level", new DeviceChangeEvent(
                            device.getChannel(),
                            SourceProtocol.HTTP,
                            StandartDeviceValueLabel.LEVEL.getName(),
                            httpItem.getLevel().toString(),
                            ValueType.DOUBLE)
                    );
                }

                if (max == null || max.getCurrentValue() == null || !max.getCurrentValue().equals(httpItem.getMax().toString())) {
                    logger.info("Channel: {}: Device \"{}\" maximum level is \"{}\"", device.getHumanReadable(), httpItem.getName(), httpItem.getMax());
                    registry.addChange(device, "max", httpItem.getMax().toString(), ValueType.DOUBLE);
                }

                if (min == null || min.getCurrentValue() == null || !min.getCurrentValue().equals(httpItem.getMin().toString())) {
                    logger.info("Channel: {}: Device \"{}\" minimum level is \"{}\"", device.getHumanReadable(), httpItem.getName(), httpItem.getMin());
                    registry.addChange(device, "min", httpItem.getMin().toString(), ValueType.DOUBLE);
                }

            } else {
                logger.error("No item \"{}\" in response from {}", deviceFromCfg.getName(), deviceFromCfg.getUrl());
            }

        });
    }

    @Override
    public void run() throws InterruptedException {

    }

    @Getter
    @Setter
    private class HTTPDevice {
        private String id;
        private String url;
        private String name;
        private Integer min;
        private Integer max;
        private Integer level;

    }
}
