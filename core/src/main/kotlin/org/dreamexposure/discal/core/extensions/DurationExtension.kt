package org.dreamexposure.discal.core.extensions

import java.time.Duration

fun Duration.getHumanReadable() = "%d d, %d h, %d m, %d s%n"
    .format(toDays(), toHoursPart(), toMinutesPart(), toSecondsPart())
