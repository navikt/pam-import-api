FROM navikt/java:11
COPY scripts/init-import-api.sh /init-scripts/init-import-api.sh
COPY build/libs/pam-import-api-*-all.jar ./app.jar