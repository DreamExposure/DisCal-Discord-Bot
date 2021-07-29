@file:Suppress("unused")

package org.dreamexposure.discal.core.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline val <reified T> T.LOGGER: Logger
    get() = LoggerFactory.getLogger(T::class.java)
