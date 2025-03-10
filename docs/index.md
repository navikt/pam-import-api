# Job Import API

## Introduction
With our job import API you will be able to upload and publish jobs to [arbeidsplassen.nav.no](https://arbeidsplassen.nav.no). 
Arbeidsplassen is a public job vacancy service provided by NAV, a place where you can search and apply for jobs in Norway. 


## Registration
Before you begin, you must register yourself as a job provider/partner. Please send us an email
with the following information:

* Provider/Company name
* Contact email
* Contact Phone

We will send you your provider identity including a secret key that gives you access to our API

## Supported languages
Ads must be in one of the following languages:
* Norwegian
* Swedish
* Danish
* English

If you wish to post an ad for the same position in multiple languages, they must be included in the same ad, meaning that one ad contains text for both languages.

## Test environment
To test the integration, you can use https://arbeidsplassen-api.ekstern.dev.nav.no/stillingsimport.

## Authentication/Authorization
The API is not publicly open, all requests need to be authenticated using 
the HTTP bearer authorization header. 

Example:
```
POST https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/transfers/{providerId}
Accept: application/x-json-stream
Cache-Control: no-cache
Content-Type: application/x-json-stream
Authorization: Bearer <your secret key>
```

# REST API
This API is designed as a lightweight REST API supporting HTTP requests with JSON.

## Open API
Open api specification is now available [here in test](https://arbeidsplassen-api.ekstern.dev.nav.no/stillingsimport/swagger-ui/), and in
[prod](https://arbeidsplassen-api.nav.no/stillingsimport/swagger-ui/).

## Posting an ad
To upload an ad, use HTTP POST as follow

```
POST https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/transfers/{providerId}
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
  }
}
```

If the request was successful you will get a response with a receipt:

```
{
  "versionId" : 1,
  "status" : "RECEIVED",
  "md5" : "3D5A0C23BC12D58D5865CF3CFC086F11",
  "items" : 1,
  "created" : "2020-04-02T09:47:58.52183",
  "updated" : "2020-04-02T09:47:58.521843"
}
```
## Posting using stream
Post ads in stream by using Content-Type: application/x-json-stream. Ads are separated by a newline "\n" for
example:

```
POST https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/transfers/{providerId}
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
  }
}
{
  "reference": "1214567",
  "positions": 1,
  "contactList": [
    {
      "name": "Ola Normann",
      "title": "Recruiter",
      "email": "ola.normann@test.com",
      "phone": "+47 123 45 678"
    }
  ],
  "locationList": [
    {
      "address": "Sandslimarka 251",
      "postalCode": "5254",
      "country": "NORGE",
      "county": "HORDALAND",
      "municipal": "BERGEN",
      "city": "SANDSLI"
    }
  ],
  "properties": {
    "extent": "Heltid",
    "employerhomepage": "http://www.nesgt.com",
    "occupation": "Ingeniør",
    "applicationdue": "24.02.2019",
    "keywords": "offshore,summer,ingeniør,engineer,oil",
    "engagementtype": "Sesong",
    "employerdescription": "<p>Aker Solutions is a global provider of products, systems and services to the oil and gas industry. Its engineering, design and technology bring discoveries into production and maximize recovery. The company employs approximately 14,000 people in about 20 countries. Go to www.akersolutions.com for more information on our business, people and values.</p>\n",
    "starttime": "01/06/2019",
    "applicationurl": "https://url.to.applicationform/recruitment/hire/input.action?adId=1214567",
    "sector": "Privat"
  },
  "title": "Do you want to showcase your skills and kick start your career?",
  "adText": "<p>Aker Solutions has recently secured new long-term contracts on the Norwegian Continental Shelf, and the project portfolio in Norway is rapidly expanding. We are now recruiting several new employees to strengthen our teams and prepare for future growth.</p>\n<p><strong>Aker Solutions summer intern 2019</strong></p>\n<p>Do you want to showcase your skills and kick start your career?<br />\nAker Solutions&#39; division based in <strong>Bergen</strong> Norway, is now offering the opportunity of becoming a summer intern starting June 2019.</p>\n<p>As an Aker Solutions summer intern, you will participate in project teams that proudly deliver world class projects. You will work together with some of our most experienced and knowledgeable engineers on specific assignments for real life projects.<br />\nYou will become well acquainted with the Aker Solutions organization, expand your network and gain relevant work experience. We can promise you it will be both challenging and fun!</p>\n<p><strong>Responsibilities and tasks:</strong><br />\n<strong>As a summer internship in one of the engineering disciplines in Bergen, you will work in one of our modification projects. The disciplines are as follows:</strong></p>\n<ul><li>Process</li><li>Safety &amp; Environment</li><li>Instrument and Telecommunication</li><li>Electrical Systems</li><li>Mechanical/HVAC</li><li>Piping &amp; Layout</li><li>Structural &amp; Marine</li></ul>\n<p><strong>Qualifications &amp; personal attributes:</strong></p>\n<ul><li>Ability to take initiative and to be flexible</li><li>Good cooperation skills written and verbally, social competency and interest in teamwork</li><li>Good communication skills in Norwegian and English, written and verbally</li><li>Summer internships are open for students at both bachelor and master level</li></ul>\n<p><strong>We offer:</strong></p>\n<p>You will be given the opportunity to work for an ambitious company in a friendly, dynamic and people-oriented working environment. We will encourage you to develop your skills and to share your knowledge with your colleagues and our customers. We&#39;re committed to involving our people in challenging projects and providing them with the support that they need to deliver quality-assured services. This role offers you the chance to participate in the next exciting chapter in the energy industry, working with international customers to understand and deliver on their requirements.</p>\n<ul><li>Competitive compensation and benefits</li><li>Good work/life balance</li><li>Positive work environment with challenging tasks</li><li>Development opportunities</li></ul>\n<p><strong>For more information about the positions, please contact:</strong></p>\n<p>Ina Brattli<br />\nRecruiter<br />\nTlf: &#43;47 41 66 35 43</p>\n<p><strong><strong>Application letter with specification on preferred discipline, cover letter and your CV should be sent by 24.02.2019. Please attach relevant educational transcript of records. Aker Solutions does not provide accommodation or travels during summer internship.</strong></strong>\nNES Advantage Solutions is Aker Solutions&#39; global recruitment services provider responsible for permanent and temporary staffing services.</p>\n",
  "privacy": "SHOW_ALL",
  "published": "2019-02-13T14:05:18.128",
  "expires": "2019-02-24T00:00:00",
  "employer": {
    "reference": "123456",
    "location":
    {
      "address": "Sandslimarka 251",
      "postalCode": "5254",
      "country": "NORGE",
      "county": "VESTLAND",
      "municipal": "BERGEN",
      "city": "SANDSLI"
    },
    "businessName": "Aker Solutions AS",
    "orgnr": "974220954"
  }
}
. 
.
.
```

You will continuously get receipt for each ad like this:

```
HTTP/1.1 200 OK
transfer-encoding: chunked
Date: Fri, 3 Apr 2020 10:37:07 GMT
transfer-encoding: chunked
content-type: application/x-json-stream

{
  "versionId" : 1,
  "status" : "RECEIVED",
  "md5" : "3D5A0C23BC12D58D5865CF3CFC086F11",
  "items" : 1,
  "created" : "2020-04-03T12:37:07.83019",
  "updated" : "2020-04-03T12:37:07.830203"
}{
  "versionId" : 2,
  "status" : "RECEIVED",
  "md5" : "CA41CC694F62E14F72FDE43B66C9821B",
  "items" : 1,
  "created" : "2020-04-03T12:37:07.861917",
  "updated" : "2020-04-03T12:37:07.861923"
}
```

It is important to check the status for each receipt, if it is not "ERROR". 
When using stream, the http status code will always return 200 OK. 


## Posting in batches
You can choose to upload the ads in stream or in batches. If you have a lot of ads, more than thousands everyday, 
we recommend you to upload in batches, You can group the ads in an array and send then in batches. The size of the array
may not go over 100 items.

```
POST https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/transfers/batch/{providerId}
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json
Authorization: Bearer <secret key>

[
    {
      "reference": "140095810",
      "positions": 2,
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
      }
    },
    {
      "reference": "1214567",
      "positions": 1,
      "contactList": [
        {
          "name": "Ola Normann",
          "title": "Recruiter",
          "email": "ola.normann@test.com",
          "phone": "+47 123 45 678"
        }
      ],
      "locationList": [
        {
          "address": "Sandslimarka 251",
          "postalCode": "5254",
          "country": "NORGE",
          "county": "HORDALAND",
          "municipal": "BERGEN",
          "city": "SANDSLI"
        }
      ],
      "properties": {
        "extent": "Heltid",
        "employerhomepage": "http://www.nesgt.com",
        "occupation": "Ingeniør",
        "applicationdue": "24.02.2019",
        "keywords": "offshore,summer,ingeniør,engineer,oil",
        "engagementtype": "Sesong",
        "employerdescription": "<p>Aker Solutions is a global provider of products, systems and services to the oil and gas industry. Its engineering, design and technology bring discoveries into production and maximize recovery. The company employs approximately 14,000 people in about 20 countries. Go to www.akersolutions.com for more information on our business, people and values.</p>\n",
        "starttime": "01/06/2019",
        "applicationurl": "https://url.to.applicationform/recruitment/hire/input.action?adId=1214567",
        "sector": "Privat",
        "workLanguage": "[\"Engelsk\",\"Norsk\"]"
      },
      "title": "Do you want to showcase your skills and kick start your career?",
      "adText": "<p>Aker Solutions has recently secured new long-term contracts on the Norwegian Continental Shelf, and the project portfolio in Norway is rapidly expanding. We are now recruiting several new employees to strengthen our teams and prepare for future growth.</p>\n<p><strong>Aker Solutions summer intern 2019</strong></p>\n<p>Do you want to showcase your skills and kick start your career?<br />\nAker Solutions&#39; division based in <strong>Bergen</strong> Norway, is now offering the opportunity of becoming a summer intern starting June 2019.</p>\n<p>As an Aker Solutions summer intern, you will participate in project teams that proudly deliver world class projects. You will work together with some of our most experienced and knowledgeable engineers on specific assignments for real life projects.<br />\nYou will become well acquainted with the Aker Solutions organization, expand your network and gain relevant work experience. We can promise you it will be both challenging and fun!</p>\n<p><strong>Responsibilities and tasks:</strong><br />\n<strong>As a summer internship in one of the engineering disciplines in Bergen, you will work in one of our modification projects. The disciplines are as follows:</strong></p>\n<ul><li>Process</li><li>Safety &amp; Environment</li><li>Instrument and Telecommunication</li><li>Electrical Systems</li><li>Mechanical/HVAC</li><li>Piping &amp; Layout</li><li>Structural &amp; Marine</li></ul>\n<p><strong>Qualifications &amp; personal attributes:</strong></p>\n<ul><li>Ability to take initiative and to be flexible</li><li>Good cooperation skills written and verbally, social competency and interest in teamwork</li><li>Good communication skills in Norwegian and English, written and verbally</li><li>Summer internships are open for students at both bachelor and master level</li></ul>\n<p><strong>We offer:</strong></p>\n<p>You will be given the opportunity to work for an ambitious company in a friendly, dynamic and people-oriented working environment. We will encourage you to develop your skills and to share your knowledge with your colleagues and our customers. We&#39;re committed to involving our people in challenging projects and providing them with the support that they need to deliver quality-assured services. This role offers you the chance to participate in the next exciting chapter in the energy industry, working with international customers to understand and deliver on their requirements.</p>\n<ul><li>Competitive compensation and benefits</li><li>Good work/life balance</li><li>Positive work environment with challenging tasks</li><li>Development opportunities</li></ul>\n<p><strong>For more information about the positions, please contact:</strong></p>\n<p>Ina Brattli<br />\nRecruiter<br />\nTlf: &#43;47 41 66 35 43</p>\n<p><strong><strong>Application letter with specification on preferred discipline, cover letter and your CV should be sent by 24.02.2019. Please attach relevant educational transcript of records. Aker Solutions does not provide accommodation or travels during summer internship.</strong></strong>\nNES Advantage Solutions is Aker Solutions&#39; global recruitment services provider responsible for permanent and temporary staffing services.</p>\n",
      "privacy": "SHOW_ALL",
      "published": "2019-02-13T14:05:18.128",
      "expires": "2019-02-24T00:00:00",
      "employer": {
        "reference": "123456",
        "location":
        {
          "address": "Sandslimarka 251",
          "postalCode": "5254",
          "country": "NORGE",
          "county": "VESTLAND",
          "municipal": "BERGEN",
          "city": "SANDSLI"
        },
        "businessName": "Aker Solutions AS",
        "orgnr": "974220954"
      }
    }
]

```

response for batch upload:

```
HTTP/1.1 201 Created
Date: Fri, 3 Apr 2020 10:52:26 GMT
content-type: application/json
content-length: 350
connection: keep-alive

{
  "versionId": 1,
  "status": "RECEIVED",
  "md5": "A814439739BD4ED36BAA643B3420002E",
  "items": 2,
  "created": "2020-04-03T12:52:26.836529",
  "updated": "2020-04-03T12:52:26.836541"
}
```
## Speed up batch uploads
To speed up data transfer, it is best to not upload ads that have no changes since last upload. This is to reduce the 
data load to the server, we recommend only uploading last modified ads.  
 
# JSON Structure

The data format is JSON, below is a diagram of the json structure:
<img src="./json-example-01.svg">
You can also download kotlin code for the DTOs 
[here](https://github.com/navikt/pam-import-api/blob/master/src/main/kotlin/no/nav/arbeidsplassen/importapi/dto/TransferDTO.kt)

## Main properties
The main properties are required

|Name           | Type      | Required | Norwegian translation | Description                                                                      | Example   |
|:------------- |:--------- |:---------|:----------------------|:---------------------------------------------------------------------------------|:------------    |
| reference     | String (255)   | Yes      | Referanse | A unique identifier for the jobAd                                                | alfanumber eg. 140095810        |
| positions     | Integer   | Yes      | Antall stilinger | Amount of employment positions avaiable                                          | 1         |
| title         | String (512) | Yes      | Overskrift | The main ad title                                                                | Ønsker du å lede en moderne og veletablert barnehage? |
| adText        | HTML      | Yes      | Annonsetekst | A describing text, html must be welformed. We only support basic html tags       | Nå har du en unik mulighet til å lede en godt faglig og veletablert barnehage. Norlandia Sørumsand barnehage ble etablert i 2006 og har moderne og fleksible oppholdsarealer...|
| published     | DATE      | Yes      | Publiseringsdato | When to publish the ad                                                           | 2019-02-13T00:00:00 |
| expires       | DATE      | Yes      | Sluttdato | Time of expiration                                                               | 2019-02-24T00:00:00 |

## Employer
Arbeidsplassen uses [Brønnøysundregistrene](https://data.brreg.no/enhetsregisteret/oppslag/underenheter)
organization number to identify the employer (orgNr). This is the registered business (Virksomhet), also
called for "underenhet" in Brønnøysundregistrene. You can download all registered [underheter](https://data.brreg.no/enhetsregisteret/api/underenheter/lastned)

If you are not able to send the "underenhet" orgNr, you must specify the employer/business name, and its location. 
For these instances we also recommend you to use the **reference**  field as a unique identifier for the employer, so that the employer can be mapped correctly next time it is used again.   


|Name | Type | Required | Norwegian translation | Description | Example |
|:----|:-----|:---------|:----------------------|:------------|:------|
|orgnr | Integer | Optional | Virksomhetsnummer | BRREG. OrgNumber (only underenhet is supported) | 989012088 (and no whitespace) |
|reference | String (255) | Optional | Referanse | A unique identifier for the employer, you don't need to specify this if you can send virksomhetsnummer | alfanumeric eg. 232151232 |
|businessName | String (255) | Yes | Arbeidsgiver navn | Name of the employer | Sørumsand Barnehage |
|location | Object | Yes | Arbeidsgiver adresse  | Address of the employer | See location table|

Location of Employer

This is the location address of the employer, see also below for work address/location of the job.

|Name | Type | Required | Norwegian translation | Description | Example |
|:----|:-----|:---------|:----------------------|:------------|:------|
|address| String | Optional | Adresse |Street address | Oslo gate 1|
|postalCode| String | Optional | Postnr. | Postal Code | 0566 |
|city | String | Optional | Sted | City | Oslo |
|municipal | String | Optional | Kommune |Municipal | Oslo |
|county | String | Optional | Fylke | County | Oslo |
|country | String | Optional | Land | Country, defaults to Norge | Norge |

## Optional Properties
An ad consists of many properties, they are all optional. However the more content the better the job ad will be. 
Some of these properties are indexed and so will make the ad easier to search for. All supported properties names
are also defined [here](https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/properties/names)
Please specify as much data as possible on the property fields below.
 
| Name                | Type    | Required | Norwegian translation | Description                                                                                                                                | Example                                                                                                     |
|:--------------------|:--------|:---------|:----------------------|:-------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------|
| sourceurl           | URL     | Optional | Visningsside          | Optional viewing the jobad on another page                                                                                                 | eg https://url.to/123456                                                                                    |
| applicationdue      | String  | Optional | Søknadsfrist          | due date/time for job applications                                                                                                         | 22.03.2020                                                                                                  |
| applicationemail    | String  | Optional | Søknadsepost          | applications can be send to this email                                                                                                     | apply-here@job.com                                                                                          |  
| applicationmail     | String  | Optional | Søknadsadresse        | Postal address for applications                                                                                                            | Oslo gate 1, 0431 Oslo, Norge                                                                               |
| applicationlabel    | String  | Optional | Søknadsreferanse      | A tag for labelling applications                                                                                                           | eg. referansenummer 312412                                                                                  |
| applicationurl      | String  | Optional | Søknadslenke          | URL to an online application form                                                                                                          | https://url.to.application/form                                                                             |
| employerdescription | HTML    | Optional | Om arbeidsgiver       | A presentation about the employer, can be in html                                                                                          | I Norlandia barnehagene vil vi være med å skape livslang lyst til lek og læring...                          |
| employerhomepage    | URL     | Optional | Hjemmeside            | URL to employer home page                                                                                                                  | https://url.to.homepage/                                                                                    |
| engagementtype      | String  | Yes      | Ansettelsesform       | type of employee engagement contract                                                                                                       | eg. Fast or Engasjement etc.                                                                                |
| extent              | String  | Optional | Omfang                | Full/Part time                                                                                                                             | Heltid                                                                                                      |
| occupation          | String  | Optional | Yrkestittel           | occupation types, separated by semicolon                                                                                                   | eg. IT Utvikling; Java Utvikler                                                                             |
| salary              | Integer | Optional | Lønn                  | Salary                                                                                                                                     | 800000                                                                                                      |
| starttime           | String  | Optional | Oppstartsdato         | The start date or first day of work                                                                                                        | eg. 24.05.2020                                                                                              |
| sector              | String  | Optional | Sektor                | Public or private sector                                                                                                                   | Offentlig or Privat                                                                                         |
| location            | String  | Optional | Arbeidssted           | the location of work, if address can not be given. also see locationList                                                                   | eg. Offshore                                                                                                |
| jobtitle            | String  | Optional | Stillingstittel       | title of position                                                                                                                          | eg. Kontorsjef                                                                                              |
| keywords            | String  | Optional | Nøkkelord             | searchable tag keywords for the job ad, separated by semicolon                                                                             | eg. 42312341;Java;Kotlin                                                                                    |
| industry            | String  | Optional | Bransje               | what kind of industry category this job belongs to                                                                                         | eg. Bygg og anlegg                                                                                          |
| workhours           | String  | Optional | Arbeidstid            | what part of the day is work hours                                                                                                         | eg. Dagtid. Multiple values can be given by passing a stringified list, eg. `"[\"Dagtid\",\"Kveld\"]"`      |
| workday             | String  | Optional | Arbeidsdager          | Day of work                                                                                                                                | eg. Ukedager. Multiple values can be given by passing a stringified list, eg. `"[\"Ukedager\",\"Lørdag\"]"` |
| workLanguage        | String  | Optional | Arbeidspråk           | Languages used at work                                                                                                                     | eg. Stringified list of languages, eg. `"[\"Engelsk\",\"Norsk\",\"Skandinavisk\",\"Samisk\"]"`              |
| facebookpage        | String  | Optional | Facebook              | facebook share URL                                                                                                                         | https://url.to.facebook/                                                                                    |
| twitteraddress      | String  | Optional | Twitter               | twitter share URL                                                                                                                          | https://url.to.twitter/                                                                                     |
| jobpercentage       | String  | Optional | Stillingsprosent      | if part time job, a percentage can be specified                                                                                            | eg 25%                                                                                                      |
| jobarrangement      | String  | Optional | Arbeidstidsordning    | what type of jobarrangement                                                                                                                | eg. Skift or Vakt                                                                                           |
| remote              | String  | Optional | Hjemmekontor          | If remote or hybrid workplace is possible                                                                                                  | Hjemmekontor, Hybridkontor, Hjemmekontor ikke mulig                                                         |
| euresflagg          | String  | Optional | EURES-flagg           | Marks a particular interest in recruiting workers from other European countries and provides additional visibility in the EURES job portal | true or false                                                                                               |
| privacy             | Enum    | Optional | Synlighet             | Controls what to be shown. Consumers outside of NAV should always use `SHOW_ALL` which is default                                          | SHOW_ALL, INTERNAL_NOT_SHOWN                                                                                |


### Properties that support only valid values 
"sector","remote","extent", "workday", "engagementtype", "jobarrangement", "workhours", "workLanguage" 
only allows for a set of valid values, they are listed [here](https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/properties/values)

### Work Address/Location

Work location is the address/place of work. It is possible to use many work locations, however we don't recommend more 
than three locations per jobAd, and at least one location must be specified. 

|Name | Type | Required | Norwegian translation | Description | Example |
|:----|:-----|:---------|:----------------------|:------------|:------|
| address | String | Optional | Adresse | Street address | Magnus Sørlis veg. 1 |
| postalCode | String | yes      | Postnr. | postal/zip code | 1920 |
| county | String | Optional | Fylke | County | Viken |
| municipal | String | Optional | Kommune | Municipal | Lillestrøm |
| city | String | Optional | Sted | City | Sørumsand |
| country | String | Optional | Land | defaults to Norge | Norge |


### Contact information
It is possible to have many contacts, we recommended at least one contact for each jobAd.

|Name | Type | Required | Norwegian translation | Description | Example |
|:----|:-----|:---------|:----------------------|:------------|:------|
| name | String | Yes | Navn | Contact name | Tom Doe |
| title | String | Optional | Stillingstittel | Job position title | Regionleder |
| email | String | Optional | Epost | Contact email | tom.doe@somewhere.com |
| phone | String | Optional | Telefon | Phone number | +47 010 20 304 | 

## Ad preview and status  
In arbeidsplassen every ad is manually checked, if it doesn't follow 
[NAVs guidelines](https://arbeidsplassen.nav.no/retningslinjer-stillingsannonser) it will be rejected.
You can check your ad if it is approved or rejected using this:

```
GET https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/adminstatus/{providerId}/{reference}
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json
Authorization: Bearer <secret key>

{
  "uuid" : "2afe26f3-9aef-4a12-97eb-20d6c06c513a",
  "status" : "DONE",
  "reference" : "12345",
  "url" : "https://arbeidsplassen.nav.no/stillinger/stilling/2afe26f3-9aef-4a12-97eb-20d6c06c513a",
  "providerId" : 10000,
  "created" : "2020-04-20T13:18:41.04",
  "updated" : "2020-04-20T13:18:41.05",
  "message": null,
  "publishStatus": "INACTIVE"
}
```

The url points to the location of the ad at Arbeidsplassen.no. If the ad is marked as "INACTIVE" it will not be available in the ad search at Arbeidsplassen.no.
If the ad is rejected, the publishStatus will be set to "REJECTED", and the message will tell the reason for rejection.

Possible publishStatus values:
* REJECTED
* INACTIVE
* ACTIVE
* STOPPED
* DELETED

When publishStatus is "REJECTED", the following reasons will be in the message field:
* NOT_APPROVED_BY_LABOUR_INSPECTION
* NO_EMPLOYMENT
* DUPLICATE
* DISCRIMINATING
* FOREIGN_JOB
* UNKNOWN

> If the reason is `NOT_APPROVED_BY_LABOUR_INSPECTION`, it means that the employer does not have authorization by the Norwegian Labor Inspection Authority. For more info, see https://www.arbeidstilsynet.no/godkjenninger/

The administration of an ad might take up to one day, it is not recommended to frequently send request for status.
You should set a delay of 1 hour or more before requesting status again.

## Deactivate/Stopping ad

Ads will follow the expiration date, and will be automatic deactivated when it has expired. 
You can also deactivate or stop an ad by sending a DELETE request:

```
DELETE https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/transfers/{providerId}/{reference}
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json
Authorization: Bearer <secret key>
```

When using DELETE request, ads will be marked as "STOPPED", and no longer be searchable. If you want to fully delete
the ad. You can use ?delete=true:
```
DELETE https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/transfers/{providerId}/{reference}?delete=true
```

NOTE: Ads that are deleted (?delete=true) cannot be updated or created again. 
You should therefore not use ?delete=true if you want to update the ad later.

## Responses

The API use standard HTTP status code when returning. Below are the most common ones:
* 20x Everything was OK
* 30x Redirect or Moved
* 40x Client Error, or Unauthorized
* 50x Server Error 

Depending on the error, the api will also return what type of error it is. 
##### ErrorTypes:
PARSE_ERROR, MISSING_PARAMETER, INVALID_VALUE, CONFLICT, NOT_FOUND, UNKNOWN


# Stats 

## Requesting for ad stats
It is possible to get stats about your ads, you can see how many views and applications you have got for a certain ad.

For example:

```
GET https://arbeidsplassen-api.nav.no/stillingsimport/api/v1/stats/{providerId}?from=yyyy-MM-ddTHH:mm&sort=updated,asc&page=0
Accept: application/json
Cache-Control: no-cache
Content-Type: application/json
Authorization: Bearer <secret key>
"content": [
    {
      "id": 1193,
      "providerId": 10041,
      "uuid": "a8778ad6-315f-4be4-a6f0-65c3d55822c9",
      "reference": "234543",
      "type": "pageviews",
      "total": 44,
      "created": "2021-12-07T08:07:41.094211",
      "updated": "2021-12-08T08:09:30.159884"
    },
    {
      "id": 4587,
      "providerId": 10041,
      "uuid": "ff3109f6-2bcb-422b-b7a1-f8ebe657baa8",
      "reference": "244838",
      "type": "applicationurlclicks",
      "total": 1,
      "created": "2021-12-08T03:08:59.178562",
      "updated": "2021-12-08T03:08:59.217267"
    },
],
  "pageable": {
    "number": 0,
    "sort": {
      "orderBy": [
        {
          "property": "updated",
          "direction": "ASC",
          "ignoreCase": false,
          "ascending": true
        }
      ],
      "sorted": true
    },
    "size": 1000,
    "offset": 0,
    "unpaged": false,
    "sorted": true
  },
  "empty": false,
  "size": 1000,
  "offset": 0,
  "pageNumber": 0,
  "numberOfElements": 2
```

### Supported types:

| type      | norwegian description |
|-----------|-----------------------|
| pageviews | Antall visning        |
| applicationurlclicks | Antall klikk på søknad |

### Limitations
You can not set the "from" parameter date older than 24h. Each request only gives max of 1000 items, 
you will have to scroll through the pages by using the "page" query parameter if "numberofElements" is 1000 or more.
Because the aggregations of the events take a lot of time, there is a delay of 2 hours before data is updated.
It is also not recommended requesting for stats more than 1 time per hour.

# FAQ
1. *Why some ads are rejected, after it has been published?* 
- All ads are automatic published, and then will be manually checked by an admin. If an ad does not follow NAVs guidelines,
it will be rejected and unpublished from Arbeidsplassen.
2. *Why is only underenhet supported for orgnr?*
- “Underenhet” is necessary for all organizations that have employees in Norway, and therefore is required in Arbeidsplassen as an employer.
3. *Is it possible to upload pictures and logos?*
- Currently we don't support picture or logos, maybe in the future.
  
   
# Issues/Suggestions/Questions
If you have any questions/issues or suggestions please feel free to report it as github 
[issues](https://github.com/navikt/pam-import-api/issues)

