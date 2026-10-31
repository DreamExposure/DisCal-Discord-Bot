package org.dreamexposure.discal.core.`object`.new.model.discal

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import discord4j.common.util.Snowflake
import org.dreamexposure.discal.core.serializers.SnowflakeMapper

data class WebChannelV3Model(
    @JsonSerialize(using = SnowflakeMapper.SnowflakeSerializer::class)
    @JsonDeserialize(using = SnowflakeMapper.SnowflakeDeserializer::class)
    val id: Snowflake,
    val name: String,
)
