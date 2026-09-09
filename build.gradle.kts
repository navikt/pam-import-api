import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val jacksonVersion = "2.22.2"
val javalinVersion = "7.2.3"
val micrometerVersion = "1.17.1"
val postgresqlVersion = "42.7.13"
val pamAnsettelsesKodeverkVersion = "1.18"
val pamStyrkKategoriMapperVersion = "1.20241202-289c80b8"
val htmlSanitizerVersion = "20260313.1"
val quartzVersion = "2.5.2"
val commonsTextVersion = "1.15.0"
val logbackVersion = "1.6.3"
val logbackEncoderVersion = "9.0"
val logbackSyslogVersion = "1.0.0"
val nimbusVersion = "10.9.1"
val flywayVersion = "13.4.0"
val hikariVersion = "7.1.0"
val kafkaClientsVersion = "4.3.1"
val openApiVersion = "7.2.3"

val testContainersVersion = "1.21.4"

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("kapt") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
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
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.mockito:mockito-core:5.23.0")

    testImplementation("net.javacrumbs.json-unit:json-unit:6.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.5.0")
}


// run.jvmArgs("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote", "-Dlogback.configurationFile=src/test/resources/logback-test.xml", "--enable-preview")
