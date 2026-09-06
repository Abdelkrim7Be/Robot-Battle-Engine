# Use Eclipse Temurin (recommended OpenJDK distribution) as base image
FROM eclipse-temurin:17-jdk

# Install required dependencies for GUI and Gradle
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    x11-apps \
    xauth \
    libxtst6 \
    libxi6 \
    libxrender1 \
    && rm -rf /var/lib/apt/lists/*

# Install Gradle 8.5 (compatible with Java 17)
RUN wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    rm /tmp/gradle.zip && \
    ln -s /opt/gradle-8.5/bin/gradle /usr/local/bin/gradle

# Set working directory
WORKDIR /app

# Copy project structure (build context should be parent directory)
# api and libs are siblings to robots/
COPY api/ ./api/
COPY libs/ ./libs/
COPY robots/ ./robots/

# Set environment variables for X11 forwarding (will be overridden by docker run)
ENV DISPLAY=:0

# Build the project (skip tests for faster Docker build)
WORKDIR /app/robots
RUN gradle :app:build -x test --no-daemon || gradle build -x test --no-daemon

# Default command to run the application
WORKDIR /app/robots
CMD ["gradle", ":app:run", "--no-daemon"]

