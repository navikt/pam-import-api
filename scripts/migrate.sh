#!/usr/bin/env sh
export DB_JDBC_URL="jdbc:postgresql://${DB_HOST}:5432/importapi"
java -jar pam-import-api-migration.jar > migration.log
