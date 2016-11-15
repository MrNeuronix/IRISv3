package ru.iris.zwave.protocol.model;

import org.zwave4j.ValueId;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import ru.iris.commons.protocol.DeviceValueChange;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

public class ZWaveDeviceValue extends AbstractDeviceValue {

	private ValueId valueId;
	private Deque<ZWaveDeviceValueChange> changes = new ConcurrentLinkedDeque<>();

	public ValueId getValueId() {
		return valueId;
	}

	public void setValueId(ValueId valueId) {
		this.valueId = valueId;
	}

	@Override
	public Deque<ZWaveDeviceValueChange> getChanges() {
		return changes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setChanges(Deque<? extends DeviceValueChange> changes) {
		this.changes = (Deque<ZWaveDeviceValueChange>) changes;
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
