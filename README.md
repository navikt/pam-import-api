![build-deploy-dev](https://github.com/navikt/pam-import-api/workflows/build-deploy-dev/badge.svg)
![deploy-prod](https://github.com/navikt/pam-import-api/workflows/deploy-prod/badge.svg)

# Import api

## Build and run

```
./gradlew clean build
./gradlew run
```

## Run kafka, postgres with docker-compose
```
docker-compose up --build
```

### Kafka 
Running with kafka in tests, you need to add these system properties:
```
KAFKA_BOOTSTRAP_SERVERS=host1:port,host2:port
KAFKA_SSL_TRUSTSTORE_LOCATION=truststore
KAFKA_SSL_TRUSTSTORE_PASSWORD=truststorepassword
KAFKA_SASL_MECHANISME=PLAIN
KAFKA_SASL_JAAS_CONFIG='org.apache.kafka.common.security.plain.PlainLoginModule required username=kafka password=password;'
KAFKA_SECURITY_PROTOKOL=SASL_SSL
```

### Creating new provider in test

````
curl -k -XPOST -H "Accept: application/json" -H "Cache-Control: no-cache" -H "Content-Type: applic
ation/json" -d '{"identifier":"jobnorge-test","email":"test@jobnorge.no", "phone":"12345678"}' https://
pam-import-api.nais.oera-q.local/stillingsimport/internal/providers

````
