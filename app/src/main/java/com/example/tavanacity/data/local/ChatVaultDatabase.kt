package com.example.tavanacity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VaultMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatVaultDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatVaultDatabase? = null

        fun getInstance(context: Context): ChatVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatVaultDatabase::class.java,
                    "tavana_city_chat_vault.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
