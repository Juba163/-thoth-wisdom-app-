package com.thoth.wisdom.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thoth.wisdom.data.dao.PhilosophyDao
import com.thoth.wisdom.data.dao.UserDataDao
import com.thoth.wisdom.data.model.PhilosophyQuestion
import com.thoth.wisdom.data.model.UserData
import com.thoth.wisdom.util.Converters

@Database(
    entities = [
        PhilosophyQuestion::class,
        UserData::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun philosophyDao(): PhilosophyDao
    abstract fun userDataDao(): UserDataDao
}
