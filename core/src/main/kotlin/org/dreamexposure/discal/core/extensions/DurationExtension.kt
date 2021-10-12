package org.dreamexposure.discal.core.extensions

import java.time.Duration

fun Duration.getHumanReadable() = "%d days, %d hours, %d minutes, %d seconds%n"
    .format(toDays(), toHoursPart(), toMinutesPart(), toSecondsPart())
