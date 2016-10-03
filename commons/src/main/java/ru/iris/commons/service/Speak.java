package ru.iris.commons.service;

import java.io.IOException;
import java.io.InputStream;

public interface Speak {

	void setLanguage(String language);
	InputStream getMP3Data(String text) throws IOException;
	void listen() throws Exception;
}
