FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

COPY api/ ./api/
COPY libs/ ./libs/
COPY robots/ ./robots/

WORKDIR /workspace/robots
RUN chmod +x ./gradlew && ./gradlew :app:installDist -x test --no-daemon

FROM eclipse-temurin:17-jre

ENV DEBIAN_FRONTEND=noninteractive
ENV DISPLAY=:0

RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    libfreetype6 \
    libxext6 \
    libxi6 \
    libxrender1 \
    libxtst6 \
    xauth \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/robots/app/build/install/app ./app/
COPY libs/ ./libs/

CMD ["./app/bin/app"]
