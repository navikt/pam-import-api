# Arbeidsplassen Job Import API

## Introduction
With our import API you will be able to upload and publish jobs to [arbeidsplassen.nav.no](https://arbeidsplassen.nav.no), 
a public job vacancy service provided by NAV.

## Registration
Before you begin, you must register yourself as a job provider. Please send your registration to this email with the 
following information:

* Provider/Company name
* Contact email
* Contact Phone

We will send you your provider identity including a key that gives you access to our API

## Authentication/Authorization
The API uses HTTP bearer authorization header for authentication and authorization

```
POST http://tjenester-q0.nav.no/stillingsimport/api/v1/transfers/1
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json
Authorization: Bearer <your secret key token>
```
## REST API

### Job transfers 
#### JSON
#### Batch upload

### Receipt/Status
## Suggestions/Questions