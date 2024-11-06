package org.dreamexposure.discal.core.exceptions

class ApiException(override val message: String? = null, val exception: Exception? = null): Exception(message, exception)
