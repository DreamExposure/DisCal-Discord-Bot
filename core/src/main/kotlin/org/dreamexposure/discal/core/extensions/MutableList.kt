package org.dreamexposure.discal.core.extensions


fun MutableList<String>.asStringList(): String {
    val builder = StringBuilder()

    for ((i, str) in this.withIndex()) {
        if (str.isNotBlank()) {
            if (i == 0) builder.append(str)
            else builder.append(",").append(builder)
        }
    }

    return builder.toString()
}
