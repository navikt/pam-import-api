FROM navikt/java:11
COPY build/libs/pam-import-api-*-all.jar ./app.jar
