package org.dreamexposure.discal.core.enums.event

import discord4j.rest.util.Color

enum class EventColor(val id: Int, val hex: String, val r: Int, val g: Int, val b: Int) {
    MELROSE(1, "A4BDFC", 164, 189, 252),
    RIPTIDE(2, "7AE7BF", 122, 231, 191),
    MAUVE(3, "DBADFF", 219, 173, 255),
    TANGERINE(4, "FF887C", 255, 136, 124),
    DANDELION(5, "FBD75B", 251, 215, 91),
    MAC_AND_CHEESE(6, "FFB878", 255, 184, 120),
    TURQUOISE(7, "46D6DB", 70, 214, 219),
    MERCURY(8, "E1E1E1", 255, 255, 255),
    BLUE(9, "5484ED", 84, 132, 237),
    GREEN(10, "51B749", 81, 183, 73),
    RED(11, "DC2127", 220, 33, 39),
    NONE(12, "NONE", 56, 138, 237);

    fun asColor(): Color = Color.of(this.r, this.g, this.b)

    companion object {
        fun exists(nameOrHexOrId: String): Boolean {
            values().forEach { c ->
                if (c.name.equals(nameOrHexOrId, true) || c.hex == nameOrHexOrId) {
                    return true
                } else {
                    try {
                        val i = nameOrHexOrId.toInt()
                        if (c.id == i)
                            return true
                    } catch (ignore: NumberFormatException) {
                    }
                }
            }

            return false
        }

        fun exists(id: Int): Boolean {
            return values().any { it.id == id }
        }

        fun fromNameOrHexOrId(nameOrHexOrId: String): EventColor {
            values().forEach { c ->
                if (c.name.equals(nameOrHexOrId, true) || c.hex == nameOrHexOrId) {
                    return c
                } else {
                    try {
                        val i = nameOrHexOrId.toInt()
                        if (c.id == i) return c
                    } catch (ignore: NumberFormatException) {
                    }
                }
            }
            return NONE
        }

        fun fromId(id: Int): EventColor {
            values().forEach { c ->
                if (c.id == id) return c
            }
            return NONE
        }
    }
}
