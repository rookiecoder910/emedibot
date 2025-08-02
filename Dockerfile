# Use Debian-based OpenJDK 17 (not Alpine!)
FROM eclipse-temurin:17-jdk

# Install dependencies
RUN apt-get update && apt-get install -y wget unzip git

# Set Android SDK environment variable
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# Install Android SDK command-line tools
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    cd ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O tools.zip && \
    unzip tools.zip && \
    mv cmdline-tools latest && \
    rm tools.zip

# Accept licenses and install SDK components
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" "platforms;android-33" "build-tools;33.0.2"

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Build the APK
RUN ./gradlew assembleDebug
