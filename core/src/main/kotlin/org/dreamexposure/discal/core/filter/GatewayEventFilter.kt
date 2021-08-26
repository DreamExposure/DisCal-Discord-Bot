package org.dreamexposure.discal.core.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import discord4j.discordjson.json.gateway.Opcode
import discord4j.gateway.json.GatewayPayload
import org.slf4j.Marker

@Suppress("unused")
class GatewayEventFilter : TurboFilter() {
    private var include: String? = null
    private var exclude: String? = null
    private var includedEvents: List<String>? = null
    private var excludedEvents: List<String>? = null
    override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?,
                        params: Array<Any>?, t: Throwable?): FilterReply {
        if (params != null && logger!!.name.startsWith("discord4j.gateway.inbound")) {
            for (param in params) {
                if (param is GatewayPayload<*>) {
                    if (Opcode.DISPATCH == param.op) {
                        if (excludedEvents != null) {
                            if (excludedEvents!!.contains(param.type!!)) {
                                return FilterReply.DENY
                            }
                        } else if (includedEvents != null) {
                            if (!includedEvents!!.contains(param.type!!)) {
                                return FilterReply.DENY
                            }
                        }
                    }
                }
            }
        }
        return FilterReply.NEUTRAL
    }

    fun setInclude(include: String?) {
        this.include = include
    }

    fun setExclude(exclude: String?) {
        this.exclude = exclude
    }

    override fun start() {
        if (exclude != null && exclude!!.trim { it <= ' ' }.isNotEmpty()) {
            excludedEvents = mutableListOf(*exclude!!.split("[;,]").toTypedArray())
            super.start()
        } else if (include != null && include!!.trim { it <= ' ' }.isNotEmpty()) {
            includedEvents = mutableListOf(*include!!.split("[;,]").toTypedArray())
            super.start()
        }
    }
}
