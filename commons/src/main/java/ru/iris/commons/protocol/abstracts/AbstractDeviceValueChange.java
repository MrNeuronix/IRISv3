package ru.iris.commons.protocol.abstracts;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.iris.commons.protocol.DeviceValueChange;

import java.util.Date;

public abstract class AbstractDeviceValueChange implements DeviceValueChange {

	protected long id;

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date date;

	protected Object value;
	protected String additionalData;

	@Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String getAdditionalData() {
		return additionalData;
	}

	@Override
	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractDeviceValueChange)) return false;

		AbstractDeviceValueChange that = (AbstractDeviceValueChange) o;

		if (id != that.id) return false;
		if (date != null ? !date.equals(that.date) : that.date != null) return false;
		if (value != null ? !value.equals(that.value) : that.value != null) return false;
		return additionalData != null ? additionalData.equals(that.additionalData) : that.additionalData == null;
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (date != null ? date.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (additionalData != null ? additionalData.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "AbstractDeviceValueChange{" +
				"id=" + id +
				", date=" + date +
				", value=" + value +
				", additionalData='" + additionalData + '\'' +
				'}';
	}
}
