export ORACLE_DB_USER=$(cat /secrets/pamimportapi/oracle/credentials/username)
export ORACLE_DB_PASSWORD=$(cat /secrets/pamimportapi/oracle/credentials/password)
export ORACLE_DRIVER=oracle.jdbc.OracleDriver
export ORACLE_JDBC_URL=$(cat /secrets/pamimportapi/oracle/config/jdbc_url)
export SERVICEUSER=$(cat /secrets/pamimportapi/serviceuser/credentials/username)
export SERVICEUSER_PASSWORD=$(cat /secrets/pamimportapi/serviceuser/credentials/password)
export KAFKA_SSL_TRUSTSTORE_LOCATION=${NAV_TRUSTSTORE_PATH}
export KAFKA_SSL_TRUSTSTORE_PASSWORD=${NAV_TRUSTSTORE_PASSWORD}
export KAFKA_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=${SERVICEUSER} password=${SERVICEUSER_PASSWORD};"
