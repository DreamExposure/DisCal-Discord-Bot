package org.dreamexposure.discal.core.`object`.event

import discord4j.common.util.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dreamexposure.discal.core.serializers.SnowflakeAsStringSerializer

@Serializable
data class RsvpData(
        @Serializable(with = SnowflakeAsStringSerializer::class)
        @SerialName("guild_id")
        val guildId: Snowflake,
        @SerialName("event_id")
        val eventId: String,
) {
    @SerialName("event_end")
    var eventEnd: Long = 0

    @SerialName("on_time")
    val goingOnTime: MutableList<String> = mutableListOf()

    @SerialName("late")
    val goingLate: MutableList<String> = mutableListOf()

    @SerialName("not_going")
    val notGoing: MutableList<String> = mutableListOf()
    val undecided: MutableList<String> = mutableListOf()

    //List string stuffs
    fun getGoingOnTimeString(): String {
        val sb = StringBuilder()
        for ((i, s) in this.goingOnTime.withIndex()) {
            if (i == 0) sb.append(s)
            else sb.append(",").append(s)
        }

        return sb.toString()
    }

    fun getGoingLateString(): String {
        val sb = StringBuilder()
        for ((i, s) in this.goingLate.withIndex()) {
            if (i == 0) sb.append(s)
            else sb.append(",").append(s)
        }

        return sb.toString()
    }

    fun getNotGoingString(): String {
        val sb = StringBuilder()
        for ((i, s) in this.notGoing.withIndex()) {
            if (i == 0) sb.append(s)
            else sb.append(",").append(s)
        }

        return sb.toString()
    }

    fun getUndecidedString(): String {
        val sb = StringBuilder()
        for ((i, s) in this.undecided.withIndex()) {
            if (i == 0) sb.append(s)
            else sb.append(",").append(s)
        }

        return sb.toString()
    }

    fun setGoingOnTimeFromString(strList: String) {
        this.goingOnTime += strList.split(",")
    }

    fun setGoingLateFromString(strList: String) {
        this.goingLate += strList.split(",")
    }

    fun setNotGoingFromString(strList: String) {
        this.notGoing += strList.split(",")
    }

    fun setUndecidedFromString(strList: String) {
        this.undecided += strList.split(",")
    }

    //Functions
    fun removeCompletely(userId: String) {
        this.goingOnTime.remove(userId)
        this.goingLate.remove(userId)
        this.notGoing.remove(userId)
        this.undecided.remove(userId)
    }

    fun shouldBeSaved(): Boolean {
        return this.goingOnTime.isNotEmpty()
                || this.goingLate.isNotEmpty()
                || this.notGoing.isNotEmpty()
                || this.undecided.isNotEmpty()
    }
}
