package org.dreamexposure.discal.core.`object`.web

import org.dreamexposure.discal.core.crypto.KeyGenerator

data class UserAPIAccount(
        val userId: String,
        val APIKey: String = KeyGenerator.csRandomAlphaNumericString(64),
        val blocked: Boolean = false,
        val timeIssued: Long = System.currentTimeMillis()
)
