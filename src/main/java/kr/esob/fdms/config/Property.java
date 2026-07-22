package kr.esob.fdms.config;

import java.io.IOException;
import java.util.Properties;

public class Property {
	private Properties configFile;

	public Property() {
		configFile = new Properties();

		try {
			configFile.load(this.getClass().getClassLoader().getResourceAsStream("docs.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getProperty(String key) {
		return this.configFile.getProperty(key);
	}
}
