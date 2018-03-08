FROM openjdk:8-jre-alpine
MAINTAINER openvidu@gmail.com

# Copy Spring Boot app
COPY target/openvidu-recording-server-endpoint-1.0.0.jar /

EXPOSE 5443

CMD ["/usr/bin/java", "-jar", "openvidu-recording-server-endpoint-1.0.0.jar"]
