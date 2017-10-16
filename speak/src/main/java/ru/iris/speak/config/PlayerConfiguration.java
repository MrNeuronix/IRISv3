package ru.iris.speak.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.speak.player.AudioFilePlayer;
import ru.iris.speak.player.AudioPlayer;
import ru.iris.speak.player.ExternalAudioPlayer;
import ru.iris.speak.player.ExternalWithRemoteAudioPlayer;
import ru.iris.speak.player.LibraryAudioPlayer;

/**
 * @author nix (02.09.2017)
 */

@Configuration
@Slf4j
public class PlayerConfiguration {

	@Autowired
	private ConfigLoader config;

	@Bean
	public AudioPlayer audioPlayer(){

		if (!config.loadPropertiesFormCfgDirectory("speak"))
			logger.error("Cant load speak-specific configs. Check speak.property if exists");

		String type = config.get("playerType");

		if (!StringUtils.isEmpty(type)){
			if(type.equals("pure")) {
				logger.debug("Using pure audio player");
				return new AudioFilePlayer();
			}
			else if(type.equals("external")) {
				logger.debug("Using external audio player");
				return new ExternalAudioPlayer();
			}
			else if(type.equals("external-network")) {
				logger.debug("Using external network audio player");
				return new ExternalWithRemoteAudioPlayer();
			}
		}

		logger.debug("Using library audio player");
		return new LibraryAudioPlayer();
	}
}
