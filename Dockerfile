# === Stage 1: Build a minimal base with nb_NO.UTF-8 ===
FROM debian:12 AS locale-builder
RUN apt-get update && apt-get install -y locales && \
    echo "nb_NO.UTF-8 UTF-8" >> /etc/locale.gen && \
    locale-gen && \
    apt-get clean

# === Stage 2: Use the distroless base and copy the locale ===
FROM gcr.io/distroless/java21-debian12
LABEL maintainer="Team PAM"

# Copy generated locales from the builder stage
COPY --from=locale-builder /usr/lib/locale /usr/lib/locale
COPY --from=locale-builder /etc/default/locale /etc/default/locale
COPY --from=locale-builder /etc/locale.alias /etc/locale.alias

COPY build/libs/pam-import-api-*-all.jar ./app.jar
ENV JAVA_OPTS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx1536m"
ENV LANG='nb_NO.UTF-8'
ENV LANGUAGE='nb_NO:nb'
ENV LC_ALL='nb_NO.UTF-8'
ENV TZ="Europe/Oslo"
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.language=no -Duser.country=NO -Duser.timezone=Europe/Oslo"
EXPOSE 8080

CMD ["/app.jar"]
