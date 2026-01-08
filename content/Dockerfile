FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN rm -f /etc/apt/sources.list.d/*.list && \
    echo "deb http://ports.ubuntu.com/ubuntu-ports jammy main restricted universe multiverse" > /etc/apt/sources.list && \
    echo "deb http://ports.ubuntu.com/ubuntu-ports jammy-updates main restricted universe multiverse" >> /etc/apt/sources.list && \
    echo "deb http://ports.ubuntu.com/ubuntu-ports jammy-security main restricted universe multiverse" >> /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        ffmpeg \
        curl \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

