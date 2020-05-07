![build-deploy-dev](https://github.com/navikt/pam-import-api/workflows/build-deploy-dev/badge.svg)
![deploy-prod](https://github.com/navikt/pam-import-api/workflows/deploy-prod/badge.svg)

# Import api

## Build and run

```
./gradlew clean build
./gradlew run
```

### Using Oracle database

You can start pam-import-api with H2 as database, just set intellij run configuration to use test classpath. If you want
to use real oracle database, you can use oracle docker container https://github.com/oracle/docker-images/blob/master/OracleDatabase/SingleInstance/README.md:

make a local folder 'oracle' and run this command:

```
docker run --name oraclexe-18 -p 1521:1521 -p 5500:5500 -e ORACLE_PWD=system -v $(pwd)/oracle:/opt/oracle/oradata oracle/database:18.4.0-xe

```

connect to oracle with "system" password and create a user for pam-import-api:

```
CREATE USER C##IMPORTAPI IDENTIFIED BY importapi;
GRANT ALL PRIVILEGES TO C##IMPORTAPI;
```


Running with kafka in tests, you need to add these system properties:
```
KAFKA_BOOTSTRAP_SERVERS=host1:port,host2:port
KAFKA_SSL_TRUSTSTORE_LOCATION=truststore
KAFKA_SSL_TRUSTSTORE_PASSWORD=truststorepassword
KAFKA_SASL_MECHANISME=PLAIN
KAFKA_SASL_JAAS_CONFIG='org.apache.kafka.common.security.plain.PlainLoginModule required username=kafka password=password;'
KAFKA_SECURITY_PROTOKOL=SASL_SSL
```


