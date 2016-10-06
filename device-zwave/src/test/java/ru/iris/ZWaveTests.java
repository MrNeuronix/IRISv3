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
import ru.iris.commons.protocol.DeviceValue;
import ru.iris.zwave.protocol.model.ZWaveDeviceValue;
import ru.iris.zwave.protocol.service.ZWaveProtoService;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.Type;
import ru.iris.zwave.protocol.model.ZWaveDevice;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("tests")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ZWaveTests {

	@Autowired
	private ZWaveProtoService service;

	ZWaveDevice device;
	Map<String, DeviceValue> values = new HashMap<>();

	@Before
	public void setUp() {

		ZWaveDeviceValue val1 = new ZWaveDeviceValue();
		val1.setName("Test val 1");
		val1.setValue(2.0D);
		values.put("Test val 1", val1);

		ZWaveDeviceValue val2 = new ZWaveDeviceValue();
		val2.setName("Test val 2");
		val2.setValue("All ok!");
		val2.setDate(new Date());
		values.put("Test val 2", val2);

		device = new ZWaveDevice();
		device.setHumanReadable("Device 1");
		device.setDate(new Date());
		device.setInternalName("zw/1");
		device.setType(Type.BINARY_SWITCH);
		device.setSource(SourceProtocol.ZWAVE);
		device.setManufacturer("Test manufact");
		device.setProductName("Test prod name");
		device.setState(State.ACTIVE);
		device.setDeviceValues(values);
	}

	@Test
	public void Z1_canSaveByService() {
		ZWaveDevice zw = service.saveIntoDatabase(device);
		Assert.assertEquals(zw.getHumanReadableName(), "Device 1");
	}

	@Test
	public void Z2_canFetchAndConvertByService() {
		Assert.assertEquals(service.getZWaveDevices().size(), 1);
	}

	@Test
	public void Z3_correctHumanReadableName() {
		ZWaveDevice zw = service.getZWaveDevices().iterator().next();
		Assert.assertEquals(zw.getHumanReadableName(), "Device 1");
	}

	@Test
	public void Z4_canChangeValuesAndSave() {
		ZWaveDevice zw = service.getZWaveDevices().iterator().next();

		zw.setState(State.DEAD);
		zw.setProductName("Changed by Z4");
		zw.getDeviceValues().get("Test val 1").setValue("new value!");

		zw = service.saveIntoDatabase(zw);

		Assert.assertEquals(zw.getDeviceValues().get("Test val 1").getValue(), "new value!");
	}
}