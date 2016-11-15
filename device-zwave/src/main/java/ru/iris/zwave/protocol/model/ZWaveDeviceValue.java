package ru.iris.zwave.protocol.model;

import org.zwave4j.ValueId;

import ru.iris.commons.LIFO;
import ru.iris.commons.protocol.DeviceValueChange;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

public class ZWaveDeviceValue extends AbstractDeviceValue {

	private ValueId valueId;
	private LIFO<ZWaveDeviceValueChange> changes = new LIFO<>(5);

	public ValueId getValueId() {
		return valueId;
	}

	public void setValueId(ValueId valueId) {
		this.valueId = valueId;
	}

	@Override
	public LIFO<ZWaveDeviceValueChange> getChanges() {
		return changes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setChanges(LIFO<? extends DeviceValueChange> changes) {
		this.changes = (LIFO<ZWaveDeviceValueChange>) changes;
	}

	@Override
	public String toString() {
		return "ZWaveDeviceValue{" +
				"id=" + id +
				", date=" + date +
				", name='" + name + '\'' +
				", value=" + currentValue +
				", units='" + units + '\'' +
				", readOnly=" + readOnly +
				", type=" + type +
				", valueId='" + valueId + '\'' +
				", currentValue=" + currentValue +
				'}';
	}
}
