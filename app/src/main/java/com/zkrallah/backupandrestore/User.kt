package com.zkrallah.backupandrestore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_table")
data class User(
    var name: String
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}
