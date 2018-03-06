package io.openvidu.recording.server.endpoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/recording")
public class RecordingsHttpHandler {

	private static final Logger log = LoggerFactory.getLogger(RecordingsHttpHandler.class);

	@Autowired
	PropertiesConfig config;

	@RequestMapping(value = "{filename:.+}", method = RequestMethod.GET)
	public void handleGetRecording(@PathVariable String filename, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		log.info("GET /recording{}", filename);

		MultipartFileSender.fromPath(new File(config.getRecordingsPath(), filename).toPath()).with(request)
				.with(response).serveResource();
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> handlePostRecording(@RequestParam("file") MultipartFile file) throws IOException {

		log.info("POST /recording{}", file.getOriginalFilename());

		if (file.isEmpty()) {
			log.error("File is empty");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Path path = Paths.get(config.getRecordingsPath());
		File uploadedFile = new File(path.toFile(), file.getOriginalFilename());
		InputStream initialStream = file.getInputStream();
		Files.copy(initialStream, uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		IOUtils.closeQuietly(initialStream);

		log.info("File succesfully uploaded to path '{}'", uploadedFile.getPath());
		
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
