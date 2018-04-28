package ru.iris;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.models.database.Device;
import ru.iris.models.database.DeviceValue;
import ru.iris.models.protocol.enums.DeviceType;
import ru.iris.models.protocol.enums.SourceProtocol;
import ru.iris.models.protocol.enums.State;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("tests")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ZWaveTests {

    private Device device, device2;
    private Map<String, DeviceValue> values = new HashMap<>();

    @Autowired
    private DeviceRegistry registry;

    @Before
    public void setUp() {

        DeviceValue val1 = new DeviceValue();
        val1.setName("Test val 1");
        val1.setCurrentValue("2.0");
        values.put("Test val 1", val1);

        DeviceValue val2 = new DeviceValue();
        val2.setName("Test val 2");
        val2.setCurrentValue("All ok!");
        val2.setDate(new Date());
        values.put("Test val 2", val2);

        // zwave device 1
        device = new Device();
        device.setHumanReadable("Device 1");
        device.setDate(new Date());
        device.setChannel("1");
        device.setType(DeviceType.BINARY_SWITCH);
        device.setSource(SourceProtocol.ZWAVE);
        device.setManufacturer("Test manufact");
        device.setProductName("Test prod name");
        device.setState(State.ACTIVE);
        device.setValues(values);

        // zwave device 2
        device2 = new Device();
        device2.setHumanReadable("Device 2");
        device2.setDate(new Date());
        device2.setChannel("2");
        device2.setType(DeviceType.TEMP_HUMI_SENSOR);
        device2.setSource(SourceProtocol.UNKNOWN);
        device2.setManufacturer("Test manufact 2");
        device2.setProductName("Test prod name 2");
        device2.setState(State.DEAD);
    }

    @Test
    public void Z1_canSaveByService() {
        Device zw = registry.addOrUpdateDevice(device);
        registry.addOrUpdateDevice(device2);
        Assert.assertEquals(zw.getHumanReadable(), "Device 1");
    }

    @Test
    public void Z2_canFetchAndConvertByService() {
        Assert.assertEquals(registry.getDevicesByProto(SourceProtocol.ZWAVE).size(), 1);
    }

    @Test
    public void Z3_correctHumanReadableName() {
        Device zw = registry.getDevicesByProto(SourceProtocol.ZWAVE).iterator().next();
        Assert.assertEquals(zw.getHumanReadable(), "Device 1");
    }

    @Test
    public void Z4_canChangeValuesAndSave() {
        Device zw = registry.getDevicesByProto(SourceProtocol.ZWAVE).iterator().next();

        zw.setState(State.DEAD);
        zw.setProductName("Changed by Z4");
        zw.getValues().get("Test val 1").setCurrentValue("new value!");

        zw = registry.addOrUpdateDevice(zw);

        Assert.assertEquals(zw.getValues().get("Test val 1").getCurrentValue(), "new value!");
        Assert.assertEquals(zw.getProductName(), "Changed by Z4");
    }
}