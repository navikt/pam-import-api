![build-deploy-dev](https://github.com/navikt/pam-import-api/workflows/build-deploy-dev/badge.svg)
![deploy-prod](https://github.com/navikt/pam-import-api/workflows/deploy-prod/badge.svg)

pam-import-api
===

Application that lets job providers upload and publish job ads to [arbeidsplassen.nav.no](https://arbeidsplassen.nav.no/).

# Technical documentation

## Technologies

* Kotlin
* Micronaut
* Postgres
* Kafka

## Environment

The image below shows a simplified sketch of pam-import-api and its internal integrations.

![Technical sketch)](images/technical-sketch.png)

### REST API

Providers can upload, publish and retrieve information about job ads through a REST API.

Information about the API is provided in the [API documentation](https://navikt.github.io/pam-import-api/).

### Frontend

A light-weight React frontend is embedded in the app. Providers can use the frontend to preview their job ads before publishing them.

### Postgres DB

The app uses a Postgres database to store different information about providers and ads.

### Integration with pam-ad

[navikt/pam-ad](https://github.com/navikt/pam-ad) consumes ads published through pam-import-api as a JSON feed.
pam-ad
stores the ad master data. (**Note:** The JSON feed will be replaced by Kafka in the future. pam-import-api is
prepared to send ad information to the Kafka topic `adstate`, but consumption is not yet implemented in pam-ad.)

pam-ad sends information about ad changes to the Kafka topic `stilling-intern` that pam-import-api listens to.

### Integration with pam-puls

[navikt/pam-puls](https://github.com/navikt/pam-puls) stores information about ad statistics and sends the
information to the Kafka topic `puls` that pam-import-api listens to.

# Getting started

## Build and run

```
./gradlew clean build
./gradlew run
```

## Run Kafka and Postgres with docker-compose

```
docker-compose up --build
```

### Kafka

Running with Kafka in tests, you need to add these system properties:

```
KAFKA_BOOTSTRAP_SERVERS=host1:port,host2:port
KAFKA_SSL_TRUSTSTORE_LOCATION=truststore
KAFKA_SSL_TRUSTSTORE_PASSWORD=truststorepassword
KAFKA_SASL_MECHANISME=PLAIN
KAFKA_SASL_JAAS_CONFIG='org.apache.kafka.common.security.plain.PlainLoginModule required username=kafka password=password;'
KAFKA_SECURITY_PROTOKOL=SASL_SSL
```

### Creating new provider in test

```
curl -k -XPOST -H "Accept: application/json" -H "Cache-Control: no-cache" -H "Content-Type: application/json" -d '{"identifier":"jobnorge-test","email":"test@jobnorge.no", "phone":"12345678"}' https://pam-import-api.nais.oera-q.local/stillingsimport/internal/providers
```

# Registering new providers

Providers must authenticate themselves with a `providerId` and `token` when using the API. These are generated manually with scripts in this app ([registerProvider.sh]() and [provider-token.sh]()).

## Prerequisites

Providers must register themselves as a job provider/partner and be approved by sending an email to us with the following information:
* Company name
* Contact email (technical support or personell)
* Contact phone number

## Using the registration scripts

Step-by-step guide:

1. Set environment variables
    1. The scripts require environment variables `PATH_PROD_KEY` and `PATH_DEV_KEY`
    2. These variables must point to files with keys for prod and dev
    3. The keys can be found in Google Secret Manager

```bash
export PATH_PROD_KEY=<path_prod_key>
export PATH_DEV_KEY=<path_dev_key>
```

2. Run `registerProvider.sh`
    1. Provide `identifier`, `email` and `phone` when prompted for these
    2. Verify that provided info is correct (`Y`/`y` to approve)
    3. Note `id` from the output (this is `providerId` needed by the provider for authentication)

```bash
bash registerProvider.sh

Register new IMPORT-API provider, please type in correct information
identifier (brukt som medium):<identifier>
email:<email>
phone:<phone>
this will create a new provider on both test and production using the json file:
{ "identifier": "<identifier>", "email": "<email>", "phone": "<phone>" } 

Are you sure? <Y>

# note id in response
```

3. Run `provider-token.sh`
    1. Provide `providerId` when prompted for this (`id` from the previous step)
    2. Verify that provided info is correct (`Y`/`y` to approve)
    3. Note `token` for dev and prod from the output (these are the `tokens` needed by the provider for authentication)

```bash
bash provider-token.sh

Create tokens for provider
provider-id:<providerId>
this will generate tokens for provider <providerId>

Are you sure? <Y>

# note token for dev and prod
```

# Deploy to prod

Before deploying to production and if the API changes, remember to send information about it to all providers.

# Inquiries

Questions regarding the code or project can be asked as GitHub issues.


