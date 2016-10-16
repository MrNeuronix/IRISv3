package ru.iris.speak.model.bus;

import ru.iris.commons.bus.AbstractEvent;

import java.util.Objects;

public class SpeakEvent extends AbstractEvent {

	private String text;

	public SpeakEvent(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "SpeakAdv{" +
				"text='" + text + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SpeakEvent speakEvent = (SpeakEvent) o;
		return Objects.equals(text, speakEvent.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}
}
