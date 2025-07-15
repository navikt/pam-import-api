import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val jacksonVersion = "2.17.2"
val javalinVersion = "6.7.0"
val micrometerVersion = "1.12.4"
val postgresqlVersion = "42.7.3"
val pamAnsettelsesKodeverkVersion = "1.18"
val pamStyrkKategoriMapperVersion = "1.20241030-dc26b440"
val htmlSanitizerVersion = "20220608.1"
val quartzVersion = "2.5.0"
val commonsTextVersion = "1.10.0"
val logbackVersion = "1.5.18"
val logbackEncoderVersion = "7.4"
val logbackSyslogVersion = "1.0.0"
val nimbusVersion = "10.0.1"
val flywayVersion = "11.5.0"
val hikariVersion = "5.1.0"
val kafkaClientsVersion = "3.6.1"
val openApiVersion = "6.7.0-1"

val testContainersVersion = "1.21.3"

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("kapt") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.2"
    // id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    application
}

application {
    mainClass.set("no.nav.arbeidsplassen.importapi.ApplicationKt")
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    maven("https://jitpack.io")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    archiveFileName.set("pam-import-api-all.jar")
    mergeServiceFiles()
}

kapt {
    javacOptions {
        option("--enable-preview", "")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("org.eclipse.jetty:jetty-util")
    implementation("io.javalin:javalin-micrometer:$javalinVersion")
    implementation("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    // implementation("io.prometheus:simpleclient_common:0.16.0")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")
    implementation("com.papertrailapp:logback-syslog4j:$logbackSyslogVersion")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")

    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    implementation("org.apache.kafka:kafka-clients:$kafkaClientsVersion")
    implementation("no.nav.arbeid.pam:pam-ansettelseskodeverk:$pamAnsettelsesKodeverkVersion")
    implementation("no.nav.arbeid.pam:pam-styrk-yrkeskategori-mapper:$pamStyrkKategoriMapperVersion")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:$htmlSanitizerVersion")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("org.quartz-scheduler:quartz:$quartzVersion")

    kapt("io.javalin.community.openapi:openapi-annotation-processor:$openApiVersion")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$openApiVersion") // for /openapi route with JSON scheme
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:$openApiVersion") // for Swagger UI
    implementation("io.javalin.community.openapi:javalin-redoc-plugin:$openApiVersion") // for Redoc UI

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("org.mockito:mockito-core:5.18.0")

    testImplementation("io.micronaut.rxjava3:micronaut-rxjava3-http-client:3.8.0")
    testImplementation("io.micronaut:micronaut-jackson-databind:4.8.9")
    testImplementation("net.javacrumbs.json-unit:json-unit:4.1.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}


// run.jvmArgs("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote", "-Dlogback.configurationFile=src/test/resources/logback-test.xml", "--enable-preview")
