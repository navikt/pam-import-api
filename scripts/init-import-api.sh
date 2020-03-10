export ORACLE_DB_USER=$(cat /secrets/pamimportapi/oracle/credentials/username)
export ORACLE_DB_PASSWORD=$(cat /secrets/pamimportapi/oracle/credentials/password)
export ORACLE_DRIVER=oracle.jdbc.OracleDriver
export ORACLE_JDBC_URL=$(cat /secrets/pamimportapi/oracle/config/jdbc_url)