#!/usr/bin/env sh
if test -r "${NAV_TRUSTSTORE_PATH}";then
  export KAFKA_SSL_TRUSTSTORE_LOCATION=${NAV_TRUSTSTORE_PATH}
  export KAFKA_SSL_TRUSTSTORE_PASSWORD=${NAV_TRUSTSTORE_PASSWORD}
  export KAFKA_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${SERVICEUSER}\" password=\"${SERVICEUSER_PASSWORD}\";"
  export KAFKA_SASL_MECHANISM=PLAIN
  export KAFKA_SECURITY_PROTOCOL=SASL_SSL
fi
export DB_JDBC_URL="jdbc:postgresql://${DB_HOST}:5432/importapi"
