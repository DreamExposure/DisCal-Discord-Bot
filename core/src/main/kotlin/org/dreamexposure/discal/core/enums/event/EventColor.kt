package org.dreamexposure.discal.core.enums.event

import discord4j.core.`object`.emoji.Emoji
import discord4j.rest.util.Color

enum class EventColor(val id: Int, val hex: String, val r: Int, val g: Int, val b: Int, val emoji: Emoji) {
    MELROSE(1, "A4BDFC", 164, 189, 252, Emoji.of(1465783266270515443, "melrose", false)),
    RIPTIDE(2, "7AE7BF", 122, 231, 191, Emoji.of(1465783271198691641, "riptide", false)),
    MAUVE(3, "DBADFF", 219, 173, 255, Emoji.of(1465783265427460291, "mauve", false)),
    TANGERINE(4, "FF887C", 255, 136, 124, Emoji.of(1465783273631252592, "tangerine", false)),
    DANDELION(5, "FBD75B", 251, 215, 91, Emoji.of(1465783259689521244, "dandelion", false)),
    MAC_AND_CHEESE(6, "FFB878", 255, 184, 120, Emoji.of(1465783264475087021, "mac_and_cheese", false)),
    TURQUOISE(7, "46D6DB", 70, 214, 219, Emoji.of(1465783276978573649, "turquoise", false)),
    MERCURY(8, "E1E1E1", 255, 255, 255, Emoji.of(1465783267381874858, "mercury", false)),
    BLUE(9, "5484ED", 84, 132, 237, Emoji.of(1465783257105698961, "blue", false)),
    GREEN(10, "51B749", 81, 183, 73, Emoji.of(1465783262084333713, "green", false)),
    RED(11, "DC2127", 220, 33, 39, Emoji.of(1465783269877354681, "red", false)),
    NONE(12, "NONE", 56, 138, 237, Emoji.unicode("\u274C"));

    fun asColor(): Color = Color.of(this.r, this.g, this.b)

    companion object {
        fun exists(nameOrHexOrId: String): Boolean {
            entries.forEach { c ->
                if (c.name.equals(nameOrHexOrId, true) || c.hex == nameOrHexOrId) {
                    return true
                } else {
                    try {
                        val i = nameOrHexOrId.toInt()
                        if (c.id == i)
                            return true
                    } catch (_: NumberFormatException) {
                    }
                }
            }

            return false
        }

        fun exists(id: Int): Boolean {
            return entries.any { it.id == id }
        }

        fun fromNameOrHexOrId(nameOrHexOrId: String): EventColor {
            entries.forEach { c ->
                if (c.name.equals(nameOrHexOrId, true) || c.hex == nameOrHexOrId) {
                    return c
                } else {
                    try {
                        val i = nameOrHexOrId.toInt()
                        if (c.id == i) return c
                    } catch (_: NumberFormatException) {
                    }
                }
            }
            return NONE
        }

        fun fromId(id: Int): EventColor {
            entries.forEach { c ->
                if (c.id == id) return c
            }
            return NONE
        }
    }
}
