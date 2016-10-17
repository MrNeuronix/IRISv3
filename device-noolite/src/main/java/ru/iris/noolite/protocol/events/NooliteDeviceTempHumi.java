package ru.iris.noolite.protocol.events;

import ru.iris.commons.bus.AbstractEvent;
import ru.iris.commons.protocol.enums.BatteryState;

public class NooliteDeviceTempHumi extends AbstractEvent {

	private byte channel;
	private double temp;
	private double humi;
	private BatteryState batteryState;

	public NooliteDeviceTempHumi(byte channel, double temp, double humi, BatteryState batteryState) {
		this.channel = channel;
		this.temp = temp;
		this.humi = humi;
		this.batteryState = batteryState;
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
	}

	public double getHumi() {
		return humi;
	}

	public void setHumi(double humi) {
		this.humi = humi;
	}

	public BatteryState getBatteryState() {
		return batteryState;
	}

	public void setBatteryState(BatteryState batteryState) {
		this.batteryState = batteryState;
	}

	@Override
	public String toString() {
		return "NooliteDeviceTempHumi{" +
				"channel=" + channel +
				", temp=" + temp +
				", humi=" + humi +
				", batteryState=" + batteryState +
				'}';
	}
}

