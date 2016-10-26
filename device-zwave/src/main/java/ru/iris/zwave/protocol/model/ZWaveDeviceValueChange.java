package ru.iris.zwave.protocol.model;

import org.zwave4j.ValueId;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValueChange;

public class ZWaveDeviceValueChange extends AbstractDeviceValueChange {

	public ValueId valueId;

	public ValueId getValueId() {
		return valueId;
	}

	public void setValueId(ValueId valueId) {
		this.valueId = valueId;
	}
}
