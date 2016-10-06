package ru.iris;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.iris.commons.bus.config.ReactorConfig;
import ru.iris.commons.config.TestJpaConfig;
import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.DeviceValueImpl;
import ru.iris.zwave.protocol.service.ZWaveProtoService;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.Type;
import ru.iris.zwave.protocol.ZWaveDevice;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("tests")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ZWaveTests {

	@Autowired
	private ZWaveProtoService service;

	ZWaveDevice device;
	Set<DeviceValue> values = new HashSet<>();

	@Before
	public void setUp() {

		DeviceValueImpl val1 = new DeviceValueImpl();
		val1.setName("Test val 1");
		val1.setValue(2.0D);
		values.add(val1);

		DeviceValueImpl val2 = new DeviceValueImpl();
		val2.setName("Test val 2");
		val2.setValue("All ok!");
		val2.setDate(new Date());
		values.add(val2);

		device = new ZWaveDevice();
		device.setHumanReadable("Device 1");
		device.setDate(new Date());
		device.setInternalName("zw/1");
		device.setType(Type.BINARY_SWITCH);
		device.setSource(SourceProtocol.ZWAVE);
		device.setManufacturer("Test manufact");
		device.setProductName("Test prod name");
		device.setState(State.ACTIVE);
		device.setValues(values);
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
		ZWaveDevice zw = service.getZWaveDevices().stream().findAny().get();
		Assert.assertEquals(zw.getHumanReadableName(), "Device 1");
	}

	@Test
	public void Z4_canChangeValuesAndSave() {
		ZWaveDevice zw = service.getZWaveDevices().stream().findAny().get();

		zw.setState(State.DEAD);
		zw.getDeviceValues().iterator().next().setValue("new value!");

		zw = service.saveIntoDatabase(zw);

		Assert.assertEquals(zw.getDeviceValues().iterator().next().getValue(), "new value!");
	}
}