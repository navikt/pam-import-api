import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val jacksonVersion = "2.17.2"
val javalinVersion = "6.6.0"
val micrometerVersion = "1.12.4"
val postgresqlVersion = "42.7.3"
val pamAnsettelsesKodeverkVersion = "1.18"
val pamStyrkKategoriMapperVersion = "1.20241030-dc26b440"
val htmlSanitizer = "20220608.1"

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
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("org.eclipse.jetty:jetty-util")
    implementation("io.javalin:javalin-micrometer:$javalinVersion")
    implementation("io.micrometer:micrometer-core:$micrometerVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    // implementation("io.prometheus:simpleclient_common:0.16.0")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("com.papertrailapp:logback-syslog4j:1.0.0")
    implementation("com.nimbusds:nimbus-jose-jwt:10.0.1")

    implementation("org.flywaydb:flyway-core:11.5.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.5.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("no.nav.arbeid.pam:pam-ansettelseskodeverk:$pamAnsettelsesKodeverkVersion")
    implementation("no.nav.arbeid.pam:pam-styrk-yrkeskategori-mapper:$pamStyrkKategoriMapperVersion")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:$htmlSanitizer")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.quartz-scheduler:quartz:2.5.0")

    kapt("io.javalin.community.openapi:openapi-annotation-processor:6.6.0")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:6.6.0") // for /openapi route with JSON scheme
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:6.6.0") // for Swagger UI
    implementation("io.javalin.community.openapi:javalin-redoc-plugin:6.6.0") // for Swagger UI


    testImplementation(kotlin("test"))
    // testImplementation("no.nav.security:mock-oauth2-server:2.1.9")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.testcontainers:testcontainers:1.20.5")
    testImplementation("org.testcontainers:postgresql:1.20.5")
    testImplementation("org.testcontainers:kafka:1.20.5")
    testImplementation("org.testcontainers:junit-jupiter:1.20.5")
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("org.mockito:mockito-core:5.18.0")

    testImplementation("io.micronaut.rxjava3:micronaut-rxjava3-http-client:3.7.0")
    testImplementation("io.micronaut:micronaut-jackson-databind:4.8.9")
    testImplementation("net.javacrumbs.json-unit:json-unit:4.1.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.testcontainers:junit-jupiter:1.21.0")
}


// run.jvmArgs("-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote", "-Dlogback.configurationFile=src/test/resources/logback-test.xml", "--enable-preview")
