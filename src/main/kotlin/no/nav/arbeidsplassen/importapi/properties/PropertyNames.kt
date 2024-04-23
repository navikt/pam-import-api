package no.nav.arbeidsplassen.importapi.properties

enum class PropertyNames(val type: PropertyType) {
    sourceurl(PropertyType.URL),
    applicationdue(PropertyType.TEXT),
    applicationemail(PropertyType.EMAIL),
    applicationmail(PropertyType.TEXT),
    applicationlabel(PropertyType.TEXT),
    applicationurl(PropertyType.URL),
    employerdescription(PropertyType.HTML),
    employerhomepage(PropertyType.URL),
    engagementtype(PropertyType.TEXT),
    extent(PropertyType.TEXT),
    occupation(PropertyType.TEXT),
    salary(PropertyType.INTEGER),
    starttime(PropertyType.TEXT),
    role(PropertyType.TEXT),
    sector(PropertyType.TEXT),
    location(PropertyType.TEXT),
    jobtitle(PropertyType.TEXT),
    keywords(PropertyType.TEXT),
    author(PropertyType.TEXT),
    industry(PropertyType.TEXT),
    workhours(PropertyType.TEXT),
    workday(PropertyType.TEXT),
    facebookpage(PropertyType.URL),
    linkedinpage(PropertyType.URL),
    twitteraddress(PropertyType.URL),
    jobpercentage(PropertyType.TEXT),
    jobarrangement(PropertyType.TEXT),
    remote(PropertyType.TEXT),
    arbeidsplassenoccupation(PropertyType.TEXT),
    euresflagg(PropertyType.TEXT),
    workLanguage(PropertyType.TEXT)
}

enum class PropertyType {
    URL, TEXT, EMAIL, HTML, INTEGER
}
