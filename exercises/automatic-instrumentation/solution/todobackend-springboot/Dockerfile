FROM docker.io/maven:3-eclipse-temurin-21 AS build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2 mvn install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM docker.io/eclipse-temurin:21-jdk-alpine
RUN mkdir -p /opt/todobackend
WORKDIR /opt/todobackend
#RUN addgroup -S demo && adduser -S demo -G demo
#USER demo
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /opt/todobackend/app/lib
COPY --from=build ${DEPENDENCY}/META-INF /opt/todobackend/app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /opt/todobackend/app

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /opt/todobackend

ENTRYPOINT ["java", "-cp", "/opt/todobackend/app:/opt/todobackend/app/lib/*",  "-javaagent:/opt/todobackend/opentelemetry-javaagent.jar","io.novatec.todobackend.TodobackendApplication"]