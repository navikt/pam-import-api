#!/usr/bin/env sh
export DB_JDBC_URL="jdbc:postgresql://${DB_HOST}:5432/pamimportapi"
java -jar pam-import-api-migration.jar migrate > migration.log
