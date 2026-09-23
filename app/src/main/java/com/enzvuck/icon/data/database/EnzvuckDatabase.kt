package com.enzvuck.icon.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.enzvuck.icon.data.dao.CustomIconDao
import com.enzvuck.icon.data.dao.IconPackDao
import com.enzvuck.icon.data.entity.CustomIconEntity
import com.enzvuck.icon.data.entity.IconPackEntity
import com.enzvuck.icon.data.entity.IconPackItemEntity

@Database(
    entities = [
        CustomIconEntity::class,
        IconPackEntity::class,
        IconPackItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class EnzvuckDatabase : RoomDatabase() {
    abstract fun customIconDao(): CustomIconDao
    abstract fun iconPackDao(): IconPackDao

    companion object {
        @Volatile
        private var INSTANCE: EnzvuckDatabase? = null

        fun getInstance(context: Context): EnzvuckDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EnzvuckDatabase::class.java,
                    "enzvuck_icons.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
