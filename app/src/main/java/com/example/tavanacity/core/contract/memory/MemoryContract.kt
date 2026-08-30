package com.example.tavanacity.core.contract.memory

import com.squareup.moshi.JsonClass

/**
 * Platform-Neutral representation of a stored memory record.
 */
@JsonClass(generateAdapter = true)
data class MemoryRecordDTO(
    val id: String,
    val key: String,
    val value: String,
    val tags: Set<String> = emptySet(),
    val namespace: String = "DEFAULT",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Query object for retrieving cross-platform memory records.
 */
@JsonClass(generateAdapter = true)
data class MemoryQueryDTO(
    val keyPrefix: String? = null,
    val tags: Set<String> = emptySet(),
    val namespace: String? = null,
    val limit: Int = 50,
    val sinceTimestamp: Long? = null
)

/**
 * Result of querying memory.
 */
@JsonClass(generateAdapter = true)
data class MemoryQueryResultDTO(
    val records: List<MemoryRecordDTO>,
    val totalFound: Int,
    val queryDurationMs: Long
)
