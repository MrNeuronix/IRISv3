package ru.iris.zwave.protocol.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.zwave4j.ValueId;
import ru.iris.commons.protocol.abstracts.AbstractDeviceValue;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"changes"})
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
				", value=" + currentValue +
				", units='" + units + '\'' +
				", readOnly=" + readOnly +
				", type=" + type +
				", valueId='" + valueId + '\'' +
				", currentValue=" + currentValue +
				'}';
	}
}
