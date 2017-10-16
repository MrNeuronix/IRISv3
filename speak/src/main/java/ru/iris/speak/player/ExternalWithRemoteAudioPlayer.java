package ru.iris.speak.player;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author nix (02.09.2017)
 */

@Component
@Slf4j
public class ExternalWithRemoteAudioPlayer implements AudioPlayer {

	@Override
	public void play(String filePath) {
		try {

			String[] cmd = {
					"/bin/sh",
					"-c",
					"export PULSE_SERVER=192.168.10.68 && /usr/bin/mplayer " + filePath
			};

			String line;
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader input =
					new BufferedReader
							(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.debug(line);
			}
			input.close();
		}
		catch (Exception err) {
			err.printStackTrace();
		}
	}
}
