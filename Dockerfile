FROM eclipse-temurin:22_36-jdk
LABEL authors="Dmitry 'tehrelt' Evteev"

RUN apt-get update \
    && apt-get install -y curl
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && apt-get install -y libglib2.0-0\
        libnss3\
        libnspr4\
        libdbus-1-3\
        libatk1.0-0\
        libatk-bridge2.0-0\
        libcups2\
        libdrm2\
        libatspi2.0-0\
        libx11-6\
        libxcomposite1\
        libxdamage1\
        libxext6\
        libxfixes3\
        libxrandr2\
        libgbm1\
        libxcb1\
        libxkbcommon0\
        libpango-1.0-0\
        libcairo2\
        libasound2

COPY ./target/*.jar ./app.jar


ENTRYPOINT ["java", "-jar", "/app.jar"]