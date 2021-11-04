package org.dreamexposure.discal.core.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Authentication(
        val access: AccessLevel
) {
    enum class AccessLevel {
        PUBLIC,
        READ,
        WRITE,
        ADMIN,
    }
}
