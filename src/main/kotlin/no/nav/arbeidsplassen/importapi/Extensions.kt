package no.nav.arbeidsplassen.importapi

import java.security.MessageDigest


val HEX_CHARS = "0123456789ABCDEF".toCharArray()


fun String.md5Hex(): String {
    val digest = MessageDigest.getInstance("MD5")!!
    val hex = digest.digest(this.toByteArray()).hexBinary()
    digest.reset()
    return hex
}

fun ByteArray.hexBinary(): String {
    val r = StringBuilder(size * 2)
    forEach {
        val i = it.toInt()
        r.append(HEX_CHARS[i shr 4 and 0xF])
        r.append(HEX_CHARS[i and 0xF])
    }
    return r.toString()
}

