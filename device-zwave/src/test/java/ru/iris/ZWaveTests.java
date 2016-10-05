package ru.iris;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.iris.commons.database.dao.DeviceDAO;
import ru.iris.commons.protocol.DeviceValue;
import ru.iris.commons.protocol.DeviceValueImpl;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;
import ru.iris.commons.protocol.enums.Type;
import ru.iris.zwave.protocol.ZWaveDevice;
import ru.iris.zwave.protocol.service.ZWaveDeviceService;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ZWaveTests {

	ZWaveDeviceService service;

	ZWaveDevice device;
	Set<DeviceValue> values = new HashSet<>();

	@Before
	public void setUp() {

		service = new ZWaveDeviceService();

		DeviceValueImpl val1 = new DeviceValueImpl();
		val1.setName("Test val 1");
		val1.setValue(2.0D);
		values.add(val1);

		DeviceValueImpl val2 = new DeviceValueImpl();
		val2.setName("Test val 2");
		val2.setValue("All ok!");
		val2.setId(5L);
		val2.setDate(new Date());
		values.add(val2);

		device = new ZWaveDevice();
		device.setId(100L);
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
	public void canSaveByService() {
		service.saveIntoDatabase(device);
	}

	@Test
	public void canFetchAndConvertByService() {
		Assert.assertEquals(service.getZWaveDevices().size(), 1);
	}
}