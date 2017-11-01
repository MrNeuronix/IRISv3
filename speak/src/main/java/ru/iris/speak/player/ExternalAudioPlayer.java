package ru.iris.speak.player;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author nix (02.09.2017)
 */

@Component
@Slf4j
public class ExternalAudioPlayer implements AudioPlayer {

	@Override
	public void play(String filePath) {
		try {
			String line;
			Process p = Runtime.getRuntime().exec("/usr/bin/mplayer " + filePath);
			BufferedReader input =
					new BufferedReader
							(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.debug(line);
			}
			input.close();
		}
		catch (Exception err) {
            logger.error("", err);
        }
	}
}
