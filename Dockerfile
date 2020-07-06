FROM navikt/java:11

RUN apt-get update && apt-get install -y curl

COPY scripts/init-pam-import-env.sh /init-scripts/init-pam-import-env.sh
COPY build/libs/pam-import-api-*-all.jar ./app.jar
ENV JAVA_OPTS="-Xms256m -Xmx1536m -Doracle.jdbc.autoCommitSpecCompliant=false"
