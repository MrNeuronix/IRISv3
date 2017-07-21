package ru.iris.zwave.protocol.model;

import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.abstracts.AbstractDevice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZWaveDevice extends AbstractDevice {

    private long homeId;
    private Map<String, ZWaveDeviceValue> values = new ConcurrentHashMap<>();

    @Override
    public Map<String, ZWaveDeviceValue> getDeviceValues() {
        return values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setDeviceValues(Map<String, ? extends DeviceValue> values) {
        this.values = (Map<String, ZWaveDeviceValue>) values;
    }

    public long getHomeId() {
        return homeId;
    }

    public void setHomeId(long homeId) {
        this.homeId = homeId;
    }

    @Override
    public String toString() {
        return "ZWaveDevice{" +
                "id=" + id +
                ", date=" + date +
                ", humanReadable='" + humanReadable + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", productName='" + productName + '\'' +
                ", homeId=" + homeId +
                ", channel=" + channel +
                ", source=" + source +
                ", type=" + type +
                ", zone=" + zone +
                ", state=" + state +
                ", values=" + values +
                '}';
    }

}
