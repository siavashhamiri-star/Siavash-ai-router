package com.example.tavanacity.domain.architect.memory

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * An individual memory record stored in the Architect Memory.
 */
data class MemoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val tags: Set<String> = emptySet(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Interface for long-term and working memory within the TAVANA Master Architect system.
 */
interface MemoryInterface {
    suspend fun store(key: String, value: String, tags: Set<String> = emptySet()): MemoryEntry
    suspend fun retrieve(key: String): MemoryEntry?
    suspend fun findByTag(tag: String): List<MemoryEntry>
    suspend fun getAllEntries(): List<MemoryEntry>
    suspend fun clear()
}

/**
 * Fast in-memory implementation of the Architect Memory Interface.
 */
class InMemoryArchitectMemory : MemoryInterface {

    private val mutex = Mutex()
    private val storage = mutableMapOf<String, MemoryEntry>()

    override suspend fun store(key: String, value: String, tags: Set<String>): MemoryEntry = mutex.withLock {
        val entry = MemoryEntry(
            key = key,
            value = value,
            tags = tags,
            timestamp = System.currentTimeMillis()
        )
        storage[key] = entry
        entry
    }

    override suspend fun retrieve(key: String): MemoryEntry? = mutex.withLock {
        storage[key]
    }

    override suspend fun findByTag(tag: String): List<MemoryEntry> = mutex.withLock {
        storage.values.filter { it.tags.contains(tag) }
    }

    override suspend fun getAllEntries(): List<MemoryEntry> = mutex.withLock {
        storage.values.toList()
    }

    override suspend fun clear() = mutex.withLock {
        storage.clear()
    }
}
