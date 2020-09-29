FROM navikt/java:11
RUN apt-get update && apt-get install -y curl
COPY scripts/init-env.sh /init-scripts/init-env.sh
COPY pam-import-api-migration/build/libs/pam-import-api-migration-*-all.jar ./pam-import-api-migration.jar
COPY build/libs/pam-import-api-*-all.jar ./app.jar
ENV JAVA_OPTS="-Xms256m -Xmx1536m"
