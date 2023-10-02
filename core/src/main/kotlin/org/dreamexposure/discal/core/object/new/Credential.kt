package org.dreamexposure.discal.core.`object`.new

import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CredentialData
import org.dreamexposure.discal.core.extensions.asInstantMilli
import java.time.Instant

data class Credential(
    val credentialNumber: Int,
    var expiresAt: Instant,
) {
    lateinit var refreshToken: String
    lateinit var accessToken: String

    constructor(data: CredentialData) : this(
        credentialNumber = data.credentialNumber,
        expiresAt = data.expiresAt.asInstantMilli(),
    ) {
        suspend {
            refreshToken = aes.decrypt(data.refreshToken).awaitSingle()
            accessToken = aes.decrypt(data.accessToken).awaitSingle()
        }
    }

    companion object {
        val aes = AESEncryption(Config.SECRET_GOOGLE_CREDENTIAL_KEY.getString())
    }
}
