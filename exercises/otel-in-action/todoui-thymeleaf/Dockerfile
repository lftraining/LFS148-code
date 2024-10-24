FROM docker.io/maven:3-eclipse-temurin-21 AS build
WORKDIR /workspace/app

COPY pom.xml .
COPY src src

RUN --mount=type=cache,target=/root/.m2 mvn install -DskipTests
# RUN mvn install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM docker.io/eclipse-temurin:21-jdk-alpine
RUN mkdir -p /opt/todoui
WORKDIR /opt/todoui
# RUN addgroup -S demo && adduser -S demo -G demo
# USER demo
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /opt/todoui/app/lib
COPY --from=build ${DEPENDENCY}/META-INF /opt/todoui/app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /opt/todoui/app

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /opt/todoui

ENTRYPOINT ["java","-cp","/opt/todoui/app:/opt/todoui/app/lib/*", "-javaagent:/opt/todoui/opentelemetry-javaagent.jar", "io.novatec.todoui.TodouiApplication"]
#ENTRYPOINT ["java", "-cp", "/opt/todobackend/app:/opt/todobackend/app/lib/*",  "-javaagent:/opt/todobackend/opentelemetry-javaagent.jar","io.novatec.todobackend.TodobackendApplication"]

