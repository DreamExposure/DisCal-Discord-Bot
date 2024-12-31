package org.dreamexposure.discal.core.`object`.new

import kotlinx.coroutines.reactor.awaitSingle
import org.dreamexposure.discal.core.config.Config
import org.dreamexposure.discal.core.crypto.AESEncryption
import org.dreamexposure.discal.core.database.CredentialData
import org.dreamexposure.discal.core.extensions.asInstantMilli
import java.time.Instant


@ConsistentCopyVisibility
data class Credential private constructor(
    val credentialNumber: Int,
    var expiresAt: Instant,
    var refreshToken: String,
    var accessToken: String,
) {

    companion object {
        val aes = AESEncryption(Config.SECRET_GOOGLE_CREDENTIAL_KEY.getString())

        suspend operator fun invoke(data: CredentialData) = Credential(
            credentialNumber = data.credentialNumber,
            expiresAt = data.expiresAt.asInstantMilli(),
            refreshToken = aes.decrypt(data.refreshToken).awaitSingle(),
            accessToken = aes.decrypt(data.accessToken).awaitSingle(),
        )
    }
}
