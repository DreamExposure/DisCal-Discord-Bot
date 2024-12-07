package org.dreamexposure.discal.core.`object`.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import org.dreamexposure.discal.core.`object`.new.model.discal.WebRoleV3Model

@Suppress("DataClassPrivateConstructor")
@Serializable
@Deprecated("Prefer to use WebRoleV3Model instead")
data class WebRole private constructor(
        @Serializable(with = LongAsStringSerializer::class)
        val id: Long,
        val name: String,

        val managed: Boolean,
        @SerialName("control_role")
        val controlRole: Boolean,

        val everyone: Boolean,
) {
    constructor(newModel: WebRoleV3Model): this(
        id = newModel.id.asLong(),
        name = newModel.name,
        managed = newModel.managed,
        controlRole = newModel.controlRole,
        everyone = newModel.everyone
    )
}
