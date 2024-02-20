package org.dreamexposure.discal.core.exceptions

class NotFoundException(message: String? = null): Exception(message ?: message ?: "resource not found")
