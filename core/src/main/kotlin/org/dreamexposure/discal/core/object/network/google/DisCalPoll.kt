package org.dreamexposure.discal.core.`object`.network.google

data class DisCalPoll(
        val credNumber: Int,
        var interval: Int,
        val expiresIn: Int,
        var remainingSeconds: Int,
        val deviceCode: String,
)
