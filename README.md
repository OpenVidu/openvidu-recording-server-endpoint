# openvidu-recording-server-endpoint

This is a Spring Boot application aimed to offer an HTTP endpoint for uploading/dowloading video recordings from browsers. It is meant to be used alongside [openvidu-browser](https://www.npmjs.com/package/openvidu-browser) library and its **LocalRecorder** API.

## Demo

Provided as a [Docker](https://store.docker.com/search?type=edition&offering=community) image:

```
docker run -p 5443:5443 openvidu/openvidu-recording-server-endpoint
```

<p align="center"><img src="https://github.com/OpenVidu/openvidu-recording-server-endpoint/blob/master/readme-images/demo.gif?raw=true"/></p>

## Docs

- **Frontend**: LocalRecorder object in *openvidu-browser-1.8.0.js* is used to record a media stream and to upload it to certain endpoint.
- **Backend**: A Java Spring Boot app exposing 3 HTTP REST endpoints:
  - `POST /recording`: receives a recording as a MultiPart file and stores it locally.
  - `GET /recording/{recordingName}`: serves one recording with certain name.
  - `GET /recording/all`: returns an array with all the recording names for which the user has permissions.
