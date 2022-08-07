package org.dreamexposure.discal.core.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Authentication(
        val access: AccessLevel,
        val tokenType: TokenType = TokenType.ANY,
) {
    enum class AccessLevel {
        PUBLIC,
        READ,
        WRITE,
        ADMIN,
    }

    enum class TokenType {
        ANY,
        BEARER,
        APPLICATION,
    }
}
