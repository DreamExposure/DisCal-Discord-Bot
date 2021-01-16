package org.dreamexposure.discal.core.`object`.network.discal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dreamexposure.discal.core.utils.GlobalConst
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class ConnectedClient(
        @SerialName("index")
        val clientIndex: Int = -1,

        val version: String = GlobalConst.version,

        @SerialName("d4j_version")
        val d4jVersion: String = GlobalConst.d4jVersion,

        @SerialName("guilds")
        val connectedServers: Int = 0,

        @SerialName("keep_alive")
        val lastKeepAlive: Long = System.currentTimeMillis(),

        val uptime: String = "ERROR",

        @SerialName("memory")
        val memUsed: Double = 0.0,

        @Transient
        val ipForRestart: String = "",

        @Transient
        val portForRestart: Int = 0,

        @Transient
        val pid: String = ""
) {
    fun getLastKeepAliveHumanReadable(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        return sdf.format(Date(this.lastKeepAlive))
    }
}
