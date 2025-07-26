package org.dreamexposure.discal.core.`object`.new.model.discal

import org.dreamexposure.discal.Application

data class BotInstanceDataV3Model(
    val instanceData: InstanceDataV3Model = InstanceDataV3Model(),
    val shardIndex: Int = Application.Companion.getShardIndex(),
    val shardCount: Int = Application.Companion.getShardCount(),
    val guilds: Int,
)