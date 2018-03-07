package io.openvidu.recording.server.endpoint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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
	public ResponseEntity<HttpStatus> handleGetRecording(@PathVariable String filename, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		log.info("GET /recording/{}", filename);

		File file = new File(config.getRecordingsPath() + "/user", filename);
		if (!file.isFile()) {
			file = new File(config.getRecordingsPath() + "/admin", filename);
			if (file.isFile() && !request.isUserInRole("ROLE_ADMIN")) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		}

		if (file.isFile()) {
			MultipartFileSender.fromPath(file.toPath()).with(request).with(response).serveResource();
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "all", method = RequestMethod.GET)
	public ResponseEntity<List<String>> handleGetRecordings(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		List<String> results = new ArrayList<String>();

		File[] filesUser = new File(config.getRecordingsPath() + "/user").listFiles();
		for (File file : filesUser) {
		    if (file.isFile()) {
		        results.add(file.getName());
		    }
		}
		
		if (request.isUserInRole("ROLE_ADMIN")) {
			File[] filesAdmin = new File(config.getRecordingsPath() + "/admin").listFiles();
			for (File file : filesAdmin) {
			    if (file.isFile()) {
			        results.add(file.getName());
			    }
			}
		}
		
		return new ResponseEntity<>(results, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<HttpStatus> handlePostRecording(HttpServletRequest request,
			@RequestParam("file") MultipartFile file) throws IOException {

		log.info("POST /recording");

		if (file.isEmpty()) {
			log.error("File is empty");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		String user = request.isUserInRole("ROLE_ADMIN") ? "admin" : "user";
		String folder = "/" + user;
		String fileName = user + "_" + file.getOriginalFilename();

		Path path = Paths.get(config.getRecordingsPath() + folder);
		String fName = findFileName(config.getRecordingsPath() + folder, getFileName(fileName),
				getFileExtension(fileName));

		File uploadedFile = new File(path.toFile(), fName);
		InputStream initialStream = file.getInputStream();
		Files.copy(initialStream, uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		IOUtils.closeQuietly(initialStream);

		log.info("File succesfully uploaded to path '{}'", uploadedFile.getPath());

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private String findFileName(final String dir, final String baseName, final String extension) {
		String name = String.format("%s.%s", baseName, extension);
		Path ret = Paths.get(dir, name);
		if (!Files.exists(ret))
			return name;

		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			name = String.format("%s%d.%s", baseName, i, extension);
			ret = Paths.get(dir, name);
			if (!Files.exists(ret))
				return name;
		}
		throw new IllegalStateException("Fail finding file name");
	}

	private String getFileName(String file) {
		String extension = "";
		int i = file.lastIndexOf('.');
		if (i > 0) {
			extension = file.substring(0, i);
		}
		return extension;
	}

	private String getFileExtension(String file) {
		String extension = "";
		int i = file.lastIndexOf('.');
		if (i > 0) {
			extension = file.substring(i + 1);
		}
		return extension;
	}

}
