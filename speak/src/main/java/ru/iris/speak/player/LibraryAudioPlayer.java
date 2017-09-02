package ru.iris.speak.player;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nix (02.09.2017)
 */

@Component
@Slf4j
public class LibraryAudioPlayer implements AudioPlayer {

	@Override
	public void play(String filePath) {
		InputStream result = null;
		try {
			result = new FileInputStream(filePath);
			Player player = new Player(result);
			player.play();
			player.close();
		} catch (FileNotFoundException | JavaLayerException e) {
			logger.error("Error while trying to play {}: {}", filePath, e.getMessage());
		} finally {
			try {
				if (result != null)
					result.close();
			} catch (IOException e) {
				logger.error("Error: ", e.getLocalizedMessage());
			}
		}
	}
}
