package kr.esob.fdms.config;

import java.io.IOException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Property {
	private Properties configFile;

	public Property() {
		configFile = new Properties();

		try {
			configFile.load(this.getClass().getClassLoader().getResourceAsStream("docs.properties"));
		} catch (IOException e) {
			log.warn("Legacy property loading failed. cause={}", e.getClass().getSimpleName());
		}
	}

	public String getProperty(String key) {
		return this.configFile.getProperty(key);
	}
}
