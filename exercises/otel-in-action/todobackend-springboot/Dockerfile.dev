FROM docker.io/eclipse-temurin:21-jdk-alpine
RUN mkdir -p /opt/todobackend
WORKDIR /opt/todobackend
ADD --chmod=555 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar .
COPY  target/todobackend-0.0.1-SNAPSHOT.jar /opt/todobackend
CMD ["java", "-javaagent:/opt/todobackend/opentelemetry-javaagent.jar", "-jar", "todobackend-0.0.1-SNAPSHOT.jar"]
