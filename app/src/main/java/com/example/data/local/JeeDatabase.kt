package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        JeeTestEntity::class,
        JeeQuestionEntity::class,
        JeeAttemptEntity::class,
        JeeUserResponseEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(JeeConverters::class)
abstract class JeeDatabase : RoomDatabase() {
    abstract fun jeeDao(): JeeDao

    companion object {
        @Volatile
        private var INSTANCE: JeeDatabase? = null

        fun getDatabase(context: Context): JeeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JeeDatabase::class.java,
                    "jee_cbt_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

