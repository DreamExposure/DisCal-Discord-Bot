package org.dreamexposure.discal.core.`object`.new.security

enum class TokenType(val schema: String) {
    BEARER("Bearer "),
    APP("App "),
    INTERNAL("Int "),
    NONE(""),
}
