FROM eclipse-temurin:17-jre-jammy

LABEL org.opencontainers.image.title="KT-1B DMS Demo"
LABEL org.opencontainers.image.description="Local Windows Server demo runtime for KT-1B DMS"

WORKDIR /opt/kt1b

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/kt1b/files /data/kt1b/tmp /var/log/kt1b

COPY app/TDMS-KT-1B.war /opt/kt1b/TDMS-KT-1B.war

ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.language=ko -Duser.country=KR -Duser.timezone=Asia/Seoul -Djava.io.tmpdir=/data/kt1b/tmp"

EXPOSE 3508

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/opt/kt1b/TDMS-KT-1B.war"]
