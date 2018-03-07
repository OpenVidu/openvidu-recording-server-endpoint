package io.openvidu.recording.server.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesConfig {

	@Value("${server.port}")
	private String serverPort;
	
	@Value("${recordings.path}")
	private String recordingsPath;

	public String getServerPort() {
		return serverPort;
	}

	public String getUserLowPermissions() {
		return "user";
	}
	
	public String getPasswordLowPermissions() {
		return "pass";
	}
	
	public String getUserHighPermissions() {
		return "admin";
	}
	
	public String getPasswordHighPermissions() {
		return "admin";
	}

	public String getRecordingsPath() {
		return recordingsPath;
	}

}
