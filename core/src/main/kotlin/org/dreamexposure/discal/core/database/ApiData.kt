package org.dreamexposure.discal.core.database

import org.springframework.data.relational.core.mapping.Table

@Table("api")
data class ApiData(
    val userId: String,
    val apiKey: String,
    val blocked: Boolean,
    val timeIssued: Long,
)
