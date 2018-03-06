package io.openvidu.recording.server.endpoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesConfig {

	@Value("${server.port}")
	private String serverPort;
	
	@Value("${http-basic.user}")
	private String user;

	@Value("${http-basic.password}")
	private String password;
	
	@Value("${recordings.path}")
	private String recordingsPath;

	public String getServerPort() {
		return serverPort;
	}

	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}

	public String getRecordingsPath() {
		return recordingsPath;
	}

}
