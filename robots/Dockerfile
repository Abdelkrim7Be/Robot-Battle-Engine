# Use Eclipse Temurin (recommended OpenJDK distribution) as base image
FROM eclipse-temurin:17-jdk

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    libxtst6 \
    libxi6 \
    libxrender1 \
    xauth \
    x11-apps \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY api/ ./api/
COPY libs/ ./libs/
COPY robots/ ./robots/

ENV DISPLAY=:0

WORKDIR /app/robots
RUN chmod +x ./gradlew && ./gradlew :app:build -x test --no-daemon

CMD ["./gradlew", ":app:run", "--no-daemon"]
