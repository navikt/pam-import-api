package no.nav.arbeidsplassen.importapi.config

data class SecretSignatureConfigProperties(
    val secret: String
) {
    companion object {
        fun SecretSignatureConfigProperties(env: Map<String, String>): SecretSignatureConfigProperties =
            SecretSignatureConfigProperties(
                secret = env.nullableVariable("JWT_SECRET") ?: "Thisisaverylongsecretandcanonlybeusedintest",
            )
    }
}
