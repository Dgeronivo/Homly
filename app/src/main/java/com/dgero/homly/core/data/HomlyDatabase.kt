package com.dgero.homly.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dgero.homly.auth.data.local.UserDao
import com.dgero.homly.auth.data.local.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class HomlyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
