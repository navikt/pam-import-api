# Job Import API

## Introduction
With our job import API you will be able to upload and publish jobs to [arbeidsplassen.nav.no](https://arbeidsplassen.nav.no). 
Arbeidsplassen is a public job vacancy service provided by NAV, a place where you can search for available jobs in Norway. 
This API is designed as a lightweight REST API supporting requests with JSON over HTTP.

## Registration
Before you begin, you must register yourself as a job provider/partner. Please send your registration to this email with the 
following information:

* Provider/Company name
* Contact email
* Contact Phone

We will send you your provider identity including a secret key that gives you access to our API

## Authentication/Authorization
The API is not publicly open, all requests need to be authenticated using 
the HTTP bearer authorization header. 

Example:
```
POST https://tjenester-q0.nav.no/stillingsimport/api/v1/transfers/{providerId}
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json
Authorization: Bearer <your secret key>
```

## Posting a job ad

To upload an ad, use HTTP POST as follow

```

```
All ads will then be identified by a combination of the providerId and its reference id.

 

#### JSON Structure

#### Properties

#### Batch upload

### Receipt/Status

## Suggestions/Questions