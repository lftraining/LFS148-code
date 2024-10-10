FROM docker.io/eclipse-temurin:21-jdk-alpine
RUN mkdir -p /opt/todoui
WORKDIR /opt/todoui
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar .
COPY target/todoui-0.0.1-SNAPSHOT.jar /opt/todoui
CMD ["java", "-javaagent:/opt/todoui/opentelemetry-javaagent.jar", "-jar", "todoui-0.0.1-SNAPSHOT.jar"]