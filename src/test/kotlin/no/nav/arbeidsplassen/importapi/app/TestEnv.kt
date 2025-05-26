package no.nav.arbeidsplassen.importapi.app

val testEnv = mutableMapOf(
    "JWT_SECRET" to "Thisisaverylongsecretandcanonlybeusedintest",
    // "DB_URL" to "jdbc:tc:postgresql:15:///puls?TC_INITSCRIPT=postgres/postgres-init.sql",
    "DB_DRIVER" to "org.postgresql.Driver", //
    "DB_DATABASE" to "test",
    "DB_USERNAME" to "test",
    "DB_PASSWORD" to "test",
)
