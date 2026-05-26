package com.dgero.homly.auth.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index("login")])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val login: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis(),
)
