package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AuthenticationState private constructor(
        @Transient
        val success: Boolean = true,
        @Transient
        val status: Int = 200,
        @SerialName("message")
        val reason: String,
        @Transient
        val fromDiscalNetwork: Boolean = false,
        @Transient
        val readOnly: Boolean = false,
        @Transient
        val keyUsed: String = ""
) {
    constructor(success: Boolean) : this(success, reason = "")

    fun status(status: Int) = this.copy(status = status)

    fun reason(reason: String) = this.copy(reason = reason)

    fun keyUsed(keyUsed: String) = this.copy(keyUsed = keyUsed)

    fun fromDisCalNetwork(from: Boolean) = this.copy(fromDiscalNetwork = from)

    fun readOnly(readOnly: Boolean) = this.copy(readOnly = readOnly)
}
