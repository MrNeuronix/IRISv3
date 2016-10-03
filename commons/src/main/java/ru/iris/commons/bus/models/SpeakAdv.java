package ru.iris.commons.bus.models;

import ru.iris.commons.bus.Advertisement;

import java.util.Objects;

public class SpeakAdv implements Advertisement {

	private String text;

	public SpeakAdv(String text) {
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
		SpeakAdv speakAdv = (SpeakAdv) o;
		return Objects.equals(text, speakAdv.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}
}
