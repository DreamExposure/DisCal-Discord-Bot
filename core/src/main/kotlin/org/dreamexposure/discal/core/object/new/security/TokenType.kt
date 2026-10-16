package org.dreamexposure.discal.core.`object`.new.security

enum class TokenType(val schema: String) {
    BEARER("Bearer "),
    APP("App "),
    TEMPORARY("TMP "),
    INTERNAL("Int "),
    NONE(""),
}
