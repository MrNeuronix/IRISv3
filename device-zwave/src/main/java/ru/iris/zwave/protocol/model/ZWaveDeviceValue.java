package ru.iris.zwave.protocol.model;

import org.zwave4j.ValueId;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

public class ZWaveDeviceValue extends AbstractDeviceValue {

	private ValueId valueId;

	public ValueId getValueId() {
		return valueId;
	}

	public void setValueId(ValueId valueId) {
		this.valueId = valueId;
	}

	@Override
	public String toString() {
		return "ZWaveDeviceValue{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", value=" + value +
				'}';
	}

}
