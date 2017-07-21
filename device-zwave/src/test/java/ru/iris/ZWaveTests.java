package ru.iris;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.iris.commons.protocol.ProtocolServiceLayer;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.zwave.protocol.model.ZWaveDevice;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("tests")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ZWaveTests {

    ZWaveDevice device, device2;
    Map<String, ZWaveDeviceValue> values = new HashMap<>();
    @Autowired
    private ProtocolServiceLayer<ZWaveDevice, ZWaveDeviceValue> service;

    @Before
    public void setUp() {

        ZWaveDeviceValue val1 = new ZWaveDeviceValue();
        val1.setName("Test val 1");
        val1.setCurrentValue(2.0D);
        values.put("Test val 1", val1);

        ZWaveDeviceValue val2 = new ZWaveDeviceValue();
        val2.setName("Test val 2");
        val2.setCurrentValue("All ok!");
        val2.setDate(new Date());
        values.put("Test val 2", val2);

        // zwave device 1
        device = new ZWaveDevice();
        device.setHumanReadable("Device 1");
        device.setDate(new Date());
        device.setChannel((short) 1);
        device.setType(DeviceType.BINARY_SWITCH);
        device.setSource(SourceProtocol.ZWAVE);
        device.setManufacturer("Test manufact");
        device.setProductName("Test prod name");
        device.setState(State.ACTIVE);
        device.setDeviceValues(values);

        // zwave device 2
        device2 = new ZWaveDevice();
        device2.setHumanReadable("Device 2");
        device2.setDate(new Date());
        device2.setChannel((short) 2);
        device2.setType(DeviceType.TEMP_HUMI_SENSOR);
        device2.setSource(SourceProtocol.UNKNOWN);
        device2.setManufacturer("Test manufact 2");
        device2.setProductName("Test prod name 2");
        device2.setState(State.DEAD);
    }

    @Test
    public void Z1_canSaveByService() {
        ZWaveDevice zw = service.saveIntoDatabase(device);
        service.saveIntoDatabase(device2);
        Assert.assertEquals(zw.getHumanReadableName(), "Device 1");
    }

    @Test
    public void Z2_canFetchAndConvertByService() {
        Assert.assertEquals(service.getDevices().size(), 2);
    }

    @Test
    public void Z3_correctHumanReadableName() {
        ZWaveDevice zw = service.getDevices().iterator().next();
        Assert.assertEquals(zw.getHumanReadableName(), "Device 1");
    }

    @Test
    public void Z4_canChangeValuesAndSave() {
        ZWaveDevice zw = service.getDevices().iterator().next();

        zw.setState(State.DEAD);
        zw.setProductName("Changed by Z4");
        zw.getDeviceValues().get("Test val 1").setCurrentValue("new value!");

        zw = service.saveIntoDatabase(zw);

        Assert.assertEquals(zw.getDeviceValues().get("Test val 1").getCurrentValue(), "new value!");
        Assert.assertEquals(zw.getProductName(), "Changed by Z4");
    }
}