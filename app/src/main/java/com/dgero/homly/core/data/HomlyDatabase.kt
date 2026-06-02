package com.dgero.homly.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dgero.homly.auth.data.local.UserDao
import com.dgero.homly.auth.data.local.UserEntity
import com.dgero.homly.shopping.data.local.ShoppingItemDao
import com.dgero.homly.shopping.data.local.ShoppingItemEntity

@Database(entities = [UserEntity::class, ShoppingItemEntity::class], version = 2, exportSchema = false)
abstract class HomlyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shoppingItemDao(): ShoppingItemDao
}
