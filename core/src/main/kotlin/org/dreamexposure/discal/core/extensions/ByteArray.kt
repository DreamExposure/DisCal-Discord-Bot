package org.dreamexposure.discal.core.extensions

import java.security.MessageDigest

fun ByteArray.sha256Hash(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(this)
    return digest.fold(StringBuilder()) { sb, it ->
        sb.append("%02x".format(it))
    }.toString()
}