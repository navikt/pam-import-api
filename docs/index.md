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
POST https://tjenester-q0.nav.no/stillingsimport/api/v1/transfers/{providerId}
Accept: application/x-json-stream
Cache-Control: no-cache
Content-Type: application/x-json-stream
Authorization: Bearer <your secret key>

{
  "reference": "140095810",
  "positions": 1,
  "contactList": [
    {
      "name": "Ola Norman",
      "title": "Regionleder",
      "email": "ola.normann@test.com",
      "phone": "+47 001 00 002"
    },
    {
      "name": "Kari Normann",
      "title": "Prosjektleder",
      "email": "kari.normann@test.com",
      "phone": "+47 003 00 004"
    }
  ],
  "locationList": [
    {
      "address": "Magnus Sørlis veg",
      "postalCode": "1920",
      "country": "NORGE",
      "county": "VIKEN",
      "municipal": "LILLESTRØM",
      "city": "SØRUMSAND"
    }
  ],
  "properties": {
    "extent": "Heltid",
    "employerhomepage": "http://www.sorumsand.norlandiabarnehagene.no",
    "applicationdue": "24.02.2019",
    "keywords": "Barnehage,daglig,leder,styrer",
    "engagementtype": "Fast",
    "employerdescription": "<p>I Norlandia barnehagene vil vi være med å skape livslang lyst til lek og læring. Hos oss er barnets beste alltid i sentrum. Våre medarbeidere er Norlandias viktigste innsatsfaktor, og lederskap vårt viktigste suksesskriterium. Våre ledere er sterke og selvstendige med ansvar for å utvikle lederteam i barnehagene, og for å bidra aktivt inn i Ledergruppen i Regionen. Norlandia Sørumsand vil være tilknyttet Region Øst.</p>\n",
    "starttime": "01.05.2019",
    "applicationemail": "ola.normann@test.com",
    "applicationurl": "https://url.to.applicationform/recruitment/hire/input.action?adId=140095810",
    "sector": "Privat",
    "applicationlabel": "Søknad Sørumsand"
  },
  "title": "Ønsker du å lede en moderne og veletablert barnehage?",
  "adText": "<p>Nå har du en unik mulighet til å lede en godt faglig og veletablert barnehage. Norlandia Sørumsand barnehage ble etablert i 2006 og har moderne og fleksible oppholdsarealer. Barnehagens satsningsområder er Mat med Smak og Null mobbing i barnehagen.</p>\n<p><strong>Hovedansvarsområder:</strong></p>\n<ul><li>Drifte og utvikle egen barnehage i tråd med gjeldende forskrifter, bestemmelser og Norlandias overordnete strategi</li><li>Personalansvar</li><li>Overordnet faglig ansvar i egen barnehage</li><li>Bidra og medvirke i regionens endrings -og strategiprosesser</li><li>Kvalitet i barnehagen i henhold til konsernets kvalitets- og miljøpolicy</li></ul>\n<p><strong>Ønskede kvalifikasjoner:</strong></p>\n<ul><li>Barnehagelærerutdanning</li><li>Gode lederegenskaper</li><li>Engasjement for mat og miljø</li><li>Økonomiforståelse</li><li>Beslutningsdyktig, proaktiv og løsningsorientert</li><li>Effektiv og evnen til å håndtere flere oppgaver samtidig</li><li>Være motivator og støttespiller for medarbeiderne</li><li>Ha gode strategiske evner</li></ul>\n<p><strong>Vi tilbyr:</strong></p>\n<ul><li>Jobb i et sterkt fagmiljø i stadig utvikling, med et stort handlingsrom innenfor fastsatte rammer</li><li>Korte beslutningsveier og muligheter for personlig og faglig utvikling</li><li>Gode personalfasiliteter</li><li>Konkurransedyktig lønn og gode pensjonsbetingelser</li><li>Gyldig politiattest (ikke eldre enn 3 måneder ved tiltredelse) må fremvises før ansettelse</li></ul>\n<p><em><strong>Dette er en unik mulighet til å få lede en moderne veletablert barnehage.</strong></em></p>\n",
  "privacy": "SHOW_ALL",
  "published": "2019-02-13T12:59:26",
  "expires": "2019-02-24T00:00:00",
  "employer": {
    "id": 255533,
    "reference": "232151232",
    "businessName": "Sørumsand barnehage",
    "orgnr": "989012088",
    "location": {
      "address": "Sannergata 2",
      "postalCode": "0566",
      "country": "Norge",
      "county": "Oslo",
      "municipal": "Oslo",
      "city": "Oslo"
    }
  },
  "categoryList": [
    {
      "code": "234204",
      "categoryType": "STYRK08",
      "name": "Barnehagelærer"
    }
  ]
}
```

If the request was successful you will get a response with a receipt:

```
{
  "versionId" : 1,
  "provider" : {
    "id" : 10000,
    "jwtid" : "test-key-id-1",
    "identifier" : "tester",
    "email" : "test@test.no",
    "phone" : "12345678"
  },
  "status" : "RECEIVED",
  "md5" : "3D5A0C23BC12D58D5865CF3CFC086F11",
  "items" : 1,
  "created" : "2020-04-02T09:47:58.52183",
  "updated" : "2020-04-02T09:47:58.521843"
}
```

#### JSON Structure

Payloads are Json Objects which be structured as the followin diagram:
<img src="./json-example-01.svg">

#### Properties

#### Batch upload

### Receipt/Status

## Suggestions/Questions