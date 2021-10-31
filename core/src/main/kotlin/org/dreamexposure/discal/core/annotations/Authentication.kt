package org.dreamexposure.discal.core.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Authentication(
        val access: AccessLevel
) {
    enum class AccessLevel {
        READ,
        WRITE,
        ADMIN,
    }
}
